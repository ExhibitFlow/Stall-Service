package com.exhibitflow.stall.service;

public class DuplicateStallCodeException extends RuntimeException {
    public DuplicateStallCodeException(String message) {
        super(message);
    }
}
