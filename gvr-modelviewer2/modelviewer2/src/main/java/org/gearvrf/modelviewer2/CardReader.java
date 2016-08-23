package org.gearvrf.modelviewer2;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 /* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class CardReader {

    private String myDirectory;
    private ArrayList<String> myExtension;

    public CardReader(String sDirectory, ArrayList<String> sExtension){
        myDirectory = sDirectory;
        myExtension = sExtension;
    }
    public File[] getModels(){
        File directory = new File(myDirectory);

        if (directory.exists() && directory.isDirectory()) {
            String extensions[] = new String[myExtension.size()];
            myExtension.toArray(extensions);

            List<File> listFiles = (List<File>) FileUtils.listFiles(directory, extensions, true);
            File list[] = new File[listFiles.size()];
            listFiles.toArray(list);
            return list;
        }
        return null;
    }

}
