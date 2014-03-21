package cl.clayster.packet;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.xmlpull.v1.XmlPullParser;

public class Failure implements PacketExtension {
	
	
	/**
	  <failure xmlns='urn:xmpp:iot:sensordata' seqnr='2' done='true'>
		<error nodeId='Device01' timestamp='2013-03-07T17:13:30'>
		Timeout.
		</error>
		</failure>
	 */
	
	
	private String seqnr, done;	// attributes
	private String nodeId, timestamp, errorText;
	
	public Failure(String seqnr, String done){
		this.seqnr = seqnr;
		this.done = done;
	}
	
	public Failure() {}

	@Override
	public String getElementName() {
		return "failure";
	}

	@Override
	public String getNamespace() {
		return "urn:xmpp:iot:sensordata";
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<").append(getElementName())
    		.append(" xmlns=\"").append(getNamespace());
        if(seqnr != null) buf.append("\" seqnr=\"").append(getSeqnr()).append('\"');
        if(done != null)	buf.append(" done=\"").append(getDone()).append('\"');
        buf.append(">").append("<error");
        if(nodeId != null)	buf.append(" nodeId=\"").append(getNodeId()).append('\"');
        if(timestamp != null)	buf.append(" timestamp=\"").append(getTimestamp()).append('\"');
        buf.append(">");
        if(errorText != null)	buf.append(getErrorText());
        buf.append("</error>").append("</failure>");
        return buf.toString();
	}

	public String getSeqnr() {
		return seqnr;
	}

	public void setSeqnr(String seqnr) {
		this.seqnr = seqnr;
	}

	public String getDone() {
		return done;
	}

	public void setDone(String done) {
		this.done = done;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}	
	
	public static class Provider implements PacketExtensionProvider {
	    public PacketExtension parseExtension (XmlPullParser parser) throws Exception {
	    	if(!(parser.getEventType() == XmlPullParser.START_TAG && "failure".equals(parser.getName()))){
            	throw new XMPPException("Parser not in proper position, or bad XML.");
	    	}
	    	Failure failure = new Failure(parser.getAttributeValue(null, "seqnr"), parser.getAttributeValue(null,"done"));
	    	int eventType = parser.next();
	    	do{
	    		// only look for an <error> element
	    		if(eventType == XmlPullParser.START_TAG && "error".equals(parser.getName())){
    				failure.setNodeId(parser.getAttributeValue(null, "nodeId"));
    				failure.setTimestamp(parser.getAttributeValue(null, "timestamp"));
    				failure.setErrorText(PacketParserUtils.parseContent(parser));
	    		}
	    		eventType = parser.next();
	    	}while(!(eventType == XmlPullParser.END_TAG && "failure".equals(parser.getName())));
	    	return failure;
	    }
	}


}
