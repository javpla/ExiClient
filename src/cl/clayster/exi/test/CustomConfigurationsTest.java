package cl.clayster.exi.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cl.clayster.exi.EXISetupConfiguration;

import com.siemens.ct.exi.CodingMode;

@RunWith(Parameterized.class)
public class CustomConfigurationsTest extends AbstractTest {
	
	@Parameters
	public static Collection<Object[]> data() {
		EXISetupConfiguration exiBytePacked = new EXISetupConfiguration(true);
		exiBytePacked.setAlignment(CodingMode.BYTE_PACKED);
		exiBytePacked.setBlockSize(2048);
		
		EXISetupConfiguration exiCompression = new EXISetupConfiguration(true);
		exiCompression.setAlignment(CodingMode.COMPRESSION);
		exiCompression.setValueMaxLength(100);
		
		EXISetupConfiguration exiPre = new EXISetupConfiguration(true);
		exiPre.setAlignment(CodingMode.PRE_COMPRESSION);
		exiPre.setStrict(true);
		
		Object[][] data = new Object[][] { 
				{true, null, false, exiCompression, "client1 uses default EXI configurations while client2 uses alignment with compression and blocksize = 2048."},
				{true, exiBytePacked, true, exiPre, "client1 uses byte-alignment and value max length set to 100, while client2 uses pre-compression alignment and strict mode."}};
		return Arrays.asList(data);
	}
	
	public CustomConfigurationsTest(boolean compression1, EXISetupConfiguration exiConfig1,
			boolean compression2, EXISetupConfiguration exiConfig2, String message) {
		super(compression1, exiConfig1, compression2, exiConfig2, message);
	}
	
	@Test
	public void test() {
		testSimpleMessage();
	}
}
