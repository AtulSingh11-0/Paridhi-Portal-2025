package com.megatronix.paridhi.constant;

public final class MessageConstant {
	private MessageConstant() {}
	
	/**
	 * Generic user-facing messages that don't expose implementation details
	 */
	public static final class UserMessage {
		private UserMessage() {}

		public static final String GENERIC_ERROR = "An unexpected error occurred. Please try again later or contact support.";
		public static final String RESOURCE_NOT_FOUND = "The requested resource was not found.";
		public static final String ACCESS_DENIED = "You don't have permission to access this resource.";
		public static final String VALIDATION_ERROR = "Please check your input and try again.";
		public static final String CONFLICT_ERROR = "The request could not be completed due to a conflict with current state.";
		public static final String AUTH_ERROR = "Authentication failed. Please check your credentials.";
		public static final String TOKEN_EXPIRED = "Your session has expired. Please log in again.";
		public static final String REGISTRATION_CLOSED = "Registration for this event is currently closed.";
		public static final String TEAM_ERROR = "There was an issue with your team. Please check the details and try again.";
		public static final String RATE_LIMIT_EXCEEDED = "Too many requests. Please try again later.";
	}
	
	/**
	 * Detailed log messages for debugging and monitoring
	 */
	public static final class LogMessage {
		private LogMessage() {}

		// Auth messages
		public static final String AUTH_LOGIN_ATTEMPT = "Login attempt for user: {}";
		public static final String AUTH_LOGIN_SUCCESS = "Login successful for user: {} (ID: {})";
		public static final String AUTH_LOGIN_FAILURE = "Login failed for user: {} - Reason: {}";
		
		// Event messages
		public static final String EVENT_CREATED = "Event created: {} (ID: {}) by user: {} (ID: {})";
		public static final String EVENT_UPDATED = "Event ID: {} updated by user: {} (ID: {}) - Fields changed: {}";
		public static final String EVENT_DELETED = "Event ID: {} deleted by user: {} (ID: {})";
		public static final String EVENT_NOT_FOUND = "Event not found with ID: {}";
		
		// Team messages
		public static final String TEAM_CREATED = "Team created: {} (ID: {}) by user: {} (ID: {}) for event: {} (ID: {})";
		public static final String TEAM_UPDATED = "Team ID: {} updated by user: {} (ID: {})";
		public static final String TEAM_NOT_FOUND = "Team not found with ID: {}";
		
		// Contact query messages
		public static final String CONTACT_QUERY_CREATED = "Contact query received from: {}, contact: {}, subject: {}";
		public static final String CONTACT_QUERY_RESOLVED = "Contact query ID: {} resolved by user: {} (ID: {})";
		
		// User profile messages
		public static final String PROFILE_CREATED = "Profile created for user: {} (ID: {})";
		public static final String PROFILE_UPDATED = "Profile updated for user: {} (ID: {})";
		
		// Error messages
		public static final String ERROR_GENERIC = "Error occurred: {} - Context: {}";
		public static final String ERROR_ACCESS_DENIED = "Access denied for user: {} (ID: {}) - Attempted to: {} - Resource: {}";
	}
	
	/**
	 * Operation types for consistent logging
	 */
	public static final class Operation {
		private Operation() {}
		
		public static final String CREATE = "create";
		public static final String READ = "read";
		public static final String UPDATE = "update";
		public static final String DELETE = "delete";
		public static final String AUTHORIZE = "authorize";
		public static final String AUTHENTICATE = "authenticate";
		public static final String REGISTER = "register";
		public static final String RESOLVE = "resolve";
	}
}