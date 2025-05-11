package com.megatronix.paridhi.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.constant.Position;
import com.megatronix.paridhi.exception.MailSendingException;
import com.megatronix.paridhi.util.LoggingUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling all email communication in the application. Provides
 * asynchronous methods for sending different types of emails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
	@Value("${spring.mail.username}")
	private String fromEmail;
	private final JavaMailSender javaMailSender;

	/**
	 * Asynchronously sends a password reset token to a user
	 *
	 * @param to    The email address of the recipient
	 * @param name  The name of the recipient
	 * @param token The password reset token
	 * @return CompletableFuture for tracking completion
	 * @throws MailSendingException if email sending fails
	 */
	@Async
	public CompletableFuture<Void> sendPasswordResetToken(String to, String name, String token) {
		// log the operation
		LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
				"Sending password reset email to: " + to + ", with token: " + token.substring(0, 5) + "...");

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX + " - Password Reset Request");
			helper.setText(getResetTokenContent(name, token), true);

			javaMailSender.send(message);

			// log the successful email sending
			LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Successfully sent password reset email to: " + to);

			return CompletableFuture.completedFuture(null);
		} catch (MessagingException e) {
			LoggingUtil.logError(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Failed to send password reset email to: " + to + MessageConstant.EmailConstants.ERROR + e.getMessage(), e);
			throw new MailSendingException("Failed to send password reset email: " + e.getMessage(), e.getCause());
		}
	}

	/**
	 * Asynchronously sends an OTP verification code to a user
	 *
	 * @param to   The email address of the recipient
	 * @param name The name of the recipient
	 * @param otp  The one-time password to send
	 * @return CompletableFuture for tracking completion
	 * @throws MailSendingException if email sending fails
	 */
	@Async
	public CompletableFuture<Void> sendOtp(String to, String name, String otp) {
		// log the operation
		LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
				"Sending OTP email to: " + to);

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX + " - Email Verification OTP");
			helper.setText(getOtpContent(name, otp), true);

			javaMailSender.send(message);

			// log the successful email sending
			LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Successfully sent OTP email to: " + to);

			return CompletableFuture.completedFuture(null);
		} catch (MessagingException e) {
			LoggingUtil.logError(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Failed to send OTP email to: " + to + MessageConstant.EmailConstants.ERROR + e.getMessage(), e);
			throw new MailSendingException("Failed to send OTP email: " + e.getMessage(), e.getCause());
		}
	}

	/**
	 * Asynchronously sends a welcome email with MRD (Member Registration Details)
	 * to a user
	 *
	 * @param to   The email address of the recipient
	 * @param name The name of the recipient
	 * @param gid  The generated Paridhi ID (GID) for the user
	 * @return CompletableFuture for tracking completion
	 * @throws MailSendingException if email sending fails
	 */
	@Async
	public CompletableFuture<Void> sendMRDWelcome(String to, String name, String gid) {
		// log the operation
		LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
				"Sending MRD welcome email to: " + to + ", with GID: " + gid);

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX + " - Registration Confirmation");
			helper.setText(getMRDContent(name, gid), true);

			javaMailSender.send(message);

			// log the successful email sending
			LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Successfully sent MRD welcome email to: " + to);

			return CompletableFuture.completedFuture(null);
		} catch (MessagingException e) {
			LoggingUtil.logError(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Failed to send MRD welcome email to: " + to + MessageConstant.EmailConstants.ERROR + e.getMessage(), e);
			throw new MailSendingException("Failed to send MRD welcome email: " + e.getMessage(), e.getCause());
		}
	}

	/**
	 * Asynchronously sends event registration confirmation email to team members
	 *
	 * @param to        Array of email addresses of the recipients
	 * @param eventName Name of the event
	 * @param teamName  Name of the team
	 * @param tid       Team ID
	 * @return CompletableFuture for tracking completion
	 * @throws MailSendingException if email sending fails
	 */
	@Async
	public CompletableFuture<Void> sendEventRegistration(String[] to, String eventName, String teamName, String tid) {
		// log the operation
		LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
				"Sending event registration email to " + to.length + MessageConstant.EmailConstants.RECIPIENTS_FOR_EVENT
						+ eventName + MessageConstant.EmailConstants.TEAM + teamName);

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX + " - Event Registration Confirmation");
			helper.setText(getRDContent(teamName, eventName, tid), true);

			javaMailSender.send(message);

			// log the successful email sending
			LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Successfully sent event registration email to " + to.length + MessageConstant.EmailConstants.RECIPIENTS);

			return CompletableFuture.completedFuture(null);
		} catch (MessagingException e) {
			LoggingUtil.logError(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Failed to send event registration email to " + to.length + MessageConstant.EmailConstants.RECIPIENTS_ERROR
							+ e.getMessage(),
					e);
			throw new MailSendingException("Failed to send event registration email: " + e.getMessage(), e.getCause());
		}
	}

	/**
	 * Asynchronously sends qualification congratulations email to team members
	 *
	 * @param to        Array of email addresses of the recipients
	 * @param eventName Name of the event
	 * @param teamName  Name of the team
	 * @param tid       Team ID
	 * @return CompletableFuture for tracking completion
	 * @throws MailSendingException if email sending fails
	 */
	@Async
	public CompletableFuture<Void> sendQualificationCongratulations(String[] to, String eventName, String teamName,
			String tid) {
		// log the operation
		LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
				"Sending qualification congratulations to " + to.length + MessageConstant.EmailConstants.RECIPIENTS_FOR_EVENT
						+ eventName + MessageConstant.EmailConstants.TEAM + teamName);

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(
					MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX + " - Congratulations on Qualifying for Finals!");
			helper.setText(getQualificationContent(teamName, eventName, tid), true);

			javaMailSender.send(message);

			// log the successful email sending
			LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Successfully sent qualification congratulations to " + to.length
							+ MessageConstant.EmailConstants.RECIPIENTS);

			return CompletableFuture.completedFuture(null);
		} catch (MessagingException e) {
			LoggingUtil.logError(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Failed to send qualification congratulations to " + to.length
							+ MessageConstant.EmailConstants.RECIPIENTS_ERROR + e.getMessage(),
					e);
			throw new MailSendingException("Failed to send qualification congratulations: " + e.getMessage(), e.getCause());
		}
	}

	/**
	 * Asynchronously sends position/ranking congratulations email to team members
	 *
	 * @param to        Array of email addresses of the recipients
	 * @param eventName Name of the event
	 * @param teamName  Name of the team
	 * @param tid       Team ID
	 * @param position  The position achieved (FIRST, SECOND, THIRD, etc.)
	 * @return CompletableFuture for tracking completion
	 * @throws MailSendingException if email sending fails
	 */
	@Async
	public CompletableFuture<Void> sendPositionCongratulations(String[] to, String eventName, String teamName, String tid,
			Position position) {
		// log the operation
		LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
				"Sending position congratulations to " + to.length + MessageConstant.EmailConstants.RECIPIENTS_FOR_EVENT
						+ eventName + MessageConstant.EmailConstants.TEAM + teamName + ", position: " + position);

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX + " - Congratulations on Securing " + position
					+ " Position!");
			helper.setText(getPositionContent(teamName, eventName, tid, position), true);

			javaMailSender.send(message);

			// log the successful email sending
			LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Successfully sent position congratulations to " + to.length + MessageConstant.EmailConstants.RECIPIENTS);

			return CompletableFuture.completedFuture(null);
		} catch (MessagingException e) {
			LoggingUtil.logError(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Failed to send position congratulations to " + to.length + MessageConstant.EmailConstants.RECIPIENTS_ERROR
							+ e.getMessage(),
					e);
			throw new MailSendingException("Failed to send position congratulations: " + e.getMessage(), e.getCause());
		}
	}

	/**
	 * Asynchronously sends a query resolution email to a user
	 *
	 * @param to       The email address of the recipient
	 * @param name     The name of the recipient
	 * @param query    The original query submitted by the user
	 * @param response The response to the query
	 * @return CompletableFuture for tracking completion
	 * @throws MailSendingException if email sending fails
	 */
	@Async
	public CompletableFuture<Void> sendQueryResolution(String to, String name, String query, String response) {
		// log the operation
		LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
				"Sending query resolution to: " + to);

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX + " - Response to Your Query");
			helper.setText(getQueryResolutionContent(name, query, response), true);

			javaMailSender.send(message);

			// log the successful email sending
			LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Successfully sent query resolution to: " + to);

			return CompletableFuture.completedFuture(null);
		} catch (MessagingException e) {
			LoggingUtil.logError(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
					"Failed to send query resolution to: " + to + MessageConstant.EmailConstants.ERROR + e.getMessage(), e);
			throw new MailSendingException("Failed to send query resolution: " + e.getMessage(), e.getCause());
		}
	}

	/**
	 * Asynchronously sends profile credentials to a new user with improved reliability
	 *
	 * @param to       The email address of the recipient
	 * @param name     The name of the recipient
	 * @param password The temporary password assigned to the user
	 * @return CompletableFuture for tracking completion
	 * @throws MailSendingException if email sending fails
	 */
	@Async
	public CompletableFuture<Void> sendProfileCredentials(String to, String name, String password) {
			// Log the operation start
			log.info("Attempting to send profile credentials to: {} with password: {}", to, password);
			LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
							"Sending profile credentials to: " + to + ", with password: " + password);

			try {
					// Create a new MimeMessage with explicitly set encoding
					MimeMessage message = javaMailSender.createMimeMessage();
					MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
					
					// Set basic email properties
					helper.setFrom(fromEmail);
					helper.setTo(to);
					helper.setSubject(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX + " - Your Account Credentials");
					
					// Generate and set email content with explicit encoding
					String emailContent = getProfileCredentialsContent(name, to, password);
					message.setContent(emailContent, "text/html; charset=UTF-8");
					
					// Log before sending (helpful for debugging)
					log.debug("About to send email to: {}", to);
					
					// Send the email
					javaMailSender.send(message);
					
					// Log success
					log.info("Successfully sent profile credentials email to: {}", to);
					LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
									"Successfully sent profile credentials to: " + to);
									
					return CompletableFuture.completedFuture(null);
			} catch (MessagingException e) {
					// Log detailed error information
					log.error("Failed to send profile credentials email to: {}. Error: {}", to, e.getMessage(), e);
					LoggingUtil.logError(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
									"Failed to send profile credentials to: " + to + MessageConstant.EmailConstants.ERROR + e.getMessage(), e);
									
					// Throw exception to be handled by caller
					throw new MailSendingException("Failed to send profile credentials: " + e.getMessage(), e.getCause());
			} catch (Exception e) {
					// Catch any other unexpected errors
					log.error("Unexpected error while sending profile credentials email to: {}. Error: {}", to, e.getMessage(), e);
					LoggingUtil.logError(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
									"Unexpected error sending profile credentials to: " + to + ". Error: " + e.getMessage(), e);
									
					throw new MailSendingException("Unexpected error sending profile credentials: " + e.getMessage(), e.getCause());
			}
	}

	/**
	 * Asynchronously sends a consolidated registration email to a user
	 *
	 * @param to       The email address of the recipient
	 * @param name     The name of the recipient
	 * @param password The temporary password assigned to the user
	 * @param gid      The generated Paridhi ID (GID) for the user
	 * @return CompletableFuture for tracking completion
	 * @throws MailSendingException if email sending fails
	 */
	@Async
	public CompletableFuture<Void> sendConsolidatedRegistrationEmail(String to, String name, String password, String gid) {
			// Log the operation
			LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
							"Sending consolidated registration email to: " + to + ", Password: " + password + ", with GID: " + gid);

			try {
					MimeMessage message = javaMailSender.createMimeMessage();
					MimeMessageHelper helper = new MimeMessageHelper(message, true);
					helper.setFrom(fromEmail);
					helper.setTo(to);
					helper.setSubject(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX + " - Registration Confirmation & Credentials");
					
					// Combined HTML content with both profile credentials and GID
					String emailContent = getConsolidatedEmailContent(to, name, password, gid);
					helper.setText(emailContent, true);

					javaMailSender.send(message);

					// Log successful email sending
					LoggingUtil.logOperation(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
									"Successfully sent consolidated registration email to: " + to);

					return CompletableFuture.completedFuture(null);
			} catch (MessagingException e) {
					LoggingUtil.logError(log, MessageConstant.Operation.CREATE, AppConstant.EMAIL_SERVICE, null,
									"Failed to send consolidated registration email to: " + to + MessageConstant.EmailConstants.ERROR + e.getMessage(), e);
					throw new MailSendingException("Failed to send consolidated registration email: " + e.getMessage(), e.getCause());
			}
	}

	/**
	 * Generates HTML content for password reset token email
	 * 
	 * @param name  The name of the recipient
	 * @param token The password reset token
	 * @return Formatted HTML content for the email
	 */
	public String getResetTokenContent(String name, String token) {
		return """
				    <!DOCTYPE html>
				    <html lang="en">
				    <head>
				        <meta charset="UTF-8">
				        <meta name="viewport" content="width=device-width, initial-scale=1.0">
				        <title>%s - Password Reset Token</title>
				    </head>
				    <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: %s;">
				        <div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: %s;">
				            <div style="text-align: center; padding: 20px; background-color: %s; color: white; border-bottom: 3px solid %s;">
				                <h1 style="margin-top: 0; color: %s;">%s</h1>
				                <h2 style="margin-top: 0; color: %s;">Password Reset Request</h2>
				            </div>
				            <div style="padding: 25px; background-color: %s;">
				                <h3 style="margin-top: 0; color: %s;">Dear %s,</h3>
				                <p style="color: %s;">You requested a password reset for your %s account. Please use the following token to reset your password:</p>
				                <div style="background-color: %s; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
				                    <p style="margin: 0; font-size: 16px; color: %s;">Your password reset token is:</p>
				                    <div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
				                </div>
				                <p style="color: %s;">This token will expire in <span style="color: #FF6060;">10 minutes</span>.</p>
				                <p style="color: %s;">If you did not request a password reset, please ignore this email or contact support.</p>
				                <div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
				                    <p style="color: %s;">Regards,<br>Team %s</p>
				                </div>
				            </div>
				            <div style="text-align: center; padding: 10px; background-color: %s; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
				                <p style="color: %s;">© 2025 Paridhi. All rights reserved.</p>
				            </div>
				        </div>
				    </body>
				    </html>
				"""
				.formatted(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX.trim(), // title
						AppConstant.BACKGROUND_COLOR, // body background
						AppConstant.BACKGROUND_COLOR, // card background
						AppConstant.PRIMARY_COLOR, // header background
						AppConstant.BORDER_BOTTOM_COLOR, // border bottom
						AppConstant.H_COLOR, // h1 color
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // h1 text
						AppConstant.H_COLOR, // h2 color
						AppConstant.BACKGROUND_COLOR, // content background
						AppConstant.H3_TEXT_COLOR, // h3 color
						name, // recipient name
						AppConstant.TEXT_COLOR, // paragraph color
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // application name
						AppConstant.HEADER_BACKGROUND, // token box background
						AppConstant.SECONDARY_COLOR, // token box left border
						AppConstant.TEXT_COLOR, // token label color
						token, // the actual token
						AppConstant.TEXT_COLOR, // expiry text color
						AppConstant.TEXT_COLOR, // disclaimer color
						AppConstant.TEXT_COLOR, // signature color
						MessageConstant.EmailConstants.MEGATRONIX, // signature name
						AppConstant.HEADER_BACKGROUND, // footer background
						AppConstant.TEXT_COLOR // footer text color
				);
	}

	/**
	 * Generates HTML content for OTP verification email
	 * 
	 * @param name The name of the recipient
	 * @param otp  The one-time password
	 * @return Formatted HTML content for the email
	 */
	public String getOtpContent(String name, String otp) {
		return """
				<!DOCTYPE html>
				<html lang="en">
				<head>
				    <meta charset="UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1.0">
				    <title>%s - Email Verification</title>
				</head>
				<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: %s;">
				    <div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: %s;">
				        <div style="text-align: center; padding: 20px; background-color: %s; color: white; border-bottom: 3px solid %s;">
				            <h1 style="margin-top: 0; color: %s;">%s</h1>
				            <h2 style="margin-top: 0; color: %s;">Email Verification</h2>
				        </div>
				        <div style="padding: 25px; background-color: %s;">
				            <h3 style="margin-top: 0; color: %s;">Dear %s,</h3>
				            <p style="color: %s;">Thank you for registering for %s!</p>
				            <div style="background-color: %s; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
				                <p style="margin: 0; font-size: 16px; color: %s;">Your verification code is:</p>
				                <div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
				            </div>
				            <p style="color: %s;">Please enter this code to verify your email address. This code will expire in <strong>10 minutes</strong>.</p>
				            <p style="color: %s;">If you didn't request this verification, please ignore this email.</p>
				            <div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
				                <p style="color: %s;">Regards,<br>Team %s</p>
				            </div>
				        </div>
				        <div style="text-align: center; padding: 10px; background-color: %s; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
				            <p style="color: %s;">© 2025 Paridhi. All rights reserved.</p>
				        </div>
				    </div>
				</body>
				</html>
				"""
				.formatted(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX.trim(), // title
						AppConstant.BACKGROUND_COLOR, // body background
						AppConstant.BACKGROUND_COLOR, // card background
						AppConstant.PRIMARY_COLOR, // header background
						AppConstant.BORDER_BOTTOM_COLOR, // border bottom
						AppConstant.H_COLOR, // h1 color
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // h1 text
						AppConstant.H_COLOR, // h2 color
						AppConstant.BACKGROUND_COLOR, // content background
						AppConstant.H3_TEXT_COLOR, // h3 color
						name, // recipient name
						AppConstant.TEXT_COLOR, // paragraph color
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // application name
						AppConstant.HEADER_BACKGROUND, // OTP box background
						AppConstant.SECONDARY_COLOR, // OTP box left border
						AppConstant.TEXT_COLOR, // OTP label color
						otp, // the actual OTP
						AppConstant.TEXT_COLOR, // expiry text color
						AppConstant.TEXT_COLOR, // disclaimer color
						AppConstant.TEXT_COLOR, // signature color
						MessageConstant.EmailConstants.MEGATRONIX, // signature name
						AppConstant.HEADER_BACKGROUND, // footer background
						AppConstant.TEXT_COLOR // footer text color
				);
	}

	/**
	 * Generates HTML content for MRD (Member Registration Details) welcome email
	 * 
	 * @param name The name of the recipient
	 * @param gid  The Paridhi ID (GID) assigned to the user
	 * @return Formatted HTML content for the email
	 */
	public String getMRDContent(String name, String gid) {
		return """
				<!DOCTYPE html>
				<html lang="en">
				<head>
				    <meta charset="UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1.0">
				    <title>%s - Registration Confirmation</title>
				</head>
				<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: %s;">
				    <div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: %s;">
				        <div style="text-align: center; padding: 20px; background-color: %s; color: white; border-bottom: 3px solid %s;">
				            <h1 style="margin-top: 0; color: %s;">%s</h1>
				            <h2 style="margin-top: 0; color: %s;">General Registration Confirmation</h2>
				        </div>
				        <div style="padding: 25px; background-color: %s;">
				            <h3 style="margin-top: 0; color: %s;">Dear %s,</h3>
				            <p style="color: %s;">Thank you for registering for %s! We are thrilled to have you join us for the 13th edition of Paridhi, organized by Megatronix.</p>
				            <div style="background-color: %s; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
				                <p style="margin: 0; font-size: 16px; color: %s;">Your Paridhi ID (GID) is:</p>
				                <div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
				            </div>
				            <p style="color: %s;">This email contains your official registration credentials, which you need to present at the registration desk on the day of the event, to complete further formalities and participate in the event. This unique GID number will be required for participation in every event of Paridhi '25.</p>
				            <p style="color: %s;">We look forward to seeing you at Paridhi '25!</p>
				            <div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
				                <p style="color: %s;">Sincerely,<br>Team %s</p>
				            </div>
				        </div>
				        <div style="text-align: center; padding: 10px; background-color: %s; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
				            <p style="color: %s;">© 2025 Paridhi. All rights reserved.</p>
				        </div>
				    </div>
				</body>
				</html>
				"""
				.formatted(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX.trim(), // title
						AppConstant.BACKGROUND_COLOR, // body background
						AppConstant.BACKGROUND_COLOR, // card background
						AppConstant.PRIMARY_COLOR, // header background
						AppConstant.BORDER_BOTTOM_COLOR, // border bottom
						AppConstant.H_COLOR, // h1 color
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // h1 text
						AppConstant.H_COLOR, // h2 color
						AppConstant.BACKGROUND_COLOR, // content background
						AppConstant.H3_TEXT_COLOR, // h3 color
						name, // recipient name
						AppConstant.TEXT_COLOR, // paragraph color
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // application name
						AppConstant.HEADER_BACKGROUND, // GID box background
						AppConstant.SECONDARY_COLOR, // GID box left border
						AppConstant.TEXT_COLOR, // GID label color
						gid, // the actual GID
						AppConstant.TEXT_COLOR, // instruction text color
						AppConstant.TEXT_COLOR, // closing text color
						AppConstant.TEXT_COLOR, // signature color
						MessageConstant.EmailConstants.MEGATRONIX, // signature name
						AppConstant.HEADER_BACKGROUND, // footer background
						AppConstant.TEXT_COLOR // footer text color
				);
	}

	/**
	 * Generates HTML content for event registration confirmation email
	 * 
	 * @param teamName  The name of the team
	 * @param eventName The name of the event
	 * @param tid       The team ID
	 * @return Formatted HTML content for the email
	 */
	public String getRDContent(String teamName, String eventName, String tid) {
		return """
				    <!DOCTYPE html>
				    <html lang="en">
				    <head>
				        <meta charset="UTF-8">
				        <meta name="viewport" content="width=device-width, initial-scale=1.0">
				        <title>%s - Event Registration</title>
				    </head>
				    <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: %s;">
				        <div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: %s;">
				            <div style="text-align: center; padding: 20px; background-color: %s; color: white; border-bottom: 3px solid %s;">
				                <h1 style="margin-top: 0; color: %s;">%s</h1>
				                <h2 style="margin-top: 0; color: %s;">Event Registration Confirmation</h2>
				            </div>
				            <div style="padding: 25px; background-color: %s;">
				                <h3 style="margin-top: 0; color: %s;">Dear %s,</h3>
				                <p style="color: %s;">Thank you for registering for <span style="color: #FF6060;">%s</span> at %s!</p>
				                <div style="background-color: %s; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
				                    <p style="margin: 0; font-size: 16px; color: %s;">Your Team ID (TID) is:</p>
				                    <div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
				                </div>
				                <p style="color: %s;">We've received your registration and you're all set for the event. Make sure to keep it handy, as it will be essential for verifying your registration and moving forward with the event process. Kindly ensure all necessary formalities associated with your TID are completed at the earliest to avoid any last-minute issues.<br>Please keep an eye on your email for further updates about the event schedule and requirements.</p>
				                <p style="color: %s;">If you have any questions, feel free to contact the event coordinators.</p>
				                <div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
				                    <p style="color: %s;">Best of luck,<br>Team %s</p>
				                </div>
				            </div>
				            <div style="text-align: center; padding: 10px; background-color: %s; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
				                <p style="color: %s;">© 2025 Paridhi. All rights reserved.</p>
				            </div>
				        </div>
				    </body>
				    </html>
				"""
				.formatted(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX.trim(), // title
						AppConstant.BACKGROUND_COLOR, // body background
						AppConstant.BACKGROUND_COLOR, // card background
						AppConstant.PRIMARY_COLOR, // header background
						AppConstant.BORDER_BOTTOM_COLOR, // border bottom
						AppConstant.H_COLOR, // h1 color
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // h1 text
						AppConstant.H_COLOR, // h2 color
						AppConstant.BACKGROUND_COLOR, // content background
						AppConstant.H3_TEXT_COLOR, // h3 color
						teamName, // team name
						AppConstant.TEXT_COLOR, // paragraph color
						eventName, // event name
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // application name
						AppConstant.HEADER_BACKGROUND, // TID box background
						AppConstant.SECONDARY_COLOR, // TID box left border
						AppConstant.TEXT_COLOR, // TID label color
						tid, // the actual TID
						AppConstant.TEXT_COLOR, // instruction text color
						AppConstant.TEXT_COLOR, // closing text color
						AppConstant.TEXT_COLOR, // signature color
						MessageConstant.EmailConstants.MEGATRONIX, // signature name
						AppConstant.HEADER_BACKGROUND, // footer background
						AppConstant.TEXT_COLOR // footer text color
				);
	}

	/**
	 * Generates HTML content for qualification congratulations email
	 * 
	 * @param teamName  The name of the team
	 * @param eventName The name of the event
	 * @param tid       The team ID
	 * @return Formatted HTML content for the email
	 */
	public String getQualificationContent(String teamName, String eventName, String tid) {
		return """
				    <!DOCTYPE html>
				    <html lang="en">
				    <head>
				        <meta charset="UTF-8">
				        <meta name="viewport" content="width=device-width, initial-scale=1.0">
				        <title>%s - Qualified for Finals</title>
				    </head>
				    <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: %s;">
				        <div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: %s;">
				            <div style="text-align: center; padding: 20px; background-color: %s; color: white; border-bottom: 3px solid %s;">
				                <h1 style="margin-top: 0; color: %s;">%s</h1>
				                <h2 style="margin-top: 0; color: %s;">Congratulations!</h2>
				            </div>
				            <div style="padding: 25px; background-color: %s;">
				                <h3 style="margin-top: 0; color: %s;">Team %s,</h3>
				                <p style="color: %s;">Congratulations! Your team has <span style="color: %s; font-weight: bold;">qualified for the finals</span> of <span style="color: #FF6060;">%s</span> at %s!</p>
				                <div style="background-color: %s; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
				                    <p style="margin: 0; font-size: 16px; color: %s;">Your Team ID (TID):</p>
				                    <div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
				                </div>
				                <p style="color: %s;">We're impressed with your performance in the preliminary round and excited to see what you'll bring to the finals!</p>
				                <p style="color: %s;">Please keep an eye on your email for details about the final round schedule, venue, and any specific requirements.</p>
				                <p style="color: %s;">This is your moment to shine - good luck!</p>
				                <div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
				                    <p style="color: %s;">Best regards,<br>Team %s</p>
				                </div>
				            </div>
				            <div style="text-align: center; padding: 10px; background-color: %s; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
				                <p style="color: %s;">© 2025 Paridhi. All rights reserved.</p>
				            </div>
				        </div>
				    </body>
				    </html>
				"""
				.formatted(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX.trim(), // title
						AppConstant.BACKGROUND_COLOR, // body background
						AppConstant.BACKGROUND_COLOR, // card background
						AppConstant.PRIMARY_COLOR, // header background
						AppConstant.BORDER_BOTTOM_COLOR, // border bottom
						AppConstant.H_COLOR, // h1 color
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // h1 text
						AppConstant.H_COLOR, // h2 color
						AppConstant.BACKGROUND_COLOR, // content background
						AppConstant.H3_TEXT_COLOR, // h3 color
						teamName, // team name
						AppConstant.TEXT_COLOR, // paragraph color
						AppConstant.SECONDARY_COLOR, // qualified highlight color
						eventName, // event name
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // application name
						AppConstant.HEADER_BACKGROUND, // TID box background
						AppConstant.SECONDARY_COLOR, // TID box left border
						AppConstant.TEXT_COLOR, // TID label color
						tid, // the actual TID
						AppConstant.TEXT_COLOR, // message text color
						AppConstant.TEXT_COLOR, // instruction text color
						AppConstant.TEXT_COLOR, // closing text color
						AppConstant.TEXT_COLOR, // signature color
						MessageConstant.EmailConstants.MEGATRONIX, // signature name
						AppConstant.HEADER_BACKGROUND, // footer background
						AppConstant.TEXT_COLOR // footer text color
				);
	}

	/**
	 * Generates HTML content for position congratulations email
	 * 
	 * @param teamName  The name of the team
	 * @param eventName The name of the event
	 * @param tid       The team ID
	 * @param position  The position achieved (FIRST, SECOND, THIRD, etc.)
	 * @return Formatted HTML content for the email
	 */
	public String getPositionContent(String teamName, String eventName, String tid, Position position) {
		// Get appropriate medal emoji and color based on position
		String medal;
		String positionColor;
		String positionText = position.toString();

		switch (position) {
		case FIRST:
			medal = "🥇";
			positionColor = AppConstant.FIRST_PLACE_COLOR;
			positionText = "FIRST PLACE";
			break;
		case SECOND:
			medal = "🥈";
			positionColor = AppConstant.SECOND_PLACE_COLOR;
			positionText = "SECOND PLACE";
			break;
		case THIRD:
			medal = "🥉";
			positionColor = AppConstant.THIRD_PLACE_COLOR;
			positionText = "THIRD PLACE";
			break;
		default:
			medal = "🏆";
			positionColor = AppConstant.SECONDARY_COLOR;
			break;
		}

		return """
				<!DOCTYPE html>
				<html lang="en">
				<head>
				    <meta charset="UTF-8">
				    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1.0">
				    <title>%s - Congratulations on Your Win!</title>
				</head>
				<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: %s;">
				    <div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: %s;">
				        <div style="text-align: center; padding: 20px; background-color: %s; color: white; border-bottom: 3px solid %s;">
				            <h1 style="margin-top: 0; color: %s;">%s</h1>
				            <h2 style="margin-top: 0; color: %s;">Congratulations on Your Victory!</h2>
				        </div>
				        <div style="padding: 25px; background-color: %s;">
				            <h3 style="margin-top: 0; color: %s;">Team %s,</h3>
				            <p style="color: %s;">We're thrilled to announce that your team has secured <span style="color: %s; font-weight: bold;">%s %s</span> in <span style="color: #FF6060;">%s</span> at %s!</p>
				            <div style="background-color: %s; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
				                <p style="margin: 0; font-size: 16px; color: %s;">Your Team ID (TID):</p>
				                <div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
				            </div>
				            <p style="color: %s;">Your exceptional performance and dedication have paid off. This achievement is a testament to your hard work, creativity, and teamwork.</p>
				            <p style="color: %s;">You'll be contacted shortly regarding the prize distribution ceremony and certificates.</p>
				            <p style="color: %s;">Once again, congratulations on your outstanding achievement!</p>
				            <div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
				                <p style="color: %s;">With appreciation,<br>Team %s</p>
				            </div>
				        </div>
				        <div style="text-align: center; padding: 10px; background-color: %s; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
				            <p style="color: %s;">© 2025 Paridhi. All rights reserved.</p>
				        </div>
				    </div>
				</body>
				</html>
				"""
				.formatted(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX.trim(), // title
						AppConstant.BACKGROUND_COLOR, // body background
						AppConstant.BACKGROUND_COLOR, // card background
						AppConstant.PRIMARY_COLOR, // header background
						AppConstant.BORDER_BOTTOM_COLOR, // border bottom
						AppConstant.H_COLOR, // h1 color
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // h1 text
						AppConstant.H_COLOR, // h2 color
						AppConstant.BACKGROUND_COLOR, // content background
						AppConstant.H3_TEXT_COLOR, // h3 color
						teamName, // team name
						AppConstant.TEXT_COLOR, // paragraph color
						positionColor, // position color
						medal, // medal emoji
						positionText, // position text
						eventName, // event name
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // application name
						AppConstant.HEADER_BACKGROUND, // TID box background
						AppConstant.SECONDARY_COLOR, // TID box left border
						AppConstant.TEXT_COLOR, // TID label color
						tid, // the actual TID
						AppConstant.TEXT_COLOR, // congratulations text color
						AppConstant.TEXT_COLOR, // info text color
						AppConstant.TEXT_COLOR, // closing text color
						AppConstant.TEXT_COLOR, // signature color
						MessageConstant.EmailConstants.MEGATRONIX, // signature name
						AppConstant.HEADER_BACKGROUND, // footer background
						AppConstant.TEXT_COLOR // footer text color
				);
	}

	/**
	 * Generates HTML content for query resolution email
	 * 
	 * @param name     The name of the recipient
	 * @param query    The original query submitted by the user
	 * @param response The response to the query
	 * @return Formatted HTML content for the email
	 */
	public String getQueryResolutionContent(String name, String query, String response) {
		return """
				<!DOCTYPE html>
				<html lang="en">
				<head>
				    <meta charset="UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1.0">
				    <title>%s - Query Resolution</title>
				</head>
				<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: %s;">
				        <div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: %s;">
				                <div style="text-align: center; padding: 20px; background-color: %s; color: white; border-bottom: 3px solid %s;">
				                        <h1 style="margin-top: 0; color: %s;">%s</h1>
				                        <h2 style="margin-top: 0; color: %s;">Query Resolution</h2>
				                </div>
				                <div style="padding: 25px; background-color: %s;">
				                        <h3 style="margin-top: 0; color: %s;">Dear %s,</h3>
				                        <p style="color: %s;">Thank you for contacting us. Here is our response to your query:</p>
				                        <div style="background-color: %s; padding: 15px; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
				                                <p style="margin: 0; font-size: 16px; color: %s;"><strong>Your Query:</strong></p>
				                                <p style="color: %s; margin-top: 10px;">%s</p>
				                        </div>
				                        <div style="background-color: %s; padding: 15px; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
				                                <p style="margin: 0; font-size: 16px; color: %s;"><strong>Our Response:</strong></p>
				                                <p style="color: %s; margin-top: 10px;">%s</p>
				                        </div>
				                        <p style="color: %s;">If you have any further questions, please feel free to contact us again.</p>
				                        <div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
				                                <p style="color: %s;">Best regards,<br>Team %s</p>
				                        </div>
				                </div>
				                <div style="text-align: center; padding: 10px; background-color: %s; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
				                        <p style="color: %s;">© 2025 Paridhi. All rights reserved.</p>
				                </div>
				        </div>
				</body>
				</html>
				"""
				.formatted(MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX.trim(), // title
						AppConstant.BACKGROUND_COLOR, // body background
						AppConstant.BACKGROUND_COLOR, // card background
						AppConstant.PRIMARY_COLOR, // header background
						AppConstant.BORDER_BOTTOM_COLOR, // border bottom
						AppConstant.H_COLOR, // h1 color
						MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // h1 text
						AppConstant.H_COLOR, // h2 color
						AppConstant.BACKGROUND_COLOR, // content background
						AppConstant.H3_TEXT_COLOR, // h3 color
						name, // recipient name
						AppConstant.TEXT_COLOR, // paragraph color
						AppConstant.HEADER_BACKGROUND, // query box background
						AppConstant.SECONDARY_COLOR, // query box left border
						AppConstant.TEXT_COLOR, // query label color
						AppConstant.TEXT_COLOR, // query text color
						query, // the actual query
						AppConstant.HEADER_BACKGROUND, // response box background
						AppConstant.PRIMARY_COLOR, // response box left border
						AppConstant.TEXT_COLOR, // response label color
						AppConstant.TEXT_COLOR, // response text color
						response, // the actual response
						AppConstant.TEXT_COLOR, // closing text color
						AppConstant.TEXT_COLOR, // signature color
						MessageConstant.EmailConstants.MEGATRONIX, // signature name
						AppConstant.HEADER_BACKGROUND, // footer background
						AppConstant.TEXT_COLOR // footer text color
				);
	}

	/**
	 * Generates enhanced HTML content for profile credentials email
	 * 
	 * @param name     The name of the recipient
	 * @param email    The email address for login
	 * @param password The temporary password
	 * @return Formatted HTML content for the email
	 */
	public String getProfileCredentialsContent(String name, String email, String password) {
		return """
						<!DOCTYPE html>
						<html lang="en">
						<head>
								<meta charset="UTF-8">
								<meta name="viewport" content="width=device-width, initial-scale=1.0">
								<title>%s - Account Credentials</title>
						</head>
						<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: %s;">
								<div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: %s;">
										<div style="text-align: center; padding: 20px; background-color: %s; color: white; border-bottom: 3px solid %s;">
												<h1 style="margin-top: 0; color: %s;">%s</h1>
												<h2 style="margin-top: 0; color: %s;">Your Account Credentials</h2>
										</div>
										<div style="padding: 25px; background-color: %s;">
												<h3 style="margin-top: 0; color: %s;">Dear %s,</h3>
												<p style="color: %s;">Your account has been created for %s. Below are your login credentials:</p>
												
												<div style="background-color: %s; padding: 15px; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
														<p style="margin: 0; font-size: 16px; color: %s;"><strong>Email:</strong></p>
														<p style="color: %s; margin-top: 5px; word-break: break-all;">%s</p>
												</div>
												
												<div style="background-color: %s; padding: 15px; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
														<p style="margin: 0; font-size: 16px; color: %s;"><strong>Temporary Password:</strong></p>
														<p style="color: #B2FFBF; font-size: 18px; font-weight: bold; margin-top: 5px; letter-spacing: 1px;">%s</p>
												</div>
												
												<p style="color: %s;"><strong style="color: #FF6060;">IMPORTANT:</strong> To ensure the security of your account, we strongly recommend updating your password immediately after your first login. This helps keep your profile safe and ensures only you have access to your information.</p>
												
												<p style="color: %s;">You can use these credentials to log in through the Megatronix app and explore your profile, track your registrations, and stay updated with everything happening at Megatronix.</p>

												<p style="color: %s;">If you have any questions, please contact us.</p>
												
												<div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
														<p style="color: %s;">Regards,<br>Team %s</p>
												</div>
										</div>
										<div style="text-align: center; padding: 10px; background-color: %s; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
												<p style="color: %s;">© 2025 Paridhi. All rights reserved.</p>
										</div>
								</div>
						</body>
						</html>
						"""
		.formatted(
				MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX.trim(), // title
				AppConstant.BACKGROUND_COLOR, // body background
				AppConstant.BACKGROUND_COLOR, // card background
				AppConstant.PRIMARY_COLOR, // header background
				AppConstant.BORDER_BOTTOM_COLOR, // border bottom
				AppConstant.H_COLOR, // h1 color
				MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // h1 text
				AppConstant.H_COLOR, // h2 color
				AppConstant.BACKGROUND_COLOR, // content background
				AppConstant.H3_TEXT_COLOR, // h3 color
				name, // recipient name
				AppConstant.TEXT_COLOR, // paragraph color
				MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // application name
				AppConstant.HEADER_BACKGROUND, // email box background
				AppConstant.SECONDARY_COLOR, // email box left border
				AppConstant.TEXT_COLOR, // email label color
				AppConstant.TEXT_COLOR, // email value color
				email, // the actual email
				AppConstant.HEADER_BACKGROUND, // password box background
				AppConstant.PRIMARY_COLOR, // password box left border
				AppConstant.TEXT_COLOR, // password label color
				password, // the actual password
				AppConstant.TEXT_COLOR, // warning text color
				AppConstant.TEXT_COLOR, // info paragraph color
				AppConstant.TEXT_COLOR, // questions text color
				AppConstant.TEXT_COLOR, // signature color
				MessageConstant.EmailConstants.MEGATRONIX, // signature name
				AppConstant.HEADER_BACKGROUND, // footer background
				AppConstant.TEXT_COLOR // footer text color
		);
	}

	/**
	 * Generates HTML content for consolidated email with credentials
	 * 
	 * @param to       The email address of the recipient
	 * @param name     The name of the recipient
	 * @param password The temporary password
	 * @param gid      The GID (Global ID)
	 * @return Formatted HTML content for the email
	 */
	private String getConsolidatedEmailContent(String to, String name, String password, String gid) {
    return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s - Registration Confirmation & Credentials</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: %s;">
                <div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: %s;">
                    <div style="text-align: center; padding: 20px; background-color: %s; color: white; border-bottom: 3px solid %s;">
                        <h1 style="margin-top: 0; color: %s;">%s</h1>
                        <h2 style="margin-top: 0; color: %s;">Registration Confirmation & Credentials</h2>
                    </div>
                    <div style="padding: 25px; background-color: %s;">
                        <h3 style="margin-top: 0; color: %s;">Dear %s,</h3>
                        <p style="color: %s;">Thank you for registering for %s! We are thrilled to have you join us for the 13th edition of Paridhi, organized by Megatronix.</p>
                        
                        <!-- Account Credentials Section -->
                        <div style="background-color: %s; padding: 15px; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
                            <p style="margin: 0; font-size: 16px; color: %s;">
															<strong>Email:</strong> <span style="color: %s; word-break: break-all;">%s</span>
														</p>
                        </div>
                        
                        <div style="background-color: %s; padding: 15px; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
                            <p style="margin: 0; font-size: 16px; color: %s;">
															<strong>Temporary Password:</strong> <span style="color: #B2FFBF; font-size: 18px; font-weight: bold; letter-spacing: 1px;">%s</span>
														</p>
                        </div>
                        
                        <!-- GID Section -->
                        <div style="background-color: %s; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid %s;">
                            <p style="margin: 0; font-size: 16px; color: %s;">Your Paridhi ID (GID) is:</p>
                            <div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
                        </div>
                        
                        <p style="color: %s;"><strong style="color: #FF6060;">IMPORTANT:</strong> To ensure the security of your account, we strongly recommend updating your password immediately after your first login.</p>
                        
                        <p style="color: %s;">This email contains your official registration credentials, which you need to present at the registration desk on the day of the event. The unique GID number will be required for participation in every event of Paridhi '25.</p>

                        <p style="color: %s;">We look forward to seeing you at Paridhi '25!</p>
                        
                        <div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
                            <p style="color: %s;">Sincerely,<br>Team %s</p>
                        </div>
                    </div>
                    <div style="text-align: center; padding: 10px; background-color: %s; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
                        <p style="color: %s;">© 2025 Paridhi. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """
    .formatted(
            MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX.trim(), // title
            AppConstant.BACKGROUND_COLOR, // body background
            AppConstant.BACKGROUND_COLOR, // card background
            AppConstant.PRIMARY_COLOR, // header background
            AppConstant.BORDER_BOTTOM_COLOR, // border bottom
            AppConstant.H_COLOR, // h1 color
            MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // h1 text
            AppConstant.H_COLOR, // h2 color
            AppConstant.BACKGROUND_COLOR, // content background
            AppConstant.H3_TEXT_COLOR, // h3 color
            name, // recipient name
            AppConstant.TEXT_COLOR, // paragraph color
            MessageConstant.EmailConstants.EMAIL_SUBJECT_PREFIX, // application name
            AppConstant.HEADER_BACKGROUND, // email box background
            AppConstant.SECONDARY_COLOR, // email box left border
            AppConstant.TEXT_COLOR, // email label color
            AppConstant.TEXT_COLOR, // email value color
            to, // Fixed: the actual email - now using the correct parameter
            AppConstant.HEADER_BACKGROUND, // password box background
            AppConstant.PRIMARY_COLOR, // password box left border
            AppConstant.TEXT_COLOR, // password label color
            password, // the actual password
            AppConstant.HEADER_BACKGROUND, // GID box background
            AppConstant.SECONDARY_COLOR, // GID box left border
            AppConstant.TEXT_COLOR, // GID label color
            gid, // the actual GID
            AppConstant.TEXT_COLOR, // warning text color
            AppConstant.TEXT_COLOR, // info paragraph color
            AppConstant.TEXT_COLOR, // closing text color
            AppConstant.TEXT_COLOR, // signature color
            MessageConstant.EmailConstants.MEGATRONIX, // signature name
            AppConstant.HEADER_BACKGROUND, // footer background
            AppConstant.TEXT_COLOR // footer text color
    );
	}
}
