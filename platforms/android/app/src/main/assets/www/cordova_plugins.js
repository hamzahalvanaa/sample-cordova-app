cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "cordova-plugin-hockeyapp.HockeyApp",
      "file": "plugins/cordova-plugin-hockeyapp/www/hockeyapp.js",
      "pluginId": "cordova-plugin-hockeyapp",
      "clobbers": [
        "hockeyapp",
        "hockeyApp"
      ]
    },
    {
      "id": "cordova-plugin-ble-central.ble",
      "file": "plugins/cordova-plugin-ble-central/www/ble.js",
      "pluginId": "cordova-plugin-ble-central",
      "clobbers": [
        "ble"
      ]
    },
    {
      "id": "cordova-plugin-hyperscanner.Hyperscanner",
      "file": "plugins/cordova-plugin-hyperscanner/www/Hyperscanner.js",
      "pluginId": "cordova-plugin-hyperscanner",
      "clobbers": [
        "cordova.plugins.Hyperscanner"
      ]
    },
    {
      "id": "cordova-plugin-cs108.Cs108Plugin",
      "file": "plugins/cordova-plugin-cs108/www/Cs108Plugin.js",
      "pluginId": "cordova-plugin-cs108",
      "clobbers": [
        "window.plugins.Cs108Plugin"
      ]
    }
  ];
  module.exports.metadata = {
    "cordova-plugin-hockeyapp": "5.2.0",
    "cordova-plugin-whitelist": "1.3.4",
    "cordova-plugin-ble-central": "1.2.5",
    "cordova-plugin-hyperscanner": "0.0.1",
    "cordova-plugin-cs108": "0.0.1"
  };
});