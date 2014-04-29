/**
* A Horoscope has predictive powers. It randomy generates numbers to predict. 
**/

public class NumberscopeEngine implements HoroscopeEngine
{
    /**
      * Get horoscope from HoroscopeEngine
      **/
   
    public String getHoroscope()
    {
	//this will randomly generate numbers 
	int r = (int)(Math.random() * 3);
	int r2 = (int)(Math.random() * 3);

	/**
	 * Create  an array of strings to display sentences with random integer. 
	 **/
	String[] horoscope = new String[3]; 
	horoscope[0] = "Today is a great day to see "  + Integer.toString(r) + " dogs and " + Integer.toString(r2) +" students walking";
               
	return horoscope[0];
    } 
   
    public String getType()
    {
	return "NumberscopeEngine";
    }
         
    
}    