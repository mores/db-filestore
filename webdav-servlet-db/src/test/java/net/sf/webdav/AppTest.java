package net.sf.webdav;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest extends TestCase
{
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( AppTest.class );

    private DBUtils dbu = null;
    private DBSystemStore dbss = null;

    public AppTest( String testName )
    {
        super( testName );
    }

    protected void setUp() throws Exception
    {
        dbu = new DBUtils();
        dbss = new DBSystemStore( new java.io.File("/") ); 
        if( com.javaranch.unittest.helper.sql.pool.JNDIUnitTestHelper.notInitialized() )
        {
            com.javaranch.unittest.helper.sql.pool.JNDIUnitTestHelper.init("jndi_unit_test_helper.properties");
        }
    }

    public static Test suite()
    {
        TestSuite testsToRun = new TestSuite();

        /* Test DBUtils */
        testsToRun.addTest( new AppTest( "initFileSystem" ) );
        testsToRun.addTest( new AppTest( "lsVar" ) );
        testsToRun.addTest( new AppTest( "getTmp" ) );
        testsToRun.addTest( new AppTest( "uploadDownload" ) );

        /* Now test the store */
        testsToRun.addTest( new AppTest( "createFolder" ) );
        
        return( testsToRun );
    }

    public void initFileSystem() throws Exception
    {
        net.sf.webdav.model.Folder tmp = new net.sf.webdav.model.Folder();
        tmp.setPath( "" );
        tmp.setName( "tmp" );
        tmp.setChecksum();
        tmp.setCreation( new java.util.Date() );
        tmp.setLastModified( new java.util.Date() );
        dbu.setObject( tmp ); 
        log.info("Created Folder: /tmp");

        net.sf.webdav.model.Folder var = new net.sf.webdav.model.Folder();
        var.setPath( "" );
        var.setName( "var" );
        var.setChecksum();
        var.setCreation( new java.util.Date() );
        var.setLastModified( new java.util.Date() );
        dbu.setObject( var );
        log.info("Created Folder: /var");

        byte[] stuff = new byte[9];
        net.sf.webdav.model.Resource messages = new net.sf.webdav.model.Resource();
        messages.setPath( "/var" );
        messages.setName( "messages" );
        messages.setChecksum();
        messages.setContent( stuff );
        messages.setCreation( new java.util.Date() );
        messages.setLastModified( new java.util.Date() );
        dbu.setObject( messages );
        log.info("Created File: /var/messages");
    }

    public void lsVar() throws Exception
    {
        java.util.List dbos = dbu.ls( "/var" ); 
        for( int i = 0, n = dbos.size(); i < n; i++)
        {
            Object dbo = dbos.get( i );
            if( dbo instanceof net.sf.webdav.model.Folder )
            {
                net.sf.webdav.model.Folder f = (net.sf.webdav.model.Folder)dbo;
                log.info( "This is a folder: " + f.getName() );
                fail( "Expecting a resource !" );
            }
            else if( dbo instanceof net.sf.webdav.model.Resource ) 
            {
                net.sf.webdav.model.Resource r = (net.sf.webdav.model.Resource)dbo;
                log.info( "This is a Resource: " + r.getName() );
                assertTrue( true );
            } 
        }
    }

    public void getTmp() throws Exception
    {
        net.sf.webdav.model.DBObject dbo = dbu.getObject( net.sf.webdav.model.Folder.class, "/tmp", false );
        if( dbo != null )
        {
            if( dbo instanceof net.sf.webdav.model.Resource )
            {
                log.info( "This is a resource" );
                fail( "Found a resource instead !" );
            }
            else if( dbo instanceof net.sf.webdav.model.Folder )
            {
                log.info( "This is a folder" );
                assertTrue( true );
            }
            else
            {
                log.error( "What is this !!!:" + dbo.getClass() );
                fail( "Why was this not a Folder !" );
            }
        }
        else
        {
            fail( "Did not find tmp folder." );
        }
    }

    public void uploadDownload() throws Exception
    {
        double rand = Math.random() * 1000;
        java.io.FileInputStream fis = new java.io.FileInputStream( "src/site/resources/images/logo.png" );
        byte fileByte[] = new byte[fis.available()];
        fis.read(fileByte);

        net.sf.webdav.model.Resource r = new net.sf.webdav.model.Resource();
        r.setPath( "" );
        r.setName( "test.file-" + rand );
        r.setChecksum();
        r.setContent( fileByte );
        r.setCreation( new java.util.Date() );
        r.setLastModified( new java.util.Date() );
        dbu.setObject( r );    

        net.sf.webdav.model.DBObject saved = dbu.getObject( net.sf.webdav.model.DBObject.class, "/test.file-" + rand, true );
        byte savedByte[] = null;
        if( saved != null )
        {
            if( saved instanceof net.sf.webdav.model.Resource )
            {
                net.sf.webdav.model.Resource file = (net.sf.webdav.model.Resource)saved;
                savedByte = file.getContent();
            }
        }

        java.util.zip.CRC32 origSum = new java.util.zip.CRC32();
        origSum.update( fileByte, 0, fileByte.length);

        java.util.zip.CRC32 savedSum = new java.util.zip.CRC32();
        savedSum.update( savedByte, 0, savedByte.length);

        assertEquals( origSum.getValue(), savedSum.getValue() );

        dbu.deleteObject( "/test.file-" + rand );
    }


    public void createFolder() throws Exception
    {
        DBTransaction transaction  = new DBTransaction();
        dbss.createFolder( transaction, "/dev" );
        dbss.createFolder( transaction, "/dev/ida" );
        dbss.createFolder( transaction, "/dev/ida/c0d0p1" ); 

        String results[] = dbss.getChildrenNames( transaction, "/dev/ida" );
        assertEquals( results[0], "c0d0p1" );
    }
}
