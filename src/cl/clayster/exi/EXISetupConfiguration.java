package cl.clayster.exi;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.siemens.ct.exi.CodingMode;
import com.siemens.ct.exi.FidelityOptions;
import com.siemens.ct.exi.exceptions.UnsupportedOption;
import com.siemens.ct.exi.helpers.DefaultEXIFactory;

/**
 * Contains all relevant values to setup EXI compression options in order to propose them to the server
 * and use them for compresses communication according to the XEP-0322 protocol.
 * @author Javier Placencio
 *
 */
public class EXISetupConfiguration extends DefaultEXIFactory{
	
	protected String configurationId;
	protected String schemaId;
	private boolean sessionWideBuffers = false;
	
	/**
	 * Constructs a new EXISetupConfigurations and initializes it with Default Values.
	 */
	public EXISetupConfiguration(){
		setDefaultValues();
	}
	
	/**
	 * Sets default values as they are defined in XEP-0322.
	 */
	protected void setDefaultValues() {
		setDefaultValues(this);
		
		try {
			getFidelityOptions().setFidelity(FidelityOptions.FEATURE_PREFIX, true);
		} catch (UnsupportedOption e) {
			e.printStackTrace();
		}
		
		setValueMaxLength(64);
		setValuePartitionCapacity(64);
		
		setLocalValuePartitions(false);
		//setMaximumNumberOfBuiltInElementGrammars(0);
		//setMaximumNumberOfBuiltInProductions(0);
	}
	
	public String getSchemaId() {
		return schemaId == null ? "urn:xmpp:exi:default" : schemaId;
	}

	public void setSchemaId(String schemaId) {
		this.schemaId = schemaId;
	}
	
	/**
	 * Returns a number to represent each data alignment option.
	 * Possible values are 0, 1, 2 or 3 meaning Bit alignment, byte alignment, pre compression, and compression, respectively.
	 * @return the alignment code representing the chosen option
	 */
	public int getAlignmentCode() {
		CodingMode cm = getCodingMode();
		int alignment = (cm.equals(CodingMode.BIT_PACKED)) ? 0 :
        	(cm.equals(CodingMode.BYTE_PACKED)) ? 1 :
        		(cm.equals(CodingMode.PRE_COMPRESSION)) ? 2 :
        			(cm.equals(CodingMode.COMPRESSION)) ? 3: 0;
		return alignment;
	}
	
	/**
	 * Returns the current aligment option as it is represented within XML stanzas according to XEP-0322. 
	 * @return a string representing the alignment option being used
	 */
	public String getAlignmentString() {
		CodingMode cm = getCodingMode();
		String alignment = (cm.equals(CodingMode.BIT_PACKED)) ? "bit-packed" :
        	(cm.equals(CodingMode.BYTE_PACKED)) ? "byte-packed" :
        		(cm.equals(CodingMode.PRE_COMPRESSION)) ? "pre-compression" :
        			(cm.equals(CodingMode.COMPRESSION)) ? "compression": "bit-packed";
		return alignment;
	}
	
	/**
	 * Sets if session wide buffers are being used or not. Session wide buffers allow 
	 * compression related values to be remembered during the connection with the server, achieving better
	 * encoding and compactness. 
	 * @param sessionWideBuffers
	 */
	public void setSessionWideBuffers(boolean sessionWideBuffers){
		this.sessionWideBuffers = sessionWideBuffers;
	}
	
	public boolean isSessionWideBuffers(){
		return this.sessionWideBuffers;
	}

