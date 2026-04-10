/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { computed, ref, reactive, onMounted } from 'vue';
import { msgSuccess, msgError, confirmDanger } from '@/utils/message';
import { Plus, Search, Menu, Setting, User, Key, Edit, Delete, InfoFilled, Opportunity, Lightning, Lock } from '@element-plus/icons-vue';
import { usePermissionApi } from '../api/permission';
import { dictionaryApi } from '@/api/dictionary';
const permissionApi = usePermissionApi();
const treeRef = ref();
const formRef = ref();
const searchKeyword = ref('');
const expandedKeys = ref([]);
const loading = ref(false);
const toggleLoading = ref(false);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const iconMap = {
    Plus,
    Menu,
    Setting,
    User,
    Key,
    Edit,
    Delete,
    Lock
};
const permissionTypeItems = ref([]);
const typeLabelMap = ref({
    catalog: '目录',
    menu: '菜单',
    button: '按钮'
});
const tagTypeMap = {
    catalog: 'info',
    menu: 'primary',
    button: 'success'
};
const permissionTree = ref([]);
const selectedPermissionId = ref('');
const formData = reactive({
    code: '',
    name: '',
    description: '',
    type: 'menu',
    routePath: '',
    icon: 'Menu',
    sort: 0,
    enabled: true,
    visible: true,
    parentId: undefined
});
const formRules = {
    code: [
        { required: true, message: '请输入权限编码', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9:_-]+$/, message: '权限编码只能包含字母、数字、冒号、下划线和连字符', trigger: 'blur' }
    ],
    name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
    type: [{ required: true, message: '请选择权限类型', trigger: 'change' }],
    icon: [{ required: true, message: '请选择图标', trigger: 'change' }],
    sort: [{ required: true, message: '请输入排序号', trigger: 'blur' }]
};
const treeProps = {
    label: 'name',
    children: 'children'
};
function getTypeLabel(type) {
    return typeLabelMap.value[type] ?? type;
}
function getTagType(type) {
    return tagTypeMap[type];
}
function flattenPermissions(nodes) {
    return nodes.flatMap(node => [node, ...(node.children ? flattenPermissions(node.children) : [])]);
}
const flatPermissions = computed(() => flattenPermissions(permissionTree.value));
const selectedPermission = computed(() => {
    return flatPermissions.value.find(item => item.id === selectedPermissionId.value) || null;
});
function filterTree(nodes, keyword) {
    if (!keyword.trim())
        return nodes;
    const normalizedKeyword = keyword.trim().toLowerCase();
    const result = [];
    nodes.forEach(node => {
        const filteredChildren = node.children ? filterTree(node.children, normalizedKeyword) : undefined;
        const matched = node.name.toLowerCase().includes(normalizedKeyword) ||
            node.code.toLowerCase().includes(normalizedKeyword);
        if (matched || (filteredChildren && filteredChildren.length > 0)) {
            result.push({
                ...node,
                children: filteredChildren
            });
        }
    });
    return result;
}
const filteredPermissionTree = computed(() => filterTree(permissionTree.value, searchKeyword.value));
async function loadPermissions() {
    loading.value = true;
    try {
        const data = await permissionApi.getPermissionTree();
        permissionTree.value = data;
        expandedKeys.value = collectExpandableKeys(data);
    }
    catch (error) {
        // 显示详细的错误信息
        const message = error.response?.data?.message || error.message || '加载权限失败';
        msgError(message);
        console.error('权限加载错误:', error);
    }
    finally {
        loading.value = false;
    }
}
function collectExpandableKeys(nodes) {
    return nodes.flatMap(node => {
        const own = node.children?.length ? [node.id] : [];
        return [...own, ...(node.children ? collectExpandableKeys(node.children) : [])];
    });
}
function handleNodeClick(data) {
    selectedPermissionId.value = data.id;
}
function selectPermissionById(id) {
    selectedPermissionId.value = id;
}
function expandAll() {
    expandedKeys.value = collectExpandableKeys(permissionTree.value);
}
function collapseAll() {
    expandedKeys.value = [];
}
function resetTree() {
    searchKeyword.value = '';
    expandedKeys.value = collectExpandableKeys(permissionTree.value);
}
function resetForm() {
    formRef.value?.clearValidate();
    formData.code = '';
    formData.name = '';
    formData.description = '';
    formData.type = 'menu';
    formData.routePath = '';
    formData.icon = 'Menu';
    formData.sort = 0;
    formData.enabled = true;
    formData.visible = true;
    formData.parentId = undefined;
    isEditMode.value = false;
}
function openCreateDialog(parentId) {
    resetForm();
    if (parentId) {
        formData.parentId = parentId;
    }
    dialogVisible.value = true;
}
function openEditDialog(permission) {
    resetForm();
    isEditMode.value = true;
    formData.code = permission.code;
    formData.name = permission.name;
    formData.description = permission.description || '';
    formData.type = permission.type;
    formData.routePath = permission.routePath || '';
    formData.icon = permission.icon;
    formData.sort = permission.sort;
    formData.enabled = permission.enabled;
    formData.visible = permission.visible;
    formData.parentId = permission.parentId;
    dialogVisible.value = true;
}
async function handleSubmit() {
    if (!formRef.value)
        return;
    await formRef.value.validate(async (valid) => {
        if (!valid)
            return;
        submitLoading.value = true;
        try {
            if (isEditMode.value && selectedPermission.value) {
                await permissionApi.updatePermission(selectedPermission.value.id, formData);
                msgSuccess('权限更新成功');
            }
            else {
                await permissionApi.createPermission(formData);
                msgSuccess('权限创建成功');
            }
            dialogVisible.value = false;
            await loadPermissions();
        }
        catch (error) {
            msgError(isEditMode.value ? '权限更新失败' : '权限创建失败');
        }
        finally {
            submitLoading.value = false;
        }
    });
}
async function handleTogglePermission(enabled) {
    if (!selectedPermission.value)
        return;
    toggleLoading.value = true;
    try {
        await permissionApi.togglePermission(selectedPermission.value.id, enabled);
        msgSuccess(enabled ? '权限已启用' : '权限已停用');
        await loadPermissions();
    }
    catch (error) {
        msgError('状态更新失败');
    }
    finally {
        toggleLoading.value = false;
    }
}
async function handleDeletePermission(id) {
    try {
        await confirmDanger('确定删除该权限吗？删除后无法恢复。');
        await permissionApi.deletePermission(id);
        msgSuccess('权限删除成功');
        if (selectedPermissionId.value === id) {
            selectedPermissionId.value = '';
        }
        await loadPermissions();
    }
    catch (error) {
        // 确认框取消时抛出 'cancel' 字符串
        if (error !== 'cancel') {
            msgError('权限删除失败');
        }
    }
}
async function handleNodeDrop(draggingNode, dropNode, dropType, ev) {
    try {
        const updates = collectSortUpdates(permissionTree.value);
        if (updates.length > 0) {
            await permissionApi.updateSort(updates);
            msgSuccess('排序已更新');
        }
    }
    catch (error) {
        msgError('排序更新失败');
        await loadPermissions();
    }
}
function collectSortUpdates(nodes, sort = 0) {
    const updates = [];
    nodes.forEach((node, index) => {
        const newSort = sort * 1000 + index;
        if (node.sort !== newSort) {
            updates.push({ id: node.id, sort: newSort });
        }
        if (node.children?.length) {
            updates.push(...collectSortUpdates(node.children, newSort + 1));
        }
    });
    return updates;
}
async function loadPermissionTypeDict() {
    try {
        permissionTypeItems.value = await dictionaryApi.getEnabledItemsByTypeCode('PERMISSION_TYPE');
        if (permissionTypeItems.value.length > 0) {
            typeLabelMap.value = Object.fromEntries(permissionTypeItems.value.map(item => [item.itemCode.toLowerCase(), item.itemName]));
        }
    }
    catch {
        permissionTypeItems.value = [];
    }
}
onMounted(() => {
    loadPermissions();
    loadPermissionTypeDict();
});
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['tree-node-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-node-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-node-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-section']} */ ;
/** @type {__VLS_StyleScopedClasses['child-permission-card']} */ ;
/** @type {__VLS_StyleScopedClasses['el-tree-node__content']} */ ;
/** @type {__VLS_StyleScopedClasses['el-tree-node__content']} */ ;
/** @type {__VLS_StyleScopedClasses['permissions-layout']} */ ;
/** @type {__VLS_StyleScopedClasses['permission-tree']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "permissions-page" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "permissions-layout" },
});
const __VLS_0 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    ...{ class: "tree-panel" },
    shadow: "never",
}));
const __VLS_2 = __VLS_1({
    ...{ class: "tree-panel" },
    shadow: "never",
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_3.slots.default;
{
    const { header: __VLS_thisSlot } = __VLS_3.slots;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "panel-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "panel-title-wrap" },
    });
    const __VLS_4 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_5 = __VLS_asFunctionalComponent(__VLS_4, new __VLS_4({
        ...{ class: "panel-title-icon" },
    }));
    const __VLS_6 = __VLS_5({
        ...{ class: "panel-title-icon" },
    }, ...__VLS_functionalComponentArgsRest(__VLS_5));
    __VLS_7.slots.default;
    const __VLS_8 = {}.Lock;
    /** @type {[typeof __VLS_components.Lock, ]} */ ;
    // @ts-ignore
    const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({}));
    const __VLS_10 = __VLS_9({}, ...__VLS_functionalComponentArgsRest(__VLS_9));
    var __VLS_7;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "panel-title" },
    });
    const __VLS_12 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_13 = __VLS_asFunctionalComponent(__VLS_12, new __VLS_12({
        ...{ 'onClick': {} },
        type: "primary",
        size: "small",
    }));
    const __VLS_14 = __VLS_13({
        ...{ 'onClick': {} },
        type: "primary",
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_13));
    let __VLS_16;
    let __VLS_17;
    let __VLS_18;
    const __VLS_19 = {
        onClick: (...[$event]) => {
            __VLS_ctx.openCreateDialog();
        }
    };
    __VLS_15.slots.default;
    const __VLS_20 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({}));
    const __VLS_22 = __VLS_21({}, ...__VLS_functionalComponentArgsRest(__VLS_21));
    __VLS_23.slots.default;
    const __VLS_24 = {}.Plus;
    /** @type {[typeof __VLS_components.Plus, ]} */ ;
    // @ts-ignore
    const __VLS_25 = __VLS_asFunctionalComponent(__VLS_24, new __VLS_24({}));
    const __VLS_26 = __VLS_25({}, ...__VLS_functionalComponentArgsRest(__VLS_25));
    var __VLS_23;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    var __VLS_15;
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "tree-toolbar" },
});
const __VLS_28 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
    modelValue: (__VLS_ctx.searchKeyword),
    placeholder: "搜索权限...",
    prefixIcon: (__VLS_ctx.Search),
    clearable: true,
    ...{ class: "tree-search" },
}));
const __VLS_30 = __VLS_29({
    modelValue: (__VLS_ctx.searchKeyword),
    placeholder: "搜索权限...",
    prefixIcon: (__VLS_ctx.Search),
    clearable: true,
    ...{ class: "tree-search" },
}, ...__VLS_functionalComponentArgsRest(__VLS_29));
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "tree-actions" },
});
const __VLS_32 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({
    ...{ 'onClick': {} },
    text: true,
    size: "small",
}));
const __VLS_34 = __VLS_33({
    ...{ 'onClick': {} },
    text: true,
    size: "small",
}, ...__VLS_functionalComponentArgsRest(__VLS_33));
let __VLS_36;
let __VLS_37;
let __VLS_38;
const __VLS_39 = {
    onClick: (__VLS_ctx.expandAll)
};
__VLS_35.slots.default;
var __VLS_35;
const __VLS_40 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
    ...{ 'onClick': {} },
    text: true,
    size: "small",
}));
const __VLS_42 = __VLS_41({
    ...{ 'onClick': {} },
    text: true,
    size: "small",
}, ...__VLS_functionalComponentArgsRest(__VLS_41));
let __VLS_44;
let __VLS_45;
let __VLS_46;
const __VLS_47 = {
    onClick: (__VLS_ctx.collapseAll)
};
__VLS_43.slots.default;
var __VLS_43;
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
    onClick: (__VLS_ctx.resetTree)
};
__VLS_51.slots.default;
var __VLS_51;
const __VLS_56 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({
    ...{ 'onClick': {} },
    text: true,
    size: "small",
    loading: (__VLS_ctx.loading),
}));
const __VLS_58 = __VLS_57({
    ...{ 'onClick': {} },
    text: true,
    size: "small",
    loading: (__VLS_ctx.loading),
}, ...__VLS_functionalComponentArgsRest(__VLS_57));
let __VLS_60;
let __VLS_61;
let __VLS_62;
const __VLS_63 = {
    onClick: (__VLS_ctx.loadPermissions)
};
__VLS_59.slots.default;
var __VLS_59;
const __VLS_64 = {}.ElTree;
/** @type {[typeof __VLS_components.ElTree, typeof __VLS_components.elTree, typeof __VLS_components.ElTree, typeof __VLS_components.elTree, ]} */ ;
// @ts-ignore
const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({
    ...{ 'onNodeClick': {} },
    ...{ 'onNodeDrop': {} },
    ref: "treeRef",
    ...{ class: "permission-tree" },
    nodeKey: "id",
    data: (__VLS_ctx.filteredPermissionTree),
    props: (__VLS_ctx.treeProps),
    expandOnClickNode: (false),
    highlightCurrent: (true),
    defaultExpandedKeys: (__VLS_ctx.expandedKeys),
    draggable: true,
}));
const __VLS_66 = __VLS_65({
    ...{ 'onNodeClick': {} },
    ...{ 'onNodeDrop': {} },
    ref: "treeRef",
    ...{ class: "permission-tree" },
    nodeKey: "id",
    data: (__VLS_ctx.filteredPermissionTree),
    props: (__VLS_ctx.treeProps),
    expandOnClickNode: (false),
    highlightCurrent: (true),
    defaultExpandedKeys: (__VLS_ctx.expandedKeys),
    draggable: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_65));
