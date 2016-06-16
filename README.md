# GearVRf-Demos

## How to run these examples
* make sure you have this repository and [the GearVR framework](https://github.com/Samsung/GearVRf) in a sibling directory 

```
git clone https://github.com/Samsung/GearVRf.git
git clone https://github.com/gearvrf/GearVRf-Demos.git
```

* make sure you have gradle in your PATH (you can check that your PATH is setup correctly and verify your gradle version by running: `gradle -v` if you get a response, great! if not, [these instructions may help](https://developer.nvidia.com/codeworks-android)

* change to the example you wish to build and invoke the included gradle wrapper file, `./gradlew` 

```
cd gvr-sample
./gradlew assembleDebug
```

* you should now have an apk that you can push to an android device

```
adb install ./app/build/outputs/apk/app-debug.apk
```
NOTE: some of these examples use the application name instead of `app` to hold the apk build directory for instance, in gvr-cubemap, you will find the apk in `./gvr-cubemap/gvrcubemap/build/outputs/apk/app-debug.apk`

```
cd gvr-cubemap
./gradlew assembleDebug
adb install ./gvrcubemap/build/outputs/apk/app-debug.apk
```

* if you want to run the examples without a headset (this is useful for debugging), change the value of `vr_only` to `vr_dual`

###gvr-complexscene
A simple sample which can contain as many bunnies as we want to make it complex


###gvr-inputsample
Demonstrates using a Mouse or Gamepad device with GVRf. The program makes use of the IO framework to create cursor objects that help implement user defined interaction with other objects in the scene graph.


###gvr-remote-scripting
The remote scripting sample enables the debug server and sets up a text object with the ipaddress of the phone so we know where to telnet into.
