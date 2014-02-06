package u;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SjProperties {

	private static Properties properties;
	
	
	static{
		properties = new Properties();
		try {
			InputStream is;
			//won't work in a jar
			//is = new FileInputStream("sj.properties");
			is = SjProperties.class.getClassLoader().getResourceAsStream("sj.properties");
			properties.load(is);
		} catch (FileNotFoundException e) {
			System.out.println("working directory : " + System.getProperty("user.dir"));
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean getPropertyAsBoolean(String propertyName){
		try{
			return Boolean.parseBoolean(properties.getProperty(propertyName));
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
}
