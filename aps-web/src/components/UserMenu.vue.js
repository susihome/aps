/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { msgSuccess, confirmAction } from '@/utils/message';
import { ArrowDown, SwitchButton } from '@element-plus/icons-vue';
import { useAuthStore } from '../stores/auth';
const router = useRouter();
const authStore = useAuthStore();
const username = computed(() => authStore.user?.username || '');
const userInitial = computed(() => username.value.charAt(0).toUpperCase());
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
const handleCommand = async (command) => {
    if (command === 'logout') {
        try {
            await confirmAction('确定要退出登录吗？');
            await authStore.logout();
            msgSuccess('已退出登录');
            router.push('/login');
        }
        catch (error) {
            // 用户取消
        }
    }
};
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['user-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['user-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['el-dropdown-menu__item']} */ ;
/** @type {__VLS_StyleScopedClasses['username']} */ ;
/** @type {__VLS_StyleScopedClasses['user-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['user-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['el-dropdown-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['el-dropdown-menu__item']} */ ;
// CSS variable injection 
// CSS variable injection end 
const __VLS_0 = {}.ElDropdown;
/** @type {[typeof __VLS_components.ElDropdown, typeof __VLS_components.elDropdown, typeof __VLS_components.ElDropdown, typeof __VLS_components.elDropdown, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    ...{ 'onCommand': {} },
    trigger: "click",
}));
const __VLS_2 = __VLS_1({
    ...{ 'onCommand': {} },
    trigger: "click",
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
let __VLS_4;
let __VLS_5;
let __VLS_6;
const __VLS_7 = {
    onCommand: (__VLS_ctx.handleCommand)
};
var __VLS_8 = {};
__VLS_3.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "user-menu" },
    role: "button",
    tabindex: "0",
    'aria-label': "用户菜单",
});
const __VLS_9 = {}.ElAvatar;
/** @type {[typeof __VLS_components.ElAvatar, typeof __VLS_components.elAvatar, typeof __VLS_components.ElAvatar, typeof __VLS_components.elAvatar, ]} */ ;
// @ts-ignore
const __VLS_10 = __VLS_asFunctionalComponent(__VLS_9, new __VLS_9({
    size: (32),
    ...{ class: "user-avatar" },
    'aria-label': (`用户 ${__VLS_ctx.username}`),
}));
const __VLS_11 = __VLS_10({
    size: (32),
    ...{ class: "user-avatar" },
    'aria-label': (`用户 ${__VLS_ctx.username}`),
}, ...__VLS_functionalComponentArgsRest(__VLS_10));
__VLS_12.slots.default;
(__VLS_ctx.userInitial);
var __VLS_12;
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "username" },
});
(__VLS_ctx.username);
const __VLS_13 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_14 = __VLS_asFunctionalComponent(__VLS_13, new __VLS_13({
    ...{ class: "el-icon--right" },
    'aria-hidden': "true",
}));
const __VLS_15 = __VLS_14({
    ...{ class: "el-icon--right" },
    'aria-hidden': "true",
}, ...__VLS_functionalComponentArgsRest(__VLS_14));
__VLS_16.slots.default;
const __VLS_17 = {}.ArrowDown;
/** @type {[typeof __VLS_components.ArrowDown, ]} */ ;
// @ts-ignore
const __VLS_18 = __VLS_asFunctionalComponent(__VLS_17, new __VLS_17({}));
const __VLS_19 = __VLS_18({}, ...__VLS_functionalComponentArgsRest(__VLS_18));
var __VLS_16;
{
    const { dropdown: __VLS_thisSlot } = __VLS_3.slots;
    const __VLS_21 = {}.ElDropdownMenu;
    /** @type {[typeof __VLS_components.ElDropdownMenu, typeof __VLS_components.elDropdownMenu, typeof __VLS_components.ElDropdownMenu, typeof __VLS_components.elDropdownMenu, ]} */ ;
    // @ts-ignore
    const __VLS_22 = __VLS_asFunctionalComponent(__VLS_21, new __VLS_21({}));
    const __VLS_23 = __VLS_22({}, ...__VLS_functionalComponentArgsRest(__VLS_22));
    __VLS_24.slots.default;
    const __VLS_25 = {}.ElDropdownItem;
    /** @type {[typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, ]} */ ;
    // @ts-ignore
    const __VLS_26 = __VLS_asFunctionalComponent(__VLS_25, new __VLS_25({
        disabled: true,
    }));
    const __VLS_27 = __VLS_26({
        disabled: true,
    }, ...__VLS_functionalComponentArgsRest(__VLS_26));
    __VLS_28.slots.default;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "user-info" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "user-name" },
    });
    (__VLS_ctx.username);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "user-role" },
    });
    (__VLS_ctx.userRole);
    var __VLS_28;
    const __VLS_29 = {}.ElDropdownItem;
    /** @type {[typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, typeof __VLS_components.ElDropdownItem, typeof __VLS_components.elDropdownItem, ]} */ ;
    // @ts-ignore
    const __VLS_30 = __VLS_asFunctionalComponent(__VLS_29, new __VLS_29({
        divided: true,
        command: "logout",
        'aria-label': "退出登录",
    }));
    const __VLS_31 = __VLS_30({
        divided: true,
        command: "logout",
        'aria-label': "退出登录",
    }, ...__VLS_functionalComponentArgsRest(__VLS_30));
    __VLS_32.slots.default;
    const __VLS_33 = {}.ElIcon;
    /** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
    // @ts-ignore
    const __VLS_34 = __VLS_asFunctionalComponent(__VLS_33, new __VLS_33({
        'aria-hidden': "true",
    }));
    const __VLS_35 = __VLS_34({
        'aria-hidden': "true",
    }, ...__VLS_functionalComponentArgsRest(__VLS_34));
    __VLS_36.slots.default;
    const __VLS_37 = {}.SwitchButton;
    /** @type {[typeof __VLS_components.SwitchButton, ]} */ ;
    // @ts-ignore
    const __VLS_38 = __VLS_asFunctionalComponent(__VLS_37, new __VLS_37({}));
    const __VLS_39 = __VLS_38({}, ...__VLS_functionalComponentArgsRest(__VLS_38));
    var __VLS_36;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    var __VLS_32;
    var __VLS_24;
}
var __VLS_3;
/** @type {__VLS_StyleScopedClasses['user-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['user-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['username']} */ ;
/** @type {__VLS_StyleScopedClasses['el-icon--right']} */ ;
/** @type {__VLS_StyleScopedClasses['user-info']} */ ;
/** @type {__VLS_StyleScopedClasses['user-name']} */ ;
/** @type {__VLS_StyleScopedClasses['user-role']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            ArrowDown: ArrowDown,
            SwitchButton: SwitchButton,
            username: username,
            userInitial: userInitial,
            userRole: userRole,
            handleCommand: handleCommand,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