	public void setStrict(boolean strict) {
		try {
			fidelityOptions.setFidelity(FidelityOptions.FEATURE_STRICT, strict);
		} catch (UnsupportedOption e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Returns a String representing the location of the canonical schema that this configuration will use
	 * for compression.
	 * @return a String representing the location of the canonical schema for this configurations
	 */
	public String getCanonicalSchemaLocation() {
		if(schemaId != null){
			return EXIUtils.getCanonicalSchemaLocationById(schemaId);
		}
		else{
			return EXIUtils.defaultCanonicalSchemaLocation;
		}
	}
	
	/**
	 * 
	 * @return the configuration ID for these EXI configurations
	 */
	public String getConfigutarionId() {
		return configurationId;
	}

	void setConfigurationId(String configurationId) {
		this.configurationId = configurationId;
	}
	
	/**
	 * Returns an XML representation of the current EXI configurations.
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("<setup schemaId='").append(getSchemaId()).append("'");
		
		EXISetupConfiguration def = new EXISetupConfiguration();
		// as attributes
		if(!getCodingMode().equals(def.getCodingMode())){
			if(getCodingMode().equals(CodingMode.COMPRESSION)){
				sb.append(" compression='true'");
			}
			else{
				sb.append(" alignment='").append(getAlignmentString()).append("'");
			}
		}
		if(!getFidelityOptions().equals(def.getFidelityOptions())){
			FidelityOptions fo = getFidelityOptions();
			if(fo.isStrict()){
				sb.append(" strict='true'");
			}
			else{
				if(fo.isFidelityEnabled(FidelityOptions.FEATURE_COMMENT)){
					sb.append(" " + SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_COMMENT) + "='true'");
				}
				if(fo.isFidelityEnabled(FidelityOptions.FEATURE_DTD)){
					sb.append(" " + SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_DTD) + "='true'");
				}
				if(fo.isFidelityEnabled(FidelityOptions.FEATURE_LEXICAL_VALUE)){
					sb.append(" " + SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_LEXICAL_VALUE) + "='true'");
				}
				if(fo.isFidelityEnabled(FidelityOptions.FEATURE_PI)){
					sb.append(" " + SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_PI) + "='true'");
				}
				if(fo.isFidelityEnabled(FidelityOptions.FEATURE_PREFIX)){
					sb.append(" " + SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_PREFIX) + "='true'");
				}
				if(fo.isFidelityEnabled(FidelityOptions.FEATURE_SC) && 
						!(getCodingMode().equals(CodingMode.PRE_COMPRESSION) || getCodingMode().equals(CodingMode.COMPRESSION))){
					sb.append(" " + SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_SC) + "='true'");
				}
			}
		}
		if(getBlockSize() != def.getBlockSize()){
			sb.append(" blockSize='").append(getBlockSize()).append("'");
		}
		if(getValueMaxLength() != def.getValueMaxLength()){
			sb.append(" valueMaxLength='").append(getValueMaxLength()).append("'");
		}
		if(getValuePartitionCapacity() != def.getValuePartitionCapacity()){
			sb.append(" valuePartitionCapacity='").append(getValuePartitionCapacity()).append("'");
		}
		if(isSessionWideBuffers()){
			sb.append(" sessionWideBuffers='true'");
		}
		sb.append(">");

		sb.append("</setup>");
		return sb.toString();
	}
	
	/**
	 * Saves this EXI configurations to a file, unless the same configurations have been saved already
	 * @return true if this configurations are saved, false otherwise 
	 * @throws IOException
	 */
	public boolean saveConfiguration() throws IOException {
		String content = this.toString();
        
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			String configId = EXIUtils.bytesToHex(md.digest(content.getBytes()));
			setConfigurationId(configId);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}		
		
		String fileName = EXIUtils.exiFolder + configurationId + ".xml";
		if(new File(fileName).exists()){
			return true;
		}
		else{
			if(EXIUtils.writeFile(fileName, content)){
				return true;
			}
			else{
				System.err.println("Error while trying to save the file. Configurations were not saved.");
				return false;
			} 
		}
	}

	/**
	 * Looks for a saved EXISetupConfiguration that have been previously used for compression.
	 * @param configId the configuration ID being loaded 
	 * @return the saved EXISetupConfiguration if it exists, null otherwise
	 * @throws DocumentException
	 */
	public static EXISetupConfiguration parseQuickConfigId(String configId) throws DocumentException {
		String fileLocation = EXIUtils.exiFolder + configId + ".xml";
		String content = EXIUtils.readFile(fileLocation);
		if(content == null)
			return null;
		
		Element configElement = DocumentHelper.parseText(content).getRootElement();
		
		EXISetupConfiguration exiConfig = new EXISetupConfiguration();
		exiConfig.setConfigurationId(configId);
		// iterate through attributes of root 
        for (@SuppressWarnings("unchecked") Iterator<Attribute> i = configElement.attributeIterator(); i.hasNext(); ) {
            Attribute att = (Attribute) i.next();
            if(att.getName().equals("schemaId")){
            	exiConfig.setSchemaId(att.getValue());
            	if(!new File(exiConfig.getCanonicalSchemaLocation()).exists()){
            		return null;
            	}	
            }
            else if(att.getName().equals("alignment")){
            	exiConfig.setCodingMode(SetupValues.getCodingMode(att.getValue()));
            }
            else if(att.getName().equals("compression")){
            	if("true".equals(att.getValue())){
            		exiConfig.setCodingMode(CodingMode.COMPRESSION);
            	}
            }
            else if(att.getName().equals("blockSize")){
            	exiConfig.setBlockSize(Integer.valueOf(att.getValue()));
            }
            else if(att.getName().equals("valueMaxLength")){
            	exiConfig.setValueMaxLength(Integer.valueOf(att.getValue()));
            }
            else if(att.getName().equals("valuePartitionCapacity")){
            	exiConfig.setValuePartitionCapacity(Integer.valueOf(att.getValue()));
            }
            else if(att.getName().equals("sessionWideBuffers")){
            	if("true".equals(att.getValue())){
            		exiConfig.setSessionWideBuffers(true);
            	}
            }
            else if(att.getName().equals("strict")){
            	if("true".equals(att.getValue())){
            		exiConfig.setFidelityOptions(FidelityOptions.createStrict());
            	}
            }
            else 
	        	try {
	        		if(att.getName().equals(SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_COMMENT)) && "true".equals(att.getValue())){
	        			exiConfig.getFidelityOptions().setFidelity(FidelityOptions.FEATURE_COMMENT, true);
	        		}
	        		else if(att.getName().equals(SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_DTD)) && "true".equals(att.getValue())){
	        			exiConfig.getFidelityOptions().setFidelity(FidelityOptions.FEATURE_DTD, true);
	        		}
	        		else if(att.getName().equals(SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_LEXICAL_VALUE)) && "true".equals(att.getValue())){
	        			exiConfig.getFidelityOptions().setFidelity(FidelityOptions.FEATURE_LEXICAL_VALUE, true);
	        		}
	        		else if(att.getName().equals(SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_PI)) && "true".equals(att.getValue())){
	        			exiConfig.getFidelityOptions().setFidelity(FidelityOptions.FEATURE_PI, true);
	        		}
	        		else if(att.getName().equals(SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_PREFIX)) && "true".equals(att.getValue())){
	        			exiConfig.getFidelityOptions().setFidelity(FidelityOptions.FEATURE_PREFIX, true);
	        		}
	        		else if(att.getName().equals(SetupValues.getFidelityOptionString(FidelityOptions.FEATURE_SC)) && "true".equals(att.getValue())){
	        			exiConfig.getFidelityOptions().setFidelity(FidelityOptions.FEATURE_SC, true);
	        		}
				} catch (UnsupportedOption e) {
					e.printStackTrace();
				}
        }
		return exiConfig;
	}

	/**
	 * Checks if the configuration given has been used before, in order to know if quick setup configurations can be used.
	 * @param exiConfig EXI Setup Configurations being cheked
	 * @return true if the configuration has been used, false otherwise
	 */
	public boolean exists() {
		String content = this.toString();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			String configId = EXIUtils.bytesToHex(md.digest(content.getBytes()));
			setConfigurationId(configId);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}		
		
		String fileName = EXIUtils.exiFolder + configurationId + ".xml";
		if(new File(fileName).exists()){
			return true;
		}
		else{
			return false;
		}
	}
}
