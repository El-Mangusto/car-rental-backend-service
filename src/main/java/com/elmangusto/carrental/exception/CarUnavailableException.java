package com.elmangusto.carrental.exception;

public class CarUnavailableException extends ConflictException  {
    public CarUnavailableException(String message) {
        super(message);
    }
}
