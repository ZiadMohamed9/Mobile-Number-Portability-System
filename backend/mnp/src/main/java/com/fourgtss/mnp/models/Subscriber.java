package com.fourgtss.mnp.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "subscriber")
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "national_id_hmac", nullable = false, length = 32, unique = true)
    private byte[] nationalIdHmac;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "national_id_last4", nullable = false, length = 4)
    private String nationalIdLast4;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    protected Subscriber() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subscriber that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}