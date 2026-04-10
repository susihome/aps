import { ElMessage, ElMessageBox } from 'element-plus';
const MSG_DURATION = 3000;
const MSG_OFFSET = 32;
const CUSTOM_CLASS = 'aps-message';
/**
 * 统一消息提醒 - 成功
 */
export function msgSuccess(text) {
    return ElMessage({
        message: text,
        type: 'success',
        duration: MSG_DURATION,
        offset: MSG_OFFSET,
        grouping: true,
        showClose: true,
        customClass: CUSTOM_CLASS,
    });
}
/**
 * 统一消息提醒 - 错误
 */
export function msgError(text) {
    return ElMessage({
        message: text,
        type: 'error',
        duration: 4500,
        offset: MSG_OFFSET,
        grouping: true,
        showClose: true,
        customClass: CUSTOM_CLASS,
    });
}
/**
 * 统一消息提醒 - 警告
 */
export function msgWarning(text) {
    return ElMessage({
        message: text,
        type: 'warning',
        duration: MSG_DURATION,
        offset: MSG_OFFSET,
        grouping: true,
        showClose: true,
        customClass: CUSTOM_CLASS,
    });
}
/**
 * 统一消息提醒 - 信息
 */
export function msgInfo(text) {
    return ElMessage({
        message: text,
        type: 'info',
        duration: MSG_DURATION,
        offset: MSG_OFFSET,
        grouping: true,
        showClose: true,
        customClass: CUSTOM_CLASS,
    });
}
/**
 * 统一确认框 - 危险操作（删除等）
 */
export function confirmDanger(message, title = '操作确认') {
    return ElMessageBox.confirm(message, title, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        draggable: true,
        customClass: 'aps-confirm-dialog',
        confirmButtonClass: 'aps-confirm-btn-danger',
        cancelButtonClass: 'aps-cancel-btn',
        showClose: true,
        closeOnClickModal: false,
        distinguishCancelAndClose: true,
    }).then(() => { });
}
/**
 * 统一确认框 - 普通确认
 */
export function confirmAction(message, title = '操作确认') {
    return ElMessageBox.confirm(message, title, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
        draggable: true,
        customClass: 'aps-confirm-dialog',
        cancelButtonClass: 'aps-cancel-btn',
        showClose: true,
        closeOnClickModal: false,
        distinguishCancelAndClose: true,
    }).then(() => { });
}
/**
 * 从 Axios 错误或普通错误中提取消息
 */
export function extractErrorMsg(error, fallback) {
    if (!error)
        return fallback;
    return ((typeof error === 'object' && error !== null && 'response' in error
        ? error.response?.data?.message
        : undefined) ||
        (error instanceof Error ? error.message : fallback));
}
