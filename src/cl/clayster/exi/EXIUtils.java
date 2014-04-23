package cl.clayster.exi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Iterator;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class EXIUtils {
	
	public static final String schemasFolder = "./schemas/";
	public static final String schemasFileLocation = schemasFolder + "schemas.xml";
	public static final String exiFolder = "./schemas/canonicalSchemas/";
	public static final String defaultCanonicalSchemaLocation = exiFolder + "defaultSchema.xsd";
	
	
	public static final char[] hexArray = "0123456789abcdef".toCharArray();
	public static final String REG_CONFIG_ID_KEY = "exi_config_id";
	public static final String REG_SCHEMA_ID_LOC = "exi_schema";
	public static final String DEFAULT_STRICT = "false";
	public static final String DEFAULT_BLOCKSIZE = "1000000";
	
	
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
	 * Looks for all schema files (*.xsd) in the schemas folder and creates two new files:<br> 
	 * <b>canonicalSchema.xsd</b> which imports all schema files in the given folder;<br>
	 * <b>schema.xml</b> which contains each schema namespace, file size in bytes, and its md5Hash code 
	 * Finally creates a default canonical schema if it does not exist
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws DocumentException 
	 */
	static void generateSchemasFile() throws IOException{
		File folder = new File(EXIUtils.schemasFolder);
		if(!folder.exists()){
			folder.mkdir();
		}
		if(!new File(EXIUtils.exiFolder).exists()){
			new File(EXIUtils.exiFolder).mkdir();
		}
        File[] listOfFiles = folder.listFiles();
        File file;
        String fileLocation;
        
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return;
		}
		InputStream is;
		DigestInputStream dis;
		
		String namespace = null, md5Hash = null;
		int r;
		
		// variables to write the stanzas in the right order (namepsace alfabethical order)
        List<String> namespaces = new ArrayList<String>();		
        HashMap<String, String> schemasStanzas = new HashMap<String, String>();
        int n = 0;
            
            for (int i = 0; i < listOfFiles.length; i++) {
            	file = listOfFiles[i];
            	if (file.isFile() && file.getName().endsWith(".xsd")) {
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
					//namespace = DocumentHelper.parseText(sb.toString()).getRootElement().attributeValue("targetNamespace");
					
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
            	}
			}
            
            // variables to write the stanzas and canonicalSchema files
            File stanzasFile = new File(schemasFileLocation);
            BufferedWriter stanzasWriter = new BufferedWriter(new FileWriter(stanzasFile));
            stanzasWriter.write("<schemas>");
            
            for(String ns : namespaces){
            	stanzasWriter.write("\n\t" + schemasStanzas.get(ns));
            }
            stanzasWriter.write("\n</schemas>");
            stanzasWriter.close();
	}
	
	/**
	 * Generates a canonical schema out of the schemas contained in the schemas file.
	 * @return schemaId of the created canonicalSchema
	 * @throws IOException
	 */
	static String generateCanonicalSchema() throws IOException {
		return generateCanonicalSchema(new ArrayList<String>(0));
	}
	
	/**
	 * Generates a canonical schema out of the schemas contained in the schemas file, ignoring those contained in <i>missingSchemas</i> 
	 * 
	 * @param missingSchemas list containing the namespace of the schemas to be ignored (as strings)
	 * @return schemaId of the created canonicalSchema
	 * @throws IOException
	 */
	static String generateCanonicalSchema(List<String> missingSchemas) throws IOException {
		Element setup;
		try {
			setup = DocumentHelper.parseText(EXIUtils.readFile(EXIUtils.schemasFileLocation)).getRootElement();
		} catch (DocumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version='1.0' encoding='UTF-8'?>"
        		+ "\n\n<xs:schema "
        		+ "\n\txmlns:xs='http://www.w3.org/2001/XMLSchema'"
        		+ "\n\txmlns:stream='http://etherx.jabber.org/streams'"
        		+ "\n\txmlns:exi='http://jabber.org/protocol/compress/exi'"
        		+ "\n\ttargetNamespace='urn:xmpp:exi:cs'"
        		+ "\n\telementFormDefault='qualified'>");
        
		Element schema;
        for (@SuppressWarnings("unchecked") Iterator<Element> i = setup.elementIterator("schema"); i.hasNext(); ) {
        	schema = i.next();
        	String ns = schema.attributeValue("ns");
        	if(!missingSchemas.contains(ns)){
        		sb.append("\n\t<xs:import namespace='" + ns + "'/>");
        	}
        }
        sb.append("\n</xs:schema>");
        
        String content = sb.toString();
        MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		String schemaId = EXIUtils.bytesToHex(md.digest(content.getBytes()));
        String fileName = getCanonicalSchemaLocationById(schemaId);
        
        BufferedWriter newCanonicalSchemaWriter = new BufferedWriter(new FileWriter(fileName));
        newCanonicalSchemaWriter.write(content);
        newCanonicalSchemaWriter.close();
        return schemaId;
	}
	
	/**
	 * Generates XEP-0322's default canonical schema
	 * @throws IOException
	 */
	static void generateDefaultCanonicalSchema() throws IOException {
		String[] schemasNeeded = {"http://etherx.jabber.org/streams", "http://jabber.org/protocol/compress/exi"};
		boolean[] schemasFound = {false, false};
		Element setup;
		try {
			setup = DocumentHelper.parseText(EXIUtils.readFile(EXIUtils.schemasFileLocation)).getRootElement();
		} catch (DocumentException e1) {
			e1.printStackTrace();
			return;
		}
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version='1.0' encoding='UTF-8'?>"
        		+ "\n\n<xs:schema "
        		+ "\n\txmlns:xs='http://www.w3.org/2001/XMLSchema'"
        		+ "\n\txmlns:stream='http://etherx.jabber.org/streams'"
        		+ "\n\txmlns:exi='http://jabber.org/protocol/compress/exi'"
        		+ "\n\ttargetNamespace='urn:xmpp:exi:default'"
        		+ "\n\telementFormDefault='qualified'>");
        
		Element schema;
        for (@SuppressWarnings("unchecked") Iterator<Element> i = setup.elementIterator("schema"); i.hasNext(); ) {
        	schema = i.next();
        	String ns = schema.attributeValue("ns");
        	if(ns.equalsIgnoreCase(schemasNeeded[0])){
        		schemasFound[0] = true;
        		if(schemasFound[1]){
        			break;
        		}
        	}
        	else if(ns.equalsIgnoreCase(schemasNeeded[1])){
        		schemasFound[1] = true;
        		if(schemasFound[0]){
        			break;
        		}
        	}
        }
        if(schemasFound[0] && schemasFound[1]){
    		sb.append("\n\t<xs:import namespace='" + schemasNeeded[0] + "'/>");
    		sb.append("\n\t<xs:import namespace='" + schemasNeeded[1] + "'/>");
    	}
        else{
        	throw new IOException("Missing schema for default canonical schema: " + (schemasFound[0] ? schemasNeeded[0] : schemasNeeded[1])); 
        }
        sb.append("\n</xs:schema>");
        
        String content = sb.toString();
        String fileName = EXIUtils.defaultCanonicalSchemaLocation;
        
        BufferedWriter newCanonicalSchemaWriter = new BufferedWriter(new FileWriter(fileName));
        newCanonicalSchemaWriter.write(content);
        newCanonicalSchemaWriter.close();
        return;
	}
	
	static String getCanonicalSchemaLocationById(String schemaId) {
		return EXIUtils.exiFolder + schemaId + ".xsd";
	}

	public static String readFile(String fileLocation){
		try{
			return new String(Files.readAllBytes(new File(fileLocation).toPath()));
		}catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean writeFile(String fileName, String content) throws IOException {
		try {
			if(fileName != null && content != null){
				FileOutputStream out;
				
				out = new FileOutputStream(fileName);
				out.write(content.getBytes());
				out.close();
				return true;
			}
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return false;
	}
	
	public static String getAttributeValue(String text, String attribute) {
		if(text.indexOf(attribute) == -1){
			return null;
		}
		text = text.substring(text.indexOf(attribute) + attribute.length());	// desde despues de targetNamespace	
    	text = text.substring(0, text.indexOf('>'));	// cortar lo que viene despues del próximo '>'
    	char comilla = '\'';
    	if(text.indexOf(comilla) == -1){
    		comilla = '\"';
    	}
    	text = text.substring(text.indexOf(comilla) + 1);	// cortar lo que hay hasta la primera comilla (inclusive)
    	text = text.substring(0, text.indexOf(comilla));		// cortar lo que hay despues de la nueva primera comilla/segunda comilla de antes (inclusive)
		return text;
	}
	
	/**
	 * Saves a register with the value given as a parameter or deletes it if the parameter is null
	 * @param configId value of the register or null to remove
	 */
	public static void saveExiConfig(EXISetupConfiguration exiConfig) {
		if(exiConfig != null){
			try {
				exiConfig.saveConfiguration();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
