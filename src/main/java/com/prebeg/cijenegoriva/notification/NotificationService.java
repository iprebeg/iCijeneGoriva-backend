package com.prebeg.cijenegoriva.notification;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import com.prebeg.cijenegoriva.data.scraper.ScraperService;


@Component
public class NotificationService {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

	private JavaMailSender mailSender;
	
	@PostConstruct
	public void create () {
		mailSender = new JavaMailSenderImpl();
	}
	
	public void sendEmail(String subject, String body)
	{
		//System.out.println("failed:" + body);
		try { 
			SimpleMailMessage mail = new SimpleMailMessage();
			mail.setTo("ivor.prebeg@gmail.com");
			mail.setFrom("ivor@prebeg.com");
			mail.setSubject(subject);
			mail.setText(body);
			mailSender.send(mail);
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
}
