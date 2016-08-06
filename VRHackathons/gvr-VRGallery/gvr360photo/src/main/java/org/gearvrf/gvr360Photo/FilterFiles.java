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

package org.gearvrf.gvr360Photo;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FilterFiles implements FilenameFilter {

    private ArrayList<String> myExtension;
    public FilterFiles(ArrayList<String> sExtension){
        myExtension = sExtension;
    }
    @Override
    public boolean accept(File directory, String fileName) {
        if(myExtension != null)
        for(String tExtension : myExtension) {
            if (fileName.endsWith(tExtension)) {
                return true;
            }
        }
        return false;
    }
}
