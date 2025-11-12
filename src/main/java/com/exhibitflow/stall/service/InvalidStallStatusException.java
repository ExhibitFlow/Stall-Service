package com.exhibitflow.stall.service;

public class InvalidStallStatusException extends RuntimeException {
    public InvalidStallStatusException(String message) {
        super(message);
    }
}
