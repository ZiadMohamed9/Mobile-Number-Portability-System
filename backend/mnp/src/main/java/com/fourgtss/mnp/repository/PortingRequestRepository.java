package com.fourgtss.mnp.repository;

import com.fourgtss.mnp.models.PortingRequest;
import com.fourgtss.mnp.models.enums.PortingRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PortingRequestRepository extends JpaRepository<PortingRequest, Long> {
    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE PortingRequest pr
        SET pr.status = com.fourgtss.mnp.models.enums.PortingRequestStatus.CANCELLED_TIMEOUT,
            pr.resolvedAt = CURRENT_TIMESTAMP
        WHERE pr.mobileNumber.phoneNumber = :phoneNumber
          AND pr.status = com.fourgtss.mnp.models.enums.PortingRequestStatus.PENDING
          AND pr.expiresAt <= CURRENT_TIMESTAMP
    """)
    int cancelExpiredPending(
            @Param("phoneNumber") String phoneNumber
    );

    boolean existsByMobileNumberPhoneNumberAndStatus(
            String phoneNumber,
            PortingRequestStatus status
    );
}
