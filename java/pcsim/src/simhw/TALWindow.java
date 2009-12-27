/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

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
package simhw;

import java.util.Enumeration;
import gnu.io.CommPortIdentifier;
import javax.swing.*;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TALWindow extends JFrame {
	
	private TALSim tsim;
	private boolean wd;
	private int inPort;
	private int outPort;
	private int ledPort;
	private javax.swing.JPanel jContentPane = null;

	private javax.swing.JRadioButton jRadioButtonWd = null;
	private javax.swing.JSlider jSliderAdc1 = null;
	private javax.swing.JPanel jPanelAdc = null;
	private javax.swing.JSlider jSliderAdc2 = null;
	private javax.swing.JSlider jSliderAdc3 = null;
	private javax.swing.JPanel jPanel1 = null;
	private javax.swing.JPanel jPanel2 = null;
	private javax.swing.JPanel jPanel3 = null;
	private javax.swing.JLabel jLabel = null;
	private javax.swing.JLabel jLabel1 = null;
	private javax.swing.JLabel jLabel2 = null;
	private javax.swing.JPanel jPanelIn = null;
	private javax.swing.JRadioButton jRadioButton1 = null;
	private javax.swing.JRadioButton jRadioButton2 = null;
	private javax.swing.JRadioButton jRadioButton3 = null;
	private javax.swing.JRadioButton jRadioButton4 = null;
	private javax.swing.JPanel jPanelSerial = null;
	private javax.swing.JPanel jPanelOutWd = null;
	private javax.swing.JLabel jLabel3 = null;
	private javax.swing.JRadioButton jRBIn[] = new javax.swing.JRadioButton[10];
	private javax.swing.JPanel jPanelLed = null;
	private javax.swing.JRadioButton jRBLed[] = new javax.swing.JRadioButton[14];
	private javax.swing.JComboBox jComboBox = null;
	/**
	 * This is the default constructor
	 */
	public TALWindow() {

		super();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		initialize();
		startThread();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(487, 403);
		this.setContentPane(getJContentPane());
		this.setTitle("TAL Simulation");
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
			jContentPane.add(getJPanelAdc(), java.awt.BorderLayout.EAST);
			jContentPane.add(getJPanelIn(), java.awt.BorderLayout.WEST);
			jContentPane.add(getJPanelLed(), java.awt.BorderLayout.CENTER);
			jContentPane.add(getJPanelSerial(), java.awt.BorderLayout.NORTH);
			jContentPane.add(getJPanelOutWd(), java.awt.BorderLayout.SOUTH);
		}
		return jContentPane;
	}
	/**
	 * This method initializes jRadioButtonWd
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private javax.swing.JRadioButton getJRadioButtonWd() {
		if(jRadioButtonWd == null) {
			jRadioButtonWd = new javax.swing.JRadioButton();
			jRadioButtonWd.setText("Watch Dog");
			jRadioButtonWd.setSelected(false);
			jRadioButtonWd.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jRadioButtonWd.setEnabled(true);
		}
		return jRadioButtonWd;
	}
	/**
	 * @param b
	 */
	public void setWd(boolean b) {
		wd = b;
	}

	/**
	 * @return
	 */
	public int getAdc1() {
		return getJSliderAdc1().getValue();
	}

	/**
	 * @return
	 */
	public int getAdc2() {
		return getJSliderAdc2().getValue();
	}

	/**
	 * @return
	 */
	public int getAdc3() {
		return getJSliderAdc3().getValue();
	}

	/**
	 * @return
	 */
	public int getInPort() {
		return inPort;
	}

	/**
	 * @param i
	 */
	public void setLedPort(int i) {
		ledPort = i;
	}

	/**
	 * @param i
	 */
	public void setOutPort(int i) {
		outPort = i;
	}
	private void listPortChoices() {
		CommPortIdentifier portId;

		Enumeration en = CommPortIdentifier.getPortIdentifiers();

		getJComboBox().addItem("null");
		// iterate through the ports.
		while (en.hasMoreElements()) {
			portId = (CommPortIdentifier) en.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				getJComboBox().addItem(portId.getName());
			}
		}
