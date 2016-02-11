package org.greengin.nquireit.logic.mail;

import org.greengin.nquireit.entities.users.UserProfile;

import java.util.List;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.logging.log4j.LogManager;
import org.springframework.scheduling.annotation.Async;

/**
 * Send a message to multiple recipients.
 */
public class Mailer {
    private static String smtpHost;
    private static String sender;
    private static String name;
    
    public static void setSMTPServer(String smtpServer, String sender, String name) {
        Mailer.smtpHost = smtpServer;
        Mailer.sender = sender;
        Mailer.name = name;
    }

    @Async
    public static boolean sendMail(String subject, String message, List<UserProfile> recipients, boolean useBcc) {
        if (recipients.isEmpty()) {
            LogManager.getLogger(Mailer.class).warn("Sending message '" + subject + "' - but no recipients!");
            return true;
        }

        try {
            System.out.println("Sending mail via SMTP host " + smtpHost);
            Properties properties = new Properties();
            properties.put("mail.smtp.host", Mailer.smtpHost);
            Session session = Session.getInstance(properties, null);
            session.setDebug(true);
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Mailer.sender, Mailer.name));
            msg.setSubject("[" + Mailer.name + "] " + subject);
            msg.setText(message);

            for(UserProfile recipient: recipients) {
                if (recipient.getEmail().isEmpty()) {
                    LogManager.getLogger(Mailer.class).warn("User " + recipient.getUsername() + " does not have an email address");
                } else if (useBcc) {
                    System.out.println("BCC: " + recipient.getEmail());
                    msg.addRecipient(MimeMessage.RecipientType.BCC, new InternetAddress(recipient.getEmail(),recipient.getUsername()));
                } else {
                    System.out.println("To: " + recipient.getEmail());
                    msg.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(recipient.getEmail(),recipient.getUsername()));
                }
            }

            System.out.println("Sent message: " + subject);
            Transport.send(msg);
            return true;
        } catch (UnsupportedEncodingException ex) {
            LogManager.getLogger(Mailer.class).error("An error occured while sending an email: " + ex);
            return false;
        } catch (MessagingException ex) {
            LogManager.getLogger(Mailer.class).error("An error occured while sending an email: " + ex);
            return false;
        }
    }
}
