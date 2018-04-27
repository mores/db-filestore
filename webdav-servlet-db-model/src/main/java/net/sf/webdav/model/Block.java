package net.sf.webdav.model;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * The object that contains the actual data of the File.
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
@FetchGroup(name="include_content", members={@Persistent(name="content")})
public class Block {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    @Persistent
    private byte[] content;

    public int getId() {
        return (id);
    }

    public void setId(final int blockId) {
        this.id = blockId;
    }

    public byte[] getContent() {
        return (content);
    }

    public void setContent(final byte[] data) {
        content = data;
    }
}
