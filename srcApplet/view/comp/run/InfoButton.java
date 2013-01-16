package view.comp.run;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import u.Constants;
import u.SjProperties;

public class InfoButton extends JButton implements ActionListener {

	private static final long serialVersionUID = 1L;
	private Component popupParent;
	private String message;
	
	
	public InfoButton(Component popupParent , String message){
		super();
		try {
			//if(SjProperties.getPropertyAsBoolean("webApp")){
				setIcon(new ImageIcon(new URL("http://"+Constants.host +"/" + Constants.context + "/images/info.gif")));
			//}
			//else{
			//	setIcon(new ImageIcon("info.gif"));
			//}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			setText("info");
		}
		this.popupParent = popupParent;
		this.message = message;
		addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(popupParent , message);
	}

}
