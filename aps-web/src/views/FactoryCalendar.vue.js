/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { ref, computed, onMounted } from 'vue';
import { msgSuccess, msgError, msgWarning, confirmDanger, extractErrorMsg } from '@/utils/message';
import { Calendar, Plus, Search, StarFilled, Edit, Delete, ArrowLeft, ArrowRight, Close } from '@element-plus/icons-vue';
import { factoryCalendarApi } from '@/api/factoryCalendar';
import { parseTime, validateShiftTime, getDateRange } from '@/utils/date';
import { holidayTemplates, getHolidayDates, isYearSupported, SUPPORTED_YEARS } from '@/config/holidays';
import { dictionaryApi } from '@/api/dictionary';
const loading = ref(false);
const calendars = ref([]);
const selectedCalendar = ref(null);
const searchKeyword = ref('');
const activeTab = ref('month');
const dateTypeItems = ref([]);
const dateTypeNameMap = ref({});
// 日历表单
const showCalendarDialog = ref(false);
const editingCalendar = ref(null);
const calendarFormRef = ref();
const calendarForm = ref({
    name: '',
    code: '',
    year: new Date().getFullYear(),
    description: ''
});
const calendarRules = {
    name: [{ required: true, message: '请输入日历名称', trigger: 'blur' }],
    code: [{ required: true, message: '请输入日历代码', trigger: 'blur' }],
    year: [{ required: true, message: '请选择年份', trigger: 'blur' }]
};
// 班次相关
const shifts = ref([]);
const showShiftDialog = ref(false);
const editingShift = ref(null);
const shiftFormRef = ref();
const shiftForm = ref({
    name: '',
    startTime: '',
    endTime: '',
    sortOrder: 0,
    breakMinutes: 0,
    nextDay: false
});
const shiftRules = {
    name: [{ required: true, message: '请输入班次名称', trigger: 'blur' }],
    startTime: [{ required: true, message: '请选择开始时间', trigger: 'blur' }],
    endTime: [{ required: true, message: '请选择结束时间', trigger: 'blur' }],
    breakMinutes: [{ required: true, message: '请输入休息时长', trigger: 'blur' }]
};
function getMinutesFromTime(time) {
    const [hours, minutes] = parseTime(time).split(':').map(Number);
    return hours * 60 + minutes;
}
function getShiftEndMinutes(startTime, endTime, nextDay) {
    const start = getMinutesFromTime(startTime);
    const end = getMinutesFromTime(endTime);
    return nextDay ? end + 24 * 60 : Math.max(end, start);
}
function getShiftDurationMinutes(startTime, endTime, nextDay) {
    return getShiftEndMinutes(startTime, endTime, nextDay) - getMinutesFromTime(startTime);
}
function formatShiftEffectiveHours(shift) {
    const totalMinutes = getShiftDurationMinutes(shift.startTime, shift.endTime, shift.nextDay ?? false);
    const breakMinutes = shift.breakMinutes ?? 0;
    const effectiveMinutes = Math.max(totalMinutes - breakMinutes, 0);
    return `${(effectiveMinutes / 60).toFixed(Number.isInteger(effectiveMinutes / 60) ? 0 : 2)} 小时`;
}
function validateShiftOverlap(currentShift, shiftList, excludeShiftId) {
    const sortedShifts = [
        ...shiftList.filter(shift => !(excludeShiftId && shift.id === excludeShiftId)),
        {
            id: excludeShiftId ?? '__new__',
            name: '当前班次',
            startTime: currentShift.startTime,
            endTime: currentShift.endTime,
            sortOrder: 0,
            nextDay: currentShift.nextDay
        }
    ]
        .map(shift => ({
        ...shift,
        startMinutes: getMinutesFromTime(shift.startTime),
        endMinutes: getShiftEndMinutes(shift.startTime, shift.endTime, shift.nextDay ?? false)
    }))
        .sort((a, b) => a.startMinutes - b.startMinutes);
    if (sortedShifts.length <= 1) {
        return { valid: true };
    }
    for (let index = 1; index < sortedShifts.length; index++) {
        const previousShift = sortedShifts[index - 1];
        const currentSortedShift = sortedShifts[index];
        const overlapMinutes = previousShift.endMinutes - currentSortedShift.startMinutes;
        if (overlapMinutes >= 1) {
            const existingShift = previousShift.id === (excludeShiftId ?? '__new__') ? currentSortedShift : previousShift;
            return {
                valid: false,
                error: `班次时间与现有班次“${existingShift.name}”不能重叠，请调整后重试`
            };
        }
    }
    const firstShift = sortedShifts[0];
    const lastShift = sortedShifts[sortedShifts.length - 1];
    const crossDayOverlapMinutes = lastShift.endMinutes - (firstShift.startMinutes + 24 * 60);
    if (crossDayOverlapMinutes >= 1) {
        const existingShift = lastShift.id === (excludeShiftId ?? '__new__') ? firstShift : lastShift;
        return {
            valid: false,
            error: `班次时间与现有班次“${existingShift.name}”不能重叠，请调整后重试`
        };
    }
    return { valid: true };
}
// 批量设置节假日
const showBatchHolidayDialog = ref(false);
const batchHolidayForm = ref({
    year: new Date().getFullYear(),
    template: '',
    customDates: [],
    label: ''
});
const showBatchWeekendDialog = ref(false);
const batchWeekendForm = ref({
    pattern: 'DOUBLE'
});
// 月视图相关
const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const calendarDates = ref([]);
const selectedDate = ref(null);
const dateForm = ref({
    dateType: 'WORKDAY',
    label: ''
});
function getDefaultMonthForYear(year) {
    const now = new Date();
    return year === now.getFullYear() ? now.getMonth() + 1 : 1;
}
// 右键菜单
const contextMenuVisible = ref(false);
const contextMenuX = ref(0);
const contextMenuY = ref(0);
const contextMenuDate = ref(null);
const weekdays = ['日', '一', '二', '三', '四', '五', '六'];
function sortCalendarDates(dates) {
    return [...dates].sort((a, b) => a.date.localeCompare(b.date));
}
const calendarGridCells = computed(() => {
    if (calendarDates.value.length === 0)
        return [];
    const firstDate = calendarDates.value[0];
    const firstWeekdayIndex = new Date(`${firstDate.date}T00:00:00`).getDay();
    const placeholders = Array.from({ length: firstWeekdayIndex }, (_, index) => ({
        key: `placeholder-${currentYear.value}-${currentMonth.value}-${index}`,
        date: null
    }));
    const dateCells = calendarDates.value.map((date) => ({
        key: date.date,
        date
    }));
    return [...placeholders, ...dateCells];
});
function getDayOfMonth(date) {
    return Number(date.split('-')[2]);
}
const filteredCalendars = computed(() => {
    if (!searchKeyword.value)
        return calendars.value;
    return calendars.value.filter((cal) => cal.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
        cal.code.toLowerCase().includes(searchKeyword.value.toLowerCase()));
});
onMounted(async () => {
    await Promise.all([loadCalendars(), loadDateTypeDict()]);
});
async function loadDateTypeDict() {
    try {
        dateTypeItems.value = await dictionaryApi.getEnabledItemsByTypeCode('DATE_TYPE');
        dateTypeNameMap.value = Object.fromEntries(dateTypeItems.value.map(item => [item.itemCode, item.itemName]));
    }
    catch {
        dateTypeItems.value = [];
    }
}
async function loadCalendars() {
    loading.value = true;
    try {
        const result = await factoryCalendarApi.getCalendars();
        calendars.value = result;
        if (selectedCalendar.value) {
            const refreshedSelectedCalendar = calendars.value.find((calendar) => calendar.id === selectedCalendar.value?.id);
            selectedCalendar.value = refreshedSelectedCalendar ?? null;
        }
        if (calendars.value.length > 0 && !selectedCalendar.value) {
            await selectCalendar(calendars.value[0]);
        }
    }
    catch (error) {
        msgError(error.message || '加载日历失败');
    }
    finally {
        loading.value = false;
    }
}
async function refreshCalendarListSelection() {
    await loadCalendars();
}
async function selectCalendar(cal) {
    selectedCalendar.value = cal;
    currentYear.value = cal.year;
    currentMonth.value = getDefaultMonthForYear(cal.year);
    await loadShifts();
    await loadMonthDates();
}
async function loadShifts() {
    if (!selectedCalendar.value)
        return;
    try {
        shifts.value = await factoryCalendarApi.getShifts(selectedCalendar.value.id);
    }
    catch (error) {
        msgError(error.message || '加载班次失败');
    }
}
async function loadMonthDates() {
    if (!selectedCalendar.value)
        return;
    try {
        const dates = await factoryCalendarApi.getDatesByMonth(selectedCalendar.value.id, currentYear.value, currentMonth.value);
        calendarDates.value = sortCalendarDates(dates);
    }
    catch (error) {
        msgError(error.message || '加载日期失败');
    }
}
function previousMonth() {
    if (currentMonth.value === 1) {
        currentMonth.value = 12;
        currentYear.value--;
    }
    else {
        currentMonth.value--;
    }
    loadMonthDates();
}
function nextMonth() {
    if (currentMonth.value === 12) {
        currentMonth.value = 1;
        currentYear.value++;
    }
    else {
        currentMonth.value++;
    }
    loadMonthDates();
}
function getDateClass(date) {
    const classes = ['date-cell'];
    if (date.dateType === 'WORKDAY')
        classes.push('workday');
    else if (date.dateType === 'RESTDAY')
        classes.push('restday');
    else if (date.dateType === 'HOLIDAY')
        classes.push('holiday');
    if (selectedDate.value?.date === date.date)
        classes.push('selected');
    return classes.join(' ');
}
function getDateTypeLabel(dateType) {
    const map = {
        WORKDAY: '工',
        RESTDAY: '休',
        HOLIDAY: '假'
    };
    return map[dateType] || '';
}
function getDateTypeName(dateType) {
    return dateTypeNameMap.value[dateType] || dateType;
}
function selectDate(date) {
    selectedDate.value = date;
    dateForm.value = {
        dateType: date.dateType,
        label: date.label || ''
    };
}
async function saveDateType() {
    if (!selectedCalendar.value || !selectedDate.value)
        return;
    try {
        await factoryCalendarApi.updateDateType(selectedCalendar.value.id, {
            date: selectedDate.value.date,
            dateType: dateForm.value.dateType,
            label: dateForm.value.label
        });
        msgSuccess(`已设置为${getDateTypeName(dateForm.value.dateType)}`);
        selectedDate.value = null;
        await loadMonthDates();
        await loadCalendars();
    }
    catch (error) {
        msgError(error.message || '设置失败');
    }
}
function showDateContextMenu(event, date) {
    contextMenuDate.value = date;
    contextMenuX.value = event.clientX;
    contextMenuY.value = event.clientY;
    contextMenuVisible.value = true;
    document.addEventListener('click', () => {
        contextMenuVisible.value = false;
    }, { once: true });
}
async function quickSetDateType(dateType) {
    if (!selectedCalendar.value || !contextMenuDate.value)
        return;
    try {
        await factoryCalendarApi.updateDateType(selectedCalendar.value.id, {
            date: contextMenuDate.value.date,
            dateType,
            label: contextMenuDate.value.label || ''
        });
        msgSuccess(`已设置为${getDateTypeName(dateType)}`);
        await loadMonthDates();
        await refreshCalendarListSelection();
    }
    catch (error) {
        msgError(error.message || '设置失败');
    }
}
function handleCreate() {
    editingCalendar.value = null;
    calendarForm.value = {
        name: '',
        code: '',
        year: new Date().getFullYear(),
        description: ''
    };
    showCalendarDialog.value = true;
}
function handleEdit(cal) {
    editingCalendar.value = cal;
    calendarForm.value = {
        name: cal.name,
        code: cal.code,
        year: cal.year,
        description: cal.description || ''
    };
    showCalendarDialog.value = true;
}
async function handleSetDefault(cal) {
    try {
        await factoryCalendarApi.setDefault(cal.id);
        msgSuccess(`已将"${cal.name}"设为默认日历`);
        await loadCalendars();
    }
    catch (error) {
        msgError(extractErrorMsg(error, '设置默认失败'));
    }
}
async function handleDelete(cal) {
    try {
        await confirmDanger(`确定删除日历 "${cal.name}" 吗？`);
        await factoryCalendarApi.deleteCalendar(cal.id);
        msgSuccess(`日历"${cal.name}"已删除`);
        if (selectedCalendar.value?.id === cal.id) {
            selectedCalendar.value = null;
        }
        await loadCalendars();
    }
    catch (error) {
        if (error !== 'cancel') {
            msgError(extractErrorMsg(error, '删除失败'));
        }
    }
}
async function saveCalendar() {
    if (!calendarFormRef.value)
        return;
    await calendarFormRef.value.validate(async (valid) => {
        if (!valid)
            return;
        try {
            if (editingCalendar.value) {
                await factoryCalendarApi.updateCalendar(editingCalendar.value.id, calendarForm.value);
                msgSuccess(`日历"${calendarForm.value.name}"已更新`);
            }
            else {
                await factoryCalendarApi.createCalendar(calendarForm.value);
                msgSuccess(`日历"${calendarForm.value.name}"已创建`);
            }
            showCalendarDialog.value = false;
            await loadCalendars();
        }
        catch (error) {
            msgError(error.message || '保存失败');
        }
    });
}
function handleAddShift() {
    editingShift.value = null;
    shiftForm.value = {
        name: '',
        startTime: '',
        endTime: '',
        sortOrder: 0,
        breakMinutes: 0,
        nextDay: false
    };
    showShiftDialog.value = true;
}
function handleEditShift(shift) {
    editingShift.value = shift;
    shiftForm.value = {
        name: shift.name,
        startTime: shift.startTime,
        endTime: shift.endTime,
        sortOrder: shift.sortOrder,
        breakMinutes: shift.breakMinutes ?? 0,
        nextDay: shift.nextDay ?? false
    };
    showShiftDialog.value = true;
}
async function handleDeleteShift(shift) {
    if (!selectedCalendar.value)
        return;
    try {
        await confirmDanger(`确定删除班次 "${shift.name}" 吗？`);
        await factoryCalendarApi.deleteShift(selectedCalendar.value.id, shift.id);
        msgSuccess(`班次"${shift.name}"已删除`);
        await loadShifts();
    }
    catch (error) {
        if (error !== 'cancel') {
            msgError(extractErrorMsg(error, '删除失败'));
        }
    }
}
async function saveShift() {
    if (!shiftFormRef.value)
        return;
    if (!selectedCalendar.value)
        return;
    await shiftFormRef.value.validate(async (valid) => {
        if (!valid)
            return;
        try {
            // 使用工具函数解析时间
            const startTime = parseTime(shiftForm.value.startTime);
            const endTime = parseTime(shiftForm.value.endTime);
            // 验证班次时间
            const validation = validateShiftTime(startTime, endTime, shiftForm.value.nextDay);
            if (!validation.valid) {
                msgError(validation.error || '时间设置不合法');
                return;
            }
            const totalShiftMinutes = getShiftDurationMinutes(startTime, endTime, shiftForm.value.nextDay);
            if (shiftForm.value.breakMinutes >= totalShiftMinutes) {
                msgError('休息时长必须小于班次总时长');
                return;
            }
            const overlapValidation = validateShiftOverlap({
                startTime,
                endTime,
                nextDay: shiftForm.value.nextDay
            }, shifts.value, editingShift.value?.id);
            if (!overlapValidation.valid) {
                msgError(overlapValidation.error || '班次时间重叠过长');
                return;
            }
            const normalizedForm = {
                name: shiftForm.value.name,
                startTime,
                endTime,
                sortOrder: shiftForm.value.sortOrder,
                breakMinutes: shiftForm.value.breakMinutes,
                nextDay: shiftForm.value.nextDay
            };
            if (editingShift.value) {
                await factoryCalendarApi.updateShift(selectedCalendar.value.id, editingShift.value.id, normalizedForm);
                msgSuccess(`班次"${shiftForm.value.name}"已更新`);
            }
            else {
                await factoryCalendarApi.addShift(selectedCalendar.value.id, normalizedForm);
                msgSuccess(`班次"${shiftForm.value.name}"已添加`);
            }
            showShiftDialog.value = false;
            await loadShifts();
        }
        catch (error) {
            msgError(error.message || '保存失败');
        }
    });
}
async function saveBatchHolidays() {
    if (!selectedCalendar.value)
        return;
    if (!batchHolidayForm.value.template) {
        msgWarning('请选择节假日模板或自定义日期');
        return;
    }
    // 检查年份是否支持
    if (!isYearSupported(batchHolidayForm.value.year)) {
        msgWarning(`年份 ${batchHolidayForm.value.year} 暂不支持，支持的年份：${SUPPORTED_YEARS.join(', ')}`);
        return;
    }
    let dates = [];
    if (batchHolidayForm.value.template === 'custom') {
        if (batchHolidayForm.value.customDates.length !== 2) {
            msgWarning('请选择开始和结束日期');
            return;
        }
        const [start, end] = batchHolidayForm.value.customDates;
        try {
            dates = getDateRange(start, end);
        }
        catch (error) {
            msgError(error instanceof Error ? error.message : '日期范围无效');
            return;
        }
    }
    else {
        try {
            dates = getHolidayDates(batchHolidayForm.value.template, batchHolidayForm.value.year);
        }
        catch (error) {
            msgError(error instanceof Error ? error.message : '获取节假日日期失败');
            return;
        }
    }
    if (dates.length === 0) {
        msgWarning('未获取到节假日日期，请检查年份和模板配置');
        return;
    }
    try {
        await factoryCalendarApi.batchSetHolidays(selectedCalendar.value.id, {
            dates,
            label: batchHolidayForm.value.label
        });
        msgSuccess(`已设置${dates.length}个${batchHolidayForm.value.label || '节假日'}`);
        showBatchHolidayDialog.value = false;
        batchHolidayForm.value = {
            year: new Date().getFullYear(),
            template: '',
            customDates: [],
            label: ''
        };
        await loadMonthDates();
        await refreshCalendarListSelection();
    }
    catch (error) {
        msgError(error.message || '设置失败');
    }
}
async function saveBatchWeekendPattern() {
    if (!selectedCalendar.value)
        return;
    if (!['SINGLE', 'DOUBLE'].includes(batchWeekendForm.value.pattern)) {
        msgWarning('请选择有效的休息规则');
        return;
    }
    try {
        await factoryCalendarApi.applyWeekendPattern(selectedCalendar.value.id, {
            pattern: batchWeekendForm.value.pattern
        });
        msgSuccess(batchWeekendForm.value.pattern === 'SINGLE' ? '已设置为单休' : '已设置为双休');
        showBatchWeekendDialog.value = false;
        await loadMonthDates();
        await refreshCalendarListSelection();
    }
    catch (error) {
        msgError(error.message || '设置失败');
    }
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['panel-header']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-list']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-list']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-list']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-card']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-card']} */ ;
/** @type {__VLS_StyleScopedClasses['info-item']} */ ;
/** @type {__VLS_StyleScopedClasses['date-cell']} */ ;
/** @type {__VLS_StyleScopedClasses['date-cell']} */ ;
/** @type {__VLS_StyleScopedClasses['date-cell']} */ ;
/** @type {__VLS_StyleScopedClasses['placeholder']} */ ;
/** @type {__VLS_StyleScopedClasses['date-cell']} */ ;
/** @type {__VLS_StyleScopedClasses['date-cell']} */ ;
/** @type {__VLS_StyleScopedClasses['date-cell']} */ ;
/** @type {__VLS_StyleScopedClasses['date-cell']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-item']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "factory-calendar-page" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "content-wrapper" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "left-panel" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "panel-header" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
const __VLS_0 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({}));
const __VLS_2 = __VLS_1({}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_3.slots.default;
const __VLS_4 = {}.Calendar;
/** @type {[typeof __VLS_components.Calendar, ]} */ ;
// @ts-ignore
const __VLS_5 = __VLS_asFunctionalComponent(__VLS_4, new __VLS_4({}));
const __VLS_6 = __VLS_5({}, ...__VLS_functionalComponentArgsRest(__VLS_5));
var __VLS_3;
const __VLS_8 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({
    ...{ 'onClick': {} },
    type: "primary",
    size: "small",
}));
const __VLS_10 = __VLS_9({
    ...{ 'onClick': {} },
    type: "primary",
    size: "small",
}, ...__VLS_functionalComponentArgsRest(__VLS_9));
let __VLS_12;
let __VLS_13;
let __VLS_14;
const __VLS_15 = {
    onClick: (__VLS_ctx.handleCreate)
};
__VLS_11.slots.default;
const __VLS_16 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_17 = __VLS_asFunctionalComponent(__VLS_16, new __VLS_16({}));
const __VLS_18 = __VLS_17({}, ...__VLS_functionalComponentArgsRest(__VLS_17));
__VLS_19.slots.default;
const __VLS_20 = {}.Plus;
/** @type {[typeof __VLS_components.Plus, ]} */ ;
// @ts-ignore
const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({}));
const __VLS_22 = __VLS_21({}, ...__VLS_functionalComponentArgsRest(__VLS_21));
var __VLS_19;
var __VLS_11;
const __VLS_24 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_25 = __VLS_asFunctionalComponent(__VLS_24, new __VLS_24({
    modelValue: (__VLS_ctx.searchKeyword),
    placeholder: "搜索日历...",
    prefixIcon: (__VLS_ctx.Search),
    clearable: true,
    ...{ class: "search-input" },
}));
const __VLS_26 = __VLS_25({
    modelValue: (__VLS_ctx.searchKeyword),
    placeholder: "搜索日历...",
    prefixIcon: (__VLS_ctx.Search),
    clearable: true,
    ...{ class: "search-input" },
}, ...__VLS_functionalComponentArgsRest(__VLS_25));
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "calendar-list" },
});
__VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.loading) }, null, null);
for (const [cal] of __VLS_getVForSourceType((__VLS_ctx.filteredCalendars))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.selectCalendar(cal);
            } },
        key: (cal.id),
        ...{ class: (['calendar-card', { active: __VLS_ctx.selectedCalendar?.id === cal.id }]) },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "calendar-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "calendar-name" },
    });
    (cal.name);
    if (cal.isDefault) {
        const __VLS_28 = {}.ElTag;
        /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
        // @ts-ignore
        const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
            type: "success",
            size: "small",
        }));
        const __VLS_30 = __VLS_29({
            type: "success",
            size: "small",
        }, ...__VLS_functionalComponentArgsRest(__VLS_29));
        __VLS_31.slots.default;
        var __VLS_31;
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "calendar-info" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "info-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "value" },
    });
    (cal.code);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "info-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "value" },
    });
    (cal.year);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "info-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "value" },
    });
    (cal.workdayCount);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "calendar-actions" },
    });
    if (!cal.isDefault) {
        const __VLS_32 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({
            ...{ 'onClick': {} },
            text: true,
            size: "small",
            type: "success",
        }));
        const __VLS_34 = __VLS_33({
            ...{ 'onClick': {} },
            text: true,
            size: "small",
            type: "success",
        }, ...__VLS_functionalComponentArgsRest(__VLS_33));
        let __VLS_36;
        let __VLS_37;
        let __VLS_38;
        const __VLS_39 = {
            onClick: (...[$event]) => {
                if (!(!cal.isDefault))
                    return;
                __VLS_ctx.handleSetDefault(cal);
            }
        };
        __VLS_35.slots.default;
        const __VLS_40 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({}));
        const __VLS_42 = __VLS_41({}, ...__VLS_functionalComponentArgsRest(__VLS_41));
        __VLS_43.slots.default;
        const __VLS_44 = {}.StarFilled;
        /** @type {[typeof __VLS_components.StarFilled, ]} */ ;
        // @ts-ignore
        const __VLS_45 = __VLS_asFunctionalComponent(__VLS_44, new __VLS_44({}));
        const __VLS_46 = __VLS_45({}, ...__VLS_functionalComponentArgsRest(__VLS_45));
        var __VLS_43;
        var __VLS_35;
    }
    const __VLS_48 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({
        ...{ 'onClick': {} },
        text: true,
        size: "small",
    }));
    const __VLS_50 = __VLS_49({
        ...{ 'onClick': {} },
        text: true,
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_49));
    let __VLS_52;
    let __VLS_53;
    let __VLS_54;
    const __VLS_55 = {
        onClick: (...[$event]) => {
            __VLS_ctx.handleEdit(cal);
        }
    };
    __VLS_51.slots.default;
    const __VLS_56 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({}));
    const __VLS_58 = __VLS_57({}, ...__VLS_functionalComponentArgsRest(__VLS_57));
    __VLS_59.slots.default;
    const __VLS_60 = {}.Edit;
    /** @type {[typeof __VLS_components.Edit, ]} */ ;
    // @ts-ignore
    const __VLS_61 = __VLS_asFunctionalComponent(__VLS_60, new __VLS_60({}));
    const __VLS_62 = __VLS_61({}, ...__VLS_functionalComponentArgsRest(__VLS_61));
    var __VLS_59;
    var __VLS_51;
    const __VLS_64 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({
        ...{ 'onClick': {} },
        text: true,
        size: "small",
    }));
    const __VLS_66 = __VLS_65({
        ...{ 'onClick': {} },
        text: true,
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_65));
    let __VLS_68;
    let __VLS_69;
    let __VLS_70;
    const __VLS_71 = {
        onClick: (...[$event]) => {
            __VLS_ctx.handleDelete(cal);
        }
    };
    __VLS_67.slots.default;
    const __VLS_72 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_73 = __VLS_asFunctionalComponent(__VLS_72, new __VLS_72({}));
    const __VLS_74 = __VLS_73({}, ...__VLS_functionalComponentArgsRest(__VLS_73));
    __VLS_75.slots.default;
    const __VLS_76 = {}.Delete;
    /** @type {[typeof __VLS_components.Delete, ]} */ ;
    // @ts-ignore
    const __VLS_77 = __VLS_asFunctionalComponent(__VLS_76, new __VLS_76({}));
    const __VLS_78 = __VLS_77({}, ...__VLS_functionalComponentArgsRest(__VLS_77));
    var __VLS_75;
    var __VLS_67;
}
if (__VLS_ctx.filteredCalendars.length === 0) {
    const __VLS_80 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_81 = __VLS_asFunctionalComponent(__VLS_80, new __VLS_80({
        description: "暂无日历",
    }));
    const __VLS_82 = __VLS_81({
        description: "暂无日历",
    }, ...__VLS_functionalComponentArgsRest(__VLS_81));
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "right-panel" },
});
if (__VLS_ctx.selectedCalendar) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "calendar-detail" },
    });
    const __VLS_84 = {}.ElTabs;
    /** @type {[typeof __VLS_components.ElTabs, typeof __VLS_components.elTabs, typeof __VLS_components.ElTabs, typeof __VLS_components.elTabs, ]} */ ;
    // @ts-ignore
    const __VLS_85 = __VLS_asFunctionalComponent(__VLS_84, new __VLS_84({
        modelValue: (__VLS_ctx.activeTab),
        ...{ class: "detail-tabs" },
    }));
    const __VLS_86 = __VLS_85({
        modelValue: (__VLS_ctx.activeTab),
        ...{ class: "detail-tabs" },
    }, ...__VLS_functionalComponentArgsRest(__VLS_85));
    __VLS_87.slots.default;
    const __VLS_88 = {}.ElTabPane;
    /** @type {[typeof __VLS_components.ElTabPane, typeof __VLS_components.elTabPane, typeof __VLS_components.ElTabPane, typeof __VLS_components.elTabPane, ]} */ ;
    // @ts-ignore
    const __VLS_89 = __VLS_asFunctionalComponent(__VLS_88, new __VLS_88({
        label: "月视图",
        name: "month",
    }));
    const __VLS_90 = __VLS_89({
        label: "月视图",
        name: "month",
    }, ...__VLS_functionalComponentArgsRest(__VLS_89));
    __VLS_91.slots.default;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "month-view" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "month-selector" },
    });
    const __VLS_92 = {}.ElButtonGroup;
    /** @type {[typeof __VLS_components.ElButtonGroup, typeof __VLS_components.elButtonGroup, typeof __VLS_components.ElButtonGroup, typeof __VLS_components.elButtonGroup, ]} */ ;
    // @ts-ignore
    const __VLS_93 = __VLS_asFunctionalComponent(__VLS_92, new __VLS_92({}));
    const __VLS_94 = __VLS_93({}, ...__VLS_functionalComponentArgsRest(__VLS_93));
    __VLS_95.slots.default;
    const __VLS_96 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_97 = __VLS_asFunctionalComponent(__VLS_96, new __VLS_96({
        ...{ 'onClick': {} },
    }));
    const __VLS_98 = __VLS_97({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_97));
    let __VLS_100;
    let __VLS_101;
    let __VLS_102;
    const __VLS_103 = {
        onClick: (__VLS_ctx.previousMonth)
    };
    __VLS_99.slots.default;
    const __VLS_104 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_105 = __VLS_asFunctionalComponent(__VLS_104, new __VLS_104({}));
    const __VLS_106 = __VLS_105({}, ...__VLS_functionalComponentArgsRest(__VLS_105));
    __VLS_107.slots.default;
    const __VLS_108 = {}.ArrowLeft;
    /** @type {[typeof __VLS_components.ArrowLeft, ]} */ ;
    // @ts-ignore
    const __VLS_109 = __VLS_asFunctionalComponent(__VLS_108, new __VLS_108({}));
    const __VLS_110 = __VLS_109({}, ...__VLS_functionalComponentArgsRest(__VLS_109));
    var __VLS_107;
    var __VLS_99;
    const __VLS_112 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_113 = __VLS_asFunctionalComponent(__VLS_112, new __VLS_112({
        disabled: true,
    }));
    const __VLS_114 = __VLS_113({
        disabled: true,
    }, ...__VLS_functionalComponentArgsRest(__VLS_113));
    __VLS_115.slots.default;
    (__VLS_ctx.currentYear);
    (String(__VLS_ctx.currentMonth).padStart(2, '0'));
    var __VLS_115;
    const __VLS_116 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_117 = __VLS_asFunctionalComponent(__VLS_116, new __VLS_116({
        ...{ 'onClick': {} },
    }));
    const __VLS_118 = __VLS_117({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_117));
    let __VLS_120;
    let __VLS_121;
    let __VLS_122;
    const __VLS_123 = {
        onClick: (__VLS_ctx.nextMonth)
    };
    __VLS_119.slots.default;
    const __VLS_124 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_125 = __VLS_asFunctionalComponent(__VLS_124, new __VLS_124({}));
    const __VLS_126 = __VLS_125({}, ...__VLS_functionalComponentArgsRest(__VLS_125));
    __VLS_127.slots.default;
    const __VLS_128 = {}.ArrowRight;
    /** @type {[typeof __VLS_components.ArrowRight, ]} */ ;
    // @ts-ignore
    const __VLS_129 = __VLS_asFunctionalComponent(__VLS_128, new __VLS_128({}));
    const __VLS_130 = __VLS_129({}, ...__VLS_functionalComponentArgsRest(__VLS_129));
    var __VLS_127;
    var __VLS_119;
    var __VLS_95;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "month-actions" },
    });
    const __VLS_132 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_133 = __VLS_asFunctionalComponent(__VLS_132, new __VLS_132({
        ...{ 'onClick': {} },
    }));
    const __VLS_134 = __VLS_133({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_133));
    let __VLS_136;
    let __VLS_137;
    let __VLS_138;
    const __VLS_139 = {
        onClick: (...[$event]) => {
            if (!(__VLS_ctx.selectedCalendar))
                return;
            __VLS_ctx.showBatchWeekendDialog = true;
        }
    };
    __VLS_135.slots.default;
    const __VLS_140 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_141 = __VLS_asFunctionalComponent(__VLS_140, new __VLS_140({}));
    const __VLS_142 = __VLS_141({}, ...__VLS_functionalComponentArgsRest(__VLS_141));
    __VLS_143.slots.default;
    const __VLS_144 = {}.Calendar;
    /** @type {[typeof __VLS_components.Calendar, ]} */ ;
    // @ts-ignore
    const __VLS_145 = __VLS_asFunctionalComponent(__VLS_144, new __VLS_144({}));
    const __VLS_146 = __VLS_145({}, ...__VLS_functionalComponentArgsRest(__VLS_145));
    var __VLS_143;
    var __VLS_135;
    const __VLS_148 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_149 = __VLS_asFunctionalComponent(__VLS_148, new __VLS_148({
        ...{ 'onClick': {} },
        type: "primary",
    }));
    const __VLS_150 = __VLS_149({
        ...{ 'onClick': {} },
        type: "primary",
    }, ...__VLS_functionalComponentArgsRest(__VLS_149));
    let __VLS_152;
    let __VLS_153;
    let __VLS_154;
    const __VLS_155 = {
        onClick: (...[$event]) => {
            if (!(__VLS_ctx.selectedCalendar))
                return;
            __VLS_ctx.showBatchHolidayDialog = true;
        }
    };
    __VLS_151.slots.default;
    const __VLS_156 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_157 = __VLS_asFunctionalComponent(__VLS_156, new __VLS_156({}));
    const __VLS_158 = __VLS_157({}, ...__VLS_functionalComponentArgsRest(__VLS_157));
    __VLS_159.slots.default;
    const __VLS_160 = {}.Calendar;
    /** @type {[typeof __VLS_components.Calendar, ]} */ ;
    // @ts-ignore
    const __VLS_161 = __VLS_asFunctionalComponent(__VLS_160, new __VLS_160({}));
    const __VLS_162 = __VLS_161({}, ...__VLS_functionalComponentArgsRest(__VLS_161));
    var __VLS_159;
    var __VLS_151;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "calendar-grid" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "weekday-header" },
    });
    for (const [day] of __VLS_getVForSourceType((__VLS_ctx.weekdays))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            key: (day),
            ...{ class: "weekday" },
        });
        (day);
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "date-cells" },
    });
    for (const [item] of __VLS_getVForSourceType((__VLS_ctx.calendarGridCells))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ onClick: (...[$event]) => {
                    if (!(__VLS_ctx.selectedCalendar))
                        return;
                    item.date && __VLS_ctx.selectDate(item.date);
                } },
            ...{ onContextmenu: (...[$event]) => {
                    if (!(__VLS_ctx.selectedCalendar))
                        return;
                    item.date && __VLS_ctx.showDateContextMenu($event, item.date);
                } },
            key: (item.key),
            ...{ class: (item.date ? ['date-cell', __VLS_ctx.getDateClass(item.date)] : ['date-cell', 'placeholder']) },
        });
        if (item.date) {
            __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                ...{ class: "date-number" },
            });
            (__VLS_ctx.getDayOfMonth(item.date.date));
            __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                ...{ class: "date-type-badge" },
            });
            (__VLS_ctx.getDateTypeLabel(item.date.dateType));
            if (item.date.label) {
                __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                    ...{ class: "date-label" },
                });
                (item.date.label);
            }
        }
    }
    if (__VLS_ctx.contextMenuVisible) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ onClick: (...[$event]) => {
                    if (!(__VLS_ctx.selectedCalendar))
                        return;
                    if (!(__VLS_ctx.contextMenuVisible))
                        return;
                    __VLS_ctx.contextMenuVisible = false;
                } },
            ...{ style: ({ left: __VLS_ctx.contextMenuX + 'px', top: __VLS_ctx.contextMenuY + 'px' }) },
            ...{ class: "context-menu" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ onClick: (...[$event]) => {
                    if (!(__VLS_ctx.selectedCalendar))
                        return;
                    if (!(__VLS_ctx.contextMenuVisible))
                        return;
                    __VLS_ctx.quickSetDateType('WORKDAY');
                } },
            ...{ class: "menu-item" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "type-badge workday-badge" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ onClick: (...[$event]) => {
                    if (!(__VLS_ctx.selectedCalendar))
                        return;
                    if (!(__VLS_ctx.contextMenuVisible))
                        return;
                    __VLS_ctx.quickSetDateType('RESTDAY');
                } },
            ...{ class: "menu-item" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "type-badge restday-badge" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ onClick: (...[$event]) => {
                    if (!(__VLS_ctx.selectedCalendar))
                        return;
                    if (!(__VLS_ctx.contextMenuVisible))
                        return;
                    __VLS_ctx.quickSetDateType('HOLIDAY');
                } },
            ...{ class: "menu-item" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "type-badge holiday-badge" },
        });
    }
    if (__VLS_ctx.selectedDate) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "date-editor" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "editor-header" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        (__VLS_ctx.selectedDate.date);
        const __VLS_164 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_165 = __VLS_asFunctionalComponent(__VLS_164, new __VLS_164({
            ...{ 'onClick': {} },
            text: true,
            size: "small",
        }));
        const __VLS_166 = __VLS_165({
            ...{ 'onClick': {} },
            text: true,
            size: "small",
        }, ...__VLS_functionalComponentArgsRest(__VLS_165));
        let __VLS_168;
        let __VLS_169;
        let __VLS_170;
        const __VLS_171 = {
            onClick: (...[$event]) => {
                if (!(__VLS_ctx.selectedCalendar))
                    return;
                if (!(__VLS_ctx.selectedDate))
                    return;
                __VLS_ctx.selectedDate = null;
            }
        };
        __VLS_167.slots.default;
        const __VLS_172 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_173 = __VLS_asFunctionalComponent(__VLS_172, new __VLS_172({}));
        const __VLS_174 = __VLS_173({}, ...__VLS_functionalComponentArgsRest(__VLS_173));
        __VLS_175.slots.default;
        const __VLS_176 = {}.Close;
        /** @type {[typeof __VLS_components.Close, ]} */ ;
        // @ts-ignore
        const __VLS_177 = __VLS_asFunctionalComponent(__VLS_176, new __VLS_176({}));
        const __VLS_178 = __VLS_177({}, ...__VLS_functionalComponentArgsRest(__VLS_177));
        var __VLS_175;
        var __VLS_167;
        const __VLS_180 = {}.ElForm;
        /** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
        // @ts-ignore
        const __VLS_181 = __VLS_asFunctionalComponent(__VLS_180, new __VLS_180({
            model: (__VLS_ctx.dateForm),
            labelWidth: "80px",
            size: "small",
        }));
        const __VLS_182 = __VLS_181({
            model: (__VLS_ctx.dateForm),
            labelWidth: "80px",
            size: "small",
        }, ...__VLS_functionalComponentArgsRest(__VLS_181));
        __VLS_183.slots.default;
        const __VLS_184 = {}.ElFormItem;
        /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
        // @ts-ignore
        const __VLS_185 = __VLS_asFunctionalComponent(__VLS_184, new __VLS_184({
            label: "日期类型",
        }));
        const __VLS_186 = __VLS_185({
            label: "日期类型",
        }, ...__VLS_functionalComponentArgsRest(__VLS_185));
        __VLS_187.slots.default;
        const __VLS_188 = {}.ElSelect;
        /** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
        // @ts-ignore
        const __VLS_189 = __VLS_asFunctionalComponent(__VLS_188, new __VLS_188({
            modelValue: (__VLS_ctx.dateForm.dateType),
        }));
        const __VLS_190 = __VLS_189({
            modelValue: (__VLS_ctx.dateForm.dateType),
        }, ...__VLS_functionalComponentArgsRest(__VLS_189));
        __VLS_191.slots.default;
        for (const [item] of __VLS_getVForSourceType((__VLS_ctx.dateTypeItems))) {
            const __VLS_192 = {}.ElOption;
            /** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
            // @ts-ignore
            const __VLS_193 = __VLS_asFunctionalComponent(__VLS_192, new __VLS_192({
                key: (item.itemCode),
                label: (item.itemName),
                value: (item.itemCode),
            }));
            const __VLS_194 = __VLS_193({
                key: (item.itemCode),
                label: (item.itemName),
                value: (item.itemCode),
            }, ...__VLS_functionalComponentArgsRest(__VLS_193));
        }
        var __VLS_191;
        var __VLS_187;
        const __VLS_196 = {}.ElFormItem;
        /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
        // @ts-ignore
        const __VLS_197 = __VLS_asFunctionalComponent(__VLS_196, new __VLS_196({
            label: "标签",
        }));
        const __VLS_198 = __VLS_197({
            label: "标签",
        }, ...__VLS_functionalComponentArgsRest(__VLS_197));
        __VLS_199.slots.default;
        const __VLS_200 = {}.ElInput;
        /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
        // @ts-ignore
        const __VLS_201 = __VLS_asFunctionalComponent(__VLS_200, new __VLS_200({
            modelValue: (__VLS_ctx.dateForm.label),
            placeholder: "如：春节",
        }));
        const __VLS_202 = __VLS_201({
            modelValue: (__VLS_ctx.dateForm.label),
            placeholder: "如：春节",
        }, ...__VLS_functionalComponentArgsRest(__VLS_201));
        var __VLS_199;
        const __VLS_204 = {}.ElFormItem;
        /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
        // @ts-ignore
        const __VLS_205 = __VLS_asFunctionalComponent(__VLS_204, new __VLS_204({}));
        const __VLS_206 = __VLS_205({}, ...__VLS_functionalComponentArgsRest(__VLS_205));
        __VLS_207.slots.default;
        const __VLS_208 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_209 = __VLS_asFunctionalComponent(__VLS_208, new __VLS_208({
            ...{ 'onClick': {} },
            type: "primary",
            size: "small",
        }));
        const __VLS_210 = __VLS_209({
            ...{ 'onClick': {} },
            type: "primary",
            size: "small",
        }, ...__VLS_functionalComponentArgsRest(__VLS_209));
        let __VLS_212;
        let __VLS_213;
        let __VLS_214;
        const __VLS_215 = {
            onClick: (__VLS_ctx.saveDateType)
        };
        __VLS_211.slots.default;
        var __VLS_211;
        var __VLS_207;
        var __VLS_183;
    }
    var __VLS_91;
    const __VLS_216 = {}.ElTabPane;
    /** @type {[typeof __VLS_components.ElTabPane, typeof __VLS_components.elTabPane, typeof __VLS_components.ElTabPane, typeof __VLS_components.elTabPane, ]} */ ;
    // @ts-ignore
    const __VLS_217 = __VLS_asFunctionalComponent(__VLS_216, new __VLS_216({
        label: "班次配置",
        name: "shifts",
    }));
    const __VLS_218 = __VLS_217({
        label: "班次配置",
        name: "shifts",
    }, ...__VLS_functionalComponentArgsRest(__VLS_217));
    __VLS_219.slots.default;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "shifts-view" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "shifts-header" },
    });
    const __VLS_220 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_221 = __VLS_asFunctionalComponent(__VLS_220, new __VLS_220({
        ...{ 'onClick': {} },
        type: "primary",
        size: "small",
    }));
    const __VLS_222 = __VLS_221({
        ...{ 'onClick': {} },
        type: "primary",
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_221));
    let __VLS_224;
    let __VLS_225;
    let __VLS_226;
    const __VLS_227 = {
        onClick: (__VLS_ctx.handleAddShift)
    };
    __VLS_223.slots.default;
    const __VLS_228 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_229 = __VLS_asFunctionalComponent(__VLS_228, new __VLS_228({}));
    const __VLS_230 = __VLS_229({}, ...__VLS_functionalComponentArgsRest(__VLS_229));
    __VLS_231.slots.default;
    const __VLS_232 = {}.Plus;
    /** @type {[typeof __VLS_components.Plus, ]} */ ;
    // @ts-ignore
    const __VLS_233 = __VLS_asFunctionalComponent(__VLS_232, new __VLS_232({}));
    const __VLS_234 = __VLS_233({}, ...__VLS_functionalComponentArgsRest(__VLS_233));
    var __VLS_231;
    var __VLS_223;
    const __VLS_236 = {}.ElTable;
    /** @type {[typeof __VLS_components.ElTable, typeof __VLS_components.elTable, typeof __VLS_components.ElTable, typeof __VLS_components.elTable, ]} */ ;
    // @ts-ignore
    const __VLS_237 = __VLS_asFunctionalComponent(__VLS_236, new __VLS_236({
        data: (__VLS_ctx.shifts),
        stripe: true,
        size: "small",
        ...{ class: "shifts-table" },
    }));
    const __VLS_238 = __VLS_237({
        data: (__VLS_ctx.shifts),
        stripe: true,
        size: "small",
        ...{ class: "shifts-table" },
    }, ...__VLS_functionalComponentArgsRest(__VLS_237));
    __VLS_239.slots.default;
    const __VLS_240 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_241 = __VLS_asFunctionalComponent(__VLS_240, new __VLS_240({
        prop: "name",
        label: "班次名称",
        width: "120",
    }));
    const __VLS_242 = __VLS_241({
        prop: "name",
        label: "班次名称",
        width: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_241));
    const __VLS_244 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_245 = __VLS_asFunctionalComponent(__VLS_244, new __VLS_244({
        prop: "startTime",
        label: "开始时间",
        width: "120",
    }));
    const __VLS_246 = __VLS_245({
        prop: "startTime",
        label: "开始时间",
        width: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_245));
    const __VLS_248 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_249 = __VLS_asFunctionalComponent(__VLS_248, new __VLS_248({
        prop: "endTime",
        label: "结束时间",
        width: "120",
    }));
    const __VLS_250 = __VLS_249({
        prop: "endTime",
        label: "结束时间",
        width: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_249));
    const __VLS_252 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_253 = __VLS_asFunctionalComponent(__VLS_252, new __VLS_252({
        label: "休息时长",
        width: "120",
    }));
    const __VLS_254 = __VLS_253({
        label: "休息时长",
        width: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_253));
    __VLS_255.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_255.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        (row.breakMinutes || 0);
    }
    var __VLS_255;
    const __VLS_256 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_257 = __VLS_asFunctionalComponent(__VLS_256, new __VLS_256({
        label: "有效时长",
        width: "120",
    }));
    const __VLS_258 = __VLS_257({
        label: "有效时长",
        width: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_257));
    __VLS_259.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_259.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        (__VLS_ctx.formatShiftEffectiveHours(row));
    }
    var __VLS_259;
    const __VLS_260 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_261 = __VLS_asFunctionalComponent(__VLS_260, new __VLS_260({
        prop: "sortOrder",
        label: "排序",
        width: "80",
    }));
    const __VLS_262 = __VLS_261({
        prop: "sortOrder",
        label: "排序",
        width: "80",
    }, ...__VLS_functionalComponentArgsRest(__VLS_261));
    const __VLS_264 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_265 = __VLS_asFunctionalComponent(__VLS_264, new __VLS_264({
        label: "操作",
        width: "120",
    }));
    const __VLS_266 = __VLS_265({
        label: "操作",
        width: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_265));
    __VLS_267.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_267.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        const __VLS_268 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_269 = __VLS_asFunctionalComponent(__VLS_268, new __VLS_268({
            ...{ 'onClick': {} },
            text: true,
            size: "small",
        }));
        const __VLS_270 = __VLS_269({
            ...{ 'onClick': {} },
            text: true,
            size: "small",
        }, ...__VLS_functionalComponentArgsRest(__VLS_269));
        let __VLS_272;
        let __VLS_273;
        let __VLS_274;
        const __VLS_275 = {
            onClick: (...[$event]) => {
                if (!(__VLS_ctx.selectedCalendar))
                    return;
                __VLS_ctx.handleEditShift(row);
            }
        };
        __VLS_271.slots.default;
        const __VLS_276 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_277 = __VLS_asFunctionalComponent(__VLS_276, new __VLS_276({}));
        const __VLS_278 = __VLS_277({}, ...__VLS_functionalComponentArgsRest(__VLS_277));
        __VLS_279.slots.default;
        const __VLS_280 = {}.Edit;
        /** @type {[typeof __VLS_components.Edit, ]} */ ;
        // @ts-ignore
        const __VLS_281 = __VLS_asFunctionalComponent(__VLS_280, new __VLS_280({}));
        const __VLS_282 = __VLS_281({}, ...__VLS_functionalComponentArgsRest(__VLS_281));
        var __VLS_279;
        var __VLS_271;
        const __VLS_284 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_285 = __VLS_asFunctionalComponent(__VLS_284, new __VLS_284({
            ...{ 'onClick': {} },
            text: true,
            size: "small",
        }));
        const __VLS_286 = __VLS_285({
            ...{ 'onClick': {} },
            text: true,
            size: "small",
        }, ...__VLS_functionalComponentArgsRest(__VLS_285));
        let __VLS_288;
        let __VLS_289;
        let __VLS_290;
        const __VLS_291 = {
            onClick: (...[$event]) => {
                if (!(__VLS_ctx.selectedCalendar))
                    return;
                __VLS_ctx.handleDeleteShift(row);
            }
        };
        __VLS_287.slots.default;
        const __VLS_292 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_293 = __VLS_asFunctionalComponent(__VLS_292, new __VLS_292({}));
        const __VLS_294 = __VLS_293({}, ...__VLS_functionalComponentArgsRest(__VLS_293));
        __VLS_295.slots.default;
        const __VLS_296 = {}.Delete;
        /** @type {[typeof __VLS_components.Delete, ]} */ ;
        // @ts-ignore
        const __VLS_297 = __VLS_asFunctionalComponent(__VLS_296, new __VLS_296({}));
        const __VLS_298 = __VLS_297({}, ...__VLS_functionalComponentArgsRest(__VLS_297));
        var __VLS_295;
        var __VLS_287;
    }
    var __VLS_267;
    var __VLS_239;
    if (__VLS_ctx.shifts.length === 0) {
        const __VLS_300 = {}.ElEmpty;
        /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
        // @ts-ignore
        const __VLS_301 = __VLS_asFunctionalComponent(__VLS_300, new __VLS_300({
            description: "暂无班次",
        }));
        const __VLS_302 = __VLS_301({
            description: "暂无班次",
        }, ...__VLS_functionalComponentArgsRest(__VLS_301));
    }
    var __VLS_219;
    var __VLS_87;
}
else {
    const __VLS_304 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_305 = __VLS_asFunctionalComponent(__VLS_304, new __VLS_304({
        description: "请选择左侧日历查看详情",
    }));
    const __VLS_306 = __VLS_305({
        description: "请选择左侧日历查看详情",
    }, ...__VLS_functionalComponentArgsRest(__VLS_305));
}
const __VLS_308 = {}.ElDialog;
/** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
// @ts-ignore
const __VLS_309 = __VLS_asFunctionalComponent(__VLS_308, new __VLS_308({
    modelValue: (__VLS_ctx.showCalendarDialog),
    title: (__VLS_ctx.editingCalendar ? '编辑日历' : '新增日历'),
    width: "500px",
}));
const __VLS_310 = __VLS_309({
    modelValue: (__VLS_ctx.showCalendarDialog),
    title: (__VLS_ctx.editingCalendar ? '编辑日历' : '新增日历'),
    width: "500px",
}, ...__VLS_functionalComponentArgsRest(__VLS_309));
__VLS_311.slots.default;
const __VLS_312 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_313 = __VLS_asFunctionalComponent(__VLS_312, new __VLS_312({
    ref: "calendarFormRef",
    model: (__VLS_ctx.calendarForm),
    rules: (__VLS_ctx.calendarRules),
    labelWidth: "100px",
}));
const __VLS_314 = __VLS_313({
    ref: "calendarFormRef",
    model: (__VLS_ctx.calendarForm),
    rules: (__VLS_ctx.calendarRules),
    labelWidth: "100px",
}, ...__VLS_functionalComponentArgsRest(__VLS_313));
/** @type {typeof __VLS_ctx.calendarFormRef} */ ;
var __VLS_316 = {};
__VLS_315.slots.default;
const __VLS_318 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_319 = __VLS_asFunctionalComponent(__VLS_318, new __VLS_318({
    label: "日历名称",
    prop: "name",
}));
const __VLS_320 = __VLS_319({
    label: "日历名称",
    prop: "name",
}, ...__VLS_functionalComponentArgsRest(__VLS_319));
__VLS_321.slots.default;
const __VLS_322 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_323 = __VLS_asFunctionalComponent(__VLS_322, new __VLS_322({
    modelValue: (__VLS_ctx.calendarForm.name),
    placeholder: "请输入日历名称",
}));
const __VLS_324 = __VLS_323({
    modelValue: (__VLS_ctx.calendarForm.name),
    placeholder: "请输入日历名称",
}, ...__VLS_functionalComponentArgsRest(__VLS_323));
var __VLS_321;
const __VLS_326 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_327 = __VLS_asFunctionalComponent(__VLS_326, new __VLS_326({
    label: "日历代码",
    prop: "code",
}));
const __VLS_328 = __VLS_327({
    label: "日历代码",
    prop: "code",
}, ...__VLS_functionalComponentArgsRest(__VLS_327));
__VLS_329.slots.default;
const __VLS_330 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_331 = __VLS_asFunctionalComponent(__VLS_330, new __VLS_330({
    modelValue: (__VLS_ctx.calendarForm.code),
    disabled: (!!__VLS_ctx.editingCalendar),
    placeholder: "请输入日历代码",
}));
const __VLS_332 = __VLS_331({
    modelValue: (__VLS_ctx.calendarForm.code),
    disabled: (!!__VLS_ctx.editingCalendar),
    placeholder: "请输入日历代码",
}, ...__VLS_functionalComponentArgsRest(__VLS_331));
var __VLS_329;
const __VLS_334 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_335 = __VLS_asFunctionalComponent(__VLS_334, new __VLS_334({
    label: "年份",
    prop: "year",
}));
const __VLS_336 = __VLS_335({
    label: "年份",
    prop: "year",
}, ...__VLS_functionalComponentArgsRest(__VLS_335));
__VLS_337.slots.default;
const __VLS_338 = {}.ElInputNumber;
/** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
// @ts-ignore
const __VLS_339 = __VLS_asFunctionalComponent(__VLS_338, new __VLS_338({
    modelValue: (__VLS_ctx.calendarForm.year),
    min: (2020),
    max: (2099),
}));
const __VLS_340 = __VLS_339({
    modelValue: (__VLS_ctx.calendarForm.year),
    min: (2020),
    max: (2099),
}, ...__VLS_functionalComponentArgsRest(__VLS_339));
var __VLS_337;
const __VLS_342 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_343 = __VLS_asFunctionalComponent(__VLS_342, new __VLS_342({
    label: "描述",
    prop: "description",
}));
const __VLS_344 = __VLS_343({
    label: "描述",
    prop: "description",
}, ...__VLS_functionalComponentArgsRest(__VLS_343));
__VLS_345.slots.default;
const __VLS_346 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_347 = __VLS_asFunctionalComponent(__VLS_346, new __VLS_346({
    modelValue: (__VLS_ctx.calendarForm.description),
    type: "textarea",
    rows: (3),
    placeholder: "请输入描述",
}));
const __VLS_348 = __VLS_347({
    modelValue: (__VLS_ctx.calendarForm.description),
    type: "textarea",
    rows: (3),
    placeholder: "请输入描述",
}, ...__VLS_functionalComponentArgsRest(__VLS_347));
var __VLS_345;
var __VLS_315;
{
    const { footer: __VLS_thisSlot } = __VLS_311.slots;
    const __VLS_350 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_351 = __VLS_asFunctionalComponent(__VLS_350, new __VLS_350({
        ...{ 'onClick': {} },
    }));
    const __VLS_352 = __VLS_351({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_351));
    let __VLS_354;
    let __VLS_355;
    let __VLS_356;
    const __VLS_357 = {
        onClick: (...[$event]) => {
            __VLS_ctx.showCalendarDialog = false;
        }
    };
    __VLS_353.slots.default;
    var __VLS_353;
    const __VLS_358 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_359 = __VLS_asFunctionalComponent(__VLS_358, new __VLS_358({
        ...{ 'onClick': {} },
        type: "primary",
    }));
    const __VLS_360 = __VLS_359({
        ...{ 'onClick': {} },
        type: "primary",
    }, ...__VLS_functionalComponentArgsRest(__VLS_359));
    let __VLS_362;
    let __VLS_363;
    let __VLS_364;
    const __VLS_365 = {
        onClick: (__VLS_ctx.saveCalendar)
    };
    __VLS_361.slots.default;
    var __VLS_361;
}
var __VLS_311;
const __VLS_366 = {}.ElDialog;
/** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
// @ts-ignore
const __VLS_367 = __VLS_asFunctionalComponent(__VLS_366, new __VLS_366({
    modelValue: (__VLS_ctx.showShiftDialog),
    title: (__VLS_ctx.editingShift ? '编辑班次' : '添加班次'),
    width: "400px",
}));
const __VLS_368 = __VLS_367({
    modelValue: (__VLS_ctx.showShiftDialog),
    title: (__VLS_ctx.editingShift ? '编辑班次' : '添加班次'),
    width: "400px",
}, ...__VLS_functionalComponentArgsRest(__VLS_367));
__VLS_369.slots.default;
const __VLS_370 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_371 = __VLS_asFunctionalComponent(__VLS_370, new __VLS_370({
    ref: "shiftFormRef",
    model: (__VLS_ctx.shiftForm),
    rules: (__VLS_ctx.shiftRules),
    labelWidth: "100px",
}));
const __VLS_372 = __VLS_371({
    ref: "shiftFormRef",
    model: (__VLS_ctx.shiftForm),
    rules: (__VLS_ctx.shiftRules),
    labelWidth: "100px",
}, ...__VLS_functionalComponentArgsRest(__VLS_371));
/** @type {typeof __VLS_ctx.shiftFormRef} */ ;
var __VLS_374 = {};
__VLS_373.slots.default;
const __VLS_376 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_377 = __VLS_asFunctionalComponent(__VLS_376, new __VLS_376({
    label: "班次名称",
    prop: "name",
}));
const __VLS_378 = __VLS_377({
    label: "班次名称",
    prop: "name",
}, ...__VLS_functionalComponentArgsRest(__VLS_377));
__VLS_379.slots.default;
const __VLS_380 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_381 = __VLS_asFunctionalComponent(__VLS_380, new __VLS_380({
    modelValue: (__VLS_ctx.shiftForm.name),
    placeholder: "如：早班",
}));
const __VLS_382 = __VLS_381({
    modelValue: (__VLS_ctx.shiftForm.name),
    placeholder: "如：早班",
}, ...__VLS_functionalComponentArgsRest(__VLS_381));
var __VLS_379;
const __VLS_384 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_385 = __VLS_asFunctionalComponent(__VLS_384, new __VLS_384({
    label: "开始时间",
    prop: "startTime",
}));
const __VLS_386 = __VLS_385({
    label: "开始时间",
    prop: "startTime",
}, ...__VLS_functionalComponentArgsRest(__VLS_385));
__VLS_387.slots.default;
const __VLS_388 = {}.ElTimePicker;
/** @type {[typeof __VLS_components.ElTimePicker, typeof __VLS_components.elTimePicker, ]} */ ;
// @ts-ignore
const __VLS_389 = __VLS_asFunctionalComponent(__VLS_388, new __VLS_388({
    modelValue: (__VLS_ctx.shiftForm.startTime),
    format: "HH:mm",
    valueFormat: "HH:mm:ss",
}));
const __VLS_390 = __VLS_389({
    modelValue: (__VLS_ctx.shiftForm.startTime),
    format: "HH:mm",
    valueFormat: "HH:mm:ss",
}, ...__VLS_functionalComponentArgsRest(__VLS_389));
var __VLS_387;
const __VLS_392 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_393 = __VLS_asFunctionalComponent(__VLS_392, new __VLS_392({
    label: "结束时间",
    prop: "endTime",
}));
const __VLS_394 = __VLS_393({
    label: "结束时间",
    prop: "endTime",
}, ...__VLS_functionalComponentArgsRest(__VLS_393));
__VLS_395.slots.default;
const __VLS_396 = {}.ElTimePicker;
/** @type {[typeof __VLS_components.ElTimePicker, typeof __VLS_components.elTimePicker, ]} */ ;
// @ts-ignore
const __VLS_397 = __VLS_asFunctionalComponent(__VLS_396, new __VLS_396({
    modelValue: (__VLS_ctx.shiftForm.endTime),
    format: "HH:mm",
    valueFormat: "HH:mm:ss",
}));
const __VLS_398 = __VLS_397({
    modelValue: (__VLS_ctx.shiftForm.endTime),
    format: "HH:mm",
    valueFormat: "HH:mm:ss",
}, ...__VLS_functionalComponentArgsRest(__VLS_397));
var __VLS_395;
const __VLS_400 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_401 = __VLS_asFunctionalComponent(__VLS_400, new __VLS_400({
    label: "休息时长",
    prop: "breakMinutes",
}));
const __VLS_402 = __VLS_401({
    label: "休息时长",
    prop: "breakMinutes",
}, ...__VLS_functionalComponentArgsRest(__VLS_401));
__VLS_403.slots.default;
const __VLS_404 = {}.ElInputNumber;
/** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
// @ts-ignore
const __VLS_405 = __VLS_asFunctionalComponent(__VLS_404, new __VLS_404({
    modelValue: (__VLS_ctx.shiftForm.breakMinutes),
    min: (0),
    step: (15),
}));
const __VLS_406 = __VLS_405({
    modelValue: (__VLS_ctx.shiftForm.breakMinutes),
    min: (0),
    step: (15),
}, ...__VLS_functionalComponentArgsRest(__VLS_405));
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ style: {} },
});
var __VLS_403;
const __VLS_408 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_409 = __VLS_asFunctionalComponent(__VLS_408, new __VLS_408({
    label: "跨天班次",
}));
const __VLS_410 = __VLS_409({
    label: "跨天班次",
}, ...__VLS_functionalComponentArgsRest(__VLS_409));
__VLS_411.slots.default;
const __VLS_412 = {}.ElCheckbox;
/** @type {[typeof __VLS_components.ElCheckbox, typeof __VLS_components.elCheckbox, typeof __VLS_components.ElCheckbox, typeof __VLS_components.elCheckbox, ]} */ ;
// @ts-ignore
const __VLS_413 = __VLS_asFunctionalComponent(__VLS_412, new __VLS_412({
    modelValue: (__VLS_ctx.shiftForm.nextDay),
}));
const __VLS_414 = __VLS_413({
    modelValue: (__VLS_ctx.shiftForm.nextDay),
}, ...__VLS_functionalComponentArgsRest(__VLS_413));
__VLS_415.slots.default;
var __VLS_415;
var __VLS_411;
const __VLS_416 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_417 = __VLS_asFunctionalComponent(__VLS_416, new __VLS_416({
    label: "排序",
    prop: "sortOrder",
}));
const __VLS_418 = __VLS_417({
    label: "排序",
    prop: "sortOrder",
}, ...__VLS_functionalComponentArgsRest(__VLS_417));
__VLS_419.slots.default;
const __VLS_420 = {}.ElInputNumber;
/** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
// @ts-ignore
const __VLS_421 = __VLS_asFunctionalComponent(__VLS_420, new __VLS_420({
    modelValue: (__VLS_ctx.shiftForm.sortOrder),
    min: (0),
}));
const __VLS_422 = __VLS_421({
    modelValue: (__VLS_ctx.shiftForm.sortOrder),
    min: (0),
}, ...__VLS_functionalComponentArgsRest(__VLS_421));
var __VLS_419;
var __VLS_373;
{
    const { footer: __VLS_thisSlot } = __VLS_369.slots;
    const __VLS_424 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_425 = __VLS_asFunctionalComponent(__VLS_424, new __VLS_424({
        ...{ 'onClick': {} },
    }));
    const __VLS_426 = __VLS_425({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_425));
    let __VLS_428;
    let __VLS_429;
    let __VLS_430;
    const __VLS_431 = {
        onClick: (...[$event]) => {
            __VLS_ctx.showShiftDialog = false;
        }
    };
    __VLS_427.slots.default;
    var __VLS_427;
    const __VLS_432 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_433 = __VLS_asFunctionalComponent(__VLS_432, new __VLS_432({
        ...{ 'onClick': {} },
        type: "primary",
    }));
    const __VLS_434 = __VLS_433({
        ...{ 'onClick': {} },
        type: "primary",
    }, ...__VLS_functionalComponentArgsRest(__VLS_433));
    let __VLS_436;
    let __VLS_437;
    let __VLS_438;
    const __VLS_439 = {
        onClick: (__VLS_ctx.saveShift)
    };
    __VLS_435.slots.default;
    var __VLS_435;
}
var __VLS_369;
const __VLS_440 = {}.ElDialog;
/** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
// @ts-ignore
const __VLS_441 = __VLS_asFunctionalComponent(__VLS_440, new __VLS_440({
    modelValue: (__VLS_ctx.showBatchHolidayDialog),
    title: "批量设置法定节假日",
    width: "600px",
}));
const __VLS_442 = __VLS_441({
    modelValue: (__VLS_ctx.showBatchHolidayDialog),
    title: "批量设置法定节假日",
    width: "600px",
}, ...__VLS_functionalComponentArgsRest(__VLS_441));
__VLS_443.slots.default;
const __VLS_444 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_445 = __VLS_asFunctionalComponent(__VLS_444, new __VLS_444({
    model: (__VLS_ctx.batchHolidayForm),
    labelWidth: "100px",
}));
const __VLS_446 = __VLS_445({
    model: (__VLS_ctx.batchHolidayForm),
    labelWidth: "100px",
}, ...__VLS_functionalComponentArgsRest(__VLS_445));
__VLS_447.slots.default;
const __VLS_448 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_449 = __VLS_asFunctionalComponent(__VLS_448, new __VLS_448({
    label: "选择年份",
}));
const __VLS_450 = __VLS_449({
    label: "选择年份",
}, ...__VLS_functionalComponentArgsRest(__VLS_449));
__VLS_451.slots.default;
const __VLS_452 = {}.ElSelect;
/** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
// @ts-ignore
const __VLS_453 = __VLS_asFunctionalComponent(__VLS_452, new __VLS_452({
    modelValue: (__VLS_ctx.batchHolidayForm.year),
    placeholder: "请选择年份",
}));
const __VLS_454 = __VLS_453({
    modelValue: (__VLS_ctx.batchHolidayForm.year),
    placeholder: "请选择年份",
}, ...__VLS_functionalComponentArgsRest(__VLS_453));
__VLS_455.slots.default;
for (const [year] of __VLS_getVForSourceType((__VLS_ctx.SUPPORTED_YEARS))) {
    const __VLS_456 = {}.ElOption;
    /** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
    // @ts-ignore
    const __VLS_457 = __VLS_asFunctionalComponent(__VLS_456, new __VLS_456({
        key: (year),
        label: (year),
        value: (year),
    }));
    const __VLS_458 = __VLS_457({
        key: (year),
        label: (year),
        value: (year),
    }, ...__VLS_functionalComponentArgsRest(__VLS_457));
}
var __VLS_455;
var __VLS_451;
const __VLS_460 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_461 = __VLS_asFunctionalComponent(__VLS_460, new __VLS_460({
    label: "节假日模板",
}));
const __VLS_462 = __VLS_461({
    label: "节假日模板",
}, ...__VLS_functionalComponentArgsRest(__VLS_461));
__VLS_463.slots.default;
const __VLS_464 = {}.ElSelect;
/** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
// @ts-ignore
const __VLS_465 = __VLS_asFunctionalComponent(__VLS_464, new __VLS_464({
    modelValue: (__VLS_ctx.batchHolidayForm.template),
    placeholder: "选择预设模板",
}));
const __VLS_466 = __VLS_465({
    modelValue: (__VLS_ctx.batchHolidayForm.template),
    placeholder: "选择预设模板",
}, ...__VLS_functionalComponentArgsRest(__VLS_465));
__VLS_467.slots.default;
for (const [template] of __VLS_getVForSourceType((__VLS_ctx.holidayTemplates))) {
    const __VLS_468 = {}.ElOption;
    /** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
    // @ts-ignore
    const __VLS_469 = __VLS_asFunctionalComponent(__VLS_468, new __VLS_468({
        key: (template.key),
        label: (template.name),
        value: (template.key),
    }));
    const __VLS_470 = __VLS_469({
        key: (template.key),
        label: (template.name),
        value: (template.key),
    }, ...__VLS_functionalComponentArgsRest(__VLS_469));
    __VLS_471.slots.default;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    (template.name);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ style: {} },
    });
    (template.description);
    var __VLS_471;
}
const __VLS_472 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_473 = __VLS_asFunctionalComponent(__VLS_472, new __VLS_472({
    label: "自定义日期",
    value: "custom",
}));
const __VLS_474 = __VLS_473({
    label: "自定义日期",
    value: "custom",
}, ...__VLS_functionalComponentArgsRest(__VLS_473));
var __VLS_467;
var __VLS_463;
if (__VLS_ctx.batchHolidayForm.template === 'custom') {
    const __VLS_476 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_477 = __VLS_asFunctionalComponent(__VLS_476, new __VLS_476({
        label: "选择日期",
    }));
    const __VLS_478 = __VLS_477({
        label: "选择日期",
    }, ...__VLS_functionalComponentArgsRest(__VLS_477));
    __VLS_479.slots.default;
    const __VLS_480 = {}.ElDatePicker;
    /** @type {[typeof __VLS_components.ElDatePicker, typeof __VLS_components.elDatePicker, ]} */ ;
    // @ts-ignore
    const __VLS_481 = __VLS_asFunctionalComponent(__VLS_480, new __VLS_480({
        modelValue: (__VLS_ctx.batchHolidayForm.customDates),
        type: "daterange",
        rangeSeparator: "至",
        startPlaceholder: "开始日期",
        endPlaceholder: "结束日期",
        valueFormat: "YYYY-MM-DD",
    }));
    const __VLS_482 = __VLS_481({
        modelValue: (__VLS_ctx.batchHolidayForm.customDates),
        type: "daterange",
        rangeSeparator: "至",
        startPlaceholder: "开始日期",
        endPlaceholder: "结束日期",
        valueFormat: "YYYY-MM-DD",
    }, ...__VLS_functionalComponentArgsRest(__VLS_481));
    var __VLS_479;
}
const __VLS_484 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_485 = __VLS_asFunctionalComponent(__VLS_484, new __VLS_484({
    label: "节假日名称",
}));
const __VLS_486 = __VLS_485({
    label: "节假日名称",
}, ...__VLS_functionalComponentArgsRest(__VLS_485));
__VLS_487.slots.default;
const __VLS_488 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_489 = __VLS_asFunctionalComponent(__VLS_488, new __VLS_488({
    modelValue: (__VLS_ctx.batchHolidayForm.label),
    placeholder: "如：春节、清明节等",
}));
const __VLS_490 = __VLS_489({
    modelValue: (__VLS_ctx.batchHolidayForm.label),
    placeholder: "如：春节、清明节等",
}, ...__VLS_functionalComponentArgsRest(__VLS_489));
var __VLS_487;
const __VLS_492 = {}.ElAlert;
/** @type {[typeof __VLS_components.ElAlert, typeof __VLS_components.elAlert, ]} */ ;
// @ts-ignore
const __VLS_493 = __VLS_asFunctionalComponent(__VLS_492, new __VLS_492({
    title: "提示",
    type: "info",
    description: "将选定日期设置为节假日（休息日）。系统会自动根据模板填充常见法定节假日日期。",
    closable: (false),
    ...{ style: {} },
}));
const __VLS_494 = __VLS_493({
    title: "提示",
    type: "info",
    description: "将选定日期设置为节假日（休息日）。系统会自动根据模板填充常见法定节假日日期。",
    closable: (false),
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_493));
var __VLS_447;
{
    const { footer: __VLS_thisSlot } = __VLS_443.slots;
    const __VLS_496 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_497 = __VLS_asFunctionalComponent(__VLS_496, new __VLS_496({
        ...{ 'onClick': {} },
    }));
    const __VLS_498 = __VLS_497({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_497));
    let __VLS_500;
    let __VLS_501;
    let __VLS_502;
    const __VLS_503 = {
        onClick: (...[$event]) => {
            __VLS_ctx.showBatchHolidayDialog = false;
        }
    };
    __VLS_499.slots.default;
    var __VLS_499;
    const __VLS_504 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_505 = __VLS_asFunctionalComponent(__VLS_504, new __VLS_504({
        ...{ 'onClick': {} },
        type: "primary",
    }));
    const __VLS_506 = __VLS_505({
        ...{ 'onClick': {} },
        type: "primary",
    }, ...__VLS_functionalComponentArgsRest(__VLS_505));
    let __VLS_508;
    let __VLS_509;
    let __VLS_510;
    const __VLS_511 = {
        onClick: (__VLS_ctx.saveBatchHolidays)
    };
    __VLS_507.slots.default;
    var __VLS_507;
}
var __VLS_443;
const __VLS_512 = {}.ElDialog;
/** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
// @ts-ignore
const __VLS_513 = __VLS_asFunctionalComponent(__VLS_512, new __VLS_512({
    modelValue: (__VLS_ctx.showBatchWeekendDialog),
    title: "批量设置单双休",
    width: "520px",
}));
const __VLS_514 = __VLS_513({
    modelValue: (__VLS_ctx.showBatchWeekendDialog),
    title: "批量设置单双休",
    width: "520px",
}, ...__VLS_functionalComponentArgsRest(__VLS_513));
__VLS_515.slots.default;
const __VLS_516 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_517 = __VLS_asFunctionalComponent(__VLS_516, new __VLS_516({
    model: (__VLS_ctx.batchWeekendForm),
    labelWidth: "100px",
}));
const __VLS_518 = __VLS_517({
    model: (__VLS_ctx.batchWeekendForm),
    labelWidth: "100px",
}, ...__VLS_functionalComponentArgsRest(__VLS_517));
__VLS_519.slots.default;
const __VLS_520 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_521 = __VLS_asFunctionalComponent(__VLS_520, new __VLS_520({
    label: "应用年份",
}));
const __VLS_522 = __VLS_521({
    label: "应用年份",
}, ...__VLS_functionalComponentArgsRest(__VLS_521));
__VLS_523.slots.default;
const __VLS_524 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_525 = __VLS_asFunctionalComponent(__VLS_524, new __VLS_524({
    modelValue: (__VLS_ctx.selectedCalendar?.year || ''),
    disabled: true,
}));
const __VLS_526 = __VLS_525({
    modelValue: (__VLS_ctx.selectedCalendar?.year || ''),
    disabled: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_525));
var __VLS_523;
const __VLS_528 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_529 = __VLS_asFunctionalComponent(__VLS_528, new __VLS_528({
    label: "休息规则",
}));
const __VLS_530 = __VLS_529({
    label: "休息规则",
}, ...__VLS_functionalComponentArgsRest(__VLS_529));
__VLS_531.slots.default;
const __VLS_532 = {}.ElRadioGroup;
/** @type {[typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, ]} */ ;
// @ts-ignore
const __VLS_533 = __VLS_asFunctionalComponent(__VLS_532, new __VLS_532({
    modelValue: (__VLS_ctx.batchWeekendForm.pattern),
}));
const __VLS_534 = __VLS_533({
    modelValue: (__VLS_ctx.batchWeekendForm.pattern),
}, ...__VLS_functionalComponentArgsRest(__VLS_533));
__VLS_535.slots.default;
const __VLS_536 = {}.ElRadio;
/** @type {[typeof __VLS_components.ElRadio, typeof __VLS_components.elRadio, typeof __VLS_components.ElRadio, typeof __VLS_components.elRadio, ]} */ ;
// @ts-ignore
const __VLS_537 = __VLS_asFunctionalComponent(__VLS_536, new __VLS_536({
    value: "SINGLE",
}));
const __VLS_538 = __VLS_537({
    value: "SINGLE",
}, ...__VLS_functionalComponentArgsRest(__VLS_537));
__VLS_539.slots.default;
var __VLS_539;
const __VLS_540 = {}.ElRadio;
/** @type {[typeof __VLS_components.ElRadio, typeof __VLS_components.elRadio, typeof __VLS_components.ElRadio, typeof __VLS_components.elRadio, ]} */ ;
// @ts-ignore
const __VLS_541 = __VLS_asFunctionalComponent(__VLS_540, new __VLS_540({
    value: "DOUBLE",
}));
const __VLS_542 = __VLS_541({
    value: "DOUBLE",
}, ...__VLS_functionalComponentArgsRest(__VLS_541));
__VLS_543.slots.default;
var __VLS_543;
var __VLS_535;
var __VLS_531;
const __VLS_544 = {}.ElAlert;
/** @type {[typeof __VLS_components.ElAlert, typeof __VLS_components.elAlert, ]} */ ;
// @ts-ignore
const __VLS_545 = __VLS_asFunctionalComponent(__VLS_544, new __VLS_544({
    title: "提示",
    type: "info",
    description: "该操作会按所选日历年份批量重置周末规则，并保留已设置的法定节假日不变。",
    closable: (false),
    ...{ style: {} },
}));
const __VLS_546 = __VLS_545({
    title: "提示",
    type: "info",
    description: "该操作会按所选日历年份批量重置周末规则，并保留已设置的法定节假日不变。",
    closable: (false),
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_545));
var __VLS_519;
{
    const { footer: __VLS_thisSlot } = __VLS_515.slots;
    const __VLS_548 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_549 = __VLS_asFunctionalComponent(__VLS_548, new __VLS_548({
        ...{ 'onClick': {} },
    }));
    const __VLS_550 = __VLS_549({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_549));
    let __VLS_552;
    let __VLS_553;
    let __VLS_554;
    const __VLS_555 = {
        onClick: (...[$event]) => {
            __VLS_ctx.showBatchWeekendDialog = false;
        }
    };
    __VLS_551.slots.default;
    var __VLS_551;
    const __VLS_556 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_557 = __VLS_asFunctionalComponent(__VLS_556, new __VLS_556({
        ...{ 'onClick': {} },
        type: "primary",
    }));
    const __VLS_558 = __VLS_557({
        ...{ 'onClick': {} },
        type: "primary",
    }, ...__VLS_functionalComponentArgsRest(__VLS_557));
    let __VLS_560;
    let __VLS_561;
    let __VLS_562;
    const __VLS_563 = {
        onClick: (__VLS_ctx.saveBatchWeekendPattern)
    };
    __VLS_559.slots.default;
    var __VLS_559;
}
var __VLS_515;
/** @type {__VLS_StyleScopedClasses['factory-calendar-page']} */ ;
/** @type {__VLS_StyleScopedClasses['content-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['left-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-header']} */ ;
/** @type {__VLS_StyleScopedClasses['search-input']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-list']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-header']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-name']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-info']} */ ;
/** @type {__VLS_StyleScopedClasses['info-item']} */ ;
/** @type {__VLS_StyleScopedClasses['label']} */ ;
/** @type {__VLS_StyleScopedClasses['value']} */ ;
/** @type {__VLS_StyleScopedClasses['info-item']} */ ;
/** @type {__VLS_StyleScopedClasses['label']} */ ;
/** @type {__VLS_StyleScopedClasses['value']} */ ;
/** @type {__VLS_StyleScopedClasses['info-item']} */ ;
/** @type {__VLS_StyleScopedClasses['label']} */ ;
/** @type {__VLS_StyleScopedClasses['value']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['right-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-detail']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-tabs']} */ ;
/** @type {__VLS_StyleScopedClasses['month-view']} */ ;
/** @type {__VLS_StyleScopedClasses['month-selector']} */ ;
/** @type {__VLS_StyleScopedClasses['month-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['calendar-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['weekday-header']} */ ;
/** @type {__VLS_StyleScopedClasses['weekday']} */ ;
/** @type {__VLS_StyleScopedClasses['date-cells']} */ ;
/** @type {__VLS_StyleScopedClasses['date-number']} */ ;
/** @type {__VLS_StyleScopedClasses['date-type-badge']} */ ;
/** @type {__VLS_StyleScopedClasses['date-label']} */ ;
/** @type {__VLS_StyleScopedClasses['context-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-item']} */ ;
/** @type {__VLS_StyleScopedClasses['type-badge']} */ ;
/** @type {__VLS_StyleScopedClasses['workday-badge']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-item']} */ ;
/** @type {__VLS_StyleScopedClasses['type-badge']} */ ;
/** @type {__VLS_StyleScopedClasses['restday-badge']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-item']} */ ;
/** @type {__VLS_StyleScopedClasses['type-badge']} */ ;
/** @type {__VLS_StyleScopedClasses['holiday-badge']} */ ;
/** @type {__VLS_StyleScopedClasses['date-editor']} */ ;
/** @type {__VLS_StyleScopedClasses['editor-header']} */ ;
/** @type {__VLS_StyleScopedClasses['shifts-view']} */ ;
/** @type {__VLS_StyleScopedClasses['shifts-header']} */ ;
/** @type {__VLS_StyleScopedClasses['shifts-table']} */ ;
// @ts-ignore
var __VLS_317 = __VLS_316, __VLS_375 = __VLS_374;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            Calendar: Calendar,
            Plus: Plus,
            Search: Search,
            StarFilled: StarFilled,
            Edit: Edit,
            Delete: Delete,
            ArrowLeft: ArrowLeft,
            ArrowRight: ArrowRight,
            Close: Close,
            holidayTemplates: holidayTemplates,
            SUPPORTED_YEARS: SUPPORTED_YEARS,
            loading: loading,
            selectedCalendar: selectedCalendar,
            searchKeyword: searchKeyword,
            activeTab: activeTab,
            dateTypeItems: dateTypeItems,
            showCalendarDialog: showCalendarDialog,
            editingCalendar: editingCalendar,
            calendarFormRef: calendarFormRef,
            calendarForm: calendarForm,
            calendarRules: calendarRules,
            shifts: shifts,
            showShiftDialog: showShiftDialog,
            editingShift: editingShift,
            shiftFormRef: shiftFormRef,
            shiftForm: shiftForm,
            shiftRules: shiftRules,
            formatShiftEffectiveHours: formatShiftEffectiveHours,
            showBatchHolidayDialog: showBatchHolidayDialog,
            batchHolidayForm: batchHolidayForm,
            showBatchWeekendDialog: showBatchWeekendDialog,
            batchWeekendForm: batchWeekendForm,
            currentYear: currentYear,
            currentMonth: currentMonth,
            selectedDate: selectedDate,
            dateForm: dateForm,
            contextMenuVisible: contextMenuVisible,
            contextMenuX: contextMenuX,
            contextMenuY: contextMenuY,
            weekdays: weekdays,
            calendarGridCells: calendarGridCells,
            getDayOfMonth: getDayOfMonth,
            filteredCalendars: filteredCalendars,
            selectCalendar: selectCalendar,
            previousMonth: previousMonth,
            nextMonth: nextMonth,
            getDateClass: getDateClass,
            getDateTypeLabel: getDateTypeLabel,
            selectDate: selectDate,
            saveDateType: saveDateType,
            showDateContextMenu: showDateContextMenu,
            quickSetDateType: quickSetDateType,
            handleCreate: handleCreate,
            handleEdit: handleEdit,
            handleSetDefault: handleSetDefault,
            handleDelete: handleDelete,
            saveCalendar: saveCalendar,
            handleAddShift: handleAddShift,
            handleEditShift: handleEditShift,
            handleDeleteShift: handleDeleteShift,
            saveShift: saveShift,
            saveBatchHolidays: saveBatchHolidays,
            saveBatchWeekendPattern: saveBatchWeekendPattern,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
