import websocket

ws = websocket.WebSocket()
ws.connect('ws://192.168.2.204:45454')
ws.send('{"amount":20,"endpoint":"PAYMENT","method":"CARD","order_tracking":"example_sale_from_test","receipt_email":"customer@gmail.com","receipt_sms":"0777777777","id":1}')
while(True):
    result = ws.recv()
    print(result)
