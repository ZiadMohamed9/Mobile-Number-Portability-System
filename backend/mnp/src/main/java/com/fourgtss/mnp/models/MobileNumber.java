package com.fourgtss.mnp.models;

import com.fourgtss.mnp.models.enums.ServiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mobile_number")
public class MobileNumber {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "phone_number", nullable = false, length = 11)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_operator_id", nullable = false)
    private Operator originOperator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "current_operator_id", nullable = false)
    private Operator currentOperator;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_status", nullable = false, length = 20)
    private ServiceStatus serviceStatus;

    @Column(name = "current_operator_since", nullable = false)
    private Instant currentOperatorSince;

    protected MobileNumber() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MobileNumber that)) return false;
        return phoneNumber.equals(that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return phoneNumber.hashCode();
    }

}
