package cl.clayster.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class QueryElement extends IQ {


	
	/*
     * <iq type='result' from='discovery@clayster.cl' to='yiperu@clayster.cl/device' id='disco1'>
     * 		<query xmlns='http://jabber.org/protocol/disco#info'/>
     * 			<feature var="urn:xmpp:iot:discovery" />
     * 		</query>
     * </iq>
     */	
	
	
	
	
	private List<String> feature;
	
	public QueryElement(){
		setType(Type.GET);
		feature = new ArrayList<String>();
	}

	@Override
	public String getChildElementXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<query").append(" xmlns=\"http://jabber.org/protocl/disco#info");

        //elements
        if(feature.isEmpty() ){
            buf.append("/>");
        } else{
        	buf.append(">");
        	for(String feature_str : feature){
            	buf.append("<feature var=\"").append(feature_str).append("\"/>");
            }
        	buf.append("</query>");
        }
        return buf.toString();
	}
	
	 public List<String> getFeature() {
		return feature;
	}

	 public void addFeature(String cad) {
			this.feature.add(cad);
	}

	 public static class Provider implements IQProvider {

	        public Provider() {
	            super();
	        }
	        
	public IQ parseIQ(XmlPullParser parser) throws XMPPException, XmlPullParserException, IOException {
		 
         if(!(parser.getEventType() == XmlPullParser.START_TAG && "query".equals(parser.getName()))){
             throw new XMPPException("Parser not in proper position, or bad XML.");
         }
         
         QueryElement queryElement = new QueryElement();
         int eventType = parser.next();
         do{
             // look for <node> and <field> elements
             if(eventType == XmlPullParser.START_TAG){
                 if("feature".equals(parser.getName())){
                     queryElement.addFeature(parser.getAttributeValue(null, "var"));
                 }
             }
             eventType = parser.next();
         }while(!(eventType == XmlPullParser.END_TAG && "query".equals(parser.getName())));
         return queryElement;
	 }
	 }
	
	 
	 
	 

}
