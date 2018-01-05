# GearVRf-Demos

## How to run these examples
* make sure you have [this repository](https://github.com/gearvrf/GearVRf-Demos) and [the GearVR framework](https://github.com/Samsung/GearVRf) in a sibling directory 

```
git clone https://github.com/Samsung/GearVRf.git -b release_v3.1
git clone https://github.com/gearvrf/GearVRf-Demos.git -b release_v3.1
```

There are two options:
- Clone the 3.2 branch: ``git clone -b release_v3.2 git@github.com:gearvrf/GearVRf-Demos.git``
- Download a zip archive: open https://github.com/gearvrf/GearVRf-Demos/tree/release_v3.2 and then select "Download ZIP"

* [download the Oculus_Mobile_SDK](https://developer3.oculus.com/downloads/mobile/1.0.4/Oculus_Mobile_SDK/) and put the SDK folder at the same directory level as your GearVRf directory.

```
#edit to use your PATH or make a env variable reference for wherever you unzipped the SDK
export OVR_MOBILE_SDK=~/mytools/ovr_sdk_mobile_1.0.4

```


* make sure you have gradle in your PATH (you can check that your PATH is setup correctly and verify your gradle version by running: `gradle -v` if you get a response, great! if not, [these instructions may help](https://developer.nvidia.com/codeworks-android)

* set up the [Oculus device signature](https://developer.oculus.com/osig/) for your device (it will look like oculussig_xxxxxxxx where xxxxxxxx is the id you get when you run `adb devices`) and copy it into an assets folder each project's `src/main/assets` directory: 

```
cp ~/Downloads/oculussig_xxxxxxxx ./gvr-cubemap/gvrcubemap/src/main/assets/oculussig_xxxxxxxx
```

* change to the example you wish to build and invoke the included gradle wrapper file, `./gradlew` 

```
cd gvr-sample
./gradlew assembleDebug
```

* you should now have an apk that you can push to an android device

```
adb install -r ./app/build/outputs/apk/app-debug.apk
```
NOTE: some of these examples use the application name instead of `app` to hold the apk build directory for instance, in gvr-cubemap, you will find the apk in `./gvr-cubemap/gvrcubemap/build/outputs/apk/app-debug.apk`

```
cd gvr-cubemap
./gradlew assembleDebug
adb install -r ./gvrcubemap/build/outputs/apk/app-debug.apk
```

* if you want to run without loading into a headset, enable Samsung VR Service developer mode: 
	- go to Settings > Applications > manage applications > Gear VR Service > Manage Storage
	- press the VR Service Version 6 times
	- if you get a message 'You are a developer' you should see a toggle to enable developer mode
	- if you get a message 'You are not a developer' you probably haven't installed a valid apk with your oculus signature - run the `adb install -r ./gvrcubemap/build/outputs/apk/app-debug.apk` command on at least one project, then the service should discover you
	 
	
### gvr-360photo
A minimal sample showing how to display an equirectangular (360) photo.

### gvr-360video
A minimal sample showing how to display an equirectangular (360) video using either Android's MediaPlayer class or the ExoPlayer class.

### gvr-3dcursor-simple
A simplified version of the gvr-3dcursor sample that shows how to use the 3DCursor plugin.

### gvr-3dcursor
Shows how to use the 3DCursor plugin.

### gvr-accessibility
Shows how to use GearVRf's accessibility classes.  For example: InvertedColors, TextToSpeech, and Zoom.

### gvr-bullet
Simple sample showing how to use GearVRf with the Physics plugin.

### gvr-camera2renderscript
Simple sample showing how to use the camera2 api along with renderscript for use with the passthrough camera.

### gvr-cardboard-audio
Simple example of Spatial Audio using GoogleVR's audio library (previously used cardboard's audio library).

### gvr-complexscene
A simple sample which can contain as many Stanford bunnies as we want to make it complex

### gvr-controls
A nice demo that shows input from both the gamepad and touchpad to control a character.

### gvr-cubemap
A simple example to show how to load in a cubemap and use it for the background as well as a reflection on an object.

### gvr-events
An example showing how to display Android Views inside VR and route events to those views.

### gvr-eyepicking
A simple picking example.

### gvr-gamepad
A minimal example showing how to receive input from a gamepad.

### gvr-immersivepedia
A larger sample that shows a concept of an immersive virtual museum.  Uses many features of GearVRf:  picking, TextViews, Video, input, etc.

### gvr-javascript
A minimal example showing how an application can be written with Javascript.

### gvr-keyboard
A sample that shows how to create a virtual keyboard, including voice input, and use it in a simple trivia game.

### gvr-lua
A minimal example showing how an application can be written with Lua.

### gvr-meshanimation
A simple sample that loads in an animated model and starts the animation.

### gvr-modelviewer2
A viewer that allows you to select and display models stored in /sdcard/GVRModelViewer2/.  You can look at the model from different angles, change lighting, look at it in wireframe, and toggle animations.  Uses the libGDX plugin for UI.

### gvr-multilight
A simple sample showing how to use multiple lights.

### gvr-outline
A sample showing how to use multiple render passes with the same geometry to show an outline.

### gvr-polyline
A sample showing how to draw lines.

### gvr-remote-scripting
The remote scripting sample enables the debug server and sets up a text object with the ipaddress of the phone so we know where to telnet into.

### gvr-renderableview
Inflates and displays some Android views onto a rotating cube.

### gvr-sceneobjects
Shows how create the various scene object types:  quad, cube, sphere, cylinder, cone, passthrough camera, text, video.  Tap the touchpad to cycle through the objects.

### gvr-shadows
A sample that shows a light source with shadowing.

### gvr-simplesample
A simple sample that creates a quad and applies a texture to it.  

### gvr-solarsystem
A sample that shows both heirarchy and animation.

### gvr-switch
A sample that shows how to use the GVRSwitch node.

### gvr-tutorial-lesson2
Shows initial scene setup and object creation.

### gvr-tutorial-lesson3
Shows event handling and picking.

### gvr-tutorial-lesson4
Shows creating and using components.  In this tutorial, a simple particle system is created.

### gvr-tutorial-lesson5
Shows how to integrate Sound and text.

### gvr-tutorial-lesson6
Shows loading and using model assets.

### gvr-video
A movie theater sample.  Plays back a video, has playback controls and has two theaters to choose from.

### gvr-vuforia
A simple augmented reality sample using the Vuforia computer vision library.  It looks for a marker and displays a teapot on top of it.  You can use either the stone or chips markers.  PDFs for the markers are in gvr-vuforia/app/src/main/.

### gvr-x3d-demo
Loads and displays an X3D model.


[Build tips](https://github.com/gearvrf/GearVRf-Demos/wiki)
