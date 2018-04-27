package net.sf.webdav;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

/**
 * The class to setup connection to the db.
 */

public final class JDOManager {

    private static PersistenceManagerFactory pmf;

    private JDOManager() {
    }

    public static PersistenceManager getPersistenceManager() {
        if (pmf == null) {
            String config = "DB-FileStore-datanucleus.properties";
            pmf = JDOHelper.getPersistenceManagerFactory(config);
        }

        return (pmf.getPersistenceManager());
    }
}
