import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
export const useTagsStore = defineStore('tags', () => {
    const visitedTags = ref([
        { path: '/dashboard', name: 'Dashboard', title: '首页', icon: 'HomeFilled', closable: false }
    ]);
    const activeTag = ref('/dashboard');
    const getVisitedTags = computed(() => visitedTags.value);
    function addTag(route) {
        const { path, name, meta } = route;
        if (!meta.title)
            return;
        const exists = visitedTags.value.some(tag => tag.path === path);
        if (!exists) {
            visitedTags.value.push({
                path,
                name: name || '',
                title: meta.title || '',
                icon: meta.icon || '',
                closable: path !== '/dashboard'
            });
        }
        activeTag.value = path;
    }
    function removeTag(targetPath) {
        const index = visitedTags.value.findIndex(tag => tag.path === targetPath);
        if (index === -1)
            return;
        const tag = visitedTags.value[index];
        if (!tag.closable)
            return;
        visitedTags.value.splice(index, 1);
        // 如果关闭的是当前激活的标签，切换到最近的标签
        if (activeTag.value === targetPath) {
            const nextTag = visitedTags.value[index] || visitedTags.value[index - 1];
            activeTag.value = nextTag ? nextTag.path : '/dashboard';
            return nextTag?.path || '/dashboard';
        }
        return null;
    }
    function removeOtherTags(targetPath) {
        visitedTags.value = visitedTags.value.filter(tag => !tag.closable || tag.path === targetPath);
    }
    function removeAllTags() {
        visitedTags.value = visitedTags.value.filter(tag => !tag.closable);
        activeTag.value = '/dashboard';
        return '/dashboard';
    }
    function setActiveTag(path) {
        activeTag.value = path;
    }
    return {
        visitedTags,
        activeTag,
        getVisitedTags,
        addTag,
        removeTag,
        removeOtherTags,
        removeAllTags,
        setActiveTag
    };
});
