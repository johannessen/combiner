package de.thaw.thesis.comb;

import org.testng.annotations.*;

public class HighwayRefTest {
	
	
	@Test( expectedExceptions = NullPointerException.class )
	public void constructorNull () {
		HighwayRef ref = HighwayRef.valueOf(null);
		assert false;  // is never reached due to an exception
	}
	
	
	@Test
	public void constructor () {
		HighwayRef ref;
		
		ref = HighwayRef.valueOf("A 5");
		assert ref.toString().equals("A 5");
	}
	
	
	@Test
	public void existence () {
		assert HighwayRef.valueOf("FOO") != null;
		assert HighwayRef.valueOf("") != null;
	}
	
	
	@Test
	public void empty () {
		assert ! HighwayRef.valueOf("A 4").isEmpty();
		assert ! HighwayRef.valueOf("FALSE").isEmpty();
		assert HighwayRef.valueOf("").isEmpty();
	}
	
	
	@Test
	public void equality () {
		assert HighwayRef.valueOf("A 4").equals( HighwayRef.valueOf("A 4") );
		assert HighwayRef.valueOf("").equals( HighwayRef.valueOf("") );
		
		assert ! HighwayRef.valueOf("A 4").equals( HighwayRef.valueOf("") );
		assert ! HighwayRef.valueOf("").equals( HighwayRef.valueOf("A 4") );
		
		assert ! HighwayRef.valueOf("").equals( null );
	}

	
	@Test( expectedExceptions = NullPointerException.class )
	public void comparisonNull () {
		HighwayType.valueOf("").compareTo( null );
		assert false;  // is never reached due to an exception
	}
	
	
	@Test
	public void hash () {
		assert HighwayRef.valueOf("A 4").hashCode() == HighwayRef.valueOf("A 4").hashCode();
		assert HighwayRef.valueOf("A 4").hashCode() != HighwayRef.valueOf("").hashCode();
		assert HighwayRef.valueOf("A 4").hashCode() != HighwayRef.valueOf("B 10").hashCode();
	}
	
}
