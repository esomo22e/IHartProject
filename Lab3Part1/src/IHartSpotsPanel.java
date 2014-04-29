// IHartSpotsPanel.java
// IHartSpotsPanel.java
// CS 201
 
// awt
import java.awt.BorderLayout;
 
// ihart
import ihart.event.CVEvent;
import ihart.event.CVEventListener;
import ihart.event.CVManager;
 
/**
* Listens for CVEVents and prints out the type of each event, and the x, y, 
* width, and height for each blob associated with the event.
* 
* When a face is detected, shows random spots.
* 
* @author Kim Faughnan 2013
**/
public class IHartSpotsPanel extends SpotsPanel implements CVEventListener 
{
	/** Maximum number of spots to generate. **/
	public static final int MAX_SPOTS = 20;
 
	// CVManager is the "go-between" to the server that is connected to the webcam
	private CVManager cvmanager;
 
	/**
	 * Constructor creates a new IHartSpotsPanel.
	 **/
	public IHartSpotsPanel() 
	{
		// call the superclass constructor
		super();
 
		// set up the IHart CVManager for subscribing to CVEvents
		setupCVManager();
	}
 
	/**
	 * Set up IHart's CVManager to connect to the webcam and
	 * sign up for listening to CVEvents.
	 **/
	public void setupCVManager()
	{		
		// set up the CVManager to connect to
		// the current computer ("localhost")
		// and port 5204
		cvmanager = new CVManager("localhost", 5204);
 
		// this is a CVEventListener, so ask the cvmanager to notify us
		// of CVEvents
		cvmanager.addCVEventListener(this);
	}
 
	/** 
	 * Override creating GUI components to omit the input panel.
	 * returns the created main panel
	 **/
	public void init()
	{
		// create panel for drawing the spots
		// and add to center
		add(  createSpotsDisplay(), BorderLayout.CENTER );
	}
 
	/** 
	 * Method to randomly set and refresh the number of spots display
	 **/
	public void seeRandomSpots()
	{
		// set the number of spots to be a random amount
		spotsDisplay.setNumberOfSpots( (int)Math.floor( Math.random()*MAX_SPOTS ) );
 
		// make sure the display refreshes
		spotsDisplay.repaint();
	}
 
	/**
	 * Utility method to show how to access data packed into CVEvents.
	 * Prints the information belonging to each CVEvent (x, y, width, height, type)
	 * @param event
	 *            the event to print information from
	 */
	public void printEventInformation(CVEvent event) 
	{
		// holds the to-be-outputed string
		String output = "";
 
		output += "EventType: " + event.getType() + "\n   int equivalent: " 
			+ event.getIntType(event.getType()) + "\n";
 
		// finds number of assiciated blobs; adds appropriate statement to output string
		int number = event.getNumBlobs();
 
		// if there is only one blob detected for this event
		if(number == 1)
			output += "1 blob is associated with this event.\n";
		else
			output += number + " blobs are associated with this event \n";
 
		// prints x, y, width, and height for each blob associated with the event
		for (int i = 0; i < number; i++) 
		{
			output += "{x = " + event.getX(i);
			output += ", y = " + event.getY(i);
			output += ", width = " + event.getWidth(i);
			output += ", height = " + event.getHeight(i) + "} \n";
		}
 
		System.out.println(output + "\n");
	}
 
	/******************* METHODS REQUIRED BY CVEventListener interface ******************/
 
	/**
	 * Invoked when faces arrives and prints the information (x, y, width, height, type)
	 * for each event
	 * 
	 * @param faEvt
	 *            the CVEvent for the face
	 */
	public void facesArrived(CVEvent faEvt) 
	{
		System.out.println("faces have arrived!");
		printEventInformation(faEvt);
 
		// update the display
		seeRandomSpots();
	}
 
	/**
	 * Invoked when holes arrive and prints the information (x, y, width, height, type)
	 * for each event
	 * 
	 * @param hoEvt
	 *            the CVEvent for the hole
	 */
	public void holesArrived(CVEvent hoEvt) 
	{
		System.out.println("holes have arrived!");
		printEventInformation(hoEvt);
	}
 
	/**
	 * Invoked when shells arrive and prints the information (x, y, width, height, type)
	 * for each event
	 * 
	 * @param shEvt
	 *            the CVEvent for the shell
	 */
	public void shellsArrived(CVEvent shEvt) 
	{
		System.out.println("shells have arrived!");
		printEventInformation(shEvt);
	}
 
	/**
	 * Invoked when general blobs (which include shells, holes and faces) arrives. 
	 * Since the
	 * program listens for each individually, nothing happens here.
	 * 
	 * @param blobEvt
	 *            the CVEvent for the blob
	 */
	public void blobsArrived(CVEvent blobEvt) 
	{
		// a blob alert is sent for shells, holes, and faces
		System.out.println("blobs have arrived!");
		printEventInformation(blobEvt);
	}
 
}