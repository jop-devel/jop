package jopui;

import com.jopdesign.jopui.JopUi;

public class JopUIDemo {
	public static void main(String[] args) {
		
		JopUi.register(new JopUiButton());
		JopUi.run();
	}
}
