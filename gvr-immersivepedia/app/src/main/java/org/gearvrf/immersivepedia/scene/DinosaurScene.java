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

package org.gearvrf.immersivepedia.scene;

import java.io.IOException;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAndroidResource.TextureCallback;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.immersivepedia.GazeController;
import org.gearvrf.immersivepedia.R;
import org.gearvrf.immersivepedia.dinosaur.DinosaurFactory;
import org.gearvrf.immersivepedia.model.GalleryDinosaurGroup;
import org.gearvrf.immersivepedia.model.RotateDinosaurGroup;
import org.gearvrf.immersivepedia.model.TextDinosaurGroup;
import org.gearvrf.immersivepedia.model.VideoDinosaurGroup;
import org.gearvrf.immersivepedia.util.FPSCounter;

public class DinosaurScene extends GVRScene {

    public static final float CAMERA_Y = 1.6f;

    GVRScene scene;

    private VideoDinosaurGroup videoDinosaur;
    private GalleryDinosaurGroup galleryDinosaur = null;
    private GVRContext gvrContext;
    private TextDinosaurGroup textDinosaur;

	private RotateDinosaurGroup rotateDinosaur;

    public DinosaurScene(GVRContext gvrContext) throws IOException {
        super(gvrContext);
        this.gvrContext = gvrContext;
        DinosaurFactory.getInstance(gvrContext);
        getMainCameraRig().getTransform().setPositionY(CAMERA_Y);

        createVideoDinosauGroup(); // TRex
        createTextDinosaurGroup(); // Ankylosaurus
        createRotateDinosaurGroup(); // Styracosaurus
        createGalleryDinosaurGroup(); // Apatosaurus

        addSceneObject(createSkybox()); //

        hide();
        addSceneObject(createBlueSkybox()); //
    }

    private void createRotateDinosaurGroup() throws IOException {
        rotateDinosaur = new RotateDinosaurGroup(getGVRContext(), this);
        addSceneObject(rotateDinosaur);
    }

    private void createTextDinosaurGroup() throws IOException {

        textDinosaur = new TextDinosaurGroup(getGVRContext(), this);

        textDinosaur.getTransform().setPositionZ(-DinosaurFactory.ANKYLOSAURUS_DISTANCE);

        textDinosaur.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.ANKYLOSAURUS_ANGLE_AROUND_CAMERA, 0, 1, 0, 0, 0, 0);

        addSceneObject(textDinosaur);

    }

    private void createVideoDinosauGroup() throws IOException {

        videoDinosaur = new VideoDinosaurGroup(getGVRContext(), this);

        videoDinosaur.getTransform().setPositionZ(-DinosaurFactory.TREX_DISTANCE);

        videoDinosaur.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.TREX_ANGLE_AROUND_CAMERA, 0, 1, 0, 0, 0, 0);

        addSceneObject(videoDinosaur);
    }

    private void createGalleryDinosaurGroup() throws IOException {
        galleryDinosaur = new GalleryDinosaurGroup(gvrContext, this);
        addSceneObject(galleryDinosaur);
    }

    public void onStep() {
        FPSCounter.tick();
        if (this.videoDinosaur != null) {
            this.videoDinosaur.onStep();
        }
    }

    private GVRSceneObject.ComponentVisitor showAnimator = new GVRSceneObject.ComponentVisitor()
    {
        @Override
        public boolean visit(GVRComponent comp)
        {
            GVRRenderData rd = (GVRRenderData) comp;
            GVRMaterial mtl = rd.getMaterial();
            GVRSceneObject owner = rd.getOwnerObject();
            try
            {
                new GVROpacityAnimation(owner, 1f, 1f).start(getGVRContext().getAnimationEngine());
            }
            catch (UnsupportedOperationException ex)
            {
                // shader doesn't support opacity
            }
            return true;
        }
    };

    private GVRSceneObject.ComponentVisitor hideAll = new GVRSceneObject.ComponentVisitor()
    {
        @Override
        public boolean visit(GVRComponent comp)
        {
            GVRRenderData rd = (GVRRenderData) comp;
            GVRMaterial mtl = rd.getMaterial();

            if (mtl.hasUniform("u_opacity"))
            {
                rd.getMaterial().setOpacity(0.0f);
            }
            else if (mtl.hasUniform("diffuse_color"))
            {
                float[] c = mtl.getDiffuseColor();
                mtl.setDiffuseColor(c[0], c[1], c[2], 0.0f);
            }
            return true;
        }
    };

    public void show() {
        GazeController.get().enableGaze();
        getRoot().forAllComponents(showAnimator, GVRRenderData.getComponentType());
    }

    public void hide() {
        getRoot().forAllComponents(hideAll, GVRRenderData.getComponentType());
    }

    private GVRSceneObject createSkybox() {
        GVRAssetLoader loader = getGVRContext().getAssetLoader();
        GVRMesh mesh = loader.loadMesh(new GVRAndroidResource(getGVRContext(), R.raw.environment_walls_mesh));
        GVRTexture texture = loader.loadTexture(new
                GVRAndroidResource(gvrContext, R.raw.environment_walls_tex_diffuse));
        final GVRSceneObject skybox = new GVRSceneObject(getGVRContext(), mesh, texture);

        skybox.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0, 0);
        skybox.getRenderData().setRenderingOrder(0);

        GVRMesh meshGround = loader.loadMesh(new GVRAndroidResource(getGVRContext(), R.raw.environment_ground_mesh));
        GVRTexture textureGround = getGVRContext().getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.raw.environment_ground_tex_diffuse));
        final GVRSceneObject skyboxGround = new GVRSceneObject(getGVRContext(), meshGround, textureGround);

        skyboxGround.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);

        GVRMesh meshFx =loader.loadMesh(new GVRAndroidResource(getGVRContext(), R.raw.windows_fx_mesh));
        GVRTexture textureFx = getGVRContext().getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.windows_fx_tex_diffuse));
        GVRSceneObject skyboxFx = new GVRSceneObject(getGVRContext(), meshFx, textureFx);
        skyboxGround.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        skyboxFx.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND + 1);
        skyboxGround.setName("SkyBoxGround");
        skyboxFx.setName("SkyBoxFX");
        skybox.addChildObject(skyboxFx);
        skybox.addChildObject(skyboxGround);

        return skybox;
    }

    private GVRSceneObject createBlueSkybox() {

        GVRMesh mesh = getGVRContext().getAssetLoader().loadMesh(new GVRAndroidResource(getGVRContext(), R.raw.skybox_mesh));
        GVRTexture texture = getGVRContext().getAssetLoader().loadTexture(new
                GVRAndroidResource(getGVRContext(), R.drawable.dino_skybox_tex_diffuse));
        GVRSceneObject skybox = new GVRSceneObject(getGVRContext(), mesh, texture);
        skybox.getTransform().setScale(1, 1, 1);
        skybox.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        skybox.setName("BlueSkyBox");
        return skybox;
    }

    public void closeObjectsInScene() {
        if (galleryDinosaur.isOpen()) {
            galleryDinosaur.closeThis();
        }
        if (textDinosaur.isOpen()) {
            textDinosaur.closeAction();
        }
        if (videoDinosaur.isOpen()) {
            videoDinosaur.closeAction();
        }
    }

	public void onPause() {
		if (rotateDinosaur.isPlayed) {
			rotateDinosaur.pauseAnimation();
		}
        if (null != videoDinosaur) {
            videoDinosaur.pauseVideo();
        }
	}
}
