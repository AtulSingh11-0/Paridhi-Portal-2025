package com.megatronix.paridhi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.EventCombo;
import com.megatronix.paridhi.constant.Domain;

@Repository
public interface ComboRepository extends JpaRepository<EventCombo, Long> {
  List<EventCombo> findByDomain(Domain domain);
  List<EventCombo> findByIsRegistrationOpen(boolean isRegistrationOpen);
}
