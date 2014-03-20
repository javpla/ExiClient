package cl.clayster.packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

/*
 	<fields xmlns='urn:xmpp:iot:sensordata' seqnr='4'>
		<node nodeId='Device01'>
			<timestamp value='2013-03-07T19:00:00'>
				<numeric name='Temperature' momentary='true' automaticReadout='true' value='23.4' unit='°C'/>
				<numeric name='Runtime' status='true' automaticReadout='true' value='12345' unit='h'/>
				<string name='Device ID' identification='true' automaticReadout='true' value='Device01'/>
			</timestamp>
		</node>
	</fields>
 */
public class Fields implements PacketExtension {
	
	private String seqnr, done;
	private List<Node> nodes = new ArrayList<Node>();
	
	public Fields(String seqnr, String done, List<Node> nodes){
		this.seqnr = seqnr;
		this.done = done;
		if(nodes != null)	this.nodes = nodes;
	}
	
	public Fields(String seqnr, String done){
		this.seqnr = seqnr;
		this.done = done;
	}
	
	public Fields(){}

	@Override
	public String getElementName() {
		return "fields";
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
        if(seqnr != null)	buf.append("\" seqnr=\"").append(getSeqnr());
        if(done != null)	buf.append("\" done=\"").append(getDone());
        buf.append("\">");
        
        for(Node n : nodes){
        	buf.append(n.toXML());
        }
        
        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
	}

	public String getSeqnr() {
		return seqnr;
	}

	public void setSeqnr(String seqnr) {
		this.seqnr = seqnr;
	}
	
	public String getDone(){
		return done;
	}
	
	public void setDone(String done){
		this.done = done; 
	}

	public Iterator<Node> getNodes() {
        synchronized (nodes) {
            return Collections.unmodifiableList(new ArrayList<Node>(nodes)).iterator();
        }
    }

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	
	public void addNode(Node n){
		this.nodes.add(n);
	}
	
	public static class Provider implements PacketExtensionProvider {
	    public PacketExtension parseExtension (XmlPullParser parser) throws Exception {
	    	Fields fields = new Fields(parser.getAttributeValue(null, "seqnr"), parser.getAttributeValue(null, "done"));
	    	parser.next();
	    	int eventType = parser.getEventType();
	    	do{
	    		// look for a <node> element
	    		if(eventType == XmlPullParser.START_TAG && "node".equals(parser.getName())){
	    			fields.addNode((Node) new Node.Provider().parseExtension(parser));
	    		}
	    		eventType = parser.next();
	    	}while(!(eventType == XmlPullParser.END_TAG && "fields".equals(parser.getName())));
	    	return fields;
	    }
	}
}
