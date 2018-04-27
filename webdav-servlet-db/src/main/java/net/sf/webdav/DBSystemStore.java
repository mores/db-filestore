package net.sf.webdav;

public class DBSystemStore implements IWebdavStore {

    private static org.slf4j.Logger log =
        org.slf4j.LoggerFactory.getLogger(DBSystemStore.class);

    private static final int PATH_POSITION = 3;
    private static final int BUFFER_SIZE = 1024;
    private static final long BIRTH_DAY = 1209614400000L;

    private java.util.regex.Pattern pattern =
        java.util.regex.Pattern.compile("^((/[^\\/]+)*)/(.*)$");

    public DBSystemStore(java.io.File root) {
        log.trace("init: " + root);
    }

    public long getResourceLength(ITransaction transaction, String uri) {
        
        DBUtils dbu = new DBUtils();
        net.sf.webdav.model.Resource file =
            (net.sf.webdav.model.Resource)dbu.getObject(net.sf.webdav.model.Resource.class, uri, true);

        log.trace("getResourceLength: " + uri + " " + file.getContent().length);

        return (file.getContent().length);
    }

    public StoredObject getStoredObject(ITransaction transaction, String uri) {

        log.trace("getStoredObject: " + uri);
        StoredObject so = null;

        if ((uri.equals("/")) || (uri.equals(""))) {
            // Root always exist
            so = new StoredObject();
            so.setLastModified(new java.util.Date(BIRTH_DAY));
            so.setCreationDate(new java.util.Date(BIRTH_DAY));
            so.setFolder(true);
            return (so);
        }

        DBUtils dbu = new DBUtils();
        net.sf.webdav.model.DBObject dbo =
            dbu.getObject(net.sf.webdav.model.DBObject.class, uri, true);

        if (dbo != null) {
            so = new StoredObject();
            so.setLastModified(dbo.getLastModified());
            so.setCreationDate(dbo.getCreation());

            if (dbo instanceof net.sf.webdav.model.Resource) {
                net.sf.webdav.model.Resource file = 
                    (net.sf.webdav.model.Resource)dbo;
                so.setResourceLength(file.getContent().length);
                so.setFolder(false);
            } else {
                so.setFolder(true);
            }
        }

        return (so);
    }

    public void removeObject(ITransaction transaction, String uri) {
        log.trace("removeObject: " + uri);
        DBUtils dbu = new DBUtils();
        dbu.deleteObject(uri);
    }

    public long setResourceContent(ITransaction transaction, String uri,
            java.io.InputStream is,
            String contentType, String characterEncoding) {

        log.trace("setResourceContent: " + uri);
        net.sf.webdav.model.Resource r = new net.sf.webdav.model.Resource();

        DBUtils dbu = new DBUtils();
        net.sf.webdav.model.DBObject dbo =
            dbu.getObject(net.sf.webdav.model.Resource.class, uri, false);
        if (dbo == null) {
            r.setCreation(new java.util.Date());
            java.util.regex.Matcher matched = pattern.matcher(uri);
            if (matched.matches()) {
                String path = matched.group(1);
                String name = matched.group(PATH_POSITION);
                r.setPath(path);
                r.setName(name);
                r.setChecksum();
            }
        } else {
            if (dbo instanceof net.sf.webdav.model.Resource) {
                r = (net.sf.webdav.model.Resource) dbo;
            }
        }

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;

        try {
            while ((len = is.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            is.close();
            out.close();
        } catch (java.io.IOException ioe) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ioe.printStackTrace(pw);
            log.error("IO Error: " + sw.toString());
            throw new net.sf.webdav.exceptions.WebdavException("IO Error");
        }

        byte[] content = out.toByteArray();

        r.setContent(content);
        r.setContentType(contentType);
        r.setLastModified(new java.util.Date());
        dbu.setObject(r);

        return (content.length);
    }

    public String[] getChildrenNames(ITransaction transaction, String uri) {
        log.trace("getChildrenNames: " + uri);
        String[] children = null;

        DBUtils dbu = new DBUtils();
        java.util.List dbos = dbu.ls(uri);
        children = new String[ dbos.size() ];
        for (int i = 0, n = dbos.size(); i < n; i++) {
            net.sf.webdav.model.DBObject dbo =
                (net.sf.webdav.model.DBObject) dbos.get(i);
            children[i] = dbo.getName();
        }

        return (children);
    }

    public void createResource(ITransaction transaction, String uri) {
        log.trace("createResource: " + uri);

        /*
            No need to do anything now.
            Allow the content to drive the rollback on error.
        */
    }
    
    public java.io.InputStream getResourceContent(ITransaction transaction, String resourceUri)
    {
        log.trace("getResourceContent: " + resourceUri);
        DBUtils dbu = new DBUtils();
        net.sf.webdav.model.Resource file =
            (net.sf.webdav.model.Resource)dbu.getObject(net.sf.webdav.model.Resource.class, resourceUri, true);

        return( new java.io.ByteArrayInputStream( file.getContent() ) );
    }

    public void createFolder(ITransaction transaction, String uri) {
        log.trace("createFolder: " + uri);
        DBUtils dbu = new DBUtils();
        net.sf.webdav.model.Folder f = new net.sf.webdav.model.Folder();

        java.util.regex.Matcher matched = pattern.matcher(uri);
        if (matched.matches()) {
            String path = matched.group(1);
            String name = matched.group(PATH_POSITION);
            f.setPath(path);
            f.setName(name);
            f.setChecksum();
        }

        f.setCreation(new java.util.Date());
        f.setLastModified(new java.util.Date());
        dbu.setObject(f);
    }

    public void rollback(ITransaction transaction) {
        log.trace("rollback");
    }

    public void commit(ITransaction transaction) {
        log.trace("commit");
    }

    public void checkAuthentication(ITransaction transaction) {
        log.trace("checkAuthentication");
    }

    public ITransaction begin(java.security.Principal principal) {
        log.trace("begin");
        return null;
    }
}
