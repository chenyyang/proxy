package com.chenyang.proxy.common;

/**
 * Created by chenyang on 15-1-28.
 */
public enum AgentErrorCode {

    UNKNOW_ERROR(-1),
    PARAMETER_ERROR(1),
    SERVICE_UNAVAILABLE(2);

    private final int value;

    private AgentErrorCode(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this enum value, as defined in the Thrift IDL.
     */
    public int getValue() {
        return value;
    }
}
