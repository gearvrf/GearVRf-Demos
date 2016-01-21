function onInit(gvrf) {
  var mainScene = gvrf.getNextMainScene();

  // 3D model
  var model = gvrf.loadModel("astro_boy.dae");
  model.getTransform().setRotationByAxis(45.0, 0.0, 1.0, 0.0);
  model.getTransform().setScale(3, 3, 3);
  model.getTransform().setPosition(0.0, -0.4, -0.5);

  mainScene.addSceneObject(model);

  // Text
  var textView = utils.newTextViewSceneObject(gvrf, "GVRf scripting in Javascript");
  var textSize = textView.getTextSize();
  textView.setTextSize(textSize);
  textView.getTransform().setPosition(0, 0, -2);

  mainScene.addSceneObject(textView);

  // Animation
  var animationEngine = gvrf.getAnimationEngine();
  var animations = utils.getAnimations(model);
  if (animations.length != 0) {
      animations[0].setRepeatMode(1).setRepeatCount(-1);
      animations[0].start(animationEngine);
  }
}

function onStep() {
}
