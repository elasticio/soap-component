package io.elastic.soap.exceptions;

public class ComponentException extends RuntimeException {

    public ComponentException(Throwable throwable) {
        super(throwable);
    }

    public ComponentException() {
    }

    public ComponentException(String s) {
        super(s);
    }

    public ComponentException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
