package cl.clayster.exi.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIUtils;

import com.siemens.ct.exi.CodingMode;

/**
 * Tries different negotiations between the client and the server: with and without quick configurations setup.
 * The rest of the tests will use quick configurations setup in order to make them more efficiently.
 * @author Javier Placencio
 *
 */
@RunWith(Parameterized.class)
public class NegotiationTest extends AbstractTest {
	
	public NegotiationTest(EXISetupConfiguration exiConfig1, EXISetupConfiguration exiConfig2, String message) {
		super(exiConfig1, exiConfig2, message);
	}

	@Parameters
	public static Collection<Object[]> data() {
		EXISetupConfiguration exiQuickConfig = new EXISetupConfiguration(true);
		EXISetupConfiguration def = new EXISetupConfiguration();
		EXISetupConfiguration exiBytePacked = new EXISetupConfiguration(true);
		exiBytePacked.setAlignment(CodingMode.BYTE_PACKED);
		exiBytePacked.setBlockSize(2048);
		
		EXISetupConfiguration exiCompression = new EXISetupConfiguration(true);
		exiCompression.setAlignment(CodingMode.COMPRESSION);
		exiCompression.setValueMaxLength(100);
		
		EXISetupConfiguration exiPre = new EXISetupConfiguration(true);
		exiPre.setAlignment(CodingMode.PRE_COMPRESSION);
		exiPre.setStrict(true);
		
		// delete previous configuration id register (just for the first test which should try quick configurations, but will then do normal negotiation instead)
		EXIUtils.saveConfigId(null);
		
		Object[][] data = new Object[][] {
				{exiQuickConfig, null, "client1 tries to use quick configurations (which do not exist) and then uses EXI with default values .client2 uses normal XMPP."},
				{exiCompression, def, "client1 uses default EXI configurations while client2 uses compression alignment and blocksize = 2048."},
				{exiBytePacked, exiPre, "client1 uses byte-alignment and value max length set to 100, while client2 uses pre-compression alignment and strict mode."},
				{exiBytePacked, exiPre, "client1 uses byte-alignment and value max length set to 100, while client2 uses pre-compression alignment and strict mode."},
				{null, exiCompression, "client1 uses default XMPP connection while client2 uses compression alignment and blocksize = 2048."},
				{exiQuickConfig, exiQuickConfig, "Both clients use EXI compression with quickConfiguration (default values)."},
				{def, def, "Both clients use EXI compression with default values."},
				};

		return Arrays.asList(data);
	}
}
