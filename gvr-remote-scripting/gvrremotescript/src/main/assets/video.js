importPackage(org.gearvrf);
importPackage(org.gearvrf.scene_objects);

var sphere = new GVRSphereSceneObject(gvrf, false);
var mesh = sphere.getRenderData().getMesh();
var mediaPlayer = new org.gearvrf.utility.ImageUtils.createMediaPlayer("https://github.com/gearvrf/GearVRf-Demos/raw/master/gvr-360video/gvr360video/src/main/assets/videos_s_3.mp4");
var video = new GVRVideoSceneObject(gvrf, mesh, mediaPlayer, GVRVideoSceneObject.GVRVideoType.MONO );
video.setName("video");
var scene = gvrf.getMainScene();
scene.addSceneObject(video);

mediaPlayer.start();
