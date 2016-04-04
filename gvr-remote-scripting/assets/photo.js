importPackage(org.gearvrf);
importPackage(org.gearvrf.scene_objects);

var url = new java.net.URL("https://upload.wikimedia.org/wikipedia/commons/8/89/Langkawi_Sky_Bridge_Photosphere.jpg");
var resource = new GVRAndroidResource(gvrf, url);
var texture = gvrf.loadFutureTexture(resource);
var sceneobj = new GVRSphereSceneObject(gvrf, false, texture);
var scene = gvrf.getMainScene();
scene.addSceneObject(sceneobj);

