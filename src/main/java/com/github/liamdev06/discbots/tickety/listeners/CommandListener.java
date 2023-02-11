package com.github.liamdev06.discbots.tickety.listeners;

import com.github.liamdev06.discbots.tickety.utility.Common;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.awt.*;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent event) {
        final String command = event.getName().toLowerCase();

        // Command: /ticket-panel. Send the ticket panel to the designated channel
        if (command.equals("ticket-panel")) {
            this.panel(event);
            return;
        }

        // Command: /shutdown. Safely shutdown the bot
        if (command.equals("shutdown")) {
            this.shutdown(event);
        }
    }

    private void panel(SlashCommandInteractionEvent event) {
        OptionMapping option = event.getOption("channel");
        if (option == null) {
            /*
            Should technically never happen since the argument/option is required
            This is specified when creating the command in TicketyBot#registerCommands
             */

            this.reply(event, "**Something went wrong!** The bot could not fetch your channel option/argument.", Color.RED);
            return;
        }

        GuildChannel channel = option.getAsChannel();
        if (!(channel instanceof TextChannel)) {
            this.reply(event, "**Invalid Channel!** The target channel can only be a text channel.", Color.RED);
            return;
        }

        // Send ticket panel
        MessageEmbed ticketPanel = new EmbedBuilder()
                .setColor(Color.BLUE)
                .setTitle("Ticket Panel")
                .appendDescription("If you need any assistance in any of the following areas, do not hesitate to open a ticket!\n\n" +
                        "**Support** - open a support ticket if you require any assistance or have any questions you'd like to ask.\n" +
                        "**Appeal** - open an appeal ticket if you would like to appeal a punishment.\n" +
                        "**Buy** - open a buy ticket if you would like to purchase any of our products.")
                .build();

        // Create the buttons on the message
        Button supportButton = Button.success("support-ticket-button", "Support");
        Button appealButton = Button.danger("appeal-ticket-button", "Appeal");
        Button buyButton = Button.primary("buy-ticket-button", "Buy");

        // Send the message to the target channel
        TextChannel target = (TextChannel) channel;
        target.sendMessageEmbeds(ticketPanel)
                .setActionRow(supportButton, appealButton, buyButton)
                .queue();

        this.reply(event, "Successfully sent the ticket panel", Color.GREEN);
    }

    private void shutdown(SlashCommandInteractionEvent event) {
        this.reply(event, "Shutting down the bot. Goodbye!", Color.YELLOW);
        event.getJDA().shutdown();
    }

    private void reply(SlashCommandInteractionEvent event, String content, Color color) {
        event.deferReply(true).addEmbeds(Common.embed(content, color)).queue();
    }
}










