package com.fourgtss.mnp.repository;

import com.fourgtss.mnp.models.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Short> {
    Optional<Operator> findByCode(String code);
}
