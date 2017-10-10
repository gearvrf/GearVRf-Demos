importPackage(org.gearvrf)

var colors = [0xFFFFFF, 0xE53935, 0xD81B60, 0x8E24AA, 0x5E35B1, 0x3949AB, 0x00897B];
var colorIdx = 0;

var sceneObject = null;
var localAnimation = -1;

function onInit(gvrf, sceneObj) {
    sceneObject = sceneObj;
}

function onSensorEvent(event) {
    var keyEvent = event.getCursorController().getKeyEvent();
    if (keyEvent == null) {
        return;
    }

    var actionType = keyEvent.getAction();
    if (actionType == 0x0 /* ACTION_DOWN */) {
	    if (++colorIdx >= colors.length) {
            colorIdx = 0;
	    }

	    // Change text color
	    var textSceneObject = event.getPickedObject().getHitObject();
	    textSceneObject.setTextColor(0xFF << 24 | colors[colorIdx]);

	    // Local animation
	    localAnimation = 60;
    }
}

function onStep() {
    if (localAnimation >= 0) {
        var scale = 1 + .175 * Math.sin((60 - localAnimation) / 60 * 2 * Math.PI);
        scale *= 50;
        sceneObject.getTransform().setScale(scale, scale, scale);
        localAnimation--;
    }
}
