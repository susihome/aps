/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { Document, Calendar, Setting, User } from '@element-plus/icons-vue';
import { useAuthStore } from '../stores/auth';
const router = useRouter();
const authStore = useAuthStore();
const username = computed(() => authStore.user?.username || '');
const userRole = computed(() => {
    const roles = authStore.user?.roles || [];
    const roleMap = {
        'ADMIN': '管理员',
        'PLANNER': '计划员',
        'SUPERVISOR': '主管'
    };
    return roles.map(r => {
        const name = typeof r === 'string' ? r : r.name;
        return roleMap[name] || name;
    }).join(', ');
});
const canAccessOrders = computed(() => {
    return authStore.hasRole('ADMIN') || authStore.hasRole('PLANNER');
});
const canAccessSchedule = computed(() => {
    return authStore.hasRole('ADMIN') || authStore.hasRole('PLANNER');
});
const canAccessUsers = computed(() => {
    return authStore.hasRole('ADMIN');
});
const goToOrders = () => router.push('/orders');
const goToSchedule = () => router.push('/schedule');
const goToUsers = () => router.push('/users');
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['stat-card']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-card']} */ ;
/** @type {__VLS_StyleScopedClasses['info-card']} */ ;
/** @type {__VLS_StyleScopedClasses['info-card']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button--primary']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button--success']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button--warning']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
/** @type {__VLS_StyleScopedClasses['welcome-section']} */ ;
/** @type {__VLS_StyleScopedClasses['welcome-text']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-value']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-label']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
/** @type {__VLS_StyleScopedClasses['dashboard']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-card']} */ ;
/** @type {__VLS_StyleScopedClasses['info-card']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
/** @type {__VLS_StyleScopedClasses['stat-card']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['el-button']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "dashboard" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "welcome-section" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
(__VLS_ctx.username);
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "welcome-text" },
});
(__VLS_ctx.userRole);
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
    xs: (24),
    sm: (12),
    md: (8),
}));
const __VLS_6 = __VLS_5({
    xs: (24),
    sm: (12),
    md: (8),
}, ...__VLS_functionalComponentArgsRest(__VLS_5));
__VLS_7.slots.default;
const __VLS_8 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({
    ...{ class: "stat-card" },
    shadow: "hover",
    role: "region",
    'aria-label': "工单统计",
}));
const __VLS_10 = __VLS_9({
    ...{ class: "stat-card" },
    shadow: "hover",
    role: "region",
    'aria-label': "工单统计",
}, ...__VLS_functionalComponentArgsRest(__VLS_9));
__VLS_11.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-content" },
});
const __VLS_12 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_13 = __VLS_asFunctionalComponent(__VLS_12, new __VLS_12({
    ...{ class: "stat-icon" },
    color: "#409EFF",
    'aria-hidden': "true",
}));
const __VLS_14 = __VLS_13({
    ...{ class: "stat-icon" },
    color: "#409EFF",
    'aria-hidden': "true",
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
    'aria-label': "工单总数",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-label" },
});
var __VLS_11;
var __VLS_7;
const __VLS_20 = {}.ElCol;
/** @type {[typeof __VLS_components.ElCol, typeof __VLS_components.elCol, typeof __VLS_components.ElCol, typeof __VLS_components.elCol, ]} */ ;
// @ts-ignore
const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({
    xs: (24),
    sm: (12),
    md: (8),
}));
const __VLS_22 = __VLS_21({
    xs: (24),
    sm: (12),
    md: (8),
}, ...__VLS_functionalComponentArgsRest(__VLS_21));
__VLS_23.slots.default;
const __VLS_24 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_25 = __VLS_asFunctionalComponent(__VLS_24, new __VLS_24({
    ...{ class: "stat-card" },
    shadow: "hover",
    role: "region",
    'aria-label': "排产计划统计",
}));
const __VLS_26 = __VLS_25({
    ...{ class: "stat-card" },
    shadow: "hover",
    role: "region",
    'aria-label': "排产计划统计",
}, ...__VLS_functionalComponentArgsRest(__VLS_25));
__VLS_27.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-content" },
});
const __VLS_28 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
    ...{ class: "stat-icon" },
    color: "#67C23A",
    'aria-hidden': "true",
}));
const __VLS_30 = __VLS_29({
    ...{ class: "stat-icon" },
    color: "#67C23A",
    'aria-hidden': "true",
}, ...__VLS_functionalComponentArgsRest(__VLS_29));
__VLS_31.slots.default;
const __VLS_32 = {}.Calendar;
/** @type {[typeof __VLS_components.Calendar, ]} */ ;
// @ts-ignore
const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({}));
const __VLS_34 = __VLS_33({}, ...__VLS_functionalComponentArgsRest(__VLS_33));
var __VLS_31;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-info" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-value" },
    'aria-label': "排产计划数",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-label" },
});
var __VLS_27;
var __VLS_23;
const __VLS_36 = {}.ElCol;
/** @type {[typeof __VLS_components.ElCol, typeof __VLS_components.elCol, typeof __VLS_components.ElCol, typeof __VLS_components.elCol, ]} */ ;
// @ts-ignore
const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({
    xs: (24),
    sm: (12),
    md: (8),
}));
const __VLS_38 = __VLS_37({
    xs: (24),
    sm: (12),
    md: (8),
}, ...__VLS_functionalComponentArgsRest(__VLS_37));
__VLS_39.slots.default;
const __VLS_40 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
    ...{ class: "stat-card" },
    shadow: "hover",
    role: "region",
    'aria-label': "设备资源统计",
}));
const __VLS_42 = __VLS_41({
    ...{ class: "stat-card" },
    shadow: "hover",
    role: "region",
    'aria-label': "设备资源统计",
}, ...__VLS_functionalComponentArgsRest(__VLS_41));
__VLS_43.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-content" },
});
const __VLS_44 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_45 = __VLS_asFunctionalComponent(__VLS_44, new __VLS_44({
    ...{ class: "stat-icon" },
    color: "#E6A23C",
    'aria-hidden': "true",
}));
const __VLS_46 = __VLS_45({
    ...{ class: "stat-icon" },
    color: "#E6A23C",
    'aria-hidden': "true",
}, ...__VLS_functionalComponentArgsRest(__VLS_45));
__VLS_47.slots.default;
const __VLS_48 = {}.Setting;
/** @type {[typeof __VLS_components.Setting, ]} */ ;
// @ts-ignore
const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({}));
const __VLS_50 = __VLS_49({}, ...__VLS_functionalComponentArgsRest(__VLS_49));
var __VLS_47;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-info" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-value" },
    'aria-label': "设备资源数",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "stat-label" },
});
var __VLS_43;
var __VLS_39;
var __VLS_3;
const __VLS_52 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_53 = __VLS_asFunctionalComponent(__VLS_52, new __VLS_52({
    ...{ class: "info-card" },
    shadow: "hover",
}));
const __VLS_54 = __VLS_53({
    ...{ class: "info-card" },
    shadow: "hover",
}, ...__VLS_functionalComponentArgsRest(__VLS_53));
__VLS_55.slots.default;
{
    const { header: __VLS_thisSlot } = __VLS_55.slots;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "quick-actions" },
});
if (__VLS_ctx.canAccessOrders) {
    const __VLS_56 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({
        ...{ 'onClick': {} },
        type: "primary",
        size: "large",
        'aria-label': "进入工单管理",
    }));
    const __VLS_58 = __VLS_57({
        ...{ 'onClick': {} },
        type: "primary",
        size: "large",
        'aria-label': "进入工单管理",
    }, ...__VLS_functionalComponentArgsRest(__VLS_57));
    let __VLS_60;
    let __VLS_61;
    let __VLS_62;
    const __VLS_63 = {
        onClick: (__VLS_ctx.goToOrders)
    };
    __VLS_59.slots.default;
    const __VLS_64 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({}));
    const __VLS_66 = __VLS_65({}, ...__VLS_functionalComponentArgsRest(__VLS_65));
    __VLS_67.slots.default;
    const __VLS_68 = {}.Document;
    /** @type {[typeof __VLS_components.Document, ]} */ ;
    // @ts-ignore
    const __VLS_69 = __VLS_asFunctionalComponent(__VLS_68, new __VLS_68({}));
    const __VLS_70 = __VLS_69({}, ...__VLS_functionalComponentArgsRest(__VLS_69));
    var __VLS_67;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    var __VLS_59;
}
if (__VLS_ctx.canAccessSchedule) {
    const __VLS_72 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_73 = __VLS_asFunctionalComponent(__VLS_72, new __VLS_72({
        ...{ 'onClick': {} },
        type: "success",
        size: "large",
        'aria-label': "进入排产计划",
    }));
    const __VLS_74 = __VLS_73({
        ...{ 'onClick': {} },
        type: "success",
        size: "large",
        'aria-label': "进入排产计划",
    }, ...__VLS_functionalComponentArgsRest(__VLS_73));
    let __VLS_76;
    let __VLS_77;
    let __VLS_78;
    const __VLS_79 = {
        onClick: (__VLS_ctx.goToSchedule)
    };
    __VLS_75.slots.default;
    const __VLS_80 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_81 = __VLS_asFunctionalComponent(__VLS_80, new __VLS_80({}));
    const __VLS_82 = __VLS_81({}, ...__VLS_functionalComponentArgsRest(__VLS_81));
    __VLS_83.slots.default;
    const __VLS_84 = {}.Calendar;
    /** @type {[typeof __VLS_components.Calendar, ]} */ ;
    // @ts-ignore
    const __VLS_85 = __VLS_asFunctionalComponent(__VLS_84, new __VLS_84({}));
    const __VLS_86 = __VLS_85({}, ...__VLS_functionalComponentArgsRest(__VLS_85));
    var __VLS_83;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    var __VLS_75;
}
if (__VLS_ctx.canAccessUsers) {
    const __VLS_88 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_89 = __VLS_asFunctionalComponent(__VLS_88, new __VLS_88({
        ...{ 'onClick': {} },
        type: "warning",
        size: "large",
        'aria-label': "进入用户管理",
    }));
    const __VLS_90 = __VLS_89({
        ...{ 'onClick': {} },
        type: "warning",
        size: "large",
        'aria-label': "进入用户管理",
    }, ...__VLS_functionalComponentArgsRest(__VLS_89));
    let __VLS_92;
    let __VLS_93;
    let __VLS_94;
    const __VLS_95 = {
        onClick: (__VLS_ctx.goToUsers)
    };
    __VLS_91.slots.default;
    const __VLS_96 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_97 = __VLS_asFunctionalComponent(__VLS_96, new __VLS_96({}));
    const __VLS_98 = __VLS_97({}, ...__VLS_functionalComponentArgsRest(__VLS_97));
    __VLS_99.slots.default;
    const __VLS_100 = {}.User;
    /** @type {[typeof __VLS_components.User, ]} */ ;
    // @ts-ignore
    const __VLS_101 = __VLS_asFunctionalComponent(__VLS_100, new __VLS_100({}));
    const __VLS_102 = __VLS_101({}, ...__VLS_functionalComponentArgsRest(__VLS_101));
    var __VLS_99;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    var __VLS_91;
}
var __VLS_55;
/** @type {__VLS_StyleScopedClasses['dashboard']} */ ;
/** @type {__VLS_StyleScopedClasses['welcome-section']} */ ;
/** @type {__VLS_StyleScopedClasses['welcome-text']} */ ;
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
/** @type {__VLS_StyleScopedClasses['info-card']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-actions']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            Document: Document,
            Calendar: Calendar,
            Setting: Setting,
            User: User,
            username: username,
            userRole: userRole,
            canAccessOrders: canAccessOrders,
            canAccessSchedule: canAccessSchedule,
            canAccessUsers: canAccessUsers,
            goToOrders: goToOrders,
            goToSchedule: goToSchedule,
            goToUsers: goToUsers,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
