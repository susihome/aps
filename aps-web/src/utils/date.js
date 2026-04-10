/**
 * 日期时间工具函数
 */
/**
 * 解析时间字符串为 HH:mm:ss 格式
 * 支持的输入格式：HH:mm, HH:mm:ss, HHmmss
 * @param timeStr 时间字符串
 * @returns HH:mm:ss 格式的时间字符串
 * @throws Error 如果时间格式无效
 */
export function parseTime(timeStr) {
    if (!timeStr || typeof timeStr !== 'string') {
        throw new Error('时间格式无效：必须是非空字符串');
    }
    // 移除所有空格
    const cleaned = timeStr.trim();
    // 已经是 HH:mm:ss 格式
    if (/^\d{2}:\d{2}:\d{2}$/.test(cleaned)) {
        return cleaned;
    }
    // HH:mm 格式，补全秒数
    if (/^\d{2}:\d{2}$/.test(cleaned)) {
        return `${cleaned}:00`;
    }
    // HHmmss 格式，添加冒号
    if (/^\d{6}$/.test(cleaned)) {
        const hh = cleaned.substring(0, 2);
        const mm = cleaned.substring(2, 4);
        const ss = cleaned.substring(4, 6);
        return `${hh}:${mm}:${ss}`;
    }
    // HHmm 格式，补全秒数并添加冒号
    if (/^\d{4}$/.test(cleaned)) {
        const hh = cleaned.substring(0, 2);
        const mm = cleaned.substring(2, 4);
        return `${hh}:${mm}:00`;
    }
    throw new Error(`时间格式不支持：${timeStr}，支持的格式有 HH:mm、HH:mm:ss、HHmm、HHmmss`);
}
/**
 * 验证时间格式是否有效
 * @param timeStr 时间字符串
 * @returns 是否有效
 */
export function isValidTime(timeStr) {
    try {
        parseTime(timeStr);
        return true;
    }
    catch {
        return false;
    }
}
/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param date 日期对象或日期字符串
 * @returns YYYY-MM-DD 格式的日期字符串
 */
export function formatDate(date) {
    const d = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(d.getTime())) {
        throw new Error('无效的日期');
    }
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}
/**
 * 验证日期格式是否有效
 * @param dateStr 日期字符串
 * @returns 是否有效
 */
export function isValidDate(dateStr) {
    try {
        formatDate(dateStr);
        return true;
    }
    catch {
        return false;
    }
}
/**
 * 获取两个日期之间的所有日期（包含起止日期）
 * @param start 开始日期
 * @param end 结束日期
 * @returns 日期数组
 */
export function getDateRange(start, end) {
    const startDate = new Date(start);
    const endDate = new Date(end);
    const dates = [];
    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
        throw new Error('无效的日期范围');
    }
    if (startDate > endDate) {
        throw new Error('开始日期不能晚于结束日期');
    }
    const current = new Date(startDate);
    while (current <= endDate) {
        dates.push(formatDate(new Date(current)));
        current.setDate(current.getDate() + 1);
    }
    return dates;
}
/**
 * 验证班次时间是否合法
 * @param startTime 开始时间 HH:mm:ss
 * @param endTime 结束时间 HH:mm:ss
 * @param nextDay 是否跨天
 * @returns 验证结果和错误消息
 */
export function validateShiftTime(startTime, endTime, nextDay) {
    try {
        const parsedStart = parseTime(startTime);
        const parsedEnd = parseTime(endTime);
        const startParts = parsedStart.split(':').map(Number);
        const endParts = parsedEnd.split(':').map(Number);
        const startMinutes = startParts[0] * 60 + startParts[1];
        const endMinutes = endParts[0] * 60 + endParts[1];
        if (!nextDay) {
            // 非跨天班次：结束时间必须大于开始时间
            if (endMinutes <= startMinutes) {
                return {
                    valid: false,
                    error: `非跨天班次的结束时间(${endTime})必须大于开始时间(${startTime})`
                };
            }
        }
        else {
            // 跨天班次：结束时间应该小于或等于开始时间（表示次日）
            if (endMinutes > startMinutes) {
                return {
                    valid: false,
                    error: `跨天班次的结束时间(${endTime})应该小于或等于开始时间(${startTime})`
                };
            }
        }
        return { valid: true };
    }
    catch (error) {
        return {
            valid: false,
            error: error instanceof Error ? error.message : '时间格式验证失败'
        };
    }
}
