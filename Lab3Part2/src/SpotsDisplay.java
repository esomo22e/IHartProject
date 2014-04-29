// SpotsDisplay.java
// Audrey St. John

// awt
import java.awt.Color;
import java.awt.Graphics;

// swing
import javax.swing.JComponent;

/**
 * SpotsDisplay is a GUI element that draws spots.
 * @author Audrey St. John
 **/
public class SpotsDisplay extends JComponent
{
	// constants
	public static final Color[] PAINT_COLORS = {Color.WHITE, Color.PINK, Color.CYAN, Color.GREEN, 
		Color.YELLOW, Color.BLACK, Color.BLUE, Color.RED };
	
	// min diameter of a spot is 10 pixels
	public static int MIN_SPOT_SIZE = 10;
	
	// instance properties
	private int numSpots; // value must be positive

	/**
	 * constructor with a parameter of the number of spots
	 **/
	public SpotsDisplay( int numSpots )
	{
		// call superclass' constructor
		super();
		
		// initialize instance properties
		this.numSpots = numSpots; 
	}

	/**
	 * constructor with no spots
	 **/
	public SpotsDisplay()
	{
		// call other constructor, passing a parameter of 0
		this( 0 );		
	}	
	
	/**
	 * Given a number, set it as the number of spots.
	 * Valid parameter values: positive integers
	 * If invalid paramter is passed, defaults to 0
	 **/
	public void setNumberOfSpots( int spots )
	{
		// check for valid value
		if ( spots >= 0 )
			numSpots = spots;
	}
	
	/**
	 * Override the paint method to draw spots.
	 **/
	public void paint( Graphics g )
	{
		// draw numSpots times
		for ( int i = 0; i < numSpots; i++ )
			// draw a random spot
			drawRandomSpot( g );
	}
	
	/**
	 * draw a random spot
	 **/
	public void drawRandomSpot( Graphics g )
	{
		// random upper left corner
		int x = (int)Math.floor(Math.random()*getWidth());
		int y = (int)Math.floor(Math.random()*getHeight());
		
		// use a random paint color
		g.setColor( randomPaintColor() );
		
		// generate a random diameter
		int diam = randomSpotDiameter();
		
		// fill oval (upper left x, upper left y, width, height)
		g.fillOval( x, y, diam, diam );
	}
	
	/**
	 * Grab a random color from the PAINT_COLORS array.
	 * @return random color
	 **/
	public Color randomPaintColor()
	{
		int randomPaintIndex = (int)Math.floor(Math.random()*PAINT_COLORS.length);
		return PAINT_COLORS[ randomPaintIndex ];		
	}
	
	/** 
	 * Generate a random diameter for a spot.
	 * @return diameter
	 **/
	public int randomSpotDiameter()
	{		
		// max size 1/5 of width or height (whichever's smaller)
		int maxSize;
		
		// if the width is smaller
		if ( getWidth() < getHeight() )
			maxSize = getWidth() / 5;
		// otherwise, the height is smaller
		else
			maxSize = getHeight() / 5;
			
		// now generate a random diameter in the range [MIN_SPOT_SIZE, maxSize)
		int randomDiam = (int)Math.floor(Math.random()*(maxSize - MIN_SPOT_SIZE));
			
		// return it
		return randomDiam;
	}
}