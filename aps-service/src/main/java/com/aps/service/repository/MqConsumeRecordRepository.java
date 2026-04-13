package com.aps.service.repository;

import com.aps.domain.entity.MqConsumeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MqConsumeRecordRepository extends JpaRepository<MqConsumeRecord, UUID> {

    Optional<MqConsumeRecord> findByMessageIdAndConsumerName(String messageId, String consumerName);
}
