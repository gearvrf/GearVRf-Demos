package org.gearvrf.arpet;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.arpet.mode.BasePetView;
import org.gearvrf.arpet.mode.OnClickExitScreen;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class ExitApplicationScreen extends BasePetView implements View.OnClickListener {
    private GVRViewSceneObject mExitApplicationScreen;
    private OnClickExitScreen mClickExitScreenListener;

    public ExitApplicationScreen(PetContext context) {
        super(context);

        onInit();
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    public void setClickExitScreenListener(OnClickExitScreen Listener) {
        mClickExitScreenListener = Listener;
    }

    private void onInit() {
        mClickExitScreenListener = null;
        final DisplayMetrics metrics = new DisplayMetrics();
        mPetContext.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        ViewGroup view = (ViewGroup) View.inflate(mPetContext.getActivity(), R.layout.screen_exit_application, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(metrics.widthPixels, metrics.heightPixels));
        mExitApplicationScreen = new GVRViewSceneObject(mPetContext.getGVRContext(), view);
        mExitApplicationScreen.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
        mExitApplicationScreen.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        mExitApplicationScreen.getTransform().setPosition(0f, 0f, -0.74f);
        addChildObject(mExitApplicationScreen);
        view.findViewById(R.id.cancel_button_screen).setOnClickListener(this);
        view.findViewById(R.id.button_confirm).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mClickExitScreenListener == null) {
            return;
        }

        if (view.getId() == R.id.cancel_button_screen) {
            mClickExitScreenListener.OnCancel();
        } else {
            mClickExitScreenListener.OnConfirm();
        }
    }
}
