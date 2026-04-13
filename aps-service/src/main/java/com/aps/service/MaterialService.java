package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.Material;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.exception.ValidationException;
import com.aps.service.repository.MaterialMoldBindingRepository;
import com.aps.service.repository.MaterialRepository;
import com.aps.service.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.xml.parsers.SAXParserFactory;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialService {

    private static final List<String> IMPORT_HEADERS = List.of(
            "materialCode", "materialName", "specification", "unit", "enabled", "remark",
            "colorCode", "rawMaterialType", "defaultLotSize", "minLotSize", "maxLotSize",
            "allowDelay", "abcClassification", "productGroup"
    );
    private static final List<String> ERROR_HEADERS = List.of("errorColumn", "errorMessage");
    private static final int EXPORT_PAGE_SIZE = 2_000;
    private static final int IMPORT_BATCH_SIZE = 1_000;
    private static final int MAX_FAILURE_PREVIEW = 100;
    private static final Duration IMPORT_ERROR_FILE_TTL = Duration.ofHours(24);
    private static final Duration MATERIAL_EXPORT_FILE_TTL = Duration.ofHours(24);
    private static final Duration MATERIAL_TEMPLATE_FILE_TTL = Duration.ofHours(24);
    private static final String MATERIAL_IMPORT_ERROR_BUSINESS_TYPE = "MATERIAL_IMPORT_ERROR";
    private static final String MATERIAL_EXPORT_BUSINESS_TYPE = "MATERIAL_EXPORT";
    private static final String MATERIAL_TEMPLATE_BUSINESS_TYPE = "MATERIAL_TEMPLATE";

    private final MaterialRepository materialRepository;
    private final OperationRepository operationRepository;
    private final MaterialMoldBindingRepository materialMoldBindingRepository;
    private final FileObjectService fileObjectService;
    private final ScheduledTaskLockService scheduledTaskLockService;

    @Transactional(readOnly = true)
    public List<Material> getAllMaterials() {
        return materialRepository.findAllByOrderByMaterialCodeAsc();
    }

    @Transactional(readOnly = true)
    public List<Material> searchMaterials(String keyword, int limit) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return materialRepository
                .findByMaterialCodeContainingIgnoreCaseOrMaterialNameContainingIgnoreCaseOrSpecificationContainingIgnoreCaseOrderByMaterialCodeAsc(
                        normalizedKeyword,
                        normalizedKeyword,
                        normalizedKeyword,
                        PageRequest.of(0, safeLimit)
                );
    }

    @Transactional(readOnly = true)
    public Material getMaterialById(UUID id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("物料不存在: " + id));
    }

    @Transactional(readOnly = true)
    public byte[] exportMaterials() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            streamExportMaterialsAsCsv(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new ValidationException("导出CSV模板失败");
        }
    }

    @Transactional(readOnly = true)
    public void streamExportMaterialsAsCsv(OutputStream outputStream) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new java.io.OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');
            writer.write(String.join(",", IMPORT_HEADERS));
            writer.newLine();
            int pageNumber = 0;
            Page<Material> page;
            do {
                page = materialRepository.findAllByOrderByMaterialCodeAsc(PageRequest.of(pageNumber, EXPORT_PAGE_SIZE));
                for (Material material : page.getContent()) {
                    writer.write(toCsvRow(toImportColumns(material)));
                    writer.newLine();
                }
                writer.flush();
                pageNumber++;
            } while (page.hasNext());
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportMaterialsAsExcel() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            streamExportMaterialsAsExcel(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new ValidationException("导出Excel模板失败");
        }
    }

    @Transactional(readOnly = true)
    public ExportFileResult exportMaterialsToFile(String format) {
        String normalizedFormat = format == null ? "xlsx" : format.trim().toLowerCase();
        if ("csv".equals(normalizedFormat)) {
            byte[] content = exportMaterials();
            String fileName = "materials-export.csv";
            String token = fileObjectService.storeTemporaryFile(
                    MATERIAL_EXPORT_BUSINESS_TYPE,
                    fileName,
                    content,
                    MATERIAL_EXPORT_FILE_TTL,
                    "text/csv");
            return new ExportFileResult(fileName, token);
        }

        byte[] content = exportMaterialsAsExcel();
        String fileName = "materials-template.xlsx";
        String token = fileObjectService.storeTemporaryFile(
                MATERIAL_EXPORT_BUSINESS_TYPE,
                fileName,
                content,
                MATERIAL_EXPORT_FILE_TTL,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return new ExportFileResult(fileName, token);
    }

    @Transactional(readOnly = true)
    public ExportFileResult exportTemplateToFile(String format) {
        String normalizedFormat = format == null ? "xlsx" : format.trim().toLowerCase();
        if ("csv".equals(normalizedFormat)) {
            byte[] content = exportMaterials();
            String fileName = "materials-template.csv";
            String token = fileObjectService.storeTemporaryFile(
                    MATERIAL_TEMPLATE_BUSINESS_TYPE,
                    fileName,
                    content,
                    MATERIAL_TEMPLATE_FILE_TTL,
                    "text/csv");
            return new ExportFileResult(fileName, token);
        }

        byte[] content = exportMaterialsAsExcel();
        String fileName = "materials-template.xlsx";
        String token = fileObjectService.storeTemporaryFile(
                MATERIAL_TEMPLATE_BUSINESS_TYPE,
                fileName,
                content,
                MATERIAL_TEMPLATE_FILE_TTL,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return new ExportFileResult(fileName, token);
    }

    @Transactional(readOnly = true)
    public void streamExportMaterialsAsExcel(OutputStream outputStream) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(200)) {
            workbook.setCompressTempFiles(true);
            var sheet = workbook.createSheet("materials");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < IMPORT_HEADERS.size(); i++) {
                headerRow.createCell(i).setCellValue(IMPORT_HEADERS.get(i));
            }
            int rowIndex = 1;
            int pageNumber = 0;
            Page<Material> page;
            do {
                page = materialRepository.findAllByOrderByMaterialCodeAsc(PageRequest.of(pageNumber, EXPORT_PAGE_SIZE));
                for (Material material : page.getContent()) {
                    Row row = sheet.createRow(rowIndex++);
                    List<Object> columns = toImportColumns(material);
                    for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                        writeCell(row, columnIndex, columns.get(columnIndex));
                    }
                }
                sheet.flushRows(200);
                pageNumber++;
            } while (page.hasNext());
            workbook.write(outputStream);
            outputStream.flush();
        }
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_UPDATE, resource = "Material")
    public MaterialImportResult importMaterials(byte[] content) {
        return importMaterials("materials.csv", new ByteArrayInputStream(content));
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_UPDATE, resource = "Material")
    public MaterialImportResult importMaterials(String filename, byte[] content) {
        return importMaterials(filename, new ByteArrayInputStream(content));
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_UPDATE, resource = "Material")
    public MaterialImportResult importMaterials(String filename, InputStream inputStream) {
        try {
            if (filename != null && filename.toLowerCase().endsWith(".xlsx")) {
                return importExcelStream(inputStream);
            }
            return importCsvStream(inputStream);
        } catch (IOException exception) {
            throw new ValidationException("读取导入文件失败");
        }
    }

    private MaterialImportResult importMaterialRecords(List<List<String>> records) {
        if (records.size() <= 1) {
            throw new ValidationException("导入文件不能为空");
        }

        List<String> headers = records.getFirst();
        List<String> normalizedHeaders = normalizeHeader(headers);
        if (!matchesImportHeaders(normalizedHeaders)) {
            throw new ValidationException("导入模板不正确，请先导出最新模板后再导入");
        }

        int createdCount = 0;
        int updatedCount = 0;
        Set<String> importedCodes = new LinkedHashSet<>();
        List<MaterialImportFailure> failures = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            int rowNumber = i + 1;
            List<String> columns = records.get(i);
            if (columns.size() < IMPORT_HEADERS.size()) {
                failures.add(new MaterialImportFailure(rowNumber, "ROW", "列数不正确"));
                continue;
            }
            List<String> importColumns = columns.subList(0, IMPORT_HEADERS.size());
            String codeValue = importColumns.getFirst();
            String materialCode = codeValue == null ? "" : codeValue.trim().toUpperCase();
            if (materialCode.isEmpty()) {
                failures.add(new MaterialImportFailure(rowNumber, "materialCode", "物料编码不能为空"));
                continue;
            }
            if (!importedCodes.add(materialCode)) {
                failures.add(new MaterialImportFailure(rowNumber, "materialCode", "物料编码重复: " + materialCode));
                continue;
            }
            try {
                Material material = materialRepository.findByMaterialCode(materialCode)
                        .orElseGet(Material::new);
                boolean exists = material.getId() != null;
                material.setMaterialCode(materialCode);
                material.setMaterialName(normalizeRequiredText(importColumns.get(1), "物料名称"));
                material.setSpecification(normalizeNullableText(importColumns.get(2)));
                material.setUnit(normalizeNullableText(importColumns.get(3)));
                material.setEnabled(parseRequiredBoolean(importColumns.get(4), "启用状态"));
                material.setRemark(normalizeNullableText(importColumns.get(5)));
                material.setColorCode(normalizeNullableText(importColumns.get(6)));
                material.setRawMaterialType(normalizeNullableText(importColumns.get(7)));
                material.setDefaultLotSize(parseNullableInteger(importColumns.get(8), "默认批量"));
                material.setMinLotSize(parseNullableInteger(importColumns.get(9), "最小批量"));
                material.setMaxLotSize(parseNullableInteger(importColumns.get(10), "最大批量"));
                material.setAllowDelay(parseNullableBoolean(importColumns.get(11), "允许延期"));
                material.setAbcClassification(normalizeNullableText(importColumns.get(12)));
                material.setProductGroup(normalizeNullableText(importColumns.get(13)));
                materialRepository.save(material);
                if (exists) {
                    updatedCount++;
                } else {
                    createdCount++;
                }
            } catch (ResourceConflictException | ValidationException exception) {
                failures.add(toFailure(rowNumber, exception.getMessage()));
            }
        }
        String errorFileToken = failures.isEmpty() ? null : storeImportErrorFile(buildErrorWorkbook(records, failures), "materials-import-errors.csv");
        return new MaterialImportResult(
                Math.max(records.size() - 1, 0),
                createdCount,
                updatedCount,
                failures.size(),
                failures,
                failures.isEmpty() ? null : "materials-import-errors.csv",
                errorFileToken
        );
    }

    private MaterialImportResult importCsvStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String> headerRecord = readCsvRecord(reader);
            if (headerRecord == null) {
                throw new ValidationException("导入文件不能为空");
            }
            List<String> normalizedHeaders = normalizeHeader(headerRecord);
            if (!matchesImportHeaders(normalizedHeaders)) {
                throw new ValidationException("导入模板不正确，请先导出最新模板后再导入");
            }

            int totalCount = 0;
            int createdCount = 0;
            int updatedCount = 0;
            int failedCount = 0;
            List<MaterialImportFailure> failurePreview = new ArrayList<>();
            Set<String> importedCodes = new LinkedHashSet<>();
            Path errorFilePath = createTempErrorFile();
            String errorFileToken = null;
            boolean hasFailures = false;

            try (BufferedWriter errorWriter = Files.newBufferedWriter(errorFilePath, StandardCharsets.UTF_8)) {
                writeErrorCsvHeader(errorWriter);
                List<CsvImportRow> batch = new ArrayList<>(IMPORT_BATCH_SIZE);
                int rowNumber = 1;
                List<String> record;
                while ((record = readCsvRecord(reader)) != null) {
                    rowNumber++;
                    if (record.stream().allMatch(String::isBlank)) {
                        continue;
                    }
                    totalCount++;
                    batch.add(new CsvImportRow(rowNumber, normalizeImportColumns(record)));
                    if (batch.size() >= IMPORT_BATCH_SIZE) {
                        BatchImportResult batchResult = processImportBatch(batch, importedCodes, failurePreview, errorWriter);
                        createdCount += batchResult.createdCount();
                        updatedCount += batchResult.updatedCount();
                        failedCount += batchResult.failedCount();
                        hasFailures = hasFailures || batchResult.failedCount() > 0;
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    BatchImportResult batchResult = processImportBatch(batch, importedCodes, failurePreview, errorWriter);
                    createdCount += batchResult.createdCount();
                    updatedCount += batchResult.updatedCount();
                    failedCount += batchResult.failedCount();
                    hasFailures = hasFailures || batchResult.failedCount() > 0;
                }
            }

            if (hasFailures) {
                errorFileToken = storeImportErrorFile(errorFilePath, "materials-import-errors.csv");
            } else {
                Files.deleteIfExists(errorFilePath);
            }

            return new MaterialImportResult(
                    totalCount,
                    createdCount,
                    updatedCount,
                    failedCount,
                    List.copyOf(failurePreview),
                    hasFailures ? "materials-import-errors.csv" : null,
                    errorFileToken
            );
        }
    }

    private MaterialImportResult importExcelStream(InputStream inputStream) {
        int[] totalCount = {0};
        int[] createdCount = {0};
        int[] updatedCount = {0};
        int[] failedCount = {0};
        int[] rowNumber = {0};
        List<MaterialImportFailure> failurePreview = new ArrayList<>();
        Set<String> importedCodes = new LinkedHashSet<>();
        List<CsvImportRow> batch = new ArrayList<>(IMPORT_BATCH_SIZE);
        Path errorFilePath;

        try {
            errorFilePath = createTempErrorFile();
        } catch (IOException exception) {
            throw new ValidationException("创建错误文件失败");
        }

        try (BufferedWriter errorWriter = Files.newBufferedWriter(errorFilePath, StandardCharsets.UTF_8)) {
            writeErrorCsvHeader(errorWriter);

            try (OPCPackage opcPackage = OPCPackage.open(inputStream)) {
                ReadOnlySharedStringsTable sharedStrings = new ReadOnlySharedStringsTable(opcPackage);
                XSSFReader reader = new XSSFReader(opcPackage);
                StylesTable styles = reader.getStylesTable();
                XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
                if (!sheets.hasNext()) {
                    throw new ValidationException("导入文件不能为空");
                }

                try (InputStream sheetStream = sheets.next()) {
                    XMLReader parser = createSheetParser(styles, sharedStrings, new XSSFSheetXMLHandler.SheetContentsHandler() {
                        private List<String> currentRow;
                        private int currentRowNum;

                        @Override
                        public void startRow(int rowNum) {
                            currentRow = new ArrayList<>();
                            currentRowNum = rowNum;
                        }

                        @Override
                        public void endRow(int rowNum) {
                            rowNumber[0] = currentRowNum + 1;
                            List<String> normalizedRow = normalizeImportColumns(currentRow);
                            if (normalizedRow.stream().allMatch(String::isBlank)) {
                                return;
                            }
                            if (currentRowNum == 0) {
                                List<String> normalizedHeaders = normalizeHeader(currentRow);
                                if (!matchesImportHeaders(normalizedHeaders)) {
                                    throw new ExcelImportAbortException(new ValidationException("导入模板不正确，请先导出最新模板后再导入"));
                                }
                                return;
                            }
                            totalCount[0]++;
                            batch.add(new CsvImportRow(currentRowNum + 1, normalizedRow));
                            if (batch.size() >= IMPORT_BATCH_SIZE) {
                                flushExcelBatch(batch, importedCodes, failurePreview, errorWriter, createdCount, updatedCount, failedCount);
                            }
                        }

                        @Override
                        public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
                            int columnIndex = columnIndexFromCellReference(cellReference);
                            while (currentRow.size() <= columnIndex) {
                                currentRow.add("");
                            }
                            currentRow.set(columnIndex, formattedValue == null ? "" : formattedValue);
                        }

                        @Override
                        public void headerFooter(String text, boolean isHeader, String tagName) {
                            // no-op
                        }
                    });
                    parser.parse(new InputSource(sheetStream));
                }
            } catch (ExcelImportAbortException exception) {
                throw exception.validationException();
            } catch (Exception exception) {
                if (exception instanceof ValidationException validationException) {
                    throw validationException;
                }
                throw new ValidationException("读取Excel导入文件失败");
            }

            if (rowNumber[0] == 0) {
                throw new ValidationException("导入文件不能为空");
            }

            if (!batch.isEmpty()) {
                flushExcelBatch(batch, importedCodes, failurePreview, errorWriter, createdCount, updatedCount, failedCount);
            }
        } catch (IOException exception) {
            throw new ValidationException("读取Excel导入文件失败");
        }

        String errorFileToken = null;
        if (failedCount[0] > 0) {
            errorFileToken = storeImportErrorFile(errorFilePath, "materials-import-errors.csv");
        } else {
            try {
                Files.deleteIfExists(errorFilePath);
            } catch (IOException ignored) {
                // ignore cleanup failure
            }
        }

        return new MaterialImportResult(
                totalCount[0],
                createdCount[0],
                updatedCount[0],
                failedCount[0],
                List.copyOf(failurePreview),
                failedCount[0] > 0 ? "materials-import-errors.csv" : null,
                errorFileToken
        );
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_CREATE, resource = "Material")
    public Material createMaterial(String materialCode, String materialName, String specification,
                                   String unit, Boolean enabled, String remark,
                                   String colorCode, String rawMaterialType,
                                   Integer defaultLotSize, Integer minLotSize, Integer maxLotSize,
                                   Boolean allowDelay, String abcClassification, String productGroup) {
        String normalizedCode = normalizeCode(materialCode, "物料编码");
        if (materialRepository.existsByMaterialCode(normalizedCode)) {
            throw new ResourceConflictException("物料编码已存在: " + normalizedCode);
        }

        Material material = new Material();
        material.setMaterialCode(normalizedCode);
        material.setMaterialName(normalizeRequiredText(materialName, "物料名称"));
        material.setSpecification(normalizeNullableText(specification));
        material.setUnit(normalizeNullableText(unit));
        material.setEnabled(enabled == null || enabled);
        material.setRemark(normalizeNullableText(remark));
        material.setColorCode(normalizeNullableText(colorCode));
        material.setRawMaterialType(normalizeNullableText(rawMaterialType));
        material.setDefaultLotSize(defaultLotSize);
        material.setMinLotSize(minLotSize);
        material.setMaxLotSize(maxLotSize);
        material.setAllowDelay(allowDelay);
        material.setAbcClassification(normalizeNullableText(abcClassification));
        material.setProductGroup(normalizeNullableText(productGroup));
        return materialRepository.save(material);
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_UPDATE, resource = "Material")
    public Material updateMaterial(UUID id, String materialName, String specification,
                                   String unit, Boolean enabled, String remark,
                                   String colorCode, String rawMaterialType,
                                   Integer defaultLotSize, Integer minLotSize, Integer maxLotSize,
                                   Boolean allowDelay, String abcClassification, String productGroup) {
        Material material = getMaterialById(id);
        if (materialName != null) {
            material.setMaterialName(normalizeRequiredText(materialName, "物料名称"));
        }
        if (specification != null) {
            material.setSpecification(normalizeNullableText(specification));
        }
        if (unit != null) {
            material.setUnit(normalizeNullableText(unit));
        }
        if (enabled != null) {
            material.setEnabled(enabled);
        }
        if (remark != null) {
            material.setRemark(normalizeNullableText(remark));
        }
        if (colorCode != null) {
            material.setColorCode(normalizeNullableText(colorCode));
        }
        if (rawMaterialType != null) {
            material.setRawMaterialType(normalizeNullableText(rawMaterialType));
        }
        if (defaultLotSize != null) {
            material.setDefaultLotSize(defaultLotSize);
        }
        if (minLotSize != null) {
            material.setMinLotSize(minLotSize);
        }
        if (maxLotSize != null) {
            material.setMaxLotSize(maxLotSize);
        }
        material.setAllowDelay(allowDelay);
        if (abcClassification != null) {
            material.setAbcClassification(normalizeNullableText(abcClassification));
        }
        if (productGroup != null) {
            material.setProductGroup(normalizeNullableText(productGroup));
        }
        return materialRepository.save(material);
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_DELETE, resource = "Material")
    public void deleteMaterial(UUID id) {
        if (!materialRepository.existsById(id)) {
            throw new ResourceNotFoundException("物料不存在: " + id);
        }
        if (operationRepository.existsByRequiredMaterial_Id(id)) {
            throw new ResourceConflictException("该物料已被工序引用，无法删除");
        }
        if (materialMoldBindingRepository.existsByMaterial_Id(id)) {
            throw new ResourceConflictException("该物料已被物料模具关系引用，无法删除");
        }
        materialRepository.deleteById(id);
    }

    private String normalizeCode(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim().toUpperCase();
        if (normalized.isEmpty()) {
            throw new ResourceConflictException(fieldName + "不能为空");
        }
        return normalized;
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new ResourceConflictException(fieldName + "不能为空");
        }
        return normalized;
    }

    private String normalizeNullableText(String value) {
        String normalized = value == null ? "" : value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private List<String> normalizeHeader(List<String> headers) {
        if (headers.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>(headers.size());
        for (int i = 0; i < headers.size(); i++) {
            String value = headers.get(i) == null ? "" : headers.get(i).trim();
            if (i == 0 && value.startsWith("\uFEFF")) {
                value = value.substring(1);
            }
            normalized.add(value);
        }
        return normalized;
    }

    private boolean matchesImportHeaders(List<String> headers) {
        if (headers.size() < IMPORT_HEADERS.size()) {
            return false;
        }
        if (!headers.subList(0, IMPORT_HEADERS.size()).equals(IMPORT_HEADERS)) {
            return false;
        }
        if (headers.size() == IMPORT_HEADERS.size()) {
            return true;
        }
        return headers.subList(IMPORT_HEADERS.size(), headers.size()).equals(ERROR_HEADERS);
    }

    private List<String> readCsvRecord(BufferedReader reader) throws IOException {
        List<String> record = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean inQuotes = false;
        boolean hasAnyChar = false;

        while (true) {
            int next = reader.read();
            if (next == -1) {
                if (!hasAnyChar && value.isEmpty() && record.isEmpty()) {
                    return null;
                }
                record.add(value.toString());
                return record;
            }

            hasAnyChar = true;
            char currentChar = (char) next;
            if (currentChar == '"') {
                reader.mark(1);
                int following = reader.read();
                if (inQuotes && following == '"') {
                    value.append('"');
                } else {
                    inQuotes = !inQuotes;
                    if (following != -1) {
                        reader.reset();
                    }
                }
            } else if (currentChar == ',' && !inQuotes) {
                record.add(value.toString());
                value.setLength(0);
            } else if ((currentChar == '\n' || currentChar == '\r') && !inQuotes) {
                if (currentChar == '\r') {
                    reader.mark(1);
                    int following = reader.read();
                    if (following != '\n' && following != -1) {
                        reader.reset();
                    }
                }
                record.add(value.toString());
                return record;
            } else {
                value.append(currentChar);
            }
        }
    }

    private List<List<String>> parseExcelRecords(byte[] content) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            var sheet = workbook.getSheetAt(0);
            List<List<String>> records = new ArrayList<>();
            for (Row row : sheet) {
                List<String> values = new ArrayList<>();
                int lastCell = Math.max(row.getLastCellNum(), (short) IMPORT_HEADERS.size());
                for (int cellIndex = 0; cellIndex < lastCell; cellIndex++) {
                    var cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    values.add(readCellAsString(cell));
                }
                boolean allBlank = values.stream().allMatch(String::isBlank);
                if (!allBlank) {
                    records.add(values);
                }
            }
            return records;
        } catch (IOException exception) {
            throw new ValidationException("读取Excel导入文件失败");
        }
    }

    private byte[] buildErrorWorkbook(List<List<String>> records, List<MaterialImportFailure> failures) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BufferedWriter writer = new BufferedWriter(new java.io.OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writeErrorCsvHeader(writer);
            Map<Integer, MaterialImportFailure> failureMap = new HashMap<>();
            for (MaterialImportFailure failure : failures) {
                failureMap.put(failure.rowNumber(), failure);
            }
            for (int rowIndex = 1; rowIndex < records.size(); rowIndex++) {
                int rowNumber = rowIndex + 1;
                MaterialImportFailure failure = failureMap.get(rowNumber);
                if (failure == null) {
                    continue;
                }
                List<String> record = normalizeImportColumns(records.get(rowIndex));
                List<Object> values = new ArrayList<>(record);
                values.add(failure.columnName());
                values.add(failure.message());
                writer.write(toCsvRow(values));
                writer.newLine();
            }
            writer.flush();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new ValidationException("生成错误文件失败");
        }
    }

    private BatchImportResult processImportBatch(List<CsvImportRow> batch, Set<String> importedCodes,
                                                 List<MaterialImportFailure> failurePreview,
                                                 BufferedWriter errorWriter) throws IOException {
        int createdCount = 0;
        int updatedCount = 0;
        int failedCount = 0;
        List<Material> toSave = new ArrayList<>();
        Map<String, CsvImportRow> uniqueRows = new HashMap<>();

        for (CsvImportRow row : batch) {
            List<String> columns = row.columns();
            if (columns.size() < IMPORT_HEADERS.size()) {
                failedCount += appendFailure(row.rowNumber(), columns, new MaterialImportFailure(row.rowNumber(), "ROW", "列数不正确"), failurePreview, errorWriter);
                continue;
            }
            String materialCode = normalizeNullableText(columns.getFirst());
            materialCode = materialCode == null ? "" : materialCode.toUpperCase();
            if (materialCode.isEmpty()) {
                failedCount += appendFailure(row.rowNumber(), columns, new MaterialImportFailure(row.rowNumber(), "materialCode", "物料编码不能为空"), failurePreview, errorWriter);
                continue;
            }
            if (!importedCodes.add(materialCode) || uniqueRows.containsKey(materialCode)) {
                failedCount += appendFailure(row.rowNumber(), columns, new MaterialImportFailure(row.rowNumber(), "materialCode", "物料编码重复: " + materialCode), failurePreview, errorWriter);
                continue;
            }
            uniqueRows.put(materialCode, row.withCode(materialCode));
        }

        Map<String, Material> existingMap = materialRepository.findByMaterialCodeIn(uniqueRows.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Material::getMaterialCode, material -> material));

        for (CsvImportRow row : uniqueRows.values()) {
            List<String> columns = row.columns();
            try {
                Material material = existingMap.getOrDefault(row.materialCode(), new Material());
                boolean exists = material.getId() != null;
                material.setMaterialCode(row.materialCode());
                material.setMaterialName(normalizeRequiredText(columns.get(1), "物料名称"));
                material.setSpecification(normalizeNullableText(columns.get(2)));
                material.setUnit(normalizeNullableText(columns.get(3)));
                material.setEnabled(parseRequiredBoolean(columns.get(4), "启用状态"));
                material.setRemark(normalizeNullableText(columns.get(5)));
                material.setColorCode(normalizeNullableText(columns.get(6)));
                material.setRawMaterialType(normalizeNullableText(columns.get(7)));
                material.setDefaultLotSize(parseNullableInteger(columns.get(8), "默认批量"));
                material.setMinLotSize(parseNullableInteger(columns.get(9), "最小批量"));
                material.setMaxLotSize(parseNullableInteger(columns.get(10), "最大批量"));
                material.setAllowDelay(parseNullableBoolean(columns.get(11), "允许延期"));
                material.setAbcClassification(normalizeNullableText(columns.get(12)));
                material.setProductGroup(normalizeNullableText(columns.get(13)));
                toSave.add(material);
                if (exists) {
                    updatedCount++;
                } else {
                    createdCount++;
                }
            } catch (ResourceConflictException | ValidationException exception) {
                failedCount += appendFailure(row.rowNumber(), columns, toFailure(row.rowNumber(), exception.getMessage()), failurePreview, errorWriter);
            }
        }

        if (!toSave.isEmpty()) {
            materialRepository.saveAll(toSave);
        }
        return new BatchImportResult(createdCount, updatedCount, failedCount);
    }

    private void flushExcelBatch(List<CsvImportRow> batch, Set<String> importedCodes,
                                 List<MaterialImportFailure> failurePreview,
                                 BufferedWriter errorWriter,
                                 int[] createdCount, int[] updatedCount, int[] failedCount) {
        try {
            BatchImportResult result = processImportBatch(batch, importedCodes, failurePreview, errorWriter);
            createdCount[0] += result.createdCount();
            updatedCount[0] += result.updatedCount();
            failedCount[0] += result.failedCount();
            batch.clear();
        } catch (IOException exception) {
            throw new ExcelImportAbortException(new ValidationException("写入错误文件失败"));
        }
    }

    private XMLReader createSheetParser(StylesTable styles, ReadOnlySharedStringsTable sharedStrings,
                                        XSSFSheetXMLHandler.SheetContentsHandler handler) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XMLReader parser = factory.newSAXParser().getXMLReader();
            parser.setContentHandler(new XSSFSheetXMLHandler(styles, null, sharedStrings, handler, null, false));
            return parser;
        } catch (javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException exception) {
            throw new ValidationException("初始化Excel解析器失败");
        }
    }

    private int columnIndexFromCellReference(String cellReference) {
        if (!StringUtils.hasText(cellReference)) {
            return 0;
        }
        int index = 0;
        for (int i = 0; i < cellReference.length(); i++) {
            char current = cellReference.charAt(i);
            if (!Character.isLetter(current)) {
                break;
            }
            index = index * 26 + (Character.toUpperCase(current) - 'A' + 1);
        }
        return Math.max(index - 1, 0);
    }

    private int appendFailure(int rowNumber, List<String> columns, MaterialImportFailure failure,
                              List<MaterialImportFailure> failurePreview,
                              BufferedWriter errorWriter) throws IOException {
        if (failurePreview.size() < MAX_FAILURE_PREVIEW) {
            failurePreview.add(failure);
        }
        List<Object> errorColumns = new ArrayList<>(normalizeImportColumns(columns));
        errorColumns.add(failure.columnName());
        errorColumns.add(failure.message());
        errorWriter.write(toCsvRow(errorColumns));
        errorWriter.newLine();
        return 1;
    }

    private List<String> normalizeImportColumns(List<String> columns) {
        List<String> normalized = new ArrayList<>(IMPORT_HEADERS.size());
        for (int i = 0; i < IMPORT_HEADERS.size(); i++) {
            normalized.add(i < columns.size() ? columns.get(i) : "");
        }
        return normalized;
    }

    private String toCsvRow(List<Object> columns) {
        return columns.stream()
                .map(this::escapeCsvValue)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private String escapeCsvValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private List<Object> toImportColumns(Material material) {
        List<Object> columns = new ArrayList<>(IMPORT_HEADERS.size());
        columns.add(material.getMaterialCode());
        columns.add(material.getMaterialName());
        columns.add(material.getSpecification());
        columns.add(material.getUnit());
        columns.add(material.getEnabled());
        columns.add(material.getRemark());
        columns.add(material.getColorCode());
        columns.add(material.getRawMaterialType());
        columns.add(material.getDefaultLotSize());
        columns.add(material.getMinLotSize());
        columns.add(material.getMaxLotSize());
        columns.add(material.getAllowDelay());
        columns.add(material.getAbcClassification());
        columns.add(material.getProductGroup());
        return columns;
    }

    private void writeErrorCsvHeader(BufferedWriter writer) throws IOException {
        List<Object> headerColumns = new ArrayList<>(IMPORT_HEADERS);
        headerColumns.addAll(ERROR_HEADERS);
        writer.write('\uFEFF');
        writer.write(toCsvRow(headerColumns));
        writer.newLine();
    }

    private void writeCell(Row row, int cellIndex, Object value) {
        if (value == null) {
            return;
        }
        row.createCell(cellIndex).setCellValue(String.valueOf(value));
    }

    private String readCellAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return Boolean.toString(cell.getBooleanCellValue());
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double numericValue = cell.getNumericCellValue();
            if (numericValue == Math.rint(numericValue)) {
                return Long.toString((long) numericValue);
            }
            return Double.toString(numericValue);
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    private MaterialImportFailure toFailure(int rowNumber, String message) {
        String columnName = "ROW";
        if (message.contains("物料编码")) {
            columnName = "materialCode";
        } else if (message.contains("物料名称")) {
            columnName = "materialName";
        } else if (message.contains("启用状态")) {
            columnName = "enabled";
        } else if (message.contains("默认批量")) {
            columnName = "defaultLotSize";
        } else if (message.contains("最小批量")) {
            columnName = "minLotSize";
        } else if (message.contains("最大批量")) {
            columnName = "maxLotSize";
        } else if (message.contains("允许延期")) {
            columnName = "allowDelay";
        }
        return new MaterialImportFailure(rowNumber, columnName, message);
    }

    private Path createTempErrorFile() throws IOException {
        return Files.createTempFile("aps-material-import-errors-", ".csv");
    }

    private String storeImportErrorFile(byte[] content, String fileName) {
        return fileObjectService.storeTemporaryFile(
                MATERIAL_IMPORT_ERROR_BUSINESS_TYPE,
                fileName,
                content,
                IMPORT_ERROR_FILE_TTL,
                "text/csv");
    }

    private String storeImportErrorFile(Path path, String fileName) {
        try {
            return fileObjectService.storeTemporaryFile(
                    MATERIAL_IMPORT_ERROR_BUSINESS_TYPE,
                    fileName,
                    Files.readAllBytes(path),
                    IMPORT_ERROR_FILE_TTL,
                    "text/csv");
        } catch (IOException exception) {
            throw new ValidationException("保存错误文件失败");
        } finally {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // ignore cleanup failure
            }
        }
    }

    public byte[] loadImportErrorFile(String token) {
        return fileObjectService.loadTemporaryFile(token, MATERIAL_IMPORT_ERROR_BUSINESS_TYPE).content();
    }

    public String getImportErrorFileName(String token) {
        return fileObjectService.loadTemporaryFile(token, MATERIAL_IMPORT_ERROR_BUSINESS_TYPE).fileName();
    }

    public byte[] loadExportFile(String token) {
        return fileObjectService.loadTemporaryFile(token, MATERIAL_EXPORT_BUSINESS_TYPE).content();
    }

    public String getExportFileName(String token) {
        return fileObjectService.loadTemporaryFile(token, MATERIAL_EXPORT_BUSINESS_TYPE).fileName();
    }

    public byte[] loadTemplateFile(String token) {
        return fileObjectService.loadTemporaryFile(token, MATERIAL_TEMPLATE_BUSINESS_TYPE).content();
    }

    public String getTemplateFileName(String token) {
        return fileObjectService.loadTemporaryFile(token, MATERIAL_TEMPLATE_BUSINESS_TYPE).fileName();
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredImportErrorFiles() {
        if (!scheduledTaskLockService.tryLock("material:file-cleanup", "material-file-cleanup-scheduler")) {
            return;
        }
        try {
            fileObjectService.cleanupExpiredFiles(MATERIAL_IMPORT_ERROR_BUSINESS_TYPE);
            fileObjectService.cleanupExpiredFiles(MATERIAL_EXPORT_BUSINESS_TYPE);
            fileObjectService.cleanupExpiredFiles(MATERIAL_TEMPLATE_BUSINESS_TYPE);
        } finally {
            scheduledTaskLockService.unlock("material:file-cleanup", "material-file-cleanup-scheduler");
        }
    }

    private Boolean parseRequiredBoolean(String value, String fieldName) {
        Boolean parsed = parseNullableBoolean(value, fieldName);
        if (parsed == null) {
            throw new ValidationException(fieldName + "不能为空");
        }
        return parsed;
    }

    private Boolean parseNullableBoolean(String value, String fieldName) {
        String normalized = normalizeNullableText(value);
        if (normalized == null) {
            return null;
        }
        if ("true".equalsIgnoreCase(normalized) || "false".equalsIgnoreCase(normalized)) {
            return Boolean.parseBoolean(normalized);
        }
        throw new ValidationException(fieldName + "必须为true或false");
    }

    private Integer parseNullableInteger(String value, String fieldName) {
        String normalized = normalizeNullableText(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Integer.valueOf(normalized);
        } catch (NumberFormatException exception) {
            throw new ValidationException(fieldName + "必须为整数");
        }
    }

    public record MaterialImportResult(
            int totalCount,
            int createdCount,
            int updatedCount,
            int failedCount,
            List<MaterialImportFailure> failures,
            String errorFileName,
            String errorFileToken
    ) {
    }

    public record MaterialImportFailure(int rowNumber, String columnName, String message) {
    }

    public record ExportFileResult(String fileName, String fileToken) {
    }

    private record CsvImportRow(int rowNumber, List<String> columns, String materialCode) {
        private CsvImportRow(int rowNumber, List<String> columns) {
            this(rowNumber, columns, null);
        }

        private CsvImportRow withCode(String code) {
            return new CsvImportRow(rowNumber, columns, code);
        }
    }

    private record BatchImportResult(int createdCount, int updatedCount, int failedCount) {
    }

    private static final class ExcelImportAbortException extends RuntimeException {
        private final ValidationException validationException;

        private ExcelImportAbortException(ValidationException validationException) {
            super(validationException);
            this.validationException = validationException;
        }

        private ValidationException validationException() {
            return validationException;
        }
    }

}
