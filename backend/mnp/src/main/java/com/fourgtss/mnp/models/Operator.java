package com.fourgtss.mnp.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "operator")
public class Operator {

    @Id
    @Column(name = "id", nullable = false)
    private Short id;

    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "number_prefix", nullable = false, length = 3, unique = true)
    private String numberPrefix;

    protected Operator() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Operator that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
