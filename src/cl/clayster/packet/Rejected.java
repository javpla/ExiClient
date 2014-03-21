package cl.clayster.packet;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.xmlpull.v1.XmlPullParser;

public class Rejected extends IQ {
	
	
	/*
	<iq type='error' from='device@clayster.com' to='client@clayster.com/amr' id='S0003'>
		<rejected xmlns='urn:xmpp:iot:sensordata' seqnr='3'>
    		<error>Access denied.</error>
		</rejected>
	</iq>
	 */
	
	
	private String seqnr, errorText;
	
	public Rejected(String seqnr){
		this.seqnr = seqnr;
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
	
	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<rejected").append(" xmlns=\"urn:xmpp:iot:sensordata\"");
        if(seqnr != null) buf.append(" seqnr=\"").append(getSeqnr()).append('\"');
        buf.append(">").append("<error>");
        if(errorText != null)	buf.append(getErrorText());
        buf.append("</error>")
        	.append("</rejected>");
        return buf.toString();
	}

	public static class Provider implements IQProvider {
		@Override
		public IQ parseIQ(XmlPullParser parser) throws Exception {
			if(!(parser.getEventType() == XmlPullParser.START_TAG && "rejected".equals(parser.getName()))){
            	throw new XMPPException("Parser not in proper position, or bad XML.");
	    	}
			Rejected rejected = new Rejected(parser.getAttributeValue(null, "seqnr"));
	    	int eventType = parser.next();
	    	do{
	    		// only look for an <error> element
	    		if(eventType == XmlPullParser.START_TAG && "error".equals(parser.getName())){
    				rejected.setErrorText(PacketParserUtils.parseContent(parser));
	    		}
	    		eventType = parser.next();
	    	}while(!(eventType == XmlPullParser.END_TAG && "rejected".equals(parser.getName())));
	    	return rejected;
		}
	}
}
