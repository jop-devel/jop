/*
  This file is part of JOP, the Java Optimized Processor (http://www.jopdesign.com/)

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * Created on 12.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ui;

import javax.swing.JPanel;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PnlIntMem extends JPanel {

	private javax.swing.JLabel jLabel = null;
	private javax.swing.JLabel jLabel1 = null;
	private javax.swing.JLabel jLabel2 = null;
	private javax.swing.JTextField jTextField = null;
	private javax.swing.JTextField jTextField1 = null;
	private javax.swing.JButton jButton = null;
	private javax.swing.JScrollPane jScrollPane = null;
	private javax.swing.JTextArea jTextArea = null;
	/**
	 * This is the default constructor
	 */
	public PnlIntMem() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(null);
		this.add(getJLabel(), null);
		this.add(getJLabel1(), null);
		this.add(getJLabel2(), null);
		this.add(getJTextField(), null);
		this.add(getJTextField1(), null);
		this.add(getJButton(), null);
		this.add(getJScrollPane(), null);
		this.setSize(418, 284);
	}
	/**
	 * This method initializes jLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel() {
		if(jLabel == null) {
			jLabel = new javax.swing.JLabel();
			jLabel.setBounds(28, 23, 156, 23);
			jLabel.setText("N/A");
		}
		return jLabel;
	}
	/**
	 * This method initializes jLabel1
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel1() {
		if(jLabel1 == null) {
			jLabel1 = new javax.swing.JLabel();
			jLabel1.setBounds(28, 67, 78, 23);
			jLabel1.setText("Max. Stack");
		}
		return jLabel1;
	}
	/**
	 * This method initializes jLabel2
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel2() {
		if(jLabel2 == null) {
			jLabel2 = new javax.swing.JLabel();
			jLabel2.setBounds(28, 111, 78, 23);
			jLabel2.setText("Memory");
		}
		return jLabel2;
	}
	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private javax.swing.JTextField getJTextField() {
		if(jTextField == null) {
			jTextField = new javax.swing.JTextField();
			jTextField.setBounds(118, 68, 84, 23);
			jTextField.setEnabled(false);
		}
		return jTextField;
	}
	/**
	 * This method initializes jTextField1
	 * 
	 * @return javax.swing.JTextField
	 */
	private javax.swing.JTextField getJTextField1() {
		if(jTextField1 == null) {
			jTextField1 = new javax.swing.JTextField();
			jTextField1.setBounds(118, 112, 84, 23);
			jTextField1.setEnabled(false);
		}
		return jTextField1;
	}
	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButton() {
		if(jButton == null) {
			jButton = new javax.swing.JButton();
			jButton.setBounds(60, 185, 91, 34);
			jButton.setText("Get Info");
			jButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					getInfo();
				}
			});
		}
		return jButton;
	}
	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private javax.swing.JScrollPane getJScrollPane() {
		if(jScrollPane == null) {
			jScrollPane = new javax.swing.JScrollPane();
			jScrollPane.setViewportView(getJTextArea());
			jScrollPane.setBounds(228, 23, 161, 226);
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	private javax.swing.JTextArea getJTextArea() {
		if(jTextArea == null) {
			jTextArea = new javax.swing.JTextArea();
		}
		return jTextArea;
	}
	
	public void setAddress(String string) {
		Connection.single.setAddress(string);
		getJLabel().setText(string);
	}
	
	private void getInfo() {
		System.out.println("get info from "+Connection.single.getAddress());
		int len = Connection.single.read("i0");
		int[] buf = Connection.single.getBuf();
		getJTextArea().setText(len+" words received \n");
		for (int i = 0; i < len; i++) {
			getJTextArea().append(i+" "+buf[i]+"\n");
		}
		getJTextField1().setText(buf[2]*4/1024+" KB");
		for (int i = 255; i >= 0; --i) {
			if (buf[i]!=0x12345678) {
				getJTextField().setText(""+i);
				break;
			} 			
		}
		
	}

}  //  @jve:visual-info  decl-index=0 visual-constraint="10,10"
