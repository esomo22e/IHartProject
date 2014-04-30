// MohoscopeEngine.java


/** 
 * Class for generating mohoscopes, 
 * which uses MHC places (randomly generated) as part of its predictive powers.
 * @author Audrey St. John
 **/
public class MohoscopeEngine implements HoroscopeEngine
{
	
	// constants
	// String array will store the mohoscope; randomly generated MHC places
	// will glue it together
	public static final String[] MOHOSCOPE_BASE = 
		{"Make sure you visit ",
		" and ",
		" on your way to ",
		" to have a great day!"};

	public static final String[] MOHO_PLACES = 
		{"Clapp", "Kendade", "Shattuck", "Skinner Green", "Carr" };
		
	/**
	 * Constructor does nothing, but here for good programming practice.
	 **/
	public MohoscopeEngine()
	{
		// purposely does nothing, as there is no initialization required
	}
	
	/**
	 * Get a horoscope.
	 **/
	public String getHoroscope()
	{
		String mohoScope = "";
		
		// for each part of the base, except for the end, append a random place
		for ( int i = 0; i < MOHOSCOPE_BASE.length - 1; i++ )
		{
			mohoScope += MOHOSCOPE_BASE[i] + randomMohoPlace();
		}
		
		// now add on the final piece
		mohoScope += MOHOSCOPE_BASE[MOHOSCOPE_BASE.length - 1];
		return mohoScope;
	}
	
	/**
	 * Get a random MHC place from constant array.
	 **/
	public String randomMohoPlace()
	{
		return MOHO_PLACES[(int) Math.floor( Math.random()*MOHO_PLACES.length )];
	}
}
