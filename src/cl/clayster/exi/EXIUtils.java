package cl.clayster.exi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EXIUtils {
	
	public static final String canonicalSchemaLocation = "./res/canonicalSchema.xsd"; 
	public static final String schemasFileLocation = "./res/schemas.xml";
	public static final String configurationIdLocation = "./res/configId.txt";
	public static final char[] hexArray = "0123456789abcdef".toCharArray();
	public static final String schemasFolder = "./res/";
	public static final String REG_KEY = "exi_config_id";	
	
	
	public static String bytesToHex(byte[] bytes){
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	/**
	 * Looks for all schema files (*.xsd) in the given folder and creates two new files: 
	 * a canonical schema file which imports all existing schema files;
	 * and an xml file called schema.xml which contains each schema namespace, file size in bytes and its md5Hash code 
	 *   
	 * 
	 * @param folderLocation
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void generateBoth() throws NoSuchAlgorithmException, IOException{
		File folder = new File(schemasFolder);
        File[] listOfFiles = folder.listFiles();
        File file;
        String fileLocation;
        
		MessageDigest md = MessageDigest.getInstance("MD5");
		InputStream is;
		DigestInputStream dis;
		
		String namespace = null, md5Hash = null;
		int r;
		
		// variables to write the stanzas in the right order (namepsace alfabethical order)
        List<String> namespaces = new ArrayList<String>();		
        HashMap<String, String> schemasStanzas = new HashMap<String, String>();	
        HashMap<String, String> canonicalSchemaStanzas = new HashMap<String, String>();
        int n = 0;
            
            for (int i = 0; i < listOfFiles.length; i++) {
            	file = listOfFiles[i];
            	if (file.isFile() && file.getName().endsWith(".xsd") && !file.getName().endsWith("canonicalSchema.xsd")) {
            	// se hace lo siguiente para cada archivo XSD en la carpeta folder	
            		fileLocation = file.getAbsolutePath();
            		
					r = 0;
					md.reset();
					StringBuilder sb = new StringBuilder();
	            	
					if(fileLocation == null)	break;
					is = Files.newInputStream(Paths.get(fileLocation));
					dis = new DigestInputStream(is, md);
					
					// leer el archivo y guardarlo en sb
					while(r != -1){
						r = dis.read();
						sb.append((char)r);
					}
					
					// buscar el namespace del schema
					namespace = getAttributeValue(sb.toString(), "targetNamespace");
					md5Hash = bytesToHex(md.digest());
	
					n = 0;
					while(n < namespaces.size() &&
							namespaces.get(n) != null && 
							namespaces.get(n).compareToIgnoreCase(namespace) <= 0){
						n++;
					}
					namespaces.add(n, namespace);
					schemasStanzas.put(namespace, "<schema ns='" + namespace + "' bytes='" + file.length() + "' md5Hash='" + md5Hash 
							+ "' schemaLocation='" + file.getCanonicalPath() + "' url=''/>");
					canonicalSchemaStanzas.put(namespace, "<xs:import namespace='" + namespace + "' schemaLocation='" + file.getCanonicalPath() + "'/>");
            	}
			}
            //variables to write the stanzas and canonicalSchema files
            File stanzasFile = new File(schemasFileLocation);
            BufferedWriter stanzasWriter = new BufferedWriter(new FileWriter(stanzasFile));
            File canonicalSchema = new File(canonicalSchemaLocation);
            BufferedWriter canonicalSchemaWriter = new BufferedWriter(new FileWriter(canonicalSchema));
            
            
            stanzasWriter.write("<setup xmlns=\'http://jabber.org/protocol/compress/exi\'"
            		+ " version=\'1\' strict=\'true\' blockSize=\'1024\'"
            		+ " valueMaxLength=\'32\' valuePartitionCapacity=\'100\'>");
            canonicalSchemaWriter.write("<?xml version='1.0' encoding='UTF-8'?> \n\n<xs:schema \n\txmlns:xs='http://www.w3.org/2001/XMLSchema' \n\ttargetNamespace='urn:xmpp:exi:cs' \n\txmlns='urn:xmpp:exi:cs' \n\telementFormDefault='qualified'>\n");
            for(String ns : namespaces){
            	stanzasWriter.write("\n\t" + schemasStanzas.get(ns));
                canonicalSchemaWriter.write("\n\t" + canonicalSchemaStanzas.get(ns));
            }
            stanzasWriter.write("\n</setup>");
			canonicalSchemaWriter.write("\n</xs:schema>");
			
			stanzasWriter.close();
            canonicalSchemaWriter.close();
	}
	
	public static String readFile(String fileLocation){
		try{
			return new String(Files.readAllBytes(new File(fileLocation).toPath()));
		}catch (IOException e) {
			return null;
		}
	}
	
	public static void writeFile(String fileName, String content) throws IOException {
		if(fileName != null && content != null){
			FileOutputStream out = new FileOutputStream(fileName);
			out.write(content.getBytes());
			out.close();
		}
	}
	
	public static String getAttributeValue(String text, String attribute) {
		if(text.indexOf(attribute) == -1){
			return null;
		}
		text = text.substring(text.indexOf(attribute) + attribute.length());	// desde despues de targetNamespace	
    	text = text.substring(0, text.indexOf('>'));	// cortar lo que viene despues del próximo '>'
    	char comilla = '"';
    	if(text.indexOf(comilla) == -1){
    		comilla = '\'';
    	}
    	text = text.substring(text.indexOf(comilla) + 1);	// cortar lo que hay hasta la primera comilla (inclusive)
    	text = text.substring(0, text.indexOf(comilla));		// cortar lo que hay despues de la nueva primera comilla/segunda comilla de antes (inclusive)
		return text;
	}
	
}
