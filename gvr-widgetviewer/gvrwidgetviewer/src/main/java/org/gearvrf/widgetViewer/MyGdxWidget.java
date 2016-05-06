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

package org.gearvrf.widgetViewer;


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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

public class MyGdxWidget extends GVRWidget {

    private Stage mStage;
    private Table mContainer;
    public boolean mResetSlider = false;
    Actor xSlider;
    Actor ySlider;
    Actor zSlider;
    Actor mColorButtonActor;
    Actor mLookInsideButtonActor;
    public float mX, mY, mZ;
    public ViewerScript mScript;
    Button mNextButton;
    Button mPreviousButton;
    Button mColorButton;
    Button mResetButton;
    Button mLookInsideButton;
    public CheckBox mCheckBox;  
    float mFontScale = 4.0f;

    @SuppressWarnings("unchecked")
    public void create() {
        mStage = new Stage();
        Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        Gdx.input.setInputProcessor(mStage);
        mContainer = new Table();
        mStage.addActor(mContainer);
        mContainer.setFillParent(true);
        Table table = new Table();
        final ScrollPane scroll = new ScrollPane(table, skin);

        InputListener stopTouchDown = new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                    int pointer, int button) {
                event.stop();
                return false;
            }
        };

        table.pad(0).defaults().expandX().space(10);
        for (int i = 0; i < 4; i++) {
            table.row();
            table.add(new Label("", skin)).expandX().fillX();
            TextButton button = null;
            if (i == 0) {
                button = new TextButton("  Next  ", skin);
                button.getLabel().setFontScale(mFontScale);
                mNextButton = button;
                button.addListener(new ClickListener() {
                    public void clicked(InputEvent event, float x, float y) {
                        System.out.println("click " + x + ", " + y);

                        mScript.ThumbnailSelected = (mScript.ThumbnailSelected + 1) % 5;
                        mNextButton.setChecked(false);
                    }
                });
            } else if (i == 1) {
                button = new TextButton("Previous", skin);
                button.getLabel().setFontScale(mFontScale);
                mPreviousButton = button;
                button.addListener(new ClickListener() {
                    public void clicked(InputEvent event, float x, float y) {
                        System.out.println("click " + x + ", " + y);
                        mScript.ThumbnailSelected = (mScript.ThumbnailSelected + 4) % 5;
                        mPreviousButton.setChecked(false);
                    }
                });
            } else if (i == 2) {

                BitmapFont f = skin.getFont("default-font");
                f.getData().setScale(mFontScale - 1.0f);
                SelectBoxStyle style = new SelectBoxStyle(f, Color.WHITE,
                        skin.getDrawable("default-select"),
                        skin.get(ScrollPaneStyle.class),
                        skin.get(ListStyle.class));

                final SelectBox selectBox = new SelectBox(style);
                selectBox.addListener(new ChangeListener() {
                    public void changed(ChangeEvent event, Actor actor) {
                        mScript.mTexColor = selectBox.getSelectedIndex() + 1;
                    }
                });
                selectBox
                        .setItems("Maroon", "Black", "Blue", "Green", "Silver");

                selectBox.setSelected("Maroon");
                selectBox.setVisible(false);
                selectBox.setName("colorbutton");

                table.add(selectBox).height(120.0f).width(600.0f);

            } else {
                final CheckBox box = new CheckBox("Reset", skin);
                mCheckBox = box;
                box.setChecked(true);
                box.addListener(new ChangeListener() {
                    public void changed(ChangeEvent event, Actor actor) {
                        ((Slider) xSlider).setValue(0.0f);
                        mResetSlider = box.isChecked();
                    }
                });
                box.getLabel().setFontScale(mFontScale);
                box.getCells().get(0).size(80.0f, 80.0f);
                table.add(box);
            }

            table.add(button).height(120).width(450);

            Slider slider = null;
            if (i < 3) {
                slider = new Slider(0, 100, 1, false, skin);
                if (i == 0) {
                    slider.setName("X");
                    slider.setVisible(false);
                }
                if (i == 1) {
                    slider.setName("Y");
                    slider.setVisible(false);
                }
                if (i == 2) {
                    slider.setName("Z");
                    slider.setVisible(false);
                }
                ;
                slider.addListener(stopTouchDown); // Stops touchDown events
                                                   // from propagating to the
                                                   // FlickScrollPane.
                if (i == 0) {
                    Label l = new Label("Rotate X", skin);
                    table.add(l);
                    l.setVisible(false);
                }
                if (i == 1) {
                    Label l = new Label("Rotate Y", skin);
                    l.setVisible(false);
                    table.add(l);
                }
                if (i == 2) {
                    Label l2 = new Label("Rotate Z", skin);
                    table.add(l2);
                    l2.setVisible(false);
                }
                table.add(slider).height(120).width(500);
            }

        }

        table.row();
        table.add(new Label("", skin)).expandX().fillX();
        TextButton button = new TextButton("Look Inside", skin);
        button.getLabel().setFontScale(mFontScale);
        mLookInsideButton = button;
        button.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                mScript.mLookInside = true;
                mLookInsideButton.setChecked(false);
                mLookInsideButton.toggle();
            }
        });
        button.setVisible(false);
        button.setName("lookinsidebutton");
        table.add(button).height(120).width(450);
        table.row();

        Slider slider = null;

        slider = new Slider(0, 100, 1, false, skin);
        slider.setName("Zoom");
        slider.addListener(stopTouchDown);
        Label zoom = new Label("  Zoom  ", skin);
        zoom.setFontScale(mFontScale);
        table.pad(10).add(zoom); 
        table.add(slider).height(150.0f).width(800);
        final TextButton flickButton = new TextButton("Flick Scroll", skin.get(
                "toggle", TextButtonStyle.class));
        flickButton.setChecked(true);
        flickButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                scroll.setFlickScroll(flickButton.isChecked());
            }
        });
        final TextButton fadeButton = new TextButton("Fade Scrollbars",
                skin.get("toggle", TextButtonStyle.class));
        fadeButton.setChecked(true);
        fadeButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                scroll.setFadeScrollBars(fadeButton.isChecked());
            }
        });
        final TextButton smoothButton = new TextButton("Smooth Scrolling",
                skin.get("toggle", TextButtonStyle.class));
        smoothButton.setChecked(true);
        smoothButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                scroll.setSmoothScrolling(smoothButton.isChecked());
            }
        });
        final TextButton onTopButton = new TextButton("Scrollbars On Top",
                skin.get("toggle", TextButtonStyle.class));
        onTopButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                scroll.setScrollbarsOnTop(onTopButton.isChecked());
            }
        });
        mContainer.add(scroll).expand().fill().colspan(4);
        mContainer.row().space(10).padBottom(10);

    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mStage.act(Gdx.graphics.getDeltaTime());
        if (xSlider == null) {
            mColorButtonActor = mStage.getRoot().findActor("colorbutton");
            mLookInsideButtonActor = mStage.getRoot().findActor(
                    "lookinsidebutton");
            xSlider = mStage.getRoot().findActor("Zoom");
            ySlider = mStage.getRoot().findActor("Y");
            zSlider = mStage.getRoot().findActor("Z");
        }
        if (xSlider != null) {
            mX = ((Slider) xSlider).getValue();
            mY = ((Slider) ySlider).getValue();
            mZ = ((Slider) zSlider).getValue();
            mScript.mZoomLevel = (mX / 100.0f * 2.0f) - 2.0f;
            mScript.mRotateZ = (360.0f / 100.0f) * mZ;
        }
        if (mCheckBox.isChecked() && mX != 0)
            mCheckBox.setChecked(false);
        if (mResetSlider) {
            ((Slider) xSlider).setValue(0);
            ((Slider) ySlider).setValue(0);
            ((Slider) zSlider).setValue(0);
            mScript.mResetRotate = true;
            for (int i = 0; i < 5; i++)
                mScript.Objects[i].getTransform().setRotationByAxis(0.0f, 0.0f,
                        0.0f, 0.0f);
            mResetSlider = false;
        }
        if (mScript.ThumbnailSelected == 1 || mScript.ThumbnailSelected == 3) {
            ((SelectBox) mColorButtonActor).setVisible(true);

        } else
            ((SelectBox) mColorButtonActor).setVisible(false);
        if (mScript.ThumbnailSelected == 3)
            ((Button) mLookInsideButtonActor).setVisible(true);
        else
            ((Button) mLookInsideButtonActor).setVisible(false);
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
