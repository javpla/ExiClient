package cl.clayster.exi.test;

import java.io.IOException;
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
		client2.addEXIEventListener(new EXIPacketLogger("2"));
	}

	@Parameters
	public static Collection<Object[]> data() {
		EXISetupConfiguration exiQuickConfig = new EXISetupConfiguration(true);
		EXISetupConfiguration exiQuickConfig2 = new EXISetupConfiguration(true);
		try {
			exiQuickConfig2.setCanonicalSchemaLocation("C:/Users/Javier/workspace/Personales/ExiClient/schemas/canonicalSchemas/defaultSchema.xsd");
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		Object[][] data = new Object[][] {
				//{null, new EXISetupConfiguration(true), "Client2 uses EXI compression with default values"}
				{exiQuickConfig, exiQuickConfig2, "Both clients use EXI compression with quickConfiguration (default values)."},
				};
		return Arrays.asList(data);
	}
	
	@Override
	public void testAll() {
		super.testAll();
		//testSimpleMessage(6);
		//testMessages();
		//testIQs();
		//testSimpleIQ(0);
	}
}
