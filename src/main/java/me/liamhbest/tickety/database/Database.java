package me.liamhbest.tickety.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import me.liamhbest.tickety.utility.BotConfig;
import me.liamhbest.tickety.utility.exceptions.DatabaseLoadException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Database {

    private final @NonNull Datastore datastore;
    private final @NonNull Map<String, Ticket> ticketsCache;
    private final @NonNull MongoClient mongoClient;

    public Database(BotConfig config) throws DatabaseLoadException {
        final Logger log = Logger.getLogger("Database");
        log.info("Connecting to the database...");

        // Creating caches
        this.ticketsCache = new HashMap<>();

        // Setup connection string
        BotConfig.Database dbConfig = config.getDatabase();
        String connectionUri = String.format(
                "mongodb+srv://%s:%s@%s/?retryWrites=true&w=majority",
                dbConfig.getUser(),
                dbConfig.getPassword(),
                dbConfig.getIp());
        ConnectionString connectionString = new ConnectionString(connectionUri);

        try {
            // Connect to database
            MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
            this.mongoClient = MongoClients.create(clientSettings);

            // Setting up datastore with Morphia
            this.datastore = Morphia.createDatastore(this.mongoClient, config.getDatabase().getDatabase());

            // Map models to collections
            this.datastore.getMapper().map(Ticket.class);
            this.datastore.ensureIndexes();
        } catch (Exception exception) {
            throw new DatabaseLoadException("Error while connecting to the Mongo database!");
        } finally {
            log.info("Successfully connected to the database!");
        }
    }

    /**
     * Will try to get the ticket from cache, if the ticket does not exist there
     * the system will load the ticket from the database
     *
     * @param id the id of the ticket to get
     * @return Ticket - ticket class that holds all ticket related information and methods
     */
    public Ticket getTicket(String id) {
        // Check if the ticket exists in the cache
        if (this.ticketsCache.containsKey(id)) {
            this.ticketsCache.get(id);
        }

        // Find the ticket in the database
        Query<Ticket> query = this.datastore.find(Ticket.class);
        Ticket ticket = query.filter(Filters.eq("id", id)).first();

        // Return the ticket if it exists
        if (ticket != null) {
            ticket.setDatabase(this);
            this.ticketsCache.put(id, ticket);
            return ticket;
        }

        return null;
    }

    /**
     * Will try to get the ticket from cache, if the ticket does not exist there
     * the system will load the ticket from the database
     *
     * @param channelId the id of the channel the ticket belongs to
     * @return Ticket - ticket class that holds all ticket related information and methods
     */
    public Ticket getTicketFromChannelId(long channelId) {
        // Check if the ticket exists in the cache
        for (Ticket ticket : this.ticketsCache.values()) {
            if (ticket.getTicketChannelId() == channelId) {
                return ticket;
            }
        }

        // Find the ticket in the database
        Query<Ticket> query = this.datastore.find(Ticket.class);
        Ticket ticket = query.filter(Filters.eq("ticketChannelId", channelId)).first();

        // Return the ticket if it exists
        if (ticket != null) {
            ticket.setDatabase(this);
            ticketsCache.put(ticket.getId(), ticket);
            return ticket;
        }

        return null;
    }

    /**
     * Save a ticket to the database
     * @param ticket the ticket to save
     */
    public void saveTicket(Ticket ticket) {
        this.datastore.save(ticket);
    }

    /**
     * Saves all tickets in the cache to the database
     */
    public void saveAllTickets() {
        for (Ticket ticket : this.ticketsCache.values()) {
            this.saveTicket(ticket);
        }
    }

    /**
     * Will get all the tickets from the tickets collection in the database
     * and return them as a list
     *
     * @return ticket collection in the database as a list
     */
    public List<Ticket> getTickets() {
        // Load the collection from the database
        MongoCollection<Ticket> collection = this.datastore.getMapper().getCollection(Ticket.class);
        List<Ticket> list = collection.find().into(new ArrayList<>());

        // Cache the result
        for (Ticket target : list) {
            this.ticketsCache.put(target.getId(), target);
        }

        return list;
    }

    /**
     * Deletes a ticket from the database and removes it from cache
     *
     * @param ticket the ticket object to delete
     */
    public void deleteTicket(Ticket ticket) {
        this.ticketsCache.remove(ticket.getId());
        this.datastore.delete(ticket);
    }

    public void shutdown() {
        this.saveAllTickets();
        this.mongoClient.close();
    }
}










