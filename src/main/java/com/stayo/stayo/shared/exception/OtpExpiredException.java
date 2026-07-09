package com.stayo.stayo.shared.exception;

public class OtpExpiredException extends RuntimeException{
    public OtpExpiredException(String message){
        super(message);
    }
}
