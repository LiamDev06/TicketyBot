package com.github.liamdev06.discbots.tickety.database.objects;

import dev.morphia.annotations.Embedded;
import org.checkerframework.checker.nullness.qual.NonNull;

@Embedded
public class TicketMessage {

    private final @NonNull String messageId;
    private final String content;
    private final long userId, sentTimestamp;

    public TicketMessage(@NonNull String messageId, String content, long userId) {
        this.messageId = messageId;
        this.content = content;
        this.userId = userId;
        this.sentTimestamp = System.currentTimeMillis();
    }

    public String getContent() {
        return this.content;
    }

    public long getUserId() {
        return this.userId;
    }

    public @NonNull String getMessageId() {
        return this.messageId;
    }

    public long getSentTimestamp() {
        return this.sentTimestamp;
    }
}
