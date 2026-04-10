/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { onMounted, ref } from 'vue';
import { msgSuccess, msgError, confirmDanger, extractErrorMsg } from '@/utils/message';
import { Delete, Edit, Plus } from '@element-plus/icons-vue';
import { factoryCalendarApi } from '@/api/factoryCalendar';
import { resourceApi, workshopApi } from '@/api/workshop';
import { dictionaryApi } from '@/api/dictionary';
const loadingWorkshops = ref(false);
const loadingResources = ref(false);
const workshops = ref([]);
const resources = ref([]);
const factoryCalendars = ref([]);
const selectedWorkshop = ref(null);
const effectiveCalendarName = ref('默认日历');
const machineStatusItems = ref([]);
const machineStatusLabelMap = ref({});
const showWorkshopDialog = ref(false);
const editingWorkshop = ref(null);
const workshopFormRef = ref();
const workshopForm = ref({
    code: '',
    name: '',
    managerName: '',
    calendarId: null,
    sortOrder: 0,
    description: '',
    enabled: true
});
const workshopRules = {
    code: [{ required: true, message: '请输入车间编码', trigger: 'blur' }],
    name: [{ required: true, message: '请输入车间名称', trigger: 'blur' }]
};
const showMachineDialog = ref(false);
const editingMachine = ref(null);
const machineFormRef = ref();
const machineForm = ref({
    resourceCode: '',
    resourceName: '',
    resourceType: 'INJECTION_MACHINE',
    tonnage: undefined,
    machineBrand: '',
    machineModel: '',
    maxShotWeight: undefined,
    status: 'IDLE',
    calendarId: null,
    available: true
});
const machineRules = {
    resourceCode: [{ required: true, message: '请输入资源编码', trigger: 'blur' }],
    resourceName: [{ required: true, message: '请输入资源名称', trigger: 'blur' }]
};
onMounted(async () => {
    await Promise.all([loadWorkshops(), loadFactoryCalendars(), loadMachineStatusDict()]);
});
async function loadMachineStatusDict() {
    try {
        machineStatusItems.value = await dictionaryApi.getEnabledItemsByTypeCode('MACHINE_STATUS');
        machineStatusLabelMap.value = Object.fromEntries(machineStatusItems.value.map(item => [item.itemCode, item.itemName]));
    }
    catch {
        machineStatusItems.value = [];
    }
}
async function loadFactoryCalendars() {
    try {
        factoryCalendars.value = await factoryCalendarApi.getCalendars();
    }
    catch (error) {
        msgError(error instanceof Error ? error.message : '加载工厂日历失败');
    }
}
async function loadWorkshops() {
    loadingWorkshops.value = true;
    try {
        workshops.value = await workshopApi.getAll();
        if (workshops.value.length > 0) {
            const next = selectedWorkshop.value
                ? workshops.value.find(item => item.id === selectedWorkshop.value?.id) ?? workshops.value[0]
                : workshops.value[0];
            await selectWorkshop(next);
        }
        else {
            selectedWorkshop.value = null;
            resources.value = [];
            effectiveCalendarName.value = '默认日历';
        }
    }
    catch (error) {
        msgError(error instanceof Error ? error.message : '加载车间失败');
    }
    finally {
        loadingWorkshops.value = false;
    }
}
async function selectWorkshop(workshop) {
    selectedWorkshop.value = workshop;
    await Promise.all([loadResources(workshop.id), loadEffectiveCalendar(workshop.id)]);
}
async function loadResources(workshopId) {
    loadingResources.value = true;
    try {
        resources.value = await resourceApi.getAll({ workshopId });
    }
    catch (error) {
        msgError(error instanceof Error ? error.message : '加载注塑机失败');
    }
    finally {
        loadingResources.value = false;
    }
}
async function loadEffectiveCalendar(workshopId) {
    try {
        const calendar = await workshopApi.getEffectiveCalendar(workshopId);
        effectiveCalendarName.value = calendar?.name || '默认日历';
    }
    catch {
        effectiveCalendarName.value = '默认日历';
    }
}
function openWorkshopDialog(workshop) {
    editingWorkshop.value = workshop ?? null;
    workshopForm.value = workshop
        ? {
            code: workshop.code,
            name: workshop.name,
            managerName: workshop.managerName || '',
            calendarId: workshop.calendarId,
            sortOrder: workshop.sortOrder || 0,
            description: workshop.description || '',
            enabled: workshop.enabled
        }
        : {
            code: '',
            name: '',
            managerName: '',
            calendarId: null,
            sortOrder: 0,
            description: '',
            enabled: true
        };
    showWorkshopDialog.value = true;
}
async function saveWorkshop() {
    if (!workshopFormRef.value)
        return;
    await workshopFormRef.value.validate(async (valid) => {
        if (!valid)
            return;
        try {
            if (editingWorkshop.value) {
                await workshopApi.update(editingWorkshop.value.id, workshopForm.value);
                msgSuccess('车间已更新');
            }
            else {
                await workshopApi.create(workshopForm.value);
                msgSuccess('车间已创建');
            }
            showWorkshopDialog.value = false;
            await loadWorkshops();
        }
        catch (error) {
            msgError(error instanceof Error ? error.message : '保存车间失败');
        }
    });
}
async function deleteWorkshop(workshop) {
    try {
        await confirmDanger(`确定删除车间“${workshop.name}”吗？`);
        await workshopApi.delete(workshop.id);
        msgSuccess('车间已删除');
        await loadWorkshops();
    }
    catch (error) {
        if (error !== 'cancel') {
            msgError(extractErrorMsg(error, '删除车间失败'));
        }
    }
}
function openMachineDialog(machine) {
    editingMachine.value = machine ?? null;
    machineForm.value = machine
        ? {
            resourceCode: machine.resourceCode,
            resourceName: machine.resourceName,
            resourceType: machine.resourceType || 'INJECTION_MACHINE',
            tonnage: machine.tonnage ?? undefined,
            machineBrand: machine.machineBrand || '',
            machineModel: machine.machineModel || '',
            maxShotWeight: machine.maxShotWeight ?? undefined,
            status: machine.status || 'IDLE',
            calendarId: machine.calendarId,
            available: machine.available
        }
        : {
            resourceCode: '',
            resourceName: '',
            resourceType: 'INJECTION_MACHINE',
            tonnage: undefined,
            machineBrand: '',
            machineModel: '',
            maxShotWeight: undefined,
            status: 'IDLE',
            calendarId: null,
            available: true
        };
    showMachineDialog.value = true;
}
async function saveMachine() {
    if (!machineFormRef.value || !selectedWorkshop.value)
        return;
    await machineFormRef.value.validate(async (valid) => {
        if (!valid)
            return;
        try {
            const payload = {
                ...machineForm.value,
                workshopId: selectedWorkshop.value.id
            };
            if (editingMachine.value) {
                await resourceApi.update(editingMachine.value.id, payload);
                msgSuccess('注塑机已更新');
            }
            else {
                await resourceApi.create(payload);
                msgSuccess('注塑机已创建');
            }
            showMachineDialog.value = false;
            await Promise.all([
                loadResources(selectedWorkshop.value.id),
                loadWorkshops()
            ]);
        }
        catch (error) {
            msgError(error instanceof Error ? error.message : '保存注塑机失败');
        }
    });
}
async function deleteMachine(machine) {
    try {
        await confirmDanger(`确定删除注塑机“${machine.resourceName}”吗？`);
        await resourceApi.delete(machine.id);
        msgSuccess('注塑机已删除');
        if (selectedWorkshop.value) {
            await loadResources(selectedWorkshop.value.id);
        }
    }
    catch (error) {
        if (error !== 'cancel') {
            msgError(extractErrorMsg(error, '删除注塑机失败'));
        }
    }
}
function getStatusLabel(status) {
    if (!status)
        return '-';
    return machineStatusLabelMap.value[status] ?? status;
}
function getStatusTagType(status) {
    switch (status) {
        case 'RUNNING':
            return 'success';
        case 'IDLE':
            return 'info';
        case 'MAINTENANCE':
            return 'warning';
        case 'DISABLED':
            return 'danger';
        default:
            return 'info';
    }
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
/** @type {__VLS_StyleScopedClasses['left-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['right-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['workshop-card']} */ ;
/** @type {__VLS_StyleScopedClasses['workshop-card']} */ ;
/** @type {__VLS_StyleScopedClasses['workshop-card-header']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-header']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-subtitle']} */ ;
/** @type {__VLS_StyleScopedClasses['machine-toolbar']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "workshop-management-page" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "page-header" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
const __VLS_0 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    ...{ 'onClick': {} },
    type: "primary",
}));
const __VLS_2 = __VLS_1({
    ...{ 'onClick': {} },
    type: "primary",
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
let __VLS_4;
let __VLS_5;
let __VLS_6;
const __VLS_7 = {
    onClick: (...[$event]) => {
        __VLS_ctx.openWorkshopDialog();
    }
};
__VLS_3.slots.default;
const __VLS_8 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({}));
const __VLS_10 = __VLS_9({}, ...__VLS_functionalComponentArgsRest(__VLS_9));
__VLS_11.slots.default;
const __VLS_12 = {}.Plus;
/** @type {[typeof __VLS_components.Plus, ]} */ ;
// @ts-ignore
const __VLS_13 = __VLS_asFunctionalComponent(__VLS_12, new __VLS_12({}));
const __VLS_14 = __VLS_13({}, ...__VLS_functionalComponentArgsRest(__VLS_13));
var __VLS_11;
var __VLS_3;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "content-wrapper" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "left-panel" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "panel-title" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "workshop-list" },
});
__VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.loadingWorkshops) }, null, null);
for (const [workshop] of __VLS_getVForSourceType((__VLS_ctx.workshops))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.selectWorkshop(workshop);
            } },
        key: (workshop.id),
        ...{ class: (['workshop-card', { active: __VLS_ctx.selectedWorkshop?.id === workshop.id }]) },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "workshop-card-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "workshop-name" },
    });
    (workshop.name);
    const __VLS_16 = {}.ElTag;
    /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
    // @ts-ignore
    const __VLS_17 = __VLS_asFunctionalComponent(__VLS_16, new __VLS_16({
        type: (workshop.enabled ? 'success' : 'info'),
        size: "small",
    }));
    const __VLS_18 = __VLS_17({
        type: (workshop.enabled ? 'success' : 'info'),
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_17));
    __VLS_19.slots.default;
    (workshop.enabled ? '启用' : '停用');
    var __VLS_19;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "workshop-meta" },
    });
    (workshop.code);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "workshop-meta" },
    });
    (workshop.managerName || '-');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "workshop-meta" },
    });
    (workshop.calendarName || '默认日历');
}
if (!__VLS_ctx.workshops.length) {
    const __VLS_20 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({
        description: "暂无车间",
    }));
    const __VLS_22 = __VLS_21({
        description: "暂无车间",
    }, ...__VLS_functionalComponentArgsRest(__VLS_21));
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "right-panel" },
});
if (__VLS_ctx.selectedWorkshop) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
    (__VLS_ctx.selectedWorkshop.name);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-subtitle" },
    });
    (__VLS_ctx.selectedWorkshop.code);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "separator" },
    });
    (__VLS_ctx.effectiveCalendarName);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "separator" },
    });
    (__VLS_ctx.selectedWorkshop.managerName || '-');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-actions" },
    });
    const __VLS_24 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_25 = __VLS_asFunctionalComponent(__VLS_24, new __VLS_24({
        ...{ 'onClick': {} },
    }));
    const __VLS_26 = __VLS_25({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_25));
    let __VLS_28;
    let __VLS_29;
    let __VLS_30;
    const __VLS_31 = {
        onClick: (...[$event]) => {
            if (!(__VLS_ctx.selectedWorkshop))
                return;
            __VLS_ctx.openWorkshopDialog(__VLS_ctx.selectedWorkshop);
        }
    };
    __VLS_27.slots.default;
    const __VLS_32 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({}));
    const __VLS_34 = __VLS_33({}, ...__VLS_functionalComponentArgsRest(__VLS_33));
    __VLS_35.slots.default;
    const __VLS_36 = {}.Edit;
    /** @type {[typeof __VLS_components.Edit, ]} */ ;
    // @ts-ignore
    const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({}));
    const __VLS_38 = __VLS_37({}, ...__VLS_functionalComponentArgsRest(__VLS_37));
    var __VLS_35;
    var __VLS_27;
    const __VLS_40 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
        ...{ 'onClick': {} },
        type: "danger",
        plain: true,
    }));
    const __VLS_42 = __VLS_41({
        ...{ 'onClick': {} },
        type: "danger",
        plain: true,
    }, ...__VLS_functionalComponentArgsRest(__VLS_41));
    let __VLS_44;
    let __VLS_45;
    let __VLS_46;
    const __VLS_47 = {
        onClick: (...[$event]) => {
            if (!(__VLS_ctx.selectedWorkshop))
                return;
            __VLS_ctx.deleteWorkshop(__VLS_ctx.selectedWorkshop);
        }
    };
    __VLS_43.slots.default;
    const __VLS_48 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({}));
    const __VLS_50 = __VLS_49({}, ...__VLS_functionalComponentArgsRest(__VLS_49));
    __VLS_51.slots.default;
    const __VLS_52 = {}.Delete;
    /** @type {[typeof __VLS_components.Delete, ]} */ ;
    // @ts-ignore
    const __VLS_53 = __VLS_asFunctionalComponent(__VLS_52, new __VLS_52({}));
    const __VLS_54 = __VLS_53({}, ...__VLS_functionalComponentArgsRest(__VLS_53));
    var __VLS_51;
    var __VLS_43;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "machine-toolbar" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "panel-title" },
    });
    const __VLS_56 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({
        ...{ 'onClick': {} },
        type: "primary",
    }));
    const __VLS_58 = __VLS_57({
        ...{ 'onClick': {} },
        type: "primary",
    }, ...__VLS_functionalComponentArgsRest(__VLS_57));
    let __VLS_60;
    let __VLS_61;
    let __VLS_62;
    const __VLS_63 = {
        onClick: (...[$event]) => {
            if (!(__VLS_ctx.selectedWorkshop))
                return;
            __VLS_ctx.openMachineDialog();
        }
    };
    __VLS_59.slots.default;
    const __VLS_64 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({}));
    const __VLS_66 = __VLS_65({}, ...__VLS_functionalComponentArgsRest(__VLS_65));
    __VLS_67.slots.default;
    const __VLS_68 = {}.Plus;
    /** @type {[typeof __VLS_components.Plus, ]} */ ;
    // @ts-ignore
    const __VLS_69 = __VLS_asFunctionalComponent(__VLS_68, new __VLS_68({}));
    const __VLS_70 = __VLS_69({}, ...__VLS_functionalComponentArgsRest(__VLS_69));
    var __VLS_67;
    var __VLS_59;
    const __VLS_72 = {}.ElTable;
    /** @type {[typeof __VLS_components.ElTable, typeof __VLS_components.elTable, typeof __VLS_components.ElTable, typeof __VLS_components.elTable, ]} */ ;
    // @ts-ignore
    const __VLS_73 = __VLS_asFunctionalComponent(__VLS_72, new __VLS_72({
        data: (__VLS_ctx.resources),
        stripe: true,
    }));
    const __VLS_74 = __VLS_73({
        data: (__VLS_ctx.resources),
        stripe: true,
    }, ...__VLS_functionalComponentArgsRest(__VLS_73));
    __VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.loadingResources) }, null, null);
    __VLS_75.slots.default;
    const __VLS_76 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_77 = __VLS_asFunctionalComponent(__VLS_76, new __VLS_76({
        prop: "resourceCode",
        label: "编码",
        width: "120",
    }));
    const __VLS_78 = __VLS_77({
        prop: "resourceCode",
        label: "编码",
        width: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_77));
    const __VLS_80 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_81 = __VLS_asFunctionalComponent(__VLS_80, new __VLS_80({
        prop: "resourceName",
        label: "名称",
        width: "140",
    }));
    const __VLS_82 = __VLS_81({
        prop: "resourceName",
        label: "名称",
        width: "140",
    }, ...__VLS_functionalComponentArgsRest(__VLS_81));
    const __VLS_84 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_85 = __VLS_asFunctionalComponent(__VLS_84, new __VLS_84({
        prop: "tonnage",
        label: "吨位",
        width: "100",
    }));
    const __VLS_86 = __VLS_85({
        prop: "tonnage",
        label: "吨位",
        width: "100",
    }, ...__VLS_functionalComponentArgsRest(__VLS_85));
    __VLS_87.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_87.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        (row.tonnage ? `${row.tonnage}T` : '-');
    }
    var __VLS_87;
    const __VLS_88 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_89 = __VLS_asFunctionalComponent(__VLS_88, new __VLS_88({
        prop: "machineBrand",
        label: "品牌",
        width: "120",
    }));
    const __VLS_90 = __VLS_89({
        prop: "machineBrand",
        label: "品牌",
        width: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_89));
    const __VLS_92 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_93 = __VLS_asFunctionalComponent(__VLS_92, new __VLS_92({
        prop: "machineModel",
        label: "型号",
        width: "140",
    }));
    const __VLS_94 = __VLS_93({
        prop: "machineModel",
        label: "型号",
        width: "140",
    }, ...__VLS_functionalComponentArgsRest(__VLS_93));
    const __VLS_96 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_97 = __VLS_asFunctionalComponent(__VLS_96, new __VLS_96({
        prop: "status",
        label: "状态",
        width: "120",
    }));
    const __VLS_98 = __VLS_97({
        prop: "status",
        label: "状态",
        width: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_97));
    __VLS_99.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_99.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        const __VLS_100 = {}.ElTag;
        /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
        // @ts-ignore
        const __VLS_101 = __VLS_asFunctionalComponent(__VLS_100, new __VLS_100({
            type: (__VLS_ctx.getStatusTagType(row.status)),
        }));
        const __VLS_102 = __VLS_101({
            type: (__VLS_ctx.getStatusTagType(row.status)),
        }, ...__VLS_functionalComponentArgsRest(__VLS_101));
        __VLS_103.slots.default;
        (__VLS_ctx.getStatusLabel(row.status));
        var __VLS_103;
    }
    var __VLS_99;
    const __VLS_104 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_105 = __VLS_asFunctionalComponent(__VLS_104, new __VLS_104({
        prop: "calendarName",
        label: "设备日历",
        minWidth: "140",
    }));
    const __VLS_106 = __VLS_105({
        prop: "calendarName",
        label: "设备日历",
        minWidth: "140",
    }, ...__VLS_functionalComponentArgsRest(__VLS_105));
    __VLS_107.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_107.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        (row.calendarName || '继承车间/默认');
    }
    var __VLS_107;
    const __VLS_108 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_109 = __VLS_asFunctionalComponent(__VLS_108, new __VLS_108({
        label: "操作",
        width: "160",
        fixed: "right",
    }));
    const __VLS_110 = __VLS_109({
        label: "操作",
        width: "160",
        fixed: "right",
    }, ...__VLS_functionalComponentArgsRest(__VLS_109));
    __VLS_111.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_111.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        const __VLS_112 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_113 = __VLS_asFunctionalComponent(__VLS_112, new __VLS_112({
            ...{ 'onClick': {} },
            text: true,
            type: "primary",
        }));
        const __VLS_114 = __VLS_113({
            ...{ 'onClick': {} },
            text: true,
            type: "primary",
        }, ...__VLS_functionalComponentArgsRest(__VLS_113));
        let __VLS_116;
        let __VLS_117;
        let __VLS_118;
        const __VLS_119 = {
            onClick: (...[$event]) => {
                if (!(__VLS_ctx.selectedWorkshop))
                    return;
                __VLS_ctx.openMachineDialog(row);
            }
        };
        __VLS_115.slots.default;
        var __VLS_115;
        const __VLS_120 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_121 = __VLS_asFunctionalComponent(__VLS_120, new __VLS_120({
            ...{ 'onClick': {} },
            text: true,
            type: "danger",
        }));
        const __VLS_122 = __VLS_121({
            ...{ 'onClick': {} },
            text: true,
            type: "danger",
        }, ...__VLS_functionalComponentArgsRest(__VLS_121));
        let __VLS_124;
        let __VLS_125;
        let __VLS_126;
        const __VLS_127 = {
            onClick: (...[$event]) => {
                if (!(__VLS_ctx.selectedWorkshop))
                    return;
                __VLS_ctx.deleteMachine(row);
            }
        };
        __VLS_123.slots.default;
        var __VLS_123;
    }
    var __VLS_111;
    var __VLS_75;
}
else {
    const __VLS_128 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_129 = __VLS_asFunctionalComponent(__VLS_128, new __VLS_128({
        description: "请选择左侧车间",
    }));
    const __VLS_130 = __VLS_129({
        description: "请选择左侧车间",
    }, ...__VLS_functionalComponentArgsRest(__VLS_129));
}
const __VLS_132 = {}.ElDialog;
/** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
// @ts-ignore
const __VLS_133 = __VLS_asFunctionalComponent(__VLS_132, new __VLS_132({
    modelValue: (__VLS_ctx.showWorkshopDialog),
    title: (__VLS_ctx.editingWorkshop ? '编辑车间' : '新建车间'),
    width: "520px",
}));
const __VLS_134 = __VLS_133({
    modelValue: (__VLS_ctx.showWorkshopDialog),
    title: (__VLS_ctx.editingWorkshop ? '编辑车间' : '新建车间'),
    width: "520px",
}, ...__VLS_functionalComponentArgsRest(__VLS_133));
__VLS_135.slots.default;
const __VLS_136 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_137 = __VLS_asFunctionalComponent(__VLS_136, new __VLS_136({
    ref: "workshopFormRef",
    model: (__VLS_ctx.workshopForm),
    rules: (__VLS_ctx.workshopRules),
    labelWidth: "100px",
}));
const __VLS_138 = __VLS_137({
    ref: "workshopFormRef",
    model: (__VLS_ctx.workshopForm),
    rules: (__VLS_ctx.workshopRules),
    labelWidth: "100px",
}, ...__VLS_functionalComponentArgsRest(__VLS_137));
/** @type {typeof __VLS_ctx.workshopFormRef} */ ;
var __VLS_140 = {};
__VLS_139.slots.default;
const __VLS_142 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_143 = __VLS_asFunctionalComponent(__VLS_142, new __VLS_142({
    label: "车间编码",
    prop: "code",
}));
const __VLS_144 = __VLS_143({
    label: "车间编码",
    prop: "code",
}, ...__VLS_functionalComponentArgsRest(__VLS_143));
__VLS_145.slots.default;
const __VLS_146 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_147 = __VLS_asFunctionalComponent(__VLS_146, new __VLS_146({
    modelValue: (__VLS_ctx.workshopForm.code),
    disabled: (!!__VLS_ctx.editingWorkshop),
}));
const __VLS_148 = __VLS_147({
    modelValue: (__VLS_ctx.workshopForm.code),
    disabled: (!!__VLS_ctx.editingWorkshop),
}, ...__VLS_functionalComponentArgsRest(__VLS_147));
var __VLS_145;
const __VLS_150 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_151 = __VLS_asFunctionalComponent(__VLS_150, new __VLS_150({
    label: "车间名称",
    prop: "name",
}));
const __VLS_152 = __VLS_151({
    label: "车间名称",
    prop: "name",
}, ...__VLS_functionalComponentArgsRest(__VLS_151));
__VLS_153.slots.default;
const __VLS_154 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_155 = __VLS_asFunctionalComponent(__VLS_154, new __VLS_154({
    modelValue: (__VLS_ctx.workshopForm.name),
}));
const __VLS_156 = __VLS_155({
    modelValue: (__VLS_ctx.workshopForm.name),
}, ...__VLS_functionalComponentArgsRest(__VLS_155));
var __VLS_153;
const __VLS_158 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_159 = __VLS_asFunctionalComponent(__VLS_158, new __VLS_158({
    label: "负责人",
}));
const __VLS_160 = __VLS_159({
    label: "负责人",
}, ...__VLS_functionalComponentArgsRest(__VLS_159));
__VLS_161.slots.default;
const __VLS_162 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_163 = __VLS_asFunctionalComponent(__VLS_162, new __VLS_162({
    modelValue: (__VLS_ctx.workshopForm.managerName),
}));
const __VLS_164 = __VLS_163({
    modelValue: (__VLS_ctx.workshopForm.managerName),
}, ...__VLS_functionalComponentArgsRest(__VLS_163));
var __VLS_161;
const __VLS_166 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_167 = __VLS_asFunctionalComponent(__VLS_166, new __VLS_166({
    label: "关联日历",
}));
const __VLS_168 = __VLS_167({
    label: "关联日历",
}, ...__VLS_functionalComponentArgsRest(__VLS_167));
__VLS_169.slots.default;
const __VLS_170 = {}.ElSelect;
/** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
// @ts-ignore
const __VLS_171 = __VLS_asFunctionalComponent(__VLS_170, new __VLS_170({
    modelValue: (__VLS_ctx.workshopForm.calendarId),
    clearable: true,
    placeholder: "为空时继承默认日历",
}));
const __VLS_172 = __VLS_171({
    modelValue: (__VLS_ctx.workshopForm.calendarId),
    clearable: true,
    placeholder: "为空时继承默认日历",
}, ...__VLS_functionalComponentArgsRest(__VLS_171));
__VLS_173.slots.default;
for (const [calendar] of __VLS_getVForSourceType((__VLS_ctx.factoryCalendars))) {
    const __VLS_174 = {}.ElOption;
    /** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
    // @ts-ignore
    const __VLS_175 = __VLS_asFunctionalComponent(__VLS_174, new __VLS_174({
        key: (calendar.id),
        label: (`${calendar.name} (${calendar.year})`),
        value: (calendar.id),
    }));
    const __VLS_176 = __VLS_175({
        key: (calendar.id),
        label: (`${calendar.name} (${calendar.year})`),
        value: (calendar.id),
    }, ...__VLS_functionalComponentArgsRest(__VLS_175));
}
var __VLS_173;
var __VLS_169;
const __VLS_178 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_179 = __VLS_asFunctionalComponent(__VLS_178, new __VLS_178({
    label: "排序",
}));
const __VLS_180 = __VLS_179({
    label: "排序",
}, ...__VLS_functionalComponentArgsRest(__VLS_179));
__VLS_181.slots.default;
const __VLS_182 = {}.ElInputNumber;
/** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
// @ts-ignore
const __VLS_183 = __VLS_asFunctionalComponent(__VLS_182, new __VLS_182({
    modelValue: (__VLS_ctx.workshopForm.sortOrder),
    min: (0),
}));
const __VLS_184 = __VLS_183({
    modelValue: (__VLS_ctx.workshopForm.sortOrder),
    min: (0),
}, ...__VLS_functionalComponentArgsRest(__VLS_183));
var __VLS_181;
if (__VLS_ctx.editingWorkshop) {
    const __VLS_186 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_187 = __VLS_asFunctionalComponent(__VLS_186, new __VLS_186({
        label: "启用状态",
    }));
    const __VLS_188 = __VLS_187({
        label: "启用状态",
    }, ...__VLS_functionalComponentArgsRest(__VLS_187));
    __VLS_189.slots.default;
    const __VLS_190 = {}.ElSwitch;
    /** @type {[typeof __VLS_components.ElSwitch, typeof __VLS_components.elSwitch, ]} */ ;
    // @ts-ignore
    const __VLS_191 = __VLS_asFunctionalComponent(__VLS_190, new __VLS_190({
        modelValue: (__VLS_ctx.workshopForm.enabled),
    }));
    const __VLS_192 = __VLS_191({
        modelValue: (__VLS_ctx.workshopForm.enabled),
    }, ...__VLS_functionalComponentArgsRest(__VLS_191));
    var __VLS_189;
}
const __VLS_194 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_195 = __VLS_asFunctionalComponent(__VLS_194, new __VLS_194({
    label: "备注",
}));
const __VLS_196 = __VLS_195({
    label: "备注",
}, ...__VLS_functionalComponentArgsRest(__VLS_195));
__VLS_197.slots.default;
const __VLS_198 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_199 = __VLS_asFunctionalComponent(__VLS_198, new __VLS_198({
    modelValue: (__VLS_ctx.workshopForm.description),
    type: "textarea",
    rows: (3),
}));
const __VLS_200 = __VLS_199({
    modelValue: (__VLS_ctx.workshopForm.description),
    type: "textarea",
    rows: (3),
}, ...__VLS_functionalComponentArgsRest(__VLS_199));
var __VLS_197;
var __VLS_139;
{
    const { footer: __VLS_thisSlot } = __VLS_135.slots;
    const __VLS_202 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_203 = __VLS_asFunctionalComponent(__VLS_202, new __VLS_202({
        ...{ 'onClick': {} },
    }));
    const __VLS_204 = __VLS_203({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_203));
    let __VLS_206;
    let __VLS_207;
    let __VLS_208;
    const __VLS_209 = {
        onClick: (...[$event]) => {
            __VLS_ctx.showWorkshopDialog = false;
        }
    };
    __VLS_205.slots.default;
    var __VLS_205;
    const __VLS_210 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_211 = __VLS_asFunctionalComponent(__VLS_210, new __VLS_210({
        ...{ 'onClick': {} },
        type: "primary",
    }));
    const __VLS_212 = __VLS_211({
        ...{ 'onClick': {} },
        type: "primary",
    }, ...__VLS_functionalComponentArgsRest(__VLS_211));
    let __VLS_214;
    let __VLS_215;
    let __VLS_216;
    const __VLS_217 = {
        onClick: (__VLS_ctx.saveWorkshop)
    };
    __VLS_213.slots.default;
    var __VLS_213;
}
var __VLS_135;
const __VLS_218 = {}.ElDialog;
/** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
// @ts-ignore
const __VLS_219 = __VLS_asFunctionalComponent(__VLS_218, new __VLS_218({
    modelValue: (__VLS_ctx.showMachineDialog),
    title: (__VLS_ctx.editingMachine ? '编辑注塑机' : '添加注塑机'),
    width: "620px",
}));
const __VLS_220 = __VLS_219({
    modelValue: (__VLS_ctx.showMachineDialog),
    title: (__VLS_ctx.editingMachine ? '编辑注塑机' : '添加注塑机'),
    width: "620px",
}, ...__VLS_functionalComponentArgsRest(__VLS_219));
__VLS_221.slots.default;
const __VLS_222 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_223 = __VLS_asFunctionalComponent(__VLS_222, new __VLS_222({
    ref: "machineFormRef",
    model: (__VLS_ctx.machineForm),
    rules: (__VLS_ctx.machineRules),
    labelWidth: "110px",
}));
const __VLS_224 = __VLS_223({
    ref: "machineFormRef",
    model: (__VLS_ctx.machineForm),
    rules: (__VLS_ctx.machineRules),
    labelWidth: "110px",
}, ...__VLS_functionalComponentArgsRest(__VLS_223));
/** @type {typeof __VLS_ctx.machineFormRef} */ ;
var __VLS_226 = {};
__VLS_225.slots.default;
const __VLS_228 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_229 = __VLS_asFunctionalComponent(__VLS_228, new __VLS_228({
    label: "资源编码",
    prop: "resourceCode",
}));
const __VLS_230 = __VLS_229({
    label: "资源编码",
    prop: "resourceCode",
}, ...__VLS_functionalComponentArgsRest(__VLS_229));
__VLS_231.slots.default;
const __VLS_232 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_233 = __VLS_asFunctionalComponent(__VLS_232, new __VLS_232({
    modelValue: (__VLS_ctx.machineForm.resourceCode),
    disabled: (!!__VLS_ctx.editingMachine),
}));
const __VLS_234 = __VLS_233({
    modelValue: (__VLS_ctx.machineForm.resourceCode),
    disabled: (!!__VLS_ctx.editingMachine),
}, ...__VLS_functionalComponentArgsRest(__VLS_233));
var __VLS_231;
const __VLS_236 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_237 = __VLS_asFunctionalComponent(__VLS_236, new __VLS_236({
    label: "资源名称",
    prop: "resourceName",
}));
const __VLS_238 = __VLS_237({
    label: "资源名称",
    prop: "resourceName",
}, ...__VLS_functionalComponentArgsRest(__VLS_237));
__VLS_239.slots.default;
const __VLS_240 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_241 = __VLS_asFunctionalComponent(__VLS_240, new __VLS_240({
    modelValue: (__VLS_ctx.machineForm.resourceName),
}));
const __VLS_242 = __VLS_241({
    modelValue: (__VLS_ctx.machineForm.resourceName),
}, ...__VLS_functionalComponentArgsRest(__VLS_241));
var __VLS_239;
const __VLS_244 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_245 = __VLS_asFunctionalComponent(__VLS_244, new __VLS_244({
    label: "资源类型",
}));
const __VLS_246 = __VLS_245({
    label: "资源类型",
}, ...__VLS_functionalComponentArgsRest(__VLS_245));
__VLS_247.slots.default;
const __VLS_248 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_249 = __VLS_asFunctionalComponent(__VLS_248, new __VLS_248({
    modelValue: (__VLS_ctx.machineForm.resourceType),
    placeholder: "如：INJECTION_MACHINE",
}));
const __VLS_250 = __VLS_249({
    modelValue: (__VLS_ctx.machineForm.resourceType),
    placeholder: "如：INJECTION_MACHINE",
}, ...__VLS_functionalComponentArgsRest(__VLS_249));
var __VLS_247;
const __VLS_252 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_253 = __VLS_asFunctionalComponent(__VLS_252, new __VLS_252({
    label: "吨位",
}));
const __VLS_254 = __VLS_253({
    label: "吨位",
}, ...__VLS_functionalComponentArgsRest(__VLS_253));
__VLS_255.slots.default;
const __VLS_256 = {}.ElInputNumber;
/** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
// @ts-ignore
const __VLS_257 = __VLS_asFunctionalComponent(__VLS_256, new __VLS_256({
    modelValue: (__VLS_ctx.machineForm.tonnage),
    min: (0),
}));
const __VLS_258 = __VLS_257({
    modelValue: (__VLS_ctx.machineForm.tonnage),
    min: (0),
}, ...__VLS_functionalComponentArgsRest(__VLS_257));
var __VLS_255;
const __VLS_260 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_261 = __VLS_asFunctionalComponent(__VLS_260, new __VLS_260({
    label: "机台品牌",
}));
const __VLS_262 = __VLS_261({
    label: "机台品牌",
}, ...__VLS_functionalComponentArgsRest(__VLS_261));
__VLS_263.slots.default;
const __VLS_264 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_265 = __VLS_asFunctionalComponent(__VLS_264, new __VLS_264({
    modelValue: (__VLS_ctx.machineForm.machineBrand),
}));
const __VLS_266 = __VLS_265({
    modelValue: (__VLS_ctx.machineForm.machineBrand),
}, ...__VLS_functionalComponentArgsRest(__VLS_265));
var __VLS_263;
const __VLS_268 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_269 = __VLS_asFunctionalComponent(__VLS_268, new __VLS_268({
    label: "机台型号",
}));
const __VLS_270 = __VLS_269({
    label: "机台型号",
}, ...__VLS_functionalComponentArgsRest(__VLS_269));
__VLS_271.slots.default;
const __VLS_272 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_273 = __VLS_asFunctionalComponent(__VLS_272, new __VLS_272({
    modelValue: (__VLS_ctx.machineForm.machineModel),
}));
const __VLS_274 = __VLS_273({
    modelValue: (__VLS_ctx.machineForm.machineModel),
}, ...__VLS_functionalComponentArgsRest(__VLS_273));
var __VLS_271;
const __VLS_276 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_277 = __VLS_asFunctionalComponent(__VLS_276, new __VLS_276({
    label: "最大射出量(g)",
}));
const __VLS_278 = __VLS_277({
    label: "最大射出量(g)",
}, ...__VLS_functionalComponentArgsRest(__VLS_277));
__VLS_279.slots.default;
const __VLS_280 = {}.ElInputNumber;
/** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
// @ts-ignore
const __VLS_281 = __VLS_asFunctionalComponent(__VLS_280, new __VLS_280({
    modelValue: (__VLS_ctx.machineForm.maxShotWeight),
    min: (0),
    precision: (2),
}));
const __VLS_282 = __VLS_281({
    modelValue: (__VLS_ctx.machineForm.maxShotWeight),
    min: (0),
    precision: (2),
}, ...__VLS_functionalComponentArgsRest(__VLS_281));
var __VLS_279;
const __VLS_284 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_285 = __VLS_asFunctionalComponent(__VLS_284, new __VLS_284({
    label: "机台状态",
}));
const __VLS_286 = __VLS_285({
    label: "机台状态",
}, ...__VLS_functionalComponentArgsRest(__VLS_285));
__VLS_287.slots.default;
const __VLS_288 = {}.ElSelect;
/** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
// @ts-ignore
const __VLS_289 = __VLS_asFunctionalComponent(__VLS_288, new __VLS_288({
    modelValue: (__VLS_ctx.machineForm.status),
    placeholder: "请选择状态",
}));
const __VLS_290 = __VLS_289({
    modelValue: (__VLS_ctx.machineForm.status),
    placeholder: "请选择状态",
}, ...__VLS_functionalComponentArgsRest(__VLS_289));
__VLS_291.slots.default;
for (const [item] of __VLS_getVForSourceType((__VLS_ctx.machineStatusItems))) {
    const __VLS_292 = {}.ElOption;
    /** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
    // @ts-ignore
    const __VLS_293 = __VLS_asFunctionalComponent(__VLS_292, new __VLS_292({
        key: (item.itemCode),
        label: (item.itemName),
        value: (item.itemCode),
    }));
    const __VLS_294 = __VLS_293({
        key: (item.itemCode),
        label: (item.itemName),
        value: (item.itemCode),
    }, ...__VLS_functionalComponentArgsRest(__VLS_293));
}
var __VLS_291;
var __VLS_287;
const __VLS_296 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_297 = __VLS_asFunctionalComponent(__VLS_296, new __VLS_296({
    label: "设备日历",
}));
const __VLS_298 = __VLS_297({
    label: "设备日历",
}, ...__VLS_functionalComponentArgsRest(__VLS_297));
__VLS_299.slots.default;
const __VLS_300 = {}.ElSelect;
/** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
// @ts-ignore
const __VLS_301 = __VLS_asFunctionalComponent(__VLS_300, new __VLS_300({
    modelValue: (__VLS_ctx.machineForm.calendarId),
    clearable: true,
    placeholder: "为空时继承车间/默认日历",
}));
const __VLS_302 = __VLS_301({
    modelValue: (__VLS_ctx.machineForm.calendarId),
    clearable: true,
    placeholder: "为空时继承车间/默认日历",
}, ...__VLS_functionalComponentArgsRest(__VLS_301));
__VLS_303.slots.default;
for (const [calendar] of __VLS_getVForSourceType((__VLS_ctx.factoryCalendars))) {
    const __VLS_304 = {}.ElOption;
    /** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
    // @ts-ignore
    const __VLS_305 = __VLS_asFunctionalComponent(__VLS_304, new __VLS_304({
        key: (calendar.id),
        label: (`${calendar.name} (${calendar.year})`),
        value: (calendar.id),
    }));
    const __VLS_306 = __VLS_305({
        key: (calendar.id),
        label: (`${calendar.name} (${calendar.year})`),
        value: (calendar.id),
    }, ...__VLS_functionalComponentArgsRest(__VLS_305));
}
var __VLS_303;
var __VLS_299;
if (__VLS_ctx.editingMachine) {
    const __VLS_308 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_309 = __VLS_asFunctionalComponent(__VLS_308, new __VLS_308({
        label: "可用",
    }));
    const __VLS_310 = __VLS_309({
        label: "可用",
    }, ...__VLS_functionalComponentArgsRest(__VLS_309));
    __VLS_311.slots.default;
    const __VLS_312 = {}.ElSwitch;
    /** @type {[typeof __VLS_components.ElSwitch, typeof __VLS_components.elSwitch, ]} */ ;
    // @ts-ignore
    const __VLS_313 = __VLS_asFunctionalComponent(__VLS_312, new __VLS_312({
        modelValue: (__VLS_ctx.machineForm.available),
    }));
    const __VLS_314 = __VLS_313({
        modelValue: (__VLS_ctx.machineForm.available),
    }, ...__VLS_functionalComponentArgsRest(__VLS_313));
    var __VLS_311;
}
var __VLS_225;
{
    const { footer: __VLS_thisSlot } = __VLS_221.slots;
    const __VLS_316 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_317 = __VLS_asFunctionalComponent(__VLS_316, new __VLS_316({
        ...{ 'onClick': {} },
    }));
    const __VLS_318 = __VLS_317({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_317));
    let __VLS_320;
    let __VLS_321;
    let __VLS_322;
    const __VLS_323 = {
        onClick: (...[$event]) => {
            __VLS_ctx.showMachineDialog = false;
        }
    };
    __VLS_319.slots.default;
    var __VLS_319;
    const __VLS_324 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_325 = __VLS_asFunctionalComponent(__VLS_324, new __VLS_324({
        ...{ 'onClick': {} },
        type: "primary",
    }));
    const __VLS_326 = __VLS_325({
        ...{ 'onClick': {} },
        type: "primary",
    }, ...__VLS_functionalComponentArgsRest(__VLS_325));
    let __VLS_328;
    let __VLS_329;
    let __VLS_330;
    const __VLS_331 = {
        onClick: (__VLS_ctx.saveMachine)
    };
    __VLS_327.slots.default;
    var __VLS_327;
}
var __VLS_221;
/** @type {__VLS_StyleScopedClasses['workshop-management-page']} */ ;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
/** @type {__VLS_StyleScopedClasses['content-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['left-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-title']} */ ;
/** @type {__VLS_StyleScopedClasses['workshop-list']} */ ;
/** @type {__VLS_StyleScopedClasses['workshop-card-header']} */ ;
/** @type {__VLS_StyleScopedClasses['workshop-name']} */ ;
/** @type {__VLS_StyleScopedClasses['workshop-meta']} */ ;
/** @type {__VLS_StyleScopedClasses['workshop-meta']} */ ;
/** @type {__VLS_StyleScopedClasses['workshop-meta']} */ ;
/** @type {__VLS_StyleScopedClasses['right-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-header']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-subtitle']} */ ;
/** @type {__VLS_StyleScopedClasses['separator']} */ ;
/** @type {__VLS_StyleScopedClasses['separator']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['machine-toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-title']} */ ;
// @ts-ignore
var __VLS_141 = __VLS_140, __VLS_227 = __VLS_226;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            Delete: Delete,
            Edit: Edit,
            Plus: Plus,
            loadingWorkshops: loadingWorkshops,
            loadingResources: loadingResources,
            workshops: workshops,
            resources: resources,
            factoryCalendars: factoryCalendars,
            selectedWorkshop: selectedWorkshop,
            effectiveCalendarName: effectiveCalendarName,
            machineStatusItems: machineStatusItems,
            showWorkshopDialog: showWorkshopDialog,
            editingWorkshop: editingWorkshop,
            workshopFormRef: workshopFormRef,
            workshopForm: workshopForm,
            workshopRules: workshopRules,
            showMachineDialog: showMachineDialog,
            editingMachine: editingMachine,
            machineFormRef: machineFormRef,
            machineForm: machineForm,
            machineRules: machineRules,
            selectWorkshop: selectWorkshop,
            openWorkshopDialog: openWorkshopDialog,
            saveWorkshop: saveWorkshop,
            deleteWorkshop: deleteWorkshop,
            openMachineDialog: openMachineDialog,
            saveMachine: saveMachine,
            deleteMachine: deleteMachine,
            getStatusLabel: getStatusLabel,
            getStatusTagType: getStatusTagType,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
