package com.chenyang.proxy.common;

/**
 * Created by chenyang on 15-1-28.
 */
public class AgentException extends Exception{
    public int code;
    public String message;

    public AgentException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
