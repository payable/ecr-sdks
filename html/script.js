let ws

window.addEventListener('load', function () {
    document.getElementById('textarea').value = JSON.stringify(JSON.parse(document.getElementById('textarea').value), undefined, 4);
})

function connection() {

    let address = document.getElementById('address').value

    if (ws) ws.close()

    ws = new WebSocket(address)

    ws.onopen = () => updateResult("Connection is opened")

    ws.onclose = () => updateResult("Connection is closed")

    ws.onerror = (err) => updateResult('Error occured: ' + err)

    ws.onmessage = (message) => { updateResult('Response received: ' + message.data) }
}

function sendPaymentRequest() {
    if (ws) {
        let request = document.getElementById('textarea').value;
        ws.send(request)
        updateResult('Request sent: ' + request)
    }
}

function updateResult(data) {
    document.getElementById('result').innerHTML = '<li>' + data + '</li>' + document.getElementById('result').innerHTML
}