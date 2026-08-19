package org.task.exceptions;

public class ExternalServiceUnavailableException extends RuntimeException {
    public ExternalServiceUnavailableException(String serviceName, Throwable cause) {
        super(serviceName + " is temporarily unavailable", cause);
    }

    public ExternalServiceUnavailableException(String serviceName, int statusCode) {
        super(serviceName + " returned unavailable status " + statusCode);
    }
}
