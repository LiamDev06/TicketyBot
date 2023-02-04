package me.liamhbest.tickety.listeners;

import me.liamhbest.tickety.TicketyBot;
import me.liamhbest.tickety.database.Database;
import me.liamhbest.tickety.database.Ticket;
import me.liamhbest.tickety.managers.ActivityManager;
import me.liamhbest.tickety.utility.BotConfig;
import me.liamhbest.tickety.utility.Common;
import me.liamhbest.tickety.utility.exceptions.TicketCreationException;
import me.liamhbest.tickety.utility.exceptions.TicketDeleteException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class ButtonListener extends ListenerAdapter {

    private final @NonNull BotConfig config;
    private final @NonNull ActivityManager activityManager;
    private final @NonNull Database database;

    public ButtonListener(TicketyBot bot) {
        this.config = bot.getConfig();
        this.activityManager = bot.getActivityManager();
        this.database = bot.getDatabase();
    }

    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        if (!(event.getChannel() instanceof TextChannel)) {
            return;
        }

        final TextChannel channel = (TextChannel) event.getChannel();
        final String id = event.getButton().getId();
        final User user = event.getUser();
        final Guild guild = event.getGuild();

        if (id == null || guild == null) {
            this.reply(event, "Something went wrong when trying to open a ticket! Please report this.", Color.RED);
            return;
        }

        try {
            String channelId = channel.getId();

            // Support ticket creation
            if (id.equals("support-ticket-button")) {
                Common.createTicket(this.database, this.activityManager, this.config, user, guild);
                this.reply(event, "A new support ticket was opened at <#" + channelId + ">", Color.GREEN);
            }

            // Appeal ticket creation
            if (id.equals("appeal-ticket-button")) {
                Common.createTicket(this.database, this.activityManager, this.config, user, guild);
                this.reply(event, "A new appeal ticket was opened at <#" + channelId + ">", Color.GREEN);
            }

            // Buy ticket creation
            if (id.equals("buy-ticket-button")) {
                Common.createTicket(this.database, this.activityManager, this.config, user, guild);
                this.reply(event, "A new buy ticket was opened at <#" + channelId + ">", Color.GREEN);
            }
        } catch (TicketCreationException exception) {
            exception.printStackTrace();
            this.reply(event, "Something went wrong when trying to open a ticket! Please report this.", Color.RED);
        }

        // Close ticket button
        if (id.equals("close-ticket-button")) {
            try {
                this.closeTicket(event, guild, channel);
            } catch (TicketDeleteException exception) {
                this.reply(event, "Something went wrong when trying to delete a ticket! Please report this.", Color.RED);
            }
        }
    }

    private void closeTicket(ButtonInteractionEvent event, Guild guild, TextChannel channel) throws TicketDeleteException {
        String ticketId = channel.getName()
                .replace("support-", "")
                .replace("appeal-", "")
                .replace("buy-", "");

        event.reply("")
                .addEmbeds(Common.embed("**Delete!** Deleting this ticket in 3 seconds...", Color.YELLOW))
                .queue();
        this.activityManager.removeChannel(channel);

        // Find the ticket
        Ticket ticket = this.database.getTicket(ticketId);
        if (ticket == null) {
            throw new TicketDeleteException("Could not delete this ticket as the ticket database file is null!");
        }

        // Get the transcript channel
        TextChannel transcriptChannel = guild.getTextChannelById(this.config.getTicketTranscriptChannelId());
        if (transcriptChannel != null) {
            try {
                // Create the file transcript
                File file = Common.createTranscriptFile(guild.getJDA(), ticket, this.config);
                InputStream stream = new FileInputStream(file);
                FileUpload fileUpload = FileUpload.fromData(stream, "ticket_transcript_" + ticketId + ".log");

                // Send the file
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(Color.CYAN)
                        .setTitle("Ticket Transcript")
                        .appendDescription("Click the file to download the ticket transcript for ticket **" + ticketId + "**.");
                transcriptChannel.sendMessageEmbeds(embed.build()).addFiles(fileUpload).queue();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        // Delete the channel and ticket
        channel.delete().queueAfter(3, TimeUnit.SECONDS);
        this.database.deleteTicket(ticket);
    }

    private void reply(ButtonInteractionEvent event, String content, Color color) {
        event.deferReply(true).addEmbeds(Common.embed(content, color)).queue();
    }
}








