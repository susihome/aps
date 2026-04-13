package com.aps.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("排产锁服务测试")
class ScheduleLockServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("尝试加锁时应写入 owner token")
    void tryLock_shouldStoreOwnerToken() {
        UUID scheduleId = UUID.randomUUID();
        String ownerToken = "owner-token";
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(
                "lock:schedule:solve:" + scheduleId,
                ownerToken,
                Duration.ofMinutes(10)))
                .thenReturn(true);
        ScheduleLockService scheduleLockService = new ScheduleLockService(stringRedisTemplate);

        boolean locked = scheduleLockService.tryLock(scheduleId, ownerToken);

        assertThat(locked).isTrue();
    }

    @Test
    @DisplayName("解锁时应校验 owner token")
    void unlock_shouldUseOwnerTokenScript() {
        UUID scheduleId = UUID.randomUUID();
        String ownerToken = "owner-token";
        when(stringRedisTemplate.execute(any(), eq(List.of("lock:schedule:solve:" + scheduleId)), eq(ownerToken)))
                .thenReturn(true);
        ScheduleLockService scheduleLockService = new ScheduleLockService(stringRedisTemplate);

        boolean unlocked = scheduleLockService.unlock(scheduleId, ownerToken);

        assertThat(unlocked).isTrue();
        verify(stringRedisTemplate).execute(any(), eq(List.of("lock:schedule:solve:" + scheduleId)), eq(ownerToken));
    }

    @Test
    @DisplayName("续期时应校验 owner token")
    void renewLock_shouldUseOwnerTokenScript() {
        UUID scheduleId = UUID.randomUUID();
        String ownerToken = "owner-token";
        when(stringRedisTemplate.execute(
                any(),
                eq(List.of("lock:schedule:solve:" + scheduleId)),
                eq(ownerToken),
                eq("600")))
                .thenReturn(true);
        ScheduleLockService scheduleLockService = new ScheduleLockService(stringRedisTemplate);

        boolean renewed = scheduleLockService.renewLock(scheduleId, ownerToken);

        assertThat(renewed).isTrue();
        verify(stringRedisTemplate).execute(
                any(),
                eq(List.of("lock:schedule:solve:" + scheduleId)),
                eq(ownerToken),
                eq("600"));
    }

    @Test
    @DisplayName("查询锁归属时应比较 owner token")
    void isOwnedBy_shouldCompareOwnerToken() {
        UUID scheduleId = UUID.randomUUID();
        String ownerToken = "owner-token";
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("lock:schedule:solve:" + scheduleId)).thenReturn(ownerToken);
        ScheduleLockService scheduleLockService = new ScheduleLockService(stringRedisTemplate);

        boolean owned = scheduleLockService.isOwnedBy(scheduleId, ownerToken);

        assertThat(owned).isTrue();
    }
}
