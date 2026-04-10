/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue';
import { msgSuccess, msgError } from '@/utils/message';
import { Search, Refresh, Download, Document, User, Operation } from '@element-plus/icons-vue';
import { searchAuditLogs, exportAuditLogs, getStatistics } from '@/api/audit';
import { dictionaryApi } from '@/api/dictionary';
import * as echarts from 'echarts';
const searchForm = reactive({
    username: '',
    action: '',
    resource: '',
    timeRange: []
});
const pagination = reactive({
    page: 1,
    size: 10,
    total: 0
});
const auditLogs = ref([]);
const loading = ref(false);
const totalLogs = ref(0);
const activeUsers = ref(0);
const totalActions = ref(0);
const auditActionItems = ref([]);
const auditActionLabelMap = ref({});
const actionChartRef = ref();
const userChartRef = ref();
let actionChart = null;
let userChart = null;
const loadAuditLogs = async () => {
    loading.value = true;
    try {
        const params = {
            page: pagination.page - 1,
            size: pagination.size
        };
        if (searchForm.username)
            params.username = searchForm.username;
        if (searchForm.action)
            params.action = searchForm.action;
        if (searchForm.resource)
            params.resource = searchForm.resource;
        if (searchForm.timeRange && searchForm.timeRange.length === 2) {
            params.startTime = searchForm.timeRange[0].toISOString();
            params.endTime = searchForm.timeRange[1].toISOString();
        }
        const response = await searchAuditLogs(params);
        if (response.data.code === 200 && response.data.data) {
            auditLogs.value = response.data.data.content;
            pagination.total = response.data.data.totalElements;
            totalLogs.value = response.data.data.totalElements;
        }
    }
    catch (error) {
        msgError('加载审计日志失败');
    }
    finally {
        loading.value = false;
    }
};
const loadStatistics = async () => {
    try {
        let startTime;
        let endTime;
        if (searchForm.timeRange && searchForm.timeRange.length === 2) {
            startTime = searchForm.timeRange[0].toISOString();
            endTime = searchForm.timeRange[1].toISOString();
        }
        const response = await getStatistics(startTime, endTime);
        if (response.data.code === 200 && response.data.data) {
            const stats = response.data.data;
            activeUsers.value = Object.keys(stats.userStatistics).length;
            totalActions.value = Object.keys(stats.actionStatistics).length;
            await nextTick();
            updateActionChart(stats.actionStatistics);
            updateUserChart(stats.userStatistics);
        }
    }
    catch (error) {
        console.error('加载统计数据失败', error);
    }
};
const updateActionChart = (data) => {
    if (!actionChartRef.value)
        return;
    if (!actionChart)
        actionChart = echarts.init(actionChartRef.value);
    const chartData = Object.entries(data).map(([name, value]) => ({
        name: getActionLabel(name),
        value
    }));
    actionChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', right: 10, top: 'center', type: 'scroll' },
        series: [{
                type: 'pie',
                radius: ['40%', '70%'],
                itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
                label: { show: false },
                emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
                data: chartData
            }]
    });
};
const updateUserChart = (data) => {
    if (!userChartRef.value)
        return;
    if (!userChart)
        userChart = echarts.init(userChartRef.value);
    const sortedData = Object.entries(data).sort((a, b) => b[1] - a[1]).slice(0, 10);
    userChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'value' },
        yAxis: { type: 'category', data: sortedData.map(([name]) => name) },
        series: [{
                type: 'bar',
                data: sortedData.map(([, value]) => value),
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                        { offset: 0, color: '#83bff6' },
                        { offset: 1, color: '#188df0' }
                    ])
                },
                barWidth: '60%'
            }]
    });
};
const handleSearch = () => {
    pagination.page = 1;
    loadAuditLogs();
    loadStatistics();
};
const handleReset = () => {
    searchForm.username = '';
    searchForm.action = '';
    searchForm.resource = '';
    searchForm.timeRange = [];
    pagination.page = 1;
    loadAuditLogs();
    loadStatistics();
};
const handleExport = async () => {
    try {
        let startTime;
        let endTime;
        if (searchForm.timeRange && searchForm.timeRange.length === 2) {
            startTime = searchForm.timeRange[0].toISOString();
            endTime = searchForm.timeRange[1].toISOString();
        }
        const response = await exportAuditLogs(startTime, endTime);
        const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `audit_logs_${new Date().getTime()}.csv`;
        link.click();
        window.URL.revokeObjectURL(url);
        msgSuccess('导出成功');
    }
    catch (error) {
        msgError('导出失败');
    }
};
const handleSizeChange = (size) => {
    pagination.size = size;
    loadAuditLogs();
};
const handlePageChange = (page) => {
    pagination.page = page;
    loadAuditLogs();
};
const handleExpandChange = (row) => { };
const formatDate = (dateStr) => {
    if (!dateStr)
        return '';
    const date = new Date(dateStr);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
};
const formatDetails = (details) => {
    if (!details)
        return '无';
    try {
        return JSON.stringify(JSON.parse(details), null, 2);
    }
    catch {
        return details;
    }
};
const getActionTagType = (action) => {
    const typeMap = {
        LOGIN: 'success', LOGOUT: 'info',
        SCHEDULE_CREATE: 'primary', SCHEDULE_UPDATE: 'warning', SCHEDULE_DELETE: 'danger',
        ORDER_CREATE: 'primary', ORDER_UPDATE: 'warning', ORDER_DELETE: 'danger',
        USER_CREATE: 'primary', USER_UPDATE: 'warning', USER_DELETE: 'danger'
    };
    return typeMap[action] || 'info';
};
const getActionLabel = (action) => {
    return auditActionLabelMap.value[action] || action;
};
onMounted(() => {
    loadAuditLogs();
    loadStatistics();
    loadAuditActionDict();
    window.addEventListener('resize', handleResize);
});
async function loadAuditActionDict() {
    try {
        auditActionItems.value = await dictionaryApi.getEnabledItemsByTypeCode('AUDIT_ACTION');
        auditActionLabelMap.value = Object.fromEntries(auditActionItems.value.map(item => [item.itemCode, item.itemName]));
    }
    catch {
        auditActionItems.value = [];
    }
}
onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
    actionChart?.dispose();
    userChart?.dispose();
    actionChart = null;
    userChart = null;
});
const handleResize = () => {
    actionChart?.resize();
    userChart?.resize();
};
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['stat-card']} */ ;
/** @type {__VLS_StyleScopedClasses['expand-content']} */ ;
/** @type {__VLS_StyleScopedClasses['expand-content']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "audit-log-page" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
const __VLS_0 = {}.ElRow;
/** @type {[typeof __VLS_components.ElRow, typeof __VLS_components.elRow, typeof __VLS_components.ElRow, typeof __VLS_components.elRow, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    gutter: (20),
    ...{ class: "stats-row" },
}));
const __VLS_2 = __VLS_1({
    gutter: (20),
    ...{ class: "stats-row" },
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_3.slots.default;
const __VLS_4 = {}.ElCol;
/** @type {[typeof __VLS_components.ElCol, typeof __VLS_components.elCol, typeof __VLS_components.ElCol, typeof __VLS_components.elCol, ]} */ ;
// @ts-ignore
const __VLS_5 = __VLS_asFunctionalComponent(__VLS_4, new __VLS_4({
    span: (8),
}));
const __VLS_6 = __VLS_5({
    span: (8),
}, ...__VLS_functionalComponentArgsRest(__VLS_5));
__VLS_7.slots.default;
const __VLS_8 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({
    shadow: "hover",
    ...{ class: "stat-card" },
}));
const __VLS_10 = __VLS_9({
    shadow: "hover",
    ...{ class: "stat-card" },
}, ...__VLS_functionalComponentArgsRest(__VLS_9));
__VLS_11.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-content" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-icon" },
    ...{ style: {} },
});
const __VLS_12 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_13 = __VLS_asFunctionalComponent(__VLS_12, new __VLS_12({
    size: (32),
}));
const __VLS_14 = __VLS_13({
    size: (32),
}, ...__VLS_functionalComponentArgsRest(__VLS_13));
__VLS_15.slots.default;
const __VLS_16 = {}.Document;
/** @type {[typeof __VLS_components.Document, ]} */ ;
// @ts-ignore
const __VLS_17 = __VLS_asFunctionalComponent(__VLS_16, new __VLS_16({}));
const __VLS_18 = __VLS_17({}, ...__VLS_functionalComponentArgsRest(__VLS_17));
var __VLS_15;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-info" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-value" },
});
(__VLS_ctx.totalLogs);
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-label" },
});
var __VLS_11;
var __VLS_7;
const __VLS_20 = {}.ElCol;
/** @type {[typeof __VLS_components.ElCol, typeof __VLS_components.elCol, typeof __VLS_components.ElCol, typeof __VLS_components.elCol, ]} */ ;
// @ts-ignore
const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({
    span: (8),
}));
const __VLS_22 = __VLS_21({
    span: (8),
}, ...__VLS_functionalComponentArgsRest(__VLS_21));
__VLS_23.slots.default;
const __VLS_24 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_25 = __VLS_asFunctionalComponent(__VLS_24, new __VLS_24({
    shadow: "hover",
    ...{ class: "stat-card" },
}));
const __VLS_26 = __VLS_25({
    shadow: "hover",
    ...{ class: "stat-card" },
}, ...__VLS_functionalComponentArgsRest(__VLS_25));
__VLS_27.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-content" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-icon" },
    ...{ style: {} },
});
const __VLS_28 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
    size: (32),
}));
const __VLS_30 = __VLS_29({
    size: (32),
}, ...__VLS_functionalComponentArgsRest(__VLS_29));
__VLS_31.slots.default;
const __VLS_32 = {}.User;
/** @type {[typeof __VLS_components.User, ]} */ ;
// @ts-ignore
const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({}));
const __VLS_34 = __VLS_33({}, ...__VLS_functionalComponentArgsRest(__VLS_33));
var __VLS_31;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-info" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-value" },
});
(__VLS_ctx.activeUsers);
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-label" },
});
var __VLS_27;
var __VLS_23;
const __VLS_36 = {}.ElCol;
/** @type {[typeof __VLS_components.ElCol, typeof __VLS_components.elCol, typeof __VLS_components.ElCol, typeof __VLS_components.elCol, ]} */ ;
// @ts-ignore
const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({
    span: (8),
}));
const __VLS_38 = __VLS_37({
    span: (8),
}, ...__VLS_functionalComponentArgsRest(__VLS_37));
__VLS_39.slots.default;
const __VLS_40 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
    shadow: "hover",
    ...{ class: "stat-card" },
}));
const __VLS_42 = __VLS_41({
    shadow: "hover",
    ...{ class: "stat-card" },
}, ...__VLS_functionalComponentArgsRest(__VLS_41));
__VLS_43.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-content" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-icon" },
    ...{ style: {} },
});
const __VLS_44 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_45 = __VLS_asFunctionalComponent(__VLS_44, new __VLS_44({
    size: (32),
}));
const __VLS_46 = __VLS_45({
    size: (32),
}, ...__VLS_functionalComponentArgsRest(__VLS_45));
__VLS_47.slots.default;
const __VLS_48 = {}.Operation;
/** @type {[typeof __VLS_components.Operation, ]} */ ;
// @ts-ignore
const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({}));
const __VLS_50 = __VLS_49({}, ...__VLS_functionalComponentArgsRest(__VLS_49));
var __VLS_47;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-info" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-value" },
});
(__VLS_ctx.totalActions);
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-label" },
});
var __VLS_43;
var __VLS_39;
var __VLS_3;
const __VLS_52 = {}.ElRow;
/** @type {[typeof __VLS_components.ElRow, typeof __VLS_components.elRow, typeof __VLS_components.ElRow, typeof __VLS_components.elRow, ]} */ ;
// @ts-ignore
const __VLS_53 = __VLS_asFunctionalComponent(__VLS_52, new __VLS_52({
    gutter: (20),
    ...{ class: "charts-row" },
}));
const __VLS_54 = __VLS_53({
    gutter: (20),
    ...{ class: "charts-row" },
}, ...__VLS_functionalComponentArgsRest(__VLS_53));
__VLS_55.slots.default;
const __VLS_56 = {}.ElCol;
/** @type {[typeof __VLS_components.ElCol, typeof __VLS_components.elCol, typeof __VLS_components.ElCol, typeof __VLS_components.elCol, ]} */ ;
// @ts-ignore
const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({
    span: (12),
}));
const __VLS_58 = __VLS_57({
    span: (12),
}, ...__VLS_functionalComponentArgsRest(__VLS_57));
__VLS_59.slots.default;
const __VLS_60 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_61 = __VLS_asFunctionalComponent(__VLS_60, new __VLS_60({
    shadow: "hover",
}));
const __VLS_62 = __VLS_61({
    shadow: "hover",
}, ...__VLS_functionalComponentArgsRest(__VLS_61));
__VLS_63.slots.default;
{
    const { header: __VLS_thisSlot } = __VLS_63.slots;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ref: "actionChartRef",
    ...{ style: {} },
});
/** @type {typeof __VLS_ctx.actionChartRef} */ ;
var __VLS_63;
var __VLS_59;
const __VLS_64 = {}.ElCol;
/** @type {[typeof __VLS_components.ElCol, typeof __VLS_components.elCol, typeof __VLS_components.ElCol, typeof __VLS_components.elCol, ]} */ ;
// @ts-ignore
const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({
    span: (12),
}));
const __VLS_66 = __VLS_65({
    span: (12),
}, ...__VLS_functionalComponentArgsRest(__VLS_65));
__VLS_67.slots.default;
const __VLS_68 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_69 = __VLS_asFunctionalComponent(__VLS_68, new __VLS_68({
    shadow: "hover",
}));
const __VLS_70 = __VLS_69({
    shadow: "hover",
}, ...__VLS_functionalComponentArgsRest(__VLS_69));
__VLS_71.slots.default;
{
    const { header: __VLS_thisSlot } = __VLS_71.slots;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ref: "userChartRef",
    ...{ style: {} },
});
/** @type {typeof __VLS_ctx.userChartRef} */ ;
var __VLS_71;
var __VLS_67;
var __VLS_55;
const __VLS_72 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_73 = __VLS_asFunctionalComponent(__VLS_72, new __VLS_72({
    shadow: "hover",
}));
const __VLS_74 = __VLS_73({
    shadow: "hover",
}, ...__VLS_functionalComponentArgsRest(__VLS_73));
__VLS_75.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "search-toolbar" },
});
const __VLS_76 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_77 = __VLS_asFunctionalComponent(__VLS_76, new __VLS_76({
    inline: (true),
    model: (__VLS_ctx.searchForm),
    ...{ class: "search-form" },
}));
const __VLS_78 = __VLS_77({
    inline: (true),
    model: (__VLS_ctx.searchForm),
    ...{ class: "search-form" },
}, ...__VLS_functionalComponentArgsRest(__VLS_77));
__VLS_79.slots.default;
const __VLS_80 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_81 = __VLS_asFunctionalComponent(__VLS_80, new __VLS_80({
    label: "用户名",
}));
const __VLS_82 = __VLS_81({
    label: "用户名",
}, ...__VLS_functionalComponentArgsRest(__VLS_81));
__VLS_83.slots.default;
const __VLS_84 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_85 = __VLS_asFunctionalComponent(__VLS_84, new __VLS_84({
    modelValue: (__VLS_ctx.searchForm.username),
    placeholder: "请输入用户名",
    clearable: true,
    ...{ style: {} },
}));
const __VLS_86 = __VLS_85({
    modelValue: (__VLS_ctx.searchForm.username),
    placeholder: "请输入用户名",
    clearable: true,
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_85));
var __VLS_83;
const __VLS_88 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_89 = __VLS_asFunctionalComponent(__VLS_88, new __VLS_88({
    label: "操作类型",
}));
const __VLS_90 = __VLS_89({
    label: "操作类型",
}, ...__VLS_functionalComponentArgsRest(__VLS_89));
__VLS_91.slots.default;
const __VLS_92 = {}.ElSelect;
/** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
// @ts-ignore
const __VLS_93 = __VLS_asFunctionalComponent(__VLS_92, new __VLS_92({
    modelValue: (__VLS_ctx.searchForm.action),
    placeholder: "请选择操作类型",
    clearable: true,
    ...{ style: {} },
}));
const __VLS_94 = __VLS_93({
    modelValue: (__VLS_ctx.searchForm.action),
    placeholder: "请选择操作类型",
    clearable: true,
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_93));
__VLS_95.slots.default;
const __VLS_96 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_97 = __VLS_asFunctionalComponent(__VLS_96, new __VLS_96({
    label: "全部",
    value: "",
}));
const __VLS_98 = __VLS_97({
    label: "全部",
    value: "",
}, ...__VLS_functionalComponentArgsRest(__VLS_97));
for (const [item] of __VLS_getVForSourceType((__VLS_ctx.auditActionItems))) {
    const __VLS_100 = {}.ElOption;
    /** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
    // @ts-ignore
    const __VLS_101 = __VLS_asFunctionalComponent(__VLS_100, new __VLS_100({
        key: (item.itemCode),
        label: (item.itemName),
        value: (item.itemCode),
    }));
    const __VLS_102 = __VLS_101({
        key: (item.itemCode),
        label: (item.itemName),
        value: (item.itemCode),
    }, ...__VLS_functionalComponentArgsRest(__VLS_101));
}
var __VLS_95;
var __VLS_91;
const __VLS_104 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_105 = __VLS_asFunctionalComponent(__VLS_104, new __VLS_104({
    label: "资源类型",
}));
const __VLS_106 = __VLS_105({
    label: "资源类型",
}, ...__VLS_functionalComponentArgsRest(__VLS_105));
__VLS_107.slots.default;
const __VLS_108 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_109 = __VLS_asFunctionalComponent(__VLS_108, new __VLS_108({
    modelValue: (__VLS_ctx.searchForm.resource),
    placeholder: "请输入资源类型",
    clearable: true,
    ...{ style: {} },
}));
const __VLS_110 = __VLS_109({
    modelValue: (__VLS_ctx.searchForm.resource),
    placeholder: "请输入资源类型",
    clearable: true,
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_109));
var __VLS_107;
const __VLS_112 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_113 = __VLS_asFunctionalComponent(__VLS_112, new __VLS_112({
    label: "时间范围",
}));
const __VLS_114 = __VLS_113({
    label: "时间范围",
}, ...__VLS_functionalComponentArgsRest(__VLS_113));
__VLS_115.slots.default;
const __VLS_116 = {}.ElDatePicker;
/** @type {[typeof __VLS_components.ElDatePicker, typeof __VLS_components.elDatePicker, ]} */ ;
// @ts-ignore
const __VLS_117 = __VLS_asFunctionalComponent(__VLS_116, new __VLS_116({
    modelValue: (__VLS_ctx.searchForm.timeRange),
    type: "datetimerange",
    rangeSeparator: "至",
    startPlaceholder: "开始时间",
    endPlaceholder: "结束时间",
    ...{ style: {} },
}));
const __VLS_118 = __VLS_117({
    modelValue: (__VLS_ctx.searchForm.timeRange),
    type: "datetimerange",
    rangeSeparator: "至",
    startPlaceholder: "开始时间",
    endPlaceholder: "结束时间",
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_117));
var __VLS_115;
const __VLS_120 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_121 = __VLS_asFunctionalComponent(__VLS_120, new __VLS_120({}));
const __VLS_122 = __VLS_121({}, ...__VLS_functionalComponentArgsRest(__VLS_121));
__VLS_123.slots.default;
const __VLS_124 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_125 = __VLS_asFunctionalComponent(__VLS_124, new __VLS_124({
    ...{ 'onClick': {} },
    type: "primary",
}));
const __VLS_126 = __VLS_125({
    ...{ 'onClick': {} },
    type: "primary",
}, ...__VLS_functionalComponentArgsRest(__VLS_125));
let __VLS_128;
let __VLS_129;
let __VLS_130;
const __VLS_131 = {
    onClick: (__VLS_ctx.handleSearch)
};
__VLS_127.slots.default;
const __VLS_132 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_133 = __VLS_asFunctionalComponent(__VLS_132, new __VLS_132({}));
const __VLS_134 = __VLS_133({}, ...__VLS_functionalComponentArgsRest(__VLS_133));
__VLS_135.slots.default;
const __VLS_136 = {}.Search;
/** @type {[typeof __VLS_components.Search, ]} */ ;
// @ts-ignore
const __VLS_137 = __VLS_asFunctionalComponent(__VLS_136, new __VLS_136({}));
const __VLS_138 = __VLS_137({}, ...__VLS_functionalComponentArgsRest(__VLS_137));
var __VLS_135;
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
var __VLS_127;
const __VLS_140 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_141 = __VLS_asFunctionalComponent(__VLS_140, new __VLS_140({
    ...{ 'onClick': {} },
}));
const __VLS_142 = __VLS_141({
    ...{ 'onClick': {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_141));
let __VLS_144;
let __VLS_145;
let __VLS_146;
const __VLS_147 = {
    onClick: (__VLS_ctx.handleReset)
};
__VLS_143.slots.default;
const __VLS_148 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_149 = __VLS_asFunctionalComponent(__VLS_148, new __VLS_148({}));
const __VLS_150 = __VLS_149({}, ...__VLS_functionalComponentArgsRest(__VLS_149));
__VLS_151.slots.default;
const __VLS_152 = {}.Refresh;
/** @type {[typeof __VLS_components.Refresh, ]} */ ;
// @ts-ignore
const __VLS_153 = __VLS_asFunctionalComponent(__VLS_152, new __VLS_152({}));
const __VLS_154 = __VLS_153({}, ...__VLS_functionalComponentArgsRest(__VLS_153));
var __VLS_151;
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
var __VLS_143;
const __VLS_156 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_157 = __VLS_asFunctionalComponent(__VLS_156, new __VLS_156({
    ...{ 'onClick': {} },
    type: "success",
}));
const __VLS_158 = __VLS_157({
    ...{ 'onClick': {} },
    type: "success",
}, ...__VLS_functionalComponentArgsRest(__VLS_157));
let __VLS_160;
let __VLS_161;
let __VLS_162;
const __VLS_163 = {
    onClick: (__VLS_ctx.handleExport)
};
__VLS_159.slots.default;
const __VLS_164 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_165 = __VLS_asFunctionalComponent(__VLS_164, new __VLS_164({}));
const __VLS_166 = __VLS_165({}, ...__VLS_functionalComponentArgsRest(__VLS_165));
__VLS_167.slots.default;
const __VLS_168 = {}.Download;
/** @type {[typeof __VLS_components.Download, ]} */ ;
// @ts-ignore
const __VLS_169 = __VLS_asFunctionalComponent(__VLS_168, new __VLS_168({}));
const __VLS_170 = __VLS_169({}, ...__VLS_functionalComponentArgsRest(__VLS_169));
var __VLS_167;
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
var __VLS_159;
var __VLS_123;
var __VLS_79;
const __VLS_172 = {}.ElTable;
/** @type {[typeof __VLS_components.ElTable, typeof __VLS_components.elTable, typeof __VLS_components.ElTable, typeof __VLS_components.elTable, ]} */ ;
// @ts-ignore
const __VLS_173 = __VLS_asFunctionalComponent(__VLS_172, new __VLS_172({
    ...{ 'onExpandChange': {} },
    data: (__VLS_ctx.auditLogs),
    ...{ style: {} },
}));
const __VLS_174 = __VLS_173({
    ...{ 'onExpandChange': {} },
    data: (__VLS_ctx.auditLogs),
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_173));
let __VLS_176;
let __VLS_177;
let __VLS_178;
const __VLS_179 = {
    onExpandChange: (__VLS_ctx.handleExpandChange)
};
__VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.loading) }, null, null);
__VLS_175.slots.default;
const __VLS_180 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_181 = __VLS_asFunctionalComponent(__VLS_180, new __VLS_180({
    type: "expand",
}));
const __VLS_182 = __VLS_181({
    type: "expand",
}, ...__VLS_functionalComponentArgsRest(__VLS_181));
__VLS_183.slots.default;
{
    const { default: __VLS_thisSlot } = __VLS_183.slots;
    const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "expand-content" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h4, __VLS_intrinsicElements.h4)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.pre, __VLS_intrinsicElements.pre)({});
    (__VLS_ctx.formatDetails(row.details));
}
var __VLS_183;
const __VLS_184 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_185 = __VLS_asFunctionalComponent(__VLS_184, new __VLS_184({
    prop: "timestamp",
    label: "操作时间",
    minWidth: "160",
    sortable: true,
}));
const __VLS_186 = __VLS_185({
    prop: "timestamp",
    label: "操作时间",
    minWidth: "160",
    sortable: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_185));
