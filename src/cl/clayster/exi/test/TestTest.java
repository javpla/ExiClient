package cl.clayster.exi.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cl.clayster.exi.EXISetupConfiguration;

@RunWith(Parameterized.class)
public class TestTest extends AbstractTest {
	
	public TestTest(boolean compression1, EXISetupConfiguration exiConfig1,
			boolean compression2, EXISetupConfiguration exiConfig2, String message) {
		super(compression1, exiConfig1, compression2, exiConfig2, message);
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				{false, null, true, null, "Both clients use EXI compression with default values."}
				};
		return Arrays.asList(data);
	}
	
	@Test
	public void test() {
		//testIQs();
		//testSimpleIQ(7);
		//testSimpleExtendedMessage();
		testMessages();
	}
}
