// SpotsApplet.java


// awt
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// swing 
import javax.swing.JButton;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** 
 * SpotsApplet takes a name as input, then uses it to draw random spots.
 * @author Audrey St. John
 **/
public class SpotsApplet extends JApplet
{
	// constructor
	public SpotsApplet()
	{
		// call super constructor
		super();
	}
	
	/** 
	 * special method that will be invoked when applet is created
	 **/
	public void start()
	{
		// create an instance of a SpotsPanel and add it
		add( new SpotsPanel() );
	}
}