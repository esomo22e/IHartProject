// HoroscopeDisplay.java


// awt
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// swing
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * HoroscopeDisplay is a GUI element that keeps a HoroscopeEngine to 
 * generate and display a horoscope.
 * @author Audrey St. John
 **/
public class HoroscopeDisplay extends JPanel implements ActionListener
{
	// instance properties
        protected HoroscopeEngine horoscopeEngine; // for getting horoscopes
	protected JTextArea horoscopeDisplay; // for displaying a horoscope

	/**
	 * constructor with an engine passed for getting horoscopes and
	 * the type of horoscope being generated
	 **/
    public HoroscopeDisplay(HoroscopeEngine eng, String horoscopeType)
	{
		// call superclass' constructor
		super();
		
		// initialize instance properties
		horoscopeEngine = eng; 
		
		// create the GUI components
		init( horoscopeType );
	}
	
	/** 
	 * Create GUI components.
	 **/
	public void init( String horoscopeType )
	{
		// first, set our layout
		setLayout( new BorderLayout() );
		
		// now create panel to display horoscope 
		// and add to center
		add(  createHoroscopeDisplay(), BorderLayout.CENTER );
	
		
	}
	
	/**
	 * Create and return text area for displaying horoscope.
	 **/
	public JTextArea createHoroscopeDisplay()
	{
		// make a new text area and store in instance variable for later access
		horoscopeDisplay = new JTextArea();
		
		// don't let the user change the text
		horoscopeDisplay.setEditable( false );
		
		// if there is too much text for one line, wrap it!
		horoscopeDisplay.setLineWrap( true );
		
		// wrap without splitting words
		horoscopeDisplay.setWrapStyleWord(true);
		
		// set up the margin
		// Insets constructor is (top, left, bottom, right)
		horoscopeDisplay.setMargin( new Insets( 30, 5, 10, 5 ) );
		
		// return the created text are
		return horoscopeDisplay;
	}
	
	
	/**
	 * Display a new horoscope.
	 **/
	public void generateHoroscope()
	{
		// get a new horoscope 
		String horoscope = horoscopeEngine.getHoroscope();
		
		// update the text
		horoscopeDisplay.setText( horoscope );
	}
	
	/**
	 * Special method required by implementing ActionListener 
	 * (function signature cannot be changed)
	 * Invoked when an action is performed on the spotsButton, since this
	 * was added as an ActionListener.
	 **/
	public void actionPerformed( ActionEvent e )
	{
		// this is where you put the code you want
		// executed when the button is pressed
		// here, we call a method to update the display     
		generateHoroscope();
	}
}
