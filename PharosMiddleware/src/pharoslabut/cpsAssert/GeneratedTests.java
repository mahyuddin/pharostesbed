package pharoslabut.cpsAssert;

import static org.junit.Assert.*;
import org.junit.Test;

public class GeneratedTests{ 
	@Test
	public void testSimple() { 
		assertEquals("Simple Test1", 2.3, 2.5, 0.5);
		assertEquals("Simple Test2", 2.1, 2.5, 0.0);
	}

	@Test
	public void testAnother() { 
		assertEquals(2.1, 2.5, 0.3);
	}
}