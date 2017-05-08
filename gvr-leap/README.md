# GVRf Leap Motion Sample

This is a very simple sample showing the integration of the Leap Motion Sensor with GearVRf.

The sample has a dependency on the leap-motion gvrf library. Use the following command to generate the 
library(note the path to the library).

````
GearVRf/GVRf/Extensions/3DCursor/IODevices/leap_motion$ ./gradlew assembleDebug
````

This should create the gvrf-leap-motion-debug aar file in the demo's "gearvrf-libs" folder:

````
GearVRf-Demos/gearvrf-libs$ ls gvrf-leap-motion-debug.aar 
gvrf-leap-motion-debug.aar
````

The sample should now compile and run successfully.

