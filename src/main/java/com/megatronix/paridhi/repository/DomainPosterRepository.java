package com.megatronix.paridhi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.model.DomainPoster;

@Repository
public interface DomainPosterRepository extends JpaRepository<DomainPoster, Long> {
	DomainPoster findByDomainName(Domain domainName);	
}
