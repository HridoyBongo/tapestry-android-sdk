import com.tapad.tapestry.DummyMath;

import android.test.AndroidTestCase;

public class TestDummyMath extends AndroidTestCase {
	public void testAdd() {
		assertEquals(5, DummyMath.add(3, 2));
	}
}
