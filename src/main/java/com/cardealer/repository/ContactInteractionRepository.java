package com.cardealer.repository;

import com.cardealer.model.ContactInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactInteractionRepository extends JpaRepository<ContactInteraction, Long> {

    List<ContactInteraction> findByDealerIdOrderByTimestampDesc(Long dealerId);
}
