package com.aps.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("定时任务锁服务测试")
class ScheduledTaskLockServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ScheduledTaskLockService scheduledTaskLockService;

    @Test
    @DisplayName("获取锁成功时应写入带TTL的Redis键")
    void tryLock_shouldSetRedisKeyWithTtl() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("lock:scheduled:auth:session-cleanup"), eq("owner"), any(java.time.Duration.class)))
                .thenReturn(true);

        boolean locked = scheduledTaskLockService.tryLock("auth:session-cleanup", "owner");

        assertThat(locked).isTrue();
    }

    @Test
    @DisplayName("释放锁时应校验owner token")
    void unlock_shouldUseOwnerTokenScript() {
        when(stringRedisTemplate.execute(any(), eq(List.of("lock:scheduled:auth:session-cleanup")), eq("owner")))
                .thenReturn(true);

        boolean unlocked = scheduledTaskLockService.unlock("auth:session-cleanup", "owner");

        assertThat(unlocked).isTrue();
        verify(stringRedisTemplate).execute(any(), eq(List.of("lock:scheduled:auth:session-cleanup")), eq("owner"));
    }
}
