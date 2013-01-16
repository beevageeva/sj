package u;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SjProperties {

	private static Properties properties;
	
	
	static{
		properties = new Properties();
		try {
			properties.load(new FileInputStream("sj.properties"));
		} catch (FileNotFoundException e) {
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
