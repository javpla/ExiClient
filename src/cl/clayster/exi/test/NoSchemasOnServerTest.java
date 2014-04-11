package cl.clayster.exi.test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cl.clayster.exi.EXISetupConfiguration;
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

	@Override
	void beforeConnect() {
		timeOut = 100;
		removeExtraSchemasOnServer();
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		// delete previous configuration id register (just for the first test which should try quick configurations, but will then do normal negotiation instead)
		EXISetupConfiguration exiConfig = new EXISetupConfiguration();
		
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
		switch(testInfo.charAt(0)){
			case 'a': client1.setUploadSchemaOption(EXIXMPPConnection.UPLOAD_BINARY);
			break;
			case 'b': client1.setUploadSchemaOption(EXIXMPPConnection.UPLOAD_EXI_BODY);
			break;
			case 'c': client1.setUploadSchemaOption(EXIXMPPConnection.UPLOAD_EXI_DOCUMENT);
			break;
			case 'd': client1.setUploadSchemaOption(EXIXMPPConnection.UPLOAD_URL);
			break;
			case 'e': client1.setUploadSchemaOption(EXIXMPPConnection.USE_AVAILABLE);
			break;
		}
		
		super.testAll();
	}
	
	public static void removeExtraSchemasOnServer(){
		File file = new File(OPENFIRE_BASE + CLASSES_FOLDER);
        File[] listOfFiles = file.listFiles();
        if(listOfFiles != null){
        	// then it is a folder
        	for (int i = 0; i < listOfFiles.length; i++) {
        		file = listOfFiles[i];
        		File aux = new File("C:/Users/Javier/workspace/Personales/openfire/src/plugins/exi/classes/" + file.getName());
                if(!aux.exists()){
                	file.delete();
                }
			}
        }
        // configurations file on client to avoid quick configurations
        file = new File("C:/Users/Javier/workspace/Personales/ExiClient/schemas/canonicalSchemas");
        listOfFiles = file.listFiles();
        if(listOfFiles != null){
        	// then it is a folder
        	for (int i = 0; i < listOfFiles.length; i++) {
        		file = listOfFiles[i];
        		if(!file.getName().equals("defaultSchema.xsd")){
        			file.delete();
        		}
			}
        }
	}
}
