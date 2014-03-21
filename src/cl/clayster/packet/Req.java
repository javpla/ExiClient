package cl.clayster.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/*
 * <iq type='get'
       from='client@clayster.com/amr'
       to='device@clayster.com'
       id='S0001'>
      <req xmlns='urn:xmpp:iot:sensordata' seqnr='1' momentary='true'/>
   </iq>
   <iq type='get'
       from='client@clayster.com/amr'
       to='device@clayster.com'
       id='S0004'>
      <req xmlns='urn:xmpp:iot:sensordata' seqnr='4' all='true' when='2013-03-07T19:00:00'/>
   </iq>
   
   <iq type='get'
       from='client@clayster.com/amr'
       to='device@clayster.com'
       id='S0005'>
      <req xmlns='urn:xmpp:iot:sensordata' seqnr='5' momentary='true'>
         <node nodeId='Device02'/>
         <node nodeId='Device03'/>
      </req>
   </iq>
   
   <iq type='get'
       from='client@clayster.com/amr'
       to='device@clayster.com'
       id='S0006'>
      <req xmlns='urn:xmpp:iot:sensordata' seqnr='6' momentary='true'>
         <node nodeId='Device04'/>
         <field name='Energy'/>
         <field name='Power'/>
      </req>
   </iq>
 */

public class Req extends IQ{
	
	private String seqnr, momentary, all, when;
	private List<String> nodeIds, fieldNames;
	
	public Req(){
		setType(Type.GET);
		nodeIds = new ArrayList<String>();
		fieldNames = new ArrayList<String>();
	}
	
	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<req").append(" xmlns=\"urn:xmpp:iot:sensordata\"");
		
		// attributes
        if(seqnr != null) buf.append(" seqnr=\"").append(seqnr).append('\"');
        if(momentary != null) buf.append(" momentary=\"").append(momentary).append('\"');
        if(all != null) buf.append(" all=\"").append(all).append('\"');
        if(when != null) buf.append(" when=\"").append(when).append('\"');
        
        //elements
        if(nodeIds.isEmpty() && fieldNames.isEmpty()){
        	buf.append("/>");
        }
        else{
        	buf.append(">");
        	for(String id : nodeIds){
            	buf.append("<node nodeId=\"").append(id).append("\"/>");
            }
        	for(String name : fieldNames){
            	buf.append("<field name=\"").append(name).append("\"/>");
            }
        	buf.append("</req>");
        }
        return buf.toString();
	}
	
	public String getSeqnr() {
		return seqnr;
	}

	public void setSeqnr(String seqnr) {
		this.seqnr = seqnr;
	}

	public String getMomentary() {
		return momentary;
	}

	public void setMomentary(String momentary) {
		this.momentary = momentary;
	}
	
	public String getAll() {
		return all;
	}

	public void setAll(String all) {
		this.all = all;
	}

	public String getWhen() {
		return when;
	}

	public void setWhen(String when) {
		this.when = when;
	}

	public List<String> getNodeIds() {
		return nodeIds;
	}
	
	public void addNodeId(String nodeId){
		nodeIds.add(nodeId);
	}

	public List<String> getFieldNames() {
		return fieldNames;
	}
	
	public void addFieldName(String fieldName){
		fieldNames.add(fieldName);
	}


	public static class Provider implements IQProvider {

        public Provider() {
            super();
        }

        public IQ parseIQ(XmlPullParser parser) throws XMPPException, XmlPullParserException, IOException {
            if(!(parser.getEventType() == XmlPullParser.START_TAG && "req".equals(parser.getName()))){
            	throw new XMPPException("Parser not in proper position, or bad XML.");
	    	}
            
            Req req = new Req();
            req.setSeqnr(parser.getAttributeValue(null, "seqnr"));
            req.setMomentary(parser.getAttributeValue(null, "momentary"));
            req.setAll(parser.getAttributeValue(null, "all"));
            req.setWhen(parser.getAttributeValue(null, "when"));
	    	int eventType = parser.next();
	    	do{
	    		// look for <node> and <field> elements
	    		if(eventType == XmlPullParser.START_TAG){
	    			if("node".equals(parser.getName())){
	    				req.addNodeId(parser.getAttributeValue(null, "nodeId"));
		    		}
		    		else if(eventType == XmlPullParser.START_TAG && "field".equals(parser.getName())){
		    			req.addFieldName(parser.getAttributeValue(null, "name"));
		    		}
	    		}
	    		else if(eventType == XmlPullParser.END_TAG){
	    			if("req".equals(parser.getName())){
	    				break;
	    			}
	    		}
	    		eventType = parser.next();
	    	}while(!(eventType == XmlPullParser.END_TAG && "req".equals(parser.getName())));
            return req;
        }
    }
}
