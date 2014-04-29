// MadoscopeEngine.java
// Audrey St. John

/**
 * The MadoscopeEngine has a set of "horoscope" templates as well as a set of
 * verbs, adjectives and nouns from which to pull. The engine will randomly
 * substitue these parts of speech values into a horoscope to generate a
 * "Madoscope"!
 **/
public class MadoscopeEngine implements HoroscopeEngine
{
	
	// constants
	public static final String[] SCOPES = 
		{ "Today is going to be a @ day. Be sure to % a #!",
		  "It looks like your # is going to % a @ # today."};
	public static final String[] POS_HOLDERS = 
		{ "#","%","@" };
	public static final String[][] POS_OPTIONS = 
		{{ "umbrella", "hat", "kitten", "laptop" },
		 { "bring", "find", "photograph" },
		 { "lovely", "mysterious", "funny", "rainy" }};
		
	/**
	 * Constructor does nothing, but here for good programming practice.
	 **/
	public MadoscopeEngine()
	{
		// purposely does nothing, as there is no initialization required
	}
		
	/**
	 * Generate a random madoscope.
	 * @return the created madoscope
	 **/
	public String getHoroscope()
	{
		// to start off, get a random scope template
		String madoscope = randomScope();
		
		// for each part of speech, stored at column 0
		for ( int pos = 0; pos < POS_HOLDERS.length; pos++ )
			madoscope = subPOS( madoscope, POS_HOLDERS[pos], POS_OPTIONS[pos] );

		// return the scope
		return madoscope;
	}
	
	/**
	 * Get a random scope; contains @ for adjective, # for noun, % for verb.
	 **/
	public String randomScope()
	{
		return randomElement(SCOPES);
	}
	
	/** 
	 * Utility to get a random element from an array.
	 **/
	public String randomElement( String[] strArray )
	{
		return strArray[(int)Math.floor(Math.random()*strArray.length)];
	}
	
	/**
	 * Substitute a random pos value wherever the posHolder appears.
	 **/
	public String subPOS( String subInto, String posHolder, String[] posOptions )
	{
		// current substituted String
		String subbed = subInto;
		
		// random substitution
		String randomSub;
		
		// while the posHolder exists
		while ( subbed.indexOf( posHolder ) >= 0)
		{
			// replace it with a random sub
			
			// first, get a random sub
			randomSub = randomElement( posOptions );
			
			// now, glue together, omitting posHolder
			subbed = subbed.substring( 0, subbed.indexOf( posHolder ) ) // up to posHolder
				+ randomSub // substitution
				+ subbed.substring( subbed.indexOf( posHolder ) + posHolder.length() ); // rest of it
		}
		
		return subbed;
	}
	
}