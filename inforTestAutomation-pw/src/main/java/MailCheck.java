import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

public class MailCheck {

	public static void main(String[] args) throws EmailException {
		MultiPartEmail email = new MultiPartEmail();
		email.setHostName("smtp.gmail.com");
		email.setSmtpPort(465);
		email.setSSLOnConnect(true);
		email.setAuthentication("<SMTP_USERNAME>", "<SMTP_PASSWORD>");
		email.setFrom("<SENDER_EMAIL>");
		email.addTo("<RECIPIENT_EMAIL>");
		email.setSubject(" - Execution results");
		email.setMsg("Detailed Execution results \n");
		email.send();
		System.out.println("Email sent with Report html Attached");
	}
}