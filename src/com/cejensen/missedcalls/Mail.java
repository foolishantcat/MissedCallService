package com.cejensen.missedcalls;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;

public class Mail extends javax.mail.Authenticator {
	private String		mUser;
	private String		mPass;

	private String[]	mTo;
	private String		mFrom;

	private String		mPort;
	private String		mSPort;

	private String		mHost;

	private String		mSubject;
	private String		mBody;

	private boolean		mAuth;

	private boolean		mDebuggable;

	private Multipart	mMultipart;

	public Mail() {
		mHost = "smtp.gmail.com"; // default smtp server
		mPort = "465"; // default smtp port
		mSPort = "465"; // default socketfactory port

		mUser = ""; // username
		mPass = ""; // password
		mFrom = ""; // email sent from
		mSubject = ""; // email subject
		mBody = ""; // email body

		mDebuggable = false; // debug mode on or off - default off
		mAuth = true; // smtp authentication - default on

		mMultipart = new MimeMultipart();

		// There is something wrong with MailCap, javamail can not find a
		// handler
		// for the multipart/mixed part, so this bit needs to be added.
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
	}

	public Mail(String user, String pass) {
		this();

		mUser = user;
		mPass = pass;
	}

	public boolean send() throws Exception {
		Properties props = _setProperties();

		if (!mUser.equals("") && !mPass.equals("") && mTo.length > 0 && !mFrom.equals("") && !mSubject.equals("")) {
			Session session = Session.getInstance(props, this);

			MimeMessage msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress(mFrom));

			InternetAddress[] addressTo = new InternetAddress[mTo.length];
			for (int i = 0; i < mTo.length; i++) {
				addressTo[i] = new InternetAddress(mTo[i]);
			}
			msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

			if (mSubject != null)
				msg.setSubject(mSubject);
			msg.setSentDate(new Date());

			// setup message body
			BodyPart messageBodyPart = new MimeBodyPart();
			if (mBody != null) {
				messageBodyPart.setText(mBody);
				mMultipart.addBodyPart(messageBodyPart);
				msg.setContent(mMultipart);
			}

			// send email
			Transport.send(msg);

			return true;
		} else {
			return false;
		}
	}

	public void addAttachment(String filename) throws Exception {
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(filename);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(filename);

		mMultipart.addBodyPart(messageBodyPart);
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(mUser, mPass);
	}

	private Properties _setProperties() {
		Properties props = new Properties();

		props.put("mail.smtp.host", mHost);

		if (mDebuggable) {
			props.put("mail.debug", "true");
		}

		if (mAuth) {
			props.put("mail.smtp.auth", "true");
		}

		props.put("mail.smtp.port", mPort);
		props.put("mail.smtp.socketFactory.port", mSPort);
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");

		return props;
	}

	// the getters and setters
	public String getBody() {
		return mBody;
	}

	public void setBody(String _body) {
		this.mBody = _body;
	}

	public void setTo(String[] toArr) {
		this.mTo = toArr;
	}

	public void setFrom(String string) {
		this.mFrom = string;
	}

	public void setSubject(String string) {
		this.mSubject = string;
	}
}
