package utils;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

public class EmailTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Email email = EmailBuilder.startingBlank()
			    .from("Giovanni Perrone", "gperrone71@yahoo.it")
			    .to("Me", "gperrone71@gmail.com")
			    .withSubject("Questa è una mail di test")
			    .withPlainText("Test testone!")
			    .buildEmail();

			MailerBuilder
			  .withSMTPServer("smtp.mail.yahoo.com", 465, "gperrone71@yahoo.it", "shdehqroqqrssrvo")
			  .withTransportStrategy(TransportStrategy.SMTPS)
			  .buildMailer()
			  .sendMail(email);
		
	}

}
