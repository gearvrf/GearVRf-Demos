importPackage(org.gearvrf);
importPackage(org.gearvrf.scene_objects);

sphere = new GVRSphereSceneObject(gvrf, false);
mesh = sphere.getRenderData().getMesh();
mediaPlayer = new org.gearvrf.utility.ImageUtils.createMediaPlayer("https://raw.githubusercontent.com/gearvrf/GearVRf-Demos/master/gvrf_minimal360video/assets/videos_s_3.mp4")
video = new GVRVideoSceneObject(gvrf, mesh, mediaPlayer, GVRVideoSceneObject.GVRVideoType.MONO );
scene = gvrf.getMainScene()
scene.addSceneObject(video)

mediaPlayer.start()
