package com.fourgtss.mnp.repository;

import com.fourgtss.mnp.models.PortingRequest;
import com.fourgtss.mnp.models.enums.PortingRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE PortingRequest pr
        SET pr.status = com.fourgtss.mnp.models.enums.PortingRequestStatus.CANCELLED_TIMEOUT,
            pr.resolvedAt = CURRENT_TIMESTAMP
        WHERE pr.status = com.fourgtss.mnp.models.enums.PortingRequestStatus.PENDING
          AND pr.expiresAt <= CURRENT_TIMESTAMP
    """)
    int cancelAllExpiredPending();

    @Query("""
        SELECT pr.mobileNumber.phoneNumber
        FROM PortingRequest pr
        WHERE pr.id = :requestId
    """)
    Optional<String> findPhoneNumberById(@Param("requestId") Long requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT pr
        FROM PortingRequest pr
        WHERE pr.id = :requestId
    """)
    Optional<PortingRequest> findForDecision(@Param("requestId") Long requestId);

    boolean existsByMobileNumberPhoneNumberAndStatus(
            String phoneNumber,
            PortingRequestStatus status
    );

    @EntityGraph(attributePaths = {
            "mobileNumber",
            "donorOperator",
            "recipientOperator"
    })
    @Query("""
        SELECT pr
        FROM PortingRequest pr
        WHERE pr.donorOperator.id = :operatorId
           OR pr.recipientOperator.id = :operatorId
           OR pr.status = com.fourgtss.mnp.models.enums.PortingRequestStatus.ACCEPTED
        ORDER BY pr.requestedAt DESC
    """)
    Page<PortingRequest> findVisibleToOperator(
            @Param("operatorId") Short operatorId,
            Pageable pageable
    );
}
