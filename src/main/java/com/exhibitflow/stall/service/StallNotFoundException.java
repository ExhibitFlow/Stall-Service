package com.exhibitflow.stall.service;

public class StallNotFoundException extends RuntimeException {
    public StallNotFoundException(String message) {
        super(message);
    }
}
