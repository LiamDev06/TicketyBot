package com.github.liamdev06.discbots.tickety;

import com.github.liamdev06.discbots.tickety.database.Database;
import com.github.liamdev06.discbots.tickety.database.Ticket;
import com.github.liamdev06.discbots.tickety.listeners.ButtonListener;
import com.github.liamdev06.discbots.tickety.listeners.CommandListener;
import com.github.liamdev06.discbots.tickety.listeners.GeneralListener;
import com.github.liamdev06.discbots.tickety.managers.ActivityManager;
import com.github.liamdev06.discbots.tickety.utility.Common;
import com.github.liamdev06.discbots.tickety.utility.exceptions.DatabaseLoadException;
import com.google.gson.Gson;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import com.github.liamdev06.discbots.tickety.utility.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TicketyBot extends ListenerAdapter {

    private static TicketyBot INSTANCE;

    private final @NonNull JDA jda;
    private final @NonNull Logger log = Logger.getLogger("Tickety");
    private final @NonNull BotConfig config;
    private final @NonNull ActivityManager activityManager;
    private final @NonNull Database database;

    public TicketyBot() throws LoginException, IOException, InterruptedException, DatabaseLoadException, URISyntaxException {
        long time = System.currentTimeMillis();
        log.info("Starting Tickety bot...");

        // Loading configuration
        URL url = ClassLoader.getSystemResource("config.json");
        File file = new File(url.toURI());
        this.config = new Gson().fromJson(new FileReader(file), BotConfig.class);

        // Initialization
        this.activityManager = new ActivityManager();
        this.database = new Database(this.config);

        // Create bot
        this.jda = JDABuilder.createDefault(this.config.getToken())
                .setActivity(Activity.watching("over tickets"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build()
                .awaitReady();

        // Register commands and listeners
        this.registerCommands();
        this.registerListeners();

        // Register all existing channels in the database to the activity manager (as the bot was shutdown, timer restarts)
        for (Ticket ticket : this.database.getTickets()) {
            TextChannel channel = this.jda.getTextChannelById(ticket.getTicketChannelId());

            // Update the last activity in the channel
            if (channel != null) {
                this.activityManager.updateActivity(channel);
            }
        }

        // Create executor service to check for ticket activity
        int activityTimeout = this.config.getActivityTimeout();
        long activityTimeoutMillis = 60_000L * activityTimeout;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = new Runnable() {
            public void run() {
                for (long channelId : new LongArrayList(activityManager.getLastActivity().keySet())) {
                    long time = activityManager.getLastActivity(channelId);

                    if (System.currentTimeMillis() > (time + activityTimeoutMillis)) {
                        TextChannel channel = jda.getTextChannelById(channelId);
                        if (channel == null) {
                            continue;
                        }

                        String ticketId = channel.getName()
                                .replace("support-", "")
                                .replace("appeal-", "")
                                .replace("buy-", "");
                        activityManager.removeChannel(channel);

                        // Warn
                        channel.sendMessageEmbeds(Common.embed(
                                "**NO ACTIVITY!** This channel has reached the threshold for no activity, it will now be deleted.",
                                        Color.RED))
                                .queue();

                        // Find the ticket
                        Ticket ticket = database.getTicket(ticketId);
                        if (ticket == null) {
                            channel.sendMessageEmbeds(Common.embed("Could not delete this ticket as the ticket database file is null!", Color.RED)).queue();
                            return;
                        }

                        // Get the transcript channel and create the transcript
                        TextChannel transcriptChannel = jda.getTextChannelById(config.getTicketTranscriptChannelId());
                        if (transcriptChannel != null) {
                            try {
                                Common.createTranscriptFile(jda, ticket, config);
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }

                        // Delete the channel and ticket
                        channel.delete().queueAfter(3, TimeUnit.SECONDS);
                        database.deleteTicket(ticket);
                    }
                }
            }
        };
        executor.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);

        // Done
        log.info("Tickety was successfully started in " + (System.currentTimeMillis() - time) + "ms!");
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        long time = System.currentTimeMillis();
        log.info("Shutting down Tickety bot...");

        // Shutdown database
        this.database.shutdown();

        // Done
        log.info("Tickety was successfully started in " + (System.currentTimeMillis() - time) + "ms!");
    }

    // Java Application start
    public static void main(String[] args) {
        try {
            INSTANCE = new TicketyBot();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void registerListeners() {
        this.jda.addEventListener(
                this,
                new CommandListener(),
                new ButtonListener(this),
                new GeneralListener(this)
        );
    }

    private void registerCommands() {
        this.jda.upsertCommand("ticket-panel", "Send the panel of the ticket for a specific channel.")
                .addOption(OptionType.CHANNEL, "channel", "Specify the channel to send the ticket panel in", true)
                .queue();

        this.jda.upsertCommand("shutdown", "Shutdown the bot properly.")
                .queue();
    }

    public static TicketyBot get() {
        return INSTANCE;
    }

    public @NonNull BotConfig getConfig() {
        return this.config;
    }

    public @NonNull ActivityManager getActivityManager() {
        return this.activityManager;
    }

    public @NonNull Database getDatabase() {
        return this.database;
    }
}













