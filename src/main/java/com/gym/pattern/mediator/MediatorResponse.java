package com.gym.pattern.mediator;

public class MediatorResponse {
    private final boolean success;
    private final String message;
    private final Object payload;

    public MediatorResponse(boolean success, String message) {
        this(success, message, null);
    }

    public MediatorResponse(boolean success, String message, Object payload) {
        this.success = success;
        this.message = message;
        this.payload = payload;
    }

    public static MediatorResponse success(String message) {
        return new MediatorResponse(true, message);
    }

    public static MediatorResponse success(String message, Object payload) {
        return new MediatorResponse(true, message, payload);
    }

    public static MediatorResponse failure(String message) {
        return new MediatorResponse(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getPayload() {
        return payload;
    }
}
