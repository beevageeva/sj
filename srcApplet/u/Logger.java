package u;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;


public class Logger {

	public static void log(String log) {
		if(SjProperties.getPropertyAsBoolean("webApp")){
			try {
				// Construct data
				String data = "log=" + URLEncoder.encode(log, "UTF-8");
	
				// Create a socket to the host
				String hostname = Constants.host;
				int port = 80;
				InetAddress addr = InetAddress.getByName(hostname);
				Socket socket = new Socket(addr, port);
	
				// Send header
				String path = "/sj/getLogging.html";
				BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream(), "UTF-8"));
				wr.write("POST " + path + " HTTP/1.0\r\n");
				wr.write("Content-Length: " + data.length() + "\r\n");
				wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
				wr.write("\r\n");
	
				// Send data
				wr.write(data);
				wr.flush();
				wr.close();
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			System.out.println(log);
		}
	}
	
	private static String getExceptionString(Exception e){
		StringBuffer sb = new StringBuffer();
		if(e.getMessage()!=null){
			sb.append(e.getMessage());
			sb.append("\n");
		}
		sb.append(e.toString());
		sb.append("\n");
		StackTraceElement ste[] =e.getStackTrace();
		for(int i = 0 ; i<ste.length; i++){
			sb.append(ste[i].toString());
			sb.append("\n");
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public static void log(Exception e){
		log(getExceptionString(e));
	}

}
