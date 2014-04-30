// HoroscopePanel.java
// 

// awt
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


// swing
import javax.swing.JPanel;

/**
 * HoroscopePanel is a GUI element that displays 3 different horoscopes.
 * 
 **/
public class HoroscopePanel extends JPanel
{
	/**
	 * constructor 
	 **/
	public HoroscopePanel()
	{
		// call superclass' constructor
		super();
		
		// create the GUI components
		init();
	}
	
	/**
	 * Initialize GUI components to have 3 columns, 
	 * one for each type of horoscope.
	 **/
	public void init()
	{
		// 1 row, 3 columns
		setLayout( new GridLayout( 1, 3 ) );
		
		// add a new HoroscopeDisplay for each type of horosocope
		add( new HoroscopeDisplay( new NumberscopeEngine(), "numberscope" ) );
	
	}
}
