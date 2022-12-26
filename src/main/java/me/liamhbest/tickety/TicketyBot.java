package me.liamhbest.tickety;

import com.google.gson.Gson;
import me.liamhbest.tickety.database.Database;
import me.liamhbest.tickety.database.Ticket;
import me.liamhbest.tickety.listeners.ButtonListener;
import me.liamhbest.tickety.listeners.CommandListener;
import me.liamhbest.tickety.listeners.GeneralListener;
import me.liamhbest.tickety.managers.ActivityManager;
import me.liamhbest.tickety.utility.BotConfig;
import me.liamhbest.tickety.utility.Common;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TicketyBot extends ListenerAdapter {

    private static TicketyBot instance;
    private final JDA jda;
    private final Logger log = Logger.getLogger("Tickety");
    private final BotConfig config;
    private final ActivityManager activityManager;
    private final Database database;

    public TicketyBot() throws Exception {
        long time = System.currentTimeMillis();
        log.info("Starting Tickety bot...");

        // Loading configuration
        URL url = ClassLoader.getSystemResource("config.json");
        File file = new File(url.toURI());
        Gson gson = new Gson();
        this.config = gson.fromJson(new FileReader(file), BotConfig.class);

        // Init
        this.activityManager = new ActivityManager();
        this.database = new Database(config);

        // Create bot
        this.jda = JDABuilder.createDefault(config.getToken())
                .setActivity(Activity.watching("over tickets"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build().awaitReady();

        // Register commands and listeners
        registerCommands();
        registerListeners();

        // Register all existing channels in the database to the activity manager
        // As bot was shutdown, the timer restarts
        for (Ticket ticket : database.getTickets()) {
            TextChannel channel = jda.getTextChannelById(ticket.getTicketChannelId());
            if (channel == null) continue;

            activityManager.updateActivity(channel);
        }

        // Create executor service to check for ticket activity
        int activityTimeout = config.getActivityTimeout();
        long activityTimeoutMillis = 60_000L * activityTimeout;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable task = new Runnable() {
            public void run() {
                // To get around the ConcurrentModificationException
                List<Long> list = new ArrayList<>(activityManager.getLastActivity().keySet());

                for (long channelId : list) {
                    long time = activityManager.getLastActivity().get(channelId);

                    if (System.currentTimeMillis() > (time + activityTimeoutMillis)) {
                        TextChannel channel = jda.getTextChannelById(channelId);
                        String ticketId = channel.getName().replace("support-", "").replace("appeal-", "").replace("buy-", "");
                        activityManager.removeChannel(channel);

                        // Warn
                        channel.sendMessageEmbeds(
                                        Common.embed("**NO ACTIVITY!** This channel has reached the threshold for no activity, it will now be deleted.", Color.RED))
                                .queue();

                        // Find the ticket
                        Ticket ticket = database.getTicket(ticketId);
                        if (ticket == null) {
                            channel.sendMessageEmbeds(Common.embed("Could not delete this ticket as the ticket database file is null!", Color.RED)).queue();
                            return;
                        }

                        // Get the transcript channel
                        TextChannel transcriptChannel = jda.getTextChannelById(config.getTicketTranscriptChannelId());

                        if (transcriptChannel != null) {
                            try {
                                // Create the file transcript
                                File file = ButtonListener.createTranscriptFile(jda, ticket, config);
                                InputStream stream = new FileInputStream(file);
                                FileUpload fileUpload = FileUpload.fromData(stream, "ticket_transcript_" + ticketId + ".log");

                                // Send the file
                                EmbedBuilder embed = new EmbedBuilder()
                                        .setColor(Color.CYAN).setTitle("Ticket Transcript")
                                        .appendDescription("Click the file to download the ticket transcript for ticket **" + ticketId + "**.");
                                transcriptChannel.sendMessageEmbeds(embed.build())
                                        .addFiles(fileUpload).queue();
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }

                        // Delete the channel
                        channel.delete().queueAfter(3, TimeUnit.SECONDS);

                        // Delete the ticket in the database
                        database.deleteTicket(ticket);
                    }
                }
            }
        };
        executor.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);

        // Done
        log.info("Tickety was successfully started in " + (System.currentTimeMillis() - time) + "ms!");
    }

    public void registerCommands() {
        jda.upsertCommand("ticket-panel", "Send the panel of the ticket for a specific channel.")
                .addOption(OptionType.CHANNEL, "channel", "Specify the channel to send the ticket panel in", true)
                .queue();

        jda.upsertCommand("shutdown", "Shutdown the bot properly.")
                .queue();
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        long time = System.currentTimeMillis();
        log.info("Shutting down Tickety bot...");

        // Shutdown database
        database.shutdown();

        // Done
        log.info("Tickety was successfully started in " + (System.currentTimeMillis() - time) + "ms!");
    }

    public void registerListeners() {
        jda.addEventListener(this, new CommandListener(), new ButtonListener(this), new GeneralListener(this));
    }

    // Java Application start
    public static void main(String[] args) {
        try {
            instance = new TicketyBot();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static TicketyBot getInstance() {
        return instance;
    }

    public JDA getJda() {
        return jda;
    }

    public Logger getLog() {
        return log;
    }

    public BotConfig getConfig() {
        return config;
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    public Database getDatabase() {
        return database;
    }
}













