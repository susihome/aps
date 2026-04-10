/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { ref, computed, onMounted, nextTick } from 'vue';
import { msgSuccess, msgError, confirmDanger, extractErrorMsg } from '@/utils/message';
import { Plus, Search, UserFilled, Avatar, MoreFilled, Lock, Check, Select, Close, Folder, Document, Operation, Edit, Delete, CircleCheckFilled } from '@element-plus/icons-vue';
import { useRoleApi } from '@/api/role';
import { usePermissionApi } from '@/api/permission';
import { dictionaryApi } from '@/api/dictionary';
const roleApi = useRoleApi();
const permissionApi = usePermissionApi();
const loading = ref(false);
const roles = ref([]);
const selectedRole = ref(null);
const permissionTree = ref([]);
const searchKeyword = ref('');
const roleTypeItems = ref([]);
const roleTypeLabelMap = ref({});
const showDialog = ref(false);
const editingRole = ref(null);
const selectedPermissionIds = ref([]);
const permissionTreeRef = ref();
const formRef = ref();
const formData = ref({
    name: '',
    description: ''
});
const formRules = {
    name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
    description: [{ max: 255, message: '描述长度不能超过 255 个字符', trigger: 'blur' }]
};
const filteredRoles = computed(() => {
    if (!searchKeyword.value)
        return roles.value;
    return roles.value.filter((r) => r.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
        r.description?.toLowerCase().includes(searchKeyword.value.toLowerCase()));
});
onMounted(async () => {
    await Promise.all([loadRoles(), loadPermissionTree(), loadRoleTypeDict()]);
});
async function loadRoleTypeDict() {
    try {
        roleTypeItems.value = await dictionaryApi.getEnabledItemsByTypeCode('ROLE_TYPE');
        roleTypeLabelMap.value = Object.fromEntries(roleTypeItems.value.map(item => [item.itemCode, item.itemName]));
    }
    catch {
        roleTypeItems.value = [];
    }
}
async function loadRoles() {
    loading.value = true;
    try {
        const result = await roleApi.getRoles(0, 100);
        roles.value = result.content;
        if (roles.value.length > 0 && !selectedRole.value) {
            await selectRole(roles.value[0]);
        }
    }
    catch (error) {
        msgError(error.message || '加载角色失败');
    }
    finally {
        loading.value = false;
    }
}
async function loadPermissionTree() {
    try {
        permissionTree.value = await permissionApi.getPermissionTree();
    }
    catch (error) {
        msgError(error.message || '加载权限树失败');
    }
}
async function selectRole(role) {
    selectedRole.value = role;
    try {
        const permissionIds = await roleApi.getRolePermissions(role.id);
        selectedPermissionIds.value = permissionIds;
        await nextTick();
        if (permissionTreeRef.value) {
            permissionTreeRef.value.setCheckedKeys(permissionIds);
        }
    }
    catch (error) {
        msgError(error.message || '加载角色权限失败');
    }
}
function handleCreate() {
    editingRole.value = null;
    formData.value = { name: '', description: '' };
    showDialog.value = true;
}
function handleCommand(command, role) {
    if (command === 'edit') {
        editingRole.value = role;
        formData.value = {
            name: role.name,
            description: role.description || ''
        };
        showDialog.value = true;
    }
    else if (command === 'delete') {
        handleDelete(role);
    }
}
async function handleSave() {
    if (!formRef.value)
        return;
    formData.value.name = formData.value.name.trim();
    await formRef.value.validate(async (valid) => {
        if (!valid)
            return;
        try {
            if (editingRole.value) {
                await roleApi.updateRole(editingRole.value.id, formData.value);
                msgSuccess('角色更新成功');
            }
            else {
                await roleApi.createRole(formData.value);
                msgSuccess('角色创建成功');
            }
            showDialog.value = false;
            await loadRoles();
        }
        catch (error) {
            msgError(error.message || '保存失败');
        }
    });
}
async function handleDelete(role) {
    try {
        await confirmDanger(`确定删除角色 "${role.name}" 吗？`);
        await roleApi.deleteRole(role.id);
        msgSuccess('删除成功');
        if (selectedRole.value?.id === role.id) {
            selectedRole.value = null;
        }
        await loadRoles();
    }
    catch (error) {
        if (error !== 'cancel') {
            msgError(extractErrorMsg(error, '删除失败'));
        }
    }
}
function handlePermissionCheck() {
    if (permissionTreeRef.value) {
        selectedPermissionIds.value = permissionTreeRef.value.getCheckedKeys();
    }
}
async function handleSavePermissions() {
    if (!selectedRole.value)
        return;
    try {
        await roleApi.assignPermissions(selectedRole.value.id, selectedPermissionIds.value);
        msgSuccess('权限保存成功');
        await loadRoles();
    }
    catch (error) {
        msgError(error.message || '保存失败');
    }
}
function selectAllPermissions() {
    const allIds = getAllPermissionIds(permissionTree.value);
    selectedPermissionIds.value = allIds;
    if (permissionTreeRef.value) {
        permissionTreeRef.value.setCheckedKeys(allIds);
    }
}
function clearAllPermissions() {
    selectedPermissionIds.value = [];
    if (permissionTreeRef.value) {
        permissionTreeRef.value.setCheckedKeys([]);
    }
}
function getAllPermissionIds(nodes) {
    let ids = [];
    for (const node of nodes) {
        ids.push(node.id);
        if (node.children && node.children.length > 0) {
            ids = ids.concat(getAllPermissionIds(node.children));
        }
    }
    return ids;
}
function getCheckedCount(node) {
    let count = 0;
    if (selectedPermissionIds.value.includes(node.id))
        count++;
    if (node.children) {
        for (const child of node.children) {
            count += getCheckedCount(child);
        }
    }
    return count;
}
function getTotalCount(node) {
    let count = 1;
    if (node.children) {
        for (const child of node.children) {
            count += getTotalCount(child);
        }
    }
    return count;
}
// 获取角色图标
function getRoleIcon(roleName) {
    const iconMap = {
        'ADMIN': UserFilled,
        'PLANNER': Document,
        'SUPERVISOR': Lock
    };
    return iconMap[roleName] || Avatar;
}
// 获取角色图标样式类
function getRoleIconClass(roleName) {
    const classMap = {
        'ADMIN': 'icon-admin',
        'PLANNER': 'icon-planner',
        'SUPERVISOR': 'icon-supervisor'
    };
    return classMap[roleName] || '';
}
// 获取角色标签类型
function getRoleTagType(roleName) {
    const typeMap = {
        'ADMIN': 'danger',
        'PLANNER': 'warning',
        'SUPERVISOR': 'info'
    };
    return typeMap[roleName] || 'info';
}
// 获取角色显示名称
function getRoleDisplayName(roleName) {
    return roleTypeLabelMap.value[roleName] || roleName;
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['panel-header']} */ ;
/** @type {__VLS_StyleScopedClasses['role-list']} */ ;
/** @type {__VLS_StyleScopedClasses['role-list']} */ ;
/** @type {__VLS_StyleScopedClasses['role-list']} */ ;
/** @type {__VLS_StyleScopedClasses['role-list']} */ ;
/** @type {__VLS_StyleScopedClasses['role-card']} */ ;
/** @type {__VLS_StyleScopedClasses['role-card']} */ ;
/** @type {__VLS_StyleScopedClasses['role-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['role-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['role-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['more-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['permission-tree-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['permission-tree-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['permission-tree-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['permission-tree-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['el-tree-node__content']} */ ;
/** @type {__VLS_StyleScopedClasses['el-tree-node']} */ ;
/** @type {__VLS_StyleScopedClasses['el-tree-node__content']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "roles-page" },
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
const __VLS_4 = {}.UserFilled;
/** @type {[typeof __VLS_components.UserFilled, ]} */ ;
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
    placeholder: "搜索角色...",
    prefixIcon: (__VLS_ctx.Search),
    clearable: true,
    ...{ class: "search-input" },
}));
const __VLS_26 = __VLS_25({
    modelValue: (__VLS_ctx.searchKeyword),
    placeholder: "搜索角色...",
    prefixIcon: (__VLS_ctx.Search),
    clearable: true,
    ...{ class: "search-input" },
}, ...__VLS_functionalComponentArgsRest(__VLS_25));
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "role-list" },
});
__VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.loading) }, null, null);
for (const [role] of __VLS_getVForSourceType((__VLS_ctx.filteredRoles))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.selectRole(role);
            } },
        key: (role.id),
        ...{ class: (['role-card', { active: __VLS_ctx.selectedRole?.id === role.id }]) },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: (['role-icon', __VLS_ctx.getRoleIconClass(role.name)]) },
    });
    const __VLS_28 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({}));
    const __VLS_30 = __VLS_29({}, ...__VLS_functionalComponentArgsRest(__VLS_29));
    __VLS_31.slots.default;
    const __VLS_32 = ((__VLS_ctx.getRoleIcon(role.name)));
    // @ts-ignore
    const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({}));
    const __VLS_34 = __VLS_33({}, ...__VLS_functionalComponentArgsRest(__VLS_33));
    var __VLS_31;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "role-info" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "role-name" },
    });
    (__VLS_ctx.getRoleDisplayName(role.name));
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "role-tags" },
    });
    const __VLS_36 = {}.ElTag;
    /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
    // @ts-ignore
    const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({
        type: (__VLS_ctx.getRoleTagType(role.name)),
        size: "small",
    }));
    const __VLS_38 = __VLS_37({
        type: (__VLS_ctx.getRoleTagType(role.name)),
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_37));
    __VLS_39.slots.default;
    (role.name);
    var __VLS_39;
    const __VLS_40 = {}.ElTag;
    /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
    // @ts-ignore
    const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
        type: "success",
        size: "small",
    }));
    const __VLS_42 = __VLS_41({
        type: "success",
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_41));
    __VLS_43.slots.default;
    const __VLS_44 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_45 = __VLS_asFunctionalComponent(__VLS_44, new __VLS_44({}));
    const __VLS_46 = __VLS_45({}, ...__VLS_functionalComponentArgsRest(__VLS_45));
    __VLS_47.slots.default;
    const __VLS_48 = {}.CircleCheckFilled;
    /** @type {[typeof __VLS_components.CircleCheckFilled, ]} */ ;
    // @ts-ignore
    const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({}));
    const __VLS_50 = __VLS_49({}, ...__VLS_functionalComponentArgsRest(__VLS_49));
    var __VLS_47;
    var __VLS_43;
    const __VLS_52 = {}.ElDropdown;
    /** @type {[typeof __VLS_components.ElDropdown, typeof __VLS_components.elDropdown, typeof __VLS_components.ElDropdown, typeof __VLS_components.elDropdown, ]} */ ;
    // @ts-ignore
    const __VLS_53 = __VLS_asFunctionalComponent(__VLS_52, new __VLS_52({
        ...{ 'onCommand': {} },
        trigger: "click",
    }));
    const __VLS_54 = __VLS_53({
        ...{ 'onCommand': {} },
        trigger: "click",
    }, ...__VLS_functionalComponentArgsRest(__VLS_53));
    let __VLS_56;
    let __VLS_57;
    let __VLS_58;
    const __VLS_59 = {
        onCommand: ((cmd) => __VLS_ctx.handleCommand(cmd, role))
    };
    __VLS_55.slots.default;
    const __VLS_60 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_61 = __VLS_asFunctionalComponent(__VLS_60, new __VLS_60({
        ...{ class: "more-icon" },
    }));
    const __VLS_62 = __VLS_61({
        ...{ class: "more-icon" },
    }, ...__VLS_functionalComponentArgsRest(__VLS_61));
    __VLS_63.slots.default;
    const __VLS_64 = {}.MoreFilled;
    /** @type {[typeof __VLS_components.MoreFilled, ]} */ ;
    // @ts-ignore
    const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({}));
    const __VLS_66 = __VLS_65({}, ...__VLS_functionalComponentArgsRest(__VLS_65));
    var __VLS_63;
    {
        const { dropdown: __VLS_thisSlot } = __VLS_55.slots;
        const __VLS_68 = {}.ElDropdownMenu;
        /** @type {[typeof __VLS_components.ElDropdownMenu, typeof __VLS_components.elDropdownMenu, typeof __VLS_components.ElDropdownMenu, typeof __VLS_components.elDropdownMenu, ]} */ ;
        // @ts-ignore
        const __VLS_69 = __VLS_asFunctionalComponent(__VLS_68, new __VLS_68({}));
        const __VLS_70 = __VLS_69({}, ...__VLS_functionalComponentArgsRest(__VLS_69));
        __VLS_71.slots.default;
        const __VLS_72 = {}.ElDropdownItem;
        /** @type {[typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, ]} */ ;
        // @ts-ignore
        const __VLS_73 = __VLS_asFunctionalComponent(__VLS_72, new __VLS_72({
            command: "edit",
        }));
        const __VLS_74 = __VLS_73({
            command: "edit",
        }, ...__VLS_functionalComponentArgsRest(__VLS_73));
        __VLS_75.slots.default;
        const __VLS_76 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_77 = __VLS_asFunctionalComponent(__VLS_76, new __VLS_76({}));
        const __VLS_78 = __VLS_77({}, ...__VLS_functionalComponentArgsRest(__VLS_77));
        __VLS_79.slots.default;
        const __VLS_80 = {}.Edit;
        /** @type {[typeof __VLS_components.Edit, ]} */ ;
        // @ts-ignore
        const __VLS_81 = __VLS_asFunctionalComponent(__VLS_80, new __VLS_80({}));
        const __VLS_82 = __VLS_81({}, ...__VLS_functionalComponentArgsRest(__VLS_81));
        var __VLS_79;
        var __VLS_75;
        const __VLS_84 = {}.ElDropdownItem;
        /** @type {[typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, ]} */ ;
        // @ts-ignore
        const __VLS_85 = __VLS_asFunctionalComponent(__VLS_84, new __VLS_84({
            command: "delete",
            divided: true,
        }));
        const __VLS_86 = __VLS_85({
            command: "delete",
            divided: true,
        }, ...__VLS_functionalComponentArgsRest(__VLS_85));
        __VLS_87.slots.default;
        const __VLS_88 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_89 = __VLS_asFunctionalComponent(__VLS_88, new __VLS_88({}));
        const __VLS_90 = __VLS_89({}, ...__VLS_functionalComponentArgsRest(__VLS_89));
        __VLS_91.slots.default;
        const __VLS_92 = {}.Delete;
        /** @type {[typeof __VLS_components.Delete, ]} */ ;
        // @ts-ignore
        const __VLS_93 = __VLS_asFunctionalComponent(__VLS_92, new __VLS_92({}));
        const __VLS_94 = __VLS_93({}, ...__VLS_functionalComponentArgsRest(__VLS_93));
        var __VLS_91;
        var __VLS_87;
        var __VLS_71;
    }
    var __VLS_55;
}
if (__VLS_ctx.filteredRoles.length === 0) {
    const __VLS_96 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_97 = __VLS_asFunctionalComponent(__VLS_96, new __VLS_96({
        description: "暂无角色",
    }));
    const __VLS_98 = __VLS_97({
        description: "暂无角色",
    }, ...__VLS_functionalComponentArgsRest(__VLS_97));
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "right-panel" },
});
if (__VLS_ctx.selectedRole) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "permission-panel" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "panel-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "header-left" },
    });
    const __VLS_100 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_101 = __VLS_asFunctionalComponent(__VLS_100, new __VLS_100({}));
    const __VLS_102 = __VLS_101({}, ...__VLS_functionalComponentArgsRest(__VLS_101));
    __VLS_103.slots.default;
    const __VLS_104 = {}.Lock;
    /** @type {[typeof __VLS_components.Lock, ]} */ ;
    // @ts-ignore
    const __VLS_105 = __VLS_asFunctionalComponent(__VLS_104, new __VLS_104({}));
    const __VLS_106 = __VLS_105({}, ...__VLS_functionalComponentArgsRest(__VLS_105));
    var __VLS_103;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    (__VLS_ctx.selectedRole.name);
    const __VLS_108 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_109 = __VLS_asFunctionalComponent(__VLS_108, new __VLS_108({
        ...{ 'onClick': {} },
        type: "primary",
        size: "small",
    }));
    const __VLS_110 = __VLS_109({
        ...{ 'onClick': {} },
        type: "primary",
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_109));
    let __VLS_112;
    let __VLS_113;
    let __VLS_114;
    const __VLS_115 = {
        onClick: (__VLS_ctx.handleSavePermissions)
    };
    __VLS_111.slots.default;
    const __VLS_116 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_117 = __VLS_asFunctionalComponent(__VLS_116, new __VLS_116({}));
    const __VLS_118 = __VLS_117({}, ...__VLS_functionalComponentArgsRest(__VLS_117));
    __VLS_119.slots.default;
    const __VLS_120 = {}.Check;
    /** @type {[typeof __VLS_components.Check, ]} */ ;
    // @ts-ignore
    const __VLS_121 = __VLS_asFunctionalComponent(__VLS_120, new __VLS_120({}));
    const __VLS_122 = __VLS_121({}, ...__VLS_functionalComponentArgsRest(__VLS_121));
    var __VLS_119;
    var __VLS_111;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "permission-actions" },
    });
    const __VLS_124 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_125 = __VLS_asFunctionalComponent(__VLS_124, new __VLS_124({
        ...{ 'onClick': {} },
        text: true,
        size: "small",
    }));
    const __VLS_126 = __VLS_125({
        ...{ 'onClick': {} },
        text: true,
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_125));
    let __VLS_128;
    let __VLS_129;
    let __VLS_130;
    const __VLS_131 = {
        onClick: (__VLS_ctx.selectAllPermissions)
    };
    __VLS_127.slots.default;
    const __VLS_132 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_133 = __VLS_asFunctionalComponent(__VLS_132, new __VLS_132({}));
    const __VLS_134 = __VLS_133({}, ...__VLS_functionalComponentArgsRest(__VLS_133));
    __VLS_135.slots.default;
    const __VLS_136 = {}.Select;
    /** @type {[typeof __VLS_components.Select, ]} */ ;
    // @ts-ignore
    const __VLS_137 = __VLS_asFunctionalComponent(__VLS_136, new __VLS_136({}));
    const __VLS_138 = __VLS_137({}, ...__VLS_functionalComponentArgsRest(__VLS_137));
    var __VLS_135;
    var __VLS_127;
    const __VLS_140 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_141 = __VLS_asFunctionalComponent(__VLS_140, new __VLS_140({
        ...{ 'onClick': {} },
        text: true,
        size: "small",
    }));
    const __VLS_142 = __VLS_141({
        ...{ 'onClick': {} },
        text: true,
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_141));
    let __VLS_144;
    let __VLS_145;
    let __VLS_146;
    const __VLS_147 = {
        onClick: (__VLS_ctx.clearAllPermissions)
    };
    __VLS_143.slots.default;
    const __VLS_148 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_149 = __VLS_asFunctionalComponent(__VLS_148, new __VLS_148({}));
    const __VLS_150 = __VLS_149({}, ...__VLS_functionalComponentArgsRest(__VLS_149));
    __VLS_151.slots.default;
    const __VLS_152 = {}.Close;
    /** @type {[typeof __VLS_components.Close, ]} */ ;
    // @ts-ignore
    const __VLS_153 = __VLS_asFunctionalComponent(__VLS_152, new __VLS_152({}));
    const __VLS_154 = __VLS_153({}, ...__VLS_functionalComponentArgsRest(__VLS_153));
    var __VLS_151;
    var __VLS_143;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "permission-count" },
    });
    (__VLS_ctx.selectedPermissionIds.length);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "permission-tree-wrapper" },
    });
    const __VLS_156 = {}.ElTree;
    /** @type {[typeof __VLS_components.ElTree, typeof __VLS_components.elTree, typeof __VLS_components.ElTree, typeof __VLS_components.elTree, ]} */ ;
    // @ts-ignore
    const __VLS_157 = __VLS_asFunctionalComponent(__VLS_156, new __VLS_156({
        ...{ 'onCheck': {} },
        ref: "permissionTreeRef",
        data: (__VLS_ctx.permissionTree),
        props: ({ children: 'children', label: 'name' }),
        nodeKey: "id",
        defaultCheckedKeys: (__VLS_ctx.selectedPermissionIds),
        defaultExpandAll: (false),
        showCheckbox: true,
    }));
    const __VLS_158 = __VLS_157({
        ...{ 'onCheck': {} },
        ref: "permissionTreeRef",
        data: (__VLS_ctx.permissionTree),
        props: ({ children: 'children', label: 'name' }),
        nodeKey: "id",
        defaultCheckedKeys: (__VLS_ctx.selectedPermissionIds),
        defaultExpandAll: (false),
        showCheckbox: true,
    }, ...__VLS_functionalComponentArgsRest(__VLS_157));
    let __VLS_160;
    let __VLS_161;
    let __VLS_162;
    const __VLS_163 = {
        onCheck: (__VLS_ctx.handlePermissionCheck)
    };
    /** @type {typeof __VLS_ctx.permissionTreeRef} */ ;
    var __VLS_164 = {};
    __VLS_159.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_159.slots;
        const [{ node, data }] = __VLS_getSlotParams(__VLS_thisSlot);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "tree-node" },
        });
        if (data.type === 'CATALOG') {
            const __VLS_166 = {}.ElIcon;
            /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
            // @ts-ignore
            const __VLS_167 = __VLS_asFunctionalComponent(__VLS_166, new __VLS_166({
                ...{ class: "node-icon" },
            }));
            const __VLS_168 = __VLS_167({
                ...{ class: "node-icon" },
            }, ...__VLS_functionalComponentArgsRest(__VLS_167));
            __VLS_169.slots.default;
            const __VLS_170 = {}.Folder;
            /** @type {[typeof __VLS_components.Folder, ]} */ ;
            // @ts-ignore
            const __VLS_171 = __VLS_asFunctionalComponent(__VLS_170, new __VLS_170({}));
            const __VLS_172 = __VLS_171({}, ...__VLS_functionalComponentArgsRest(__VLS_171));
            var __VLS_169;
        }
        else if (data.type === 'MENU') {
            const __VLS_174 = {}.ElIcon;
            /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
            // @ts-ignore
            const __VLS_175 = __VLS_asFunctionalComponent(__VLS_174, new __VLS_174({
                ...{ class: "node-icon" },
            }));
            const __VLS_176 = __VLS_175({
                ...{ class: "node-icon" },
            }, ...__VLS_functionalComponentArgsRest(__VLS_175));
            __VLS_177.slots.default;
            const __VLS_178 = {}.Document;
            /** @type {[typeof __VLS_components.Document, ]} */ ;
            // @ts-ignore
            const __VLS_179 = __VLS_asFunctionalComponent(__VLS_178, new __VLS_178({}));
            const __VLS_180 = __VLS_179({}, ...__VLS_functionalComponentArgsRest(__VLS_179));
            var __VLS_177;
        }
        else {
            const __VLS_182 = {}.ElIcon;
            /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
            // @ts-ignore
            const __VLS_183 = __VLS_asFunctionalComponent(__VLS_182, new __VLS_182({
                ...{ class: "node-icon" },
            }));
            const __VLS_184 = __VLS_183({
                ...{ class: "node-icon" },
            }, ...__VLS_functionalComponentArgsRest(__VLS_183));
            __VLS_185.slots.default;
            const __VLS_186 = {}.Operation;
            /** @type {[typeof __VLS_components.Operation, ]} */ ;
            // @ts-ignore
            const __VLS_187 = __VLS_asFunctionalComponent(__VLS_186, new __VLS_186({}));
            const __VLS_188 = __VLS_187({}, ...__VLS_functionalComponentArgsRest(__VLS_187));
            var __VLS_185;
        }
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "node-label" },
        });
        (data.name);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "node-code" },
        });
        (data.code);
        if (data.children && data.children.length > 0) {
            __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
                ...{ class: "node-count" },
            });
            (__VLS_ctx.getCheckedCount(data));
            (__VLS_ctx.getTotalCount(data));
        }
    }
    var __VLS_159;
}
else {
    const __VLS_190 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_191 = __VLS_asFunctionalComponent(__VLS_190, new __VLS_190({
        description: "请选择左侧角色节点查看详情",
    }));
    const __VLS_192 = __VLS_191({
        description: "请选择左侧角色节点查看详情",
    }, ...__VLS_functionalComponentArgsRest(__VLS_191));
}
const __VLS_194 = {}.ElDialog;
/** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
// @ts-ignore
const __VLS_195 = __VLS_asFunctionalComponent(__VLS_194, new __VLS_194({
    modelValue: (__VLS_ctx.showDialog),
    title: (__VLS_ctx.editingRole ? '编辑角色' : '新增角色'),
    width: "500px",
}));
const __VLS_196 = __VLS_195({
    modelValue: (__VLS_ctx.showDialog),
    title: (__VLS_ctx.editingRole ? '编辑角色' : '新增角色'),
    width: "500px",
}, ...__VLS_functionalComponentArgsRest(__VLS_195));
__VLS_197.slots.default;
const __VLS_198 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_199 = __VLS_asFunctionalComponent(__VLS_198, new __VLS_198({
    ref: "formRef",
    model: (__VLS_ctx.formData),
    rules: (__VLS_ctx.formRules),
    labelWidth: "100px",
}));
const __VLS_200 = __VLS_199({
    ref: "formRef",
    model: (__VLS_ctx.formData),
    rules: (__VLS_ctx.formRules),
    labelWidth: "100px",
}, ...__VLS_functionalComponentArgsRest(__VLS_199));
/** @type {typeof __VLS_ctx.formRef} */ ;
var __VLS_202 = {};
__VLS_201.slots.default;
const __VLS_204 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_205 = __VLS_asFunctionalComponent(__VLS_204, new __VLS_204({
    label: "角色名称",
    prop: "name",
}));
const __VLS_206 = __VLS_205({
    label: "角色名称",
    prop: "name",
}, ...__VLS_functionalComponentArgsRest(__VLS_205));
__VLS_207.slots.default;
const __VLS_208 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_209 = __VLS_asFunctionalComponent(__VLS_208, new __VLS_208({
    modelValue: (__VLS_ctx.formData.name),
    disabled: (!!__VLS_ctx.editingRole),
    placeholder: "请输入角色名称",
    clearable: true,
}));
const __VLS_210 = __VLS_209({
    modelValue: (__VLS_ctx.formData.name),
    disabled: (!!__VLS_ctx.editingRole),
    placeholder: "请输入角色名称",
    clearable: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_209));
