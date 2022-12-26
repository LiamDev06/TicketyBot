package me.liamhbest.tickety.database;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Transient;
import me.liamhbest.tickety.database.enums.TicketType;
import me.liamhbest.tickety.database.objects.TicketMessage;

import java.util.ArrayList;
import java.util.List;

@Entity("tickets")
public class Ticket {

    @Id
    private final String id;

    @Transient
    private Database database;

    // Values
    private long userCreatorId;
    private long ticketChannelId;
    private String ticketType;
    private List<TicketMessage> messageHistory = new ArrayList<>();

    // Setup new ticket
    public Ticket(String id, long userCreatorId,TicketType ticketType) {
        this.id = id;
        this.userCreatorId = userCreatorId;
        this.ticketType = ticketType.name();
    }

    // Database Instance
    public Ticket setDatabaseInstance(Database database) {
        this.database = database;
        return this;
    }

    public Database getDatabase() {
        return database;
    }

    // Saving user to database
    public Ticket save() {
        database.saveTicket(this);
        return this;
    }

    public Ticket setupLists() {
        if (messageHistory == null) messageHistory = new ArrayList<>();
        return this;
    }

    public String getId() {
        return id;
    }

    public long getUserCreatorId() {
        return userCreatorId;
    }

    public Ticket setUserCreatorId(long userCreatorId) {
        this.userCreatorId = userCreatorId;
        return this;
    }

    public TicketType getTicketType() {
        return TicketType.valueOf(ticketType);
    }

    public Ticket setTicketType(TicketType type) {
        this.ticketType = type.name();
        return this;
    }

    public List<TicketMessage> getMessageHistory() {
        return messageHistory;
    }

    public Ticket addTicketMessage(TicketMessage ticketMessage) {
        if (this.messageHistory == null) this.messageHistory = new ArrayList<>();
        this.messageHistory.add(ticketMessage);
        return this;
    }

    public long getTicketChannelId() {
        return ticketChannelId;
    }

    public Ticket setTicketChannelId(long ticketChannelId) {
        this.ticketChannelId = ticketChannelId;
        return this;
    }
}












