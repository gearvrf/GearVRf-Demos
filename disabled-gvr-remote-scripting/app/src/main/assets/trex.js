importPackage(org.gearvrf);

var scene = gvrf.getMainScene();

var trex = gvrf.getAssetLoader().loadModel("https://github.com/gearvrf/GearVRf-Demos/raw/master/gvr-meshanimation/app/src/main/assets/TRex_NoGround.fbx");
scene.addSceneObject(trex);
scene.bindShaders();

trex.getTransform().setPosition(0.0, -10.0, -10.0);
trex.getTransform().setRotationByAxis(90.0, 1.0, 0.0, 0.0);
trex.getTransform().setRotationByAxis(40.0, 0.0, 1.0, 0.0);
trex.getTransform().setScale(1.5, 1.5, 1.5);
trex.setName("trex");

var engine = gvrf.getAnimationEngine();
var animation = trex.getAnimations().get(0);
animation.setRepeatMode(1).setRepeatCount(-1);
animation.start(engine);

