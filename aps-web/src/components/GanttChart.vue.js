/// <reference types="../../node_modules/.vue-global-types/vue_3.5_0_0_0.d.ts" />
import { ref, onMounted, onUnmounted } from 'vue';
import Gantt from 'frappe-gantt';
import 'frappe-gantt/dist/frappe-gantt.css';
import { useScheduleStore } from '../stores/schedule';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { msgError } from '@/utils/message';
const scheduleStore = useScheduleStore();
const ganttContainer = ref();
let ganttInstance = null;
let stompClient = null;
onMounted(() => {
    initGantt();
    connectWebSocket();
});
onUnmounted(() => {
    if (stompClient) {
        stompClient.deactivate();
    }
});
function initGantt() {
    const tasks = [
        {
            id: '1',
            name: '工序1',
            start: '2026-04-02',
            end: '2026-04-03',
            progress: 0,
            dependencies: ''
        }
    ];
    if (ganttContainer.value) {
        try {
            ganttInstance = new Gantt(ganttContainer.value, tasks, {
                view_mode: 'Day',
                on_click: (task) => {
                    // 任务点击事件
                },
                on_date_change: (task, start, end) => {
                    // 任务日期变更事件
                }
            });
        }
        catch (error) {
            const message = error instanceof Error ? error.message : '初始化甘特图失败';
            msgError(message);
        }
    }
}
function connectWebSocket() {
    try {
        stompClient = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws/schedule-progress'),
            onConnect: () => {
                stompClient?.subscribe('/topic/schedule/1', (message) => {
                    try {
                        const data = JSON.parse(message.body);
                        if (data.type === 'PROGRESS') {
                            scheduleStore.updateProgress(data.progress);
                        }
                    }
                    catch (error) {
                        const errorMessage = error instanceof Error ? error.message : 'WebSocket消息解析失败';
                        msgError(errorMessage);
                    }
                });
            },
            onStompError: (frame) => {
                msgError('WebSocket连接错误');
            },
            onWebSocketError: () => {
                msgError('WebSocket连接失败');
            }
        });
        stompClient.activate();
    }
    catch (error) {
        const message = error instanceof Error ? error.message : 'WebSocket初始化失败';
        msgError(message);
    }
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['solve-button']} */ ;
/** @type {__VLS_StyleScopedClasses['solve-button']} */ ;
/** @type {__VLS_StyleScopedClasses['solve-button']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-container']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-container']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-container']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-container']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-container']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-container']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-container']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-container']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-container']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-container']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['el-progress']} */ ;
/** @type {__VLS_StyleScopedClasses['solve-button']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['solve-button']} */ ;
/** @type {__VLS_StyleScopedClasses['solve-button']} */ ;
/** @type {__VLS_StyleScopedClasses['solve-button']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "gantt-wrapper" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "toolbar" },
});
const __VLS_0 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    ...{ 'onClick': {} },
    type: "primary",
    ...{ class: "solve-button" },
    'aria-label': "开始排产",
}));
const __VLS_2 = __VLS_1({
    ...{ 'onClick': {} },
    type: "primary",
    ...{ class: "solve-button" },
    'aria-label': "开始排产",
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
let __VLS_4;
let __VLS_5;
let __VLS_6;
const __VLS_7 = {
    onClick: (__VLS_ctx.scheduleStore.triggerSolve)
};
__VLS_3.slots.default;
var __VLS_3;
const __VLS_8 = {}.ElProgress;
/** @type {[typeof __VLS_components.ElProgress, typeof __VLS_components.elProgress, ]} */ ;
// @ts-ignore
const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({
    percentage: (__VLS_ctx.scheduleStore.progress),
    ...{ style: ({ width: '300px', marginLeft: '20px' }) },
    strokeWidth: (8),
    'aria-label': "排产进度",
}));
const __VLS_10 = __VLS_9({
    percentage: (__VLS_ctx.scheduleStore.progress),
    ...{ style: ({ width: '300px', marginLeft: '20px' }) },
    strokeWidth: (8),
    'aria-label': "排产进度",
}, ...__VLS_functionalComponentArgsRest(__VLS_9));
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ref: "ganttContainer",
    ...{ class: "gantt-container" },
    role: "region",
    'aria-label': "甘特图",
});
/** @type {typeof __VLS_ctx.ganttContainer} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['solve-button']} */ ;
/** @type {__VLS_StyleScopedClasses['gantt-container']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            scheduleStore: scheduleStore,
            ganttContainer: ganttContainer,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
