package com.stayo.stayo.common.exception;

public class MaxOtpAttemptsExceededException extends RuntimeException{
    public MaxOtpAttemptsExceededException(String message) {
        super(message);
    }
}
