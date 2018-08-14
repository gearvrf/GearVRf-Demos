package org.gearvrf.arpet.mode;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.OnHudItemClicked;
import org.gearvrf.arpet.R;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.utility.Log;

public class HudScene extends GVRSceneObject implements View.OnClickListener, IViewEvents {
    private GVRContext mContext;
    private LinearLayout MenuHud;
    private Button menuButton, closeButton, editModeButton;
    private GVRViewSceneObject mHudMenu;
    private OnHudItemClicked mListener;

    public HudScene(GVRContext gvrContext, OnHudItemClicked listener) {
        super(gvrContext);
        mContext = gvrContext;
        mListener = listener;
        mHudMenu = new GVRViewSceneObject(mContext, R.layout.hud_layout, this);

    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.btn_menu:
                menuButton.post(new Runnable() {
                    @Override
                    public void run() {
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
                        MenuHud.setVisibility(View.INVISIBLE);
                        closeButton.setVisibility(View.INVISIBLE);
                        menuButton.setVisibility(View.VISIBLE);

                    }
                });
                break;
            case R.id.btn_edit:
                editModeButton.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onEditModeClicked();
                    }
                });
                break;
            default:
                Log.d("XX", "Invalid Option");
        }
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        MenuHud = view.findViewById(R.id.menuHud);
        menuButton = view.findViewById(R.id.btn_menu);
        closeButton = view.findViewById(R.id.btn_close);
        editModeButton = view.findViewById(R.id.btn_edit);
        menuButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        editModeButton.setOnClickListener(this);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        gvrViewSceneObject.getTransform().setScale(3.2f, 3.2f, 1.0f);
        addChildObject(mHudMenu);
    }

}
