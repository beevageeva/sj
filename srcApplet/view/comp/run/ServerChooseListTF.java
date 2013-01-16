package view.comp.run;

import java.awt.Component;
import java.io.IOException;
import java.net.MalformedURLException;

public class ServerChooseListTF extends ServerChooseList {

	public ServerChooseListTF(Component parent, String urlString) {
		super(parent, urlString);
	}

	@Override
	public String getName() {
		int sIndex = ListDialogTF.getSelectedIndex();
		if(sIndex!=-1){
			return values[sIndex];
		}
		return null;
	}
	
	public int getValue(){
		return ListDialogTF.getValue();
	}

	@Override
	public void showDialog(Component locationComp, String labelText, String title, int initialIndex, String longValue) {
		try {
			values = getValues();
			ListDialogTF.showDialog(parent, locationComp, labelText,
					title, values, initialIndex, longValue , "Ok");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
}
