// const Cs108Plugin = require("../../Cs108Plugin/www/Cs108Plugin");

function onDeviceReady() {
    alert("Calling onDeviceReady()");
    window.plugins.Cs108Plugin.showToast('nganu', 'long', function (resp) {
        console.log('nice shot = ', resp);
    }, function (error) {
        console.log(error);
    });
}

setTimeout(function () {
    document.addEventListener('deviceready', onDeviceReady, false);
}, 9000);

function openAnyPage(param) {
    var pageName = param;
    console.log(pageName);
}

function funcTest() {
    alert('another func')
}

function functionOpenContact(params) {
    document.getElementById('name').value = params.name;
}

function doScan() {
    window.plugins.Cs108Plugin.scan(function (resp) {
        console.log(resp);
    }, function (error) {
        console.log(error);
    });
}

function doConnect() {
    window.plugins.Cs108Plugin.connect(function (resp) {
        console.log(resp);
    }, function (error) {
        console.log(error);
    });
}

function bleScan() {
    ble.startScan([], function (device) {
        console.log(JSON.stringify(device));
    }, function (error) {
        console.log(error);
    });

    setTimeout(ble.stopScan,
        5000,
        function () { console.log("Scan complete"); },
        function () { console.log("stopScan failed"); }
    );
}
