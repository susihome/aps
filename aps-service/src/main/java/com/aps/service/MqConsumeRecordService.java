package com.aps.service;

import com.aps.domain.entity.MqConsumeRecord;
import com.aps.service.repository.MqConsumeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MqConsumeRecordService {

    private final MqConsumeRecordRepository mqConsumeRecordRepository;

    @Transactional
    public boolean tryStartConsume(String messageId, String consumerName, String businessKey) {
        return mqConsumeRecordRepository.findByMessageIdAndConsumerName(messageId, consumerName).isEmpty();
    }

    @Transactional
    public void markConsumed(String messageId, String consumerName, String businessKey) {
        if (mqConsumeRecordRepository.findByMessageIdAndConsumerName(messageId, consumerName).isPresent()) {
            return;
        }

        MqConsumeRecord record = new MqConsumeRecord();
        record.setMessageId(messageId);
        record.setConsumerName(consumerName);
        record.setBusinessKey(businessKey);
        record.setConsumedAt(LocalDateTime.now());
        mqConsumeRecordRepository.save(record);
    }
}
