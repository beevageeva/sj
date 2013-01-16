package view.comp.config;

import javax.swing.JOptionPane;
import javax.swing.JPanel;


import view.model.Errors;

public abstract class  ConfigPanel extends JPanel{
	
	private boolean inputEnabled = true;
	
	public void setInputFieldsEnabled(boolean enable){
		inputEnabled = enable;
		setFieldsEnabled(enable);
	}
	
	public boolean exitPanel(){
		if(inputEnabled){
			Errors err = new Errors();
			validateFields(err);
			if(err.isEmpty()){
				saveFields();
				return true;
			}
			else{
				JOptionPane.showMessageDialog(this,  err.displayErrors(),"errors", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}
	
	
	
	protected abstract void saveFields();
	
	public abstract void setFieldsEnabled(boolean enable);
	
	protected abstract void validateFields(Errors err);
	
	public abstract void updateFieldsFromConfiguration();
	
}
