package com.megatronix.paridhi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.request.ContactQueryRequest;
import com.megatronix.paridhi.dto.request.ResolveQueryRequest;
import com.megatronix.paridhi.dto.response.ContactQueryResponse;
import com.megatronix.paridhi.exception.ContactQueryNotFoundException;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.exception.InvalidRequestException;
import com.megatronix.paridhi.model.ContactQuery;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.ContactQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactQueryService {
	private final ContactQueryRepository contactQueryRepository;
	private final EmailService emailService;
	
	@CacheEvict(value = "contactQueries", allEntries = true)
	public ContactQueryResponse createContactQuery(ContactQueryRequest request) {
		log.info("Creating contact query for: {}, email: {}", request.getName(), request.getEmail());
		
		ContactQuery contactQuery = ContactQuery.builder()
			.name(request.getName())
			.email(request.getEmail())
			.contact(request.getContact())
			.query(request.getQuery())
			.resolved(false)
			.build();
		
		ContactQuery savedQuery = contactQueryRepository.save(contactQuery);
		log.info("Contact query created successfully with ID: {}", savedQuery.getId());
		
		return ContactQueryResponse.fromContactQuery(savedQuery);
	}
	
	@Cacheable(value = "contactQueries")
	public List<ContactQueryResponse> getAllQueries() {
		log.info("Fetching all contact queries");
		return contactQueryRepository.findByOrderByCreatedAtDesc().stream()
			.map(ContactQueryResponse::fromContactQuery)
			.collect(Collectors.toList());
	}
	
	@Cacheable(value = "contactQueriesByResolved", key = "#resolved")
	public List<ContactQueryResponse> getQueriesByResolutionStatus(boolean resolved) {
		log.info("Fetching contact queries with resolved status: {}", resolved);
		return contactQueryRepository.findByResolvedOrderByCreatedAtDesc(resolved).stream()
			.map(ContactQueryResponse::fromContactQuery)
			.collect(Collectors.toList());
	}
	
	@Cacheable(value = "contactQueryById", key = "#id")
	public ContactQueryResponse getQueryById(Long id) {
		log.info("Fetching contact query with ID: {}", id);
		ContactQuery query = contactQueryRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Contact query not found with ID: {}", id);
				return new ContactQueryNotFoundException("Contact query not found with ID: " + id);
			});
		
		return ContactQueryResponse.fromContactQuery(query);
	}
	
	@CacheEvict(value = {"contactQueries", "contactQueriesByResolved", "contactQueryById"}, allEntries = true)
	@Transactional
	public ContactQueryResponse resolveQuery(Long id, ResolveQueryRequest request, User user) {
		log.info("Resolving contact query with ID: {}, by user: {}", id, user.getEmail());
		
		// Check if user has admin rights
		if (user.getRole() != Role.ROLE_ADMIN && user.getRole() != Role.ROLE_SUPERADMIN) {
			log.error("User {} does not have admin rights to resolve queries", user.getEmail());
			throw new ForbiddenAccessException("You don't have permission to resolve queries");
		}
		
		ContactQuery query = contactQueryRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Contact query not found with ID: {}", id);
				return new ContactQueryNotFoundException("Contact query not found with ID: " + id);
			});
		
		if (query.isResolved()) {
			log.warn("Contact query with ID: {} is already resolved", id);
			throw new InvalidRequestException("Query is already resolved");
		}
		
		query.setResolved(true);
		query.setResponse(request.getResponse());
		query.setResolvedBy(user);
		
		ContactQuery updatedQuery = contactQueryRepository.save(query);
		log.info("Contact query resolved successfully with ID: {}", updatedQuery.getId());
		
		// Send email with the response
		emailService.sendQueryResolution(query.getEmail(), query.getName(), query.getQuery(), request.getResponse());
		
		return ContactQueryResponse.fromContactQuery(updatedQuery);
	}
}