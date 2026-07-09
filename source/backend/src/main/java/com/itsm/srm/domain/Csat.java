package com.itsm.srm.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 만족도 평가(요청당 1:1).
 */
@Getter
@Entity
@Table(name = "csat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Csat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_request_id", nullable = false, unique = true)
    private Long serviceRequestId;

    @Column(nullable = false)
    private short score;

    @Column(length = 500)
    private String comment;

    public Csat(Long serviceRequestId, short score, String comment) {
        this.serviceRequestId = serviceRequestId;
        this.score = score;
        this.comment = comment;
    }
}
