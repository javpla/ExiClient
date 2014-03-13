package cl.clayster.exi.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIUtils;
import cl.clayster.exi.EXIXMPPConnection;

@RunWith(Parameterized.class)
public class NoSchemasOnServerTest extends AbstractTest {
	
	public NoSchemasOnServerTest(boolean compression1, EXISetupConfiguration exiConfig1,
			boolean compression2, EXISetupConfiguration exiConfig2, String message) {
		super(compression1, exiConfig1, compression2, exiConfig2, message);
	}

	@Parameters
	public static Collection<Object[]> data() {
		
		// delete previous configuration id register (just for the first test which should try quick configurations, but will then do normal negotiation instead)
		EXIUtils.saveConfigId(null);
		
		Object[][] data = new Object[][] {
				//{true, body, false, null, "client1 uploads exi body files."}};
				{true, null, false, null, "a:client1 uploads binary files."},
				{true, null, false, null, "b:client1 uploads exi-compressed files (only the exi body)."},
				{true, null, false, null, "c:client1 uploads exi-compressed files."},
				{true, null, false, null, "d:client1 uploads a URL for the server to download it, or else uploads binary files."},
				{true, null, false, null, "e:client1 aborts compression negotiation after receiving missing schema files."}};
		return Arrays.asList(data);
	}
	
	@Test
	public void test() {
		clearClassesFolder();
		switch(message.charAt(0)){
			case 'a': client1.setUploadSchemaOption(EXIXMPPConnection.UPLOAD_BINARY);
			break;
			case 'b': client1.setUploadSchemaOption(EXIXMPPConnection.UPLOAD_EXI_BODY);
			break;
			case 'c': client1.setUploadSchemaOption(EXIXMPPConnection.UPLOAD_EXI_DOCUMENT);
			break;
			case 'd': client1.setUploadSchemaOption(EXIXMPPConnection.UPLOAD_URL);
			break;
			case 'e': client1.setUploadSchemaOption(EXIXMPPConnection.ABORT_COMPRESSION);
			break;
		}
		
		testSimpleMessage();
	}
}
