/**
 * 法定节假日配置
 * 可以根据每年国务院发布的法定节假日安排更新此文件
 */
/**
 * 生成指定年份的春节日期
 * 春节日期每年不同，需要根据农历计算
 * 这里提供 2024-2028 年的春节日期
 */
function getSpringFestivalDates(year) {
    const springFestivals = {
        2024: ['2024-02-10', '2024-02-11', '2024-02-12', '2024-02-13', '2024-02-14', '2024-02-15', '2024-02-16', '2024-02-17'],
        2025: ['2025-01-28', '2025-01-29', '2025-01-30', '2025-01-31', '2025-02-01', '2025-02-02', '2025-02-03', '2025-02-04'],
        2026: ['2026-02-16', '2026-02-17', '2026-02-18', '2026-02-19', '2026-02-20', '2026-02-21', '2026-02-22', '2026-02-23'],
        2027: ['2027-02-05', '2027-02-06', '2027-02-07', '2027-02-08', '2027-02-09', '2027-02-10', '2027-02-11', '2027-02-12'],
        2028: ['2028-01-25', '2028-01-26', '2028-01-27', '2028-01-28', '2028-01-29', '2028-01-30', '2028-01-31', '2028-02-01']
    };
    return springFestivals[year] || [];
}
/**
 * 生成清明节日期（4月4日-6日之间，根据农历计算）
 */
function getQingmingFestivalDates(year) {
    // 清明节通常在4月4日、5日或6日
    // 这里使用4月4日-6日作为通用假期
    return [`${year}-04-04`, `${year}-04-05`, `${year}-04-06`];
}
/**
 * 生成劳动节日期（5月1日-5日）
 */
function getLaborDayDates(year) {
    return [`${year}-05-01`, `${year}-05-02`, `${year}-05-03`, `${year}-05-04`, `${year}-05-05`];
}
/**
 * 生成端午节日期（农历五月初五，公历5月-6月之间）
 */
function getDragonBoatFestivalDates(year) {
    const dragonBoatFestivals = {
        2024: ['2024-06-10'],
        2025: ['2025-05-31', '2025-06-01', '2025-06-02'],
        2026: ['2026-06-19', '2026-06-20', '2026-06-21'],
        2027: ['2027-06-09', '2027-06-10', '2027-06-11'],
        2028: ['2028-05-28', '2028-05-29', '2028-05-30']
    };
    return dragonBoatFestivals[year] || [];
}
/**
 * 生成中秋节日期（农历八月十五，公历9月-10月之间）
 */
function getMidAutumnFestivalDates(year) {
    const midAutumnFestivals = {
        2024: ['2024-09-15', '2024-09-16', '2024-09-17'],
        2025: ['2025-10-04', '2025-10-05', '2025-10-06'],
        2026: ['2026-09-24', '2026-09-25', '2026-09-26'],
        2027: ['2027-09-14', '2027-09-15', '2027-09-16'],
        2028: ['2028-10-02', '2028-10-03', '2028-10-04']
    };
    return midAutumnFestivals[year] || [];
}
/**
 * 生成国庆节日期（10月1日-7日）
 */
function getNationalDayDates(year) {
    return [`${year}-10-01`, `${year}-10-02`, `${year}-10-03`, `${year}-10-04`, `${year}-10-05`, `${year}-10-06`, `${year}-10-07`];
}
/**
 * 节假日模板配置
 */
export const holidayTemplates = [
    {
        key: 'spring',
        name: '春节',
        description: '农历新年，通常为8天假期',
        getDates: getSpringFestivalDates
    },
    {
        key: 'qingming',
        name: '清明节',
        description: '通常为4月4日-6日',
        getDates: getQingmingFestivalDates
    },
    {
        key: 'labor',
        name: '劳动节',
        description: '5月1日-5日',
        getDates: getLaborDayDates
    },
    {
        key: 'dragon',
        name: '端午节',
        description: '农历五月初五',
        getDates: getDragonBoatFestivalDates
    },
    {
        key: 'mid-autumn',
        name: '中秋节',
        description: '农历八月十五',
        getDates: getMidAutumnFestivalDates
    },
    {
        key: 'national',
        name: '国庆节',
        description: '10月1日-7日',
        getDates: getNationalDayDates
    }
];
/**
 * 根据模板key获取节假日日期
 * @param template 模板key
 * @param year 年份
 * @returns 日期数组
 */
export function getHolidayDates(template, year) {
    const holidayTemplate = holidayTemplates.find(t => t.key === template);
    if (!holidayTemplate) {
        throw new Error(`未找到节假日模板: ${template}`);
    }
    return holidayTemplate.getDates(year);
}
/**
 * 获取支持的年份范围
 */
export const SUPPORTED_YEARS = [2024, 2025, 2026, 2027, 2028];
/**
 * 检查年份是否支持
 */
export function isYearSupported(year) {
    return SUPPORTED_YEARS.includes(year);
}
