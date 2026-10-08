package com.example.ProjectManagement.exception;

public class UnauthorizedActionException extends RuntimeException{
    public UnauthorizedActionException(String message){
        super(message);
    }
}
