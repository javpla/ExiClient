package cl.clayster.exi.test;

import java.io.File;

public class TestUtils {

	static final String SERVER = "exi.clayster.cl";
	static final String CONTACT = "javier@exi.clayster.cl";	// usuario al cual se le envían mensajes
	static final String USER = "exiuser";
	static final String PASSWORD = "exiuser";
	static final String OPENFIRE_BASE = "C:/Users/Javier/workspace/Personales/openfire/target/openfire";
	static final String RES_FOLDER = "/plugins/exi/res";
	
	public static void deleteFolder(String folderLocation){
		File folder = new File(folderLocation);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles != null)
        	for (int i = 0; i < listOfFiles.length; i++) {
        		listOfFiles[i].delete();
        	}
		folder.delete();
	}
}
