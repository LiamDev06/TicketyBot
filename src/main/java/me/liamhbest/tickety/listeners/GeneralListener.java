package me.liamhbest.tickety.listeners;

import me.liamhbest.tickety.TicketyBot;
import me.liamhbest.tickety.database.Database;
import me.liamhbest.tickety.database.Ticket;
import me.liamhbest.tickety.database.objects.TicketMessage;
import me.liamhbest.tickety.managers.ActivityManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GeneralListener extends ListenerAdapter {

    private final ActivityManager activityManager;
    private final Database database;

    public GeneralListener(TicketyBot bot) {
        this.activityManager = bot.getActivityManager();
        this.database = bot.getDatabase();
    }

    // Listens for when reactions are added
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getChannel() instanceof TextChannel) {
            TextChannel channel = (TextChannel) event.getChannel();
            if (!channelIsValid(channel)) return;

            // Activity found in the channel, update activity time
            activityManager.updateActivity(channel);
        }
    }

    // Listens for when reactions are removed
    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getChannel() instanceof TextChannel) {
            TextChannel channel = (TextChannel) event.getChannel();
            if (!channelIsValid(channel)) return;

            // Activity found in the channel, update activity time
            activityManager.updateActivity(channel);
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel() instanceof TextChannel) {
            TextChannel channel = (TextChannel) event.getChannel();
            if (!channelIsValid(channel)) return;
            Ticket ticket = getTicketFromChannel(channel);
            User author = event.getAuthor();
            Message message = event.getMessage();

            if (!author.isSystem() && !author.isBot() && !message.isWebhookMessage()) {
                // Add the message as a TicketMessage for history
                TicketMessage ticketMessage = new TicketMessage(event.getMessageId(),
                        message.getContentStripped(),
                        author.getIdLong());
                ticket.addTicketMessage(ticketMessage);

                // Activity found in the channel, update activity time
                activityManager.updateActivity(channel);
            }
        }
    }

    private boolean channelIsValid(TextChannel channel) {
        return getTicketFromChannel(channel) != null;
    }

    private Ticket getTicketFromChannel(TextChannel channel) {
        return database.getTicketFromChannelId(channel.getIdLong());
    }
}















