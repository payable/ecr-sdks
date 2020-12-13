![](https://i.imgur.com/P8L2Oc7.png)
### PAYable ECR SDKs - ECR Integration
ECR SDKs - [ecr-git-demo.payable.lk](https://ecr-git-demo.payable.lk/)
<hr>

### 1.0 Integration 

Make sure the ECR payment service is running on the terminal as below in the notification bar. 

![](https://i.imgur.com/agTUUmw.png)

The connection between the terminal and host system will be established using WebSocket which is running inside the ECR application. 

The server is implemented based on these WebSocket protocol versions

* [RFC 6455](https://tools.ietf.org/html/rfc6455) 
* [RFC 7692](https://tools.ietf.org/html/rfc7692)

Refer to the Mozilla [WebSocket APIs](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API) to write your WebSocket client or use any libraries available based on the above protocol versions.

#### 1.0 Internal connection through LAN Network

The below explanation can guide you to establish a connection between the host system and the ECR terminal using an internal (LAN - Local Area Network) network.

#### 1.1 Establishing connection between  ECR terminal and the host system through LAN

Initiate a WebSocket connection to the terminal's LAN IP address if both are in the same network, let's assume the example IP address of the terminal as `192.168.8.101` then the address would be starting with `ws://` and the ECR port number is **45454**.

* Token: **4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=**
* POS host name:  **ARPICO-1** - This is the name of your current POS system.

Example:

```
ws://192.168.8.101:45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=ARPICO-1
```

Once the connection is established, the host system will be notified from the implemented WebSocket client with a success message from the terminal as below. 

```json
{
   "origin":"PP35271812000161",
   "request":{
      "origin":"ARPICO-1"
   },
   "status":"STATUS_TERMINAL_AUTHORIZED"
}
```

In the below situations handshake will be failed with the 404 error code:

1. Token is invalid or not presented
2. POS name is empty or not provided
3. POS name is duplicate and already in use

Error:

```
Error during WebSocket handshake: Unexpected response code: 404
```

#### 1.2 Sending payment request to the ECR terminal  

Adjust the below JSON request as per the payment details, send as plain text to the connected terminal and wait for the response.

Example request:

```json
{
   "amount": 20.00,
   "endpoint": "PAYMENT",
   "method": "CARD",
   "order_tracking": "example_sale_from_test",
   "receipt_email": "customer@gmail.com",
   "receipt_sms": "0777777777",
   "id": 14526
}
```
When the payment gets succeeded or failed, the ECR terminal will send the response back to the requested host system with the request, so the response will be received to the host system if it's listening to the response message.

Example success response:

```json
{
   "approval_code":"408809",
   "card_name":"MOHAMMED/ASLAM            ",
   "card_type":"MASTER",
   "mid":"242332553252353",
   "origin":"PP35271812000161",
   "request":{
      "amount":20.0,
      "endpoint":"PAYMENT",
      "id":1,
      "method":"CARD",
      "order_tracking":"example_sale_from_test",
      "origin":"ARPICO-1",
      "receipt_email":"customer@gmail.com",
      "receipt_sms":"0777777777",
      "txid":14526
   },
   "server_time":"2020-12-12 01:30:55 AM",
   "status":"STATUS_SUCCESS",
   "tid":"24343227",
   "transaction_type":"EMV",
   "txid":44910
}
```

Example failure response:

```json
{
   "error":"Received duplicate transaction request.",
   "origin":"PP35271812000161",
   "request":{
      "amount":20.0,
      "endpoint":"PAYMENT",
      "id":1,
      "method":"CARD",
      "order_tracking":"example_sale_from_test",
      "origin":"ARPICO-1",
      "receipt_email":"customer@gmail.com",
      "receipt_sms":"0777777777",
      "txid":14526
   },
   "status":"STATUS_FAILED"
}
```

Transaction status types:

```
STATUS_TERMINAL_AUTHORIZED
STATUS_SUCCESS
STATUS_API_UNREACHABLE
STATUS_FAILED
STATUS_NOT_LOGIN
STATUS_INVALID_AMOUNT
STATUS_BUSY
```

#### 1.3 Checking the terminal status

Browse the terminal's IP address with 8080 port number in the same network to ensure the terminal status. 

Example: `http://192.168.8.101:8080`

If the device is online with the local network, the URL will respond as below or else it won't respond anything since the terminal is offline.

![](https://i.imgur.com/ESCsDb3.png)

This will show the connected POS hosts from the internal and external network sources using LAN and WAN.

#### 2.0 External connection through WAN Network

The below explanation can guide you to establish a connection between the host system and the ECR terminal using an external (WAN - Wide Area Network) network/internet.

#### 2.1 Establishing connection between  ECR terminal and the host system through WAN

1. Initiate a WebSocket connection to our centralized ECR network with the token and POS name.

* Centralized ECR network: **ws://ecr.payable.lk**
* Token: **4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=**
* POS host name:  **ARPICO-1** - This is the name of your current POS system.

Example:

```
ws://ecr.payable.lk?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=ARPICO-1
```

Once the connection is established, the implemented WebSocket client will be notified as the server accepted the handshake and opened the socket connection by triggering the `onOpen` method.

In the below situations handshake will be failed:

1. Token and POS/Host name are not provided

	Error:

	> HTTP/1.1 400 Bad Request

	Example:

	```
	Error during WebSocket handshake: Unexpected response code: 400
	```

2. Token is invalid

	Error:

	> HTTP/1.1 401 Unauthorized

	Example:

	```
	Error during WebSocket handshake: Unexpected response code: 401
	```

	Some clients can identify the response code 401 as invalid authentication

	```
	HTTP Authentication failed; no valid credentials available
	```

3. POS name is duplicate and already in use

	Error:

	> HTTP/1.1 429 Too Many Requests

	Example:

	```
	Error during WebSocket handshake: Unexpected response code: 429
	```

<hr>

### Java SDK Integration 

1. Copy or include the ECR JAR library [ecr-1.0.jar](https://github.com/payable/ecr-sdks/raw/master/maven/ecr-test/lib/ecr-1.0.jar) to the Java libs folder of your Java project.

2. Construct the `ECRTerminal` object with IP address, token, POS host name and implement the listener interface.

```java
ECRTerminal ecrTerminal = new ECRTerminal("192.168.8.101", "4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=", "JAVA-POS", new ECRTerminal.Listener(){

    @Override
    public void onOpen(String data) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onMessage(ByteBuffer message) {

    }

    @Override
    public void onError(Exception ex) {

    }
});
```

Initiate the terminal connection, call this method once and do error handling when the terminal disconnected.

```java
ecrTerminal.connect();
```

After the connection is successfully established you can start to send the payment request to the terminal.

3. Construct the payment request object and convert it to JSON

```java
PAYableRequest request = new PAYableRequest(PAYableRequest.ENDPOINT_PAYMENT, 252, 256.00, PAYableRequest.METHOD_CARD);
String jsonRequest = request.toJson();
```

4. Send to terminal

```java
ecrTerminal.send(jsonRequest);
```

> You can expect the reponse at `onMessage` method of the listener.

Refer to the below demonstration to know more about connection and payment requests.

```java
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import com.google.gson.JsonSyntaxException;

import org.payable.ecr.ECRTerminal;
import org.payable.ecr.PAYableRequest;
import org.payable.ecr.PAYableResponse;

class Demo {

    ECRTerminal ecrTerminal;

    public void start() {

        try {

            ecrTerminal = new ECRTerminal("192.168.2.204", "4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=", "JAVA-POS", new ECRTerminal.Listener() {

                @Override
                public void onOpen(String data) {

                    // 1. Construct the sale request object
                    PAYableRequest request = new PAYableRequest(PAYableRequest.ENDPOINT_PAYMENT, 252, 256.00, PAYableRequest.METHOD_CARD);

                    // 2. Convert to JSON
                    String jsonRequest = request.toJson();

                    // 3. Send to terminal
                    ecrTerminal.send(jsonRequest);

                    // 4. Expect the reponse at 'onMessage' method
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {

                }

                @Override
                public void onMessage(String message) {

                    // Map JSON to Java object (optional) or handle from String data
                    try {
                        PAYableResponse response = PAYableResponse.from(message);
                        System.out.println("RESPONSE: " + response.status);
                    } catch (JsonSyntaxException ex) {
                        // Invalid JSON
                    }
                }

                @Override
                public void onMessage(ByteBuffer message) {
                    // Optional implementation
                }

                @Override
                public void onError(Exception ex) {
                    // Handle connection exceptions
                }
            });

            // Just for the console print
            ecrTerminal.debug = true;

            // Initiate the terminal connection, call this method once and handle error when terminal disconnected
            ecrTerminal.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Demo().start();
    }
}
```

<hr>

### JavaScript SDK Integration 

Establishing connection

```javascript
let  ws  =  new  WebSocket('ws://192.168.8.101:45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=ARPICO-1')

ws.onopen  = () =>  console.log("Connection is opened")

ws.onclose  = () =>  console.log("Connection is closed")

ws.onerror  = (err) =>  console.log('Error occured: '  +  err)

ws.onmessage  = (message) =>  console.log('Response received: '  +  message.data)
```

Sending payment request to the terminal

```javascript
ws.send(`{"endpoint":"PAYMENT","amount":20.00,"id":1,"method":"CARD","order_tracking":"some_id","receipt_email":"customer@some.lk","receipt_sms":"0771111111"}`)
```

Refer the examples for HTML and JavaScript implementations 
[payable.github.io/ecr-sdks/html](https://payable.github.io/ecr-sdks/html/)

<hr>

### Python SDK Integration

```python
import websocket

ws = websocket.WebSocket()
ws.connect('ws://192.168.8.101:45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=ARPICO-1')
ws.send('{"amount":20,"endpoint":"PAYMENT","method":"CARD","order_tracking":"example_sale_from_test","receipt_email":"customer@gmail.com","receipt_sms":"0777777777","id":1}')
while(True):
    result = ws.recv()
    print(result)
```

<hr>

### USB Connection

> Request PAYable to enable the USB ADB feature to your terminal

If you want to connect the terminal using USB cable, please follow the below steps.

1. Install Android Debug Bridge (adb) and set the environment path for ADB directory on your host POS system, this can be downloaded from [Android platform-tools](https://developer.android.com/studio/releases/platform-tools)

2. If you are using PAYable Java ECR SDK, you can ignore the below step, or else you have to run this command on your Linux/Mac Terminal or Windows PowerShell.

```sh
adb forward tcp:45454 tcp:45454
```

3. When you connect the terminal through USB, you have to provide the IP address as `127.0.0.1`

```
ws://127.0.0.1:45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=ARPICO-1
```

### USB connection Using PAYable Java ECR SDK

1. Install ADB as per the previous step

2. Set the IP address as `127.0.0.1`

```java
ECRTerminal ecrTerminal = new ECRTerminal("127.0.0.1", ...
```

3. (Optional) If you have not installed ADB or any issues with your USB libraries, handle the below exceptions.

```java
...
} catch (URISyntaxException e) {
   e.printStackTrace();
} catch (IOException e) {
   e.printStackTrace();
} catch (InterruptedException e) {
   e.printStackTrace();
}
```

Refer this repository to learn more.

*PAYable ECR SDKs Integration*