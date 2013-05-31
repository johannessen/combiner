package de.thaw.thesis.comb;

import org.testng.annotations.*;

public class HighwayTypeTest {
	
/*
@BeforeClass
	public void setUp() {
		// code that will be invoked when this test is instantiated
	}
*/
	
	
	@Test( expectedExceptions = NullPointerException.class )
	public void constructorNull () {
		HighwayType type = HighwayType.valueOf(null);
		assert false;  // is never reached due to an exception
	}
	
	
	@Test
	public void constructor () {
		HighwayType type = HighwayType.valueOf("trunk");
		assert type.name() == "trunk";
	}
	
	
	@Test
	public void existence () {
		assert HighwayType.valueOf("residential") != null;
		assert HighwayType.valueOf("") != null;
		assert HighwayType.valueOf("NULL") != null;
	}
	
	
	@Test
	public void identity () {
		assert HighwayType.valueOf("motorway") == HighwayType.valueOf("motorway");
		assert HighwayType.valueOf("") == HighwayType.valueOf("");
		assert HighwayType.valueOf("NONE") == HighwayType.valueOf("NONE");
	}
	
	
	@Test
	public void equality () {
		assert HighwayType.valueOf("motorway").equals( HighwayType.valueOf("motorway") );
		assert HighwayType.valueOf("").equals( HighwayType.valueOf("") );
		
		assert ! HighwayType.valueOf("primary").equals( HighwayType.valueOf("service") );
		assert ! HighwayType.valueOf("service").equals( HighwayType.valueOf("primary") );
		assert ! HighwayType.valueOf("").equals( HighwayType.valueOf("secondary") );
		assert ! HighwayType.valueOf("secondary").equals( HighwayType.valueOf("") );
	}
	
	// :TODO: test hashCode
	
	
	@Test
	public void comparison () {
		HighwayType motorway = HighwayType.valueOf("motorway");
		HighwayType trunk = HighwayType.valueOf("trunk");
		HighwayType primary = HighwayType.valueOf("primary");
		
		assert primary.compareTo(trunk) < 0;
		assert trunk.compareTo(motorway) < 0;
		assert primary.compareTo(motorway) < 0;
		
		assert trunk.compareTo(trunk) == 0;
		assert trunk.compareTo(primary) > 0;
		assert trunk.compareTo(HighwayType.valueOf("BOGUS")) > 0;
	}

	
	@Test
	public void nameValue () {
		assert HighwayType.valueOf("motorway").name().equals( "motorway" );
		assert HighwayType.valueOf("FOO").name().equals( "" );
	}
	
}
