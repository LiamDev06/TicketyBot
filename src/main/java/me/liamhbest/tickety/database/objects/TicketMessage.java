package me.liamhbest.tickety.database.objects;

import dev.morphia.annotations.Embedded;

@Embedded
public class TicketMessage {

    // Id of the message
    private final String messageId;

    // Content of the message
    private final String content;

    // Id of the sender of the ticket
    private final long userId;

    // The timestamp of when the message was sent
    private final long sentTimestamp;

    public TicketMessage(String messageId, String content, long userId) {
        this.messageId = messageId;
        this.content = content;
        this.userId = userId;
        this.sentTimestamp = System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public long getUserId() {
        return userId;
    }

    public String getMessageId() {
        return messageId;
    }

    public long getSentTimestamp() {
        return sentTimestamp;
    }
}
