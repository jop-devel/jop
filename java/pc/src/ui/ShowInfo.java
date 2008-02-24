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
 * Created on 09.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ui;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ShowInfo extends JFrame {

	private javax.swing.JPanel jContentPane = null;

	private javax.swing.JPanel jPanel = null;
	private javax.swing.JLabel jLabel = null;
	private javax.swing.JTextField jTextField = null;
	private javax.swing.JButton jButton = null;
	private ui.PnlIntMem pnlIMem = null;
	private ui.PnlFlash pnlFlash = null;
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		ShowInfo myApp = new ShowInfo();

	}
	/**
	 * This is the default constructor
	 */
	public ShowInfo() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(800, 411);
		this.setContentPane(getJContentPane());
		this.setTitle("JOP Info");
		this.setVisible(true);
		this.addWindowListener(new java.awt.event.WindowAdapter() { 
			public void windowClosing(java.awt.event.WindowEvent e) {    
				System.exit(0);
			}
		});
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.add(getJPanel(), java.awt.BorderLayout.NORTH);
			jContentPane.add(getPnlIMem(), java.awt.BorderLayout.WEST);
			jContentPane.add(getPnlFlash(), java.awt.BorderLayout.EAST);
		}
		return jContentPane;
	}
	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanel() {
		if(jPanel == null) {
			jPanel = new javax.swing.JPanel();
			jPanel.add(getJLabel(), null);
			jPanel.add(getJTextField(), null);
			jPanel.add(getJButton(), null);
		}
		return jPanel;
	}
	/**
	 * This method initializes jLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel() {
		if(jLabel == null) {
			jLabel = new javax.swing.JLabel();
			jLabel.setText("IP Address:");
		}
		return jLabel;
	}
	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private javax.swing.JTextField getJTextField() {
		if(jTextField == null) {
			jTextField = new javax.swing.JTextField();
			jTextField.setColumns(10);
			jTextField.setText("192.168.0.4");
		}
		return jTextField;
	}
	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButton() {
		if(jButton == null) {
			jButton = new javax.swing.JButton();
			jButton.setText("Set");
			jButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					getPnlIMem().setAddress(getJTextField().getText());
					getPnlFlash().setAddress(getJTextField().getText());
				}
			});
		}
		return jButton;
	}
	/**
	 * This method initializes pnlIMem
	 * 
	 * @return ui.PnlSSS
	 */
	private ui.PnlIntMem getPnlIMem() {
		if(pnlIMem == null) {
			pnlIMem = new ui.PnlIntMem();
			pnlIMem.setPreferredSize(new java.awt.Dimension(400,200));
		}
		return pnlIMem;
	}
	/**
	 * This method initializes pnlFlash
	 * 
	 * @return ui.PnlFlash
	 */
	private ui.PnlFlash getPnlFlash() {
		if(pnlFlash == null) {
			pnlFlash = new ui.PnlFlash();
			pnlFlash.setPreferredSize(new java.awt.Dimension(400,200));
		}
		return pnlFlash;
	}
}  //  @jve:visual-info  decl-index=0 visual-constraint="10,10"
