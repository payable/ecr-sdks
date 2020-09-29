#!/usr/bin/env node

const W3CWebSocket = require('websocket').w3cwebsocket;
const readline = require('readline');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const client = new W3CWebSocket('ws://192.168.2.222:45454');

client.onerror = function () {
    console.log('Connection Error');
};

client.onopen = function () {
    console.log('Client Connected');
};

client.onclose = function () {
    console.log('Client Closed');
};

client.onmessage = function (e) {
    console.log("Received: '" + e.data + "'");
    client.send('from JavaScript: ' + e.data);
};