package cl.clayster.exi.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIUtils;

@RunWith(Parameterized.class)
public class DefaultValuesTest extends AbstractTest {
	
	public DefaultValuesTest(boolean compression1, EXISetupConfiguration exiConfig1,
			boolean compression2, EXISetupConfiguration exiConfig2, String message) {
		super(compression1, exiConfig1, compression2, exiConfig2, message);
	}

	@Parameters
	public static Collection<Object[]> data() {
		EXISetupConfiguration exiQuickConfig = new EXISetupConfiguration(true);
		
		// delete previous configuration id register (just for the first test which should try quick configurations, but will then do normal negotiation instead)
		EXIUtils.saveConfigId(null);
		
		Object[][] data = new Object[][] {
			    {true, exiQuickConfig, false, null, "client1 uses EXI with default values and tries to use quick configurations (which do not exist).client2 uses normal XMPP."},
			    {true, exiQuickConfig, false, null, "client1 uses EXI with default values and quick configurations. client2 uses normal XMPP."},
				{false, null, true, null, "Both clients use EXI compression with default values."},
				{true, null, true, null, "Both clients use EXI compression with default values."},
				{true, exiQuickConfig, true, null, "Both clients use EXI with default values and client1 uses quick configurations setup."},
				{true, exiQuickConfig, true, exiQuickConfig, "Both clients use EXI with default values and quick configurations setup."},
				{true, null, false, null, "client1 uses EXI with default values and client2 uses normal XMPP. αινσϊ δλοφό ρ"},
				{false, null, true, null, "client1 uses EXI with default values and client2 uses normal XMPP. αινσϊ δλοφό ρ"}
				};
		return Arrays.asList(data);
	}
	
	@Test
	public void test() {
		testMessages();
	}
}
