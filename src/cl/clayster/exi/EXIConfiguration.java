package cl.clayster.exi;

import com.siemens.ct.exi.CodingMode;
import com.siemens.ct.exi.FidelityOptions;
import com.siemens.ct.exi.exceptions.UnsupportedOption;

public class EXIConfiguration {
	
	EXISetupConfiguration esc;
	
	/** Alignments **/
	public static final int ALIGN_BIT_PACKED = 0;
	public static final int ALIGN_BYTE_PACKED = 1;
	public static final int ALIGN_PRE_COMPRESSION = 2;
	public static final int ALIGN_COMPRESSION = 3;
	
	/** Fidelity Options (preserve) **/
	public static final String PRESERVE_COMMENT = FidelityOptions.FEATURE_COMMENT;
	public static final String PRESERVE_PI = FidelityOptions.FEATURE_PI;
	public static final String PRESERVE_DTD = FidelityOptions.FEATURE_DTD;
	public static final String PRESERVE_PREFIX = FidelityOptions.FEATURE_PREFIX;
	public static final String PRESERVE_LEXICAL_VALUE = FidelityOptions.FEATURE_LEXICAL_VALUE;
	public static final String PRESERVE_SC = FidelityOptions.FEATURE_SC;
	public static final String PRESERVE_STRICT = FidelityOptions.FEATURE_STRICT;
	
	public EXIConfiguration(){
		esc = new EXISetupConfiguration();
	}
	
	public EXISetupConfiguration toEXISetupConfiguration(){
		return esc;
	}
	
	/**
	 * Sets the alignment option to be used. 
	 * The alignment option is to control how the values are encoded. 
	 * The default option 'bit-packed' fits most of communication use cases. 
	 * If TLS compression is used at the same time, pre-compression will make the best result. 
	 * @param cm represents the option chosen
	 */
	public void setAlignment(int cm){
		CodingMode codingMode = CodingMode.BIT_PACKED;
		switch (cm){
			case ALIGN_COMPRESSION:
				codingMode = CodingMode.COMPRESSION;
				break;
			case ALIGN_BIT_PACKED:
				codingMode = CodingMode.BIT_PACKED;
				break;
			case ALIGN_BYTE_PACKED:
				codingMode = CodingMode.BYTE_PACKED;
				break;
			case ALIGN_PRE_COMPRESSION:
				codingMode = CodingMode.PRE_COMPRESSION;
				break;
		}
		esc.setCodingMode(codingMode);
	}
	
	public String getAlignment(){
		return esc.getAlignmentString();
	}
	

	
	/**
	 * Chooses a part of the XML document that should be preserved and restored when decoded on the recever's side. 
	 * Such parts can be comments, processing instructions, etc. 
	 * @param key is what will be preserved and can be accessed through this class as a static field
	 */
	public void setPreserve(String key){
		try {
			esc.getFidelityOptions().setFidelity(key, true);
		} catch (UnsupportedOption e) {
			e.printStackTrace();
		}
	}
	
	public void setStrict(boolean strict) {
		try {
			esc.getFidelityOptions().setFidelity(FidelityOptions.FEATURE_STRICT, strict);
		} catch (UnsupportedOption e) {
			e.printStackTrace();
		}		
	}
}