var __VLS_207;
const __VLS_212 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_213 = __VLS_asFunctionalComponent(__VLS_212, new __VLS_212({
    label: "角色描述",
    prop: "description",
}));
const __VLS_214 = __VLS_213({
    label: "角色描述",
    prop: "description",
}, ...__VLS_functionalComponentArgsRest(__VLS_213));
__VLS_215.slots.default;
const __VLS_216 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_217 = __VLS_asFunctionalComponent(__VLS_216, new __VLS_216({
    modelValue: (__VLS_ctx.formData.description),
    type: "textarea",
    rows: (3),
    placeholder: "请输入角色描述",
}));
const __VLS_218 = __VLS_217({
    modelValue: (__VLS_ctx.formData.description),
    type: "textarea",
    rows: (3),
    placeholder: "请输入角色描述",
}, ...__VLS_functionalComponentArgsRest(__VLS_217));
var __VLS_215;
var __VLS_201;
{
    const { footer: __VLS_thisSlot } = __VLS_197.slots;
    const __VLS_220 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_221 = __VLS_asFunctionalComponent(__VLS_220, new __VLS_220({
        ...{ 'onClick': {} },
    }));
    const __VLS_222 = __VLS_221({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_221));
    let __VLS_224;
    let __VLS_225;
    let __VLS_226;
    const __VLS_227 = {
        onClick: (...[$event]) => {
            __VLS_ctx.showDialog = false;
        }
    };
    __VLS_223.slots.default;
    var __VLS_223;
    const __VLS_228 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_229 = __VLS_asFunctionalComponent(__VLS_228, new __VLS_228({
        ...{ 'onClick': {} },
        type: "primary",
    }));
    const __VLS_230 = __VLS_229({
        ...{ 'onClick': {} },
        type: "primary",
    }, ...__VLS_functionalComponentArgsRest(__VLS_229));
    let __VLS_232;
    let __VLS_233;
    let __VLS_234;
    const __VLS_235 = {
        onClick: (__VLS_ctx.handleSave)
    };
    __VLS_231.slots.default;
    var __VLS_231;
}
var __VLS_197;
/** @type {__VLS_StyleScopedClasses['roles-page']} */ ;
/** @type {__VLS_StyleScopedClasses['content-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['left-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-header']} */ ;
/** @type {__VLS_StyleScopedClasses['search-input']} */ ;
/** @type {__VLS_StyleScopedClasses['role-list']} */ ;
/** @type {__VLS_StyleScopedClasses['role-info']} */ ;
/** @type {__VLS_StyleScopedClasses['role-name']} */ ;
/** @type {__VLS_StyleScopedClasses['role-tags']} */ ;
/** @type {__VLS_StyleScopedClasses['more-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['right-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['permission-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-header']} */ ;
/** @type {__VLS_StyleScopedClasses['header-left']} */ ;
/** @type {__VLS_StyleScopedClasses['permission-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['permission-count']} */ ;
/** @type {__VLS_StyleScopedClasses['permission-tree-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-node']} */ ;
/** @type {__VLS_StyleScopedClasses['node-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['node-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['node-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['node-label']} */ ;
/** @type {__VLS_StyleScopedClasses['node-code']} */ ;
/** @type {__VLS_StyleScopedClasses['node-count']} */ ;
// @ts-ignore
var __VLS_165 = __VLS_164, __VLS_203 = __VLS_202;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            Plus: Plus,
            Search: Search,
            UserFilled: UserFilled,
            MoreFilled: MoreFilled,
            Lock: Lock,
            Check: Check,
            Select: Select,
            Close: Close,
            Folder: Folder,
            Document: Document,
            Operation: Operation,
            Edit: Edit,
            Delete: Delete,
            CircleCheckFilled: CircleCheckFilled,
            loading: loading,
            selectedRole: selectedRole,
            permissionTree: permissionTree,
            searchKeyword: searchKeyword,
            showDialog: showDialog,
            editingRole: editingRole,
            selectedPermissionIds: selectedPermissionIds,
            permissionTreeRef: permissionTreeRef,
            formRef: formRef,
            formData: formData,
            formRules: formRules,
            filteredRoles: filteredRoles,
            selectRole: selectRole,
            handleCreate: handleCreate,
            handleCommand: handleCommand,
            handleSave: handleSave,
            handlePermissionCheck: handlePermissionCheck,
            handleSavePermissions: handleSavePermissions,
            selectAllPermissions: selectAllPermissions,
            clearAllPermissions: clearAllPermissions,
            getCheckedCount: getCheckedCount,
            getTotalCount: getTotalCount,
            getRoleIcon: getRoleIcon,
            getRoleIconClass: getRoleIconClass,
            getRoleTagType: getRoleTagType,
            getRoleDisplayName: getRoleDisplayName,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
