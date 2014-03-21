package cl.clayster.exi;

import cl.clayster.exi.EXIEventListener;
import cl.clayster.exi.EXIUtils;

public class EXIPacketLogger implements EXIEventListener{
	
	String name;
	boolean exi, xml;
	
	public EXIPacketLogger(String name){
		this.name = name;
		exi = xml = true;
	}
	
	/**
	 * 
	 * @param name
	 * @param exi	show EXI packets or not 
	 * @param xml	show XML packets or not
	 */
	public EXIPacketLogger(String name, boolean exi, boolean xml){
		this.name = name;
		this.exi = exi;
		this.xml = xml;
	}

	@Override
	public void compressionStarted() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void packetDecoded(String xml, byte[] exi) {
		if(this.exi)	System.err.println(name + "-EXI :(" + exi.length + ")" + EXIUtils.bytesToHex(exi));
		if(this.xml)	System.err.println(name + "-XML:(" + xml.length() + ")" + xml);
	}

	@Override
	public void packetEncoded(String xml, byte[] exi) {
		if(this.xml)	System.out.println(name + "-XML:(" + xml.length() + ")" + xml);
		if(this.exi)	System.out.println(name + "-EXI:(" + exi.length + ")" + EXIUtils.bytesToHex(exi));
	}
}
