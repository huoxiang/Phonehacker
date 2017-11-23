package com.aa.aaa;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class EmailUtil {

	class MyAuthenticator extends javax.mail.Authenticator {
		private String strUser;
		private String strPwd;

		public MyAuthenticator(String user, String password) {
			this.strUser = user;
			this.strPwd = password;
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(strUser, strPwd);
		}
	}

	public void sendMail(String toMail, String fromMail, String server,
			String username, String password, String title, String body,
			String attachment) throws Exception {

		Properties props = System.getProperties();// Get system properties

		props.put("mail.smtp.host", server);// Setup mail server

		props.put("mail.smtp.auth", "true");

		MyAuthenticator myauth = new MyAuthenticator(username, password);// Get

		Session session = Session.getDefaultInstance(props, myauth);

		MimeMessage message = new MimeMessage(session); // Define message

		message.setFrom(new InternetAddress(fromMail)); // Set the from address

		message.addRecipient(Message.RecipientType.TO, new InternetAddress(
				toMail));// Set
		// address
		message.setSubject(title);// Set the subject

		// message.setText(MimeUtility.encodeWord(body));// Set the content

		MimeMultipart allMultipart = new MimeMultipart("mixed");

		MimeBodyPart attachPart = new MimeBodyPart();
		FileDataSource fds = new FileDataSource(attachment);
		attachPart.setDataHandler(new DataHandler(fds));
		attachPart.setFileName(MimeUtility.encodeWord(fds.getName()));

		MimeBodyPart textBodyPart = new MimeBodyPart();
		textBodyPart.setText(body);

		allMultipart.addBodyPart(attachPart);
		allMultipart.addBodyPart(textBodyPart);
		message.setContent(allMultipart);
		message.saveChanges();
		Transport.send(message);
	}
}