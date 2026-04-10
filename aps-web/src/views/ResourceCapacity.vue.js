/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { computed, onMounted, ref } from 'vue';
import { msgError, msgSuccess, msgWarning, extractErrorMsg } from '@/utils/message';
import { resourceCapacityApi } from '@/api/resourceCapacity';
const loadingResources = ref(false);
const loadingMonth = ref(false);
const batchSaving = ref(false);
const resources = ref([]);
const selectedResource = ref(null);
const monthData = ref(null);
const searchKeyword = ref('');
const savingDate = ref(null);
const selectedDates = ref([]);
const batchDialogVisible = ref(false);
const batchForm = ref(createBatchForm());
const monthTableRef = ref(null);
const now = new Date();
const currentYear = ref(now.getFullYear());
const currentMonth = ref(now.getMonth() + 1);
const dateTypeLabelMap = {
    WORKDAY: '工作日',
    RESTDAY: '休息日',
    HOLIDAY: '节假日'
};
const dateTypeTagMap = {
    WORKDAY: 'success',
    RESTDAY: 'info',
    HOLIDAY: 'danger'
};
const filteredResources = computed(() => {
    const keyword = searchKeyword.value.trim().toLowerCase();
    if (!keyword)
        return resources.value;
    return resources.value.filter(item => {
        const resourceCode = item.resourceCode?.toLowerCase() ?? '';
        const resourceName = item.resourceName?.toLowerCase() ?? '';
        return resourceCode.includes(keyword) || resourceName.includes(keyword);
    });
});
const selectedDatesText = computed(() => {
    if (!selectedDates.value.length) {
        return '未选择日期';
    }
    return selectedDates.value.join('，');
});
onMounted(async () => {
    await loadResources();
});
function createBatchForm() {
    return {
        updateShiftMinutesOverride: false,
        shiftMinutesOverride: null,
        updateUtilizationRate: false,
        utilizationRate: null,
        remark: null,
        remarkAction: 'ignore'
    };
}
function normalizeRemark(remark) {
    if (remark == null) {
        return null;
    }
    const trimmed = remark.trim();
    return trimmed ? trimmed : null;
}
function normalizeShiftMinutes(value) {
    if (value == null) {
        return value;
    }
    return Number.isFinite(value) ? value : undefined;
}
function minutesToHours(value) {
    if (value == null) {
        return value;
    }
    return Number((value / 60).toFixed(2));
}
function hoursToMinutes(value) {
    if (value == null) {
        return value;
    }
    if (!Number.isFinite(value)) {
        return undefined;
    }
    return Math.round(value * 60);
}
function updateRowShiftHours(row, value) {
    row.shiftMinutesOverride = hoursToMinutes(value) ?? null;
}
function updateBatchShiftHours(value) {
    batchForm.value.shiftMinutesOverride = hoursToMinutes(value) ?? null;
}
function normalizeUtilizationRate(value) {
    if (value == null) {
        return value;
    }
    return Number.isFinite(value) ? value : undefined;
}
function validateShiftMinutes(value) {
    if (value == null) {
        return true;
    }
    if (!Number.isFinite(value)) {
        msgWarning('班次小时必须是有效数字');
        return false;
    }
    if (value < 0) {
        msgWarning('班次小时不能小于0');
        return false;
    }
    return true;
}
function validateUtilizationRate(value) {
    if (value == null) {
        msgWarning('利用率不能为空');
        return false;
    }
    if (!Number.isFinite(value)) {
        msgWarning('利用率必须是有效数字');
        return false;
    }
    if (value < 0 || value > 1) {
        msgWarning('利用率必须在0到1之间');
        return false;
    }
    return true;
}
function buildSingleDayPayload(row) {
    const shiftMinutesOverride = normalizeShiftMinutes(row.shiftMinutesOverride);
    const utilizationRate = normalizeUtilizationRate(row.utilizationRate);
    const remark = normalizeRemark(row.remark);
    if (!validateShiftMinutes(shiftMinutesOverride) || !validateUtilizationRate(utilizationRate) || utilizationRate == null) {
        return null;
    }
    return {
        ...(shiftMinutesOverride === null ? { shiftMinutesOverride: null } : shiftMinutesOverride !== undefined ? { shiftMinutesOverride } : {}),
        utilizationRate,
        remark
    };
}
function buildBatchPayload(form) {
    const payload = {};
    if (form.updateShiftMinutesOverride) {
        const shiftMinutesOverride = normalizeShiftMinutes(form.shiftMinutesOverride);
        if (!validateShiftMinutes(shiftMinutesOverride)) {
            return null;
        }
        payload.shiftMinutesOverride = shiftMinutesOverride ?? null;
    }
    if (form.updateUtilizationRate) {
        const utilizationRate = normalizeUtilizationRate(form.utilizationRate);
        if (!validateUtilizationRate(utilizationRate) || utilizationRate == null) {
            return null;
        }
        payload.utilizationRate = utilizationRate;
    }
    if (form.remarkAction === 'set') {
        const remark = normalizeRemark(form.remark);
        if (remark == null) {
            msgWarning('请输入要设置的备注');
            return null;
        }
        payload.remark = remark;
    }
    if (form.remarkAction === 'clear') {
        payload.remark = null;
    }
    if (!form.updateShiftMinutesOverride && !form.updateUtilizationRate && form.remarkAction === 'ignore') {
        msgWarning('请至少填写一个需要修改的字段');
        return null;
    }
    return payload;
}
async function loadResources() {
    loadingResources.value = true;
    try {
        resources.value = await resourceCapacityApi.getResources();
        if (resources.value.length > 0) {
            await selectResource(resources.value[0]);
        }
    }
    catch (error) {
        msgError(extractErrorMsg(error, '加载设备列表失败'));
    }
    finally {
        loadingResources.value = false;
    }
}
async function selectResource(resource) {
    selectedResource.value = resource;
    clearSelection();
    batchDialogVisible.value = false;
    batchForm.value = createBatchForm();
    await loadMonthData();
}
async function loadMonthData() {
    if (!selectedResource.value)
        return false;
    loadingMonth.value = true;
    try {
        monthData.value = await resourceCapacityApi.getMonthCapacity({
            resourceId: selectedResource.value.id,
            year: currentYear.value,
            month: currentMonth.value
        });
        return true;
    }
    catch (error) {
        monthData.value = null;
        msgError(extractErrorMsg(error, '加载设备日产能失败'));
        return false;
    }
    finally {
        loadingMonth.value = false;
    }
}
async function changeMonth(offset) {
    const previousYear = currentYear.value;
    const previousMonth = currentMonth.value;
    const next = new Date(currentYear.value, currentMonth.value - 1 + offset, 1);
    currentYear.value = next.getFullYear();
    currentMonth.value = next.getMonth() + 1;
    clearSelection();
    batchDialogVisible.value = false;
    const loaded = await loadMonthData();
    if (!loaded) {
        currentYear.value = previousYear;
        currentMonth.value = previousMonth;
    }
}
function handleSelectionChange(rows) {
    selectedDates.value = rows.map(row => row.date);
}
function handleRowClick(row, _column, event) {
    const target = event.target;
    if (target?.closest('.el-input, .el-input-number, .el-button, .el-checkbox, textarea, input, button')) {
        return;
    }
    const isSelected = selectedDates.value.includes(row.date);
    monthTableRef.value?.toggleRowSelection(row, !isSelected);
}
function clearSelection() {
    selectedDates.value = [];
    monthTableRef.value?.clearSelection();
}
function openBatchDialog() {
    if (!selectedDates.value.length) {
        msgWarning('请先选择日期');
        return;
    }
    batchForm.value = createBatchForm();
    batchDialogVisible.value = true;
}
async function saveDay(row) {
    if (!selectedResource.value)
        return;
    const payload = buildSingleDayPayload(row);
    if (!payload || payload.utilizationRate == null) {
        return;
    }
    savingDate.value = row.date;
    try {
        const saved = await resourceCapacityApi.updateDay(selectedResource.value.id, row.date, payload);
        updateDayRow(saved);
        recomputeSummary();
        msgSuccess('保存成功');
    }
    catch (error) {
        msgError(extractErrorMsg(error, '保存失败'));
    }
    finally {
        savingDate.value = null;
    }
}
async function saveBatchDays() {
    if (!selectedResource.value)
        return;
    if (!selectedDates.value.length) {
        msgWarning('请先选择日期');
        return;
    }
    const payload = buildBatchPayload(batchForm.value);
    if (!payload) {
        return;
    }
    batchSaving.value = true;
    try {
        await resourceCapacityApi.batchUpdateDays(selectedResource.value.id, {
            dates: [...selectedDates.value],
            ...payload
        });
        batchDialogVisible.value = false;
        batchForm.value = createBatchForm();
        clearSelection();
        const refreshed = await loadMonthData();
        msgSuccess('批量保存成功');
        if (!refreshed) {
            msgWarning('数据已保存，列表刷新失败，请手动重新加载');
        }
    }
    catch (error) {
        msgError(extractErrorMsg(error, '批量保存失败'));
    }
    finally {
        batchSaving.value = false;
    }
}
function updateDayRow(saved) {
    if (!monthData.value) {
        return;
    }
    monthData.value = {
        ...monthData.value,
        days: monthData.value.days.map(item => item.date === saved.date ? saved : item)
    };
}
function recomputeSummary() {
    if (!monthData.value)
        return;
    const days = monthData.value.days.map(item => calculateCapacity(item));
    monthData.value = {
        ...monthData.value,
        days,
        totalDefaultShiftMinutes: days.reduce((sum, item) => sum + item.defaultShiftMinutes, 0),
        totalEffectiveShiftMinutes: days.reduce((sum, item) => sum + item.effectiveShiftMinutes, 0),
        totalAvailableCapacityMinutes: days.reduce((sum, item) => sum + item.availableCapacityMinutes, 0),
        averageUtilizationRate: days.length
            ? days.reduce((sum, item) => sum + Number(item.utilizationRate || 0), 0) / days.length
            : 0
    };
}
function computeCapacity(row) {
    return calculateCapacity(row).availableCapacityMinutes;
}
function calculateCapacity(row) {
    const shiftMinutes = row.shiftMinutesOverride ?? row.defaultShiftMinutes;
    return {
        ...row,
        effectiveShiftMinutes: shiftMinutes,
        availableCapacityMinutes: Math.round(shiftMinutes * Number(row.utilizationRate || 0))
    };
}
function formatHours(minutes) {
    return (minutes / 60).toFixed(2);
}
function formatPercent(rate) {
    return `${(Number(rate || 0) * 100).toFixed(2)}%`;
}
function parseLocalDate(dateText) {
    const [yearText, monthText, dayText] = dateText.split('-');
    const year = Number(yearText);
    const month = Number(monthText);
    const day = Number(dayText);
    return new Date(year, month - 1, day);
}
function formatWeekday(dateText) {
    const date = parseLocalDate(dateText);
    return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()];
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
/** @type {__VLS_StyleScopedClasses['resource-card']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-header']} */ ;
/** @type {__VLS_StyleScopedClasses['el-table']} */ ;
/** @type {__VLS_StyleScopedClasses['el-table']} */ ;
/** @type {__VLS_StyleScopedClasses['el-table']} */ ;
/** @type {__VLS_StyleScopedClasses['cell']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-card']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-value']} */ ;
/** @type {__VLS_StyleScopedClasses['resource-list']} */ ;
/** @type {__VLS_StyleScopedClasses['content-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-toolbar__actions']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "resource-capacity-page" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "page-header" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "content-wrapper" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "left-panel" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "panel-title" },
});
const __VLS_0 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    modelValue: (__VLS_ctx.searchKeyword),
    placeholder: "搜索设备编码/名称",
    clearable: true,
    ...{ class: "search-input" },
}));
const __VLS_2 = __VLS_1({
    modelValue: (__VLS_ctx.searchKeyword),
    placeholder: "搜索设备编码/名称",
    clearable: true,
    ...{ class: "search-input" },
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "resource-list" },
});
__VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.loadingResources) }, null, null);
for (const [resource] of __VLS_getVForSourceType((__VLS_ctx.filteredResources))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.selectResource(resource);
            } },
        key: (resource.id),
        ...{ class: (['resource-card', { active: __VLS_ctx.selectedResource?.id === resource.id }]) },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "resource-card-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "resource-name" },
    });
    (resource.resourceName);
    const __VLS_4 = {}.ElTag;
    /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
    // @ts-ignore
    const __VLS_5 = __VLS_asFunctionalComponent(__VLS_4, new __VLS_4({
        size: "small",
    }));
    const __VLS_6 = __VLS_5({
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_5));
    __VLS_7.slots.default;
    (resource.resourceCode);
    var __VLS_7;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "resource-meta" },
    });
    (resource.resourceType || '-');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "resource-meta" },
    });
    (resource.workshopName || '-');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "resource-meta" },
    });
    (resource.calendarName || '继承车间/默认');
}
if (!__VLS_ctx.filteredResources.length) {
    const __VLS_8 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({
        description: "暂无设备",
    }));
    const __VLS_10 = __VLS_9({
        description: "暂无设备",
    }, ...__VLS_functionalComponentArgsRest(__VLS_9));
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "right-panel" },
});
if (__VLS_ctx.selectedResource && __VLS_ctx.monthData) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
    (__VLS_ctx.monthData.resourceName);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-subtitle" },
    });
    (__VLS_ctx.monthData.resourceCode);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "separator" },
    });
    (__VLS_ctx.monthData.workshopName || '-');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "separator" },
    });
    (__VLS_ctx.monthData.calendarName || '-');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-actions" },
    });
    const __VLS_12 = {}.ElButtonGroup;
    /** @type {[typeof __VLS_components.ElButtonGroup, typeof __VLS_components.elButtonGroup, typeof __VLS_components.ElButtonGroup, typeof __VLS_components.elButtonGroup, ]} */ ;
    // @ts-ignore
    const __VLS_13 = __VLS_asFunctionalComponent(__VLS_12, new __VLS_12({}));
    const __VLS_14 = __VLS_13({}, ...__VLS_functionalComponentArgsRest(__VLS_13));
    __VLS_15.slots.default;
    const __VLS_16 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_17 = __VLS_asFunctionalComponent(__VLS_16, new __VLS_16({
        ...{ 'onClick': {} },
    }));
    const __VLS_18 = __VLS_17({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_17));
    let __VLS_20;
    let __VLS_21;
    let __VLS_22;
    const __VLS_23 = {
        onClick: (...[$event]) => {
            if (!(__VLS_ctx.selectedResource && __VLS_ctx.monthData))
                return;
            __VLS_ctx.changeMonth(-1);
        }
    };
    __VLS_19.slots.default;
    var __VLS_19;
    const __VLS_24 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_25 = __VLS_asFunctionalComponent(__VLS_24, new __VLS_24({
        disabled: true,
    }));
    const __VLS_26 = __VLS_25({
        disabled: true,
    }, ...__VLS_functionalComponentArgsRest(__VLS_25));
    __VLS_27.slots.default;
    (__VLS_ctx.currentYear);
    (String(__VLS_ctx.currentMonth).padStart(2, '0'));
    var __VLS_27;
    const __VLS_28 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
        ...{ 'onClick': {} },
    }));
    const __VLS_30 = __VLS_29({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_29));
    let __VLS_32;
    let __VLS_33;
    let __VLS_34;
    const __VLS_35 = {
        onClick: (...[$event]) => {
            if (!(__VLS_ctx.selectedResource && __VLS_ctx.monthData))
                return;
            __VLS_ctx.changeMonth(1);
        }
    };
    __VLS_31.slots.default;
    var __VLS_31;
    var __VLS_15;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-grid" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-card" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-value" },
    });
    (__VLS_ctx.monthData.workdayCount);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-card" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-value" },
    });
    (__VLS_ctx.formatHours(__VLS_ctx.monthData.totalDefaultShiftMinutes));
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-card" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-value" },
    });
    (__VLS_ctx.formatHours(__VLS_ctx.monthData.totalEffectiveShiftMinutes));
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-card" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-value" },
    });
    (__VLS_ctx.formatHours(__VLS_ctx.monthData.totalAvailableCapacityMinutes));
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-card" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "summary-value" },
    });
    (__VLS_ctx.formatPercent(__VLS_ctx.monthData.averageUtilizationRate));
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "batch-toolbar" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "batch-toolbar__info" },
    });
    (__VLS_ctx.selectedDates.length);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "batch-toolbar__actions" },
    });
    const __VLS_36 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({
        ...{ 'onClick': {} },
        disabled: (!__VLS_ctx.selectedDates.length),
    }));
    const __VLS_38 = __VLS_37({
        ...{ 'onClick': {} },
        disabled: (!__VLS_ctx.selectedDates.length),
    }, ...__VLS_functionalComponentArgsRest(__VLS_37));
    let __VLS_40;
    let __VLS_41;
    let __VLS_42;
    const __VLS_43 = {
        onClick: (__VLS_ctx.clearSelection)
    };
    __VLS_39.slots.default;
    var __VLS_39;
    const __VLS_44 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_45 = __VLS_asFunctionalComponent(__VLS_44, new __VLS_44({
        ...{ 'onClick': {} },
        type: "primary",
        disabled: (!__VLS_ctx.selectedDates.length),
    }));
    const __VLS_46 = __VLS_45({
        ...{ 'onClick': {} },
        type: "primary",
        disabled: (!__VLS_ctx.selectedDates.length),
    }, ...__VLS_functionalComponentArgsRest(__VLS_45));
    let __VLS_48;
    let __VLS_49;
    let __VLS_50;
    const __VLS_51 = {
        onClick: (__VLS_ctx.openBatchDialog)
    };
    __VLS_47.slots.default;
    var __VLS_47;
    const __VLS_52 = {}.ElTable;
    /** @type {[typeof __VLS_components.ElTable, typeof __VLS_components.elTable, typeof __VLS_components.ElTable, typeof __VLS_components.elTable, ]} */ ;
    // @ts-ignore
    const __VLS_53 = __VLS_asFunctionalComponent(__VLS_52, new __VLS_52({
        ...{ 'onRowClick': {} },
        ...{ 'onSelectionChange': {} },
        ref: "monthTableRef",
        data: (__VLS_ctx.monthData.days),
        height: "calc(100vh - 320px)",
        stripe: true,
    }));
    const __VLS_54 = __VLS_53({
        ...{ 'onRowClick': {} },
        ...{ 'onSelectionChange': {} },
        ref: "monthTableRef",
        data: (__VLS_ctx.monthData.days),
        height: "calc(100vh - 320px)",
        stripe: true,
    }, ...__VLS_functionalComponentArgsRest(__VLS_53));
    let __VLS_56;
    let __VLS_57;
    let __VLS_58;
    const __VLS_59 = {
        onRowClick: (__VLS_ctx.handleRowClick)
    };
    const __VLS_60 = {
        onSelectionChange: (__VLS_ctx.handleSelectionChange)
    };
    __VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.loadingMonth || __VLS_ctx.batchSaving) }, null, null);
    /** @type {typeof __VLS_ctx.monthTableRef} */ ;
    var __VLS_61 = {};
    __VLS_55.slots.default;
    const __VLS_63 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_64 = __VLS_asFunctionalComponent(__VLS_63, new __VLS_63({
        type: "selection",
        width: "60",
    }));
    const __VLS_65 = __VLS_64({
        type: "selection",
        width: "60",
    }, ...__VLS_functionalComponentArgsRest(__VLS_64));
    const __VLS_67 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_68 = __VLS_asFunctionalComponent(__VLS_67, new __VLS_67({
        prop: "date",
        label: "日期",
        width: "108",
    }));
    const __VLS_69 = __VLS_68({
        prop: "date",
        label: "日期",
        width: "108",
    }, ...__VLS_functionalComponentArgsRest(__VLS_68));
    const __VLS_71 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_72 = __VLS_asFunctionalComponent(__VLS_71, new __VLS_71({
        label: "星期",
        width: "78",
    }));
    const __VLS_73 = __VLS_72({
        label: "星期",
        width: "78",
    }, ...__VLS_functionalComponentArgsRest(__VLS_72));
    __VLS_74.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_74.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        (__VLS_ctx.formatWeekday(row.date));
    }
    var __VLS_74;
    const __VLS_75 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_76 = __VLS_asFunctionalComponent(__VLS_75, new __VLS_75({
        label: "日期类型",
        width: "88",
    }));
    const __VLS_77 = __VLS_76({
        label: "日期类型",
        width: "88",
    }, ...__VLS_functionalComponentArgsRest(__VLS_76));
    __VLS_78.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_78.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        const __VLS_79 = {}.ElTag;
        /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
        // @ts-ignore
        const __VLS_80 = __VLS_asFunctionalComponent(__VLS_79, new __VLS_79({
            type: (__VLS_ctx.dateTypeTagMap[row.dateType]),
        }));
        const __VLS_81 = __VLS_80({
            type: (__VLS_ctx.dateTypeTagMap[row.dateType]),
        }, ...__VLS_functionalComponentArgsRest(__VLS_80));
        __VLS_82.slots.default;
        (__VLS_ctx.dateTypeLabelMap[row.dateType]);
        var __VLS_82;
    }
    var __VLS_78;
    const __VLS_83 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_84 = __VLS_asFunctionalComponent(__VLS_83, new __VLS_83({
        label: "默认班次",
        width: "92",
    }));
    const __VLS_85 = __VLS_84({
        label: "默认班次",
        width: "92",
    }, ...__VLS_functionalComponentArgsRest(__VLS_84));
    __VLS_86.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_86.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        (__VLS_ctx.formatHours(row.defaultShiftMinutes));
    }
    var __VLS_86;
    const __VLS_87 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_88 = __VLS_asFunctionalComponent(__VLS_87, new __VLS_87({
        label: "班次小时",
        width: "150",
    }));
    const __VLS_89 = __VLS_88({
        label: "班次小时",
        width: "150",
    }, ...__VLS_functionalComponentArgsRest(__VLS_88));
    __VLS_90.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_90.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        const __VLS_91 = {}.ElInputNumber;
        /** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
        // @ts-ignore
        const __VLS_92 = __VLS_asFunctionalComponent(__VLS_91, new __VLS_91({
            ...{ 'onUpdate:modelValue': {} },
            modelValue: (__VLS_ctx.minutesToHours(row.shiftMinutesOverride)),
            min: (0),
            step: (0.5),
            precision: (2),
            placeholder: "留空用默认",
            controlsPosition: "right",
            ...{ style: {} },
        }));
        const __VLS_93 = __VLS_92({
            ...{ 'onUpdate:modelValue': {} },
            modelValue: (__VLS_ctx.minutesToHours(row.shiftMinutesOverride)),
            min: (0),
            step: (0.5),
            precision: (2),
            placeholder: "留空用默认",
            controlsPosition: "right",
            ...{ style: {} },
        }, ...__VLS_functionalComponentArgsRest(__VLS_92));
        let __VLS_95;
        let __VLS_96;
        let __VLS_97;
        const __VLS_98 = {
            'onUpdate:modelValue': ((value) => __VLS_ctx.updateRowShiftHours(row, value))
        };
        var __VLS_94;
    }
    var __VLS_90;
    const __VLS_99 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_100 = __VLS_asFunctionalComponent(__VLS_99, new __VLS_99({
        label: "利用率",
        width: "118",
    }));
    const __VLS_101 = __VLS_100({
        label: "利用率",
        width: "118",
    }, ...__VLS_functionalComponentArgsRest(__VLS_100));
    __VLS_102.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_102.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        const __VLS_103 = {}.ElInputNumber;
        /** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
        // @ts-ignore
        const __VLS_104 = __VLS_asFunctionalComponent(__VLS_103, new __VLS_103({
            modelValue: (row.utilizationRate),
            min: (0),
            max: (1),
            step: (0.1),
            precision: (2),
            controlsPosition: "right",
            ...{ style: {} },
        }));
        const __VLS_105 = __VLS_104({
            modelValue: (row.utilizationRate),
            min: (0),
            max: (1),
            step: (0.1),
            precision: (2),
            controlsPosition: "right",
            ...{ style: {} },
        }, ...__VLS_functionalComponentArgsRest(__VLS_104));
    }
    var __VLS_102;
    const __VLS_107 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_108 = __VLS_asFunctionalComponent(__VLS_107, new __VLS_107({
        label: "可用产能",
        width: "96",
    }));
    const __VLS_109 = __VLS_108({
        label: "可用产能",
        width: "96",
    }, ...__VLS_functionalComponentArgsRest(__VLS_108));
    __VLS_110.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_110.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        (__VLS_ctx.formatHours(__VLS_ctx.computeCapacity(row)));
    }
    var __VLS_110;
    const __VLS_111 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_112 = __VLS_asFunctionalComponent(__VLS_111, new __VLS_111({
        label: "备注",
        minWidth: "140",
    }));
    const __VLS_113 = __VLS_112({
        label: "备注",
        minWidth: "140",
    }, ...__VLS_functionalComponentArgsRest(__VLS_112));
    __VLS_114.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_114.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        const __VLS_115 = {}.ElInput;
        /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
        // @ts-ignore
        const __VLS_116 = __VLS_asFunctionalComponent(__VLS_115, new __VLS_115({
            modelValue: (row.remark),
            placeholder: "备注",
            clearable: true,
        }));
        const __VLS_117 = __VLS_116({
            modelValue: (row.remark),
            placeholder: "备注",
            clearable: true,
        }, ...__VLS_functionalComponentArgsRest(__VLS_116));
    }
    var __VLS_114;
    const __VLS_119 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_120 = __VLS_asFunctionalComponent(__VLS_119, new __VLS_119({
        label: "操作",
        width: "76",
        fixed: "right",
    }));
    const __VLS_121 = __VLS_120({
        label: "操作",
        width: "76",
        fixed: "right",
    }, ...__VLS_functionalComponentArgsRest(__VLS_120));
    __VLS_122.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_122.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        const __VLS_123 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_124 = __VLS_asFunctionalComponent(__VLS_123, new __VLS_123({
            ...{ 'onClick': {} },
            text: true,
            type: "primary",
            loading: (__VLS_ctx.savingDate === row.date),
        }));
        const __VLS_125 = __VLS_124({
            ...{ 'onClick': {} },
            text: true,
            type: "primary",
            loading: (__VLS_ctx.savingDate === row.date),
        }, ...__VLS_functionalComponentArgsRest(__VLS_124));
        let __VLS_127;
        let __VLS_128;
        let __VLS_129;
        const __VLS_130 = {
            onClick: (...[$event]) => {
                if (!(__VLS_ctx.selectedResource && __VLS_ctx.monthData))
                    return;
                __VLS_ctx.saveDay(row);
            }
        };
        __VLS_126.slots.default;
        var __VLS_126;
    }
    var __VLS_122;
    var __VLS_55;
}
else {
    const __VLS_131 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_132 = __VLS_asFunctionalComponent(__VLS_131, new __VLS_131({
        description: "请选择左侧设备",
    }));
    const __VLS_133 = __VLS_132({
        description: "请选择左侧设备",
    }, ...__VLS_functionalComponentArgsRest(__VLS_132));
}
const __VLS_135 = {}.ElDialog;
/** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
// @ts-ignore
const __VLS_136 = __VLS_asFunctionalComponent(__VLS_135, new __VLS_135({
    modelValue: (__VLS_ctx.batchDialogVisible),
    title: "批量修改日产能",
    width: "520px",
}));
const __VLS_137 = __VLS_136({
    modelValue: (__VLS_ctx.batchDialogVisible),
    title: "批量修改日产能",
    width: "520px",
}, ...__VLS_functionalComponentArgsRest(__VLS_136));
__VLS_138.slots.default;
const __VLS_139 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_140 = __VLS_asFunctionalComponent(__VLS_139, new __VLS_139({
    labelWidth: "110px",
}));
const __VLS_141 = __VLS_140({
    labelWidth: "110px",
}, ...__VLS_functionalComponentArgsRest(__VLS_140));
__VLS_142.slots.default;
const __VLS_143 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_144 = __VLS_asFunctionalComponent(__VLS_143, new __VLS_143({
    label: "已选日期",
}));
const __VLS_145 = __VLS_144({
    label: "已选日期",
}, ...__VLS_functionalComponentArgsRest(__VLS_144));
__VLS_146.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "batch-selected-dates" },
});
(__VLS_ctx.selectedDatesText);
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "batch-form-tip" },
});
var __VLS_146;
const __VLS_147 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_148 = __VLS_asFunctionalComponent(__VLS_147, new __VLS_147({
    label: "班次小时",
}));
const __VLS_149 = __VLS_148({
    label: "班次小时",
}, ...__VLS_functionalComponentArgsRest(__VLS_148));
__VLS_150.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "batch-input-row" },
});
const __VLS_151 = {}.ElSwitch;
/** @type {[typeof __VLS_components.ElSwitch, typeof __VLS_components.elSwitch, ]} */ ;
// @ts-ignore
const __VLS_152 = __VLS_asFunctionalComponent(__VLS_151, new __VLS_151({
    modelValue: (__VLS_ctx.batchForm.updateShiftMinutesOverride),
}));
const __VLS_153 = __VLS_152({
    modelValue: (__VLS_ctx.batchForm.updateShiftMinutesOverride),
}, ...__VLS_functionalComponentArgsRest(__VLS_152));
const __VLS_155 = {}.ElInputNumber;
/** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
// @ts-ignore
const __VLS_156 = __VLS_asFunctionalComponent(__VLS_155, new __VLS_155({
    ...{ 'onUpdate:modelValue': {} },
    modelValue: (__VLS_ctx.minutesToHours(__VLS_ctx.batchForm.shiftMinutesOverride)),
    disabled: (!__VLS_ctx.batchForm.updateShiftMinutesOverride),
    min: (0),
    step: (0.5),
    precision: (2),
    placeholder: "留空恢复默认",
    controlsPosition: "right",
    ...{ style: {} },
}));
const __VLS_157 = __VLS_156({
    ...{ 'onUpdate:modelValue': {} },
    modelValue: (__VLS_ctx.minutesToHours(__VLS_ctx.batchForm.shiftMinutesOverride)),
    disabled: (!__VLS_ctx.batchForm.updateShiftMinutesOverride),
    min: (0),
    step: (0.5),
    precision: (2),
    placeholder: "留空恢复默认",
    controlsPosition: "right",
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_156));
let __VLS_159;
let __VLS_160;
let __VLS_161;
const __VLS_162 = {
    'onUpdate:modelValue': (__VLS_ctx.updateBatchShiftHours)
};
var __VLS_158;
var __VLS_150;
const __VLS_163 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_164 = __VLS_asFunctionalComponent(__VLS_163, new __VLS_163({
    label: "利用率",
}));
const __VLS_165 = __VLS_164({
    label: "利用率",
}, ...__VLS_functionalComponentArgsRest(__VLS_164));
__VLS_166.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "batch-input-row" },
});
const __VLS_167 = {}.ElSwitch;
/** @type {[typeof __VLS_components.ElSwitch, typeof __VLS_components.elSwitch, ]} */ ;
// @ts-ignore
const __VLS_168 = __VLS_asFunctionalComponent(__VLS_167, new __VLS_167({
    modelValue: (__VLS_ctx.batchForm.updateUtilizationRate),
}));
const __VLS_169 = __VLS_168({
    modelValue: (__VLS_ctx.batchForm.updateUtilizationRate),
}, ...__VLS_functionalComponentArgsRest(__VLS_168));
const __VLS_171 = {}.ElInputNumber;
/** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
// @ts-ignore
const __VLS_172 = __VLS_asFunctionalComponent(__VLS_171, new __VLS_171({
    modelValue: (__VLS_ctx.batchForm.utilizationRate),
    disabled: (!__VLS_ctx.batchForm.updateUtilizationRate),
    min: (0),
    max: (1),
    step: (0.1),
    precision: (2),
    controlsPosition: "right",
    ...{ style: {} },
}));
const __VLS_173 = __VLS_172({
    modelValue: (__VLS_ctx.batchForm.utilizationRate),
    disabled: (!__VLS_ctx.batchForm.updateUtilizationRate),
    min: (0),
    max: (1),
    step: (0.1),
    precision: (2),
    controlsPosition: "right",
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_172));
var __VLS_166;
const __VLS_175 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_176 = __VLS_asFunctionalComponent(__VLS_175, new __VLS_175({
    label: "备注操作",
}));
const __VLS_177 = __VLS_176({
    label: "备注操作",
}, ...__VLS_functionalComponentArgsRest(__VLS_176));
__VLS_178.slots.default;
const __VLS_179 = {}.ElRadioGroup;
/** @type {[typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, ]} */ ;
// @ts-ignore
const __VLS_180 = __VLS_asFunctionalComponent(__VLS_179, new __VLS_179({
    modelValue: (__VLS_ctx.batchForm.remarkAction),
}));
const __VLS_181 = __VLS_180({
    modelValue: (__VLS_ctx.batchForm.remarkAction),
}, ...__VLS_functionalComponentArgsRest(__VLS_180));
__VLS_182.slots.default;
const __VLS_183 = {}.ElRadio;
/** @type {[typeof __VLS_components.ElRadio, typeof __VLS_components.elRadio, typeof __VLS_components.ElRadio, typeof __VLS_components.elRadio, ]} */ ;
// @ts-ignore
const __VLS_184 = __VLS_asFunctionalComponent(__VLS_183, new __VLS_183({
    value: "ignore",
}));
const __VLS_185 = __VLS_184({
    value: "ignore",
}, ...__VLS_functionalComponentArgsRest(__VLS_184));
__VLS_186.slots.default;
var __VLS_186;
const __VLS_187 = {}.ElRadio;
/** @type {[typeof __VLS_components.ElRadio, typeof __VLS_components.elRadio, typeof __VLS_components.ElRadio, typeof __VLS_components.elRadio, ]} */ ;
// @ts-ignore
const __VLS_188 = __VLS_asFunctionalComponent(__VLS_187, new __VLS_187({
    value: "set",
}));
const __VLS_189 = __VLS_188({
    value: "set",
}, ...__VLS_functionalComponentArgsRest(__VLS_188));
__VLS_190.slots.default;
var __VLS_190;
const __VLS_191 = {}.ElRadio;
/** @type {[typeof __VLS_components.ElRadio, typeof __VLS_components.elRadio, typeof __VLS_components.ElRadio, typeof __VLS_components.elRadio, ]} */ ;
// @ts-ignore
const __VLS_192 = __VLS_asFunctionalComponent(__VLS_191, new __VLS_191({
    value: "clear",
}));
const __VLS_193 = __VLS_192({
    value: "clear",
}, ...__VLS_functionalComponentArgsRest(__VLS_192));
__VLS_194.slots.default;
var __VLS_194;
var __VLS_182;
var __VLS_178;
const __VLS_195 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_196 = __VLS_asFunctionalComponent(__VLS_195, new __VLS_195({
    label: "备注",
}));
const __VLS_197 = __VLS_196({
    label: "备注",
}, ...__VLS_functionalComponentArgsRest(__VLS_196));
__VLS_198.slots.default;
const __VLS_199 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_200 = __VLS_asFunctionalComponent(__VLS_199, new __VLS_199({
    modelValue: (__VLS_ctx.batchForm.remark),
    disabled: (__VLS_ctx.batchForm.remarkAction !== 'set'),
    placeholder: "输入要批量设置的备注",
    clearable: true,
}));
const __VLS_201 = __VLS_200({
    modelValue: (__VLS_ctx.batchForm.remark),
    disabled: (__VLS_ctx.batchForm.remarkAction !== 'set'),
    placeholder: "输入要批量设置的备注",
    clearable: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_200));
