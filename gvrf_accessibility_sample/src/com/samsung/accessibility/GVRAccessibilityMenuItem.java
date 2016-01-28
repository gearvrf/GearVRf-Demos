package com.samsung.accessibility;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

final class GVRAccessibilityMenuItem extends GVRAccessibilityInteractiveObject {

	private GVRContext mGvrContext;
	public GVRSceneObject backIcon;

	public GVRAccessibilityMenuItem(GVRContext gvrContext, GVRTexture iconMenu) {
		super(gvrContext);
		mGvrContext = gvrContext;
		GVRMesh mSlotMesh = gvrContext.loadMesh(new GVRAndroidResource(
				gvrContext, R.raw.circle_menu));
		GVRTexture mSpacerTexture = gvrContext
				.loadTexture(new GVRAndroidResource(gvrContext,
						R.drawable.circle_normal_alpha));
		GVRMaterial material = new GVRMaterial(gvrContext);
		GVRRenderData renderData = new GVRRenderData(gvrContext);
		renderData.setMaterial(material);
		renderData.setMesh(mSlotMesh);
		attachRenderData(renderData);
		this.getRenderData().getMaterial().setMainTexture(mSpacerTexture);
		createIcon(iconMenu);
	}

	private void createIcon(GVRTexture iconMenu) {
		backIcon = new GVRSceneObject(mGvrContext, mGvrContext.createQuad(.35f,
				.14f), iconMenu);
		backIcon.getTransform().setPosition(-0f, 0.02f, -0.7f);
		backIcon.getTransform().rotateByAxis(-90, 1, 0, 0);
		backIcon.getTransform().rotateByAxisWithPivot(245, 0, 1, 0, 0, 0, 0);
		backIcon.getRenderData().setRenderingOrder(GVRRenderingOrder.OVERLAY);

		addChildObject(backIcon);
	}

}
