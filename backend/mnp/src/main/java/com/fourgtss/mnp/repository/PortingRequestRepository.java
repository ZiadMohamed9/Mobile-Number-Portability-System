package com.fourgtss.mnp.repository;

import com.fourgtss.mnp.models.PortingRequest;
import com.fourgtss.mnp.models.enums.PortingRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    List<PortingRequest> findByStatus(PortingRequestStatus status);

    List<PortingRequest> findByDonorOperator_Id(Short donorOperatorId);

    List<PortingRequest> findByRecipientOperator_Id(Short receiverOperatorId);
}
