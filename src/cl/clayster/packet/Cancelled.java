package cl.clayster.packet;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/*
 <iq type='result'
       from='device@clayster.com'
       to='client@clayster.com/amr'
       id='S0008'>
      <cancelled xmlns='urn:xmpp:iot:sensordata' seqnr='8'/>
   </iq>
 */

public class Cancelled extends IQ{
	
	private String seqnr;
	
	public Cancelled(String seqnr){
		setType(Type.RESULT);
		this.seqnr = seqnr;
	}
	
	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<cancelled").append(" xmlns=\"urn:xmpp:iot:sensordata\"");
		
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
            if(!(parser.getEventType() == XmlPullParser.START_TAG && "cancelled".equals(parser.getName()))){
            	throw new XMPPException("Parser not in proper position, or bad XML.");
	    	}

            return new Cancelled(parser.getAttributeValue(null, "seqnr"));
        }
    }
}
