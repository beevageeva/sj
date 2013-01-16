package u;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

import view.model.Errors;

public class DefTextField extends JTextField{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int ncol;
	
	public DefTextField(int ncol) {
		super(ncol);
		this.ncol = ncol;
		addKeyListener(new DigitTextFieldKeyAdapter());
	}
	
	public boolean validateFieldLength(String fieldname , Errors err ){
		if(getText().length() == 0){
			err.addError(fieldname + " field cannot be empty");
			return false;
		}
		if(getText().length()>ncol){
			err.addError(fieldname + " field cannot have more than " + ncol + " digits");
			return false;
		}
		return true;
	}
	
	
	public boolean validateField(String fieldname , Errors err , int minValue , int maxValue){
		if(validateFieldLength(fieldname , err)){
			int val ;
			try{
				val = Integer.parseInt(getText());
				if(val<minValue){
					err.addError(fieldname + " field must be ge than " + minValue);
					return false;
				}
				else{
					if(val>maxValue){
						err.addError(fieldname + " field must be le than " + maxValue);
						return false;
					}
					
				}
			}
			catch(NumberFormatException e){
				err.addError(fieldname + " field is not a number");
				return false;
			}
			
		}
		return true;
	}
	
	class DigitTextFieldKeyAdapter extends KeyAdapter{

	    public void keyTyped(KeyEvent e) {
	        char c = e.getKeyChar();      
	        if (!((Character.isDigit(c) ||
	           (c == KeyEvent.VK_BACK_SPACE) ||
	           (c == KeyEvent.VK_DELETE)))) {
	          e.consume();
	        }
	      }

	}

}
