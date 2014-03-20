package cl.clayster.exi.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	cl.clayster.exi.test.DefaultValuesTest.class,
	cl.clayster.exi.test.CustomConfigurationsTest.class,
	cl.clayster.exi.test.NoSchemasOnServerTest.class
})
public class AllTests {
	
}
