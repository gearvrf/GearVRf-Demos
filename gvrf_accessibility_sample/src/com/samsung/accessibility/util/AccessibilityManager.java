package com.samsung.accessibility.util;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.accessibility.GVRAccessibilityCaptions;
import org.gearvrf.accessibility.GVRAccessibilityInvertedColors;
import org.gearvrf.accessibility.GVRAccessibilitySpeechRecognition;
import org.gearvrf.accessibility.GVRAccessibilityTalkBack;
import org.gearvrf.accessibility.GVRAccessibilityZoom;

public class AccessibilityManager {

    private List<GVRAccessibilityTalkBack> mTalkBacks;
    private GVRAccessibilitySpeechRecognition mSpeechRecognition;
    private GVRAccessibilityInvertedColors mInvertedColors;
    private GVRAccessibilityCaptions mCaptions;
    private GVRAccessibilityZoom mZoom;

    public AccessibilityManager(GVRContext gvrContext) {
        mTalkBacks = new ArrayList<GVRAccessibilityTalkBack>();
        mSpeechRecognition = new GVRAccessibilitySpeechRecognition();
        mInvertedColors = new GVRAccessibilityInvertedColors(gvrContext);
        mCaptions = GVRAccessibilityCaptions.getInstance(gvrContext);
        mZoom = new GVRAccessibilityZoom();
    }

    public List<GVRAccessibilityTalkBack> getTalkBack() {
        return mTalkBacks;
    }

    public GVRAccessibilitySpeechRecognition getSpeechRecognition() {
        return mSpeechRecognition;
    }

    public GVRAccessibilityInvertedColors getInvertedColors() {
        return mInvertedColors;
    }

    public GVRAccessibilityCaptions getCaptions() {
        return mCaptions;
    }

    public GVRAccessibilityZoom getZoom() {
        return mZoom;
    }

}
