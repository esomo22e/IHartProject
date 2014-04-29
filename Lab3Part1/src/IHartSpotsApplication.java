// IHartSpotsApplication.java
// swing 
import javax.swing.JFrame;

/** 
 * Main application to show a SpotsFrame 
 **/
public class IHartSpotsApplication
{
	/**
	 * main method starts the program
	 **/
	public static void main( String[] args )
	{
		// create a new JFrame to hold SpotsPanel
		JFrame spotsFrame = new JFrame();
		
		// set size
		spotsFrame.setSize( 600, 400 );

		// create a IHartSpotsPanel and add it
		spotsFrame.add( new IHartSpotsPanel() );

		// exit normally on closing the window
		spotsFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		// show frame
		spotsFrame.setVisible( true );
	}
}