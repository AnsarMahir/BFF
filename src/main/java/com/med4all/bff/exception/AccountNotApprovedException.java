// exception/AccountNotApprovedException.java
package com.med4all.bff.exception;

public class AccountNotApprovedException extends RuntimeException {
    public AccountNotApprovedException(String message) {
        super(message);
    }
}