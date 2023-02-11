package com.github.liamdev06.discbots.tickety.utility.exceptions;

public class DatabaseLoadException extends Exception {

    /**
     * The Database failed to startup / load in properly
     *
     * @param message error message / cause
     */
    public DatabaseLoadException(String message) {
        super(message);
    }

}
