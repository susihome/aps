package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.ResourceDto;
import com.aps.domain.entity.Resource;
import com.aps.domain.enums.MachineStatus;
import com.aps.service.ResourceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public AjaxResult<List<ResourceDto>> getAllResources(
            @RequestParam(required = false) UUID workshopId,
            @RequestParam(required = false) MachineStatus status) {
        List<Resource> resources = resourceService.getAllResources(workshopId, status);
        return AjaxResult.success(resources.stream().map(ResourceDto::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public AjaxResult<ResourceDto> getResource(@PathVariable UUID id) {
        Resource resource = resourceService.getResourceById(id);
        return AjaxResult.success(ResourceDto.fromEntity(resource));
    }

    @PostMapping
    public AjaxResult<ResourceDto> createResource(@Valid @RequestBody CreateResourceRequest request) {
        Resource resource = resourceService.createResource(
                request.resourceCode(), request.resourceName(), request.resourceType(),
                request.workshopId(), request.tonnage(), request.machineBrand(),
                request.machineModel(), request.maxShotWeight(), request.status(), request.calendarId());
        return AjaxResult.success(ResourceDto.fromEntity(resource));
    }

    @PutMapping("/{id}")
    public AjaxResult<ResourceDto> updateResource(@PathVariable UUID id,
                                                   @RequestBody java.util.Map<String, Object> request) {
        Resource resource = resourceService.updateResource(
                id,
                (String) request.get("resourceName"),
                (String) request.get("resourceType"),
                request.containsKey("workshopId") && request.get("workshopId") != null
                        ? UUID.fromString(request.get("workshopId").toString())
                        : null,
                request.get("tonnage") instanceof Number ? ((Number) request.get("tonnage")).intValue() : null,
                (String) request.get("machineBrand"),
                (String) request.get("machineModel"),
                request.get("maxShotWeight") instanceof Number ? BigDecimal.valueOf(((Number) request.get("maxShotWeight")).doubleValue()) : null,
                request.get("status") != null ? MachineStatus.valueOf(request.get("status").toString()) : null,
                request.containsKey("calendarId") && request.get("calendarId") != null
                        ? UUID.fromString(request.get("calendarId").toString())
                        : null,
                request.get("available") instanceof Boolean ? (Boolean) request.get("available") : null,
                request.containsKey("workshopId"),
                request.containsKey("calendarId")
        );
        return AjaxResult.success(ResourceDto.fromEntity(resource));
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> deleteResource(@PathVariable UUID id) {
        resourceService.deleteResource(id);
        return AjaxResult.success(null);
    }

    // ===== Request Records =====

    public record CreateResourceRequest(
            @NotBlank(message = "资源编码不能为空") String resourceCode,
            @NotBlank(message = "资源名称不能为空") String resourceName,
            String resourceType,
            UUID workshopId,
            Integer tonnage,
            String machineBrand,
            String machineModel,
            BigDecimal maxShotWeight,
            MachineStatus status,
            UUID calendarId) {}

    public record UpdateResourceRequest(
            String resourceName,
            String resourceType,
            UUID workshopId,
            Integer tonnage,
            String machineBrand,
            String machineModel,
            BigDecimal maxShotWeight,
            MachineStatus status,
            UUID calendarId,
            Boolean available) {}
}
