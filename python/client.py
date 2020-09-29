import websocket

ws = websocket.WebSocket()
ws.connect('ws://192.168.2.222:45454')
print('connected')
while(True):
    result = ws.recv()
    print(result)
    # ws.send('from Python: ' + result)
    ws.send('{"amount":200,"id":1,"method":"CARD","order_tracking":"666"}')
