package com.izforge.izpack.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by aabulawi on 25/09/14.
 */
public class PrefixFileFilter implements FilenameFilter {

    private String prefix;
    private boolean onlyDirectories;

    public PrefixFileFilter(String prefix, boolean onlyDirectories) {
        this.prefix = prefix;
        this.onlyDirectories = onlyDirectories;
    }

    public boolean accept(File dir, String name) {

        File fullPath = new File(dir.getAbsolutePath() + System.getProperty("file.separator") + name);
        if (onlyDirectories && !fullPath.isDirectory()){
            return false;
        }

        if (this.prefix.isEmpty() && name.startsWith(".")) {
            return false;
        } else if (name.startsWith(this.prefix)) {
            return true;
        } else {
            return false;
        }
    }
}
