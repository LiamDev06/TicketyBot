package me.liamhbest.tickety.utility.exceptions;

public class TicketDeleteException extends Exception {

    /**
     * A ticket failed to be deleted properly because the ticket database object does not exist
     *
     * @param message error message/cause
     */
    public TicketDeleteException(String message) {
        super(message);
    }
}
