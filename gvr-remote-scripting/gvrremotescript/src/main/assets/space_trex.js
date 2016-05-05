importPackage(org.gearvrf);
importPackage(org.gearvrf.scene_objects);

// get a handle to the scene
var scene = gvrf.getMainScene();

// space background
var space_url = new java.net.URL("https://github.com/gearvrf/GearVRf-Demos/raw/master/gvr-remote-scripting/gvrremotescript/src/main/assets/space.jpg");
var space_resource = new GVRAndroidResource(gvrf, space_url);
var space_texture = gvrf.loadFutureTexture(space_resource);
var space_photosphere = new GVRSphereSceneObject(gvrf, false, space_texture);
space_photosphere.getTransform().setScale(20, 20, 20);
space_photosphere.setName("space_photosphere");
scene.addSceneObject(space_photosphere);

// space platform
var platform_url = new java.net.URL("https://github.com/gearvrf/GearVRf-Demos/raw/master/gvr-remote-scripting/gvrremotescript/src/main/assets/platform.fbx");
var platform = gvrf.loadModelFromURL(platform_url);
platform.getTransform().setPosition(0, -2, -10);
platform.setName("platform");
scene.addSceneObject(platform);

// space trex
var trex_url = new java.net.URL("https://github.com/gearvrf/GearVRf-Demos/raw/master/gvr-meshanimation/gvrmeshanimation/src/main/assets/TRex_NoGround.fbx");
var trex = gvrf.loadModelFromURL(trex_url);
trex.setName("trex");
scene.addSceneObject(trex);

// done adding objects, bind shaders
scene.bindShaders();

// place trex
trex.getTransform().setPosition(0.0, -2.0, -10.0);
trex.getTransform().setRotationByAxis(90.0, 1.0, 0.0, 0.0);
trex.getTransform().setRotationByAxis(0.0, 0.0, 1.0, 0.0);
trex.getTransform().setScale(1, 1, 1);

// animate trex
var engine = gvrf.getAnimationEngine();
var animation = trex.getAnimations().get(0);
animation.setRepeatMode(1).setRepeatCount(-1);
animation.start(engine);


