package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.request.ContactQueryRequest;
import com.megatronix.paridhi.dto.request.ResolveQueryRequest;
import com.megatronix.paridhi.dto.response.ContactQueryResponse;
import com.megatronix.paridhi.exception.ContactQueryNotFoundException;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.model.ContactQuery;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.ContactQueryRepository;
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactQueryService {
	private final EmailService emailService;
	private final ContactQueryRepository contactQueryRepository;
	private static final String CONTACT_QUERY = " Contact Query";
	private static final String CONTACT_QUERY_NOT_FOUND = "Contact query not found with ID: ";

	/*
	 * public methods - doesn't require authentication
	 */

	// @CacheEvict(value = "contactQueries", allEntries = true)
	@CacheEvict(
		value = {
			"contactQueries", 
			"contactQueriesByResolved", 
			"contactQueryById"
		}, 
		allEntries = true
	)
	public ContactQueryResponse createContactQuery(ContactQueryRequest request) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.CONTACT_QUERY,
			null,
			"Creating contact query for: " + request.getName() + ", email: " + request.getEmail()
		);
		
		// create a contact query builder object
		ContactQuery contactQuery = ContactQuery.builder()
			.name(request.getName())
			.email(request.getEmail())
			.contact(request.getContact())
			.query(request.getQuery())
			.resolved(false)
			.build();
		
		// save the contact query to the database
		ContactQuery savedQuery = contactQueryRepository.save(contactQuery);
		
		// log the successful creation of the contact query
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.CONTACT_QUERY,
			null,
			"Contact query created successfully with ID: " + savedQuery.getId()
		);
		
		// return the ContactQueryResponse object
		return ContactQueryResponse.fromContactQuery(savedQuery);
	}
	
	/*
	 * protected methods - requires authentication
	 */

	@Cacheable(value = "contactQueries")
	public List<ContactQueryResponse> getAllQueries(User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.CONTACT_QUERY,
			user,
			"Fetching all contact queries"
		);
		
		// validate user access
		checkUserAccess(user, MessageConstant.Operation.READ);

		// fetch all contact queries from the database - sorted by createdAt in descending order
		List<ContactQuery> queries = contactQueryRepository.findByOrderByCreatedAtDesc();
		
		// log the successful retrieval of contact queries
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.CONTACT_QUERY,
			user,
			"Successfully retrieved " + queries.size() + " contact queries"
		);
		
		// return the list of ContactQueryResponse objects
		return queries.stream()
			.map(ContactQueryResponse::fromContactQuery)
			.toList();
	}
	
	@Cacheable(value = "contactQueriesByResolved", key = "#resolved")
	public List<ContactQueryResponse> getQueriesByResolutionStatus(boolean resolved, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.CONTACT_QUERY,
			user,
			"Fetching contact queries with resolved status: " + (resolved ? AppConstant.RESOLVED : AppConstant.UNRESOLVED)
		);
		
		// validate user access
		checkUserAccess(user, MessageConstant.Operation.READ);

		// fetch contact queries by resolution status from the database - sorted by createdAt in descending order
		List<ContactQuery> queries = contactQueryRepository.findByResolvedOrderByCreatedAtDesc(resolved);
		
		// log the successful retrieval of contact queries
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.CONTACT_QUERY,
			user,
			"Successfully retrieved " + queries.size() + " " + (resolved ? AppConstant.RESOLVED : AppConstant.UNRESOLVED) + " contact queries"
		);
		
		// return the list of ContactQueryResponse objects
		return queries.stream()
			.map(ContactQueryResponse::fromContactQuery)
			.toList();
	}
	
	@Cacheable(value = "contactQueryById", key = "#id")
	public ContactQueryResponse getQueryById(Long id, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.CONTACT_QUERY,
			user,
			"Fetching contact query with ID: " + id
		);
		
		// validate user access
		checkUserAccess(user, MessageConstant.Operation.READ);

		// fetch contact query by ID from the database
		ContactQuery query = contactQueryRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.READ,
					AppConstant.CONTACT_QUERY,
					user,
					CONTACT_QUERY_NOT_FOUND + id,
					null
				);
				return new ContactQueryNotFoundException(CONTACT_QUERY_NOT_FOUND + id);
			});
		
		// log the successful retrieval of the contact query
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.CONTACT_QUERY,
			user,
			"Successfully retrieved contact query with ID: " + id
		);

		// return the ContactQueryResponse object
		return ContactQueryResponse.fromContactQuery(query);
	}
	
	@CacheEvict(
		value = {
			"contactQueries", 
			"contactQueriesByResolved", 
			"contactQueryById"
		}, 
		allEntries = true
	)
	@Transactional
	public ContactQueryResponse resolveQuery(Long id, ResolveQueryRequest request, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.RESOLVE, 
			AppConstant.CONTACT_QUERY, 
			user, 
			"Resolving query ID: " + id
		);

		// validate user access
		checkUserAccess(user, MessageConstant.Operation.RESOLVE);
		
		// fetch contact query by ID from the database
		ContactQuery query = contactQueryRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.READ,
					AppConstant.CONTACT_QUERY, 
					user,
					CONTACT_QUERY_NOT_FOUND + id,
					null
				);
				return new ContactQueryNotFoundException(CONTACT_QUERY_NOT_FOUND + id);
			});
		
		// Business logic...
		query.setResolved(true);
		query.setResponse(request.getResponse());
		query.setResolvedBy(user);

		// save the updated contact query to the database
		ContactQuery updatedQuery = contactQueryRepository.save(query);
		
		// send email with the response
		emailService.sendQueryResolution(query.getEmail(), query.getName(), query.getQuery(), request.getResponse());
		
		// log the successful resolution of the contact query
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.RESOLVE,
			AppConstant.CONTACT_QUERY,
			user,
			"Successfully resolved query ID: " + id + " for email: " + query.getEmail()
		);
		
		// return the ContactQueryResponse object
		return ContactQueryResponse.fromContactQuery(updatedQuery);
	}

	/*
	 * private methods - used internally only
	 */
	private void checkUserAccess(User user, String methodType) {
		// check if user is null
		if (user == null) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				null,
				AppConstant.FAILED,
				"Authorization required to " + methodType + CONTACT_QUERY
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
    }
    
		// check if user has ROLE_USER
    if (user.getRole().equals(Role.ROLE_USER)) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				user,
				AppConstant.FAILED,
				"User lacks permission to " + methodType + CONTACT_QUERY
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
    }
    
		// log the successful authorization
    LoggingUtil.logSecurity(
			log,
			AppConstant.ACCESS_GRANTED,
			user,
			AppConstant.SUCCESS,
			"User authorized to " + methodType + CONTACT_QUERY
    );
	}
}