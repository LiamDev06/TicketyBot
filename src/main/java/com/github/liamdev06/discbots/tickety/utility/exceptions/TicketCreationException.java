package com.github.liamdev06.discbots.tickety.utility.exceptions;

public class TicketCreationException extends Exception {

    /**
     * A ticket failed to be created properly
     *
     * @param message error message / cause
     */
    public TicketCreationException(String message) {
        super(message);
    }

}
