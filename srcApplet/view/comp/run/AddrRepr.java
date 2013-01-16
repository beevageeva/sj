package view.comp.run;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import u.Helper;


public class AddrRepr extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField[] labels;
	private JLabel titleLabel;
	
	public AddrRepr(int numberFields , String label){
		super();
		setSize(new Dimension(200,50));
		setLayout(new BorderLayout());
		titleLabel = new JLabel(label);
		add(titleLabel , BorderLayout.NORTH);
		titleLabel.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent e) {
				showText();
			}});
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		JPanel p = new JPanel(new GridLayout(2,2*numberFields));
		labels = new JTextField[2*numberFields];
		p.add(new JLabel("B"));
		for(int i =0;i<2*numberFields;i++){
			labels[i]= new JTextField(4);
			//labels[i].setEditable(false);
		}
		for(int i = 0;i<numberFields-1;i++){
			p.add(labels[2*i]);
			p.add(new JSeparator(JSeparator.VERTICAL));
		}
		p.add(labels[2*numberFields-2]);
		p.add(new JLabel("D"));
		for(int i = 0;i<numberFields-1;i++){
			p.add(labels[2*i+1]);
			p.add(new JSeparator(JSeparator.VERTICAL));
		}
		p.add(labels[2*numberFields-1]);
		add(p , BorderLayout.CENTER);
	}
	
	
	
	public void setText(int index , String binaryRepr, int decimalRepr){
		labels[2*index].setText(binaryRepr);
		labels[2*index+1].setText(String.valueOf(decimalRepr));
	}
	
	public void setText(int index , String binaryRepr){
		//if it's the empty String , that is because is 0
		if(binaryRepr==""){
			setText(index , binaryRepr , 0);
		}
		else{
			setText(index , binaryRepr , Integer.parseInt(binaryRepr , 2));
		}
	}
	
	public void setText(int index , int decimalRepr , int numberBits){
		setText(index , Helper.convertDecimalToBinary(decimalRepr , numberBits) ,decimalRepr);
	}
	
	public void setLabel(String text){
		titleLabel.setText(text);
	}
	
	private String getTextFieldsText(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i<labels.length/2 - 1; i++){
			sb.append(labels[2*i].getText());
			sb.append("|");
		}
		sb.append(labels[labels.length - 2].getText());
		sb.append("\n");
		for(int i = 0 ; i<labels.length/2 - 1; i++){
			sb.append(labels[2*i + 1].getText());
			sb.append("|");
		}
		sb.append(labels[labels.length - 1].getText());
		return sb.toString();
	}
	
	private void showText() {
		JOptionPane.showMessageDialog(this , getTextFieldsText());
	}
	
}
