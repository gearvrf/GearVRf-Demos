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

package org.gearvrf.modelviewer2;


import org.gearvrf.utility.Log;
import org.gearvrf.widgetplugin.GVRWidget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import java.util.ArrayList;

public class MyMenu extends GVRWidget {

    private Stage mStage;
    private Table mContainer;
    public ModelViewer2Manager mManager;

    float mFontScale = 4.5f;
    Skin skin;

    boolean flagForSkyBox = true;
    boolean flagForCustomShader = true;
    boolean flagForAnimation = true;
    boolean flagForModels = true;
    boolean flagForLights = true;
    boolean lightOnOff = false;

    public void create() {
        mStage = new Stage();
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        Gdx.input.setInputProcessor(mStage);

        // Parent Table contains all child tables
        mContainer = new Table();
        mStage.addActor(mContainer);
        mContainer.setFillParent(true);

        // Add Items Required for Menu and and it to child table

        Table childTable = new Table();
        final ScrollPane scroll = new ScrollPane(childTable, skin);

        InputListener stopTouchDown = new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                event.stop();
                return false;
            }
        };

        childTable.row();
        //childTable.add(new Label("", skin)).expandX().fillX();

        // Adding Position Select Box
        childTable.row();
        BitmapFont f = skin.getFont("default-font");
        f.getData().setScale(mFontScale - 1.0f);

        //childTable.add(new Label("", skin)).expandX().fillX();
        SelectBoxStyle style = new SelectBoxStyle(f, Color.WHITE,
                skin.getDrawable("default-select"),
                skin.get(ScrollPaneStyle.class),
                skin.get(ListStyle.class));


        Label modelLabel = new Label("Models", skin);
        modelLabel.setFontScale(mFontScale);
        childTable.add(modelLabel);

        final SelectBox selectBoxModels = new SelectBox(style);
        selectBoxModels.setName("ModelsType");
        selectBoxModels.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                mManager.setSelectedModel(selectBoxModels.getSelectedIndex());
                flagForAnimation = true;
            }
        });

        selectBoxModels.setVisible(true);
        childTable.add(selectBoxModels).height(120.0f).width(600.0f);


        // For Animations
        Label animaLabel = new Label("Animations", skin);
        animaLabel.setFontScale(mFontScale);
        childTable.add(animaLabel);

        final SelectBox selectBoxA = new SelectBox(style);
        selectBoxA.setName("AnimationType");
        selectBoxA.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                mManager.setSelectedAnimation(selectBoxA.getSelectedIndex());
            }
        });

        selectBoxA.setVisible(true);
        selectBoxA.setItems("Animation None");
        childTable.add(selectBoxA).height(120.0f).width(600.0f);

        childTable.row();


        // Labels for Light and Parameters
        Label lightLabel = new Label("Light(On/Off)", skin);
        lightLabel.setFontScale(mFontScale);
        childTable.add(lightLabel);

        Label ambientLabel = new Label("Ambient", skin);
        ambientLabel.setFontScale(mFontScale);
        childTable.add(ambientLabel);

        Label diffuseLabel = new Label("Diffuse", skin);
        diffuseLabel.setFontScale(mFontScale);
        childTable.add(diffuseLabel);

        Label specularLabel = new Label("Specular", skin);
        specularLabel.setFontScale(mFontScale);
        childTable.add(specularLabel);

        childTable.row();
        // Check Box For Lights
        final CheckBox box = new CheckBox("Lights", skin);
        box.setChecked(false);
        box.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                lightOnOff = box.isChecked();
                mManager.turnOnOffLight(box.isChecked());
            }
        });
        box.getLabel().setFontScale(mFontScale);
        box.getCells().get(0).size(80.0f, 80.0f);
        childTable.add(box);

        // Ambient
        final SelectBox selectBoxAmbient = new SelectBox(style);
        selectBoxAmbient.setName("AmbientType");
        selectBoxAmbient.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                mManager.setAmbient(selectBoxAmbient.getSelectedIndex(), lightOnOff);
            }
        });


        selectBoxAmbient.setVisible(true);
        childTable.add(selectBoxAmbient).height(120.0f).width(600.0f);

        // Diffuse
        final SelectBox selectBoxDiffuse = new SelectBox(style);
        selectBoxDiffuse.setName("DiffuseType");
        selectBoxDiffuse.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                mManager.setDiffuse(selectBoxDiffuse.getSelectedIndex(), lightOnOff);
            }
        });

        selectBoxDiffuse.setVisible(true);
        childTable.add(selectBoxDiffuse).height(120.0f).width(600.0f);

        // Specular
        final SelectBox selectBoxSpecular = new SelectBox(style);
        selectBoxSpecular.setName("SpecularType");
        selectBoxSpecular.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                mManager.setSpecular(selectBoxSpecular.getSelectedIndex(), lightOnOff);
            }
        });

        selectBoxSpecular.setVisible(true);
        childTable.add(selectBoxSpecular).height(120.0f).width(600.0f);


        childTable.row();
        Label SkyBoxLabel = new Label("SkyBox", skin);
        SkyBoxLabel.setFontScale(mFontScale);
        childTable.add(SkyBoxLabel);

        final SelectBox selectBox = new SelectBox(style);
        selectBox.setName("SkyBoxType");
        selectBox.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                mManager.addSkyBox(selectBox.getSelectedIndex());
            }
        });

        selectBox.setVisible(true);
        childTable.add(selectBox).height(120.0f).width(600.0f);

        childTable.row();

        Label CSLabel = new Label("Custom Shader", skin);
        CSLabel.setFontScale(mFontScale);
        childTable.add(CSLabel);

        final SelectBox selectBoxCP = new SelectBox(style);
        selectBoxCP.setName("CustomShaderType");
        selectBoxCP.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                mManager.setSelectedCustomShader(selectBoxCP.getSelectedIndex());
            }
        });

        selectBoxCP.setVisible(true);
        childTable.add(selectBoxCP).height(120.0f).width(600.0f);

        childTable.row();

        // Slider for Zoom
        childTable.row();
        Slider slider = null;
        slider = new Slider(0, 100, 1, false, skin);
        slider.setName("Zoom");
        slider.setVisible(true);
        slider.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                float value = ((Slider) actor).getValue();
                Log.e("Abhijit", "Value zoom" + ((Slider) actor).getValue());


                mManager.zoomCurrentModel(value);
            }
        });
        Label zoom = new Label("  Zoom  ", skin);
        zoom.setFontScale(mFontScale);
        childTable.pad(10).add(zoom);
        childTable.add(slider).height(150.0f).width(800);


        mContainer.add(scroll).expand().fill().colspan(1);
        mContainer.row().space(1).padBottom(1);
    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mStage.act(Gdx.graphics.getDeltaTime());

        if (flagForSkyBox && mManager.controllerReadyFlag) {
            Actor tempActor = mStage.getRoot().findActor("SkyBoxType");
            ArrayList<String> list = mManager.getSkyBoxList();
            String tempList[] = new String[list.size()];
            for (int i = 0; i < list.size(); i++)
                tempList[i] = list.get(i);

            ((SelectBox) tempActor).setItems(tempList);
            flagForSkyBox = false;
        }

        if (flagForModels && mManager.controllerReadyFlag) {
            Actor tempActor = mStage.getRoot().findActor("ModelsType");
            ArrayList<String> list = mManager.getModelsList();
            String tempList[] = new String[list.size()];
            for (int i = 0; i < list.size(); i++)
                tempList[i] = list.get(i);

            ((SelectBox) tempActor).setItems(tempList);
            flagForModels = false;
        }


        if (flagForCustomShader && mManager.controllerReadyFlag) {
            Actor tempActor = mStage.getRoot().findActor("CustomShaderType");
            ArrayList<String> list = mManager.getListOfCustomShaders();
            String tempList[] = new String[list.size()];
            for (int i = 0; i < list.size(); i++)
                tempList[i] = list.get(i);

            ((SelectBox) tempActor).setItems(tempList);
            flagForCustomShader = false;
        }

        if (flagForAnimation && mManager.controllerReadyFlag && mManager.isModelPresent()) {
            Actor tempActor = mStage.getRoot().findActor("AnimationType");
            int count = mManager.getCountOfAnimations();
            ArrayList<String> list = new ArrayList<String>();
            list.add("Animation None");
            for (int i = 0; i < count; i++)
                list.add("Animation " + Integer.toString(i));

            String tempList[] = new String[list.size()];
            for (int i = 0; i < list.size(); i++)
                tempList[i] = list.get(i);

            ((SelectBox) tempActor).setItems(tempList);
            flagForAnimation = false;
        }

        if (flagForLights && mManager.controllerReadyFlag) {
            Actor tempActor = mStage.getRoot().findActor("AmbientType");
            ArrayList<String> list = mManager.getAmbient();
            String tempList[] = new String[list.size()];
            for (int i = 0; i < list.size(); i++)
                tempList[i] = list.get(i);

            ((SelectBox) tempActor).setItems(tempList);

            list.clear();
            tempActor = mStage.getRoot().findActor("DiffuseType");
            list = mManager.getDiffuse();
            tempList = new String[list.size()];
            for (int i = 0; i < list.size(); i++)
                tempList[i] = list.get(i);

            ((SelectBox) tempActor).setItems(tempList);

            list.clear();
            tempActor = mStage.getRoot().findActor("SpecularType");
            list = mManager.getSpecular();
            tempList = new String[list.size()];
            for (int i = 0; i < list.size(); i++)
                tempList[i] = list.get(i);

            ((SelectBox) tempActor).setItems(tempList);

            flagForLights = false;
        }

        mStage.draw();
    }

    public void resize(int width, int height) {
        super.resize(width, height);
        mStage.getViewport().update(width, height, true);
    }

    public void dispose() {
        mStage.dispose();
    }

    public boolean needsGL20() {
        return false;
    }
}
