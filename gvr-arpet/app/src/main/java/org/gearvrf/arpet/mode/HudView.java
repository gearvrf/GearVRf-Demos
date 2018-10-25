package org.gearvrf.arpet.mode;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.connection.socket.ConnectionMode;
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.arpet.util.LayoutViewUtils;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.utility.Log;

public class HudView extends BasePetView implements View.OnClickListener {
    private static final String TAG = "HudView";

    private LinearLayout rootLayout, menuHud, menuButton, closeButton, playBoneButton, shareAnchorButton, cameraButton, editModeButton;
    private final GVRViewSceneObject mHudMenuObject;
    private final GVRViewSceneObject mStartMenuObject;
    private final GVRViewSceneObject mConnectedLabel;
    private final GVRViewSceneObject mDisconnectViewObject;
    private Button connectedButton, cancelButton, disconnectButton;
    private TextView disconnectViewMessage;
    private OnHudItemClicked mListener;
    private OnDisconnectClicked mDisconnectListener;
    private OnClickDisconnectViewHandler mDisconnectViewHandler;
    private Animation openAnimation;
    private Animation closeAnimation;

    public HudView(PetContext petContext) {
        super(petContext);

        // Create a root layout to set the display metrics on it
        rootLayout = new LinearLayout(petContext.getActivity());
        final DisplayMetrics metrics = new DisplayMetrics();
        petContext.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels));

        View.inflate(petContext.getActivity(), R.layout.view_disconnect_sharing, rootLayout);

        mListener = null;
        mDisconnectListener = null;
        mStartMenuObject = new GVRViewSceneObject(petContext.getGVRContext(),
                R.layout.hud_start_layout, startMenuInitEvents);
        mHudMenuObject = new GVRViewSceneObject(petContext.getGVRContext(),
                R.layout.hud_menus_layout, hudMenuInitEvents);
        mConnectedLabel = new GVRViewSceneObject(petContext.getGVRContext(),
                R.layout.share_connected_layout, connectButtonInitEvents);
        mDisconnectViewObject = new GVRViewSceneObject(petContext.getGVRContext(), rootLayout);
        rootLayout.post(new Runnable() {
            @Override
            public void run() {
                disconnectViewInitEvents.onInitView(mDisconnectViewObject, rootLayout);
                disconnectViewInitEvents.onStartRendering(mDisconnectViewObject, rootLayout);
            }
        });
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mConnectedLabel.setEnable(mPetContext.getMode() != SharedMixedReality.OFF);
        mHudMenuObject.setEnable(mPetContext.getMode() != SharedMixedReality.GUEST);

        mainScene.getMainCameraRig().addChildObject(this);
    }

    public void hideDisconnectView() {
        mDisconnectViewObject.setEnable(false);
    }

    public void showDisconnectView(@ConnectionMode int mode) {
        if (mode == ConnectionMode.SERVER) {
            disconnectViewMessage.setText(R.string.disconnect_host);
        } else {
            disconnectViewMessage.setText(R.string.disconnect_guest);
        }
        mDisconnectViewObject.setEnable(true);
    }

    public void hideConnectedLabel() {
        mConnectedLabel.setEnable(false);
    }

    public void showConnectedLabel() {
        mConnectedLabel.setEnable(true);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    public void setListener(OnHudItemClicked listener) {
        mListener = listener;
    }

    public void setDisconnectListener(OnDisconnectClicked listener) {
        mDisconnectListener = listener;
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
                menuButton.setVisibility(View.GONE);
                closeButton.setVisibility(View.VISIBLE);

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

                menuButton.setVisibility(View.VISIBLE);
                closeButton.setVisibility(View.GONE);

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
                        closeButton.setVisibility(View.GONE);
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
                break;
            case R.id.btn_connected:
                mPetContext.getGVRContext().runOnGlThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onConnectedClicked();
                    }
                });
                break;
            default:
                Log.d(TAG, "Invalid Option");
        }
    }

    IViewEvents hudMenuInitEvents = new IViewEvents() {
        @Override
        public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
            menuHud = view.findViewById(R.id.menuHud);
            editModeButton = view.findViewById(R.id.btn_edit);
            playBoneButton = view.findViewById(R.id.btn_fetchbone);
            shareAnchorButton = view.findViewById(R.id.btn_shareanchor);
            cameraButton = view.findViewById(R.id.btn_camera);
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
                    gvrViewSceneObject, 590f, 20f, 44f, 270f);
            gvrViewSceneObject.setEnable(false);
            addChildObject(gvrViewSceneObject);
        }
    };

    IViewEvents startMenuInitEvents = new IViewEvents() {
        @Override
        public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
            menuButton = view.findViewById(R.id.btn_start_menu);
            closeButton = view.findViewById(R.id.btn_close);
            menuButton.setOnClickListener(HudView.this);
            closeButton.setOnClickListener(HudView.this);
        }

        @Override
        public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
            gvrViewSceneObject.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
            gvrViewSceneObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
            LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                    gvrViewSceneObject, 590f, 304f, 44f, 44f);
            addChildObject(gvrViewSceneObject);
        }
    };

    IViewEvents connectButtonInitEvents = new IViewEvents() {
        @Override
        public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
            connectedButton = view.findViewById(R.id.btn_connected);
            connectedButton.setOnClickListener(HudView.this);
        }

        @Override
        public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
            gvrViewSceneObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
            LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                    gvrViewSceneObject, 4.0f, 4.0f, 144.0f, 44.0f);
            addChildObject(gvrViewSceneObject);
        }
    };

    IViewEvents disconnectViewInitEvents = new IViewEvents() {
        @Override
        public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
            disconnectViewMessage = view.findViewById(R.id.disconnect_message_text);
            cancelButton = view.findViewById(R.id.button_cancel);
            disconnectButton = view.findViewById(R.id.button_disconnect);
            mDisconnectViewHandler = new OnClickDisconnectViewHandler();
            cancelButton.setOnClickListener(mDisconnectViewHandler);
            disconnectButton.setOnClickListener(mDisconnectViewHandler);
        }

        @Override
        public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
            gvrViewSceneObject.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
            gvrViewSceneObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
            gvrViewSceneObject.getTransform().setPosition(0.0f, 0.0f, -0.74f);
            gvrViewSceneObject.setEnable(false);
            addChildObject(gvrViewSceneObject);
        }
    };

    private class OnClickDisconnectViewHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mDisconnectListener == null) {
                return;
            }

            switch (v.getId()) {
                case R.id.button_cancel:
                    mDisconnectListener.onCancel();
                    break;
                case R.id.button_disconnect:
                    mDisconnectListener.onDisconnect();
                    break;
                default:
                    Log.d(TAG, "invalid ID in disconnect view handler");
            }
        }
    }
}
