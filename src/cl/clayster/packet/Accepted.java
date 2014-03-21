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
       id='S0004'>
      <accepted xmlns='urn:xmpp:iot:sensordata' seqnr='4' queued='true'/>
   </iq>
 */

public class Accepted extends IQ{
	
	private String seqnr, queued;
	
	public Accepted(String seqnr, String queued){
		setType(Type.RESULT);
		this.seqnr = seqnr;
		this.queued = queued;
	}
	
	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<accepted").append(" xmlns=\"urn:xmpp:iot:sensordata\"");
		
		// attributes
        if(seqnr != null) buf.append(" seqnr=\"").append(seqnr).append('\"');
        if(queued != null) buf.append(" queued=\"").append(queued).append('\"');
        buf.append("/>");
        
        return buf.toString();
	}
	
	public String getSeqnr() {
		return seqnr;
	}

	public void setSeqnr(String seqnr) {
		this.seqnr = seqnr;
	}
	
	public String getQueued() {
		return queued;
	}

	public void setQueued(String queued) {
		this.queued = queued;
	}



	public static class Provider implements IQProvider {

        public Provider() {
            super();
        }

        public IQ parseIQ(XmlPullParser parser) throws XMPPException, XmlPullParserException, IOException {
            if(!(parser.getEventType() == XmlPullParser.START_TAG && "accepted".equals(parser.getName()))){
            	throw new XMPPException("Parser not in proper position, or bad XML.");
	    	}

            return new Accepted(parser.getAttributeValue(null, "seqnr"), parser.getAttributeValue(null, "queued"));
        }
    }
}
