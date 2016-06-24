# GearVRf-Demos

## How to run these examples
* make sure you have [this repository](https://github.com/gearvrf/GearVRf-Demos) and [the GearVR framework](https://github.com/Samsung/GearVRf) in a sibling directory 

```
git clone https://github.com/Samsung/GearVRf.git
git clone https://github.com/gearvrf/GearVRf-Demos.git
```

* if you don't have your computer set up for Android [SDK](https://developer.android.com/studio/index.html) and [NDK](https://developer.android.com/ndk/downloads/index.html) development - the simplest way to get those configured is to use [NVIDIA's Codeworks](https://developer.nvidia.com/codeworks-android) which will automatically configure your `~/.bash_profile` with several important environment variables. They require a registration (free) to download, but you could also download each of the required tools separately and add the directory paths to `~/.bash_profile` yourself:
 
```
#update version numbers and paths for your local system
export ANDROID_HOME="~/mytools/android-sdk-macosx"
export NDK_ROOT="~/mytools/android-ndk-r11c"
export NDKROOT=$NDK_ROOT
export NVPACK_NDK_VERSION="android-ndk-r11c"
export ANT_HOME="~/mytools/apache-ant-1.8.2"
export GRADLE_HOME="~/mytools/gradle-2.2.1"
export PATH="$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools:$NDK_ROOT:$ANT_HOME/bin:$GRADLE_HOME:$PATH"
```

* [download the Oculus_Mobile_SDK](https://developer.oculus.com/downloads/mobile/1.0.0.1/Oculus_Mobile_SDK/) and move the relevant jar files into `GearVRf/GVRf/Framework/framework/src/main/libs/`

```
#edit to use your PATH or make a env variable reference for wherever you unzipped the SDK
export OVR_MOBILE_SDK=~/mytools/ovr_sdk_mobile_1.0.0.1

cd GearVRf/GVRf/Framework/framework/src/main/libs/
cp $OVR_MOBILE_SDK/VrAppFramework/Libs/Android/AppFramework.jar ./
cp $OVR_MOBILE_SDK/VrApi/Libs/Android/VrApi.jar ./
cp $OVR_MOBILE_SDK/VrAppSupport/SystemUtils/Libs/Android/SystemUtils.jar ./

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
	 
	
###gvr-complexscene
A simple sample which can contain as many bunnies as we want to make it complex


###gvr-inputsample
Demonstrates using a Mouse or Gamepad device with GVRf. The program makes use of the IO framework to create cursor objects that help implement user defined interaction with other objects in the scene graph.


###gvr-remote-scripting
The remote scripting sample enables the debug server and sets up a text object with the ipaddress of the phone so we know where to telnet into.

