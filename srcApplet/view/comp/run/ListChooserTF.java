package view.comp.run;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import u.DefTextField;
import u.Graph;
import view.model.Errors;

public class ListChooserTF extends ListChooser{
	
	private static final long serialVersionUID = 1L;
	protected DefTextField inputText;
	protected int textValue;

	public ListChooserTF(Frame frame, Component locationComp, String labelText, String title, Object[] data, int initialIndex, String longValue,  String okLabel) {
		super(frame, locationComp, labelText, title, data, initialIndex, longValue,
				okLabel);
		inputText = new DefTextField(5);
		getContentPane().add(Graph.createPanel(inputText , "TUnits" , null) , BorderLayout.NORTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (!"cancel".equalsIgnoreCase(e.getActionCommand())) {
			if(validateTextField()){
				textValue = Integer.parseInt(inputText.getText());
			}
			else{
				selectedIndex = -1;
			}
		}
	}
	
	private boolean validateTextField(){
		Errors err = new Errors();
		inputText.validateField("TUnits"  , err , 1,10000);
		if(!err.isEmpty()){
			JOptionPane.showMessageDialog(this , err.displayErrors());
			return false;
		}
		return true;
	}
	
	
	public int getTextValue(){
		return textValue;
	}
	

}
