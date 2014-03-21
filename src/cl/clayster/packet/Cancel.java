package cl.clayster.packet;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/*
 <iq type='get'
       from='client@clayster.com/amr'
       to='device@clayster.com'
       id='S0008'>
      <cancel xmlns='urn:xmpp:iot:sensordata' seqnr='8'/>
   </iq> 
 */

public class Cancel extends IQ{
	
	private String seqnr;
	
	public Cancel(String seqnr){
		setType(Type.GET);
		this.seqnr = seqnr;
	}
	
	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<cancel").append(" xmlns=\"urn:xmpp:iot:sensordata\"");
		
		// attributes
        if(seqnr != null) buf.append(" seqnr=\"").append(seqnr).append('\"');
        buf.append("/>");
        
        return buf.toString();
	}
	
	public String getSeqnr() {
		return seqnr;
	}

	public void setSeqnr(String seqnr) {
		this.seqnr = seqnr;
	}

	
	public static class Provider implements IQProvider {

        public Provider() {
            super();
        }

        public IQ parseIQ(XmlPullParser parser) throws XMPPException, XmlPullParserException, IOException {
            if(!(parser.getEventType() == XmlPullParser.START_TAG && "cancel".equals(parser.getName()))){
            	throw new XMPPException("Parser not in proper position, or bad XML.");
	    	}

            return new Cancel(parser.getAttributeValue(null, "seqnr"));
        }
    }
}
