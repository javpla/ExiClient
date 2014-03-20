package cl.clayster.packet;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.xmlpull.v1.XmlPullParser;

public class Provider implements PacketExtensionProvider {
    public PacketExtension parseExtension (XmlPullParser parser) throws Exception {
    	Failure failure = new Failure();
    	Fields fields = new Fields();
    	List<Node> nodes = new ArrayList<Node>();
    	List<Timestamp> timestamps = new ArrayList<Timestamp>();
    	
    	int eventType = parser.getEventType();
    	do{
    		if(eventType == XmlPullParser.START_TAG){
    			if("fields".equals(parser.getName())){
    				fields = new Fields(parser.getAttributeValue(null, "seqnr"), parser.getAttributeValue(null, "done"));
    			}
    			else if("node".equals(parser.getName())){
    				nodes.add(new Node(parser.getAttributeValue(null, "nodeId")));
    			}
    			else if("timestamp".equals(parser.getName())){
    				timestamps.add(new Timestamp(parser.getAttributeValue(null, "value")));
    			}
    			
    			else if("numeric".equals(parser.getName())){
    				timestamps.get(timestamps.size() - 1).addNumerics(
    						new Numeric(parser.getAttributeValue("", "name"), parser.getAttributeValue(null, "historicalHour"),
    								parser.getAttributeValue(null, "historicalDay"), parser.getAttributeValue(null, "status"),
    								parser.getAttributeValue(null, "momentary"), parser.getAttributeValue(null, "automaticReadout"),
    								parser.getAttributeValue(null, "value"), parser.getAttributeValue(null, "unit")));
    			}
    			else if("string".equals(parser.getName())){
    				timestamps.get(timestamps.size() - 1).addString(
    						new StringElement(parser.getAttributeValue("", "name"), parser.getAttributeValue("", "identification"),
    								parser.getAttributeValue("", "automaticReadout"), parser.getAttributeValue("", "value")));
    			}
    			else if("started".equals(parser.getName())){
    				return new Started(parser.getAttributeValue(null, "seqnr"));
    			}
    			else if("done".equals(parser.getName())){
    				return new Done(parser.getAttributeValue(null, "seqnr"));
    			}
    			else if("failure".equals(parser.getName())){
    				failure = new Failure(parser.getAttributeValue(null, "seqnr"), parser.getAttributeValue(null,"done"));
    			}
    			else if("error".equals(parser.getName())){
    				failure.setNodeId(parser.getAttributeValue(null, "nodeId"));
    				failure.setTimestamp(parser.getAttributeValue(null, "timestamp"));
    				failure.setErrorText(PacketParserUtils.parseContent(parser));
    			}
    		}
    		else if (eventType == XmlPullParser.END_TAG) {
    			if("fields".equals(parser.getName())){
    				for(Node n : nodes){
    					fields.addNode(n);
    				}
    				return fields;
    			}
    			else if("node".equals(parser.getName())){
    				for(Timestamp t : timestamps){
    					nodes.get(nodes.size() - 1).addTimestamp(t);
    				}
    			}
    			else if("failure".equals(parser.getName())){
    				return failure;
    			}
    		}
    		eventType = parser.next();
    	}while(eventType != XmlPullParser.END_DOCUMENT);
    	return null;
    }
}
