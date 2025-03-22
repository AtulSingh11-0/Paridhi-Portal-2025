package com.megatronix.paridhi.exception;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.megatronix.paridhi.dto.response.ErrorResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAllUncaughtException(
		Exception exception,
		WebRequest request,
		HttpServletRequest httpRequest
	) {
		log.error("Unknown error occurred", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
			.message("An unexpected error occurred")
			.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(httpRequest.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}


	/************************************************************** NOT FOUND EXCEPTION's **************************************************************/

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
		EntityNotFoundException exception,
		HttpServletRequest request
	) {
		log.error("Entity not found", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.NOT_FOUND.value())
			.message(exception.getMessage())
			.error(HttpStatus.NOT_FOUND.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(EventNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleEventNotFoundException(
		EventNotFoundException exception,
		HttpServletRequest request
	) {
		log.error("Event not found: ", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.NOT_FOUND.value())
			.message(exception.getMessage())
			.error(HttpStatus.NOT_FOUND.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(GIDNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleGIDNotFoundException(
		GIDNotFoundException exception,
		HttpServletRequest request
	) {
		log.error("GID not found: ", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.NOT_FOUND.value())
			.message(exception.getMessage())
			.error(HttpStatus.NOT_FOUND.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
		ResourceNotFoundException exception,
		HttpServletRequest request
	) {
		log.error("Resource not found", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.NOT_FOUND.value())
			.message(exception.getMessage())
			.error(HttpStatus.NOT_FOUND.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(TeamNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleTeamNotFoundException(
		TeamNotFoundException exception,
		HttpServletRequest request
	) {
		log.error("Team not found", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.NOT_FOUND.value())
			.message(exception.getMessage())
			.error(HttpStatus.NOT_FOUND.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFoundException(
		UserNotFoundException exception,
		HttpServletRequest request
	) {
		log.error("User not found", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.NOT_FOUND.value())
			.message(exception.getMessage())
			.error(HttpStatus.NOT_FOUND.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(
		@NonNull NoHandlerFoundException ex,
		@NonNull HttpHeaders headers,
		@NonNull HttpStatusCode status,
		@NonNull WebRequest request
	) {
		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.NOT_FOUND.value())
			.message(String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL()))
			.error(HttpStatus.NOT_FOUND.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getDescription(false).substring(4))
			.build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	/************************************************************** NOT FOUND EXCEPTION's **************************************************************/


	/************************************************************** BAD REQUEST EXCEPTION's **************************************************************/

	@ExceptionHandler(InvalidOtpException.class)
	public ResponseEntity<ErrorResponse> handleInvalidOtpException(
		InvalidOtpException exception,
		HttpServletRequest request
	) {
		log.error("Invalid OTP", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message(exception.getMessage())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<ErrorResponse> handleInvalidRequestException(
		InvalidRequestException exception,
		HttpServletRequest request
	) {
		log.error("Invalid request", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message(exception.getMessage())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(InvalidResetTokenException.class)
	public ResponseEntity<ErrorResponse> handleInvalidResetTokenException(
		InvalidResetTokenException exception,
		HttpServletRequest request
	) {
		log.error("Invalid reset token", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message(exception.getMessage())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(OtpExpiredException.class)
	public ResponseEntity<ErrorResponse> handleOtpExpiredException(
		OtpExpiredException exception,
		HttpServletRequest request
	) {
		log.error("OTP expired", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message(exception.getMessage())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<ErrorResponse> handleTokenExpiredException(
		TokenExpiredException exception,
		HttpServletRequest request
	) {
		log.error("Token expired", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message(exception.getMessage())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(
		ConstraintViolationException exception,
		HttpServletRequest request
	) {
		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message("Validation error")
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		exception.getConstraintViolations().forEach(violation -> {
			errorResponse.addValidationError(
				violation.getPropertyPath().toString(),
				violation.getMessage()
			);
		});

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		@NonNull MethodArgumentNotValidException ex,
		@NonNull HttpHeaders headers,
		@NonNull HttpStatusCode status,@NonNull WebRequest request
	) {
		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message("Validation error")
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getDescription(false).substring(4))
			.build();

		ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
			errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
		});

		return ResponseEntity.badRequest().body(errorResponse);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException exception,
		HttpServletRequest request
	) {
		log.error("Method argument type mismatch", exception);

		String expectedType = exception.getRequiredType() != null
			? exception.getRequiredType().getSimpleName()
			: "unknown";

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message("Invalid parameter type. Expected " +
				expectedType +
				" but got " + exception.getValue())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
		@NonNull HttpMessageNotReadableException ex,
		@NonNull HttpHeaders headers,
		@NonNull HttpStatusCode status,
		@NonNull WebRequest request
	) {
		log.error("Message not readable", ex);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message("Malformed JSON request: " + ex.getMessage())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getDescription(false).substring(4))
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
		@NonNull MissingServletRequestParameterException ex,
		@NonNull HttpHeaders headers,
		@NonNull HttpStatusCode status,
		@NonNull WebRequest request
	) {
		log.error("Missing parameter", ex);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message("Required parameter '" + ex.getParameterName() + "' is missing")
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getDescription(false).substring(4))
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	/************************************************************** BAD REQUEST EXCEPTION's **************************************************************/


	/************************************************************** FORBIDDEN EXCEPTION's **************************************************************/

	@ExceptionHandler(RegistrationClosedException.class)
	public ResponseEntity<ErrorResponse> handleRegistrationClosedException(
		RegistrationClosedException exception,
		HttpServletRequest request
	) {
		log.error("Registration closed", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.FORBIDDEN.value())
			.message(exception.getMessage())
			.error(HttpStatus.FORBIDDEN.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
	}

	@ExceptionHandler(TeamRegistrationException.class)
	public ResponseEntity<ErrorResponse> handleTeamRegistrationException(
		TeamRegistrationException exception,
		HttpServletRequest request
	) {
		log.error("Team registration error", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.FORBIDDEN.value())
			.message(exception.getMessage())
			.error(HttpStatus.FORBIDDEN.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(
		AccessDeniedException exception,
		HttpServletRequest request
	) {
		log.error("Access denied", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.FORBIDDEN.value())
			.message("You don't have permission to access this resource")
			.error(HttpStatus.FORBIDDEN.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
	}

	@ExceptionHandler(ForbiddenAccessException.class)
	public ResponseEntity<ErrorResponse> handleForbiddenAccessException(
		ForbiddenAccessException exception,
		HttpServletRequest request
	) {
		log.error("Forbidden access", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.FORBIDDEN.value())
			.message(exception.getMessage())
			.error(HttpStatus.FORBIDDEN.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
	}

	/************************************************************** FORBIDDEN EXCEPTION's **************************************************************/


	/************************************************************** CONFLICT EXCEPTION's **************************************************************/

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
		DuplicateResourceException exception,
		HttpServletRequest request
	) {
		log.error("Duplicate resource", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.CONFLICT.value())
			.message(exception.getMessage())
			.error(HttpStatus.CONFLICT.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
		UserAlreadyExistsException exception,
		HttpServletRequest request
	) {
		log.error("User already exists", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.CONFLICT.value())
			.message(exception.getMessage())
			.error(HttpStatus.CONFLICT.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(UserAlreadyVerifiedException.class)
	public ResponseEntity<ErrorResponse> handleUserAlreadyVerifiedException(
		UserAlreadyVerifiedException exception,
		HttpServletRequest request
	) {
		log.error("User already verified", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.CONFLICT.value())
			.message(exception.getMessage())
			.error(HttpStatus.CONFLICT.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
		DataIntegrityViolationException exception,
		HttpServletRequest request
	) {
		log.error("Data integrity violation", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.CONFLICT.value())
			.message("Data integrity violation: Operation cannot be performed")
			.error(HttpStatus.CONFLICT.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	/************************************************************** CONFLICT EXCEPTION's **************************************************************/


	/************************************************************** UNAUTHORIZED EXCEPTION's **************************************************************/

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentialsException(
		BadCredentialsException exception,
		HttpServletRequest request
	) {
		log.error("Authentication failed", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.UNAUTHORIZED.value())
			.message("Invalid username or password")
			.error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(
		AuthenticationException exception,
		HttpServletRequest request
	) {
		log.error("Authentication error", exception);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.UNAUTHORIZED.value())
			.message("Authentication failed: " + exception.getMessage())
			.error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	@ExceptionHandler(JwtException.class)
	public ResponseEntity<ErrorResponse> handleJwtException(
		JwtException exception,
		HttpServletRequest request
	) {
		log.error("JWT token error", exception);

		HttpStatus status = HttpStatus.UNAUTHORIZED;
		String message = "Invalid token";

		if (exception instanceof ExpiredJwtException) {
			message = "Token has expired";
		}

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(status.value())
			.message(message)
			.error(status.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(status).body(errorResponse);
	}

	/************************************************************** UNAUTHORIZED EXCEPTION's **************************************************************/


	/************************************************************** PAYLOAD TOO LARGE EXCEPTION's **************************************************************/

	@Override
	protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
		@NonNull MaxUploadSizeExceededException ex,
		@NonNull HttpHeaders headers,
		@NonNull HttpStatusCode status,
		@NonNull WebRequest request
	) {
		log.error("File size exceeds maximum allowed size", ex);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.PAYLOAD_TOO_LARGE.value())
			.message("File size exceeds the maximum allowed size")
			.error(HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getDescription(false).substring(4))
			.build();

		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
	}

	/************************************************************** PAYLOAD TOO LARGE EXCEPTION's **************************************************************/


	/************************************************************** PAYLOAD TOO LARGE EXCEPTION's **************************************************************/

	/************************************************************** METHOD NOT ALLOWED EXCEPTION's **************************************************************/

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
		@NonNull HttpRequestMethodNotSupportedException ex,
		@NonNull HttpHeaders headers,
		@NonNull HttpStatusCode status,
		@NonNull WebRequest request
	) {
		log.error("Method not allowed", ex);

		StringBuilder builder = new StringBuilder();
		builder.append(ex.getMethod());
		builder.append(" method is not supported for this request. Supported methods are ");
		if (ex.getSupportedHttpMethods() != null) {
			ex.getSupportedHttpMethods().forEach(method -> builder.append(method).append(" "));
		} else {
			builder.append("none");
		}

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.METHOD_NOT_ALLOWED.value())
			.message(builder.toString())
			.error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getDescription(false).substring(4))
			.build();

		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
	}

	/************************************************************** METHOD NOT ALLOWED EXCEPTION's **************************************************************/


	/************************************************************** UNSUPPORTED MEDIA TYPE EXCEPTION's **************************************************************/

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
		@NonNull HttpMediaTypeNotSupportedException ex,
		@NonNull HttpHeaders headers,
		@NonNull HttpStatusCode status,
		@NonNull WebRequest request
	) {
		log.error("Media type not supported", ex);

		StringBuilder builder = new StringBuilder();
		builder.append(ex.getContentType());
		builder.append(" media type is not supported. Supported media types are ");
		ex.getSupportedMediaTypes().forEach(mediaType -> builder.append(mediaType).append(", "));

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
			.message(builder.substring(0, builder.length() - 2))
			.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase())
			.timestamp(LocalDateTime.now())
			.path(request.getDescription(false).substring(4))
			.build();

		return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
	}

	/************************************************************** UNSUPPORTED MEDIA TYPE EXCEPTION's **************************************************************/

}
