package me.liamhbest.tickety.listeners;

import me.liamhbest.tickety.TicketyBot;
import me.liamhbest.tickety.database.Database;
import me.liamhbest.tickety.database.Ticket;
import me.liamhbest.tickety.database.objects.TicketMessage;
import me.liamhbest.tickety.managers.ActivityManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.checkerframework.checker.nullness.qual.NonNull;

public class GeneralListener extends ListenerAdapter {

    private final @NonNull ActivityManager activityManager;
    private final @NonNull Database database;

    public GeneralListener(TicketyBot bot) {
        this.activityManager = bot.getActivityManager();
        this.database = bot.getDatabase();
    }

    @Override
    public void onMessageReactionAdd(@NonNull MessageReactionAddEvent event) {
        this.updateActivity(event.getChannel());
    }

    // Listens for when reactions are removed
    @Override
    public void onMessageReactionRemove(@NonNull MessageReactionRemoveEvent event) {
        this.updateActivity(event.getChannel());
    }

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        if (event.getChannel() instanceof TextChannel) {
            final TextChannel channel = (TextChannel) event.getChannel();
            final User author = event.getAuthor();
            final Message message = event.getMessage();

            // Check if the message can be considered as activity
            if (this.isChannelInvalid(channel) || author.isSystem() || author.isBot() || message.isWebhookMessage()) {
                return;
            }

            // Add the message as a TicketMessage for history
            this.getTicketFromChannel(channel)
                    .addTicketMessage(new TicketMessage(event.getMessageId(), message.getContentStripped(), author.getIdLong()));

            // Update the activity
            this.activityManager.updateActivity(channel);
        }
    }

    private void updateActivity(MessageChannelUnion channel) {
        if (channel instanceof TextChannel) {
            TextChannel textChannel = (TextChannel) channel;

            // The channel is not a ticket channel
            if (this.isChannelInvalid(textChannel)) {
                return;
            }

            // Activity found in the channel, update activity time
            this.activityManager.updateActivity(textChannel);
        }
    }

    private boolean isChannelInvalid(TextChannel channel) {
        return this.getTicketFromChannel(channel) == null;
    }

    private Ticket getTicketFromChannel(TextChannel channel) {
        return this.database.getTicketFromChannelId(channel.getIdLong());
    }
}















