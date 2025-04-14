package com.megatronix.paridhi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.megatronix.paridhi.constant.Position;
import com.megatronix.paridhi.exception.MailSendingException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender javaMailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Async
  public void sendPasswordResetToken(String to, String token, String name) {
    log.info("Sending password reset token to {}", to);

    // create simple mail message
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject("Paridhi 2025 - Password Reset Token Request");
      helper.setText(getResetTokenContent(name, token), true);

      javaMailSender.send(message);
      log.info("Password reset token sent successfully to {}", to);
    } catch (MessagingException e) {
      log.error("Failed to send password reset token to {}", to, e);
      throw new MailSendingException("Failed to send password reset token " + e.getMessage(), e.getCause());
    }
  }

  @Async
  public void sendVerificationOtp(String to, String otp, String name) {
    log.info("Sending verification OTP to {}", to);

    // create simple mail message
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject("Paridhi 2025 - Email Verification");
      helper.setText(getOtpContent(name, otp), true);

      javaMailSender.send(message);
      log.info("Verification OTP sent successfully to {}", to);
    } catch (MessagingException e) {
      log.error("Failed to send verification OTP to {}", to, e);
      throw new MailSendingException("Failed to send verification OTP " + e.getMessage(), e.getCause());
    }
  }

  @Async
  public void sendMRDConfirmation(String to, String gid, String name) {
    log.info("Sending MRD registration confirmation to {}", to);

    // create simple mail message
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject("Paridhi 2025 - Registration Confirmation");
      helper.setText(getMRDContent(name, gid), true);

      javaMailSender.send(message);
      log.info("MRD Registration confirmation sent successfully to {}", to);
    } catch (MessagingException e) {
      log.error("Failed to send MRD registration confirmation to {}", to, e);
      throw new MailSendingException("Failed to send MRD registration confirmation " + e.getMessage(), e.getCause());
    }
  }

  @Async
  public void sendEventRegistration(String to, String eventName, String teamName, String tid) {
    log.info("Sending event registration confirmation to {}", to);

    // create simple mail message
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject("Paridhi 2025 - Event Registration Confirmation");
      helper.setText(getRDContent(teamName, eventName, tid), true);

      javaMailSender.send(message);
      log.info("Event registration confirmation sent successfully to {}", to);
    } catch (MessagingException e) {
      log.error("Failed to send event registration confirmation to {}", to, e);
      throw new MailSendingException("Failed to send event registration confirmation " + e.getMessage(), e.getCause());
    }
  }

	@Async
	public void sendEventRegistration(String[] to, String eventName, String teamName, String tid) {
		log.info("Sending event registration confirmation to {}", (Object)to);

		// create simple mail message
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject("Paridhi 2025 - Event Registration Confirmation");
			helper.setText(getRDContent(teamName, eventName, tid), true);

			javaMailSender.send(message);
			log.info("Event registration confirmation sent successfully to {}", (Object)to);
		} catch (MessagingException e) {
			log.error("Failed to send event registration confirmation to {}", to, e);
			throw new MailSendingException("Failed to send event registration confirmation " + e.getMessage(), e.getCause());
		}
	}

	@Async
	public void sendQualificationCongratulations(String[] to, String eventName, String teamName, String tid) {
		log.info("Sending qualification congratulations to {}", (Object)to);

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject("Paridhi 2025 - Congratulations on Qualifying for Finals!");
			helper.setText(getQualificationContent(teamName, eventName, tid), true);

			javaMailSender.send(message);
			log.info("Qualification congratulations sent successfully to {}", (Object)to);
		} catch (MessagingException e) {
			log.error("Failed to send qualification congratulations to {}", (Object)to, e);
			throw new MailSendingException("Failed to send qualification congratulations " + e.getMessage(), e.getCause());
		}
	}

	@Async
	public void sendPositionCongratulations(String[] to, String eventName, String teamName, String tid, Position position) {
		log.info("Sending position congratulations to {}", (Object)to);

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject("Paridhi 2025 - Congratulations on Your Achievement!");
			helper.setText(getPositionContent(teamName, eventName, tid, position), true);

			javaMailSender.send(message);
			log.info("Position congratulations sent successfully to {}", (Object)to);
		} catch (MessagingException e) {
			log.error("Failed to send position congratulations to {}", (Object)to, e);
			throw new MailSendingException("Failed to send position congratulations " + e.getMessage(), e.getCause());
		}
	}

	@Async
