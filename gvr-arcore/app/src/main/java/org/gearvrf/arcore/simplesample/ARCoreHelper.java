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

package org.gearvrf.arcore.simplesample;

import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRTransform;
import org.gearvrf.arcore.simplesample.arobjects.GVRAnchorObject;
import org.gearvrf.arcore.simplesample.arobjects.GVRPlaneObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper to handle virtual objects.
 */
public class ARCoreHelper {
    private static String TAG = "GVR_ARCORE";
    private static int MAX_VIRTUAL_OBJECTS = 20;

    private final GVRContext mGvrContext;
    private final GVRScene mScene;

    // Cache of virtual objects
    private List<GVRAnchorObject> mVirtualObjects;
    private Map<Plane, GVRPlaneObject> mVirtualPlanes;

    private int mVirtObjCount;

    public ARCoreHelper(GVRContext gvrContext, GVRScene scene) {
        mGvrContext = gvrContext;
        mScene = scene;
        mVirtualPlanes = new HashMap<>();
        mVirtualObjects = new ArrayList<GVRAnchorObject>();
        mVirtObjCount = 0;
    }

    public void updateVirtualObjects(float[] arViewMatrix, float[] vrCamMatrix, float scale) {
        for (GVRAnchorObject obj: mVirtualObjects) {
            obj.update(arViewMatrix, vrCamMatrix, scale);
        }
    }

    public void addVirtualObject(Anchor anchor) {
        if (anchor == null)
            return;

        if (mVirtObjCount < MAX_VIRTUAL_OBJECTS) {
            GVRAnchorObject gvrAnchor = new VirtualObject(mGvrContext);

            mScene.addSceneObject(gvrAnchor);
            mVirtualObjects.add(gvrAnchor);
        }

        GVRAnchorObject obj = mVirtualObjects.get(mVirtObjCount % mVirtualObjects.size());
        obj.setARAnchor(anchor);
        obj.setName("id: " + mVirtObjCount);

        Log.d(TAG, "New virtual object " + obj.getName());

        mVirtObjCount++;
    }

    public void removeAllVirtualObjects() {
        if (mVirtObjCount == 0)
            return;

        for (GVRAnchorObject obj: mVirtualObjects) {
            obj.setARAnchor(null);
        }

        mVirtObjCount = 0;
    }

    public Anchor createARCoreAnchor(List<HitResult> hitResult) {
        for (HitResult hit : hitResult) {
            // Check if any plane was hit, and if it was hit inside the plane polygon
            Trackable trackable = hit.getTrackable();
            // Creates an anchor if a plane or an oriented point was hit.
            if ((trackable instanceof Plane
                    && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                    && ((Plane) trackable).getSubsumedBy() == null) {
                return hit.createAnchor();
            }
        }

        return null;
    }

    public void updateVirtualPlanes(Collection<Plane> allPlanes,
                                     float[] arViewMatrix, float[] vrCamMatrix, float scale) {
        for (Plane plane : allPlanes) {
            if (plane.getTrackingState() != TrackingState.TRACKING || plane.getSubsumedBy() != null
                    || mVirtualPlanes.containsKey(plane)) {
                continue;
            }

            addVirtualPlane(plane);
        }

        for (Plane plane: mVirtualPlanes.keySet()) {
            GVRPlaneObject gvrPlane = mVirtualPlanes.get(plane);

            if (!gvrPlane.update(arViewMatrix, vrCamMatrix, scale)) {
                removeVirtualPlane(gvrPlane);
            } else {
                // FIXME: draw plane's polygon to have a real representation.
                /* The plane's polygon may change at runtime so we may need
                a way to update GVRMesh at runtime.
                 */
                GVRTransform t = gvrPlane.getChildByIndex(0).getTransform();
                t.setScale(plane.getExtentX() * 0.95f, plane.getExtentZ() * 0.95f, 1.0f);
            }
        }
    }

    private void addVirtualPlane(Plane plane) {
        GVRPlaneObject gvrPlane = new VirtualPlane(mGvrContext);
        gvrPlane.setARPlane(plane);

        mScene.addSceneObject(gvrPlane);
        mVirtualPlanes.put(gvrPlane.getARPlane(), gvrPlane);

        Log.d(TAG, "Number of detected planes: " + mVirtualPlanes.size());
    }

    public void removeAllVirtualPlanes() {
        for (Plane plane: mVirtualPlanes.keySet()) {
            removeVirtualPlane(plane);
        }
    }

    private void removeVirtualPlane(Plane plane) {
        removeVirtualPlane(mVirtualPlanes.get(plane));
    }

    private void removeVirtualPlane(GVRPlaneObject gvrPlane) {
        mScene.removeSceneObject(gvrPlane);
        mVirtualPlanes.remove(gvrPlane.getARPlane());
        gvrPlane.setARPlane(null);
    }


}
