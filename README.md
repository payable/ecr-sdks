
![](https://i.imgur.com/P8L2Oc7.png)
### PAYable ECR SDKs - ECR Integration
ECR SDKs - https://github.com/payable/ecr-sdks
<hr>

#### Integration 

Make sure the ECR payment service is running on the terminal as below in the notification bar. 

![](https://i.imgur.com/agTUUmw.png)

The connection between the terminal and host system will be established using WebSocket which is running inside the ECR application. 

The server is implemented based in these WebSocket protocol versions

* [RFC 6455](https://tools.ietf.org/html/rfc6455) 
* [RFC 7692](https://tools.ietf.org/html/rfc7692)

Refer to the Mozilla [WebSocket APIs](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API) to write your WebSocket client or use any libraries available based on the above protocol versions.

#### Establishing connection between  ECR terminal and the host system

Initiate a WebSocket connection to the terminal's LAN IP address if both are in the same network, let's assume the example IP address of the terminal as `192.168.2.204` then the address would be starting with `ws://` and the ECR port number is 45454.

Example: `ws://192.168.2.204:45454`

Once the connection is established the host system will be notified from the implemented WebSocket client as connection established.

Success message with serial number: `Terminal connection established: PP35271812000161`

#### Sending payment request to the ECR terminal  

Adjust the below JSON request as per the payment details and send as plain text to the connected terminal and wait for the response.

Example request:
```
{
   "amount": 20.00,
   "endpoint": "PAYMENT",
   "method": "CARD",
   "order_tracking": "example_sale_from_test",
   "receipt_email": "customer@gmail.com",
   "receipt_sms": "0777777777",
   "id": 1
}
```
When the payment gets succeeded or failed, the ECR terminal will send the response back to the requested host system, so the response will be received to the host system if it's listening to the response message.

Example response:
```
{
   "approval_code":"408809",
   "card_name":"04/TLE PROJECT TEST CARD ",
   "card_type":"VISA",
   "mid":"242332553252353",
   "request":{
      "amount":20.0,
      "endpoint":"PAYMENT",
      "id":1,
      "method":"CARD",
      "order_tracking":"example_sale_from_test",
      "origin":"/192.168.2.230:41588",
      "receipt_email":"customer@gmail.com",
      "receipt_sms":"0777777777",
      "txid":0
   },
   "server_time":"2020-09-29 01:42:22 PM",
   "status":"STATUS_SUCCESS",
   "tid":"24343227",
   "transaction_type":"EMV",
   "txid":26741
}
```

#### Checking the terminal status

Browser the terminal's IP address with 8080 port number in the same network to ensure the terminal status. 

Example: `http://192.168.2.204:8080`

If the device is online with the local network, the URL will respond as below or else it won't respond anything since the terminal is offline.

![](https://i.imgur.com/LCKl7xh.png)

<hr>

### Java SDK Integration 

1. Copy or include the ECR JAR library [ecr-1.0.jar](https://github.com/payable/ecr-sdks/raw/master/maven/ecr-test/lib/ecr-1.0.jar) to the Java libs folder.

2. Construct the `ECRTerminal` object with IP address and implement the listener interface.

```java
ECRTerminal ecrTerminal = new ECRTerminal("192.168.2.204", new ECRTerminal.Listener(){

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

Initiate the terminal connection, call this method once and handle error when terminal disconnected

```java
ecrTerminal.connect();
```

After the connection is successfully established you can start to send the sale request to terminal.

3. Construct the sale request object and convert to JSON
```java
PAYableRequest request = new PAYableRequest(PAYableRequest.ENDPOINT_PAYMENT, 252, 256.00, PAYableRequest.METHOD_CARD);
String jsonRequest = request.toJson();
```

4. Send to terminal
```java
ecrTerminal.send(jsonRequest);
```

You can expect the reponse at `onMessage` method of the listener.

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

            ecrTerminal = new ECRTerminal("192.168.2.204", new ECRTerminal.Listener() {

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
let  ws  =  new  WebSocket('ws://192.168.2.204:45454')

ws.onopen  = () =>  console.log("Connection is opened")

ws.onclose  = () =>  console.log("Connection is closed")

ws.onerror  = (err) =>  console.log('Error occured: '  +  err)

ws.onmessage  = (message) =>  console.log('Terminal: '  +  message.data)
```

Sending payment request to the terminal

```javascript
ws.send(`{"endpoint":"PAYMENT","amount":20.00,"id":1,"method":"CARD","order_tracking":"some_id","receipt_email":"customer@some.lk","receipt_sms":"0771111111"}`)
```

Refer the example for HTML and JavaScript
https://payable.github.io/ecr-sdks/html/

<hr>

### USB Connection

If you want to connect the terminal using USB cable, please follow the below steps.

1. Install Android Debug Bridge (adb) and set the environment path for ADB directory, this can be downloaded from [Android platform-tools](https://developer.android.com/studio/releases/platform-tools)

2. If you are using PAYable Java ECR SDK, you can ignore this, or else you have to run this command on your terminal or PowerShell.

```sh
adb forward tcp:45454 tcp:45454
```

3. When you connect the terminal using IP address, you have to provide the IP address as `ws://127.0.0.1:45454`

### USB connection Using PAYable Java ECR SDK

1. Install ADB as per the previous step.

2. Set the IP address as `127.0.0.1`.

```java
ECRTerminal ecrTerminal = new ECRTerminal("127.0.0.1", new ECRTerminal.Listener(){...
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


*PAYable ECR SDKs Integration*
