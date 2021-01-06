package org.payable.ecr;

import com.google.gson.Gson;

public class PAYableResponse {

    public static final String STATUS_FAILED = "STATUS_FAILED";
    public static final String STATUS_SUCCESS = "STATUS_SUCCESS";
    public static final String STATUS_NOT_LOGIN = "STATUS_NOT_LOGIN";
    public static final String STATUS_INVALID_AMOUNT = "STATUS_INVALID_AMOUNT";
    public static final String STATUS_API_UNREACHABLE = "STATUS_API_UNREACHABLE";
    public static final String STATUS_BUSY = "STATUS_BUSY";
    public static final String STATUS_TERMINAL_AUTHORIZED = "STATUS_TERMINAL_AUTHORIZED";

    public PAYableRequest request;
    public String origin;
    public String status;
    public Long txid;
    public String mid;
    public String tid;
    public String transaction_type;
    public String card_name;
    public String card_no;
    public String card_type;
    public String approval_code;
    public String server_time;
    public String error;

    public PAYableResponse(PAYableRequest request) {
        this.request = request;
    }

    public static PAYableResponse from(String data) {
        return new Gson().fromJson(data, PAYableResponse.class);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
