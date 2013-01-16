package view.comp.run;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class ServerChooseList {

	protected Component parent;

	protected String urlString;
	
	protected String[] values;

	public ServerChooseList(Component parent, String urlString) {
		this.parent = parent;
		this.urlString = urlString;
	}

	public void showDialog(Component locationComp, String labelText,
			String title,  int initialIndex,
			String longValue) {

		try {
			values = getValues();
			ListDialog.showDialog(parent, locationComp, labelText,
					title, values, initialIndex, longValue , "Ok");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getName(){
		int sIndex = ListDialog.getSelectedIndex();
		if(sIndex!=-1){
			return values[sIndex];
		}
		return null;
	}

	protected String[] getValues() throws MalformedURLException, IOException {
		new URL(urlString).openStream();
		InputStream is = new URL(urlString).openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		ArrayList<String> val = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			val.add(line);
		}
		is.close();
		
		return (String[]) val.toArray(new String[val.size()]);

	}

}
