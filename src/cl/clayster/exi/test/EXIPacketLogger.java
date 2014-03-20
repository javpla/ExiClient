package cl.clayster.exi.test;

import cl.clayster.exi.EXIEventListener;
import cl.clayster.exi.EXIUtils;

public class EXIPacketLogger implements EXIEventListener{
	
	public EXIPacketLogger(){
	}

	@Override
	public void compressionStarted() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void packetDecoded(String xml, byte[] exi) {
		System.err.println("EXI:(" + exi.length + ")" + EXIUtils.bytesToHex(exi));
		System.err.println("XML:(" + xml.length() + ")" + xml);
	}

	@Override
	public void packetEncoded(String xml, byte[] exi) {
		System.out.println("XML:(" + xml.length() + ")" + xml);
		System.out.println("EXI:(" + exi.length + ")" + EXIUtils.bytesToHex(exi));
	}
}
