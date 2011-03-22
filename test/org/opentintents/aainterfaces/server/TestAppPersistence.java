package org.opentintents.aainterfaces.server;
import javax.jdo.PersistenceManager;

import org.openintents.aainterfaces.server.App;
import org.openintents.aainterfaces.server.PMF;

import junit.framework.TestCase;


public class TestAppPersistence extends TestCase {
	public void testMakePersistent(){
		PersistenceManager pm = PMF.get().getPersistenceManager();
		App app = new App();
		app.setPackageName("org.test");
		pm.makePersistent(app);
		
	}
}
