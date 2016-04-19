importPackage(org.gearvrf)

function onPause() {
    utils.log("script log: onPause");
}

function onResume() {
    utils.log("script log: onResume");
}

function onDestroy() {
    utils.log("script log: onDestroy");
}

function onSetScript(script) {
    utils.log("script log: onSetScript");
}