public void sendQueryResolution(String to, String name, String query, String response) {
	log.info("Sending query resolution to {}", to);

	try {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(fromEmail);
		helper.setTo(to);
		helper.setSubject("Paridhi 2025 - Response to Your Query");
		helper.setText(getQueryResolutionContent(name, query, response), true);

		javaMailSender.send(message);
		log.info("Query resolution sent successfully to {}", to);
	} catch (MessagingException e) {
		log.error("Failed to send query resolution to {}", to, e);
		throw new MailSendingException("Failed to send query resolution " + e.getMessage(), e.getCause());
	}
}

	public String getResetTokenContent(String name, String token) {
    return """
			<!DOCTYPE html>
			<html lang="en">
			<head>
					<meta charset="UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>Paridhi 2025 - Password Reset Token</title>
			</head>
			<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: #111111;">
				<div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: #222222;">
					<div style="text-align: center; padding: 20px; background-color: #1A1A1A; color: white; border-bottom: 3px solid #F73747;">
							<h1 style="margin-top: 0; color: #F73747;">Paridhi 2025</h1>
							<h2 style="margin-top: 0; color: #F73747;">Password Reset Request</h2>
					</div>
					<div style="padding: 25px; background-color: #222222;">
							<h3 style="margin-top: 0; color: #F73747;">Hello %s,</h3>
							<p style="color: #CCCCCC;">You requested a password reset for your Paridhi 2025 account. Please use the following token to reset your password:</p>
							<div style="background-color: #1A1A1A; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid #64C882;">
									<p style="margin: 0; font-size: 16px; color: #CCCCCC;">Your password reset token is:</p>
									<div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
							</div>
							<p style="color: #CCCCCC;">Enter this token in the password reset form to complete the process.</p>
							<p style="color: #CCCCCC;">This token will expire in <span style="color: #FF6060;">10 minutes</span>.</p>
							<p style="color: #CCCCCC;">If you did not request a password reset, please ignore this email or contact support.</p>
							<div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
									<p style="color: #CCCCCC;">Regards,<br>Paridhi 2025 Team</p>
							</div>
					</div>
					<div style="text-align: center; padding: 10px; background-color: #1A1A1A; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
							<p style="color: #CCCCCC;">© 2025 Paridhi. All rights reserved.</p>
					</div>
				</div>
			</body>
			</html>
		""".formatted(name, token);	
	}

	public String getOtpContent(String name, String otp) {
			return """
			<!DOCTYPE html>
			<html lang="en">
			<head>
					<meta charset="UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>Paridhi 2025 - Email Verification</title>
			</head>
			<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: #111111;">
				<div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: #222222;">
					<div style="text-align: center; padding: 20px; background-color: #1A1A1A; color: white; border-bottom: 3px solid #F73747;">
							<h1 style="margin-top: 0; color: #F73747;">Paridhi 2025</h1>
							<h2 style="margin-top: 0; color: #F73747;">Email Verification</h2>
					</div>
					<div style="padding: 25px; background-color: #222222;">
							<h3 style="margin-top: 0; color: #F73747;">Hello %s,</h3>
							<p style="color: #CCCCCC;">Thank you for registering for Paridhi 2025!</p>
							<div style="background-color: #1A1A1A; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid #64C882;">
									<p style="margin: 0; font-size: 16px; color: #CCCCCC;">Your verification code is:</p>
									<div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
							</div>
							<p style="color: #CCCCCC;">Please enter this code to verify your email address. This code will expire in <strong>10 minutes</strong>.</p>
							<p style="color: #CCCCCC;">If you didn't request this verification, please ignore this email.</p>
							<div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
									<p style="color: #CCCCCC;">Regards,<br>Paridhi 2025 Team</p>
							</div>
					</div>
					<div style="text-align: center; padding: 10px; background-color: #1A1A1A; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
							<p style="color: #CCCCCC;">© 2025 Paridhi. All rights reserved.</p>
					</div>
				</div>
			</body>
			</html>
			""".formatted(name, otp);
	}

	public String getMRDContent(String name, String gid) {
    return """
			<!DOCTYPE html>
			<html lang="en">
			<head>
					<meta charset="UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>Paridhi 2025 - Registration Confirmation</title>
			</head>
			<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: #111111;">
				<div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: #222222;">
					<div style="text-align: center; padding: 20px; background-color: #1A1A1A; color: white; border-bottom: 3px solid #F73747;">
							<h1 style="margin-top: 0; color: #F73747;">Paridhi 2025</h1>
							<h2 style="margin-top: 0; color: #F73747;">Registration Confirmation</h2>
					</div>
					<div style="padding: 25px; background-color: #222222;">
							<h3 style="margin-top: 0; color: #F73747;">Hello %s,</h3>
							<p style="color: #CCCCCC;">Thank you for registering for Paridhi 2025! We're excited to have you join us.</p>
							<div style="background-color: #1A1A1A; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid #64C882;">
									<p style="margin: 0; font-size: 16px; color: #CCCCCC;">Your Paridhi ID (GID) is:</p>
									<div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
							</div>
							<p style="color: #CCCCCC;">Please keep this ID handy for all Paridhi-related activities. You'll need it to register for events, claim certificates, and more.</p>
							<p style="color: #CCCCCC;">Looking forward to seeing you at the fest!</p>
							<div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
									<p style="color: #CCCCCC;">Regards,<br>Paridhi 2025 Team</p>
							</div>
					</div>
					<div style="text-align: center; padding: 10px; background-color: #1A1A1A; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
							<p style="color: #CCCCCC;">© 2025 Paridhi. All rights reserved.</p>
					</div>
				</div>
			</body>
			</html>
    """.formatted(name, gid);
	}

	public String getRDContent(String teamName, String eventName, String tid) {
    return """
			<!DOCTYPE html>
			<html lang="en">
			<head>
					<meta charset="UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>Paridhi 2025 - Event Registration</title>
			</head>
			<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: #111111;">
				<div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: #222222;">
					<div style="text-align: center; padding: 20px; background-color: #1A1A1A; color: white; border-bottom: 3px solid #F73747;">
							<h1 style="margin-top: 0; color: #F73747;">Paridhi 2025</h1>
							<h2 style="margin-top: 0; color: #F73747;">Event Registration Confirmation</h2>
					</div>
					<div style="padding: 25px; background-color: #222222;">
							<h3 style="margin-top: 0; color: #F73747;">Hello %s,</h3>
							<p style="color: #CCCCCC;">Thank you for registering for <span style="color: #FF6060;">%s</span> at Paridhi 2025!</p>
							<div style="background-color: #1A1A1A; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid #64C882;">
									<p style="margin: 0; font-size: 16px; color: #CCCCCC;">Your Team ID (TID) is:</p>
									<div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
							</div>
							<p style="color: #CCCCCC;">We've received your registration and you're all set for the event. Please keep an eye on your email for further updates about the event schedule and requirements.</p>
							<p style="color: #CCCCCC;">If you have any questions, feel free to contact the event coordinators.</p>
							<div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
									<p style="color: #CCCCCC;">Best of luck,<br>Paridhi 2025 Team</p>
							</div>
					</div>
					<div style="text-align: center; padding: 10px; background-color: #1A1A1A; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
							<p style="color: #CCCCCC;">© 2025 Paridhi. All rights reserved.</p>
					</div>
				</div>
			</body>
			</html>
    """.formatted(teamName, eventName, tid);
	}

	public String getQualificationContent(String teamName, String eventName, String tid) {
		return """
			<!DOCTYPE html>
			<html lang="en">
			<head>
					<meta charset="UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>Paridhi 2025 - Qualified for Finals</title>
			</head>
			<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: #111111;">
				<div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: #222222;">
					<div style="text-align: center; padding: 20px; background-color: #1A1A1A; color: white; border-bottom: 3px solid #F73747;">
							<h1 style="margin-top: 0; color: #F73747;">Paridhi 2025</h1>
							<h2 style="margin-top: 0; color: #F73747;">Congratulations!</h2>
					</div>
					<div style="padding: 25px; background-color: #222222;">
							<h3 style="margin-top: 0; color: #F73747;">Team %s,</h3>
							<p style="color: #CCCCCC;">Congratulations! Your team has <span style="color: #64C882; font-weight: bold;">qualified for the finals</span> of <span style="color: #FF6060;">%s</span> at Paridhi 2025!</p>
							<div style="background-color: #1A1A1A; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid #64C882;">
									<p style="margin: 0; font-size: 16px; color: #CCCCCC;">Your Team ID (TID):</p>
									<div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
							</div>
							<p style="color: #CCCCCC;">We're impressed with your performance in the preliminary round and excited to see what you'll bring to the finals!</p>
							<p style="color: #CCCCCC;">Please keep an eye on your email for details about the final round schedule, venue, and any specific requirements.</p>
							<p style="color: #CCCCCC;">This is your moment to shine - good luck!</p>
							<div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
									<p style="color: #CCCCCC;">Best regards,<br>Paridhi 2025 Team</p>
							</div>
					</div>
					<div style="text-align: center; padding: 10px; background-color: #1A1A1A; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
							<p style="color: #CCCCCC;">© 2025 Paridhi. All rights reserved.</p>
					</div>
				</div>
			</body>
			</html>
		""".formatted(teamName, eventName, tid);
	}
	
	public String getPositionContent(String teamName, String eventName, String tid, Position position) {
		// Get appropriate medal emoji and color based on position
		String medal;
		String positionColor;
		String positionText = position.toString();
		
		switch (position) {
			case FIRST:
				medal = "🥇";
				positionColor = "#FFD700"; // Gold
				positionText = "FIRST PLACE";
				break;
			case SECOND:
				medal = "🥈";
				positionColor = "#C0C0C0"; // Silver
				positionText = "SECOND PLACE";
				break;
			case THIRD:
				medal = "🥉";
				positionColor = "#CD7F32"; // Bronze
				positionText = "THIRD PLACE";
				break;
			default:
				medal = "🏆";
				positionColor = "#64C882"; // Green
				break;
		}
	
		return """
			<!DOCTYPE html>
			<html lang="en">
			<head>
					<meta charset="UTF-8">
					<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>Paridhi 2025 - Congratulations on Your Win!</title>
			</head>
			<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: #111111;">
				<div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: #222222;">
					<div style="text-align: center; padding: 20px; background-color: #1A1A1A; color: white; border-bottom: 3px solid #F73747;">
							<h1 style="margin-top: 0; color: #F73747;">Paridhi 2025</h1>
							<h2 style="margin-top: 0; color: #F73747;">Congratulations on Your Victory!</h2>
					</div>
					<div style="padding: 25px; background-color: #222222;">
							<h3 style="margin-top: 0; color: #F73747;">Team %s,</h3>
							<p style="color: #CCCCCC;">We're thrilled to announce that your team has secured <span style="color: %s; font-weight: bold;">%s %s</span> in <span style="color: #FF6060;">%s</span> at Paridhi 2025!</p>
							<div style="background-color: #1A1A1A; padding: 15px; text-align: center; border-radius: 4px; margin: 20px 0; border-left: 4px solid #64C882;">
									<p style="margin: 0; font-size: 16px; color: #CCCCCC;">Your Team ID (TID):</p>
									<div class="code" style="font-size: 24px; font-weight: bold; color: #B2FFBF; margin: 10px 0; letter-spacing: 2px;">%s</div>
							</div>
							<p style="color: #CCCCCC;">Your exceptional performance and dedication have paid off. This achievement is a testament to your hard work, creativity, and teamwork.</p>
							<p style="color: #CCCCCC;">You'll be contacted shortly regarding the prize distribution ceremony and certificates.</p>
							<p style="color: #CCCCCC;">Once again, congratulations on your outstanding achievement!</p>
							<div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
									<p style="color: #CCCCCC;">With appreciation,<br>Paridhi 2025 Team</p>
							</div>
					</div>
					<div style="text-align: center; padding: 10px; background-color: #1A1A1A; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
							<p style="color: #CCCCCC;">© 2025 Paridhi. All rights reserved.</p>
					</div>
				</div>
			</body>
			</html>
		""".formatted(teamName, positionColor, medal, positionText, eventName, tid);
	}

	public String getQueryResolutionContent(String name, String query, String response) {
    return """
		<!DOCTYPE html>
		<html lang="en">
		<head>
				<meta charset="UTF-8">
				<meta name="viewport" content="width=device-width, initial-scale=1.0">
				<title>Paridhi 2025 - Query Resolution</title>
		</head>
		<body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; color: #FFFFFF; background-color: #111111;">
				<div class="card" style="border: 1px solid #333333; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.3); background-color: #222222;">
						<div style="text-align: center; padding: 20px; background-color: #1A1A1A; color: white; border-bottom: 3px solid #F73747;">
								<h1 style="margin-top: 0; color: #F73747;">Paridhi 2025</h1>
								<h2 style="margin-top: 0; color: #F73747;">Query Resolution</h2>
						</div>
						<div style="padding: 25px; background-color: #222222;">
								<h3 style="margin-top: 0; color: #F73747;">Hello %s,</h3>
								<p style="color: #CCCCCC;">Thank you for contacting us. Here is our response to your query:</p>
								<div style="background-color: #1A1A1A; padding: 15px; border-radius: 4px; margin: 20px 0; border-left: 4px solid #64C882;">
										<p style="margin: 0; font-size: 16px; color: #CCCCCC;"><strong>Your Query:</strong></p>
										<p style="color: #CCCCCC; margin-top: 10px;">%s</p>
								</div>
								<div style="background-color: #1A1A1A; padding: 15px; border-radius: 4px; margin: 20px 0; border-left: 4px solid #F73747;">
										<p style="margin: 0; font-size: 16px; color: #CCCCCC;"><strong>Our Response:</strong></p>
										<p style="color: #CCCCCC; margin-top: 10px;">%s</p>
								</div>
								<p style="color: #CCCCCC;">If you have any further questions, please feel free to contact us again.</p>
								<div style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #333333; color: #999999;">
										<p style="color: #CCCCCC;">Best regards,<br>Paridhi 2025 Team</p>
								</div>
						</div>
						<div style="text-align: center; padding: 10px; background-color: #1A1A1A; font-size: 12px; color: #777; border-radius: 0 0 4px 4px;">
								<p style="color: #CCCCCC;">© 2025 Paridhi. All rights reserved.</p>
						</div>
				</div>
		</body>
		</html>
		""".formatted(name, query, response);
	}
}