//		portChoice.select(parameters.getPortName());
	}

	private void startThread() {
		
		//
		//	update display state
		//
		new Thread() {
			
			public void run() {
				for (;;) {
					getJRadioButton1().setSelected((outPort&1) != 0);
					getJRadioButton2().setSelected((outPort&2) != 0);
					getJRadioButton3().setSelected((outPort&4) != 0);
					getJRadioButton4().setSelected((outPort&8) != 0);
					getJRadioButtonWd().setSelected(wd);
					for (int i = 0; i < jRBLed.length; i++) {
						getJRBLed(i).setSelected((ledPort&(1<<i))!=0);
					}
					int val = 0;
					for (int i = 0; i < jRBIn.length; i++) {
						if (getJRBIn(i).isSelected()) {
							val |= 1<<i;
						}
					}
					inPort = val;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
		}.start();
	}

	/**
	 * This method initializes jSliderAdc1
	 * 
	 * @return javax.swing.JSlider
	 */
	private javax.swing.JSlider getJSliderAdc1() {
		if(jSliderAdc1 == null) {
			jSliderAdc1 = new javax.swing.JSlider();
			jSliderAdc1.setMaximum(65535);
			jSliderAdc1.setOrientation(javax.swing.JSlider.VERTICAL);
			jSliderAdc1.setName("ADC1");
			jSliderAdc1.setPaintLabels(true);
			jSliderAdc1.setPaintTicks(true);
			jSliderAdc1.setExtent(5);
			jSliderAdc1.setValue(40000);
			jSliderAdc1.setPreferredSize(new java.awt.Dimension(32,250));
		}
		return jSliderAdc1;
	}
	/**
	 * This method initializes jPanelAdc
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanelAdc() {
		if(jPanelAdc == null) {
			jPanelAdc = new javax.swing.JPanel();
			jPanelAdc.add(getJPanel1(), null);
			jPanelAdc.add(getJPanel2(), null);
			jPanelAdc.add(getJPanel3(), null);
			jPanelAdc.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		}
		return jPanelAdc;
	}
	/**
	 * This method initializes jSliderAdc2
	 * 
	 * @return javax.swing.JSlider
	 */
	private javax.swing.JSlider getJSliderAdc2() {
		if(jSliderAdc2 == null) {
			jSliderAdc2 = new javax.swing.JSlider();
			jSliderAdc2.setMaximum(65535);
			jSliderAdc2.setOrientation(javax.swing.JSlider.VERTICAL);
			jSliderAdc2.setPaintLabels(true);
			jSliderAdc2.setPaintTicks(true);
			jSliderAdc2.setExtent(5);
			jSliderAdc2.setValue(40000);
			jSliderAdc2.setPreferredSize(new java.awt.Dimension(32,250));
		}
		return jSliderAdc2;
	}
	/**
	 * This method initializes jSliderAdc3
	 * 
	 * @return javax.swing.JSlider
	 */
	private javax.swing.JSlider getJSliderAdc3() {
		if(jSliderAdc3 == null) {
			jSliderAdc3 = new javax.swing.JSlider();
			jSliderAdc3.setMaximum(65535);
			jSliderAdc3.setOrientation(javax.swing.JSlider.VERTICAL);
			jSliderAdc3.setPaintLabels(true);
			jSliderAdc3.setPaintTicks(true);
			jSliderAdc3.setExtent(5);
			jSliderAdc3.setValue(40000);
			jSliderAdc3.setPreferredSize(new java.awt.Dimension(32,250));
		}
		return jSliderAdc3;
	}
	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanel1() {
		if(jPanel1 == null) {
			jPanel1 = new javax.swing.JPanel();
			jPanel1.setLayout(new java.awt.BorderLayout());
			jPanel1.add(getJLabel(), java.awt.BorderLayout.NORTH);
			jPanel1.add(getJSliderAdc1(), java.awt.BorderLayout.CENTER);
		}
		return jPanel1;
	}
	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanel2() {
		if(jPanel2 == null) {
			jPanel2 = new javax.swing.JPanel();
			jPanel2.setLayout(new java.awt.BorderLayout());
			jPanel2.add(getJLabel1(), java.awt.BorderLayout.NORTH);
			jPanel2.add(getJSliderAdc2(), java.awt.BorderLayout.CENTER);
		}
		return jPanel2;
	}
	/**
	 * This method initializes jPanel3
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanel3() {
		if(jPanel3 == null) {
			jPanel3 = new javax.swing.JPanel();
			jPanel3.setLayout(new java.awt.BorderLayout());
			jPanel3.add(getJLabel2(), java.awt.BorderLayout.NORTH);
			jPanel3.add(getJSliderAdc3(), java.awt.BorderLayout.CENTER);
		}
		return jPanel3;
	}
	/**
	 * This method initializes jLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel() {
		if(jLabel == null) {
			jLabel = new javax.swing.JLabel();
			jLabel.setText("I1");
			jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
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
			jLabel1.setText("I2");
			jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
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
			jLabel2.setText("UB");
			jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		}
		return jLabel2;
	}
	/**
	 * This method initializes jPanelIn
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanelIn() {
		if(jPanelIn == null) {
			jPanelIn = new javax.swing.JPanel();
			jPanelIn.setLayout(new javax.swing.BoxLayout(jPanelIn, javax.swing.BoxLayout.Y_AXIS));
			for (int i = 0; i < jRBIn.length; i++) {
				jPanelIn.add(getJRBIn(i), null);
			}
			jPanelIn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		}
		return jPanelIn;
	}
	/**
	 * @param i
	 * @return
	 */
	private JRadioButton getJRBIn(int i) {
		if(jRBIn[i] == null) {
			jRBIn[i] = new javax.swing.JRadioButton();
			jRBIn[i].setText("In "+(i+1));
			jRBIn[i].setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		}
		return jRBIn[i];
	}
	/**
	 * @param i
	 * @return
	 */
	private JRadioButton getJRBLed(int i) {
		if(jRBLed[i] == null) {
			jRBLed[i] = new javax.swing.JRadioButton();
			jRBLed[i].setText("LED "+(i+1));
			jRBLed[i].setHorizontalTextPosition(
				((i&1) == 0) ? javax.swing.SwingConstants.LEFT :
				javax.swing.SwingConstants.RIGHT
			);
			jRBLed[i].setHorizontalAlignment(
				((i&1) == 1) ? javax.swing.SwingConstants.LEFT :
				javax.swing.SwingConstants.RIGHT
			);
		}
		return jRBLed[i];
	}
	/**
	 * This method initializes jRadioButton1
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private javax.swing.JRadioButton getJRadioButton1() {
		if(jRadioButton1 == null) {
			jRadioButton1 = new javax.swing.JRadioButton();
			jRadioButton1.setText("Out 1");
		}
		return jRadioButton1;
	}
	/**
	 * This method initializes jRadioButton2
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private javax.swing.JRadioButton getJRadioButton2() {
		if(jRadioButton2 == null) {
			jRadioButton2 = new javax.swing.JRadioButton();
			jRadioButton2.setText("Out 2");
		}
		return jRadioButton2;
	}
	/**
	 * This method initializes jRadioButton3
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private javax.swing.JRadioButton getJRadioButton3() {
		if(jRadioButton3 == null) {
			jRadioButton3 = new javax.swing.JRadioButton();
			jRadioButton3.setText("Out 3");
		}
		return jRadioButton3;
	}
	/**
	 * This method initializes jRadioButton4
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private javax.swing.JRadioButton getJRadioButton4() {
		if(jRadioButton4 == null) {
			jRadioButton4 = new javax.swing.JRadioButton();
			jRadioButton4.setText(" Out 4");
		}
		return jRadioButton4;
	}
	/**
	 * This method initializes jPanelSerial
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanelSerial() {
		if(jPanelSerial == null) {
			jPanelSerial = new javax.swing.JPanel();
			jPanelSerial.add(getJLabel3(), null);
			jPanelSerial.add(getJComboBox(), null);
			listPortChoices();
			jPanelSerial.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		}
		return jPanelSerial;
	}
	/**
	 * This method initializes jPanelOutWd
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanelOutWd() {
		if(jPanelOutWd == null) {
			jPanelOutWd = new javax.swing.JPanel();
			jPanelOutWd.add(getJRadioButton1(), null);
			jPanelOutWd.add(getJRadioButton2(), null);
			jPanelOutWd.add(getJRadioButton3(), null);
			jPanelOutWd.add(getJRadioButton4(), null);
			jPanelOutWd.add(getJRadioButtonWd(), null);
			jPanelOutWd.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		}
		return jPanelOutWd;
	}
	/**
	 * This method initializes jLabel3
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel3() {
		if(jLabel3 == null) {
			jLabel3 = new javax.swing.JLabel();
			jLabel3.setText("Serial Port:");
		}
		return jLabel3;
	}
	/**
	 * This method initializes jPanelLed
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanelLed() {
		if(jPanelLed == null) {
			jPanelLed = new javax.swing.JPanel();
			java.awt.GridLayout layGridLayout1 = new java.awt.GridLayout();
			layGridLayout1.setRows(7);
			layGridLayout1.setColumns(2);
			jPanelLed.setLayout(layGridLayout1);
			jPanelLed.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
			for (int i = 0; i < jRBLed.length; i++) {
				jPanelLed.add(getJRBLed(i));
			}
		}
		return jPanelLed;
	}
	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private javax.swing.JComboBox getJComboBox() {
		if(jComboBox == null) {
			jComboBox = new javax.swing.JComboBox();
			jComboBox.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (tsim!=null) {
						tsim.setPortName((String) getJComboBox().getSelectedItem());
					}
				}
			});
		}
		return jComboBox;
	}
	/**
	 * @param sim
	 */
	public void setTsim(TALSim sim) {
		tsim = sim;
	}

}  //  @jve:visual-info  decl-index=0 visual-constraint="10,10"
