package view.comp.run;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import javax.swing.SwingUtilities;

import u.DefTextField;
import u.Graph;
import view.model.Errors;


public class JFileChooserTF extends JFileChooser{

	private static final long serialVersionUID = 8787471995451507527L;
	
	protected int value;
	protected DefTextField inputTf;

	protected JDialog createDialog(Component parent) throws HeadlessException
	   {
	     Frame toUse = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
	     if(toUse==null){
	    	 throw new HeadlessException("Component has no frame parent");
	     }
	     JDialog dialog = new JDialog(toUse);
	     dialog.setSize(new Dimension(500 , 400));
	     setSelectedFile(null);
	     dialog.getContentPane().setLayout(new BorderLayout());
	     inputTf = new DefTextField(5);
	     dialog.getContentPane().add(Graph.createPanel(inputTf , "tUnits" , null) , BorderLayout.NORTH);
	     dialog.getContentPane().add(this , BorderLayout.CENTER);
	     dialog.setModal(true);
	     dialog.invalidate();
	     dialog.repaint();
	 
	     return dialog;
	   }

	@Override
	public void setSelectedFile(File f) {
		super.setSelectedFile(f);
		if(f!=null){
			if(validateTextField()){
				value = Integer.parseInt(inputTf.getText());
			}
			else{
				setSelectedFile(null);
			}
		}
	}
	
	public int getValue(){
		return value;
	}
	
	private boolean validateTextField(){
		Errors err = new Errors();
		inputTf.validateField("TUnits"  , err , 1,10000);
		if(!err.isEmpty()){
			JOptionPane.showMessageDialog(this , err.displayErrors());
			return false;
		}
		return true;
	}

	

	
	
}
