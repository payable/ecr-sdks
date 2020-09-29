package org.payable.ecr;

import com.google.gson.Gson;

public class PAYableRequest {

    public static final String METHOD_ANY = "ANY";
    public static final String METHOD_CARD = "CARD";
    public static final String METHOD_WALLET = "WALLET";
    public static final String ENDPOINT_PAYMENT = "PAYMENT";
    public static final String ENDPOINT_VOID = "VOID";

    public String endpoint;
    public int id;
    public String origin;
    public double amount;
    public String method;
    public String order_tracking;
    public String receipt_email;
    public String receipt_sms;
    public long txid;

    public PAYableRequest(String endpoint, int id, double amount, String method) {
        this.endpoint = endpoint;
        this.id = id;
        this.amount = amount;
        this.method = method;
    }

    public static PAYableRequest from(String data) {
        return new Gson().fromJson(data, PAYableRequest.class);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
