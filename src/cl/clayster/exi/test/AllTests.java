package cl.clayster.exi.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	cl.clayster.exi.test.AllOKTest.class,
	cl.clayster.exi.test.NoSchemasOnServerTest.class,
	cl.clayster.exi.test.UploadSchemasTest.class,
	cl.clayster.exi.test.NoStringTableOK.class
})
public class AllTests {
	
}
