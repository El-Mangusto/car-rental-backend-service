package com.elmangusto.carrental.exception;

public class ResourceAlreadyExistsException extends ConflictException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
