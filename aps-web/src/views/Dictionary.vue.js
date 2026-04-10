/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { onMounted, ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { Plus } from '@element-plus/icons-vue';
import { dictionaryApi } from '@/api/dictionary';
import { confirmDanger, extractErrorMsg, msgError, msgSuccess } from '@/utils/message';
const authStore = useAuthStore();
const canListTypes = authStore.hasPermission('system:dict:type:list');
const canListItems = authStore.hasPermission('system:dict:item:list');
const canQueryDictionary = authStore.hasPermission('system:dict:query');
const canAccessDictionaryPage = canListTypes || canListItems || canQueryDictionary;
const canAddType = authStore.hasPermission('system:dict:type:add');
const canEditType = authStore.hasPermission('system:dict:type:edit');
const canRemoveType = authStore.hasPermission('system:dict:type:remove');
const canAddItem = authStore.hasPermission('system:dict:item:add');
const canEditItem = authStore.hasPermission('system:dict:item:edit');
const canRemoveItem = authStore.hasPermission('system:dict:item:remove');
const typeLoading = ref(false);
const itemLoading = ref(false);
const typeList = ref([]);
const itemList = ref([]);
const selectedType = ref(null);
const typeKeyword = ref('');
const itemKeyword = ref('');
const typePageNo = ref(1);
const typePageSize = ref(10);
const typeTotal = ref(0);
const itemPageNo = ref(1);
const itemPageSize = ref(10);
const itemTotal = ref(0);
const typeDialogVisible = ref(false);
const itemDialogVisible = ref(false);
const editingType = ref(null);
const editingItem = ref(null);
const typeFormRef = ref();
const itemFormRef = ref();
const typeForm = ref({
    code: '',
    name: '',
    description: '',
    enabled: true,
    sortOrder: 0,
});
const itemForm = ref({
    itemCode: '',
    itemName: '',
    itemValue: '',
    description: '',
    enabled: true,
    sortOrder: 0,
    isSystem: false,
});
const typeRules = {
    code: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
    name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
};
const itemRules = {
    itemCode: [{ required: true, message: '请输入项编码', trigger: 'blur' }],
    itemName: [{ required: true, message: '请输入项名称', trigger: 'blur' }],
    itemValue: [{ required: true, message: '请输入项值', trigger: 'blur' }],
};
onMounted(async () => {
    await loadTypes();
});
async function loadTypes() {
    if (!canListTypes) {
        typeList.value = [];
        typeTotal.value = 0;
        selectedType.value = null;
        itemList.value = [];
        itemTotal.value = 0;
        return;
    }
    typeLoading.value = true;
    try {
        const page = await dictionaryApi.getTypes({
            pageNo: typePageNo.value,
            pageSize: typePageSize.value,
            keyword: typeKeyword.value || undefined,
        });
        typeList.value = page.items;
        typeTotal.value = page.total;
        if (page.items.length === 0) {
            selectedType.value = null;
            itemList.value = [];
            itemTotal.value = 0;
            return;
        }
        const nextSelected = selectedType.value
            ? page.items.find(item => item.id === selectedType.value?.id) ?? page.items[0]
            : page.items[0];
        selectedType.value = nextSelected;
        await loadItems();
    }
    catch (error) {
        msgError(extractErrorMsg(error, '加载字典类型失败'));
    }
    finally {
        typeLoading.value = false;
    }
}
function handleTypeSelect(row) {
    if (!row) {
        return;
    }
    selectedType.value = row;
    itemPageNo.value = 1;
    loadItems();
}
async function loadItems() {
    if (!canListItems || !selectedType.value) {
        itemList.value = [];
        itemTotal.value = 0;
        return;
    }
    itemLoading.value = true;
    try {
        const typeId = selectedType.value.id;
        const page = await dictionaryApi.getItemsByType(typeId, {
            pageNo: itemPageNo.value,
            pageSize: itemPageSize.value,
            keyword: itemKeyword.value || undefined,
        });
        itemList.value = page.items;
        itemTotal.value = page.total;
    }
    catch (error) {
        msgError(extractErrorMsg(error, '加载字典项失败'));
    }
    finally {
        itemLoading.value = false;
    }
}
function handleTypeSizeChange(size) {
    typePageSize.value = size;
    typePageNo.value = 1;
    loadTypes();
}
function handleItemSizeChange(size) {
    itemPageSize.value = size;
    itemPageNo.value = 1;
    loadItems();
}
function openTypeDialog(type) {
    editingType.value = type ?? null;
    typeForm.value = type
        ? {
            code: type.code,
            name: type.name,
            description: type.description ?? '',
            enabled: type.enabled,
            sortOrder: type.sortOrder,
        }
        : {
            code: '',
            name: '',
            description: '',
            enabled: true,
            sortOrder: 0,
        };
    typeDialogVisible.value = true;
}
async function saveType() {
    if (!typeFormRef.value) {
        return;
    }
    await typeFormRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        try {
            const payload = {
                ...typeForm.value,
                code: typeForm.value.code.trim().toUpperCase(),
                name: typeForm.value.name.trim(),
                description: typeForm.value.description?.trim(),
            };
            if (editingType.value) {
                await dictionaryApi.updateType(editingType.value.id, payload);
                msgSuccess('字典类型已更新');
            }
            else {
                await dictionaryApi.createType(payload);
                msgSuccess('字典类型已创建');
            }
            typeDialogVisible.value = false;
            await loadTypes();
        }
        catch (error) {
            msgError(extractErrorMsg(error, '保存字典类型失败'));
        }
    });
}
async function deleteType(type) {
    try {
        await confirmDanger(`确定删除字典类型“${type.name}”吗？`);
        await dictionaryApi.deleteType(type.id);
        msgSuccess('字典类型已删除');
        if (selectedType.value?.id === type.id) {
            selectedType.value = null;
        }
        await loadTypes();
    }
    catch (error) {
        if (error !== 'cancel') {
            msgError(extractErrorMsg(error, '删除字典类型失败'));
        }
    }
}
function openItemDialog(item) {
    if (!selectedType.value) {
        msgError('请先选择字典类型');
        return;
    }
    editingItem.value = item ?? null;
    itemForm.value = item
        ? {
            itemCode: item.itemCode,
            itemName: item.itemName,
            itemValue: item.itemValue,
            description: item.description ?? '',
            enabled: item.enabled,
            sortOrder: item.sortOrder,
            isSystem: item.isSystem,
        }
        : {
            itemCode: '',
            itemName: '',
            itemValue: '',
            description: '',
            enabled: true,
            sortOrder: 0,
            isSystem: false,
        };
    itemDialogVisible.value = true;
}
async function saveItem() {
    if (!itemFormRef.value || !selectedType.value) {
        return;
    }
    await itemFormRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        try {
            const payload = {
                ...itemForm.value,
                itemCode: itemForm.value.itemCode.trim().toUpperCase(),
                itemName: itemForm.value.itemName.trim(),
                itemValue: itemForm.value.itemValue.trim(),
                description: itemForm.value.description?.trim(),
            };
            const selected = selectedType.value;
            if (!selected) {
                msgError('请先选择字典类型');
                return;
            }
            const typeId = selected.id;
            if (editingItem.value) {
                await dictionaryApi.updateItem(editingItem.value.id, payload);
                msgSuccess('字典项已更新');
            }
            else {
                await dictionaryApi.createItem(typeId, payload);
                msgSuccess('字典项已创建');
            }
            itemDialogVisible.value = false;
            await loadItems();
        }
        catch (error) {
            msgError(extractErrorMsg(error, '保存字典项失败'));
        }
    });
}
async function toggleItem(item, enabled) {
    try {
        await dictionaryApi.updateItem(item.id, {
            itemCode: item.itemCode,
            itemName: item.itemName,
            itemValue: item.itemValue,
            description: item.description ?? '',
            enabled,
            sortOrder: item.sortOrder,
            isSystem: item.isSystem
        });
        item.enabled = enabled;
        msgSuccess('字典项状态已更新');
    }
    catch (error) {
        msgError(extractErrorMsg(error, '更新字典项状态失败'));
        await loadItems();
    }
}
async function deleteItem(item) {
    try {
        await confirmDanger(`确定删除字典项“${item.itemName}”吗？`);
        await dictionaryApi.deleteItem(item.id);
        msgSuccess('字典项已删除');
        await loadItems();
    }
    catch (error) {
        if (error !== 'cancel') {
            msgError(extractErrorMsg(error, '删除字典项失败'));
        }
    }
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "dictionary-page" },
});
if (!__VLS_ctx.canAccessDictionaryPage) {
    const __VLS_0 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
        description: "暂无编码管理权限",
    }));
    const __VLS_2 = __VLS_1({
        description: "暂无编码管理权限",
    }, ...__VLS_functionalComponentArgsRest(__VLS_1));
}
else {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "page-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
    if (__VLS_ctx.canAddType) {
        const __VLS_4 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_5 = __VLS_asFunctionalComponent(__VLS_4, new __VLS_4({
            ...{ 'onClick': {} },
            type: "primary",
        }));
        const __VLS_6 = __VLS_5({
            ...{ 'onClick': {} },
            type: "primary",
        }, ...__VLS_functionalComponentArgsRest(__VLS_5));
        let __VLS_8;
        let __VLS_9;
        let __VLS_10;
        const __VLS_11 = {
            onClick: (...[$event]) => {
                if (!!(!__VLS_ctx.canAccessDictionaryPage))
                    return;
                if (!(__VLS_ctx.canAddType))
                    return;
                __VLS_ctx.openTypeDialog();
            }
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
        var __VLS_7;
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "content-wrapper" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "left-panel" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "panel-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "panel-title" },
    });
    const __VLS_20 = {}.ElInput;
    /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
    // @ts-ignore
    const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({
        ...{ 'onKeyup': {} },
        modelValue: (__VLS_ctx.typeKeyword),
        placeholder: "搜索类型编码/名称",
        clearable: true,
        disabled: (!__VLS_ctx.canListTypes),
    }));
    const __VLS_22 = __VLS_21({
        ...{ 'onKeyup': {} },
        modelValue: (__VLS_ctx.typeKeyword),
        placeholder: "搜索类型编码/名称",
        clearable: true,
        disabled: (!__VLS_ctx.canListTypes),
    }, ...__VLS_functionalComponentArgsRest(__VLS_21));
    let __VLS_24;
    let __VLS_25;
    let __VLS_26;
    const __VLS_27 = {
        onKeyup: (__VLS_ctx.loadTypes)
    };
    __VLS_23.slots.default;
    {
        const { append: __VLS_thisSlot } = __VLS_23.slots;
        const __VLS_28 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
            ...{ 'onClick': {} },
            disabled: (!__VLS_ctx.canListTypes),
        }));
        const __VLS_30 = __VLS_29({
            ...{ 'onClick': {} },
            disabled: (!__VLS_ctx.canListTypes),
        }, ...__VLS_functionalComponentArgsRest(__VLS_29));
        let __VLS_32;
        let __VLS_33;
        let __VLS_34;
        const __VLS_35 = {
            onClick: (__VLS_ctx.loadTypes)
        };
        __VLS_31.slots.default;
        var __VLS_31;
    }
    var __VLS_23;
    const __VLS_36 = {}.ElTable;
    /** @type {[typeof __VLS_components.ElTable, typeof __VLS_components.elTable, typeof __VLS_components.ElTable, typeof __VLS_components.elTable, ]} */ ;
    // @ts-ignore
    const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({
        ...{ 'onCurrentChange': {} },
        data: (__VLS_ctx.typeList),
        height: "460",
        highlightCurrentRow: true,
    }));
    const __VLS_38 = __VLS_37({
        ...{ 'onCurrentChange': {} },
        data: (__VLS_ctx.typeList),
        height: "460",
        highlightCurrentRow: true,
    }, ...__VLS_functionalComponentArgsRest(__VLS_37));
    let __VLS_40;
    let __VLS_41;
    let __VLS_42;
    const __VLS_43 = {
        onCurrentChange: (__VLS_ctx.handleTypeSelect)
    };
    __VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.typeLoading) }, null, null);
    __VLS_39.slots.default;
    const __VLS_44 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_45 = __VLS_asFunctionalComponent(__VLS_44, new __VLS_44({
        prop: "code",
        label: "编码",
        minWidth: "120",
    }));
    const __VLS_46 = __VLS_45({
        prop: "code",
        label: "编码",
        minWidth: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_45));
    const __VLS_48 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({
        prop: "name",
        label: "名称",
        minWidth: "120",
    }));
    const __VLS_50 = __VLS_49({
        prop: "name",
        label: "名称",
        minWidth: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_49));
    const __VLS_52 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_53 = __VLS_asFunctionalComponent(__VLS_52, new __VLS_52({
        label: "状态",
        width: "90",
    }));
    const __VLS_54 = __VLS_53({
        label: "状态",
        width: "90",
    }, ...__VLS_functionalComponentArgsRest(__VLS_53));
    __VLS_55.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_55.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        const __VLS_56 = {}.ElTag;
        /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
        // @ts-ignore
        const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({
            type: (row.enabled ? 'success' : 'info'),
            size: "small",
        }));
        const __VLS_58 = __VLS_57({
            type: (row.enabled ? 'success' : 'info'),
            size: "small",
        }, ...__VLS_functionalComponentArgsRest(__VLS_57));
        __VLS_59.slots.default;
        (row.enabled ? '启用' : '停用');
        var __VLS_59;
    }
    var __VLS_55;
    if (__VLS_ctx.canEditType || __VLS_ctx.canRemoveType) {
        const __VLS_60 = {}.ElTableColumn;
        /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
        // @ts-ignore
        const __VLS_61 = __VLS_asFunctionalComponent(__VLS_60, new __VLS_60({
            label: "操作",
            width: "150",
            fixed: "right",
        }));
        const __VLS_62 = __VLS_61({
            label: "操作",
            width: "150",
            fixed: "right",
        }, ...__VLS_functionalComponentArgsRest(__VLS_61));
        __VLS_63.slots.default;
        {
            const { default: __VLS_thisSlot } = __VLS_63.slots;
            const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
            if (__VLS_ctx.canEditType) {
                const __VLS_64 = {}.ElButton;
                /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
                // @ts-ignore
                const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({
                    ...{ 'onClick': {} },
                    text: true,
                    type: "primary",
                }));
                const __VLS_66 = __VLS_65({
                    ...{ 'onClick': {} },
                    text: true,
                    type: "primary",
                }, ...__VLS_functionalComponentArgsRest(__VLS_65));
                let __VLS_68;
                let __VLS_69;
                let __VLS_70;
                const __VLS_71 = {
                    onClick: (...[$event]) => {
                        if (!!(!__VLS_ctx.canAccessDictionaryPage))
                            return;
                        if (!(__VLS_ctx.canEditType || __VLS_ctx.canRemoveType))
                            return;
                        if (!(__VLS_ctx.canEditType))
                            return;
                        __VLS_ctx.openTypeDialog(row);
                    }
                };
                __VLS_67.slots.default;
                var __VLS_67;
            }
            if (__VLS_ctx.canRemoveType) {
                const __VLS_72 = {}.ElButton;
                /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
                // @ts-ignore
                const __VLS_73 = __VLS_asFunctionalComponent(__VLS_72, new __VLS_72({
                    ...{ 'onClick': {} },
                    text: true,
                    type: "danger",
                }));
                const __VLS_74 = __VLS_73({
                    ...{ 'onClick': {} },
                    text: true,
                    type: "danger",
                }, ...__VLS_functionalComponentArgsRest(__VLS_73));
                let __VLS_76;
                let __VLS_77;
                let __VLS_78;
                const __VLS_79 = {
                    onClick: (...[$event]) => {
                        if (!!(!__VLS_ctx.canAccessDictionaryPage))
                            return;
                        if (!(__VLS_ctx.canEditType || __VLS_ctx.canRemoveType))
                            return;
                        if (!(__VLS_ctx.canRemoveType))
                            return;
                        __VLS_ctx.deleteType(row);
                    }
                };
                __VLS_75.slots.default;
                var __VLS_75;
            }
        }
        var __VLS_63;
    }
    var __VLS_39;
    const __VLS_80 = {}.ElPagination;
    /** @type {[typeof __VLS_components.ElPagination, typeof __VLS_components.elPagination, ]} */ ;
    // @ts-ignore
    const __VLS_81 = __VLS_asFunctionalComponent(__VLS_80, new __VLS_80({
        ...{ 'onCurrentChange': {} },
        ...{ 'onSizeChange': {} },
        ...{ class: "pager" },
        currentPage: (__VLS_ctx.typePageNo),
        pageSize: (__VLS_ctx.typePageSize),
        total: (__VLS_ctx.typeTotal),
        layout: "total, sizes, prev, pager, next",
        pageSizes: ([10, 20, 50]),
    }));
    const __VLS_82 = __VLS_81({
        ...{ 'onCurrentChange': {} },
        ...{ 'onSizeChange': {} },
        ...{ class: "pager" },
        currentPage: (__VLS_ctx.typePageNo),
        pageSize: (__VLS_ctx.typePageSize),
        total: (__VLS_ctx.typeTotal),
        layout: "total, sizes, prev, pager, next",
        pageSizes: ([10, 20, 50]),
    }, ...__VLS_functionalComponentArgsRest(__VLS_81));
    let __VLS_84;
    let __VLS_85;
    let __VLS_86;
    const __VLS_87 = {
        onCurrentChange: (__VLS_ctx.loadTypes)
    };
    const __VLS_88 = {
        onSizeChange: (__VLS_ctx.handleTypeSizeChange)
    };
    var __VLS_83;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "right-panel" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "panel-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "panel-title" },
    });
    if (__VLS_ctx.selectedType) {
        (__VLS_ctx.selectedType.name);
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "item-actions" },
    });
    const __VLS_89 = {}.ElInput;
    /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
    // @ts-ignore
    const __VLS_90 = __VLS_asFunctionalComponent(__VLS_89, new __VLS_89({
        ...{ 'onKeyup': {} },
        modelValue: (__VLS_ctx.itemKeyword),
        placeholder: "搜索项编码/名称/值",
        clearable: true,
        disabled: (!__VLS_ctx.canListItems),
        ...{ style: {} },
    }));
    const __VLS_91 = __VLS_90({
        ...{ 'onKeyup': {} },
        modelValue: (__VLS_ctx.itemKeyword),
        placeholder: "搜索项编码/名称/值",
        clearable: true,
        disabled: (!__VLS_ctx.canListItems),
        ...{ style: {} },
    }, ...__VLS_functionalComponentArgsRest(__VLS_90));
    let __VLS_93;
    let __VLS_94;
    let __VLS_95;
    const __VLS_96 = {
        onKeyup: (__VLS_ctx.loadItems)
    };
    var __VLS_92;
    const __VLS_97 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_98 = __VLS_asFunctionalComponent(__VLS_97, new __VLS_97({
        ...{ 'onClick': {} },
        disabled: (!__VLS_ctx.canListItems),
    }));
    const __VLS_99 = __VLS_98({
        ...{ 'onClick': {} },
        disabled: (!__VLS_ctx.canListItems),
    }, ...__VLS_functionalComponentArgsRest(__VLS_98));
    let __VLS_101;
    let __VLS_102;
    let __VLS_103;
    const __VLS_104 = {
        onClick: (__VLS_ctx.loadItems)
    };
    __VLS_100.slots.default;
    var __VLS_100;
    if (__VLS_ctx.canAddItem) {
        const __VLS_105 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_106 = __VLS_asFunctionalComponent(__VLS_105, new __VLS_105({
            ...{ 'onClick': {} },
            type: "primary",
            disabled: (!__VLS_ctx.selectedType),
        }));
        const __VLS_107 = __VLS_106({
            ...{ 'onClick': {} },
            type: "primary",
            disabled: (!__VLS_ctx.selectedType),
        }, ...__VLS_functionalComponentArgsRest(__VLS_106));
        let __VLS_109;
        let __VLS_110;
        let __VLS_111;
        const __VLS_112 = {
            onClick: (...[$event]) => {
                if (!!(!__VLS_ctx.canAccessDictionaryPage))
                    return;
                if (!(__VLS_ctx.canAddItem))
                    return;
                __VLS_ctx.openItemDialog();
            }
        };
        __VLS_108.slots.default;
        const __VLS_113 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_114 = __VLS_asFunctionalComponent(__VLS_113, new __VLS_113({}));
        const __VLS_115 = __VLS_114({}, ...__VLS_functionalComponentArgsRest(__VLS_114));
        __VLS_116.slots.default;
        const __VLS_117 = {}.Plus;
        /** @type {[typeof __VLS_components.Plus, ]} */ ;
        // @ts-ignore
        const __VLS_118 = __VLS_asFunctionalComponent(__VLS_117, new __VLS_117({}));
        const __VLS_119 = __VLS_118({}, ...__VLS_functionalComponentArgsRest(__VLS_118));
        var __VLS_116;
        var __VLS_108;
    }
    const __VLS_121 = {}.ElTable;
    /** @type {[typeof __VLS_components.ElTable, typeof __VLS_components.elTable, typeof __VLS_components.ElTable, typeof __VLS_components.elTable, ]} */ ;
    // @ts-ignore
    const __VLS_122 = __VLS_asFunctionalComponent(__VLS_121, new __VLS_121({
        data: (__VLS_ctx.itemList),
        height: "460",
    }));
    const __VLS_123 = __VLS_122({
        data: (__VLS_ctx.itemList),
        height: "460",
    }, ...__VLS_functionalComponentArgsRest(__VLS_122));
    __VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.itemLoading) }, null, null);
    __VLS_124.slots.default;
    const __VLS_125 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_126 = __VLS_asFunctionalComponent(__VLS_125, new __VLS_125({
        prop: "itemCode",
        label: "项编码",
        minWidth: "110",
    }));
    const __VLS_127 = __VLS_126({
        prop: "itemCode",
        label: "项编码",
        minWidth: "110",
    }, ...__VLS_functionalComponentArgsRest(__VLS_126));
    const __VLS_129 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_130 = __VLS_asFunctionalComponent(__VLS_129, new __VLS_129({
        prop: "itemName",
        label: "项名称",
        minWidth: "120",
    }));
    const __VLS_131 = __VLS_130({
        prop: "itemName",
        label: "项名称",
        minWidth: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_130));
    const __VLS_133 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_134 = __VLS_asFunctionalComponent(__VLS_133, new __VLS_133({
        prop: "itemValue",
        label: "项值",
        minWidth: "120",
    }));
    const __VLS_135 = __VLS_134({
        prop: "itemValue",
        label: "项值",
        minWidth: "120",
    }, ...__VLS_functionalComponentArgsRest(__VLS_134));
    if (__VLS_ctx.canEditItem) {
        const __VLS_137 = {}.ElTableColumn;
        /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
        // @ts-ignore
        const __VLS_138 = __VLS_asFunctionalComponent(__VLS_137, new __VLS_137({
            label: "状态",
            width: "120",
        }));
        const __VLS_139 = __VLS_138({
            label: "状态",
            width: "120",
        }, ...__VLS_functionalComponentArgsRest(__VLS_138));
        __VLS_140.slots.default;
        {
            const { default: __VLS_thisSlot } = __VLS_140.slots;
            const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
            const __VLS_141 = {}.ElSwitch;
            /** @type {[typeof __VLS_components.ElSwitch, typeof __VLS_components.elSwitch, ]} */ ;
            // @ts-ignore
            const __VLS_142 = __VLS_asFunctionalComponent(__VLS_141, new __VLS_141({
                ...{ 'onChange': {} },
                modelValue: (row.enabled),
                inlinePrompt: true,
                activeText: "启用",
                inactiveText: "停用",
            }));
            const __VLS_143 = __VLS_142({
                ...{ 'onChange': {} },
                modelValue: (row.enabled),
                inlinePrompt: true,
                activeText: "启用",
                inactiveText: "停用",
            }, ...__VLS_functionalComponentArgsRest(__VLS_142));
            let __VLS_145;
            let __VLS_146;
            let __VLS_147;
            const __VLS_148 = {
                onChange: ((value) => __VLS_ctx.toggleItem(row, value))
            };
            var __VLS_144;
        }
        var __VLS_140;
    }
    else {
        const __VLS_149 = {}.ElTableColumn;
        /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
        // @ts-ignore
        const __VLS_150 = __VLS_asFunctionalComponent(__VLS_149, new __VLS_149({
            label: "状态",
            width: "90",
        }));
        const __VLS_151 = __VLS_150({
            label: "状态",
            width: "90",
        }, ...__VLS_functionalComponentArgsRest(__VLS_150));
        __VLS_152.slots.default;
        {
            const { default: __VLS_thisSlot } = __VLS_152.slots;
            const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
            const __VLS_153 = {}.ElTag;
            /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
            // @ts-ignore
            const __VLS_154 = __VLS_asFunctionalComponent(__VLS_153, new __VLS_153({
                type: (row.enabled ? 'success' : 'info'),
                size: "small",
            }));
            const __VLS_155 = __VLS_154({
                type: (row.enabled ? 'success' : 'info'),
                size: "small",
            }, ...__VLS_functionalComponentArgsRest(__VLS_154));
            __VLS_156.slots.default;
            (row.enabled ? '启用' : '停用');
            var __VLS_156;
        }
        var __VLS_152;
    }
    const __VLS_157 = {}.ElTableColumn;
    /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
    // @ts-ignore
    const __VLS_158 = __VLS_asFunctionalComponent(__VLS_157, new __VLS_157({
        label: "系统项",
        width: "90",
    }));
    const __VLS_159 = __VLS_158({
        label: "系统项",
        width: "90",
    }, ...__VLS_functionalComponentArgsRest(__VLS_158));
    __VLS_160.slots.default;
    {
        const { default: __VLS_thisSlot } = __VLS_160.slots;
        const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
        const __VLS_161 = {}.ElTag;
        /** @type {[typeof __VLS_components.ElTag, typeof __VLS_components.elTag, typeof __VLS_components.ElTag, typeof __VLS_components.elTag, ]} */ ;
        // @ts-ignore
        const __VLS_162 = __VLS_asFunctionalComponent(__VLS_161, new __VLS_161({
            size: "small",
            type: (row.isSystem ? 'warning' : 'info'),
        }));
        const __VLS_163 = __VLS_162({
            size: "small",
            type: (row.isSystem ? 'warning' : 'info'),
        }, ...__VLS_functionalComponentArgsRest(__VLS_162));
        __VLS_164.slots.default;
        (row.isSystem ? '是' : '否');
        var __VLS_164;
    }
    var __VLS_160;
    if (__VLS_ctx.canEditItem || __VLS_ctx.canRemoveItem) {
        const __VLS_165 = {}.ElTableColumn;
        /** @type {[typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, typeof __VLS_components.ElTableColumn, typeof __VLS_components.elTableColumn, ]} */ ;
        // @ts-ignore
        const __VLS_166 = __VLS_asFunctionalComponent(__VLS_165, new __VLS_165({
            label: "操作",
            width: "150",
            fixed: "right",
        }));
        const __VLS_167 = __VLS_166({
            label: "操作",
            width: "150",
            fixed: "right",
        }, ...__VLS_functionalComponentArgsRest(__VLS_166));
        __VLS_168.slots.default;
        {
            const { default: __VLS_thisSlot } = __VLS_168.slots;
            const [{ row }] = __VLS_getSlotParams(__VLS_thisSlot);
            if (__VLS_ctx.canEditItem) {
                const __VLS_169 = {}.ElButton;
                /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
                // @ts-ignore
                const __VLS_170 = __VLS_asFunctionalComponent(__VLS_169, new __VLS_169({
                    ...{ 'onClick': {} },
                    text: true,
                    type: "primary",
                }));
                const __VLS_171 = __VLS_170({
                    ...{ 'onClick': {} },
                    text: true,
                    type: "primary",
                }, ...__VLS_functionalComponentArgsRest(__VLS_170));
                let __VLS_173;
                let __VLS_174;
                let __VLS_175;
                const __VLS_176 = {
                    onClick: (...[$event]) => {
                        if (!!(!__VLS_ctx.canAccessDictionaryPage))
                            return;
                        if (!(__VLS_ctx.canEditItem || __VLS_ctx.canRemoveItem))
                            return;
                        if (!(__VLS_ctx.canEditItem))
                            return;
                        __VLS_ctx.openItemDialog(row);
                    }
                };
                __VLS_172.slots.default;
                var __VLS_172;
            }
            if (__VLS_ctx.canRemoveItem) {
                const __VLS_177 = {}.ElButton;
                /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
                // @ts-ignore
                const __VLS_178 = __VLS_asFunctionalComponent(__VLS_177, new __VLS_177({
                    ...{ 'onClick': {} },
                    text: true,
                    type: "danger",
                    disabled: (row.isSystem),
                }));
                const __VLS_179 = __VLS_178({
                    ...{ 'onClick': {} },
                    text: true,
                    type: "danger",
                    disabled: (row.isSystem),
                }, ...__VLS_functionalComponentArgsRest(__VLS_178));
                let __VLS_181;
                let __VLS_182;
                let __VLS_183;
                const __VLS_184 = {
                    onClick: (...[$event]) => {
                        if (!!(!__VLS_ctx.canAccessDictionaryPage))
                            return;
                        if (!(__VLS_ctx.canEditItem || __VLS_ctx.canRemoveItem))
                            return;
                        if (!(__VLS_ctx.canRemoveItem))
                            return;
                        __VLS_ctx.deleteItem(row);
                    }
                };
                __VLS_180.slots.default;
                var __VLS_180;
            }
        }
        var __VLS_168;
    }
    var __VLS_124;
    const __VLS_185 = {}.ElPagination;
    /** @type {[typeof __VLS_components.ElPagination, typeof __VLS_components.elPagination, ]} */ ;
    // @ts-ignore
    const __VLS_186 = __VLS_asFunctionalComponent(__VLS_185, new __VLS_185({
        ...{ 'onCurrentChange': {} },
        ...{ 'onSizeChange': {} },
        ...{ class: "pager" },
        currentPage: (__VLS_ctx.itemPageNo),
        pageSize: (__VLS_ctx.itemPageSize),
        total: (__VLS_ctx.itemTotal),
        layout: "total, sizes, prev, pager, next",
        pageSizes: ([10, 20, 50]),
    }));
    const __VLS_187 = __VLS_186({
        ...{ 'onCurrentChange': {} },
        ...{ 'onSizeChange': {} },
        ...{ class: "pager" },
        currentPage: (__VLS_ctx.itemPageNo),
        pageSize: (__VLS_ctx.itemPageSize),
        total: (__VLS_ctx.itemTotal),
        layout: "total, sizes, prev, pager, next",
        pageSizes: ([10, 20, 50]),
    }, ...__VLS_functionalComponentArgsRest(__VLS_186));
    let __VLS_189;
    let __VLS_190;
    let __VLS_191;
    const __VLS_192 = {
        onCurrentChange: (__VLS_ctx.loadItems)
    };
    const __VLS_193 = {
        onSizeChange: (__VLS_ctx.handleItemSizeChange)
    };
    var __VLS_188;
    const __VLS_194 = {}.ElDialog;
    /** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
    // @ts-ignore
    const __VLS_195 = __VLS_asFunctionalComponent(__VLS_194, new __VLS_194({
        modelValue: (__VLS_ctx.typeDialogVisible),
        title: (__VLS_ctx.editingType ? '编辑字典类型' : '新增字典类型'),
        width: "520px",
    }));
    const __VLS_196 = __VLS_195({
        modelValue: (__VLS_ctx.typeDialogVisible),
        title: (__VLS_ctx.editingType ? '编辑字典类型' : '新增字典类型'),
        width: "520px",
    }, ...__VLS_functionalComponentArgsRest(__VLS_195));
    __VLS_197.slots.default;
    const __VLS_198 = {}.ElForm;
    /** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
    // @ts-ignore
    const __VLS_199 = __VLS_asFunctionalComponent(__VLS_198, new __VLS_198({
        ref: "typeFormRef",
        model: (__VLS_ctx.typeForm),
        rules: (__VLS_ctx.typeRules),
        labelWidth: "100px",
    }));
    const __VLS_200 = __VLS_199({
        ref: "typeFormRef",
        model: (__VLS_ctx.typeForm),
        rules: (__VLS_ctx.typeRules),
        labelWidth: "100px",
    }, ...__VLS_functionalComponentArgsRest(__VLS_199));
    /** @type {typeof __VLS_ctx.typeFormRef} */ ;
    var __VLS_202 = {};
    __VLS_201.slots.default;
    const __VLS_204 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_205 = __VLS_asFunctionalComponent(__VLS_204, new __VLS_204({
        label: "类型编码",
        prop: "code",
    }));
    const __VLS_206 = __VLS_205({
        label: "类型编码",
        prop: "code",
    }, ...__VLS_functionalComponentArgsRest(__VLS_205));
    __VLS_207.slots.default;
    const __VLS_208 = {}.ElInput;
    /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
    // @ts-ignore
    const __VLS_209 = __VLS_asFunctionalComponent(__VLS_208, new __VLS_208({
        modelValue: (__VLS_ctx.typeForm.code),
        disabled: (!!__VLS_ctx.editingType),
    }));
    const __VLS_210 = __VLS_209({
        modelValue: (__VLS_ctx.typeForm.code),
        disabled: (!!__VLS_ctx.editingType),
    }, ...__VLS_functionalComponentArgsRest(__VLS_209));
    var __VLS_207;
    const __VLS_212 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_213 = __VLS_asFunctionalComponent(__VLS_212, new __VLS_212({
        label: "类型名称",
        prop: "name",
    }));
    const __VLS_214 = __VLS_213({
        label: "类型名称",
        prop: "name",
    }, ...__VLS_functionalComponentArgsRest(__VLS_213));
    __VLS_215.slots.default;
    const __VLS_216 = {}.ElInput;
    /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
    // @ts-ignore
    const __VLS_217 = __VLS_asFunctionalComponent(__VLS_216, new __VLS_216({
        modelValue: (__VLS_ctx.typeForm.name),
    }));
    const __VLS_218 = __VLS_217({
        modelValue: (__VLS_ctx.typeForm.name),
    }, ...__VLS_functionalComponentArgsRest(__VLS_217));
    var __VLS_215;
    const __VLS_220 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_221 = __VLS_asFunctionalComponent(__VLS_220, new __VLS_220({
        label: "排序",
    }));
    const __VLS_222 = __VLS_221({
        label: "排序",
    }, ...__VLS_functionalComponentArgsRest(__VLS_221));
    __VLS_223.slots.default;
    const __VLS_224 = {}.ElInputNumber;
    /** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
    // @ts-ignore
    const __VLS_225 = __VLS_asFunctionalComponent(__VLS_224, new __VLS_224({
        modelValue: (__VLS_ctx.typeForm.sortOrder),
        min: (0),
    }));
    const __VLS_226 = __VLS_225({
        modelValue: (__VLS_ctx.typeForm.sortOrder),
        min: (0),
    }, ...__VLS_functionalComponentArgsRest(__VLS_225));
    var __VLS_223;
    const __VLS_228 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_229 = __VLS_asFunctionalComponent(__VLS_228, new __VLS_228({
        label: "启用状态",
    }));
    const __VLS_230 = __VLS_229({
        label: "启用状态",
    }, ...__VLS_functionalComponentArgsRest(__VLS_229));
    __VLS_231.slots.default;
    const __VLS_232 = {}.ElSwitch;
    /** @type {[typeof __VLS_components.ElSwitch, typeof __VLS_components.elSwitch, ]} */ ;
    // @ts-ignore
    const __VLS_233 = __VLS_asFunctionalComponent(__VLS_232, new __VLS_232({
        modelValue: (__VLS_ctx.typeForm.enabled),
    }));
    const __VLS_234 = __VLS_233({
        modelValue: (__VLS_ctx.typeForm.enabled),
    }, ...__VLS_functionalComponentArgsRest(__VLS_233));
    var __VLS_231;
    const __VLS_236 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_237 = __VLS_asFunctionalComponent(__VLS_236, new __VLS_236({
        label: "描述",
    }));
    const __VLS_238 = __VLS_237({
        label: "描述",
    }, ...__VLS_functionalComponentArgsRest(__VLS_237));
    __VLS_239.slots.default;
    const __VLS_240 = {}.ElInput;
    /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
    // @ts-ignore
    const __VLS_241 = __VLS_asFunctionalComponent(__VLS_240, new __VLS_240({
        modelValue: (__VLS_ctx.typeForm.description),
        type: "textarea",
        rows: (3),
    }));
    const __VLS_242 = __VLS_241({
        modelValue: (__VLS_ctx.typeForm.description),
        type: "textarea",
        rows: (3),
    }, ...__VLS_functionalComponentArgsRest(__VLS_241));
    var __VLS_239;
    var __VLS_201;
    {
        const { footer: __VLS_thisSlot } = __VLS_197.slots;
        const __VLS_244 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_245 = __VLS_asFunctionalComponent(__VLS_244, new __VLS_244({
            ...{ 'onClick': {} },
        }));
        const __VLS_246 = __VLS_245({
            ...{ 'onClick': {} },
        }, ...__VLS_functionalComponentArgsRest(__VLS_245));
        let __VLS_248;
        let __VLS_249;
        let __VLS_250;
        const __VLS_251 = {
            onClick: (...[$event]) => {
                if (!!(!__VLS_ctx.canAccessDictionaryPage))
                    return;
                __VLS_ctx.typeDialogVisible = false;
            }
        };
        __VLS_247.slots.default;
        var __VLS_247;
        const __VLS_252 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_253 = __VLS_asFunctionalComponent(__VLS_252, new __VLS_252({
            ...{ 'onClick': {} },
            type: "primary",
        }));
        const __VLS_254 = __VLS_253({
            ...{ 'onClick': {} },
            type: "primary",
        }, ...__VLS_functionalComponentArgsRest(__VLS_253));
        let __VLS_256;
        let __VLS_257;
        let __VLS_258;
        const __VLS_259 = {
            onClick: (__VLS_ctx.saveType)
        };
        __VLS_255.slots.default;
        var __VLS_255;
    }
    var __VLS_197;
    const __VLS_260 = {}.ElDialog;
    /** @type {[typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, typeof __VLS_components.ElDialog, typeof __VLS_components.elDialog, ]} */ ;
    // @ts-ignore
    const __VLS_261 = __VLS_asFunctionalComponent(__VLS_260, new __VLS_260({
        modelValue: (__VLS_ctx.itemDialogVisible),
        title: (__VLS_ctx.editingItem ? '编辑字典项' : '新增字典项'),
        width: "560px",
    }));
    const __VLS_262 = __VLS_261({
        modelValue: (__VLS_ctx.itemDialogVisible),
        title: (__VLS_ctx.editingItem ? '编辑字典项' : '新增字典项'),
        width: "560px",
    }, ...__VLS_functionalComponentArgsRest(__VLS_261));
    __VLS_263.slots.default;
    const __VLS_264 = {}.ElForm;
    /** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
    // @ts-ignore
    const __VLS_265 = __VLS_asFunctionalComponent(__VLS_264, new __VLS_264({
        ref: "itemFormRef",
        model: (__VLS_ctx.itemForm),
        rules: (__VLS_ctx.itemRules),
        labelWidth: "100px",
    }));
    const __VLS_266 = __VLS_265({
        ref: "itemFormRef",
        model: (__VLS_ctx.itemForm),
        rules: (__VLS_ctx.itemRules),
        labelWidth: "100px",
    }, ...__VLS_functionalComponentArgsRest(__VLS_265));
    /** @type {typeof __VLS_ctx.itemFormRef} */ ;
    var __VLS_268 = {};
    __VLS_267.slots.default;
    const __VLS_270 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_271 = __VLS_asFunctionalComponent(__VLS_270, new __VLS_270({
        label: "项编码",
        prop: "itemCode",
    }));
    const __VLS_272 = __VLS_271({
        label: "项编码",
        prop: "itemCode",
    }, ...__VLS_functionalComponentArgsRest(__VLS_271));
    __VLS_273.slots.default;
    const __VLS_274 = {}.ElInput;
    /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
    // @ts-ignore
    const __VLS_275 = __VLS_asFunctionalComponent(__VLS_274, new __VLS_274({
        modelValue: (__VLS_ctx.itemForm.itemCode),
        disabled: (!!__VLS_ctx.editingItem),
    }));
    const __VLS_276 = __VLS_275({
        modelValue: (__VLS_ctx.itemForm.itemCode),
        disabled: (!!__VLS_ctx.editingItem),
    }, ...__VLS_functionalComponentArgsRest(__VLS_275));
    var __VLS_273;
    const __VLS_278 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_279 = __VLS_asFunctionalComponent(__VLS_278, new __VLS_278({
        label: "项名称",
        prop: "itemName",
    }));
    const __VLS_280 = __VLS_279({
        label: "项名称",
        prop: "itemName",
    }, ...__VLS_functionalComponentArgsRest(__VLS_279));
    __VLS_281.slots.default;
    const __VLS_282 = {}.ElInput;
    /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
    // @ts-ignore
    const __VLS_283 = __VLS_asFunctionalComponent(__VLS_282, new __VLS_282({
        modelValue: (__VLS_ctx.itemForm.itemName),
    }));
    const __VLS_284 = __VLS_283({
        modelValue: (__VLS_ctx.itemForm.itemName),
    }, ...__VLS_functionalComponentArgsRest(__VLS_283));
    var __VLS_281;
    const __VLS_286 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_287 = __VLS_asFunctionalComponent(__VLS_286, new __VLS_286({
        label: "项值",
        prop: "itemValue",
    }));
    const __VLS_288 = __VLS_287({
        label: "项值",
        prop: "itemValue",
    }, ...__VLS_functionalComponentArgsRest(__VLS_287));
    __VLS_289.slots.default;
    const __VLS_290 = {}.ElInput;
    /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
    // @ts-ignore
    const __VLS_291 = __VLS_asFunctionalComponent(__VLS_290, new __VLS_290({
        modelValue: (__VLS_ctx.itemForm.itemValue),
    }));
    const __VLS_292 = __VLS_291({
        modelValue: (__VLS_ctx.itemForm.itemValue),
    }, ...__VLS_functionalComponentArgsRest(__VLS_291));
    var __VLS_289;
    const __VLS_294 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_295 = __VLS_asFunctionalComponent(__VLS_294, new __VLS_294({
        label: "排序",
    }));
    const __VLS_296 = __VLS_295({
        label: "排序",
    }, ...__VLS_functionalComponentArgsRest(__VLS_295));
    __VLS_297.slots.default;
    const __VLS_298 = {}.ElInputNumber;
    /** @type {[typeof __VLS_components.ElInputNumber, typeof __VLS_components.elInputNumber, ]} */ ;
    // @ts-ignore
    const __VLS_299 = __VLS_asFunctionalComponent(__VLS_298, new __VLS_298({
        modelValue: (__VLS_ctx.itemForm.sortOrder),
        min: (0),
    }));
    const __VLS_300 = __VLS_299({
        modelValue: (__VLS_ctx.itemForm.sortOrder),
        min: (0),
    }, ...__VLS_functionalComponentArgsRest(__VLS_299));
    var __VLS_297;
    const __VLS_302 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_303 = __VLS_asFunctionalComponent(__VLS_302, new __VLS_302({
        label: "启用状态",
    }));
    const __VLS_304 = __VLS_303({
        label: "启用状态",
    }, ...__VLS_functionalComponentArgsRest(__VLS_303));
    __VLS_305.slots.default;
    const __VLS_306 = {}.ElSwitch;
    /** @type {[typeof __VLS_components.ElSwitch, typeof __VLS_components.elSwitch, ]} */ ;
    // @ts-ignore
    const __VLS_307 = __VLS_asFunctionalComponent(__VLS_306, new __VLS_306({
        modelValue: (__VLS_ctx.itemForm.enabled),
    }));
    const __VLS_308 = __VLS_307({
        modelValue: (__VLS_ctx.itemForm.enabled),
    }, ...__VLS_functionalComponentArgsRest(__VLS_307));
    var __VLS_305;
    const __VLS_310 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_311 = __VLS_asFunctionalComponent(__VLS_310, new __VLS_310({
        label: "系统项",
    }));
    const __VLS_312 = __VLS_311({
        label: "系统项",
    }, ...__VLS_functionalComponentArgsRest(__VLS_311));
    __VLS_313.slots.default;
    const __VLS_314 = {}.ElSwitch;
    /** @type {[typeof __VLS_components.ElSwitch, typeof __VLS_components.elSwitch, ]} */ ;
    // @ts-ignore
    const __VLS_315 = __VLS_asFunctionalComponent(__VLS_314, new __VLS_314({
        modelValue: (__VLS_ctx.itemForm.isSystem),
        disabled: (!!__VLS_ctx.editingItem && __VLS_ctx.editingItem.isSystem),
    }));
    const __VLS_316 = __VLS_315({
        modelValue: (__VLS_ctx.itemForm.isSystem),
        disabled: (!!__VLS_ctx.editingItem && __VLS_ctx.editingItem.isSystem),
    }, ...__VLS_functionalComponentArgsRest(__VLS_315));
    var __VLS_313;
    const __VLS_318 = {}.ElFormItem;
    /** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
    // @ts-ignore
    const __VLS_319 = __VLS_asFunctionalComponent(__VLS_318, new __VLS_318({
        label: "描述",
    }));
    const __VLS_320 = __VLS_319({
        label: "描述",
    }, ...__VLS_functionalComponentArgsRest(__VLS_319));
    __VLS_321.slots.default;
    const __VLS_322 = {}.ElInput;
    /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
    // @ts-ignore
    const __VLS_323 = __VLS_asFunctionalComponent(__VLS_322, new __VLS_322({
        modelValue: (__VLS_ctx.itemForm.description),
        type: "textarea",
        rows: (3),
    }));
    const __VLS_324 = __VLS_323({
        modelValue: (__VLS_ctx.itemForm.description),
        type: "textarea",
        rows: (3),
    }, ...__VLS_functionalComponentArgsRest(__VLS_323));
    var __VLS_321;
    var __VLS_267;
    {
        const { footer: __VLS_thisSlot } = __VLS_263.slots;
        const __VLS_326 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_327 = __VLS_asFunctionalComponent(__VLS_326, new __VLS_326({
            ...{ 'onClick': {} },
        }));
        const __VLS_328 = __VLS_327({
            ...{ 'onClick': {} },
        }, ...__VLS_functionalComponentArgsRest(__VLS_327));
        let __VLS_330;
        let __VLS_331;
        let __VLS_332;
        const __VLS_333 = {
            onClick: (...[$event]) => {
                if (!!(!__VLS_ctx.canAccessDictionaryPage))
                    return;
                __VLS_ctx.itemDialogVisible = false;
            }
        };
        __VLS_329.slots.default;
        var __VLS_329;
        const __VLS_334 = {}.ElButton;
        /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
        // @ts-ignore
        const __VLS_335 = __VLS_asFunctionalComponent(__VLS_334, new __VLS_334({
            ...{ 'onClick': {} },
            type: "primary",
        }));
        const __VLS_336 = __VLS_335({
            ...{ 'onClick': {} },
            type: "primary",
        }, ...__VLS_functionalComponentArgsRest(__VLS_335));
        let __VLS_338;
        let __VLS_339;
        let __VLS_340;
        const __VLS_341 = {
            onClick: (__VLS_ctx.saveItem)
        };
        __VLS_337.slots.default;
        var __VLS_337;
    }
    var __VLS_263;
}
/** @type {__VLS_StyleScopedClasses['dictionary-page']} */ ;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
/** @type {__VLS_StyleScopedClasses['content-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['left-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-header']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-title']} */ ;
/** @type {__VLS_StyleScopedClasses['pager']} */ ;
/** @type {__VLS_StyleScopedClasses['right-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-header']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-title']} */ ;
/** @type {__VLS_StyleScopedClasses['item-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['pager']} */ ;
// @ts-ignore
var __VLS_203 = __VLS_202, __VLS_269 = __VLS_268;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            Plus: Plus,
            canListTypes: canListTypes,
            canListItems: canListItems,
            canAccessDictionaryPage: canAccessDictionaryPage,
            canAddType: canAddType,
            canEditType: canEditType,
            canRemoveType: canRemoveType,
            canAddItem: canAddItem,
            canEditItem: canEditItem,
            canRemoveItem: canRemoveItem,
            typeLoading: typeLoading,
            itemLoading: itemLoading,
            typeList: typeList,
            itemList: itemList,
            selectedType: selectedType,
            typeKeyword: typeKeyword,
            itemKeyword: itemKeyword,
            typePageNo: typePageNo,
            typePageSize: typePageSize,
            typeTotal: typeTotal,
            itemPageNo: itemPageNo,
            itemPageSize: itemPageSize,
            itemTotal: itemTotal,
            typeDialogVisible: typeDialogVisible,
            itemDialogVisible: itemDialogVisible,
            editingType: editingType,
            editingItem: editingItem,
            typeFormRef: typeFormRef,
            itemFormRef: itemFormRef,
            typeForm: typeForm,
            itemForm: itemForm,
            typeRules: typeRules,
            itemRules: itemRules,
            loadTypes: loadTypes,
            handleTypeSelect: handleTypeSelect,
            loadItems: loadItems,
            handleTypeSizeChange: handleTypeSizeChange,
            handleItemSizeChange: handleItemSizeChange,
            openTypeDialog: openTypeDialog,
            saveType: saveType,
            deleteType: deleteType,
            openItemDialog: openItemDialog,
            saveItem: saveItem,
            toggleItem: toggleItem,
            deleteItem: deleteItem,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
