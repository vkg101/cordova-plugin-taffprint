module.exports = {
    greet: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TaffPrint", "greet", [name]);
    },
    scan: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TaffPrint", "scan", []);
    },
    connect: function(name, success, error) {
        //Note: These callbacks will fire whenever a status change occurs.
        cordova.exec(success, error, "TaffPrint", "connect", [name]);
    },
    print: function(message, success, error) {
        cordova.exec(success, error, "TaffPrint", "print", [message]);
    },
    printLine: function(message, success, error) {
        console.log("Not implemented yet...");
        cordova.exec(success, error, "TaffPrint", "printLine", [message]);
    },
    printLogo: function(success, error) {
        cordova.exec(success, error, "TaffPrint", "printLogo", []);
    }

};