importPackage(org.gearvrf)

var colors = [0xFFFFFF, 0xE53935, 0xD81B60, 0x8E24AA, 0x5E35B1, 0x3949AB, 0x00897B];
var colorIdx = 0;

function onSensorEvent(event) {
    var keyEvent = event.getCursorController().getKeyEvent();
    if (keyEvent == null) {
        return;
    }

    if (keyEvent.getAction() == 0x0 /* ACTION_DOWN */) {
	    if (++colorIdx >= colors.length) {
            colorIdx = 0;
	    }

	    // Change text color
	    var textSceneObject = event.getObject();
	    textSceneObject.setTextColor(0xFF << 24 | colors[colorIdx]);
    }
}