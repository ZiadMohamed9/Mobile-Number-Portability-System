package com.fourgtss.mnp.repository;

import com.fourgtss.mnp.models.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Optional<Subscriber> findByNationalIdHmac(byte[] nationalIdHmac);
}
