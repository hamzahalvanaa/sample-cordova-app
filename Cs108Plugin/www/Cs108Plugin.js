var exec = require('cordova/exec');

var PLUGIN_NAME = 'Cs108Plugin';

// Empty constructor
function Cs108Plugin() { }

// The function that passes work along to native shells
Cs108Plugin.prototype.showToast = function (message, duration, successCallback, errorCallback) {
    var options = {};
    options.message = message;
    options.duration = duration;
    exec(successCallback, errorCallback, PLUGIN_NAME, 'showToast', [options]);
};

Cs108Plugin.prototype.scan = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, PLUGIN_NAME, 'scan', []);
};

Cs108Plugin.prototype.connect = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, PLUGIN_NAME, 'connect', []);
};

// Installation constructor that binds CS108Plugin to window
Cs108Plugin.install = function () {
    if (!window.plugins) {
        window.plugins = {};
    }
    window.plugins.Cs108Plugin = new Cs108Plugin();
    return window.plugins.Cs108Plugin;
};
cordova.addConstructor(Cs108Plugin.install);
