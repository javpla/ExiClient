package cl.clayster.exi.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cl.clayster.exi.EXIPacketLogger;
import cl.clayster.exi.EXISetupConfiguration;

@RunWith(Parameterized.class)
public class TestTest extends AbstractTest {
	
	public TestTest(EXISetupConfiguration exiConfig1, EXISetupConfiguration exiConfig2, String message) {
		super(exiConfig1, exiConfig2, message);
		client1.addEXIEventListener(new EXIPacketLogger("1"));
		client2.addEXIEventListener(new EXIPacketLogger("2"));
	}

	@Override
	public void beforeConnect(){}
	
	@Parameters
	public static Collection<Object[]> data() {
		
		Object[][] data = new Object[][] {
				{null, new EXISetupConfiguration(), "Client2 uses EXI compression with default values"}
				};
		return Arrays.asList(data);
	}
	
	@Override
	public void testAll() {
		//super.testAll();
		//testSimpleMessage(6);
		//testMessages();
		//testIQs();
		//testSimpleIQ(8);
		
	}
}
