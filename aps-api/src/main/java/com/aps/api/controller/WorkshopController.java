package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.FactoryCalendarDto;
import com.aps.api.dto.WorkshopDto;
import com.aps.domain.entity.FactoryCalendar;
import com.aps.domain.entity.Workshop;
import com.aps.service.WorkshopService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workshops")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WorkshopController {

    private final WorkshopService workshopService;

    @GetMapping
    public AjaxResult<List<WorkshopDto>> getAllWorkshops() {
        List<Workshop> workshops = workshopService.getAllWorkshops();
        return AjaxResult.success(workshops.stream().map(WorkshopDto::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public AjaxResult<WorkshopDto> getWorkshop(@PathVariable UUID id) {
        Workshop workshop = workshopService.getWorkshopById(id);
        return AjaxResult.success(WorkshopDto.fromEntity(workshop));
    }

    @PostMapping
    public AjaxResult<WorkshopDto> createWorkshop(@Valid @RequestBody CreateWorkshopRequest request) {
        Workshop workshop = workshopService.createWorkshop(
                request.code(), request.name(), request.calendarId(),
                request.managerName(), request.sortOrder(), request.description());
        return AjaxResult.success(WorkshopDto.fromEntity(workshop));
    }

    @PutMapping("/{id}")
    public AjaxResult<WorkshopDto> updateWorkshop(@PathVariable UUID id,
                                                   @RequestBody java.util.Map<String, Object> request) {
        Workshop workshop = workshopService.updateWorkshop(
                id,
                (String) request.get("name"),
                request.containsKey("calendarId") && request.get("calendarId") != null
                        ? UUID.fromString(request.get("calendarId").toString())
                        : null,
                (String) request.get("managerName"),
                request.get("sortOrder") instanceof Number ? ((Number) request.get("sortOrder")).intValue() : null,
                (String) request.get("description"),
                request.get("enabled") instanceof Boolean ? (Boolean) request.get("enabled") : null,
                request.containsKey("calendarId")
        );
        return AjaxResult.success(WorkshopDto.fromEntity(workshop));
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> deleteWorkshop(@PathVariable UUID id) {
        workshopService.deleteWorkshop(id);
        return AjaxResult.success(null);
    }

    @GetMapping("/{id}/effective-calendar")
    public AjaxResult<FactoryCalendarDto> getEffectiveCalendar(@PathVariable UUID id) {
        FactoryCalendar calendar = workshopService.getEffectiveCalendar(id);
        if (calendar == null) {
            return AjaxResult.success(null);
        }
        return AjaxResult.success(FactoryCalendarDto.fromEntity(calendar));
    }

    // ===== Request Records =====

    public record CreateWorkshopRequest(
            @NotBlank(message = "车间编码不能为空") String code,
            @NotBlank(message = "车间名称不能为空") String name,
            UUID calendarId,
            String managerName,
            Integer sortOrder,
            String description) {}

    public record UpdateWorkshopRequest(
            String name,
            UUID calendarId,
            String managerName,
            Integer sortOrder,
            String description,
            Boolean enabled) {}
}
