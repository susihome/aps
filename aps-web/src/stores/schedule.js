import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from 'axios';
export const useScheduleStore = defineStore('schedule', () => {
    const progress = ref(0);
    const currentScheduleId = ref(null);
    async function triggerSolve() {
        if (!currentScheduleId.value) {
            const res = await axios.post('/api/schedules', {});
            currentScheduleId.value = res.data.id;
        }
        await axios.post(`/api/schedules/${currentScheduleId.value}/solve`);
    }
    function updateProgress(value) {
        progress.value = value;
    }
    return {
        progress,
        currentScheduleId,
        triggerSolve,
        updateProgress
    };
});
