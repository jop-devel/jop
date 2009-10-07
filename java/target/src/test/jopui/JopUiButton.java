package jopui;


import com.jopdesign.jopui.JopUiApplication;
import com.jopdesign.jopui.core.Button;
import com.jopdesign.jopui.core.CheckBox;
import com.jopdesign.jopui.core.Label;
import com.jopdesign.jopui.core.Option;
import com.jopdesign.jopui.core.OptionGroup;
import com.jopdesign.jopui.core.TextField;
import com.jopdesign.jopui.event.JopEvent;
import com.jopdesign.jopui.event.KeyboardEvent;
import com.jopdesign.jopui.event.MouseEvent;
import com.jopdesign.jopui.helper.Color8Bit;

public class JopUiButton extends JopUiApplication {

	private TextField text;
	
	private Label disp;
	
	private Option valignTop;
	private Option valignMiddle;
	private Option valignBottom;

	private Option halignLeft;
	private Option halignCenter;
	private Option halignRight;

	private CheckBox reset;	
	private Button apply;
	
	
	public boolean init() {
		text = new TextField(109, 159, 100, 15, "demo");
		canvas.add(text);
		disp = new Label(59,89,200,60, "demo");
		disp.setHalign(Label.CENTER);
		disp.setValign(Label.MIDDLE);
		disp.setColorBody(new Color8Bit(Color8Bit.BLACK));
		canvas.add(disp);
		
		apply = new Button(209,214,100,15, "apply");
		apply.register(this, "apply");
		canvas.add(apply);
		
		reset = new CheckBox(209, 14, 100, 15, "reset");
		canvas.add(reset);
		
		OptionGroup og = new OptionGroup();
		valignTop = new Option(14, 14, 75, 15, "top");
		valignTop.setOptionGroup(og);
		canvas.add(valignTop);
		
		valignMiddle = new Option(14, 29, 75, 15, "middle");
		valignMiddle.setOptionGroup(og);
		valignMiddle.setState(Option.MARKED);
		canvas.add(valignMiddle);

		valignBottom = new Option(14, 44, 75, 15, "bottom");
		valignBottom.setOptionGroup(og);
		canvas.add(valignBottom);

		og = new OptionGroup();
		halignLeft = new Option(89, 14, 75, 15, "left");
		halignLeft.setOptionGroup(og);
		canvas.add(halignLeft);
		
		halignCenter = new Option(89, 29, 75, 15, "center");
		halignCenter.setOptionGroup(og);
		halignCenter.setState(Option.MARKED);
		canvas.add(halignCenter);
		
		halignRight = new Option(89, 44, 75, 15, "right");
		halignRight.setOptionGroup(og);
		canvas.add(halignRight);

		return true;
	}

	public boolean notify(JopEvent ev) {
	
		boolean ret = false;
		System.out.println("notify - start");

		if(ev.getCommand() == "apply") {
			if(ev.getEventType() == JopEvent.MOUSE_EVENT) {
				MouseEvent mev = (MouseEvent) ev;
				if(mev.getButton() == MouseEvent.LEFT_BUTTON &&
				   mev.getAction() == MouseEvent.MOUSE_UP) {
					ret = true;
					apply();
				}
					
			} else {
				KeyboardEvent kbev = (KeyboardEvent) ev;
				if(kbev.getCharacter() == ' ' && 
				   kbev.getAction() == KeyboardEvent.KEY_RELEASED) {
					ret = true;
					apply();
				}
			}
		
		}
		System.out.println("notify - end");
		return ret;
	}
	
	private void apply() {
	
		if(reset.getState() == CheckBox.CHECKED) {
		
			disp.setText("demo");
			disp.setHalign(Label.CENTER);
			disp.setValign(Label.MIDDLE);
			text.setText("demo");
			valignMiddle.setState(Option.MARKED);
			halignCenter.setState(Option.MARKED);
			reset.setState(CheckBox.UNCHECKED);
			
		} else {
			disp.setText(text.getText());
			if(valignTop.getState() == Option.MARKED)
				disp.setValign(Label.TOP);
				
			if(valignMiddle.getState() == Option.MARKED)
				disp.setValign(Label.MIDDLE);

			if(valignBottom.getState() == Option.MARKED)
				disp.setValign(Label.BOTTOM);

			if(halignLeft.getState() == Option.MARKED)
				disp.setHalign(Label.LEFT);
				
			if(halignCenter.getState() == Option.MARKED)
				disp.setHalign(Label.CENTER);
				
			if(halignRight.getState() == Option.MARKED)
				disp.setHalign(Label.RIGHT);
		}
	}

	public void terminate() {
		
	};
	
	
}
