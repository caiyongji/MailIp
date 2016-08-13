import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailIp {
	private final static String USER = "**********@163.com";
	private final static String PASSWD = "**********";
	private final static String URL = "http://www.ip138.com/ip2city.asp";
	private final static String LOG_FILE_URL = "log-MailIp";
	private final static String SEND_TIME = "10:00:";
	private static boolean FORCE_EXC=true;
	public static void main(String[] args) {
		do {
			int index= nowTime().indexOf(SEND_TIME);
			if (FORCE_EXC||index>=0) {
				FORCE_EXC=false;
				run();
			}
			try {
				Thread.sleep(60000);
			} catch (Exception e) {
				FORCE_EXC=true;
			}
		} while (true);
	}

	private static void run() {
		String to = "**********@126.com";
		String from = "**********@163.com";
		String smptHost = "smtp.163.com";
		String computer=coumputer();
		String ip = getWebIp(URL);
		String subject = "[IP]"+computer+" : " + ip;
		String content = "\n"
				+ "[COMPUTER]  :"+computer+"\n"
				+ "[IP]        :" + ip + "\n"
				+ "                   本邮件由MailIp自动发送。\n"
				+ "                      ---@author CaiYongji";
		String result = sendMail(from, to, smptHost, subject, content);
		String fileContent="\n"
				+ "------------------------------\n"
				+ "TIME:\n"
				+nowTime()+"\n"
				+"COMPUTER:\n"
				+computer+"\n"
				+ "\nSUBJECT:\n"
				+subject+""
				+ "\nCONTENT:\n"
				+content+"\n"
				+ "RESULT:\n"
				+result+"\n";
		System.out.println("[SUCCESS]  ["+computer+"]  ["+ip+"]");
		writeFile("./"+LOG_FILE_URL, fileContent);
	}

	private static String sendMail(String from, String to, String smptHost,
			String subject, String content) {
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", smptHost);
		properties.put("mail.smtp.auth", "true");
		Session session = Session.getDefaultInstance(properties,
				new Authenticator() {
					@Override
					public PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(USER, PASSWD); // 发件人邮件用户名、密码
					}
				});
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));
			message.setSubject(subject);
			message.setText(content);
			Transport.send(message);
			return "SUCCESS";
		} catch (MessagingException e) {
			FORCE_EXC=true;
			return e.getMessage();
		}
	}

	private static String getWebIp(String strUrl) {
		try {

			URL url = new URL(strUrl);

			BufferedReader br = new BufferedReader(new InputStreamReader(url

			.openStream()));

			String s = "";

			StringBuffer sb = new StringBuffer("");

			String webContent = "";

			while ((s = br.readLine()) != null) {
				sb.append(s + "\r\n");
			}

			br.close();
			webContent = sb.toString();
			if (webContent.indexOf("[") == 0) {
				return webContent;
			}
			int start = webContent.indexOf("[") + 1;
			int end = webContent.indexOf("]");
			webContent = webContent.substring(start, end);
			return webContent;
		} catch (Exception e) {
			FORCE_EXC=true;
			return null;
		}
	}

	private static void writeFile(String file, String fileContent){
		BufferedWriter out=null;
		try {
			out= new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true)));
			out.write(fileContent);
			out.close();
		} catch (Exception e) {
			FORCE_EXC=true;
		}finally{
			try {
				out.close();
			} catch (Exception e2) {
				FORCE_EXC=true;
			}
		}
	}
	
	private static String nowTime() {
		Calendar calendar=Calendar.getInstance(Locale.CHINA);
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String now=format.format(calendar.getTime());
		return now;
	}
	private static String coumputer(){
		String computer=System.getenv().get("COMPUTERNAME");
		if (computer!=null) {
			return computer;
		}
		try {
			computer=InetAddress.getLocalHost().getHostName();
			return computer;
		} catch (UnknownHostException e) {
			return computer;
		}
	}
}
