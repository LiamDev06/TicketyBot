package me.liamhbest.tickety.database;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Transient;
import me.liamhbest.tickety.database.enums.TicketType;
import me.liamhbest.tickety.database.objects.TicketMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity("tickets")
public class Ticket {

    @Id
    private final @NonNull String id;

    @Transient
    private @NonNull Database database;

    // Values
    private final long userCreatorId;
    private long ticketChannelId;
    private final @NonNull String ticketType;
    private final @NonNull List<TicketMessage> messageHistory = new ArrayList<>();

    public Ticket(@NonNull Database database, @NonNull String id, long userCreatorId, TicketType ticketType) {
        this.database = database;
        this.id = id;
        this.userCreatorId = userCreatorId;
        this.ticketType = ticketType.name();
    }

    public void setDatabase(@NonNull Database database) {
        this.database = database;
    }

    public void save() {
        this.database.saveTicket(this);
    }

    public @NonNull String getId() {
        return this.id;
    }

    public long getUserCreatorId() {
        return this.userCreatorId;
    }

    public TicketType getTicketType() {
        return TicketType.valueOf(this.ticketType);
    }

    public @NonNull List<TicketMessage> getMessageHistory() {
        return this.messageHistory;
    }

    public void addTicketMessage(TicketMessage ticketMessage) {
        this.messageHistory.add(ticketMessage);
    }

    public long getTicketChannelId() {
        return this.ticketChannelId;
    }

    public Ticket setTicketChannelId(long ticketChannelId) {
        this.ticketChannelId = ticketChannelId;
        return this;
    }
}












