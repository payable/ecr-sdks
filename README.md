
![](https://i.imgur.com/ERpCDa7.png)
ECR SDKs - https://github.com/payable/ecr-sdks
### PAYable ECR SDKs - ECR Integration 
<hr>

### Initialization 

* Make sure the ECR payment service is running on the terminal as below. <br/><br/>
![](https://i.imgur.com/pka7PqI.png)

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

*Terminal Serial: PP35271812000161*
*ECR Version: 1.0*
*PAYable Status: READY*

*- PAYable ECR SDKs Integration*

#### Java SDK Integration 

1. Copy or include the ECR JAR library [ecr-1.0.jar](https://github.com/payable/ecr-sdks/raw/master/maven/ecr-test/lib/ecr-1.0.jar) to the Java libs folder.

```
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import com.google.gson.JsonSyntaxException;

import org.payable.ecr.ECRTerminal;
import org.payable.ecr.PAYableRequest;
import org.payable.ecr.PAYableResponse;

/**
 * PAYable SDK Demo
 *
 */
class Demo {

    ECRTerminal ecrTerminal;

    public void start() {

        try {

            ecrTerminal = new ECRTerminal("192.168.2.204", new ECRTerminal.Listener() {

                @Override
                public void onOpen(String data) {

                    // After the connection is successfully established you can start to send the sale request to terminal
                    // Sample JSON request for ECR sale transaction.
                    // {"endpoint":"PAYMENT","amount":20,"id":1,"method":"CARD","order_tracking":"some_id","receipt_email":"aslam@payable.lk","receipt_sms":"0762724081","txid":14526}

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
