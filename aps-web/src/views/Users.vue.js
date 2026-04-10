/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { computed, ref, onMounted } from 'vue';
import { msgSuccess, msgError, msgInfo } from '@/utils/message';
import { Plus, Edit, Delete, Key } from '@element-plus/icons-vue';
import { userApi } from '../api';
import { useAuthStore } from '../stores/auth';
const authStore = useAuthStore();
const users = ref([]);
const dialogVisible = ref(false);
const loading = ref(false);
const formRef = ref();
const canCreateUser = computed(() => authStore.hasPermission('system:user:add'));
const canEditUser = computed(() => authStore.hasPermission('system:user:edit'));
const canDeleteUser = computed(() => authStore.hasPermission('system:user:delete'));
const canResetPassword = computed(() => authStore.hasPermission('system:user:reset_password'));
const form = ref({
    username: '',
    password: '',
    email: ''
});
const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
    ],
    email: [
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
};
onMounted(() => {
    loadUsers();
});
async function loadUsers() {
    try {
        users.value = await userApi.list();
    }
    catch (error) {
        msgError('加载用户列表失败');
    }
}
function showCreateDialog() {
    form.value = {
        username: '',
        password: '',
        email: ''
    };
    dialogVisible.value = true;
}
function handleEdit(_user) {
    msgInfo('编辑用户功能待接入后端接口');
}
function handleDelete(_user) {
    msgInfo('删除用户功能待接入后端接口');
}
function handleResetPassword(_user) {
    msgInfo('重置密码功能待接入后端接口');
}
async function handleCreate() {
    if (!formRef.value)
        return;
    await formRef.value.validate(async (valid) => {
        if (!valid)
            return;
        loading.value = true;
        try {
            await userApi.create({
                ...form.value,
                roleIds: []
            });
            msgSuccess('用户创建成功');
            dialogVisible.value = false;
            loadUsers();
        }
        catch (error) {
            const message = error instanceof Error ? error.message : '创建用户失败';
            msgError(message);
        }
        finally {
            loading.value = false;
        }
    });
}
function getRoleLabel(role) {
    const roleMap = {
        'ADMIN': '管理员',
        'PLANNER': '计划员',
        'SUPERVISOR': '主管'
    };
    return roleMap[role.name] || role.name;
}
function formatDate(dateStr) {
    return new Date(dateStr).toLocaleString('zh-CN');
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['users-page']} */ ;
/** @type {__VLS_StyleScopedClasses['users-page']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
/** @type {__VLS_StyleScopedClasses['el-table']} */ ;
/** @type {__VLS_StyleScopedClasses['el-table']} */ ;
/** @type {__VLS_StyleScopedClasses['users-page']} */ ;
/** @type {__VLS_StyleScopedClasses['el-table']} */ ;
/** @type {__VLS_StyleScopedClasses['users-page']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "users-page" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
const __VLS_0 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    shadow: "hover",
}));
const __VLS_2 = __VLS_1({
    shadow: "hover",
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_3.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "toolbar" },
});
if (__VLS_ctx.canCreateUser) {
    const __VLS_4 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_5 = __VLS_asFunctionalComponent(__VLS_4, new __VLS_4({
        ...{ 'onClick': {} },
        type: "primary",
        'aria-label': "新建用户",
    }));
    const __VLS_6 = __VLS_5({
        ...{ 'onClick': {} },
        type: "primary",
        'aria-label': "新建用户",
    }, ...__VLS_functionalComponentArgsRest(__VLS_5));
    let __VLS_8;
    let __VLS_9;
    let __VLS_10;
    const __VLS_11 = {
        onClick: (__VLS_ctx.showCreateDialog)
    };
    __VLS_7.slots.default;
    const __VLS_12 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_13 = __VLS_asFunctionalComponent(__VLS_12, new __VLS_12({}));
    const __VLS_14 = __VLS_13({}, ...__VLS_functionalComponentArgsRest(__VLS_13));
    __VLS_15.slots.default;
    const __VLS_16 = {}.Plus;
    /** @type {[typeof __VLS_components.Plus, ]} */ ;
    // @ts-ignore
    const __VLS_17 = __VLS_asFunctionalComponent(__VLS_16, new __VLS_16({}));
    const __VLS_18 = __VLS_17({}, ...__VLS_functionalComponentArgsRest(__VLS_17));
    var __VLS_15;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    var __VLS_7;
}
const __VLS_20 = {}.ElTable;
/** @type {[typeof __VLS_components.ElTable, typeof __VLS_components.elTable, typeof __VLS_components.ElTable, typeof __VLS_components.elTable, ]} */ ;
// @ts-ignore
const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({
    data: (__VLS_ctx.users),
    ...{ style: {} },
    'aria-label': "用户列表",
}));
const __VLS_22 = __VLS_21({
    data: (__VLS_ctx.users),
    ...{ style: {} },
    'aria-label': "用户列表",
}, ...__VLS_functionalComponentArgsRest(__VLS_21));
__VLS_23.slots.default;
const __VLS_24 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_25 = __VLS_asFunctionalComponent(__VLS_24, new __VLS_24({
    prop: "username",
    label: "用户名",
    minWidth: "120",
}));
const __VLS_26 = __VLS_25({
    prop: "username",
    label: "用户名",
    minWidth: "120",
}, ...__VLS_functionalComponentArgsRest(__VLS_25));
const __VLS_28 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
    prop: "email",
    label: "邮箱",
    minWidth: "180",
}));
const __VLS_30 = __VLS_29({
    prop: "email",
    label: "邮箱",
    minWidth: "180",
}, ...__VLS_functionalComponentArgsRest(__VLS_29));
const __VLS_32 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({
    label: "角色",
    minWidth: "150",
}));
const __VLS_34 = __VLS_33({
    label: "角色",
    minWidth: "150",
}, ...__VLS_functionalComponentArgsRest(__VLS_33));
__VLS_35.slots.default;
{
    const { default: __VLS_thisSlot } = __VLS_35.slots;
    const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
    for (const [role] of __VLS_getVForSourceType((row.roles))) {
        const __VLS_36 = {}.ElTag;
        /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
        // @ts-ignore
        const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({
            key: (role),
            size: "small",
            ...{ style: {} },
        }));
        const __VLS_38 = __VLS_37({
            key: (role),
            size: "small",
            ...{ style: {} },
        }, ...__VLS_functionalComponentArgsRest(__VLS_37));
        __VLS_39.slots.default;
        (__VLS_ctx.getRoleLabel(role));
        var __VLS_39;
    }
}
var __VLS_35;
const __VLS_40 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
    label: "状态",
    width: "100",
}));
const __VLS_42 = __VLS_41({
    label: "状态",
    width: "100",
}, ...__VLS_functionalComponentArgsRest(__VLS_41));
__VLS_43.slots.default;
{
    const { default: __VLS_thisSlot } = __VLS_43.slots;
    const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
    const __VLS_44 = {}.ElTag;
    /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
    // @ts-ignore
    const __VLS_45 = __VLS_asFunctionalComponent(__VLS_44, new __VLS_44({
        type: (row.enabled ? 'success' : 'danger'),
        size: "small",
    }));
    const __VLS_46 = __VLS_45({
        type: (row.enabled ? 'success' : 'danger'),
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_45));
    __VLS_47.slots.default;
    (row.enabled ? '启用' : '禁用');
    var __VLS_47;
}
var __VLS_43;
const __VLS_48 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({
    label: "创建时间",
    minWidth: "160",
}));
const __VLS_50 = __VLS_49({
    label: "创建时间",
    minWidth: "160",
}, ...__VLS_functionalComponentArgsRest(__VLS_49));
__VLS_51.slots.default;
{
    const { default: __VLS_thisSlot } = __VLS_51.slots;
    const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
    (__VLS_ctx.formatDate(row.createTime));
}
var __VLS_51;
const __VLS_52 = {}.ElTableColumn;
/** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
// @ts-ignore
const __VLS_53 = __VLS_asFunctionalComponent(__VLS_52, new __VLS_52({
    label: "操作",
    minWidth: "240",
    fixed: "right",
}));
const __VLS_54 = __VLS_53({
    label: "操作",
    minWidth: "240",
    fixed: "right",
}, ...__VLS_functionalComponentArgsRest(__VLS_53));
__VLS_55.slots.default;
{
    const { default: __VLS_thisSlot } = __VLS_55.slots;
    const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "row-actions" },
    });
    if (__VLS_ctx.canEditUser) {
        const __VLS_56 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({
            ...{ 'onClick': {} },
            text: true,
            type: "primary",
            size: "small",
        }));
        const __VLS_58 = __VLS_57({
            ...{ 'onClick': {} },
            text: true,
            type: "primary",
            size: "small",
        }, ...__VLS_functionalComponentArgsRest(__VLS_57));
        let __VLS_60;
        let __VLS_61;
        let __VLS_62;
        const __VLS_63 = {
            onClick: (...[$event]) => {
                if (!(__VLS_ctx.canEditUser))
                    return;
                __VLS_ctx.handleEdit(row);
            }
        };
        __VLS_59.slots.default;
        const __VLS_64 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({}));
        const __VLS_66 = __VLS_65({}, ...__VLS_functionalComponentArgsRest(__VLS_65));
        __VLS_67.slots.default;
        const __VLS_68 = {}.Edit;
        /** @type {[typeof __VLS_components.Edit, ]} */ ;
        // @ts-ignore
        const __VLS_69 = __VLS_asFunctionalComponent(__VLS_68, new __VLS_68({}));
        const __VLS_70 = __VLS_69({}, ...__VLS_functionalComponentArgsRest(__VLS_69));
        var __VLS_67;
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        var __VLS_59;
    }
    if (__VLS_ctx.canDeleteUser) {
        const __VLS_72 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_73 = __VLS_asFunctionalComponent(__VLS_72, new __VLS_72({
            ...{ 'onClick': {} },
            text: true,
            type: "danger",
            size: "small",
        }));
        const __VLS_74 = __VLS_73({
            ...{ 'onClick': {} },
            text: true,
            type: "danger",
            size: "small",
        }, ...__VLS_functionalComponentArgsRest(__VLS_73));
        let __VLS_76;
        let __VLS_77;
        let __VLS_78;
        const __VLS_79 = {
            onClick: (...[$event]) => {
                if (!(__VLS_ctx.canDeleteUser))
                    return;
                __VLS_ctx.handleDelete(row);
            }
        };
        __VLS_75.slots.default;
        const __VLS_80 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_81 = __VLS_asFunctionalComponent(__VLS_80, new __VLS_80({}));
        const __VLS_82 = __VLS_81({}, ...__VLS_functionalComponentArgsRest(__VLS_81));
        __VLS_83.slots.default;
        const __VLS_84 = {}.Delete;
        /** @type {[typeof __VLS_components.Delete, ]} */ ;
        // @ts-ignore
        const __VLS_85 = __VLS_asFunctionalComponent(__VLS_84, new __VLS_84({}));
        const __VLS_86 = __VLS_85({}, ...__VLS_functionalComponentArgsRest(__VLS_85));
        var __VLS_83;
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        var __VLS_75;
    }
    if (__VLS_ctx.canResetPassword) {
        const __VLS_88 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_89 = __VLS_asFunctionalComponent(__VLS_88, new __VLS_88({
            ...{ 'onClick': {} },
            text: true,
            type: "warning",
            size: "small",
        }));
        const __VLS_90 = __VLS_89({
            ...{ 'onClick': {} },
            text: true,
            type: "warning",
            size: "small",
        }, ...__VLS_functionalComponentArgsRest(__VLS_89));
        let __VLS_92;
        let __VLS_93;
        let __VLS_94;
        const __VLS_95 = {
            onClick: (...[$event]) => {
                if (!(__VLS_ctx.canResetPassword))
                    return;
                __VLS_ctx.handleResetPassword(row);
            }
        };
        __VLS_91.slots.default;
        const __VLS_96 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_97 = __VLS_asFunctionalComponent(__VLS_96, new __VLS_96({}));
        const __VLS_98 = __VLS_97({}, ...__VLS_functionalComponentArgsRest(__VLS_97));
        __VLS_99.slots.default;
        const __VLS_100 = {}.Key;
        /** @type {[typeof __VLS_components.Key, ]} */ ;
        // @ts-ignore
        const __VLS_101 = __VLS_asFunctionalComponent(__VLS_100, new __VLS_100({}));
        const __VLS_102 = __VLS_101({}, ...__VLS_functionalComponentArgsRest(__VLS_101));
        var __VLS_99;
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        var __VLS_91;
    }
}
var __VLS_55;
var __VLS_23;
var __VLS_3;
const __VLS_104 = {}.ElDialog;
/** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
// @ts-ignore
const __VLS_105 = __VLS_asFunctionalComponent(__VLS_104, new __VLS_104({
    modelValue: (__VLS_ctx.dialogVisible),
    title: "新建用户",
    width: "90%",
    ...{ style: ({ maxWidth: '500px' }) },
    closeOnClickModal: (false),
    'aria-labelledby': "dialog-title",
}));
const __VLS_106 = __VLS_105({
    modelValue: (__VLS_ctx.dialogVisible),
    title: "新建用户",
    width: "90%",
    ...{ style: ({ maxWidth: '500px' }) },
    closeOnClickModal: (false),
    'aria-labelledby': "dialog-title",
}, ...__VLS_functionalComponentArgsRest(__VLS_105));
__VLS_107.slots.default;
const __VLS_108 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_109 = __VLS_asFunctionalComponent(__VLS_108, new __VLS_108({
    model: (__VLS_ctx.form),
    rules: (__VLS_ctx.rules),
    ref: "formRef",
    labelWidth: "80px",
}));
const __VLS_110 = __VLS_109({
    model: (__VLS_ctx.form),
    rules: (__VLS_ctx.rules),
    ref: "formRef",
    labelWidth: "80px",
}, ...__VLS_functionalComponentArgsRest(__VLS_109));
/** @type {typeof __VLS_ctx.formRef} */ ;
var __VLS_112 = {};
__VLS_111.slots.default;
const __VLS_114 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_115 = __VLS_asFunctionalComponent(__VLS_114, new __VLS_114({
    label: "用户名",
    prop: "username",
}));
const __VLS_116 = __VLS_115({
    label: "用户名",
    prop: "username",
}, ...__VLS_functionalComponentArgsRest(__VLS_115));
__VLS_117.slots.default;
const __VLS_118 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_119 = __VLS_asFunctionalComponent(__VLS_118, new __VLS_118({
    modelValue: (__VLS_ctx.form.username),
    placeholder: "请输入用户名",
    autocomplete: "off",
    'aria-label': "用户名",
}));
const __VLS_120 = __VLS_119({
    modelValue: (__VLS_ctx.form.username),
    placeholder: "请输入用户名",
    autocomplete: "off",
    'aria-label': "用户名",
}, ...__VLS_functionalComponentArgsRest(__VLS_119));
var __VLS_117;
const __VLS_122 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_123 = __VLS_asFunctionalComponent(__VLS_122, new __VLS_122({
    label: "密码",
    prop: "password",
}));
const __VLS_124 = __VLS_123({
    label: "密码",
    prop: "password",
}, ...__VLS_functionalComponentArgsRest(__VLS_123));
__VLS_125.slots.default;
const __VLS_126 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_127 = __VLS_asFunctionalComponent(__VLS_126, new __VLS_126({
    modelValue: (__VLS_ctx.form.password),
    type: "password",
    placeholder: "请输入密码",
    showPassword: true,
    autocomplete: "new-password",
    'aria-label': "密码",
}));
const __VLS_128 = __VLS_127({
    modelValue: (__VLS_ctx.form.password),
    type: "password",
    placeholder: "请输入密码",
    showPassword: true,
    autocomplete: "new-password",
    'aria-label': "密码",
}, ...__VLS_functionalComponentArgsRest(__VLS_127));
var __VLS_125;
const __VLS_130 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_131 = __VLS_asFunctionalComponent(__VLS_130, new __VLS_130({
    label: "邮箱",
    prop: "email",
}));
const __VLS_132 = __VLS_131({
    label: "邮箱",
    prop: "email",
}, ...__VLS_functionalComponentArgsRest(__VLS_131));
__VLS_133.slots.default;
const __VLS_134 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_135 = __VLS_asFunctionalComponent(__VLS_134, new __VLS_134({
    modelValue: (__VLS_ctx.form.email),
    placeholder: "请输入邮箱",
    autocomplete: "email",
    'aria-label': "邮箱",
}));
const __VLS_136 = __VLS_135({
    modelValue: (__VLS_ctx.form.email),
    placeholder: "请输入邮箱",
    autocomplete: "email",
    'aria-label': "邮箱",
}, ...__VLS_functionalComponentArgsRest(__VLS_135));
var __VLS_133;
var __VLS_111;
{
    const { footer: __VLS_thisSlot } = __VLS_107.slots;
    const __VLS_138 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_139 = __VLS_asFunctionalComponent(__VLS_138, new __VLS_138({
        ...{ 'onClick': {} },
        'aria-label': "取消",
    }));
    const __VLS_140 = __VLS_139({
        ...{ 'onClick': {} },
        'aria-label': "取消",
    }, ...__VLS_functionalComponentArgsRest(__VLS_139));
    let __VLS_142;
    let __VLS_143;
    let __VLS_144;
    const __VLS_145 = {
        onClick: (...[$event]) => {
            __VLS_ctx.dialogVisible = false;
        }
    };
    __VLS_141.slots.default;
    var __VLS_141;
    const __VLS_146 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_147 = __VLS_asFunctionalComponent(__VLS_146, new __VLS_146({
        ...{ 'onClick': {} },
        type: "primary",
        loading: (__VLS_ctx.loading),
        'aria-label': "创建用户",
    }));
    const __VLS_148 = __VLS_147({
        ...{ 'onClick': {} },
        type: "primary",
        loading: (__VLS_ctx.loading),
        'aria-label': "创建用户",
    }, ...__VLS_functionalComponentArgsRest(__VLS_147));
    let __VLS_150;
    let __VLS_151;
    let __VLS_152;
    const __VLS_153 = {
        onClick: (__VLS_ctx.handleCreate)
    };
    __VLS_149.slots.default;
    var __VLS_149;
}
var __VLS_107;
/** @type {__VLS_StyleScopedClasses['users-page']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['row-actions']} */ ;
// @ts-ignore
var __VLS_113 = __VLS_112;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            Plus: Plus,
            Edit: Edit,
            Delete: Delete,
            Key: Key,
            users: users,
            dialogVisible: dialogVisible,
            loading: loading,
            formRef: formRef,
            canCreateUser: canCreateUser,
            canEditUser: canEditUser,
            canDeleteUser: canDeleteUser,
            canResetPassword: canResetPassword,
            form: form,
            rules: rules,
            showCreateDialog: showCreateDialog,
            handleEdit: handleEdit,
            handleDelete: handleDelete,
            handleResetPassword: handleResetPassword,
            handleCreate: handleCreate,
            getRoleLabel: getRoleLabel,
            formatDate: formatDate,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
