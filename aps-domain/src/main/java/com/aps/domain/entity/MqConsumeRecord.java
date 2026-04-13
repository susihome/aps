package com.aps.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mq_consume_record")
@Getter
@Setter
public class MqConsumeRecord extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String messageId;

    @Column(nullable = false, length = 128)
    private String consumerName;

    @Column(length = 128)
    private String businessKey;

    @Column(nullable = false)
    private LocalDateTime consumedAt;
}
