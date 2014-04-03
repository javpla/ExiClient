package cl.clayster.exi.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cl.clayster.exi.EXIPacketLogger;
import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIXMPPConnection;

@RunWith(Parameterized.class)
public class No extends AbstractTest {
	
	public No(EXISetupConfiguration exiConfig1, EXISetupConfiguration exiConfig2, String info){
		super(exiConfig1, exiConfig2, info);
		
	    this.client1 = new EXIXMPPConnection(config1, exiConfig1);
		this.client2 = new EXIXMPPConnection(config2, exiConfig2);
	}
	
	@Override
	public void connect() {
		super.connect();
		client1.addEXIEventListener(new EXIPacketLogger("1", true, false));
		client2.addEXIEventListener(new EXIPacketLogger("2", true, false));
	};

	@Parameters
	public static Collection<Object[]> data() {
		EXISetupConfiguration def = new EXISetupConfiguration(true);
		EXISetupConfiguration cn = new EXISetupConfiguration(true);
		try {
			cn.setCanonicalSchemaLocation("C:/Users/Javier/workspace/Personales/ExiClient/schemas/canonicalSchemas/cn.xsd");
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		Object[][] data = new Object[][] {
				{def, def, "-Both clients use EXI compression with default values but different canonical schemas."},
				};
		return Arrays.asList(data);
	}
}
