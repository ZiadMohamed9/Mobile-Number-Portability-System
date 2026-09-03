package com.fourgtss.mnp.service;

import com.fourgtss.mnp.dto.CreateMobileNumber;
import com.fourgtss.mnp.dto.MobileNumberResponse;
import com.fourgtss.mnp.exception.MobileNumberErrorCode;
import com.fourgtss.mnp.exception.MobileNumberException;
import com.fourgtss.mnp.models.MobileNumber;
import com.fourgtss.mnp.models.Operator;
import com.fourgtss.mnp.models.Subscriber;
import com.fourgtss.mnp.repository.MobileNumberRepository;
import com.fourgtss.mnp.repository.OperatorRepository;
import com.fourgtss.mnp.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class MobileNumberService {

    private final MobileNumberRepository mobileNumberRepository;
    private final SubscriberRepository subscriberRepository;
    private final OperatorRepository operatorRepository;
    private final NationalIdVerifier nationalIdVerifier;
    private final Clock clock;

    @Transactional
    public MobileNumberResponse create(CreateMobileNumber request) {
        Operator operator = resolveOperator(request.operatorCode());
        validatePrefix(request.phoneNumber(), operator);
        validateNotFutureDate(request.currentOperatorSince());

        byte[] hmac = nationalIdVerifier.calculateHmac(request.nationalId());
        Subscriber subscriber = resolveOrCreateSubscriber(hmac, request);

        MobileNumber mobileNumber = MobileNumber.create(
                request.phoneNumber(),
                subscriber,
                operator,
                request.serviceStatus(),
                request.currentOperatorSince().atStartOfDay(ZoneOffset.UTC).toInstant()
        );

        try {
            mobileNumberRepository.saveAndFlush(mobileNumber);
        } catch (DataIntegrityViolationException exception) {
            throw new MobileNumberException(MobileNumberErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
        }

        return toResponse(mobileNumber);
    }

    private Operator resolveOperator(String operatorCode) {
        return operatorRepository.findByCode(operatorCode.trim().toUpperCase())
                .orElseThrow(() -> new MobileNumberException(MobileNumberErrorCode.UNKNOWN_OPERATOR));
    }

    private void validatePrefix(String phoneNumber, Operator operator) {
        String phonePrefix = phoneNumber.substring(0, 3);
        if (!phonePrefix.equals(operator.getNumberPrefix())) {
            throw new MobileNumberException(MobileNumberErrorCode.OPERATOR_PREFIX_MISMATCH);
        }
    }

    private void validateNotFutureDate(LocalDate date) {
        if (date.isAfter(LocalDate.now(clock))) {
            throw new MobileNumberException(MobileNumberErrorCode.FUTURE_DATE);
        }
    }

    private Subscriber resolveOrCreateSubscriber(byte[] hmac, CreateMobileNumber request) {
        return subscriberRepository.findByNationalIdHmac(hmac)
                .orElseGet(() -> {
                    String normalizedName = request.fullName().trim().replaceAll("\\s+", " ");
                    String last4 = request.nationalId().substring(request.nationalId().length() - 4);
                    Subscriber newSubscriber = Subscriber.create(hmac, last4, normalizedName);
                    return subscriberRepository.saveAndFlush(newSubscriber);
                });
    }

    private static MobileNumberResponse toResponse(MobileNumber mobileNumber) {
        return new MobileNumberResponse(
                mobileNumber.getPhoneNumber(),
                mobileNumber.getSubscriber().getNationalIdLast4(),
                mobileNumber.getServiceStatus().name(),
                mobileNumber.getCurrentOperatorSince().toString(),
                mobileNumber.getOriginOperator().getCode(),
                mobileNumber.getCurrentOperator().getCode()
        );
    }
}
