/**
 * HoroscopeTester tests out various HoroscopeEngines. 
 * Takes the type of horoscope as a command line argument. 
 * @author Audrey St. John
 **/
public class HoroscopeTester
{
	
	/** 
	 * Test out the passed engine by printout out its horoscope.
	 **/
	public static void testHoroscopeEngine( HoroscopeEngine e ) 
	{
		System.out.println( e.getHoroscope() );
	}
	
	/**
	 * Takes one argument, which is the type of horoscope:
	 *    number, moho, mad
	 * sample USAGE: java HoroscopeTester mad
	 **/
	public static void main( String[] args )
	{
		// if we got an argument
		if ( args.length > 0 )
		{
			// check if it's number
			if ( args[0].equals( "number" ) )
				testHoroscopeEngine( new NumberscopeEngine() );
			// check if it's moho
			else if ( args[0].equals( "moho" ) )
				testHoroscopeEngine( new MohoscopeEngine() );
			// check if it's mad	
			else if ( args[0].equals( "mad" ) )
				testHoroscopeEngine( new MadoscopeEngine() );
			// otherwise say how to use this
			else
				System.out.println( "USAGE: java HoroscopeTester <type>," +
					" where <type> is \"number\", \"moho\" or \"mad\"");
		}
		// default to numberscope
		else
			testHoroscopeEngine( new NumberscopeEngine() );		
	}
}