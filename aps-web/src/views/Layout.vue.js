/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { ref, computed, watch, onMounted, onUnmounted, markRaw } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { HomeFilled, Document, Calendar, User, UserFilled, Expand, Fold, Search, Close, ArrowDown, Setting, SetUp, Cpu, Lock, Collection } from '@element-plus/icons-vue';
import { useAuthStore } from '../stores/auth';
import { useTagsStore } from '../stores/tags';
import UserMenu from '../components/UserMenu.vue';
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const tagsStore = useTagsStore();
const isCollapsed = ref(false);
const isMobile = ref(false);
const menuSearchQuery = ref('');
// 图标映射
const iconMap = {
    HomeFilled: markRaw(HomeFilled),
    Document: markRaw(Document),
    Calendar: markRaw(Calendar),
    User: markRaw(User),
    UserFilled: markRaw(UserFilled),
    Lock: markRaw(Lock),
    Setting: markRaw(Setting),
    SetUp: markRaw(SetUp),
    Cpu: markRaw(Cpu),
    Collection: markRaw(Collection),
};
// 从路由构建菜单数据
const layoutRoute = computed(() => {
    return router.options.routes.find(r => r.path === '/');
});
const allMenuItems = computed(() => {
    if (!layoutRoute.value?.children)
        return [];
    return layoutRoute.value.children
        .filter(r => r.meta?.title)
        .map(r => ({
        path: '/' + r.path,
        title: r.meta.title,
        icon: r.meta.icon || '',
        group: r.meta.group || '',
        groupIcon: r.meta.groupIcon || '',
        roles: r.meta.roles || [],
    }));
});
// 根据权限过滤菜单项
const accessibleMenuItems = computed(() => {
    return allMenuItems.value.filter(item => {
        if (!item.roles || item.roles.length === 0)
            return true;
        return item.roles.some(role => authStore.hasRole(role));
    });
});
// 搜索过滤
const searchFilteredItems = computed(() => {
    if (!menuSearchQuery.value.trim())
        return accessibleMenuItems.value;
    const query = menuSearchQuery.value.toLowerCase();
    return accessibleMenuItems.value.filter(item => item.title.toLowerCase().includes(query) ||
        item.group.toLowerCase().includes(query));
});
// 无分组菜单
const filteredUngroupedMenus = computed(() => {
    return searchFilteredItems.value.filter(item => !item.group);
});
// 分组菜单
const filteredGroupedMenus = computed(() => {
    const groups = {};
    searchFilteredItems.value
        .filter(item => item.group)
        .forEach(item => {
        if (!groups[item.group]) {
            groups[item.group] = {
                name: item.group,
                icon: item.groupIcon,
                children: []
            };
        }
        groups[item.group].children.push({
            path: item.path,
            title: item.title,
            icon: item.icon,
            roles: item.roles
        });
    });
    return Object.values(groups);
});
// 默认展开的分组
const defaultOpeneds = computed(() => {
    return filteredGroupedMenus.value.map(g => g.name);
});
const activeMenu = computed(() => route.path);
const sidebarWidth = computed(() => isCollapsed.value ? '64px' : '220px');
// 面包屑信息
const currentTitle = computed(() => route.meta?.title || '');
const currentGroup = computed(() => route.meta?.group || '');
// 监听路由变化，添加标签
watch(() => route.path, () => {
    if (route.meta?.title) {
        tagsStore.addTag(route);
    }
}, { immediate: true });
// 标签页操作
function handleTagClick(tag) {
    if (tag.path !== route.path) {
        router.push(tag.path);
    }
}
function handleTagClose(path) {
    const redirectPath = tagsStore.removeTag(path);
    if (redirectPath) {
        router.push(redirectPath);
    }
}
function openTagContextMenu(_event, _tag) {
    // 可扩展右键菜单
}
function handleTagsCommand(command) {
    if (command === 'closeOther') {
        tagsStore.removeOtherTags(route.path);
    }
    else if (command === 'closeAll') {
        const redirectPath = tagsStore.removeAllTags();
        if (route.path !== redirectPath) {
            router.push(redirectPath);
        }
    }
}
const toggleSidebar = () => {
    isCollapsed.value = !isCollapsed.value;
};
const checkMobile = () => {
    isMobile.value = window.innerWidth < 768;
    if (isMobile.value) {
        isCollapsed.value = true;
    }
};
onMounted(() => {
    checkMobile();
    window.addEventListener('resize', checkMobile);
});
onUnmounted(() => {
    window.removeEventListener('resize', checkMobile);
});
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['menu-toggle']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-aside']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-search-input']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-search-input']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['el-menu-item']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['el-menu-item']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['el-menu-item']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['el-menu-item']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['el-sub-menu__title']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['el-menu-item']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-breadcrumb']} */ ;
/** @type {__VLS_StyleScopedClasses['tags-scrollbar']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-item']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-item']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-close']} */ ;
/** @type {__VLS_StyleScopedClasses['tags-action-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-header']} */ ;
/** @type {__VLS_StyleScopedClasses['logo-text']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-main']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-header']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-aside']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['el-menu-item']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-toggle']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-item']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-close']} */ ;
/** @type {__VLS_StyleScopedClasses['fade-enter-active']} */ ;
/** @type {__VLS_StyleScopedClasses['fade-leave-active']} */ ;
// CSS variable injection 
// CSS variable injection end 
const __VLS_0 = {}.ElContainer;
/** @type {[typeof __VLS_components.ElContainer, typeof __VLS_components.elContainer, typeof __VLS_components.ElContainer, typeof __VLS_components.elContainer, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    ...{ class: "layout-container" },
}));
const __VLS_2 = __VLS_1({
    ...{ class: "layout-container" },
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
var __VLS_4 = {};
__VLS_3.slots.default;
const __VLS_5 = {}.ElHeader;
/** @type {[typeof __VLS_components.ElHeader, typeof __VLS_components.elHeader, typeof __VLS_components.ElHeader, typeof __VLS_components.elHeader, ]} */ ;
// @ts-ignore
const __VLS_6 = __VLS_asFunctionalComponent(__VLS_5, new __VLS_5({
    ...{ class: "layout-header" },
}));
const __VLS_7 = __VLS_6({
    ...{ class: "layout-header" },
}, ...__VLS_functionalComponentArgsRest(__VLS_6));
__VLS_8.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "header-left" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.router.push('/dashboard');
        } },
    ...{ class: "logo-area" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "logo-icon" },
});
const __VLS_9 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_10 = __VLS_asFunctionalComponent(__VLS_9, new __VLS_9({
    size: (22),
    color: "#fff",
}));
const __VLS_11 = __VLS_10({
    size: (22),
    color: "#fff",
}, ...__VLS_functionalComponentArgsRest(__VLS_10));
__VLS_12.slots.default;
const __VLS_13 = {}.Cpu;
/** @type {[typeof __VLS_components.Cpu, ]} */ ;
// @ts-ignore
const __VLS_14 = __VLS_asFunctionalComponent(__VLS_13, new __VLS_13({}));
const __VLS_15 = __VLS_14({}, ...__VLS_functionalComponentArgsRest(__VLS_14));
var __VLS_12;
if (!__VLS_ctx.isCollapsed) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "logo-text" },
    });
}
const __VLS_17 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_18 = __VLS_asFunctionalComponent(__VLS_17, new __VLS_17({
    ...{ 'onClick': {} },
    ...{ class: "menu-toggle" },
    icon: (__VLS_ctx.isCollapsed ? __VLS_ctx.Expand : __VLS_ctx.Fold),
    'aria-label': "切换侧边栏",
    text: true,
}));
const __VLS_19 = __VLS_18({
    ...{ 'onClick': {} },
    ...{ class: "menu-toggle" },
    icon: (__VLS_ctx.isCollapsed ? __VLS_ctx.Expand : __VLS_ctx.Fold),
    'aria-label': "切换侧边栏",
    text: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_18));
