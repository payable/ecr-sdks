#!/usr/bin/env node

const W3CWebSocket = require('websocket').w3cwebsocket;

// ws://127.0.0.1:3001/socket.io/?EIO=3&transport=websocket&token=ASLAMs
const ws = new W3CWebSocket('ws://192.168.2.222:45454');

ws.onerror = function () {
    console.log('Connection Error');
};

ws.onopen = function () {
    console.log('Client Connected');
};

ws.onclose = function () {
    console.log('Client Closed');
};

ws.onmessage = function (e) {
    console.log("Received: '" + e.data + "'");
    // client.send('from JavaScript: ' + e.data);
    // ws.send('42' + JSON.stringify(['client_data', '{"token":"ASLAM", "data": "{}"}']));
};