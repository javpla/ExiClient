package cl.clayster.packet;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.xmlpull.v1.XmlPullParser;

public class Error extends IQ {
	
	
	/*
	<iq type='error' from='device@clayster.com' to='client@clayster.com/amr' id='S0003'>
		<rejected xmlns='urn:xmpp:iot:sensordata' seqnr='3'>
    		<error>Access denied.</error>
		</rejected>
	</iq>
	<iq type='error' from='device@clayster.com' to='client@clayster.com/amr' id='S0003'>
      <error type='cancel'>
         <forbidden xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>
         <text xmlns='urn:ietf:params:xml:ns:xmpp-stanzas' xml:lang='en'>Access denied.</text>
         <rejected xmlns='urn:xmpp:iot:sensordata' seqnr='3'/>
      </error>
   </iq>
	 */
	
	
	private String seqnr, errorText, lang = "en";
	
	public Error(){
	}

	public String getSeqnr() {
		return seqnr;
	}

	public void setSeqnr(String seqnr) {
		this.seqnr = seqnr;
	}
	
	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}
	
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<error type=\"cancel\">");
		buf.append("<forbidden xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/>")
			.append("text xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\" xml:lang=\"")
			.append(lang).append("\">");
		if(errorText != null)	buf.append(getErrorText()).append("</text>");
		buf.append("<rejected xmlns=\"urn:xmpp:iot:sensordata\"");
		if(seqnr != null) buf.append(" seqnr=\"").append(getSeqnr()).append('\"');
        buf.append("/>")
        	.append("</error>");
        return buf.toString();
	}
	

	public static class Provider implements IQProvider {
		@Override
		public IQ parseIQ(XmlPullParser parser) throws Exception {
			if(!(parser.getEventType() == XmlPullParser.START_TAG && "error".equals(parser.getName()))){
            	throw new XMPPException("Parser not in proper position, or bad XML.");
	    	}
			Error error = new Error();
	    	int eventType = parser.next();
	    	do{
	    		// only look for an <error> element
	    		if(eventType == XmlPullParser.START_TAG){
	    			if("text".equals(parser.getName())){
		    			error.setLang(parser.getAttributeValue(null, "lang"));
		    			error.setErrorText(PacketParserUtils.parseContent(parser));
	    			}
	    			if("rejected".equals(parser.getName())){
		    			error.setLang(parser.getAttributeValue(null, "seqnr"));
	    			}
	    		}
	    		eventType = parser.next();
	    	}while(!(eventType == XmlPullParser.END_TAG && "error".equals(parser.getName())));
	    	return error;
		}
	}
}
