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

package org.gearvrf.keyboard.keyboard.model;

import android.graphics.Color;
import android.graphics.Paint;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapImage;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.keyboard.model.KeyboardCharItem;
import org.gearvrf.keyboard.shader.TransparentButtonShaderThreeStates;
import org.gearvrf.keyboard.util.GVRTextBitmapFactory;
import org.gearvrf.keyboard.util.RenderingOrder;
import org.gearvrf.keyboard.util.SceneObjectNames;
import org.gearvrf.keyboard.util.Util;

public abstract class KeyboardItemBase extends GVRSceneObject {

    protected KeyboardItemStyle styleItem;
    protected KeyboardCharItem keyboardCharItem;

    public KeyboardItemBase(GVRContext gvrContext, KeyboardItemStyle styleItem) {
        super(gvrContext);
        setName(SceneObjectNames.KEYBOARD_ITEM);

        this.styleItem = styleItem;
    }

    public abstract void setNormalMaterial();

    public abstract void setHoverMaterial();

    public abstract void switchMaterialState(int state);

    public float getWidth() {
        return styleItem.getSizeQuadWidth() + styleItem.getSpace();
    }

    public void createTextures() {

        GVRRenderData renderData = new GVRRenderData(getGVRContext());

        GVRMesh mesh = new GVRMesh(getGVRContext(), "float3 a_position float2 a_texcoord");
        mesh.createQuad(
                Util.convertPixelToVRFloatValue(styleItem.getSizeQuadWidth()),
                Util.convertPixelToVRFloatValue(styleItem.getSizeQuadHeight()));

        GVRMaterial mat = new GVRMaterial(getGVRContext(), new GVRShaderId(TransparentButtonShaderThreeStates.class));//dif.getShaderId());
        renderData.setMesh(mesh);
        renderData.setMaterial(mat);

        attachRenderData(renderData);

        getRenderData().setRenderingOrder(RenderingOrder.KEYBOARD);

        getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.OPACITY,
                styleItem.getOpacityTarget());
    }

    public void configureTextures() {

        getRenderData().getMaterial().setTexture(
                TransparentButtonShaderThreeStates.TEXTURE_KEY,
                getGVRContext().getAssetLoader().loadTexture(
                        new GVRAndroidResource(getGVRContext(), styleItem.getTexture())));

        getRenderData().getMaterial().setTexture(
                TransparentButtonShaderThreeStates.TEXTURE_HOVER_KEY,
                getGVRContext().getAssetLoader().loadTexture(
                        new GVRAndroidResource(getGVRContext(), styleItem.getTextureHover())));

        getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH,
                0.0f);
    }

    protected void setTextureFromResource(String shaderKey, int resource) {

        getRenderData().getMaterial().setTexture(shaderKey,
                getGVRContext().getAssetLoader().loadTexture(new GVRAndroidResource(getGVRContext(), resource)));
    }

    public void setNomalTexture(String character, String ShaderKey) {

        GVRBitmapImage bitmapNormal = new GVRBitmapImage(getGVRContext(),
                GVRTextBitmapFactory.create(
                        styleItem.getCharacterBackgroundWidth(),
                        styleItem.getCharacterBackgroundHeight(), character, styleItem
                                .getFontSize(), Paint.Align.CENTER,
                        styleItem.getColorText(), Color.argb(0, 0, 0, 0), getGVRContext()
                                .getContext().getApplicationContext()));

        GVRTexture texture = new GVRTexture(getGVRContext());
        texture.setImage(bitmapNormal);
        getRenderData().getMaterial().setTexture(ShaderKey, texture);
    }

    public void setHoverTexture(String character, String ShaderKey) {

        GVRBitmapImage bitmapHover = new GVRBitmapImage(getGVRContext(),
                GVRTextBitmapFactory.create(
                        styleItem.getCharacterBackgroundWidth(),
                        styleItem.getCharacterBackgroundHeight(), character,
                        styleItem.getFontSize(), Paint.Align.CENTER,
                        styleItem.getHoverTextColor(), styleItem.getColorBackgroundTextHover(),
                        getGVRContext().getContext().getApplicationContext()));

        GVRTexture texture = new GVRTexture(getGVRContext());
        texture.setImage(bitmapHover);
        getRenderData().getMaterial().setTexture(ShaderKey, texture);
    }

    public KeyboardCharItem getKeyboardCharItem() {
        return keyboardCharItem;
    }
}
