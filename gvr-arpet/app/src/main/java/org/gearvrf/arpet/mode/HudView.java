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
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.utility.Log;

public class HudView extends BasePetView implements View.OnClickListener, IViewEvents {
    private static final String TAG = "HudView";

    private LinearLayout MenuHud, menuButton, closeButton, playBoneButton, shareAnchorButton, cameraButton, editModeButton;
    private GVRViewSceneObject mHudMenu;
    private OnHudItemClicked mListener;
    private Animation openAnimation;
    private Animation closeAnimation;

    public HudView(PetContext petContext) {
        super(petContext);
        mHudMenu = new GVRViewSceneObject(petContext.getGVRContext(), R.layout.hud_layout, this);
        mHudMenu.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        mListener = null;
        getTransform().setPosition(0.95f, 0.0f,-1.6f);
    }

    @Override
    protected void onShow(GVRScene mainScene) {
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
            case R.id.btn_menu:
                menuButton.post(new Runnable() {
                    @Override
                    public void run() {
                        MenuHud.startAnimation(openAnimation);
                        MenuHud.setVisibility(View.VISIBLE);
                        closeButton.setVisibility(View.VISIBLE);
                        menuButton.setVisibility(View.GONE);

                    }
                });
                break;
            case R.id.btn_close:
                closeButton.post(new Runnable() {
                    @Override
                    public void run() {
                        MenuHud.startAnimation(closeAnimation);
                        MenuHud.setVisibility(View.INVISIBLE);
                        closeButton.setVisibility(View.INVISIBLE);
                        menuButton.setVisibility(View.VISIBLE);

                    }
                });
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
                        MenuHud.startAnimation(closeAnimation);
                        MenuHud.setVisibility(View.INVISIBLE);
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

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        MenuHud = view.findViewById(R.id.menuHud);
        menuButton = view.findViewById(R.id.btn_menu);
        closeButton = view.findViewById(R.id.btn_close);
        editModeButton = view.findViewById(R.id.btn_edit);
        playBoneButton = view.findViewById(R.id.btn_fetchbone);
        shareAnchorButton = view.findViewById(R.id.btn_shareanchor);
        cameraButton = view.findViewById(R.id.btn_camera);
        menuButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        editModeButton.setOnClickListener(this);
        playBoneButton.setOnClickListener(this);
        shareAnchorButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        openAnimation = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.open);
        closeAnimation = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.close);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        gvrViewSceneObject.setTextureBufferSize(ApiConstants.TEXTURE_BUFFER_SIZE);
        addChildObject(gvrViewSceneObject);
    }

}
