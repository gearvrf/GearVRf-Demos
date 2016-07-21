importPackage(org.gearvrf);
importPackage(org.gearvrf.scene_objects);

function onInit(context)
{
	var scene = context.getMainScene();
	scene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
	scene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

	var trackerURL = new java.net.URL("https://raw.githubusercontent.com/gearvrf/GearVRf-Demos/master/gvr-tutorial-lesson2/app/src/main/drawable-xxxhdpi/headtrackingpointer.jpg");
	var trackerTex = new GVRAndroidResource(context, trackerURL);
	var headTracker = new GVRSceneObject(context, context.createQuad(0.1, 0.1), trackerTex);
	headTracker.getTransform().setPosition(0.0, 0.0, -1.0);
	headTracker.getRenderData().setDepthTest(false);
	headTracker.getRenderData().setRenderingOrder(100000);
	scene.getMainCameraRig().addChildObject(headTracker);

	var balloon = makeBalloon(context);
	scene.addSceneObject(balloon);
	var environment = makeEnvironment(context);
	scene.addSceneObject(environment);
}
    

function makeBalloon(context) {
	var sphere = new GVRSphereSceneObject(context, true);
	var rdata = sphere.getRenderData();
	var mtl = new GVRMaterial(context);
	mtl.setDiffuseColor(1.0, 0.0, 1.0, 0.5);
	sphere.setName("balloon");
	rdata.setShaderTemplate(GVRPhongShader.class);
	rdata.setAlphaBlend(true);
	rdata.setMaterial(mtl);
	rdata.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
	sphere.getTransform().setPositionZ(-3.0f);
	return sphere;
}

function makeEnvironment(context)
{
	var tex = context.loadFutureCubemapTexture(new GVRAndroidResource(context, R.raw.lycksele3));
	var material = new GVRMaterial(context, GVRMaterial.GVRShaderType.Cubemap.ID);
	material.setMainTexture(tex);
	var environment = new GVRSphereSceneObject(context, 18, 36, false, material, 4, 4);
	environment.getTransform().setScale(20.0f, 20.0f, 20.0f);

	var sunLight = new GVRDirectLight(context);
	sunLight.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
	sunLight.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
	environment.attachComponent(sunLight);
	return environment;
}

}