__VLS_187.slots.default;
{
    const { default: __VLS_thisSlot } = __VLS_187.slots;
    const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
    (__VLS_ctx.formatDate(row.timestamp));
}
var __VLS_187;
const __VLS_188 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_189 = __VLS_asFunctionalComponent(__VLS_188, new __VLS_188({
    prop: "username",
    label: "用户名",
    minWidth: "120",
}));
const __VLS_190 = __VLS_189({
    prop: "username",
    label: "用户名",
    minWidth: "120",
}, ...__VLS_functionalComponentArgsRest(__VLS_189));
const __VLS_192 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_193 = __VLS_asFunctionalComponent(__VLS_192, new __VLS_192({
    prop: "action",
    label: "操作类型",
    minWidth: "140",
}));
const __VLS_194 = __VLS_193({
    prop: "action",
    label: "操作类型",
    minWidth: "140",
}, ...__VLS_functionalComponentArgsRest(__VLS_193));
__VLS_195.slots.default;
{
    const { default: __VLS_thisSlot } = __VLS_195.slots;
    const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
    const __VLS_196 = {}.ElTag;
    /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
    // @ts-ignore
    const __VLS_197 = __VLS_asFunctionalComponent(__VLS_196, new __VLS_196({
        type: (__VLS_ctx.getActionTagType(row.action)),
        size: "small",
    }));
    const __VLS_198 = __VLS_197({
        type: (__VLS_ctx.getActionTagType(row.action)),
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_197));
    __VLS_199.slots.default;
    (__VLS_ctx.getActionLabel(row.action));
    var __VLS_199;
}
var __VLS_195;
const __VLS_200 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_201 = __VLS_asFunctionalComponent(__VLS_200, new __VLS_200({
    prop: "resource",
    label: "资源类型",
    minWidth: "120",
}));
const __VLS_202 = __VLS_201({
    prop: "resource",
    label: "资源类型",
    minWidth: "120",
}, ...__VLS_functionalComponentArgsRest(__VLS_201));
const __VLS_204 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_205 = __VLS_asFunctionalComponent(__VLS_204, new __VLS_204({
    prop: "ipAddress",
    label: "IP地址",
    minWidth: "140",
}));
const __VLS_206 = __VLS_205({
    prop: "ipAddress",
    label: "IP地址",
    minWidth: "140",
}, ...__VLS_functionalComponentArgsRest(__VLS_205));
var __VLS_175;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "pagination" },
});
const __VLS_208 = {}.ElPagination;
/** @type {[typeof __VLS_components.ElPagination, typeof __VLS_components.elPagination, ]} */ ;
// @ts-ignore
const __VLS_209 = __VLS_asFunctionalComponent(__VLS_208, new __VLS_208({
    ...{ 'onSizeChange': {} },
    ...{ 'onCurrentChange': {} },
    currentPage: (__VLS_ctx.pagination.page),
    pageSize: (__VLS_ctx.pagination.size),
    pageSizes: ([10, 20, 50, 100]),
    total: (__VLS_ctx.pagination.total),
    layout: "total, sizes, prev, pager, next, jumper",
}));
const __VLS_210 = __VLS_209({
    ...{ 'onSizeChange': {} },
    ...{ 'onCurrentChange': {} },
    currentPage: (__VLS_ctx.pagination.page),
    pageSize: (__VLS_ctx.pagination.size),
    pageSizes: ([10, 20, 50, 100]),
    total: (__VLS_ctx.pagination.total),
    layout: "total, sizes, prev, pager, next, jumper",
}, ...__VLS_functionalComponentArgsRest(__VLS_209));
let __VLS_212;
let __VLS_213;
let __VLS_214;
const __VLS_215 = {
    onSizeChange: (__VLS_ctx.handleSizeChange)
};
const __VLS_216 = {
    onCurrentChange: (__VLS_ctx.handlePageChange)
};
var __VLS_211;
var __VLS_75;
/** @type {__VLS_StyleScopedClasses['audit-log-page']} */ ;
/** @type {__VLS_StyleScopedClasses['stats-row']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-card']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-content']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-info']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-value']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-label']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-card']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-content']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-info']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-value']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-label']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-card']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-content']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-info']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-value']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-label']} */ ;
/** @type {__VLS_StyleScopedClasses['charts-row']} */ ;
/** @type {__VLS_StyleScopedClasses['card-header']} */ ;
/** @type {__VLS_StyleScopedClasses['card-header']} */ ;
/** @type {__VLS_StyleScopedClasses['search-toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['search-form']} */ ;
/** @type {__VLS_StyleScopedClasses['expand-content']} */ ;
/** @type {__VLS_StyleScopedClasses['pagination']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            Search: Search,
            Refresh: Refresh,
            Download: Download,
            Document: Document,
            User: User,
            Operation: Operation,
            searchForm: searchForm,
            pagination: pagination,
            auditLogs: auditLogs,
            loading: loading,
            totalLogs: totalLogs,
            activeUsers: activeUsers,
            totalActions: totalActions,
            auditActionItems: auditActionItems,
            actionChartRef: actionChartRef,
            userChartRef: userChartRef,
            handleSearch: handleSearch,
            handleReset: handleReset,
            handleExport: handleExport,
            handleSizeChange: handleSizeChange,
            handlePageChange: handlePageChange,
            handleExpandChange: handleExpandChange,
            formatDate: formatDate,
            formatDetails: formatDetails,
            getActionTagType: getActionTagType,
            getActionLabel: getActionLabel,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
