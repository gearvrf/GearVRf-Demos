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


package org.gearvrf.simplesample;

import android.os.Bundle;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBone;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderData;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRVertexBuffer;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SampleActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(new SampleMain(), "gvr.xml");
    }

    private static class SampleMain extends GVRMain {
        @Override
        public void onInit(GVRContext ctx)
        {
            final int BATCH_SIZE = 3;
            final int NUM_STACKS = 9;
            final int NUM_SLICE = 4;
            final GVRScene scene = ctx.getMainScene();
            GVRCylinderSceneObject.CylinderParams cylparams = new GVRCylinderSceneObject.CylinderParams();
            GVRMaterial mtl = new GVRMaterial(ctx, GVRMaterial.GVRShaderType.Phong.ID);
            GVRSceneObject root = new GVRSceneObject(ctx);

            mtl.setDiffuseColor(1.0f, 0.5f, 0.8f, 1);
            cylparams.Material = mtl;
            cylparams.HasTopCap = false;
            cylparams.HasBottomCap = false;
            cylparams.TopRadius = 0.5f;
            cylparams.BottomRadius = 0.5f;
            cylparams.Height = 2.0f;
            cylparams.FacingOut = true;
            cylparams.StackNumber = NUM_STACKS;
            cylparams.SliceNumber = NUM_SLICE;
            cylparams.VertexDescriptor = "float3 a_position int4 a_bone_indices float4 a_bone_weights";
            GVRCylinderSceneObject cyl = new GVRCylinderSceneObject(ctx, cylparams);
            GVRMesh cylMesh = cyl.getRenderData().getMesh();

        /*
         * Add bone indices and bone weights to the cylinder vertex buffer.
         */
            GVRVertexBuffer vbuf = cylMesh.getVertexBuffer();

            int nverts = vbuf.getVertexCount();
            int vertsPerStack = nverts / cylparams.StackNumber;
            int[] boneIndices = new int[nverts * 4];
            float[] boneWeights = new float[nverts * 4];
            int v;

            Arrays.fill(boneIndices, 0, nverts * 4, 0);
            Arrays.fill(boneWeights, 0, nverts * 4, 0.0f);
        //
        // top of cylinder controlled by bone 0
        //
        for (int i = 0; i < BATCH_SIZE * vertsPerStack; ++i)
        {
            v = i * 4;
            boneWeights[v] = 1.0f;
        }
        //
        // middle of cylinder controlled by both bones
        //
        for (int i = BATCH_SIZE * vertsPerStack; i < (NUM_STACKS - BATCH_SIZE) * vertsPerStack; ++i)
        {
            v = i * 4;
            boneIndices[v + 1] = 1;
            boneWeights[v] = 0.5f;
            boneWeights[v + 1] = 0.5f;
        }
        //
        // bottom of cylinder controlled by bone 1
        //
        for (int i = (NUM_STACKS - BATCH_SIZE) * vertsPerStack; i < NUM_STACKS * vertsPerStack; ++i)
        {
            v = i * 4;
            boneIndices[v] = 1;
            boneWeights[v] = 1.0f;
        }
        /*
         * Define the two bones which control the mesh.
         * One bone is at the origin, the other is 1 unit below the first
         */
            GVRBone bone0 = new GVRBone(ctx);
            GVRBone bone1 = new GVRBone(ctx);
            Matrix4f bone0Mtx = new Matrix4f();
            Matrix4f bone1Mtx = new Matrix4f();
            float[] temp1 = new float[16];
            float[] temp2 = new float[16];
            List<GVRBone> bones = new ArrayList<>();
            Matrix4f finalMatrix = new Matrix4f();

            finalMatrix.get(temp2);
            bones.add(bone0);
            bones.add(bone1);
            bone0.setName("top");
            bone0Mtx.get(temp1);
            bone0.setOffsetMatrix(temp1);
            bone1.setName("bottom");
            bone1Mtx.translate(0, -1.0f, 0.0f);
            bone1Mtx.get(temp1);
            bone1.setOffsetMatrix(temp1);
            cylMesh.setBones(bones);
            vbuf.setIntArray("a_bone_indices", boneIndices);
            vbuf.setFloatArray("a_bone_weights", boneWeights);
            bone1.setFinalTransformMatrix(temp2);
            finalMatrix.rotate(45, 1, 0, 0);
            finalMatrix.get(temp2);
            bone0.setFinalTransformMatrix(temp2);
            root.getTransform().setPositionZ(-3.0f);
            root.addChildObject(cyl);
            scene.addSceneObject(root);
        }
    }
}