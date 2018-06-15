package com.github.nexus.key.exception;

import com.github.nexus.exception.NexusException;

public class KeyNotFoundException extends NexusException {

    public KeyNotFoundException(String message) {
        super(message);
    }

    public KeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyNotFoundException(Throwable cause) {
        super(cause);
    }
}
