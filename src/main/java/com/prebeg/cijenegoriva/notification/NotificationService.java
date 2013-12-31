package com.prebeg.cijenegoriva.notification;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;

import com.prebeg.cijenegoriva.data.scraper.ScraperService;


@Component
public class NotificationService {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

  private static String SMTP_HOST_NAME;
  private static String SMTP_AUTH_USER;
  private static String SMTP_AUTH_PWD; 

	@PostConstruct
	public void create () {
    SMTP_HOST_NAME = "smtp.sendgrid.net";
    SMTP_AUTH_USER = System.getenv("SENDGRID_USERNAME");
    SMTP_AUTH_PWD  = System.getenv("SENDGRID_PASSWORD");
	}
	
  public void sendEmail(String subject, String body)
  {
      try 
      {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.port", 587);
        props.put("mail.smtp.auth", "true");

        Authenticator auth = new SMTPAuthenticator();
        Session mailSession = Session.getDefaultInstance(props, auth);
        // uncomment for debugging infos to stdout
        // mailSession.setDebug(true);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
      
        Multipart multipart = new MimeMultipart("alternative");
  
        BodyPart bodypart = new MimeBodyPart();
        bodypart.setText(body);

        multipart.addBodyPart(bodypart);

        message.setContent(multipart);
        message.setFrom(new InternetAddress("icijenegoriva@icijenegoriva.herokuapp.com"));
        message.setSubject(subject);
        message.addRecipient(Message.RecipientType.TO,
            new InternetAddress("ivor.prebeg@gmail.com"));

        transport.connect();
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        transport.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            String username = SMTP_AUTH_USER;
            String password = SMTP_AUTH_PWD;
            return new PasswordAuthentication(username, password);
        }
    }
}

