package me.liamhbest.tickety.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.Mapper;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import me.liamhbest.tickety.utility.BotConfig;
import me.liamhbest.tickety.utility.exceptions.DatabaseLoadException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Database {

    private final Datastore datastore;
    private final Logger log = Logger.getLogger("Database");
    private Map<String, Ticket> ticketsCache;
    private final MongoClient mongoClient;

    public Database(BotConfig config) throws DatabaseLoadException {
        log.info("Starting database module...");

        // Creating caches
        this.ticketsCache = new HashMap<>();

        // Setup connection string
        String connectionUri = "mongodb+srv://" +
                config.getDatabase().getUser() + ":" + config.getDatabase().getPassword() + "@"
                + config.getDatabase().getIp() + "/?retryWrites=true&w=majority";
        ConnectionString connectionString = new ConnectionString(connectionUri);

        try {
            // Connect to database
            MongoClientSettings clientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString).build();
            this.mongoClient = MongoClients.create(clientSettings);

            // Setting up datastore with Morphia
            this.datastore = Morphia.createDatastore(mongoClient, config.getDatabase().getDatabase());

            // Map models to collections
            Mapper mapper = datastore.getMapper();
            mapper.map(Ticket.class);
            datastore.ensureIndexes();
        } catch (Exception exception) {
            throw new DatabaseLoadException("Error while handling to MongoDB. Could not connect!");
        }

        log.info("Successfully connected to MongoDB!");
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
        if (ticketsCache.containsKey(id)) return ticketsCache.get(id);

        // Find the ticket in the database
        Query<Ticket> query = datastore.find(Ticket.class);
        Ticket ticket = query.filter(Filters.eq("id", id)).first();

        // Return the ticket if it exists
        if (ticket != null) {
            ticket.setDatabaseInstance(this);

            // Cache the ticket
            ticketsCache.put(id, ticket);
            return ticket;
        }

        // Return null, the ticket does not exist at all
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
        for (Ticket ticket : ticketsCache.values()) {
            if (ticket.getTicketChannelId() == channelId) return ticket;
        }

        // Find the ticket in the database
        Query<Ticket> query = datastore.find(Ticket.class);
        Ticket ticket = query.filter(Filters.eq("ticketChannelId", channelId)).first();

        // Return the ticket if it exists
        if (ticket != null) {
            ticket.setDatabaseInstance(this);

            // Cache the ticket
            ticketsCache.put(ticket.getId(), ticket);
            return ticket;
        }

        // Return null, the ticket does not exist at all
        return null;
    }

    /**
     * Save a ticket to the database
     * @param ticket the ticket to save
     */
    public void saveTicket(Ticket ticket) {
        datastore.save(ticket);
    }

    /**
     * Saves all tickets in the cache to the database
     */
    public void saveAllTickets() {
        for (Ticket ticket : this.ticketsCache.values()) {
            saveTicket(ticket);
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
        MongoCollection<Ticket> collection = datastore.getMapper().getCollection(Ticket.class);
        List<Ticket> list = collection.find().into(new ArrayList<>());

        // Cache the result
        for (Ticket target : list) {
            ticketsCache.put(target.getId(), target);
        }

        // Return the list
        return list;
    }

    /**
     * Deletes a ticket from the database and removes it from cache
     *
     * @param ticket the ticket object to delete
     */
    public void deleteTicket(Ticket ticket) {
        ticketsCache.remove(ticket.getId());
        datastore.delete(ticket);
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public Map<String, Ticket> getTicketsCache() {
        return ticketsCache;
    }

    public void shutdown() {
        // Save everything
        saveAllTickets();

        // Cache set to null
        this.ticketsCache = null;

        // Close
        mongoClient.close();
    }
}










