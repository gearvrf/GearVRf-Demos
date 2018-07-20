importPackage(org.gearvrf)
importPackage(org.gearvrf.scene_objects)
importPackage(org.gearvrf.script)
importPackage(org.gearvrf.animation)

function onInit(gvrf) {
  var mainScene = gvrf.getMainScene();

  // 3D Boat Scene
  var boat = gvrf.getAssetLoader().loadModel("RowBoatFBX/RowBoatAnimated.fbx", mainScene);
  boat.setName("boat");
  boat.getTransform().setScale(.5, .5, .5);
  boat.getTransform().setRotationByAxis(20, 0, 1, 0);
  boat.getTransform().setPosition(20, -20, -40);

  // Text
  var textView = new GVRTextViewSceneObject(gvrf);
  textView.setText("GVRf scripting in Javascript");
  textView.setRefreshFrequency(GVRTextViewSceneObject.IntervalFrequency.REALTIME);
  textView.setName("text");
  textView.setTextSize(6);
  textView.getTransform().setPosition(0, 0, -100);
  textView.getTransform().setScale(50, 50, 50);

  var textSensor = new GVRSensor(gvrf);

  textView.attachComponent(textSensor);
  var script = new GVRScriptBehavior(gvrf, "text.js");
  textView.attachComponent(script);
  mainScene.addSceneObject(textView);

  // Animation
  var animation = boat.getComponent(GVRAnimator.getComponentType());
  if (animation)
  {
      animation.setRepeatMode(1);
      animation.setRepeatCount(-1);
      animation.start();
  }

  mainScene.setFrustumCulling(false);
}