package org.jboss.brmsbpmsuite.patching.client;

import java.io.File;
import java.io.IOException;

public interface Patcher {

    public static final char CANONICAL_NAME_SEPARATOR_CHAR = '/';

    public void checkDistro();
    
    public void backup(File backupBasedir) throws IOException;
    
    public void apply() throws IOException;

    public void verify();
    
    public void cleanUp();

}
