package org.payable.ecr;

import com.google.gson.Gson;

import org.java_websocket.WebSocket;

public class PAYableRequest {

    public static final String METHOD_ANY = "ANY";
    public static final String METHOD_CARD = "CARD";
    public static final String METHOD_WALLET = "WALLET";
    public static final String ENDPOINT_PAYMENT = "PAYMENT";
    public static final String ENDPOINT_VOID = "VOID";

    public String endpoint;
    public String terminal;
    public Boolean external;
    public Integer auth_code;
    public Integer id;
    public String origin;
    public Double amount;
    public String method;
    public String order_tracking;
    public String receipt_email;
    public String receipt_sms;
    public Long txid;

    public PAYableRequest() {
    }

    public PAYableRequest(String endpoint, int id, double amount, String method) {
        this.endpoint = endpoint;
        this.id = id;
        this.amount = amount;
        this.method = method;
    }

    public PAYableRequest(String terminal, String endpoint, int id, double amount, String method) {
        this.terminal = terminal;
        this.endpoint = endpoint;
        this.id = id;
        this.amount = amount;
        this.method = method;
    }

    public PAYableRequest(WebSocket conn) {
        this.origin = conn.getAttachment();
    }

    public PAYableRequest(String terminal, int auth_code) {
        this.terminal = terminal;
        this.auth_code = auth_code;
    }

    public static PAYableRequest from(String data) {
        return new Gson().fromJson(data, PAYableRequest.class);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public boolean isExternal() {
        return external != null && external == true;
    }
}
