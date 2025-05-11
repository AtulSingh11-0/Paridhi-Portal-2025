package com.megatronix.paridhi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.ContactQuery;

@Repository
public interface ContactQueryRepository extends JpaRepository<ContactQuery, Long> {
	List<ContactQuery> findByIsResolved(boolean isResolved);
	List<ContactQuery> findByOrderByCreatedAtDesc();
	List<ContactQuery> findByIsResolvedOrderByCreatedAtDesc(boolean isResolved);
}