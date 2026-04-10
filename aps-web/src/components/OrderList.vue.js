/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { ref, onMounted, computed } from 'vue';
import { orderApi } from '../api';
import { useAuthStore } from '../stores/auth';
import { msgSuccess, msgError } from '@/utils/message';
const authStore = useAuthStore();
const canCreate = computed(() => {
    return authStore.hasRole('ADMIN') || authStore.hasRole('PLANNER');
});
const orders = ref([]);
const dialogVisible = ref(false);
const loading = ref(false);
const form = ref({
    orderNo: '',
    productCode: '',
    productName: '',
    quantity: 0,
    priority: 'NORMAL',
    dueDate: ''
});
onMounted(() => {
    loadOrders();
});
async function loadOrders() {
    try {
        orders.value = await orderApi.list();
    }
    catch (error) {
        const message = error instanceof Error ? error.message : '加载工单列表失败';
        msgError(message);
    }
}
async function submitForm() {
    loading.value = true;
    try {
        await orderApi.create({
            orderNo: form.value.orderNo,
            productCode: form.value.productCode,
            productName: form.value.productName,
            quantity: form.value.quantity,
            priority: form.value.priority,
            dueDate: form.value.dueDate
        });
        msgSuccess('工单创建成功');
        dialogVisible.value = false;
        loadOrders();
    }
    catch (error) {
        const message = error instanceof Error ? error.message : '创建工单失败';
        msgError(message);
    }
    finally {
        loading.value = false;
    }
}
function showCreateDialog() {
    form.value = {
        orderNo: '',
        productCode: '',
        productName: '',
        quantity: 0,
        priority: 'NORMAL',
        dueDate: ''
    };
    dialogVisible.value = true;
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['create-button']} */ ;
/** @type {__VLS_StyleScopedClasses['create-button']} */ ;
/** @type {__VLS_StyleScopedClasses['create-button']} */ ;
/** @type {__VLS_StyleScopedClasses['el-table']} */ ;
/** @type {__VLS_StyleScopedClasses['el-table']} */ ;
/** @type {__VLS_StyleScopedClasses['el-table']} */ ;
/** @type {__VLS_StyleScopedClasses['el-table']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input-number']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['el-table']} */ ;
/** @type {__VLS_StyleScopedClasses['create-button']} */ ;
/** @type {__VLS_StyleScopedClasses['order-list']} */ ;
/** @type {__VLS_StyleScopedClasses['create-button']} */ ;
/** @type {__VLS_StyleScopedClasses['create-button']} */ ;
/** @type {__VLS_StyleScopedClasses['create-button']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "order-list" },
});
if (__VLS_ctx.canCreate) {
    const __VLS_0 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
        ...{ 'onClick': {} },
        type: "primary",
        ...{ class: "create-button" },
        'aria-label': "新建工单",
    }));
    const __VLS_2 = __VLS_1({
        ...{ 'onClick': {} },
        type: "primary",
        ...{ class: "create-button" },
        'aria-label': "新建工单",
    }, ...__VLS_functionalComponentArgsRest(__VLS_1));
    let __VLS_4;
    let __VLS_5;
    let __VLS_6;
    const __VLS_7 = {
        onClick: (__VLS_ctx.showCreateDialog)
    };
    __VLS_3.slots.default;
    var __VLS_3;
}
const __VLS_8 = {}.ElTable;
/** @type {[typeof __VLS_components.ElTable, typeof __VLS_components.elTable, typeof __VLS_components.ElTable, typeof __VLS_components.elTable, ]} */ ;
// @ts-ignore
const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({
    data: (__VLS_ctx.orders),
    ...{ style: {} },
    'aria-label': "工单列表",
}));
const __VLS_10 = __VLS_9({
    data: (__VLS_ctx.orders),
    ...{ style: {} },
    'aria-label': "工单列表",
}, ...__VLS_functionalComponentArgsRest(__VLS_9));
__VLS_11.slots.default;
const __VLS_12 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_13 = __VLS_asFunctionalComponent(__VLS_12, new __VLS_12({
    prop: "orderNo",
    label: "工单号",
    minWidth: "120",
}));
const __VLS_14 = __VLS_13({
    prop: "orderNo",
    label: "工单号",
    minWidth: "120",
}, ...__VLS_functionalComponentArgsRest(__VLS_13));
const __VLS_16 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_17 = __VLS_asFunctionalComponent(__VLS_16, new __VLS_16({
    prop: "productName",
    label: "产品名称",
    minWidth: "150",
}));
const __VLS_18 = __VLS_17({
    prop: "productName",
    label: "产品名称",
    minWidth: "150",
}, ...__VLS_functionalComponentArgsRest(__VLS_17));
const __VLS_20 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({
    prop: "quantity",
    label: "数量",
    width: "100",
}));
const __VLS_22 = __VLS_21({
    prop: "quantity",
    label: "数量",
    width: "100",
}, ...__VLS_functionalComponentArgsRest(__VLS_21));
const __VLS_24 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_25 = __VLS_asFunctionalComponent(__VLS_24, new __VLS_24({
    prop: "priority",
    label: "优先级",
    width: "100",
}));
const __VLS_26 = __VLS_25({
    prop: "priority",
    label: "优先级",
    width: "100",
}, ...__VLS_functionalComponentArgsRest(__VLS_25));
const __VLS_28 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
    prop: "dueDate",
    label: "交期",
    minWidth: "160",
}));
const __VLS_30 = __VLS_29({
    prop: "dueDate",
    label: "交期",
    minWidth: "160",
}, ...__VLS_functionalComponentArgsRest(__VLS_29));
var __VLS_11;
const __VLS_32 = {}.ElDialog;
/** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
// @ts-ignore
const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({
    modelValue: (__VLS_ctx.dialogVisible),
    title: "新建工单",
    width: "90%",
    ...{ style: ({ maxWidth: '600px' }) },
    closeOnClickModal: (false),
    'aria-labelledby': "dialog-title",
}));
const __VLS_34 = __VLS_33({
    modelValue: (__VLS_ctx.dialogVisible),
    title: "新建工单",
    width: "90%",
    ...{ style: ({ maxWidth: '600px' }) },
    closeOnClickModal: (false),
    'aria-labelledby': "dialog-title",
}, ...__VLS_functionalComponentArgsRest(__VLS_33));
__VLS_35.slots.default;
const __VLS_36 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({
    model: (__VLS_ctx.form),
    labelWidth: "100px",
}));
const __VLS_38 = __VLS_37({
    model: (__VLS_ctx.form),
    labelWidth: "100px",
}, ...__VLS_functionalComponentArgsRest(__VLS_37));
__VLS_39.slots.default;
const __VLS_40 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
    label: "工单号",
}));
const __VLS_42 = __VLS_41({
    label: "工单号",
}, ...__VLS_functionalComponentArgsRest(__VLS_41));
__VLS_43.slots.default;
const __VLS_44 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_45 = __VLS_asFunctionalComponent(__VLS_44, new __VLS_44({
    modelValue: (__VLS_ctx.form.orderNo),
    'aria-label': "工单号",
}));
const __VLS_46 = __VLS_45({
    modelValue: (__VLS_ctx.form.orderNo),
    'aria-label': "工单号",
}, ...__VLS_functionalComponentArgsRest(__VLS_45));
var __VLS_43;
const __VLS_48 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({
    label: "产品编码",
}));
const __VLS_50 = __VLS_49({
    label: "产品编码",
}, ...__VLS_functionalComponentArgsRest(__VLS_49));
__VLS_51.slots.default;
const __VLS_52 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_53 = __VLS_asFunctionalComponent(__VLS_52, new __VLS_52({
    modelValue: (__VLS_ctx.form.productCode),
    'aria-label': "产品编码",
}));
const __VLS_54 = __VLS_53({
    modelValue: (__VLS_ctx.form.productCode),
    'aria-label': "产品编码",
}, ...__VLS_functionalComponentArgsRest(__VLS_53));
var __VLS_51;
const __VLS_56 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({
    label: "产品名称",
}));
const __VLS_58 = __VLS_57({
    label: "产品名称",
}, ...__VLS_functionalComponentArgsRest(__VLS_57));
__VLS_59.slots.default;
const __VLS_60 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_61 = __VLS_asFunctionalComponent(__VLS_60, new __VLS_60({
    modelValue: (__VLS_ctx.form.productName),
    'aria-label': "产品名称",
}));
const __VLS_62 = __VLS_61({
    modelValue: (__VLS_ctx.form.productName),
    'aria-label': "产品名称",
}, ...__VLS_functionalComponentArgsRest(__VLS_61));
var __VLS_59;
const __VLS_64 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({
    label: "数量",
}));
const __VLS_66 = __VLS_65({
    label: "数量",
}, ...__VLS_functionalComponentArgsRest(__VLS_65));
__VLS_67.slots.default;
const __VLS_68 = {}.ElInputNumber;
/** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
// @ts-ignore
const __VLS_69 = __VLS_asFunctionalComponent(__VLS_68, new __VLS_68({
    modelValue: (__VLS_ctx.form.quantity),
    min: (1),
    'aria-label': "数量",
}));
const __VLS_70 = __VLS_69({
    modelValue: (__VLS_ctx.form.quantity),
    min: (1),
    'aria-label': "数量",
}, ...__VLS_functionalComponentArgsRest(__VLS_69));
var __VLS_67;
const __VLS_72 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_73 = __VLS_asFunctionalComponent(__VLS_72, new __VLS_72({
    label: "优先级",
}));
const __VLS_74 = __VLS_73({
    label: "优先级",
}, ...__VLS_functionalComponentArgsRest(__VLS_73));
__VLS_75.slots.default;
const __VLS_76 = {}.ElSelect;
/** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
// @ts-ignore
const __VLS_77 = __VLS_asFunctionalComponent(__VLS_76, new __VLS_76({
    modelValue: (__VLS_ctx.form.priority),
    'aria-label': "优先级",
}));
const __VLS_78 = __VLS_77({
    modelValue: (__VLS_ctx.form.priority),
    'aria-label': "优先级",
}, ...__VLS_functionalComponentArgsRest(__VLS_77));
__VLS_79.slots.default;
const __VLS_80 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_81 = __VLS_asFunctionalComponent(__VLS_80, new __VLS_80({
    label: "紧急",
    value: "URGENT",
}));
const __VLS_82 = __VLS_81({
    label: "紧急",
    value: "URGENT",
}, ...__VLS_functionalComponentArgsRest(__VLS_81));
const __VLS_84 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_85 = __VLS_asFunctionalComponent(__VLS_84, new __VLS_84({
    label: "高",
    value: "HIGH",
}));
const __VLS_86 = __VLS_85({
    label: "高",
    value: "HIGH",
}, ...__VLS_functionalComponentArgsRest(__VLS_85));
const __VLS_88 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_89 = __VLS_asFunctionalComponent(__VLS_88, new __VLS_88({
    label: "普通",
    value: "NORMAL",
}));
const __VLS_90 = __VLS_89({
    label: "普通",
    value: "NORMAL",
}, ...__VLS_functionalComponentArgsRest(__VLS_89));
const __VLS_92 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_93 = __VLS_asFunctionalComponent(__VLS_92, new __VLS_92({
    label: "低",
    value: "LOW",
}));
const __VLS_94 = __VLS_93({
    label: "低",
    value: "LOW",
}, ...__VLS_functionalComponentArgsRest(__VLS_93));
var __VLS_79;
var __VLS_75;
const __VLS_96 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_97 = __VLS_asFunctionalComponent(__VLS_96, new __VLS_96({
    label: "交期",
}));
const __VLS_98 = __VLS_97({
    label: "交期",
}, ...__VLS_functionalComponentArgsRest(__VLS_97));
__VLS_99.slots.default;
const __VLS_100 = {}.ElDatePicker;
/** @type {[typeof __VLS_components.ElDatePicker, typeof __VLS_components.elDatePicker, ]} */ ;
// @ts-ignore
const __VLS_101 = __VLS_asFunctionalComponent(__VLS_100, new __VLS_100({
    modelValue: (__VLS_ctx.form.dueDate),
    type: "datetime",
    'aria-label': "交期",
}));
const __VLS_102 = __VLS_101({
    modelValue: (__VLS_ctx.form.dueDate),
    type: "datetime",
    'aria-label': "交期",
}, ...__VLS_functionalComponentArgsRest(__VLS_101));
var __VLS_99;
var __VLS_39;
{
    const { footer: __VLS_thisSlot } = __VLS_35.slots;
    const __VLS_104 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_105 = __VLS_asFunctionalComponent(__VLS_104, new __VLS_104({
        ...{ 'onClick': {} },
        'aria-label': "取消",
    }));
    const __VLS_106 = __VLS_105({
        ...{ 'onClick': {} },
        'aria-label': "取消",
    }, ...__VLS_functionalComponentArgsRest(__VLS_105));
    let __VLS_108;
    let __VLS_109;
    let __VLS_110;
    const __VLS_111 = {
        onClick: (...[$event]) => {
            __VLS_ctx.dialogVisible = false;
        }
    };
    __VLS_107.slots.default;
    var __VLS_107;
    const __VLS_112 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_113 = __VLS_asFunctionalComponent(__VLS_112, new __VLS_112({
        ...{ 'onClick': {} },
        type: "primary",
        loading: (__VLS_ctx.loading),
        'aria-label': "提交工单",
    }));
    const __VLS_114 = __VLS_113({
        ...{ 'onClick': {} },
        type: "primary",
        loading: (__VLS_ctx.loading),
        'aria-label': "提交工单",
    }, ...__VLS_functionalComponentArgsRest(__VLS_113));
    let __VLS_116;
    let __VLS_117;
    let __VLS_118;
    const __VLS_119 = {
        onClick: (__VLS_ctx.submitForm)
    };
    __VLS_115.slots.default;
    var __VLS_115;
}
var __VLS_35;
/** @type {__VLS_StyleScopedClasses['order-list']} */ ;
/** @type {__VLS_StyleScopedClasses['create-button']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            canCreate: canCreate,
            orders: orders,
            dialogVisible: dialogVisible,
            loading: loading,
            form: form,
            submitForm: submitForm,
            showCreateDialog: showCreateDialog,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
