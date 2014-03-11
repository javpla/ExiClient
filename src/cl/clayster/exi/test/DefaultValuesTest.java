package cl.clayster.exi.test;

import org.junit.Test;

import cl.clayster.exi.EXIUtils;
import cl.clayster.exi.EXIXMPPConnection;

public class DefaultValuesTest extends AbstractTest {
	
	@Override
	protected void prepareTest() {
		EXIUtils.saveConfigId(null); // remove config Id to avoid quick configurations
	}
	
	@Override
	protected void setUser() {
		user = new EXIXMPPConnection(config, null);
	}

	@Override
	protected void setContact() {
		contact = new EXIXMPPConnection(config, null);
	}

	@Override
	protected void setMessage() {
		message = "Using EXI with default values.";
	}
	
	@Test
	public void test() {
		testCommunication();
	}
}
