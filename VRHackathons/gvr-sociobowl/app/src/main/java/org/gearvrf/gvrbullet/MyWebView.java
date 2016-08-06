package org.gearvrf.gvrbullet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by m on 7/30/16.
 */
public class MyWebView extends WebView {

    public boolean _isTouchable = false;

    public MyWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyWebView(Context context) {
        super(context);
    }

    public void isTouchable(boolean val) {
        this._isTouchable = val;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(this._isTouchable){
            return super.onTouchEvent(event);
        }
        return false;
    }

}