let __VLS_68;
let __VLS_69;
let __VLS_70;
const __VLS_71 = {
    onNodeClick: (__VLS_ctx.handleNodeClick)
};
const __VLS_72 = {
    onNodeDrop: (__VLS_ctx.handleNodeDrop)
};
/** @type {typeof __VLS_ctx.treeRef} */ ;
var __VLS_73 = {};
__VLS_67.slots.default;
{
    const { default: __VLS_thisSlot } = __VLS_67.slots;
    const [{ data }] = __VLS_getSlotParams(__VLS_thisSlot);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "tree-node" },
        ...{ class: ({ active: __VLS_ctx.selectedPermission?.id === data.id }) },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "tree-node-main" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "tree-node-icon" },
        ...{ class: (data.type) },
    });
    const __VLS_75 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_76 = __VLS_asFunctionalComponent(__VLS_75, new __VLS_75({}));
    const __VLS_77 = __VLS_76({}, ...__VLS_functionalComponentArgsRest(__VLS_76));
    __VLS_78.slots.default;
    const __VLS_79 = ((__VLS_ctx.iconMap[data.icon] || __VLS_ctx.Menu));
    // @ts-ignore
    const __VLS_80 = __VLS_asFunctionalComponent(__VLS_79, new __VLS_79({}));
    const __VLS_81 = __VLS_80({}, ...__VLS_functionalComponentArgsRest(__VLS_80));
    var __VLS_78;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "tree-node-text" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "tree-node-title" },
    });
    (data.name);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "tree-node-code" },
    });
    (data.code);
    const __VLS_83 = {}.ElTag;
    /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
    // @ts-ignore
    const __VLS_84 = __VLS_asFunctionalComponent(__VLS_83, new __VLS_83({
        size: "small",
        round: true,
        type: (__VLS_ctx.getTagType(data.type)),
    }));
    const __VLS_85 = __VLS_84({
        size: "small",
        round: true,
        type: (__VLS_ctx.getTagType(data.type)),
    }, ...__VLS_functionalComponentArgsRest(__VLS_84));
    __VLS_86.slots.default;
    (__VLS_ctx.getTypeLabel(data.type));
    var __VLS_86;
}
var __VLS_67;
var __VLS_3;
const __VLS_87 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_88 = __VLS_asFunctionalComponent(__VLS_87, new __VLS_87({
    ...{ class: "detail-panel" },
    shadow: "never",
}));
const __VLS_89 = __VLS_88({
    ...{ class: "detail-panel" },
    shadow: "never",
}, ...__VLS_functionalComponentArgsRest(__VLS_88));
__VLS_90.slots.default;
{
    const { header: __VLS_thisSlot } = __VLS_90.slots;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-title-wrap" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-icon" },
    });
    const __VLS_91 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_92 = __VLS_asFunctionalComponent(__VLS_91, new __VLS_91({}));
    const __VLS_93 = __VLS_92({}, ...__VLS_functionalComponentArgsRest(__VLS_92));
    __VLS_94.slots.default;
    const __VLS_95 = ((__VLS_ctx.iconMap[__VLS_ctx.selectedPermission?.icon || 'Menu'] || __VLS_ctx.Menu));
    // @ts-ignore
    const __VLS_96 = __VLS_asFunctionalComponent(__VLS_95, new __VLS_95({}));
    const __VLS_97 = __VLS_96({}, ...__VLS_functionalComponentArgsRest(__VLS_96));
    var __VLS_94;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-title" },
    });
    (__VLS_ctx.selectedPermission?.name || '请选择权限');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-subtitle" },
    });
    (__VLS_ctx.selectedPermission?.code || '点击左侧权限节点查看详情');
    if (__VLS_ctx.selectedPermission) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "detail-actions" },
        });
        const __VLS_99 = {}.ElSwitch;
        /** @type {[typeof __VLS_components.ElSwitch, typeof __VLS_components.elSwitch, ]} */ ;
        // @ts-ignore
        const __VLS_100 = __VLS_asFunctionalComponent(__VLS_99, new __VLS_99({
            ...{ 'onChange': {} },
            modelValue: (__VLS_ctx.selectedPermission.enabled),
            inlinePrompt: true,
            activeText: "启用",
            inactiveText: "停用",
            loading: (__VLS_ctx.toggleLoading),
        }));
        const __VLS_101 = __VLS_100({
            ...{ 'onChange': {} },
            modelValue: (__VLS_ctx.selectedPermission.enabled),
            inlinePrompt: true,
            activeText: "启用",
            inactiveText: "停用",
            loading: (__VLS_ctx.toggleLoading),
        }, ...__VLS_functionalComponentArgsRest(__VLS_100));
        let __VLS_103;
        let __VLS_104;
        let __VLS_105;
        const __VLS_106 = {
            onChange: (__VLS_ctx.handleTogglePermission)
        };
        var __VLS_102;
        const __VLS_107 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_108 = __VLS_asFunctionalComponent(__VLS_107, new __VLS_107({
            ...{ 'onClick': {} },
            type: "primary",
            plain: true,
        }));
        const __VLS_109 = __VLS_108({
            ...{ 'onClick': {} },
            type: "primary",
            plain: true,
        }, ...__VLS_functionalComponentArgsRest(__VLS_108));
        let __VLS_111;
        let __VLS_112;
        let __VLS_113;
        const __VLS_114 = {
            onClick: (...[$event]) => {
                if (!(__VLS_ctx.selectedPermission))
                    return;
                __VLS_ctx.openEditDialog(__VLS_ctx.selectedPermission);
            }
        };
        __VLS_110.slots.default;
        const __VLS_115 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_116 = __VLS_asFunctionalComponent(__VLS_115, new __VLS_115({}));
        const __VLS_117 = __VLS_116({}, ...__VLS_functionalComponentArgsRest(__VLS_116));
        __VLS_118.slots.default;
        const __VLS_119 = {}.Edit;
        /** @type {[typeof __VLS_components.Edit, ]} */ ;
        // @ts-ignore
        const __VLS_120 = __VLS_asFunctionalComponent(__VLS_119, new __VLS_119({}));
        const __VLS_121 = __VLS_120({}, ...__VLS_functionalComponentArgsRest(__VLS_120));
        var __VLS_118;
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        var __VLS_110;
        const __VLS_123 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_124 = __VLS_asFunctionalComponent(__VLS_123, new __VLS_123({
            ...{ 'onClick': {} },
            type: "danger",
            plain: true,
        }));
        const __VLS_125 = __VLS_124({
            ...{ 'onClick': {} },
            type: "danger",
            plain: true,
        }, ...__VLS_functionalComponentArgsRest(__VLS_124));
        let __VLS_127;
        let __VLS_128;
        let __VLS_129;
        const __VLS_130 = {
            onClick: (...[$event]) => {
                if (!(__VLS_ctx.selectedPermission))
                    return;
                __VLS_ctx.handleDeletePermission(__VLS_ctx.selectedPermission.id);
            }
        };
        __VLS_126.slots.default;
        const __VLS_131 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_132 = __VLS_asFunctionalComponent(__VLS_131, new __VLS_131({}));
        const __VLS_133 = __VLS_132({}, ...__VLS_functionalComponentArgsRest(__VLS_132));
        __VLS_134.slots.default;
        const __VLS_135 = {}.Delete;
        /** @type {[typeof __VLS_components.Delete, ]} */ ;
        // @ts-ignore
        const __VLS_136 = __VLS_asFunctionalComponent(__VLS_135, new __VLS_135({}));
        const __VLS_137 = __VLS_136({}, ...__VLS_functionalComponentArgsRest(__VLS_136));
        var __VLS_134;
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        var __VLS_126;
    }
}
if (__VLS_ctx.selectedPermission) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
        ...{ class: "detail-section" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "section-title" },
    });
    const __VLS_139 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_140 = __VLS_asFunctionalComponent(__VLS_139, new __VLS_139({}));
    const __VLS_141 = __VLS_140({}, ...__VLS_functionalComponentArgsRest(__VLS_140));
    __VLS_142.slots.default;
    const __VLS_143 = {}.InfoFilled;
    /** @type {[typeof __VLS_components.InfoFilled, ]} */ ;
    // @ts-ignore
    const __VLS_144 = __VLS_asFunctionalComponent(__VLS_143, new __VLS_143({}));
    const __VLS_145 = __VLS_144({}, ...__VLS_functionalComponentArgsRest(__VLS_144));
    var __VLS_142;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "info-grid" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "info-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-value" },
    });
    (__VLS_ctx.selectedPermission.code);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "info-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-value" },
    });
    (__VLS_ctx.selectedPermission.name);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "info-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-value" },
    });
    (__VLS_ctx.typeLabelMap[__VLS_ctx.selectedPermission.type]);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "info-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-value" },
    });
    (__VLS_ctx.selectedPermission.routePath || '无');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "info-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-value" },
    });
    (__VLS_ctx.selectedPermission.icon);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "info-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-value" },
    });
    (__VLS_ctx.selectedPermission.sort);
    if (__VLS_ctx.selectedPermission.description) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "description-box" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "info-label" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
            ...{ class: "description-text" },
        });
        (__VLS_ctx.selectedPermission.description);
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
        ...{ class: "detail-section" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "section-title" },
    });
    const __VLS_147 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_148 = __VLS_asFunctionalComponent(__VLS_147, new __VLS_147({}));
    const __VLS_149 = __VLS_148({}, ...__VLS_functionalComponentArgsRest(__VLS_148));
    __VLS_150.slots.default;
    const __VLS_151 = {}.Opportunity;
    /** @type {[typeof __VLS_components.Opportunity, ]} */ ;
    // @ts-ignore
    const __VLS_152 = __VLS_asFunctionalComponent(__VLS_151, new __VLS_151({}));
    const __VLS_153 = __VLS_152({}, ...__VLS_functionalComponentArgsRest(__VLS_152));
    var __VLS_150;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "status-grid" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "status-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-label" },
    });
    const __VLS_155 = {}.ElTag;
    /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
    // @ts-ignore
    const __VLS_156 = __VLS_asFunctionalComponent(__VLS_155, new __VLS_155({
        type: (__VLS_ctx.selectedPermission.enabled ? 'success' : 'danger'),
        round: true,
    }));
    const __VLS_157 = __VLS_156({
        type: (__VLS_ctx.selectedPermission.enabled ? 'success' : 'danger'),
        round: true,
    }, ...__VLS_functionalComponentArgsRest(__VLS_156));
    __VLS_158.slots.default;
    (__VLS_ctx.selectedPermission.enabled ? '已启用' : '已停用');
    var __VLS_158;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "status-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-label" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "info-value" },
    });
    (__VLS_ctx.selectedPermission.visible ? '可见' : '隐藏');
    if (__VLS_ctx.selectedPermission.children?.length) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
            ...{ class: "detail-section" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "section-title" },
        });
        const __VLS_159 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_160 = __VLS_asFunctionalComponent(__VLS_159, new __VLS_159({}));
        const __VLS_161 = __VLS_160({}, ...__VLS_functionalComponentArgsRest(__VLS_160));
        __VLS_162.slots.default;
        const __VLS_163 = {}.Lightning;
        /** @type {[typeof __VLS_components.Lightning, ]} */ ;
        // @ts-ignore
        const __VLS_164 = __VLS_asFunctionalComponent(__VLS_163, new __VLS_163({}));
        const __VLS_165 = __VLS_164({}, ...__VLS_functionalComponentArgsRest(__VLS_164));
        var __VLS_162;
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        (__VLS_ctx.selectedPermission.children.length);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "child-permissions" },
        });
        for (const [child] of __VLS_getVForSourceType((__VLS_ctx.selectedPermission.children))) {
            __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                key: (child.id),
                ...{ class: "child-permission-card" },
            });
            __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                ...{ onClick: (...[$event]) => {
                        if (!(__VLS_ctx.selectedPermission))
                            return;
                        if (!(__VLS_ctx.selectedPermission.children?.length))
                            return;
                        __VLS_ctx.selectPermissionById(child.id);
                    } },
                ...{ class: "child-main" },
            });
            __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                ...{ class: "child-icon" },
            });
            const __VLS_167 = {}.ElIcon;
            /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
            // @ts-ignore
            const __VLS_168 = __VLS_asFunctionalComponent(__VLS_167, new __VLS_167({}));
            const __VLS_169 = __VLS_168({}, ...__VLS_functionalComponentArgsRest(__VLS_168));
            __VLS_170.slots.default;
            const __VLS_171 = ((__VLS_ctx.iconMap[child.icon] || __VLS_ctx.Key));
            // @ts-ignore
            const __VLS_172 = __VLS_asFunctionalComponent(__VLS_171, new __VLS_171({}));
            const __VLS_173 = __VLS_172({}, ...__VLS_functionalComponentArgsRest(__VLS_172));
            var __VLS_170;
            __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                ...{ class: "child-text" },
            });
            __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                ...{ class: "child-title" },
            });
            (child.name);
            __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                ...{ class: "child-code" },
            });
            (child.code);
            __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                ...{ class: "child-actions" },
            });
            const __VLS_175 = {}.ElButton;
            /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
            // @ts-ignore
            const __VLS_176 = __VLS_asFunctionalComponent(__VLS_175, new __VLS_175({
                ...{ 'onClick': {} },
                text: true,
                size: "small",
            }));
            const __VLS_177 = __VLS_176({
                ...{ 'onClick': {} },
                text: true,
                size: "small",
            }, ...__VLS_functionalComponentArgsRest(__VLS_176));
            let __VLS_179;
            let __VLS_180;
            let __VLS_181;
            const __VLS_182 = {
                onClick: (...[$event]) => {
                    if (!(__VLS_ctx.selectedPermission))
                        return;
                    if (!(__VLS_ctx.selectedPermission.children?.length))
                        return;
                    __VLS_ctx.openEditDialog(child);
                }
            };
            __VLS_178.slots.default;
            var __VLS_178;
            const __VLS_183 = {}.ElButton;
            /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
            // @ts-ignore
            const __VLS_184 = __VLS_asFunctionalComponent(__VLS_183, new __VLS_183({
                ...{ 'onClick': {} },
                text: true,
                size: "small",
                type: "danger",
            }));
            const __VLS_185 = __VLS_184({
                ...{ 'onClick': {} },
                text: true,
                size: "small",
                type: "danger",
            }, ...__VLS_functionalComponentArgsRest(__VLS_184));
            let __VLS_187;
            let __VLS_188;
            let __VLS_189;
            const __VLS_190 = {
                onClick: (...[$event]) => {
                    if (!(__VLS_ctx.selectedPermission))
                        return;
                    if (!(__VLS_ctx.selectedPermission.children?.length))
                        return;
                    __VLS_ctx.handleDeletePermission(child.id);
                }
            };
            __VLS_186.slots.default;
            var __VLS_186;
        }
    }
}
else {
    const __VLS_191 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_192 = __VLS_asFunctionalComponent(__VLS_191, new __VLS_191({
        description: "请选择左侧权限节点",
    }));
    const __VLS_193 = __VLS_192({
        description: "请选择左侧权限节点",
    }, ...__VLS_functionalComponentArgsRest(__VLS_192));
}
var __VLS_90;
const __VLS_195 = {}.ElDialog;
/** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
// @ts-ignore
const __VLS_196 = __VLS_asFunctionalComponent(__VLS_195, new __VLS_195({
    ...{ 'onClose': {} },
    modelValue: (__VLS_ctx.dialogVisible),
    title: (__VLS_ctx.isEditMode ? '编辑权限' : '新增权限'),
    width: "600px",
}));
const __VLS_197 = __VLS_196({
    ...{ 'onClose': {} },
    modelValue: (__VLS_ctx.dialogVisible),
    title: (__VLS_ctx.isEditMode ? '编辑权限' : '新增权限'),
    width: "600px",
}, ...__VLS_functionalComponentArgsRest(__VLS_196));
let __VLS_199;
let __VLS_200;
let __VLS_201;
const __VLS_202 = {
    onClose: (__VLS_ctx.resetForm)
};
__VLS_198.slots.default;
const __VLS_203 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_204 = __VLS_asFunctionalComponent(__VLS_203, new __VLS_203({
    ref: "formRef",
    model: (__VLS_ctx.formData),
    rules: (__VLS_ctx.formRules),
    labelWidth: "100px",
    labelPosition: "top",
}));
const __VLS_205 = __VLS_204({
    ref: "formRef",
    model: (__VLS_ctx.formData),
    rules: (__VLS_ctx.formRules),
    labelWidth: "100px",
    labelPosition: "top",
}, ...__VLS_functionalComponentArgsRest(__VLS_204));
/** @type {typeof __VLS_ctx.formRef} */ ;
var __VLS_207 = {};
__VLS_206.slots.default;
const __VLS_209 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_210 = __VLS_asFunctionalComponent(__VLS_209, new __VLS_209({
    label: "权限编码",
    prop: "code",
}));
const __VLS_211 = __VLS_210({
    label: "权限编码",
    prop: "code",
}, ...__VLS_functionalComponentArgsRest(__VLS_210));
__VLS_212.slots.default;
const __VLS_213 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_214 = __VLS_asFunctionalComponent(__VLS_213, new __VLS_213({
    modelValue: (__VLS_ctx.formData.code),
    placeholder: "如: system:user:add",
    disabled: (__VLS_ctx.isEditMode),
}));
const __VLS_215 = __VLS_214({
    modelValue: (__VLS_ctx.formData.code),
    placeholder: "如: system:user:add",
    disabled: (__VLS_ctx.isEditMode),
}, ...__VLS_functionalComponentArgsRest(__VLS_214));
var __VLS_212;
const __VLS_217 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_218 = __VLS_asFunctionalComponent(__VLS_217, new __VLS_217({
    label: "权限名称",
    prop: "name",
}));
const __VLS_219 = __VLS_218({
    label: "权限名称",
    prop: "name",
}, ...__VLS_functionalComponentArgsRest(__VLS_218));
__VLS_220.slots.default;
const __VLS_221 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_222 = __VLS_asFunctionalComponent(__VLS_221, new __VLS_221({
    modelValue: (__VLS_ctx.formData.name),
    placeholder: "如: 新增用户",
}));
const __VLS_223 = __VLS_222({
    modelValue: (__VLS_ctx.formData.name),
    placeholder: "如: 新增用户",
}, ...__VLS_functionalComponentArgsRest(__VLS_222));
var __VLS_220;
const __VLS_225 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_226 = __VLS_asFunctionalComponent(__VLS_225, new __VLS_225({
    label: "权限类型",
    prop: "type",
}));
const __VLS_227 = __VLS_226({
    label: "权限类型",
    prop: "type",
}, ...__VLS_functionalComponentArgsRest(__VLS_226));
__VLS_228.slots.default;
const __VLS_229 = {}.ElSelect;
/** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
// @ts-ignore
const __VLS_230 = __VLS_asFunctionalComponent(__VLS_229, new __VLS_229({
    modelValue: (__VLS_ctx.formData.type),
    placeholder: "选择权限类型",
}));
const __VLS_231 = __VLS_230({
    modelValue: (__VLS_ctx.formData.type),
    placeholder: "选择权限类型",
}, ...__VLS_functionalComponentArgsRest(__VLS_230));
__VLS_232.slots.default;
for (const [item] of __VLS_getVForSourceType((__VLS_ctx.permissionTypeItems))) {
    const __VLS_233 = {}.ElOption;
    /** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
    // @ts-ignore
    const __VLS_234 = __VLS_asFunctionalComponent(__VLS_233, new __VLS_233({
        key: (item.itemCode),
        label: (item.itemName),
        value: (item.itemCode.toLowerCase()),
    }));
    const __VLS_235 = __VLS_234({
        key: (item.itemCode),
        label: (item.itemName),
        value: (item.itemCode.toLowerCase()),
    }, ...__VLS_functionalComponentArgsRest(__VLS_234));
}
var __VLS_232;
var __VLS_228;
const __VLS_237 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_238 = __VLS_asFunctionalComponent(__VLS_237, new __VLS_237({
    label: "父权限",
    prop: "parentId",
}));
const __VLS_239 = __VLS_238({
    label: "父权限",
    prop: "parentId",
}, ...__VLS_functionalComponentArgsRest(__VLS_238));
__VLS_240.slots.default;
const __VLS_241 = {}.ElTreeSelect;
/** @type {[typeof __VLS_components.ElTreeSelect, typeof __VLS_components.elTreeSelect, ]} */ ;
// @ts-ignore
const __VLS_242 = __VLS_asFunctionalComponent(__VLS_241, new __VLS_241({
    modelValue: (__VLS_ctx.formData.parentId),
    data: (__VLS_ctx.permissionTree),
    nodeKey: "id",
    props: ({ label: 'name', children: 'children' }),
    placeholder: "选择父权限（可选）",
    clearable: true,
    checkStrictly: true,
}));
const __VLS_243 = __VLS_242({
    modelValue: (__VLS_ctx.formData.parentId),
    data: (__VLS_ctx.permissionTree),
    nodeKey: "id",
    props: ({ label: 'name', children: 'children' }),
    placeholder: "选择父权限（可选）",
    clearable: true,
    checkStrictly: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_242));
