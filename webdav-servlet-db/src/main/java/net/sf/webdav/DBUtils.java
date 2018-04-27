package net.sf.webdav;

import net.sf.webdav.exceptions.WebdavException;

public class DBUtils {
    private static org.slf4j.Logger log =
        org.slf4j.LoggerFactory.getLogger(DBUtils.class);

    private static final int FILE_NAME = 3;

    private java.util.regex.Pattern pattern =
        java.util.regex.Pattern.compile("^((/[^\\/]+)*)/(.*)$");

    public void setObject(final Object obj) {
        log.trace("Entering setObject");

        javax.jdo.PersistenceManager pm = JDOManager.getPersistenceManager();
        javax.jdo.Transaction tx = pm.currentTransaction();
        try {
            tx.begin();

            pm.makePersistent(obj);

            tx.commit();
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            log.error("DB Error: " + sw.toString());
            throw new WebdavException("Error. Please see log for more info.");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    public net.sf.webdav.model.DBObject getObject(final Class cls,
        final String uri, final boolean all) {

        log.trace("getItem: " + uri);
        net.sf.webdav.model.DBObject dbo = null;

        String path = "";
        String name = "";

        java.util.regex.Matcher matched = pattern.matcher(uri);
        if (matched.matches()) {
            path = matched.group(1);
            name = matched.group(FILE_NAME);
            log.trace("Path: " + path + " Name: " + name);
        } else {
            log.error("No MATCH: " + uri);
            return (dbo);
        }

        javax.jdo.PersistenceManager pm = JDOManager.getPersistenceManager();
        if (all) {
            pm.getFetchPlan().addGroup("include_content");
        }
        javax.jdo.Transaction tx = pm.currentTransaction();
        tx.begin();

        javax.jdo.Query query =
            pm.newQuery(cls, "path == pth && name == nme");
        query.declareParameters("java.lang.String pth, java.lang.String nme");

        java.util.List result = (java.util.List) query.execute(path, name);
        log.trace("length of result set: " + result.size());
        if (result.size() == 1) {
            result = (java.util.List) pm.detachCopyAll(result);
            dbo = (net.sf.webdav.model.DBObject) result.get(0);
        }
        tx.commit();

        return (dbo);
    }

    public void deleteObject(final String uri) {
        log.trace("deleteItem: " + uri);

        net.sf.webdav.model.DBObject dbo =
            getObject(net.sf.webdav.model.DBObject.class, uri, false);

        if (dbo != null) {
            javax.jdo.PersistenceManager pm =
                JDOManager.getPersistenceManager();
            javax.jdo.Transaction tx = pm.currentTransaction();
            tx.begin();

            pm.deletePersistent(dbo);

            tx.commit();
        }
    }

    public java.util.List ls(final String path) {
        log.trace("Entering ls: " + path);

        String searchPath = path;
        if (searchPath.equals("/")) {
            searchPath = "";
        }

        javax.jdo.PersistenceManager pm = JDOManager.getPersistenceManager();
        javax.jdo.Transaction tx = pm.currentTransaction();
        tx.begin();

        javax.jdo.Query query =
            pm.newQuery(net.sf.webdav.model.DBObject.class,
            "path == this_path");
        query.declareParameters("java.lang.String this_path");
        java.util.List result = (java.util.List) query.execute(searchPath);
        log.trace("length of result set: " + result.size());
        if (result.size() > 0) {
            result = (java.util.List) pm.detachCopyAll(result);
        }
        tx.commit();

        return (result);
    }

}
