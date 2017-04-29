module.exports = {
    greet: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TaffPrint", "greet", [name]);
    },
    scan: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TaffPrint", "scan", [name]);
    }
};
// Taffprint in the separate plugin project