var __VLS_240;
const __VLS_245 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_246 = __VLS_asFunctionalComponent(__VLS_245, new __VLS_245({
    label: "路由路径",
    prop: "routePath",
}));
const __VLS_247 = __VLS_246({
    label: "路由路径",
    prop: "routePath",
}, ...__VLS_functionalComponentArgsRest(__VLS_246));
__VLS_248.slots.default;
const __VLS_249 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_250 = __VLS_asFunctionalComponent(__VLS_249, new __VLS_249({
    modelValue: (__VLS_ctx.formData.routePath),
    placeholder: "如: /users",
}));
const __VLS_251 = __VLS_250({
    modelValue: (__VLS_ctx.formData.routePath),
    placeholder: "如: /users",
}, ...__VLS_functionalComponentArgsRest(__VLS_250));
var __VLS_248;
const __VLS_253 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_254 = __VLS_asFunctionalComponent(__VLS_253, new __VLS_253({
    label: "图标",
    prop: "icon",
}));
const __VLS_255 = __VLS_254({
    label: "图标",
    prop: "icon",
}, ...__VLS_functionalComponentArgsRest(__VLS_254));
__VLS_256.slots.default;
const __VLS_257 = {}.ElSelect;
/** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
// @ts-ignore
const __VLS_258 = __VLS_asFunctionalComponent(__VLS_257, new __VLS_257({
    modelValue: (__VLS_ctx.formData.icon),
    placeholder: "选择图标",
}));
const __VLS_259 = __VLS_258({
    modelValue: (__VLS_ctx.formData.icon),
    placeholder: "选择图标",
}, ...__VLS_functionalComponentArgsRest(__VLS_258));
__VLS_260.slots.default;
const __VLS_261 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_262 = __VLS_asFunctionalComponent(__VLS_261, new __VLS_261({
    label: "Menu",
    value: "Menu",
}));
const __VLS_263 = __VLS_262({
    label: "Menu",
    value: "Menu",
}, ...__VLS_functionalComponentArgsRest(__VLS_262));
const __VLS_265 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_266 = __VLS_asFunctionalComponent(__VLS_265, new __VLS_265({
    label: "Setting",
    value: "Setting",
}));
const __VLS_267 = __VLS_266({
    label: "Setting",
    value: "Setting",
}, ...__VLS_functionalComponentArgsRest(__VLS_266));
const __VLS_269 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_270 = __VLS_asFunctionalComponent(__VLS_269, new __VLS_269({
    label: "User",
    value: "User",
}));
const __VLS_271 = __VLS_270({
    label: "User",
    value: "User",
}, ...__VLS_functionalComponentArgsRest(__VLS_270));
const __VLS_273 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_274 = __VLS_asFunctionalComponent(__VLS_273, new __VLS_273({
    label: "Lock",
    value: "Lock",
}));
const __VLS_275 = __VLS_274({
    label: "Lock",
    value: "Lock",
}, ...__VLS_functionalComponentArgsRest(__VLS_274));
const __VLS_277 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_278 = __VLS_asFunctionalComponent(__VLS_277, new __VLS_277({
    label: "Key",
    value: "Key",
}));
const __VLS_279 = __VLS_278({
    label: "Key",
    value: "Key",
}, ...__VLS_functionalComponentArgsRest(__VLS_278));
const __VLS_281 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_282 = __VLS_asFunctionalComponent(__VLS_281, new __VLS_281({
    label: "Plus",
    value: "Plus",
}));
const __VLS_283 = __VLS_282({
    label: "Plus",
    value: "Plus",
}, ...__VLS_functionalComponentArgsRest(__VLS_282));
const __VLS_285 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_286 = __VLS_asFunctionalComponent(__VLS_285, new __VLS_285({
    label: "Edit",
    value: "Edit",
}));
const __VLS_287 = __VLS_286({
    label: "Edit",
    value: "Edit",
}, ...__VLS_functionalComponentArgsRest(__VLS_286));
const __VLS_289 = {}.ElOption;
/** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
// @ts-ignore
const __VLS_290 = __VLS_asFunctionalComponent(__VLS_289, new __VLS_289({
    label: "Delete",
    value: "Delete",
}));
const __VLS_291 = __VLS_290({
    label: "Delete",
    value: "Delete",
}, ...__VLS_functionalComponentArgsRest(__VLS_290));
var __VLS_260;
var __VLS_256;
const __VLS_293 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_294 = __VLS_asFunctionalComponent(__VLS_293, new __VLS_293({
    label: "排序号",
    prop: "sort",
}));
const __VLS_295 = __VLS_294({
    label: "排序号",
    prop: "sort",
}, ...__VLS_functionalComponentArgsRest(__VLS_294));
__VLS_296.slots.default;
const __VLS_297 = {}.ElInputNumber;
/** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
// @ts-ignore
const __VLS_298 = __VLS_asFunctionalComponent(__VLS_297, new __VLS_297({
    modelValue: (__VLS_ctx.formData.sort),
    min: (0),
    max: (9999),
}));
const __VLS_299 = __VLS_298({
    modelValue: (__VLS_ctx.formData.sort),
    min: (0),
    max: (9999),
}, ...__VLS_functionalComponentArgsRest(__VLS_298));
var __VLS_296;
const __VLS_301 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_302 = __VLS_asFunctionalComponent(__VLS_301, new __VLS_301({
    label: "描述",
    prop: "description",
}));
const __VLS_303 = __VLS_302({
    label: "描述",
    prop: "description",
}, ...__VLS_functionalComponentArgsRest(__VLS_302));
__VLS_304.slots.default;
const __VLS_305 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_306 = __VLS_asFunctionalComponent(__VLS_305, new __VLS_305({
    modelValue: (__VLS_ctx.formData.description),
    type: "textarea",
    rows: "3",
    placeholder: "权限描述（可选）",
}));
const __VLS_307 = __VLS_306({
    modelValue: (__VLS_ctx.formData.description),
    type: "textarea",
    rows: "3",
    placeholder: "权限描述（可选）",
}, ...__VLS_functionalComponentArgsRest(__VLS_306));
var __VLS_304;
const __VLS_309 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_310 = __VLS_asFunctionalComponent(__VLS_309, new __VLS_309({
    label: "状态",
}));
const __VLS_311 = __VLS_310({
    label: "状态",
}, ...__VLS_functionalComponentArgsRest(__VLS_310));
__VLS_312.slots.default;
const __VLS_313 = {}.ElSwitch;
/** @type {[typeof __VLS_components.ElSwitch, typeof __VLS_components.elSwitch, ]} */ ;
// @ts-ignore
const __VLS_314 = __VLS_asFunctionalComponent(__VLS_313, new __VLS_313({
    modelValue: (__VLS_ctx.formData.enabled),
    activeText: "启用",
    inactiveText: "停用",
}));
const __VLS_315 = __VLS_314({
    modelValue: (__VLS_ctx.formData.enabled),
    activeText: "启用",
    inactiveText: "停用",
}, ...__VLS_functionalComponentArgsRest(__VLS_314));
var __VLS_312;
const __VLS_317 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_318 = __VLS_asFunctionalComponent(__VLS_317, new __VLS_317({
    label: "可见性",
}));
const __VLS_319 = __VLS_318({
    label: "可见性",
}, ...__VLS_functionalComponentArgsRest(__VLS_318));
__VLS_320.slots.default;
const __VLS_321 = {}.ElSwitch;
/** @type {[typeof __VLS_components.ElSwitch, typeof __VLS_components.elSwitch, ]} */ ;
// @ts-ignore
const __VLS_322 = __VLS_asFunctionalComponent(__VLS_321, new __VLS_321({
    modelValue: (__VLS_ctx.formData.visible),
    activeText: "可见",
    inactiveText: "隐藏",
}));
const __VLS_323 = __VLS_322({
    modelValue: (__VLS_ctx.formData.visible),
    activeText: "可见",
    inactiveText: "隐藏",
}, ...__VLS_functionalComponentArgsRest(__VLS_322));
var __VLS_320;
var __VLS_206;
{
    const { footer: __VLS_thisSlot } = __VLS_198.slots;
    const __VLS_325 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_326 = __VLS_asFunctionalComponent(__VLS_325, new __VLS_325({
        ...{ 'onClick': {} },
    }));
    const __VLS_327 = __VLS_326({
        ...{ 'onClick': {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_326));
    let __VLS_329;
    let __VLS_330;
    let __VLS_331;
    const __VLS_332 = {
        onClick: (...[$event]) => {
            __VLS_ctx.dialogVisible = false;
        }
    };
    __VLS_328.slots.default;
    var __VLS_328;
    const __VLS_333 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_334 = __VLS_asFunctionalComponent(__VLS_333, new __VLS_333({
        ...{ 'onClick': {} },
        type: "primary",
        loading: (__VLS_ctx.submitLoading),
    }));
    const __VLS_335 = __VLS_334({
        ...{ 'onClick': {} },
        type: "primary",
        loading: (__VLS_ctx.submitLoading),
    }, ...__VLS_functionalComponentArgsRest(__VLS_334));
    let __VLS_337;
    let __VLS_338;
    let __VLS_339;
    const __VLS_340 = {
        onClick: (__VLS_ctx.handleSubmit)
    };
    __VLS_336.slots.default;
    (__VLS_ctx.isEditMode ? '更新' : '创建');
    var __VLS_336;
}
var __VLS_198;
/** @type {__VLS_StyleScopedClasses['permissions-page']} */ ;
/** @type {__VLS_StyleScopedClasses['permissions-layout']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-header']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-title-wrap']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-title-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-title']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-search']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['permission-tree']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-node']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-node-main']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-node-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-node-text']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-node-title']} */ ;
/** @type {__VLS_StyleScopedClasses['tree-node-code']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-header']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-title-wrap']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-title']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-subtitle']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-section']} */ ;
/** @type {__VLS_StyleScopedClasses['section-title']} */ ;
/** @type {__VLS_StyleScopedClasses['info-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['info-item']} */ ;
/** @type {__VLS_StyleScopedClasses['info-label']} */ ;
/** @type {__VLS_StyleScopedClasses['info-value']} */ ;
/** @type {__VLS_StyleScopedClasses['info-item']} */ ;
/** @type {__VLS_StyleScopedClasses['info-label']} */ ;
/** @type {__VLS_StyleScopedClasses['info-value']} */ ;
/** @type {__VLS_StyleScopedClasses['info-item']} */ ;
/** @type {__VLS_StyleScopedClasses['info-label']} */ ;
/** @type {__VLS_StyleScopedClasses['info-value']} */ ;
/** @type {__VLS_StyleScopedClasses['info-item']} */ ;
/** @type {__VLS_StyleScopedClasses['info-label']} */ ;
/** @type {__VLS_StyleScopedClasses['info-value']} */ ;
/** @type {__VLS_StyleScopedClasses['info-item']} */ ;
/** @type {__VLS_StyleScopedClasses['info-label']} */ ;
/** @type {__VLS_StyleScopedClasses['info-value']} */ ;
/** @type {__VLS_StyleScopedClasses['info-item']} */ ;
/** @type {__VLS_StyleScopedClasses['info-label']} */ ;
/** @type {__VLS_StyleScopedClasses['info-value']} */ ;
/** @type {__VLS_StyleScopedClasses['description-box']} */ ;
/** @type {__VLS_StyleScopedClasses['info-label']} */ ;
/** @type {__VLS_StyleScopedClasses['description-text']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-section']} */ ;
/** @type {__VLS_StyleScopedClasses['section-title']} */ ;
/** @type {__VLS_StyleScopedClasses['status-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['status-item']} */ ;
/** @type {__VLS_StyleScopedClasses['info-label']} */ ;
/** @type {__VLS_StyleScopedClasses['status-item']} */ ;
/** @type {__VLS_StyleScopedClasses['info-label']} */ ;
/** @type {__VLS_StyleScopedClasses['info-value']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-section']} */ ;
/** @type {__VLS_StyleScopedClasses['section-title']} */ ;
/** @type {__VLS_StyleScopedClasses['child-permissions']} */ ;
/** @type {__VLS_StyleScopedClasses['child-permission-card']} */ ;
/** @type {__VLS_StyleScopedClasses['child-main']} */ ;
/** @type {__VLS_StyleScopedClasses['child-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['child-text']} */ ;
/** @type {__VLS_StyleScopedClasses['child-title']} */ ;
/** @type {__VLS_StyleScopedClasses['child-code']} */ ;
/** @type {__VLS_StyleScopedClasses['child-actions']} */ ;
// @ts-ignore
var __VLS_74 = __VLS_73, __VLS_208 = __VLS_207;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            Plus: Plus,
            Search: Search,
            Menu: Menu,
            Key: Key,
            Edit: Edit,
            Delete: Delete,
            InfoFilled: InfoFilled,
            Opportunity: Opportunity,
            Lightning: Lightning,
            Lock: Lock,
            treeRef: treeRef,
            formRef: formRef,
            searchKeyword: searchKeyword,
            expandedKeys: expandedKeys,
            loading: loading,
            toggleLoading: toggleLoading,
            submitLoading: submitLoading,
            dialogVisible: dialogVisible,
            isEditMode: isEditMode,
            iconMap: iconMap,
            permissionTypeItems: permissionTypeItems,
            typeLabelMap: typeLabelMap,
            permissionTree: permissionTree,
            formData: formData,
            formRules: formRules,
            treeProps: treeProps,
            getTypeLabel: getTypeLabel,
            getTagType: getTagType,
            selectedPermission: selectedPermission,
            filteredPermissionTree: filteredPermissionTree,
            loadPermissions: loadPermissions,
            handleNodeClick: handleNodeClick,
            selectPermissionById: selectPermissionById,
            expandAll: expandAll,
            collapseAll: collapseAll,
            resetTree: resetTree,
            resetForm: resetForm,
            openCreateDialog: openCreateDialog,
            openEditDialog: openEditDialog,
            handleSubmit: handleSubmit,
            handleTogglePermission: handleTogglePermission,
            handleDeletePermission: handleDeletePermission,
            handleNodeDrop: handleNodeDrop,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
