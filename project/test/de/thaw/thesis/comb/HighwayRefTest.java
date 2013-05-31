package de.thaw.thesis.comb;

import org.testng.annotations.*;

public class HighwayRefTest {
	
/*
	@BeforeClass
	public void setUp() {
		// code that will be invoked when this test is instantiated
	}
*/

	@Test( expectedExceptions = NullPointerException.class )
	public void constructorNull () {
		HighwayRef ref = new HighwayRef(null);
		assert false;  // is never reached due to an exception
	}
	
	
	@Test
	public void constructor () {
		HighwayRef ref;
		
		ref = new HighwayRef("A 5");
		assert ref.ref().equals("A 5");
		assert ref.ref() == "A 5";
		
		ref = new HighwayRef("");
		assert ref.ref().equals("");
		assert ref.ref() == "";
		
		ref = new HighwayRef("Fv46");
		assert ref.ref().equals("Fv46");
		assert ref.ref() == "Fv46";
	}
	
}
