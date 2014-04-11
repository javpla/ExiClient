package cl.clayster.exi.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	cl.clayster.exi.test.NoSchemasOnServerTest.class,
	cl.clayster.exi.test.NegotiationTest.class,
	cl.clayster.exi.test.AlternativeBindingTests.class
})
public class AllTests {
	
}
