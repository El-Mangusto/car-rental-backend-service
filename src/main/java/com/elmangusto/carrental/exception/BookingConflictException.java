package com.elmangusto.carrental.exception;

public class BookingConflictException extends ConflictException  {
    public BookingConflictException(String message) {
        super(message);
    }
}
