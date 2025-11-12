package com.inmapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.from:no-reply@localhost}")
  private String from;

  public void enviarHtml(String para, String asunto, String html) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
      helper.setFrom(from);
      helper.setTo(para);
      helper.setSubject(asunto);
      helper.setText(html, true);
      mailSender.send(message);
    } catch (Exception e) {
      throw new RuntimeException("No se pudo enviar el correo", e);
    }
  }
  
  public void enviar(String para, String asunto, String htmlBody, String txtBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); 

            helper.setFrom(from);
            helper.setTo(para);
            helper.setSubject(asunto);
            helper.setText(txtBody, htmlBody);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el correo", e);
        }
    }
}

