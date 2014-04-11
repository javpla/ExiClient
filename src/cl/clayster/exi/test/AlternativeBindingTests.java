package cl.clayster.exi.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cl.clayster.exi.EXIPacketLogger;
import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIXMPPAlternativeConnection;

import com.siemens.ct.exi.CodingMode;

@RunWith(Parameterized.class)
public class AlternativeBindingTests extends AbstractTest {
	
	EXISetupConfiguration exiConfig1, exiConfig2;
	
	public AlternativeBindingTests(EXISetupConfiguration exiConfig1, EXISetupConfiguration exiConfig2, String info){
		super(exiConfig1, exiConfig2, info);
		this.exiConfig1 = exiConfig1;
		this.exiConfig2 = exiConfig2;
	}
	
	@Override
	void beforeConnect() {
		timeOut = 60;
	}
	
	@Override
	public void connect() {
		addExtensionProviders();
		this.client1 = new EXIXMPPAlternativeConnection(config1, exiConfig1);
		this.client2 = new EXIXMPPAlternativeConnection(config2, exiConfig2);
		super.connect();
		client1.addEXIEventListener(new EXIPacketLogger("1", true, false));
		client2.addEXIEventListener(new EXIPacketLogger("2", true, false));
	};

	@Parameters
	public static Collection<Object[]> data() {
		EXISetupConfiguration def = null;
		EXISetupConfiguration custom = new EXISetupConfiguration();
		custom.setCodingMode(CodingMode.COMPRESSION);
		custom.setBlockSize(2048);
		
		Object[][] data = new Object[][] {
				{def, custom, "Client1 uses default configurations and client2 uses Compression for alignment and blocksize=2048"}
				};
		return Arrays.asList(data);
	}
}
