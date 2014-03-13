package cl.clayster.exi;

public interface EXIEventListener {
	
	/**
	 * Called when EXI compression is activated on the client.
	 */
	public void compressionStarted();
	
	/**
	 * Called when the client receives and decodes an EXI packet
	 * @param xml the decoded XML String
	 * @param exi the received EXI bytes
	 */
	public void packetDecoded(String xml, byte[] exi);
	
	/**
	 * Called when the client encodes and sends an EXI packet
	 * @param xml the encoded XML String
	 * @param exi the sent EXI bytes
	 */
	public void packetEncoded(String xml, byte[] exi);
	
}
