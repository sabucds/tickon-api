package com.tickon.identity.auth.infrastructure.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import com.tickon.identity.auth.application.ports.out.EmailSender;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SendGridEmailSender implements EmailSender {

  private static final Logger log = LoggerFactory.getLogger(SendGridEmailSender.class);

  private final SendGrid sendGrid;
  private final EmailProperties emailProperties;

  public SendGridEmailSender(EmailProperties emailProperties) {
    if (emailProperties.getApiKey() == null || emailProperties.getApiKey().isBlank()) {
      throw new IllegalStateException("SendGrid API key is not configured");
    }
    this.sendGrid = new SendGrid(emailProperties.getApiKey());
    this.emailProperties = emailProperties;
  }

  @Override
  public void sendPasswordResetEmail(com.tickon.common.identity.domain.valueobjects.Email to, String resetToken,
      String recipientName) {
    String resetUrl = emailProperties.getResetUrlBase() + "?token=" + resetToken;

    Email from = new Email(emailProperties.getFromEmail(), emailProperties.getFromName());
    Email toEmail = new Email(to.value(), recipientName);
    String subject = "Reset Your Password";

    String htmlContent = buildHtmlContent(resetUrl, recipientName);
    String textContent = buildTextContent(resetUrl, recipientName);

    Mail mail = new Mail();
    mail.setFrom(from);
    mail.setSubject(subject);

    Personalization personalization = new Personalization();
    personalization.addTo(toEmail);
    mail.addPersonalization(personalization);

    // Order matters for SendGrid:
    mail.addContent(new Content("text/plain", textContent));
    mail.addContent(new Content("text/html", htmlContent));

    try {
      Request request = new Request();
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());

      Response response = sendGrid.api(request);

      if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
        log.info("Password reset email sent successfully to {}", to.value());
      } else {
        log.error("Failed to send password reset email to {}. Status: {}, Body: {}", to.value(),
            response.getStatusCode(), response.getBody());
      }
    } catch (IOException e) {
      log.error("Error sending password reset email to {}", to.value(), e);
    }
  }

  private String buildHtmlContent(String resetUrl, String recipientName) {
    return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .button { display: inline-block; padding: 12px 24px; background-color: #007bff;
                          color: white; text-decoration: none; border-radius: 4px; }
                .footer { margin-top: 30px; font-size: 12px; color: #666; }
            </style>
        </head>
        <body>
            <div class="container">
                <h2>Password Reset Request</h2>
                <p>Hi %s,</p>
                <p>We received a request to reset your password. Click the button below to create a new password:</p>
                <p><a href="%s" class="button">Reset Password</a></p>
                <p>Or copy and paste this link into your browser:</p>
                <p><a href="%s">%s</a></p>
                <p>This link will expire in 1 hour.</p>
                <p>If you didn't request a password reset, you can safely ignore this email.</p>
                <div class="footer">
                    <p>Thanks,<br>The Tickon Team</p>
                </div>
            </div>
        </body>
        </html>
        """, recipientName, resetUrl, resetUrl, resetUrl);
  }

  private String buildTextContent(String resetUrl, String recipientName) {
    return String.format("""
        Hi %s,

        We received a request to reset your password.

        Click this link to reset your password:
        %s

        This link will expire in 1 hour.

        If you didn't request a password reset, you can safely ignore this email.

        Thanks,
        The Tickon Team
        """, recipientName, resetUrl);
  }
}
