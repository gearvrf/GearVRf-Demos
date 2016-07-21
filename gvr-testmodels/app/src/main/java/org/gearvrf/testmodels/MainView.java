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

package org.gearvrf.testmodels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSceneObject.BoundingVolume;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMain;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.FileNameUtils;
import org.gearvrf.utility.Log;
import org.gearvrf.IAssetEvents;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;

public class MainView extends GVRMain
{
    class AssetListener implements IAssetEvents
    {
        @Override
        public void onAssetLoaded(GVRContext ctx, GVRSceneObject model, String filename, String errors)
        {
            if (model != null)
            {
                mState = 10;
                BoundingVolume bv = model.getBoundingVolume();
                float sf = 1 / (4.0f * bv.radius);
                model.getTransform().setScale(sf, sf, sf);
                bv = model.getBoundingVolume();
                model.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z - 1.5f * bv.radius);
                mMainScene.clear();
                mCurrentModel = (GVRModelSceneObject) model;
                if (mMainScene.getLightList().length == 0)
                {
                	GVRDirectLight headlight = new GVRDirectLight(ctx);
                	mMainScene.getMainCameraRig().getOwnerObject().attachComponent(headlight);
                    mMainScene.addSceneObject(model);
                }
             }
            else
            {
                mState = 0;
            }
        }
        public void onModelError(GVRContext arg0, String arg1, String arg2) { }
        public void onModelLoaded(GVRContext arg0, GVRSceneObject arg1, String arg2) { }
        public void onTextureError(GVRContext arg0, String arg1, String arg2) { }
        public void onTextureLoaded(GVRContext arg0, GVRTexture arg1, String arg2) { }        
    }

    private static final String TAG = Log
            .tag(MainActivity.class);

    private GVRScene mMainScene;
    private GVRContext mContext;
    private int mCurrentFileIndex;
    private String mFileName;
    private ArrayList<String> mFileList = new ArrayList<String>();
    private GVRModelSceneObject mCurrentModel;
    private int mState = 0;
    private AssetListener mAssetListener;

    @Override
    public void onInit(GVRContext gvrContext) {
        mContext = gvrContext;
        mMainScene = gvrContext.getNextMainScene();
        mMainScene.setFrustumCulling(true);
        mCurrentFileIndex = 0;
        mCurrentModel = null;
        getModelList("GearVRF", mFileList);
        GVRCameraRig mainCameraRig = mMainScene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.WHITE);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.WHITE);
        mainCameraRig.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        mAssetListener = new AssetListener();
    }

    public GVRModelSceneObject loadModel(String filename)
    {
        GVRModelSceneObject model = (GVRModelSceneObject) mMainScene.getSceneObjectByName(filename);
        if (model == null)
        {
            try
            {
               model = mContext.getAssetLoader().loadModel("sd:" + filename, mAssetListener);
               model.setName(filename);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Failed to load model: %s", e);
                mState = 0;
                return null;
            }
        }
        return model;
    }

    private void getModelList(String directory, ArrayList<String> fileList)
    {
        final String environmentPath = Environment.getExternalStorageDirectory().getPath();
        String extensions = ".fbx .3ds .dae .obj .ma .x3d";
        File dir = new File(environmentPath + "/" + directory);

        if (dir.exists() && dir.isDirectory())
        {
            File list[] = dir.listFiles();
            for (File f : list)
            {
                String fileName = f.getName();
                String ext = "." + FileNameUtils.getExtension(fileName.toLowerCase());
                if (f.isDirectory())
                {
                    getModelList(directory + "/" + fileName, fileList);
                }
                else if (extensions.contains(ext))
                {
                    fileList.add(directory + "/" + fileName);
                }
             }
        }
    }
    
    public void onTap() {}
    
    public void onStep()
    {
        if (mState == 0)
        {
            mState = 1;     	// 1 = model loading
            switchModel();
        }
        else if (mState >= 2)	// >2 = wait for display
        {
        	if (mState == 2)	// 2 = taking screenshot
        	{
            	screenShot(mFileName);        		
        	}
        	--mState;
        }
    }
    
    void switchModel()
    {
        if (mCurrentFileIndex >= mFileList.size())
        {
            return;          
        }
        mFileName = mFileList.get(mCurrentFileIndex++);
        GVRModelSceneObject model = loadModel(mFileName);
        if (model == null)
        {
            return;
        }
        if (mCurrentModel != null)
        {
            mCurrentModel.setEnable(false);
        }
        model.setEnable(true);
        mCurrentModel = model;
    }

    void screenShot(final String filename)
    {
        GVRScreenshotCallback callback = new GVRScreenshotCallback() 
        {
            @Override
            public void onScreenCaptured(Bitmap bitmap)
            {
                try
                {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                    File sdcard = Environment.getExternalStorageDirectory();
                    int i = filename.lastIndexOf("/");
                    String basename = filename;
                    if (i > 0)
                    {
                        basename = filename.substring(i + 1);
                    }
                    basename = FileNameUtils.getBaseName(basename);
                    String fname = sdcard.getAbsolutePath() + "/GearVRF/" + basename + "_screen.png";
                    File f = new File(fname);
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    Log.d(TAG, "Saved screenshot of %s", filename);
                    mState = 0;
                }
                catch (Exception e)
                {
                    Log.d(TAG, "Could not save screenshot of %s", filename);
                    mState = 0;
                }
            }   
        };
        mContext.captureScreenCenter(callback);
    }
}
