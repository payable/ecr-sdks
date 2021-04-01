### PAYable ECR SDKs - ECR Integration

![](https://i.imgur.com/P8L2Oc7.png)

ECR SDKs - [ecr-git-demo.payable.lk](https://ecr-git-demo.payable.lk/)

PAYable ECR SDKs provide a bridge between PAYable terminals and any kind of POS platform without any language barriers to complete the PAYment transaction requests.

<hr>

### Integration 

Make sure the ECR payment service is running on the terminal as below in the notification bar. 

![](https://i.imgur.com/agTUUmw.png)

The connection between the terminal and host system will be established using WebSocket which is running inside the ECR application. 

The server is implemented based on these WebSocket protocol versions

* [RFC 6455](https://tools.ietf.org/html/rfc6455) 
* [RFC 7692](https://tools.ietf.org/html/rfc7692)

Refer to the Mozilla [WebSocket APIs](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API) to write your WebSocket client or use any libraries available based on the above protocol versions.

#### 1. Internal connection through LAN Network

The below explanation can guide you to establish a connection between the host system and the ECR terminal using an internal (LAN - Local Area Network) network.

#### 1.1 Establishing connection between  ECR terminal and the host system through LAN

Initiate a WebSocket connection to the terminal's LAN IP address if both are in the same network, let's assume the example IP address of the terminal as `192.168.8.101` then the address would be starting with `ws://` and the ECR port number is **45454**.

* Token: **4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=**
* POS host name:  **COMPANY-1** - This is the name of your current POS system.

Example:

```
ws://192.168.8.101:45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=COMPANY-1
```

Once the connection is established, the host system will be notified from the implemented WebSocket client with a success message from the terminal as below. 

```json
{
   "origin":"PP35271812000161",
   "request":{
      "origin":"COMPANY-1"
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

1.2.1 Card payment

Adjust the below JSON request as per the payment details, send as plain text to the connected terminal and wait for the response.

Example request:

```json
{
   "amount": 20.00,
   "endpoint": "PAYMENT",
   "method": "CARD",

   "order_tracking": "example_sale_from_test", // any tracking reference data - optional
   "receipt_email": "customer@gmail.com", // customer email - optional
   "receipt_sms": "0777777777", // customer phone number - optional
   "id": 14526 // any numeric reference id - optional
}
```
When the payment gets succeeded or failed, the ECR terminal will send the response back to the requested host system with the request, so the response will be received to the host system if it's listening to the response message.

Example successful response:

```json
{
   "approval_code":"408809",
   "card_name":"MOHAMMED/ASLAM            ",
   "card_no":"XXXX-XXXX-XXXX-5050",
   "card_type":"MASTER",
   "mid":"242332553252353",
   "origin":"PP35271812000161",
   "request":{
      "amount":20.0,
      "endpoint":"PAYMENT",
      "id":1,
      "method":"CARD",
      "order_tracking":"example_sale_from_test",
      "origin":"COMPANY-1",
      "receipt_email":"customer@gmail.com",
      "receipt_sms":"0777777777"
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
      "origin":"COMPANY-1",
      "receipt_email":"customer@gmail.com",
      "receipt_sms":"0777777777"
   },
   "status":"STATUS_FAILED"
}
```

1.2.2 Wallet payment

Example request:

```json
{
   "amount": 20.00,
   "endpoint": "PAYMENT",
   "method": "WALLET",

   "id":1, // any numeric reference id - optional
   "mobile_no": "0777777777", // customer mobile number - optional
   "bill_no": "unique_no", // unique reference no - optional
   "print_receipt": "YES" // YES | NO - optional
}
```

Example successful response:

```json
{
   "rrn":"408809",
   "tid":"24343227",
   "mid":"242332553252353",
   "origin":"PP35271812000161",
   "request":{
      "amount":20.0,
      "bill_no":"unique_no",
      "endpoint":"PAYMENT",
      "id":1,
      "method":"WALLET",
      "mobile_no":"0777777777",
      "origin":"COMPANY-1",
      "print_receipt":"YES"
   },
   "status":"STATUS_SUCCESS"
}
```

Example failure response:

```json
{
   "error":"Duplicate Bill number",
   "origin":"PP35271812000161",
   "request":{
      "amount":20.0,
      "bill_no":"unique_no",
      "endpoint":"PAYMENT",
      "id":1,
      "method":"WALLET",
      "mobile_no":"0777777777",
      "origin":"COMPANY-1",
      "print_receipt":"YES"
   },
   "status":"STATUS_FAILED"
}
```
#### 1.3 Sending discount payment request to the ECR terminal

The discount is applied based on the card bin numbers that you set to the terminal. If the payee's card matched with the given bin ranges the amount will be applied as per the discount amount otherwise you can take the decision to reject the card or continue with the default payment amount.

Example request:

```json
{
   "endpoint":"PAYMENT",
   "amount":100,
   "id":1, // any numeric reference id - optional
   "method":"CARD",
   "discounts":[
      {
         "name":"COMMERCIAL-CREDIT-CARD",
         "amount":55.05,
         "bin_ranges":[
            432571,
            432572,
            437840,
            510484,
            550489
         ]
      }
   ]
}
```

As per the above request, if the payee's card bin number does not match with the above discount bin ranges, the default amount 100 will be proceeded, or if it matches then the 55.05 amount will be proceeded.

But if you want to stop the payment process if the payee's card number does not match with any given discount bin ranges you need to add the below property to the request.

```json
"discounts_only":"YES"
```

The responses are sent back to you based on your requests, the below are example responses;

1. When the card matches with the given discount ranges you will get the applied discount in `applied_discount` property:

```json
{
   "applied_discount":{
      "amount":55.05,
      "bin_ranges":[
         432571,
         432572,
         437840,
         510484,
         550489
      ],
      "name":"COMMERCIAL-CREDIT-CARD"
   },
   "approval_code":"408809",
   "card_name":"SOMENAME/S",
   "card_no":"4325-7200-XXXX-3006",
   "card_type":"VISA",
   "mid":"000000000000101",
   "origin":"PP352720A1004626",
   "request":{
      "amount":100.0,
      "discounts":[
         {
            "amount":55.05,
            "bin_ranges":[
               432571,
               432572,
               437840,
               510484,
               550489
            ],
            "name":"COMMERCIAL-CREDIT-CARD"
         }
      ],
      "endpoint":"PAYMENT",
      "id":1,
      "method":"CARD",
      "origin":"COMPANY-1"
   },
   "server_time":"2021-02-24 09:47:43 AM",
   "status":"STATUS_SUCCESS",
   "tid":"00000101",
   "transaction_type":"EMV",
   "txid":413455
}
```

2. If you set this property to YES `"discounts_only":"YES"` and the card does not match with the discount ranges:

```json
{
   "error":"Discount is not available",
   "origin":"PP352720A1004626",
   "request":{
      "amount":100.0,
      "discounts":[
         {
            "amount":55.05,
            "bin_ranges":[
               432571,
               432572,
               437840,
               510484,
               550489
            ],
            "name":"COMMERCIAL-CREDIT-CARD"
         }
      ],
      "discounts_only":"YES",
      "endpoint":"PAYMENT",
      "id":1,
      "method":"CARD",
      "origin":"COMPANY-1"
   },
   "status":"STATUS_FAILED"
}
```

#### 1.4 Checking the terminal status through LAN

Browse the terminal's IP address with 8080 port number in the same network to ensure the terminal status. 

Example: `http://192.168.8.101:8080`

If the device is online with the local network, the URL will respond as below or else it won't respond anything since the terminal is offline.

![](https://i.imgur.com/JosAnMu.png)

This will show the connected POS hosts from the internal and external network sources using LAN and WAN.

#### 1.5 Response status types

```
STATUS_TERMINAL_AUTHORIZED
STATUS_TERMINAL_AUTHORIZED_ALREADY
STATUS_SUCCESS
STATUS_INVALID_AUTHCODE
STATUS_API_UNREACHABLE
STATUS_FAILED
STATUS_NOT_LOGIN
STATUS_INVALID_AMOUNT
STATUS_INVALID_DATA
STATUS_BUSY
```

1.5.1 If the request data does not contain required fields or not a valid JSON the below status will be thrown with the error message.

```
STATUS_INVALID_DATA
```

1.5.2 If the PAYable application is not available on the terminal or the version of the application does not meet the requirement.

```
STATUS_API_UNREACHABLE
```

#### 2. External connection through WAN Network

The below explanation can guide you to establish a connection between the host system and the ECR terminal using an external (WAN - Wide Area Network) network/internet.

#### 2.1 Establishing connection between ECR terminal and the host system through WAN

2.1.1 Initiate a WebSocket connection to our centralized ECR network with the token and POS name.

* Centralized ECR network: **ws://ecr.payable.lk**
* Token: **4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=**
* POS host name:  **COMPANY-1** - This is the name of your current POS system.

Example:

```
ws://ecr.payable.lk?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=COMPANY-1
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

3. POS/Host name is duplicate and already in use

	Error:
	> HTTP/1.1 429 Too Many Requests

	Example:
	```
	Error during WebSocket handshake: Unexpected response code: 429
	```

2.1.2 Send a auth request to authorize the terminal with the current POS/Host system if it's not already authorized.

1. Turn on your terminal device and connect with the internet
2. Make sure the terminal is connected with the ECR network
3. Send the authorization request to the terminal's serial number as below

Example auth request:

```json
{
    "terminal": "PP35271812000161",
    "auth_code": 44280
}
```

The terminal will validate your auth code and accept the authorization request with a successful response as below.

Status:
```
STATUS_TERMINAL_AUTHORIZED
```
 
Example successful auth response:

```json
{
   "origin":"PP35271812000161",
   "request":{
      "auth_code":44280,
      "external":true,
      "origin":"COMPANY-1",
      "terminal":"PP35271812000161"
   },
   "status":"STATUS_TERMINAL_AUTHORIZED"
}
```

If you send an auth request to a terminal that is not connected and online with the ECR network, you will get the response as terminal unreachable with below status.

```
STATUS_TERMINAL_UNREACHABLE
```

If the auth code is invalid you will receive the response as the below status.

```
STATUS_INVALID_AUTHCODE
```

If the POS/Host system has already done a successful authorization process with the terminal, it will get the response status as below.

```
STATUS_TERMINAL_AUTHORIZED_ALREADY
```

#### 2.2 Sending payment request to the ECR terminal through WAN

The process is same as the LAN request/response that we already explained above in 1.2.

Additionally you need to add terminal in each request as below.

```json
{
    "terminal": "PP35271812000161",
    ...
}
```

Please check out the JavaScript example to [learn more.](http://ecr-git-demo.payable.lk/html)

You will get the below status when you send a payment request to a terminal that is not connected to the ECR network and online.

```
STATUS_TERMINAL_UNREACHABLE
```

When you send a payment request to a terminal that is not authorized with your POS/Host system at least once, you will get the status as not authorized.

```
STATUS_TERMINAL_UNAUTHORIZED
```

> JavaScript Demonstration: [http://ecr-git-demo.payable.lk/html](http://ecr-git-demo.payable.lk/html)

<hr>

#### 3. ECR Android Service

ECR Android service is running in the terminal to expose the ECR facilities.

#### 3.1 Auth Code

Auth code is the authorization code that helps the POS/Host systems to make the connection between terminal and POS/Host within the ECR network through WAN.

![](https://i.imgur.com/pdEvwam.png)

#### 3.2 Revoke Auth Code

The auth code can be revoked from the below option in the ECR application of the terminal, which would help to prevent the new POS/Host system from connecting to the terminal.

![](https://i.imgur.com/RgQiemb.png)

#### 3.2 Remove external POS/Host systems from the terminal

POS/Host systems that are authorized already would be listed here in the ECR application of the terminal, it can be removed from the terminal using the remove button which also removes the absolute authorizations of those POS/Host systems.

![](https://i.imgur.com/NE0EBN8.png)

### Java SDK Integration 

1. Copy or include the ECR JAR library [ecr-1.1.jar](https://github.com/payable/ecr-sdks/raw/master/maven/maven-test/lib/ecr-1.1.jar) to the Java libs folder of your Java project.

2. Construct the `ECRTerminal` object with IP address, token, POS host name and implement the listener interface.

If you connect the terminal using the LAN network internally, the object should be constructed as below:

```java
ECRTerminal ecrTerminal = new ECRTerminal(ip_address, token, pos_name, listener);
```

If you connect the terminal using the WAN network externally, the object should be constructed as below:

```java
ECRTerminal ecrTerminal = new ECRTerminal(token, pos_name, listener);
```

Example: 

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

If you have connected the terminal using the WAN network externally, the payment request should be constructed as below:

The first parameter is the serial number of your terminal

```java
PAYableRequest  request  =  new  PAYableRequest("PP35271812000161", PAYableRequest.ENDPOINT_PAYMENT, 252, 256.00, PAYableRequest.METHOD_CARD);
```

Explanation: 

```java
PAYableRequest  request  =  new  PAYableRequest(terminal, PAYableRequest.ENDPOINT_PAYMENT, id, amount, PAYableRequest.METHOD_CARD);
```

4. Send to terminal

```java
ecrTerminal.send(jsonRequest);
```

> You can expect the reponse at `onMessage` method of the listener.

If the terminal rejects your request with `STATUS_TERMINAL_UNAUTHORIZED` status, you need to send the auth request to the terminal as below:

```java
PAYableRequest  request  =  new  PAYableRequest(terminal_serial, auth_code);
ecrTerminal.send(request.toJson());
```

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

                    // If you want to add a discount options to the request
                    // request.addDiscount(new PAYableDiscount("COM", 10, 432572, 435262, 432572));

                    // 2. Send to terminal
                    ecrTerminal.send(request.toJson());

                    // 3. Expect the response at 'onMessage' method

                    // If you want to stop the ongoing payment process in LAN/USB
                    // ecrTerminal.send(PAYableRequest.stop());

                    // If you want to stop the ongoing payment process in WAN
                    // ecrTerminal.send(PAYableRequest.stop("PP352720A1004626"));
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
let  ws  =  new  WebSocket('ws://192.168.8.101:45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=COMPANY-1')

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
[payable.github.io/ecr-sdks/html](http://payable.github.io/ecr-sdks/html/)

<hr>

### Python SDK Integration

```python
import websocket

ws = websocket.WebSocket()
ws.connect('ws://192.168.8.101:45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=COMPANY-1')
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
ws://127.0.0.1:45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=COMPANY-1
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

### P2P (Wifi-Direct) ECR SDK

Wi-Fi Direct (P2P) allows Android 4.0 (API level 14) and higher devices with the appropriate hardware to connect directly to each other via Wi-Fi without an intermediate access point.

P2P library provides instant integration (PnP) support for WIFI-Direct P2P for any Android projects plus it remembers the recently connected device and reconnects it automatically when it's available.

* No SSID Access Point is required
* No need to connect to WIFI Network
* Can be used while Mobile Data is turned on
* Auto reconnects when the terminal is available nearby

Refer the [P2P Documentation](https://aslamanver.github.io/p2p/demo-projects/demo-ecr) to know more about PAYable P2P Wi-Fi Direct implementation.

Refer this repository to learn more.

*PAYable ECR SDKs Integration*
