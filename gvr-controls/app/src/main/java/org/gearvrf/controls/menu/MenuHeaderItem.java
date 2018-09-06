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

package org.gearvrf.controls.menu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRBitmapImage;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.controls.R;
import org.gearvrf.controls.focus.ControlSceneObject;
import org.gearvrf.controls.shaders.ButtonShader;
import org.gearvrf.controls.util.GVRTextBitmapFactory;
import org.gearvrf.controls.util.RenderingOrder;
import org.gearvrf.controls.util.Text;

class MenuHeaderItem extends ControlSceneObject {

    private final int IDLE_STATE = 0;
    private final int HOVER_STATE = 1;
    private final int SELECTED_STATE = 2;

    private static final String TEXT_FONT_TYPE = "fonts/samsung-f-bik.ttf";

    private  int WIDTH = 0;
    private  int HEIGHT = 0;

    private float quadWidth = .85f;
    private float quadHeigth = 0.245f;

    private boolean isSelected = false;
    private headerType type;

    private ItemSelectedListener onTapListener;

    public enum headerType {
        MOTION, COLOR, SCALE, ROTATION
    }

    public MenuHeaderItem(GVRContext gvrContext, String title, headerType type, ItemSelectedListener onTapListener) {
        super(gvrContext);

        this.onTapListener = onTapListener;
        this.type = type;

        GVRMesh sMesh = getGVRContext().createQuad(quadWidth, quadHeigth);

        WIDTH = (int)(100.0f * quadWidth);
        HEIGHT = (int) (100.0f * quadHeigth);

        attachRenderData(new GVRRenderData(gvrContext));
        getRenderData().setMaterial(new GVRMaterial(gvrContext, new GVRShaderId(ButtonShader.class)));
        getRenderData().setMesh(sMesh);

        createTextures(gvrContext, title);

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);

        attachComponent(new GVRMeshCollider(getGVRContext(), false));
    }

    private void createTextures(GVRContext gvrContext, String title) {

        Text text = new Text(title, Align.CENTER, 2.8f, Color.parseColor("#4b4b4b"), Color.parseColor("#ffffff"), 255);
        GVRMaterial material = getRenderData().getMaterial();
        GVRAssetLoader importer = gvrContext.getAssetLoader();
        GVRBitmapImage bitmapIdle = new GVRBitmapImage(getGVRContext());

        bitmapIdle.setFileName(title + "_idle");
        bitmapIdle.setBitmap(createText(text, false));
        material.setTexture(ButtonShader.STATE1_BACKGROUND_TEXTURE,
                            importer.loadTexture(new GVRAndroidResource(gvrContext, R.raw.empty)));
        GVRTexture tex1 = new GVRTexture(gvrContext);
        tex1.setImage(bitmapIdle);
        material.setTexture(ButtonShader.STATE1_TEXT_TEXTURE, tex1);
        text.textColor = 0xffff6f54;

        GVRBitmapImage bitmapHover = new GVRBitmapImage(getGVRContext());
        bitmapHover.setFileName(title + "_hover");
        bitmapHover.setBitmap(createText(text, false));
        material.setTexture(ButtonShader.STATE2_BACKGROUND_TEXTURE,
                            importer.loadTexture(new GVRAndroidResource(gvrContext, R.raw.empty)));
        GVRTexture tex2 = new GVRTexture(gvrContext);
        tex2.setImage(bitmapHover);
        material.setTexture(ButtonShader.STATE2_TEXT_TEXTURE, tex2);

        GVRBitmapImage bitmapSelected = new GVRBitmapImage(getGVRContext());
        bitmapSelected.setFileName(title + "_selected");
        bitmapSelected.setBitmap(createText(text, true));
        material.setTexture(ButtonShader.STATE3_BACKGROUND_TEXTURE,
                            importer.loadTexture(new GVRAndroidResource(gvrContext, R.raw.empty)));

        GVRTexture tex3 = new GVRTexture(gvrContext);
        tex3.setImage(bitmapSelected);
        material.setTexture(ButtonShader.STATE3_TEXT_TEXTURE, tex3);
        getRenderData().setRenderingOrder(RenderingOrder.MENU_TEXT);
    }

    public Bitmap createText(Text text, boolean showBottomLine){

        if(showBottomLine){
            return create(getGVRContext().getContext(), WIDTH, HEIGHT, text, TEXT_FONT_TYPE);
        } else {
            return GVRTextBitmapFactory.create(getGVRContext().getContext(), WIDTH, HEIGHT, text, TEXT_FONT_TYPE);
        }
    }

    @Override
    public void gainedFocus() {

        if(!isSelected){
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, HOVER_STATE);
        }
    }

    @Override
    public void lostFocus() {

        if(!isSelected){
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        }
    }

    @Override
    protected void singleTap() {
        super.singleTap();

        if(!isSelected){
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
            setSelected(true);
            this.onTapListener.selected(this);
        }
    }

    public void select(){

        if(!isSelected){
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
            setSelected(true);
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void unselect() {
        isSelected = false;

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
    }

    public headerType getHeaderType() {
        return type;
    }

    public static Bitmap create(Context context, int width, int height, Text text, String font) {

        Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), font);

        Resources res = context.getResources();
        float scale = res.getDisplayMetrics().density;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Style.FILL);
        paint.setTypeface(myTypeface);
        paint.setTextSize(text.textSize * scale);
        paint.setFakeBoldText(true);
        paint.setColor(text.textColor);
        paint.setFilterBitmap(true);

        Rect rectText = new Rect();
        paint.getTextBounds(text.text, 0, text.text.length(), rectText);

        canvas.drawColor(text.backgroundColor);

        if(text.align == Align.CENTER){
            canvas.drawText(text.text, width / 2 - rectText.exactCenterX(), height / 2 - rectText.exactCenterY(), paint);
        } else if(text.align == Align.LEFT){
            canvas.drawText(text.text, 0, height / 2 - rectText.exactCenterY(), paint);
        }

        Paint bottomLine = new Paint();
        bottomLine.setStrokeWidth(3.5f);
        bottomLine.setColor(0xffff6f54);
        bottomLine.setStyle(Paint.Style.STROKE);
        bottomLine.setStrokeJoin(Paint.Join.ROUND);

        float x1 = 0;
        float x2 = width;

        float y1 = 22.9f;
        float y2 = y1;

        canvas.drawLine(x1, y1, x2, y2, bottomLine);

        return bitmap;
    }
}