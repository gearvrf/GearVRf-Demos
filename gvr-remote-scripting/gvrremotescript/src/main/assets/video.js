importPackage(org.gearvrf);
importPackage(org.gearvrf.scene_objects);

var sphere = new GVRSphereSceneObject(gvrf, false);
var mesh = sphere.getRenderData().getMesh();
var mediaPlayer = new org.gearvrf.utility.ImageUtils.createMediaPlayer("https://raw.githubusercontent.com/gearvrf/GearVRf-Demos/master/gvrf_minimal360video/assets/videos_s_3.mp4");
var video = new GVRVideoSceneObject(gvrf, mesh, mediaPlayer, GVRVideoSceneObject.GVRVideoType.MONO );
var scene = gvrf.getMainScene();
scene.addSceneObject(video);

mediaPlayer.start();
