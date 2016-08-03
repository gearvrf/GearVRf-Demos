package com.gearvrf.fasteater;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by siva.penke on 7/31/2016.
 */
public class ImageFrameAnimation extends GVRAnimation {
    private final List<Future<GVRTexture>> animationTextures;
    private int lastFileIndex = -1;

    /**
     * @param material             {@link GVRMaterial} to animate
     * @param duration             The animation duration, in seconds.
     * @param texturesForAnimation arrayList of GVRTexture used during animation
     */
    public ImageFrameAnimation(GVRMaterial material, float duration,
                                  final List<Future<GVRTexture>> texturesForAnimation) {
        super(material, duration);
        animationTextures = texturesForAnimation;
    }

    @Override
    protected void animate(GVRHybridObject target, float ratio) {
        final int size = animationTextures.size();
        final int fileIndex = (int) (ratio * size);

        if (lastFileIndex == fileIndex || fileIndex == size) {
            return;
        }

        lastFileIndex = fileIndex;

        GVRMaterial material = (GVRMaterial) target;
        material.setMainTexture(animationTextures.get(fileIndex));
    }
}
