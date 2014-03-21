package cl.clayster.exi.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIUtils;
import cl.clayster.exi.EXIXMPPConnection;

/**
 * Tests uploading schemas to the server by every way possible. Before starting every test it removes the schemas contained on the server
 * to make sure all schemas are missing. Uses quick configurations setup to make this faster 
 * @author Javier
 *
 */
@RunWith(Parameterized.class)
public class NoSchemasOnServerTest extends AbstractTest {
	
	public NoSchemasOnServerTest(EXISetupConfiguration exiConfig1,
			EXISetupConfiguration exiConfig2, String message) {
		super(exiConfig1, exiConfig2, message);
	}

	@Parameters
	public static Collection<Object[]> data() {
		
		// delete previous configuration id register (just for the first test which should try quick configurations, but will then do normal negotiation instead)
		EXIUtils.saveConfigId(null);
		EXISetupConfiguration exiConfig = new EXISetupConfiguration(true);
		
		Object[][] data = new Object[][] {
				{exiConfig, null, "a:client1 uploads binary files."},
				{exiConfig, null, "b:client1 uploads exi-compressed files (only the exi body)."},
				{exiConfig, null, "c:client1 uploads exi-compressed files."},
				{exiConfig, null, "d:client1 uploads a URL for the server to download it, or else uploads binary files."}
				};
		return Arrays.asList(data);
	}
	
	@Override
	public void testAll() {
		clearClassesFolder();
		switch(testInfo.charAt(0)){
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
		
		super.testAll();
	}
}
