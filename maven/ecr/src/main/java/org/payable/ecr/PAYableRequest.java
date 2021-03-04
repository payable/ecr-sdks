package org.payable.ecr;

import com.google.gson.Gson;

import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.List;

public class PAYableRequest {

    public static final String METHOD_ANY = "ANY";
    public static final String METHOD_CARD = "CARD";
    public static final String METHOD_WALLET = "WALLET";
    public static final String ENDPOINT_PAYMENT = "PAYMENT";
    public static final String ENDPOINT_VOID = "VOID";
    public static final String ENDPOINT_STOP = "STOP";

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
    public String mobile_no;
    public String bill_no;
    public String print_receipt;
    public List<PAYableDiscount> discounts;
    public String discounts_only;

    private PAYableRequest() {
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

    public void addDiscount(PAYableDiscount discount) {
        if (this.discounts == null) this.discounts = new ArrayList<>();
        this.discounts.add(discount);
    }

    public void clearDiscount() {
        this.discounts = null;
    }

    public PAYableRequest(WebSocket conn) {
        this.origin = conn.getAttachment();
    }

    public PAYableRequest(String terminal, int auth_code) {
        this.terminal = terminal;
        this.auth_code = auth_code;
    }

    public static PAYableRequest from(String data) {
        try {
            return new Gson().fromJson(data, PAYableRequest.class);
        } catch (JsonSyntaxException ex) {
            return null;
        }
    }

    public static String stop() {
        PAYableRequest request = new PAYableRequest();
        request.endpoint = ENDPOINT_STOP;
        return request.toJson();
    }

    public static String stop(String terminal) {
        PAYableRequest request = new PAYableRequest();
        request.endpoint = ENDPOINT_STOP;
        request.terminal = terminal;
        return request.toJson();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public boolean isExternal() {
        return external != null && external == true;
    }
}