let __VLS_21;
let __VLS_22;
let __VLS_23;
const __VLS_24 = {
    onClick: (__VLS_ctx.toggleSidebar)
};
var __VLS_20;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "header-right" },
});
/** @type {[typeof UserMenu, ]} */ ;
// @ts-ignore
const __VLS_25 = __VLS_asFunctionalComponent(UserMenu, new UserMenu({}));
const __VLS_26 = __VLS_25({}, ...__VLS_functionalComponentArgsRest(__VLS_25));
var __VLS_8;
const __VLS_28 = {}.ElContainer;
/** @type {[typeof __VLS_components.ElContainer, typeof __VLS_components.elContainer, typeof __VLS_components.ElContainer, typeof __VLS_components.elContainer, ]} */ ;
// @ts-ignore
const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({}));
const __VLS_30 = __VLS_29({}, ...__VLS_functionalComponentArgsRest(__VLS_29));
__VLS_31.slots.default;
const __VLS_32 = {}.ElAside;
/** @type {[typeof __VLS_components.ElAside, typeof __VLS_components.elAside, typeof __VLS_components.ElAside, typeof __VLS_components.elAside, ]} */ ;
// @ts-ignore
const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({
    width: (__VLS_ctx.sidebarWidth),
    ...{ class: "layout-aside" },
    ...{ class: ({ collapsed: __VLS_ctx.isCollapsed }) },
}));
const __VLS_34 = __VLS_33({
    width: (__VLS_ctx.sidebarWidth),
    ...{ class: "layout-aside" },
    ...{ class: ({ collapsed: __VLS_ctx.isCollapsed }) },
}, ...__VLS_functionalComponentArgsRest(__VLS_33));
__VLS_35.slots.default;
if (!__VLS_ctx.isCollapsed) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "sidebar-search" },
    });
    const __VLS_36 = {}.ElInput;
    /** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
    // @ts-ignore
    const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({
        modelValue: (__VLS_ctx.menuSearchQuery),
        placeholder: "搜索菜单...",
        prefixIcon: (__VLS_ctx.Search),
        clearable: true,
        size: "small",
        ...{ class: "menu-search-input" },
    }));
    const __VLS_38 = __VLS_37({
        modelValue: (__VLS_ctx.menuSearchQuery),
        placeholder: "搜索菜单...",
        prefixIcon: (__VLS_ctx.Search),
        clearable: true,
        size: "small",
        ...{ class: "menu-search-input" },
    }, ...__VLS_functionalComponentArgsRest(__VLS_37));
}
const __VLS_40 = {}.ElScrollbar;
/** @type {[typeof __VLS_components.ElScrollbar, typeof __VLS_components.elScrollbar, typeof __VLS_components.ElScrollbar, typeof __VLS_components.elScrollbar, ]} */ ;
// @ts-ignore
const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
    ...{ class: "menu-scrollbar" },
}));
const __VLS_42 = __VLS_41({
    ...{ class: "menu-scrollbar" },
}, ...__VLS_functionalComponentArgsRest(__VLS_41));
__VLS_43.slots.default;
const __VLS_44 = {}.ElMenu;
/** @type {[typeof __VLS_components.ElMenu, typeof __VLS_components.elMenu, typeof __VLS_components.ElMenu, typeof __VLS_components.elMenu, ]} */ ;
// @ts-ignore
const __VLS_45 = __VLS_asFunctionalComponent(__VLS_44, new __VLS_44({
    defaultActive: (__VLS_ctx.activeMenu),
    defaultOpeneds: (__VLS_ctx.defaultOpeneds),
    router: true,
    ...{ class: "layout-menu" },
    collapse: (__VLS_ctx.isCollapsed),
    collapseTransition: (true),
    uniqueOpened: (false),
}));
const __VLS_46 = __VLS_45({
    defaultActive: (__VLS_ctx.activeMenu),
    defaultOpeneds: (__VLS_ctx.defaultOpeneds),
    router: true,
    ...{ class: "layout-menu" },
    collapse: (__VLS_ctx.isCollapsed),
    collapseTransition: (true),
    uniqueOpened: (false),
}, ...__VLS_functionalComponentArgsRest(__VLS_45));
__VLS_47.slots.default;
for (const [item] of __VLS_getVForSourceType((__VLS_ctx.filteredUngroupedMenus))) {
    const __VLS_48 = {}.ElMenuItem;
    /** @type {[typeof __VLS_components.ElMenuItem, typeof __VLS_components.elMenuItem, typeof __VLS_components.ElMenuItem, typeof __VLS_components.elMenuItem, ]} */ ;
    // @ts-ignore
    const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({
        index: (item.path),
        'aria-label': (item.title),
    }));
    const __VLS_50 = __VLS_49({
        index: (item.path),
        'aria-label': (item.title),
    }, ...__VLS_functionalComponentArgsRest(__VLS_49));
    __VLS_51.slots.default;
    const __VLS_52 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_53 = __VLS_asFunctionalComponent(__VLS_52, new __VLS_52({}));
    const __VLS_54 = __VLS_53({}, ...__VLS_functionalComponentArgsRest(__VLS_53));
    __VLS_55.slots.default;
    const __VLS_56 = ((__VLS_ctx.iconMap[item.icon]));
    // @ts-ignore
    const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({}));
    const __VLS_58 = __VLS_57({}, ...__VLS_functionalComponentArgsRest(__VLS_57));
    var __VLS_55;
    {
        const { title: __VLS_thisSlot } = __VLS_51.slots;
        (item.title);
    }
    var __VLS_51;
}
for (const [group] of __VLS_getVForSourceType((__VLS_ctx.filteredGroupedMenus))) {
    (group.name);
    if (!__VLS_ctx.isCollapsed) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "menu-group-label" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        (group.name);
    }
    if (__VLS_ctx.isCollapsed) {
        const __VLS_60 = {}.ElSubMenu;
        /** @type {[typeof __VLS_components.ElSubMenu, typeof __VLS_components.elSubMenu, typeof __VLS_components.ElSubMenu, typeof __VLS_components.elSubMenu, ]} */ ;
        // @ts-ignore
        const __VLS_61 = __VLS_asFunctionalComponent(__VLS_60, new __VLS_60({
            index: (group.name),
        }));
        const __VLS_62 = __VLS_61({
            index: (group.name),
        }, ...__VLS_functionalComponentArgsRest(__VLS_61));
        __VLS_63.slots.default;
        {
            const { title: __VLS_thisSlot } = __VLS_63.slots;
            const __VLS_64 = {}.ElIcon;
            /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
            // @ts-ignore
            const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({}));
            const __VLS_66 = __VLS_65({}, ...__VLS_functionalComponentArgsRest(__VLS_65));
            __VLS_67.slots.default;
            const __VLS_68 = ((__VLS_ctx.iconMap[group.icon]));
            // @ts-ignore
            const __VLS_69 = __VLS_asFunctionalComponent(__VLS_68, new __VLS_68({}));
            const __VLS_70 = __VLS_69({}, ...__VLS_functionalComponentArgsRest(__VLS_69));
            var __VLS_67;
            __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
            (group.name);
        }
        for (const [item] of __VLS_getVForSourceType((group.children))) {
            const __VLS_72 = {}.ElMenuItem;
            /** @type {[typeof __VLS_components.ElMenuItem, typeof __VLS_components.elMenuItem, typeof __VLS_components.ElMenuItem, typeof __VLS_components.elMenuItem, ]} */ ;
            // @ts-ignore
            const __VLS_73 = __VLS_asFunctionalComponent(__VLS_72, new __VLS_72({
                key: (item.path),
                index: (item.path),
                'aria-label': (item.title),
            }));
            const __VLS_74 = __VLS_73({
                key: (item.path),
                index: (item.path),
                'aria-label': (item.title),
            }, ...__VLS_functionalComponentArgsRest(__VLS_73));
            __VLS_75.slots.default;
            const __VLS_76 = {}.ElIcon;
            /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
            // @ts-ignore
            const __VLS_77 = __VLS_asFunctionalComponent(__VLS_76, new __VLS_76({}));
            const __VLS_78 = __VLS_77({}, ...__VLS_functionalComponentArgsRest(__VLS_77));
            __VLS_79.slots.default;
            const __VLS_80 = ((__VLS_ctx.iconMap[item.icon]));
            // @ts-ignore
            const __VLS_81 = __VLS_asFunctionalComponent(__VLS_80, new __VLS_80({}));
            const __VLS_82 = __VLS_81({}, ...__VLS_functionalComponentArgsRest(__VLS_81));
            var __VLS_79;
            {
                const { title: __VLS_thisSlot } = __VLS_75.slots;
                (item.title);
            }
            var __VLS_75;
        }
        var __VLS_63;
    }
    else {
        for (const [item] of __VLS_getVForSourceType((group.children))) {
            const __VLS_84 = {}.ElMenuItem;
            /** @type {[typeof __VLS_components.ElMenuItem, typeof __VLS_components.elMenuItem, typeof __VLS_components.ElMenuItem, typeof __VLS_components.elMenuItem, ]} */ ;
            // @ts-ignore
            const __VLS_85 = __VLS_asFunctionalComponent(__VLS_84, new __VLS_84({
                key: (item.path),
                index: (item.path),
                'aria-label': (item.title),
            }));
            const __VLS_86 = __VLS_85({
                key: (item.path),
                index: (item.path),
                'aria-label': (item.title),
            }, ...__VLS_functionalComponentArgsRest(__VLS_85));
            __VLS_87.slots.default;
            const __VLS_88 = {}.ElIcon;
            /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
            // @ts-ignore
            const __VLS_89 = __VLS_asFunctionalComponent(__VLS_88, new __VLS_88({}));
            const __VLS_90 = __VLS_89({}, ...__VLS_functionalComponentArgsRest(__VLS_89));
            __VLS_91.slots.default;
            const __VLS_92 = ((__VLS_ctx.iconMap[item.icon]));
            // @ts-ignore
            const __VLS_93 = __VLS_asFunctionalComponent(__VLS_92, new __VLS_92({}));
            const __VLS_94 = __VLS_93({}, ...__VLS_functionalComponentArgsRest(__VLS_93));
            var __VLS_91;
            {
                const { title: __VLS_thisSlot } = __VLS_87.slots;
                (item.title);
            }
            var __VLS_87;
        }
    }
}
var __VLS_47;
var __VLS_43;
if (!__VLS_ctx.isCollapsed) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "sidebar-footer" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "version-text" },
    });
}
var __VLS_35;
const __VLS_96 = {}.ElContainer;
/** @type {[typeof __VLS_components.ElContainer, typeof __VLS_components.elContainer, typeof __VLS_components.ElContainer, typeof __VLS_components.elContainer, ]} */ ;
// @ts-ignore
const __VLS_97 = __VLS_asFunctionalComponent(__VLS_96, new __VLS_96({
    ...{ class: "main-container" },
    direction: "vertical",
}));
const __VLS_98 = __VLS_97({
    ...{ class: "main-container" },
    direction: "vertical",
}, ...__VLS_functionalComponentArgsRest(__VLS_97));
__VLS_99.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "nav-header" },
});
const __VLS_100 = {}.ElBreadcrumb;
/** @type {[typeof __VLS_components.ElBreadcrumb, typeof __VLS_components.elBreadcrumb, typeof __VLS_components.ElBreadcrumb, typeof __VLS_components.elBreadcrumb, ]} */ ;
// @ts-ignore
const __VLS_101 = __VLS_asFunctionalComponent(__VLS_100, new __VLS_100({
    separator: ">",
    ...{ class: "nav-breadcrumb" },
}));
const __VLS_102 = __VLS_101({
    separator: ">",
    ...{ class: "nav-breadcrumb" },
}, ...__VLS_functionalComponentArgsRest(__VLS_101));
__VLS_103.slots.default;
const __VLS_104 = {}.ElBreadcrumbItem;
/** @type {[typeof __VLS_components.ElBreadcrumbItem, typeof __VLS_components.elBreadcrumbItem, typeof __VLS_components.ElBreadcrumbItem, typeof __VLS_components.elBreadcrumbItem, ]} */ ;
// @ts-ignore
const __VLS_105 = __VLS_asFunctionalComponent(__VLS_104, new __VLS_104({
    to: ({ path: '/dashboard' }),
}));
const __VLS_106 = __VLS_105({
    to: ({ path: '/dashboard' }),
}, ...__VLS_functionalComponentArgsRest(__VLS_105));
__VLS_107.slots.default;
const __VLS_108 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_109 = __VLS_asFunctionalComponent(__VLS_108, new __VLS_108({
    ...{ style: {} },
}));
const __VLS_110 = __VLS_109({
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_109));
__VLS_111.slots.default;
const __VLS_112 = {}.HomeFilled;
/** @type {[typeof __VLS_components.HomeFilled, ]} */ ;
// @ts-ignore
const __VLS_113 = __VLS_asFunctionalComponent(__VLS_112, new __VLS_112({}));
const __VLS_114 = __VLS_113({}, ...__VLS_functionalComponentArgsRest(__VLS_113));
var __VLS_111;
var __VLS_107;
if (__VLS_ctx.currentGroup) {
    const __VLS_116 = {}.ElBreadcrumbItem;
    /** @type {[typeof __VLS_components.ElBreadcrumbItem, typeof __VLS_components.elBreadcrumbItem, typeof __VLS_components.ElBreadcrumbItem, typeof __VLS_components.elBreadcrumbItem, ]} */ ;
    // @ts-ignore
    const __VLS_117 = __VLS_asFunctionalComponent(__VLS_116, new __VLS_116({}));
    const __VLS_118 = __VLS_117({}, ...__VLS_functionalComponentArgsRest(__VLS_117));
    __VLS_119.slots.default;
    (__VLS_ctx.currentGroup);
    var __VLS_119;
}
if (__VLS_ctx.currentTitle) {
    const __VLS_120 = {}.ElBreadcrumbItem;
    /** @type {[typeof __VLS_components.ElBreadcrumbItem, typeof __VLS_components.elBreadcrumbItem, typeof __VLS_components.ElBreadcrumbItem, typeof __VLS_components.elBreadcrumbItem, ]} */ ;
    // @ts-ignore
    const __VLS_121 = __VLS_asFunctionalComponent(__VLS_120, new __VLS_120({}));
    const __VLS_122 = __VLS_121({}, ...__VLS_functionalComponentArgsRest(__VLS_121));
    __VLS_123.slots.default;
    (__VLS_ctx.currentTitle);
    var __VLS_123;
}
var __VLS_103;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "tags-view" },
});
const __VLS_124 = {}.ElScrollbar;
/** @type {[typeof __VLS_components.ElScrollbar, typeof __VLS_components.elScrollbar, typeof __VLS_components.ElScrollbar, typeof __VLS_components.elScrollbar, ]} */ ;
// @ts-ignore
const __VLS_125 = __VLS_asFunctionalComponent(__VLS_124, new __VLS_124({
    ...{ class: "tags-scrollbar" },
}));
const __VLS_126 = __VLS_125({
    ...{ class: "tags-scrollbar" },
}, ...__VLS_functionalComponentArgsRest(__VLS_125));
__VLS_127.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "tags-wrapper" },
});
for (const [tag] of __VLS_getVForSourceType((__VLS_ctx.tagsStore.visitedTags))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.handleTagClick(tag);
            } },
        ...{ onContextmenu: (...[$event]) => {
                __VLS_ctx.openTagContextMenu($event, tag);
            } },
        key: (tag.path),
        ...{ class: "tag-item" },
        ...{ class: ({ active: tag.path === __VLS_ctx.tagsStore.activeTag }) },
    });
    if (tag.icon) {
        const __VLS_128 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_129 = __VLS_asFunctionalComponent(__VLS_128, new __VLS_128({
            size: (14),
        }));
        const __VLS_130 = __VLS_129({
            size: (14),
        }, ...__VLS_functionalComponentArgsRest(__VLS_129));
        __VLS_131.slots.default;
        const __VLS_132 = ((__VLS_ctx.iconMap[tag.icon]));
        // @ts-ignore
        const __VLS_133 = __VLS_asFunctionalComponent(__VLS_132, new __VLS_132({}));
        const __VLS_134 = __VLS_133({}, ...__VLS_functionalComponentArgsRest(__VLS_133));
        var __VLS_131;
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "tag-title" },
    });
    (tag.title);
    if (tag.closable) {
        const __VLS_136 = {}.ElIcon;
        /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
        // @ts-ignore
        const __VLS_137 = __VLS_asFunctionalComponent(__VLS_136, new __VLS_136({
            ...{ 'onClick': {} },
            ...{ class: "tag-close" },
            size: (12),
        }));
        const __VLS_138 = __VLS_137({
            ...{ 'onClick': {} },
            ...{ class: "tag-close" },
            size: (12),
        }, ...__VLS_functionalComponentArgsRest(__VLS_137));
        let __VLS_140;
        let __VLS_141;
        let __VLS_142;
        const __VLS_143 = {
            onClick: (...[$event]) => {
                if (!(tag.closable))
                    return;
                __VLS_ctx.handleTagClose(tag.path);
            }
        };
        __VLS_139.slots.default;
        const __VLS_144 = {}.Close;
        /** @type {[typeof __VLS_components.Close, ]} */ ;
        // @ts-ignore
        const __VLS_145 = __VLS_asFunctionalComponent(__VLS_144, new __VLS_144({}));
        const __VLS_146 = __VLS_145({}, ...__VLS_functionalComponentArgsRest(__VLS_145));
        var __VLS_139;
    }
}
var __VLS_127;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "tags-actions" },
});
const __VLS_148 = {}.ElDropdown;
/** @type {[typeof __VLS_components.ElDropdown, typeof __VLS_components.elDropdown, typeof __VLS_components.ElDropdown, typeof __VLS_components.elDropdown, ]} */ ;
// @ts-ignore
const __VLS_149 = __VLS_asFunctionalComponent(__VLS_148, new __VLS_148({
    ...{ 'onCommand': {} },
    trigger: "click",
}));
const __VLS_150 = __VLS_149({
    ...{ 'onCommand': {} },
    trigger: "click",
}, ...__VLS_functionalComponentArgsRest(__VLS_149));
let __VLS_152;
let __VLS_153;
let __VLS_154;
const __VLS_155 = {
    onCommand: (__VLS_ctx.handleTagsCommand)
};
__VLS_151.slots.default;
const __VLS_156 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_157 = __VLS_asFunctionalComponent(__VLS_156, new __VLS_156({
    icon: (__VLS_ctx.ArrowDown),
    size: "small",
    text: true,
    ...{ class: "tags-action-btn" },
}));
const __VLS_158 = __VLS_157({
    icon: (__VLS_ctx.ArrowDown),
    size: "small",
    text: true,
    ...{ class: "tags-action-btn" },
}, ...__VLS_functionalComponentArgsRest(__VLS_157));
{
    const { dropdown: __VLS_thisSlot } = __VLS_151.slots;
    const __VLS_160 = {}.ElDropdownMenu;
    /** @type {[typeof __VLS_components.ElDropdownMenu, typeof __VLS_components.elDropdownMenu, typeof __VLS_components.ElDropdownMenu, typeof __VLS_components.elDropdownMenu, ]} */ ;
    // @ts-ignore
    const __VLS_161 = __VLS_asFunctionalComponent(__VLS_160, new __VLS_160({}));
    const __VLS_162 = __VLS_161({}, ...__VLS_functionalComponentArgsRest(__VLS_161));
    __VLS_163.slots.default;
    const __VLS_164 = {}.ElDropdownItem;
    /** @type {[typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, ]} */ ;
    // @ts-ignore
    const __VLS_165 = __VLS_asFunctionalComponent(__VLS_164, new __VLS_164({
        command: "closeOther",
    }));
    const __VLS_166 = __VLS_165({
        command: "closeOther",
    }, ...__VLS_functionalComponentArgsRest(__VLS_165));
    __VLS_167.slots.default;
    var __VLS_167;
    const __VLS_168 = {}.ElDropdownItem;
    /** @type {[typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, ]} */ ;
    // @ts-ignore
    const __VLS_169 = __VLS_asFunctionalComponent(__VLS_168, new __VLS_168({
        command: "closeAll",
    }));
    const __VLS_170 = __VLS_169({
        command: "closeAll",
    }, ...__VLS_functionalComponentArgsRest(__VLS_169));
    __VLS_171.slots.default;
    var __VLS_171;
    var __VLS_163;
}
var __VLS_151;
const __VLS_172 = {}.ElMain;
/** @type {[typeof __VLS_components.ElMain, typeof __VLS_components.elMain, typeof __VLS_components.ElMain, typeof __VLS_components.elMain, ]} */ ;
// @ts-ignore
const __VLS_173 = __VLS_asFunctionalComponent(__VLS_172, new __VLS_172({
    ...{ class: "layout-main" },
}));
const __VLS_174 = __VLS_173({
    ...{ class: "layout-main" },
}, ...__VLS_functionalComponentArgsRest(__VLS_173));
__VLS_175.slots.default;
const __VLS_176 = {}.transition;
/** @type {[typeof __VLS_components.Transition, typeof __VLS_components.transition, typeof __VLS_components.Transition, typeof __VLS_components.transition, ]} */ ;
// @ts-ignore
const __VLS_177 = __VLS_asFunctionalComponent(__VLS_176, new __VLS_176({
    name: "fade",
    mode: "out-in",
}));
const __VLS_178 = __VLS_177({
    name: "fade",
    mode: "out-in",
}, ...__VLS_functionalComponentArgsRest(__VLS_177));
__VLS_179.slots.default;
const __VLS_180 = {}.RouterView;
/** @type {[typeof __VLS_components.RouterView, typeof __VLS_components.routerView, ]} */ ;
// @ts-ignore
const __VLS_181 = __VLS_asFunctionalComponent(__VLS_180, new __VLS_180({}));
const __VLS_182 = __VLS_181({}, ...__VLS_functionalComponentArgsRest(__VLS_181));
var __VLS_179;
var __VLS_175;
var __VLS_99;
var __VLS_31;
var __VLS_3;
/** @type {__VLS_StyleScopedClasses['layout-container']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-header']} */ ;
/** @type {__VLS_StyleScopedClasses['header-left']} */ ;
/** @type {__VLS_StyleScopedClasses['logo-area']} */ ;
/** @type {__VLS_StyleScopedClasses['logo-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['logo-text']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-toggle']} */ ;
/** @type {__VLS_StyleScopedClasses['header-right']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-aside']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar-search']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-search-input']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-scrollbar']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-group-label']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar-footer']} */ ;
/** @type {__VLS_StyleScopedClasses['version-text']} */ ;
/** @type {__VLS_StyleScopedClasses['main-container']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-header']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-breadcrumb']} */ ;
/** @type {__VLS_StyleScopedClasses['tags-view']} */ ;
/** @type {__VLS_StyleScopedClasses['tags-scrollbar']} */ ;
/** @type {__VLS_StyleScopedClasses['tags-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-item']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-title']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-close']} */ ;
/** @type {__VLS_StyleScopedClasses['tags-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['tags-action-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['layout-main']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            HomeFilled: HomeFilled,
            Expand: Expand,
            Fold: Fold,
            Search: Search,
            Close: Close,
            ArrowDown: ArrowDown,
            Cpu: Cpu,
            UserMenu: UserMenu,
            router: router,
            tagsStore: tagsStore,
            isCollapsed: isCollapsed,
            menuSearchQuery: menuSearchQuery,
            iconMap: iconMap,
            filteredUngroupedMenus: filteredUngroupedMenus,
            filteredGroupedMenus: filteredGroupedMenus,
            defaultOpeneds: defaultOpeneds,
            activeMenu: activeMenu,
            sidebarWidth: sidebarWidth,
            currentTitle: currentTitle,
            currentGroup: currentGroup,
            handleTagClick: handleTagClick,
            handleTagClose: handleTagClose,
            openTagContextMenu: openTagContextMenu,
            handleTagsCommand: handleTagsCommand,
            toggleSidebar: toggleSidebar,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
