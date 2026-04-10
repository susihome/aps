/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { msgSuccess, msgError } from '@/utils/message';
import { User, Lock, Calendar, Document, Setting, DataLine } from '@element-plus/icons-vue';
import { useAuthStore } from '../stores/auth';
const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const formRef = ref();
const loading = ref(false);
const showShake = ref(false);
const form = reactive({
    username: '',
    password: ''
});
const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
};
const handleLogin = async () => {
    if (!formRef.value)
        return;
    await formRef.value.validate(async (valid) => {
        if (!valid)
            return;
        loading.value = true;
        try {
            await authStore.login({
                username: form.username,
                password: form.password
            });
            msgSuccess('登录成功');
            const redirect = route.query.redirect;
            router.push(redirect || '/dashboard');
        }
        catch (error) {
            showShake.value = true;
            setTimeout(() => {
                showShake.value = false;
            }, 500);
            const message = error instanceof Error ? error.message : '登录失败，请检查用户名和密码';
            msgError(message);
        }
        finally {
            loading.value = false;
        }
    });
};
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['brand-section']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-header']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-content']} */ ;
/** @type {__VLS_StyleScopedClasses['feature-item']} */ ;
/** @type {__VLS_StyleScopedClasses['login-card']} */ ;
/** @type {__VLS_StyleScopedClasses['login-card']} */ ;
/** @type {__VLS_StyleScopedClasses['login-form']} */ ;
/** @type {__VLS_StyleScopedClasses['login-form']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['login-form']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['login-form']} */ ;
/** @type {__VLS_StyleScopedClasses['login-form']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__inner']} */ ;
/** @type {__VLS_StyleScopedClasses['login-form']} */ ;
/** @type {__VLS_StyleScopedClasses['login-button']} */ ;
/** @type {__VLS_StyleScopedClasses['login-button']} */ ;
/** @type {__VLS_StyleScopedClasses['login-footer']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-section']} */ ;
/** @type {__VLS_StyleScopedClasses['form-section']} */ ;
/** @type {__VLS_StyleScopedClasses['form-section']} */ ;
/** @type {__VLS_StyleScopedClasses['login-card']} */ ;
/** @type {__VLS_StyleScopedClasses['login-card']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-content']} */ ;
/** @type {__VLS_StyleScopedClasses['login-card']} */ ;
/** @type {__VLS_StyleScopedClasses['login-card']} */ ;
/** @type {__VLS_StyleScopedClasses['shake']} */ ;
/** @type {__VLS_StyleScopedClasses['login-form']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['login-button']} */ ;
/** @type {__VLS_StyleScopedClasses['login-button']} */ ;
/** @type {__VLS_StyleScopedClasses['login-button']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "login-container" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "brand-section" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "brand-header" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "brand-icon" },
});
const __VLS_0 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({}));
const __VLS_2 = __VLS_1({}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_3.slots.default;
const __VLS_4 = {}.Calendar;
/** @type {[typeof __VLS_components.Calendar, ]} */ ;
// @ts-ignore
const __VLS_5 = __VLS_asFunctionalComponent(__VLS_4, new __VLS_4({}));
const __VLS_6 = __VLS_5({}, ...__VLS_functionalComponentArgsRest(__VLS_5));
var __VLS_3;
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "brand-content" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.br)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "brand-subtitle" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "feature-list" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "feature-item" },
});
const __VLS_8 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({}));
const __VLS_10 = __VLS_9({}, ...__VLS_functionalComponentArgsRest(__VLS_9));
__VLS_11.slots.default;
const __VLS_12 = {}.Document;
/** @type {[typeof __VLS_components.Document, ]} */ ;
// @ts-ignore
const __VLS_13 = __VLS_asFunctionalComponent(__VLS_12, new __VLS_12({}));
const __VLS_14 = __VLS_13({}, ...__VLS_functionalComponentArgsRest(__VLS_13));
var __VLS_11;
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "feature-item" },
});
const __VLS_16 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_17 = __VLS_asFunctionalComponent(__VLS_16, new __VLS_16({}));
const __VLS_18 = __VLS_17({}, ...__VLS_functionalComponentArgsRest(__VLS_17));
__VLS_19.slots.default;
const __VLS_20 = {}.Setting;
/** @type {[typeof __VLS_components.Setting, ]} */ ;
// @ts-ignore
const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({}));
const __VLS_22 = __VLS_21({}, ...__VLS_functionalComponentArgsRest(__VLS_21));
var __VLS_19;
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "feature-item" },
});
const __VLS_24 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_25 = __VLS_asFunctionalComponent(__VLS_24, new __VLS_24({}));
const __VLS_26 = __VLS_25({}, ...__VLS_functionalComponentArgsRest(__VLS_25));
__VLS_27.slots.default;
const __VLS_28 = {}.DataLine;
/** @type {[typeof __VLS_components.DataLine, ]} */ ;
// @ts-ignore
const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({}));
const __VLS_30 = __VLS_29({}, ...__VLS_functionalComponentArgsRest(__VLS_29));
var __VLS_27;
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "form-section" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "login-card" },
    ...{ class: ({ shake: __VLS_ctx.showShake }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "card-icon" },
});
const __VLS_32 = {}.ElIcon;
/** @type {[typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, typeof __VLS_components.ElIcon, typeof __VLS_components.elIcon, ]} */ ;
// @ts-ignore
const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({}));
const __VLS_34 = __VLS_33({}, ...__VLS_functionalComponentArgsRest(__VLS_33));
__VLS_35.slots.default;
const __VLS_36 = {}.Calendar;
/** @type {[typeof __VLS_components.Calendar, ]} */ ;
// @ts-ignore
const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({}));
const __VLS_38 = __VLS_37({}, ...__VLS_functionalComponentArgsRest(__VLS_37));
var __VLS_35;
__VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "card-subtitle" },
});
const __VLS_40 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
    ...{ 'onKeyup': {} },
    ref: "formRef",
    model: (__VLS_ctx.form),
    rules: (__VLS_ctx.rules),
    ...{ class: "login-form" },
    'aria-label': "登录表单",
}));
const __VLS_42 = __VLS_41({
    ...{ 'onKeyup': {} },
    ref: "formRef",
    model: (__VLS_ctx.form),
    rules: (__VLS_ctx.rules),
    ...{ class: "login-form" },
    'aria-label': "登录表单",
}, ...__VLS_functionalComponentArgsRest(__VLS_41));
let __VLS_44;
let __VLS_45;
let __VLS_46;
const __VLS_47 = {
    onKeyup: (__VLS_ctx.handleLogin)
};
/** @type {typeof __VLS_ctx.formRef} */ ;
var __VLS_48 = {};
__VLS_43.slots.default;
const __VLS_50 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_51 = __VLS_asFunctionalComponent(__VLS_50, new __VLS_50({
    prop: "username",
}));
const __VLS_52 = __VLS_51({
    prop: "username",
}, ...__VLS_functionalComponentArgsRest(__VLS_51));
__VLS_53.slots.default;
const __VLS_54 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_55 = __VLS_asFunctionalComponent(__VLS_54, new __VLS_54({
    modelValue: (__VLS_ctx.form.username),
    placeholder: "用户名",
    size: "large",
    prefixIcon: (__VLS_ctx.User),
    autocomplete: "username",
    'aria-label': "用户名",
}));
const __VLS_56 = __VLS_55({
    modelValue: (__VLS_ctx.form.username),
    placeholder: "用户名",
    size: "large",
    prefixIcon: (__VLS_ctx.User),
    autocomplete: "username",
    'aria-label': "用户名",
}, ...__VLS_functionalComponentArgsRest(__VLS_55));
var __VLS_53;
const __VLS_58 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_59 = __VLS_asFunctionalComponent(__VLS_58, new __VLS_58({
    prop: "password",
}));
const __VLS_60 = __VLS_59({
    prop: "password",
}, ...__VLS_functionalComponentArgsRest(__VLS_59));
__VLS_61.slots.default;
const __VLS_62 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_63 = __VLS_asFunctionalComponent(__VLS_62, new __VLS_62({
    modelValue: (__VLS_ctx.form.password),
    type: "password",
    placeholder: "密码",
    size: "large",
    prefixIcon: (__VLS_ctx.Lock),
    showPassword: true,
    autocomplete: "current-password",
    'aria-label': "密码",
}));
const __VLS_64 = __VLS_63({
    modelValue: (__VLS_ctx.form.password),
    type: "password",
    placeholder: "密码",
    size: "large",
    prefixIcon: (__VLS_ctx.Lock),
    showPassword: true,
    autocomplete: "current-password",
    'aria-label': "密码",
}, ...__VLS_functionalComponentArgsRest(__VLS_63));
var __VLS_61;
const __VLS_66 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_67 = __VLS_asFunctionalComponent(__VLS_66, new __VLS_66({}));
const __VLS_68 = __VLS_67({}, ...__VLS_functionalComponentArgsRest(__VLS_67));
__VLS_69.slots.default;
const __VLS_70 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_71 = __VLS_asFunctionalComponent(__VLS_70, new __VLS_70({
    ...{ 'onClick': {} },
    type: "primary",
    size: "large",
    loading: (__VLS_ctx.loading),
    ...{ class: "login-button" },
    'aria-label': "登录",
}));
const __VLS_72 = __VLS_71({
    ...{ 'onClick': {} },
    type: "primary",
    size: "large",
    loading: (__VLS_ctx.loading),
    ...{ class: "login-button" },
    'aria-label': "登录",
}, ...__VLS_functionalComponentArgsRest(__VLS_71));
let __VLS_74;
let __VLS_75;
let __VLS_76;
const __VLS_77 = {
    onClick: (__VLS_ctx.handleLogin)
};
__VLS_73.slots.default;
(__VLS_ctx.loading ? '登录中...' : '登录');
var __VLS_73;
var __VLS_69;
var __VLS_43;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "login-footer" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
/** @type {__VLS_StyleScopedClasses['login-container']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-section']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-header']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-content']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-subtitle']} */ ;
/** @type {__VLS_StyleScopedClasses['feature-list']} */ ;
/** @type {__VLS_StyleScopedClasses['feature-item']} */ ;
/** @type {__VLS_StyleScopedClasses['feature-item']} */ ;
/** @type {__VLS_StyleScopedClasses['feature-item']} */ ;
/** @type {__VLS_StyleScopedClasses['form-section']} */ ;
/** @type {__VLS_StyleScopedClasses['login-card']} */ ;
/** @type {__VLS_StyleScopedClasses['card-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['card-subtitle']} */ ;
/** @type {__VLS_StyleScopedClasses['login-form']} */ ;
/** @type {__VLS_StyleScopedClasses['login-button']} */ ;
/** @type {__VLS_StyleScopedClasses['login-footer']} */ ;
// @ts-ignore
var __VLS_49 = __VLS_48;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            User: User,
            Lock: Lock,
            Calendar: Calendar,
            Document: Document,
            Setting: Setting,
            DataLine: DataLine,
            formRef: formRef,
            loading: loading,
            showShake: showShake,
            form: form,
            rules: rules,
            handleLogin: handleLogin,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
