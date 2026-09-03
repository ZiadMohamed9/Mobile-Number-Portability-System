package com.fourgtss.mnp.repository;

import com.fourgtss.mnp.models.MobileNumber;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobileNumberRepository extends JpaRepository<MobileNumber, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT mn FROM MobileNumber mn WHERE mn.phoneNumber = :phoneNumber
    """)
    Optional<MobileNumber> findForPortingRequest(@Param("phoneNumber") String phoneNumber);
}
