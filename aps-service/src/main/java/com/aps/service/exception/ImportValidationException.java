package com.aps.service.exception;

import com.aps.service.MaterialService;

import java.util.List;

public class ImportValidationException extends BusinessException {

    private final List<MaterialService.MaterialImportFailure> failures;

    public ImportValidationException(String message, List<MaterialService.MaterialImportFailure> failures) {
        super(400, message);
        this.failures = List.copyOf(failures);
    }

    public List<MaterialService.MaterialImportFailure> getFailures() {
        return failures;  // already an unmodifiable copy
    }
}
