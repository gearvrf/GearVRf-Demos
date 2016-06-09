importPackage(org.gearvrf);
importPackage(org.gearvrf.scene_objects);

var url = new java.net.URL("https://raw.githubusercontent.com/gearvrf/GearVRf-Demos/master/gvr-360photo/gvr360photo/src/main/res/raw/photosphere.jpg");
var resource = new GVRAndroidResource(gvrf, url);
var texture = gvrf.loadFutureTexture(resource);
var photosphere = new GVRSphereSceneObject(gvrf, false, texture);
photosphere.getTransform().setScale(20, 20, 20);
photosphere.setName("photosphere");
var scene = gvrf.getMainScene();
scene.addSceneObject(photosphere);