var __VLS_198;
var __VLS_142;
{
    const { footer: __VLS_thisSlot } = __VLS_138.slots;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "dialog-footer" },
    });
    const __VLS_203 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_204 = __VLS_asFunctionalComponent(__VLS_203, new __VLS_203({
        ...{ 'onClick': {} },
    }));
    const __VLS_205 = __VLS_204({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_204));
    let __VLS_207;
    let __VLS_208;
    let __VLS_209;
    const __VLS_210 = {
        onClick: (...[$event]) => {
            __VLS_ctx.batchDialogVisible = false;
        }
    };
    __VLS_206.slots.default;
    var __VLS_206;
    const __VLS_211 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_212 = __VLS_asFunctionalComponent(__VLS_211, new __VLS_211({
        ...{ 'onClick': {} },
        type: "primary",
        loading: (__VLS_ctx.batchSaving),
    }));
    const __VLS_213 = __VLS_212({
        ...{ 'onClick': {} },
        type: "primary",
        loading: (__VLS_ctx.batchSaving),
    }, ...__VLS_functionalComponentArgsRest(__VLS_212));
    let __VLS_215;
    let __VLS_216;
    let __VLS_217;
    const __VLS_218 = {
        onClick: (__VLS_ctx.saveBatchDays)
    };
    __VLS_214.slots.default;
    var __VLS_214;
}
var __VLS_138;
/** @type {__VLS_StyleScopedClasses['resource-capacity-page']} */ ;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
/** @type {__VLS_StyleScopedClasses['content-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['left-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-title']} */ ;
/** @type {__VLS_StyleScopedClasses['search-input']} */ ;
/** @type {__VLS_StyleScopedClasses['resource-list']} */ ;
/** @type {__VLS_StyleScopedClasses['resource-card-header']} */ ;
/** @type {__VLS_StyleScopedClasses['resource-name']} */ ;
/** @type {__VLS_StyleScopedClasses['resource-meta']} */ ;
/** @type {__VLS_StyleScopedClasses['resource-meta']} */ ;
/** @type {__VLS_StyleScopedClasses['resource-meta']} */ ;
/** @type {__VLS_StyleScopedClasses['right-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-header']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-subtitle']} */ ;
/** @type {__VLS_StyleScopedClasses['separator']} */ ;
/** @type {__VLS_StyleScopedClasses['separator']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-card']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-label']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-value']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-card']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-label']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-value']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-card']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-label']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-value']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-card']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-label']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-value']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-card']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-label']} */ ;
/** @type {__VLS_StyleScopedClasses['summary-value']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-toolbar__info']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-toolbar__actions']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-selected-dates']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-form-tip']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-input-row']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-input-row']} */ ;
/** @type {__VLS_StyleScopedClasses['dialog-footer']} */ ;
// @ts-ignore
var __VLS_62 = __VLS_61;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            loadingResources: loadingResources,
            loadingMonth: loadingMonth,
            batchSaving: batchSaving,
            selectedResource: selectedResource,
            monthData: monthData,
            searchKeyword: searchKeyword,
            savingDate: savingDate,
            selectedDates: selectedDates,
            batchDialogVisible: batchDialogVisible,
            batchForm: batchForm,
            monthTableRef: monthTableRef,
            currentYear: currentYear,
            currentMonth: currentMonth,
            dateTypeLabelMap: dateTypeLabelMap,
            dateTypeTagMap: dateTypeTagMap,
            filteredResources: filteredResources,
            selectedDatesText: selectedDatesText,
            minutesToHours: minutesToHours,
            updateRowShiftHours: updateRowShiftHours,
            updateBatchShiftHours: updateBatchShiftHours,
            selectResource: selectResource,
            changeMonth: changeMonth,
            handleSelectionChange: handleSelectionChange,
            handleRowClick: handleRowClick,
            clearSelection: clearSelection,
            openBatchDialog: openBatchDialog,
            saveDay: saveDay,
            saveBatchDays: saveBatchDays,
            computeCapacity: computeCapacity,
            formatHours: formatHours,
            formatPercent: formatPercent,
            formatWeekday: formatWeekday,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
