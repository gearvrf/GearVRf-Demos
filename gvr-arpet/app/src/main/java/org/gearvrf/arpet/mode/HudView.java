package org.gearvrf.arpet.mode;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.arpet.util.LayoutViewUtils;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.utility.Log;

public class HudView extends BasePetView implements View.OnClickListener {
    private static final String TAG = "HudView";

    private LinearLayout menuHud, menuButton, closeButton, playBoneButton, shareAnchorButton, cameraButton, editModeButton;
    private final GVRViewSceneObject mHudMenuObject;
    private final GVRViewSceneObject mStartMenuObject;
    private final GVRViewSceneObject mConnectedLabel;
    private OnHudItemClicked mListener;
    private Animation openAnimation;
    private Animation closeAnimation;

    public HudView(PetContext petContext) {
        super(petContext);
        mStartMenuObject = new GVRViewSceneObject(petContext.getGVRContext(), R.layout.hud_start_layout,
                new IViewEvents() {
                    @Override
                    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
                        menuButton = view.findViewById(R.id.btn_start_menu);
                        menuButton.setOnClickListener(HudView.this);
                    }

                    @Override
                    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
                        gvrViewSceneObject.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
                        gvrViewSceneObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
                        LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                                gvrViewSceneObject, 590f, 302f, 44f, 44f);
                        gvrViewSceneObject.getTransform().setPositionZ(
                                gvrViewSceneObject.getTransform().getPositionZ() - 0.001f);
                        addChildObject(gvrViewSceneObject);
                    }
                });

        mHudMenuObject = new GVRViewSceneObject(petContext.getGVRContext(), R.layout.hud_menus_layout,
                new IViewEvents() {
                    @Override
                    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
                        menuHud = view.findViewById(R.id.menuHud);
                        closeButton = view.findViewById(R.id.btn_close);
                        editModeButton = view.findViewById(R.id.btn_edit);
                        playBoneButton = view.findViewById(R.id.btn_fetchbone);
                        shareAnchorButton = view.findViewById(R.id.btn_shareanchor);
                        cameraButton = view.findViewById(R.id.btn_camera);
                        closeButton.setOnClickListener(HudView.this);
                        editModeButton.setOnClickListener(HudView.this);
                        playBoneButton.setOnClickListener(HudView.this);
                        shareAnchorButton.setOnClickListener(HudView.this);
                        cameraButton.setOnClickListener(HudView.this);
                        openAnimation = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.open);
                        closeAnimation = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.close);
                    }

                    @Override
                    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
                        gvrViewSceneObject.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
                        gvrViewSceneObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
                        LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                                gvrViewSceneObject, 590f, 20f, 44f, 314f);
                        gvrViewSceneObject.setEnable(false);
                        addChildObject(gvrViewSceneObject);
                    }
                });

        mListener = null;
        mHudMenuObject.getTransform().setPosition(0.95f, 0.0f,-1.6f);

        mConnectedLabel = new GVRViewSceneObject(petContext.getGVRContext(), R.layout.share_connected_layout);
        mConnectedLabel.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        mConnectedLabel.getTransform().setPosition(-1.7f, 0.95f,-3.6f);
        addChildObject(mConnectedLabel);
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mConnectedLabel.setEnable(mPetContext.getMode() != SharedMixedReality.OFF);
        mHudMenuObject.setEnable(mPetContext.getMode() != SharedMixedReality.GUEST);

        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    public void setListener(OnHudItemClicked listener) {
        mListener = listener;
    }

    @Override
    public void onClick(final View view) {
        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.btn_start_menu:
                menuButton.post(new Runnable() {
                    @Override
                    public void run() {
                        menuHud.startAnimation(openAnimation);
                        menuHud.setVisibility(View.VISIBLE);
                        mHudMenuObject.setEnable(true);
                    }
                });

                menuButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mStartMenuObject.setEnable(false);
                    }
                }, 100);
                break;
            case R.id.btn_close:
                closeButton.post(new Runnable() {
                    @Override
                    public void run() {
                        menuHud.startAnimation(closeAnimation);
                        menuHud.setVisibility(View.INVISIBLE);
                        menuHud.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mHudMenuObject.setEnable(false);
                            }
                        }, 500);
                    }
                });

                closeButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mStartMenuObject.setEnable(true);
                    }
                }, 100);

                break;
            case R.id.btn_edit:
                mPetContext.getGVRContext().runOnGlThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onEditModeClicked();
                    }
                });
                break;
            case R.id.btn_fetchbone:
                playBoneButton.post(new Runnable() {
                    @Override
                    public void run() {
                        menuHud.startAnimation(closeAnimation);
                        menuHud.setVisibility(View.INVISIBLE);
                        closeButton.setVisibility(View.INVISIBLE);
                        menuButton.setVisibility(View.VISIBLE);
                        playBoneButton.setBackgroundResource(R.drawable.bg_button_ball);
                    }
                });
                mPetContext.getGVRContext().runOnGlThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onBallClicked();
                    }
                });
                break;
            case R.id.btn_shareanchor:
                mPetContext.getGVRContext().runOnGlThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onShareAnchorClicked();
                    }
                });
                break;
            case R.id.btn_camera:
                mPetContext.getGVRContext().runOnGlThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onCameraClicked();
                    }
                });
            default:
                Log.d(TAG, "Invalid Option");
        }
    }
}
