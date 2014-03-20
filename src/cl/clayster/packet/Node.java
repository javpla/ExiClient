package cl.clayster.packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class Node implements PacketExtension {
	
	/**
	<node nodeId='Device01'>
		<timestamp value='2013-03-07T19:00:00'>
			<numeric name='Temperature' momentary='true' automaticReadout='true' value='23.4' unit='°C'/>
			<numeric name='Runtime' status='true' automaticReadout='true' value='12345' unit='h'/>
			<string name='Device ID' identification='true' automaticReadout='true' value='Device01'/>
		</timestamp>
	</node>
	 */

	private String nodeId;
	private List<Timestamp> timestamps = new ArrayList<Timestamp>();
	
	public Node(String nodeId){
		this.nodeId = nodeId;
	}
	
	public Node(String nodeId, List<Timestamp> timestamps){
		this.nodeId = nodeId;
		if(timestamps != null)	this.timestamps = timestamps;
	}
	
	@Override
	public String getElementName() {
		return "node";
	}

	@Override
	public String getNamespace() {
		return "urn:xmpp:iot:sensordata";
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();
        buf.append("<").append(getElementName());
        if(nodeId != null)	buf.append(" nodeId=\"").append(getNodeId());
        buf.append("\">");
        
        for(Timestamp t : timestamps){
        	buf.append(t.toXML());
        }
        
        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public Iterator<Timestamp> getTimestamps() {
        synchronized (timestamps) {
            return Collections.unmodifiableList(new ArrayList<Timestamp>(timestamps)).iterator();
        }
    }

	public void setTimestamps(List<Timestamp> timestamps) {
		this.timestamps = timestamps;
	}
	
	public void addTimestamp(Timestamp t){
		this.timestamps.add(t);
	}
	
	public static class Provider implements PacketExtensionProvider {
		@Override
		public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
			Node node = new Node(parser.getAttributeValue(null, "nodeId")); 
			parser.next();
			int eventType = parser.getEventType();
	    	do{
	    		if(eventType == XmlPullParser.START_TAG){
	    			if("timestamp".equals(parser.getName())){
	    				node.addTimestamp((Timestamp) new Timestamp.Provider().parseExtension(parser));
	    			}
	    		}
	    		eventType = parser.next();
	    	}while(!(eventType == XmlPullParser.END_TAG && "node".equals(parser.getName())));
			return node;
		}
	}
}
