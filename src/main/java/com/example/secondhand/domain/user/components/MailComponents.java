package com.example.secondhand.domain.user.components;

import static com.example.secondhand.global.exception.CustomErrorCode.SEND_EMAIL_FAIL;

import com.example.secondhand.global.exception.CustomException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MailComponents {
	private final JavaMailSender javaMailSender;

	public void sendMail(String mail, String subject, String text){
		MimeMessagePreparator msg = new MimeMessagePreparator() {
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				mimeMessageHelper.setTo(mail);
				mimeMessageHelper.setSubject(subject);
				mimeMessageHelper.setText(text, true);
			}
		};
		try{
			javaMailSender.send(msg);
		} catch (Exception e){
			throw new CustomException(SEND_EMAIL_FAIL);
		}
	}
}
