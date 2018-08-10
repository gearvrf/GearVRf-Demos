package org.gearvrf.arpet;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class Hud extends GVRSceneObject implements View.OnClickListener, IViewEvents{
    private GVRContext mContext;
    private LinearLayout MenuHud;
    private Button OptionsMenu;
    private GVRViewSceneObject mHudMenu;
    private OnHudItemClicked mListener;

    public Hud(GVRContext gvrContext) {
        super(gvrContext);
        mContext = gvrContext;
        mHudMenu = new GVRViewSceneObject(mContext, R.layout.hud_layout, this);

    }

    @Override
    public void onClick(final View view) {
        final Drawable btn_close = view.getResources().getDrawable(R.drawable.btn_close);
        if (view.getId() == R.id.OptionsMenu){
             OptionsMenu.post(new Runnable() {
                 @Override
                 public void run() {
                    OptionsMenu.setBackground(btn_close);
                    MenuHud.setVisibility(View.VISIBLE);
                 }
             });
        }
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        MenuHud = view.findViewById(R.id.menuHud);
        OptionsMenu = view.findViewById(R.id.OptionsMenu);
        OptionsMenu.setOnClickListener(this);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        gvrViewSceneObject.getTransform().setScale(3.2f,3.2f,1.0f);
        addChildObject(mHudMenu);
    }

    public void registerListener(OnHudItemClicked listener){
        mListener = listener;
        mListener.onBallClicked();
    }
}
