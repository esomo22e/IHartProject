// NumberscopeEngine.java


/** 
 * Class for generating numberscopes, 
 * which uses numbers (randomly generated) as part of its predictive powers.
 * @author Audrey St. John
 **/
public class NumberscopeEngine implements HoroscopeEngine
{
	
	// constants
	// String array will store the numberscope; randomly generated numbers
	// will glue it together
	public static final String[] NUMBERSCOPE_BASE = 
		{"Today is the perfect day to spot ",
		 " falling leaves and ",
		 " cups of coffee."};
		
	public static final int LO_VALUE = 2; // minimum (inclusive) number to generate
	public static final int HI_VALUE = 10; // maximum (exclusive) number to generate
	
	/**
	 * Constructor does nothing, but here for good programming practice.
	 **/
	public NumberscopeEngine()
	{
		// purposely does nothing, as there is no initialization required
	}
	
	/**
	 * Get a horoscope.
	 **/
	public String getHoroscope()
	{
		String numScope = "";
		
		// for each part of the base, except for the end, append a random number
		for ( int i = 0; i < NUMBERSCOPE_BASE.length - 1; i++ )
		{
			numScope += NUMBERSCOPE_BASE[i] + Integer.toString( randomNumber() );
		}
		
		// now add on the final piece
		numScope += NUMBERSCOPE_BASE[NUMBERSCOPE_BASE.length - 1];
		
		return numScope;
	}
	
	/**
	 * Get a random number from the range [LO_VALUE, HI_VALUE).
	 **/
	public int randomNumber()
	{
		return (int) Math.floor( Math.random()*(HI_VALUE - LO_VALUE ) ) + LO_VALUE;
	}
}
