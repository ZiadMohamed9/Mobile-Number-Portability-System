package com.fourgtss.mnp.models;

import com.fourgtss.mnp.models.enums.PortingRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "porting_request", indexes = {
        @Index(name = "idx_pr_phone_status", columnList = "phone_number, status")
})
public class PortingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phone_number", nullable = false)
    private MobileNumber mobileNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_operator_id", nullable = false)
    private Operator recipientOperator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_operator_id", nullable = false)
    private Operator donorOperator;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PortingRequestStatus status = PortingRequestStatus.PENDING;

    @Generated(event = EventType.INSERT)
    @Column(
            name = "requested_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant requestedAt;

    @Generated(event = EventType.INSERT)
    @Column(
            name = "expires_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant expiresAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    protected PortingRequest() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortingRequest that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
