package org.gearvrf.videoplayer;

import java.io.File;
import java.io.FileFilter;

public class VideosFileFilter implements FileFilter {
    @Override
    public boolean accept(File file) {
        return file.getName().toLowerCase().endsWith(".mp4");
    }
}
