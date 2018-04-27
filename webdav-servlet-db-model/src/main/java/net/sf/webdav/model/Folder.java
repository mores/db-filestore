package net.sf.webdav.model;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

/**
 * The object that represents the Folder.
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Folder extends DBObject {
}
