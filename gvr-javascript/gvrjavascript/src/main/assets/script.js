importPackage(org.gearvrf)
importPackage(org.gearvrf.scene_objects)

function onInit(gvrf) {
  var mainScene = gvrf.getNextMainScene();

  // 3D Boat Scene
  var boat = gvrf.loadModel("RowBoatFBX/RowBoatAnimated.fbx");
  boat.setName("boat");
  boat.getTransform().setScale(.5, .5, .5);
  boat.getTransform().setRotationByAxis(20, 0, 1, 0);
  boat.getTransform().setPosition(20, -20, -40);
  mainScene.addSceneObject(boat);

  // Text
  var textView = new GVRTextViewSceneObject(gvrf);
  textView.setText("GVRf scripting in Javascript");
  textView.setRefreshFrequency(GVRTextViewSceneObject.IntervalFrequency.REALTIME);
  textView.setName("text");
  textView.setTextSize(6);
  textView.getTransform().setPosition(0, 0, -100);
  textView.getTransform().setScale(50, 50, 50);

  var textSensor = new GVRBaseSensor(gvrf);
  textView.setSensor(textSensor);

  mainScene.addSceneObject(textView);

  // Animation
  var animationEngine = gvrf.getAnimationEngine();
  var animations = boat.getAnimations();
  if (animations.size() != 0) {
      animations.get(0).setRepeatMode(1).setRepeatCount(-1);
      animations.get(0).start(animationEngine);
  }

  mainScene.setFrustumCulling(false);
}

/*
function onStep() {
}
*/