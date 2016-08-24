package org.gearvrf.util;


import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import org.gearvrf.GVRActivity;

import java.io.IOException;

public final class AssetsReader {

   public static String[] getAssetsList(GVRActivity activity, String sDirectoryPath) {
        String sAList[] = null;
        try {
            Resources resources = activity.getResources();
            AssetManager assetManager = resources.getAssets();
            sAList = assetManager.list(sDirectoryPath);
            for (int i = 0; i < sAList.length; i++) {
                Log.d("", sAList[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Modelviewer", "Directory " + sDirectoryPath + " not found");
        }
        return sAList;
    }
}
