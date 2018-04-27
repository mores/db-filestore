package net.sf.webdav.model;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * The object that represents the File.
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
@FetchGroup(name="include_content", members={@Persistent(name="blocks")})
public class Resource extends DBObject {

    @Persistent
    @Element(types=net.sf.webdav.model.Block.class, dependent="true")
    private java.util.List blocks;

    @Persistent
    private String contentType = "";

    @Persistent
    private int length = 0;

    private static final int BLOCK_SIZE = 262144;

    public java.util.List getBlocks() {
        return (blocks);
    }

    public void setBlocks(final java.util.List b) {
        this.blocks = b;
    }

    public byte[] getContent() {
        // Build this up from blocks
        java.io.ByteArrayOutputStream baos =
            new java.io.ByteArrayOutputStream();

        if (blocks != null) {
            for (int i = 0; i < blocks.size(); i++) {
                Block b = (Block) blocks.get(i);
                byte[] data = b.getContent();
                baos.write(data, 0, data.length);
            }
        }

        return (baos.toByteArray());
    }

    public void setContent(final byte[] data) {
        this.length = data.length;

        // Break this into blocks
        blocks = new java.util.Vector();

        java.io.ByteArrayInputStream bais =
            new java.io.ByteArrayInputStream(data);
        int readByte = -1;
        byte[] buffer = new byte[BLOCK_SIZE];

        while ((readByte = bais.read(buffer, 0, BLOCK_SIZE)) > -1)  {
            Block b = new Block();
            byte[] leftOver = new byte[readByte];
            System.arraycopy(buffer, 0, leftOver, 0, readByte);
            b.setContent(leftOver);

            blocks.add(b);
        }
    }

    public String getContentType() {
        return (contentType);
    }

    public void setContentType(final String type) {
        contentType = type;
    }

    public int getLength() {
        return (length);
    }
}

