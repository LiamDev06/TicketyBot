package me.liamhbest.tickety.listeners;

import me.liamhbest.tickety.utility.Common;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();

        // Ticket-panel command
        if (command.equalsIgnoreCase("ticket-panel")) {
            // Check if channel is text channel
            GuildChannel channel = event.getOption("channel").getAsChannel();
            if (!(channel instanceof TextChannel)) {
                event.deferReply(true)
                        .addEmbeds(Common.embed("**Invalid Channel!** The target channel can only be a text channel.", Color.RED)).queue();
                return;
            }

            // Send ticket panel
            EmbedBuilder ticketPanel = new EmbedBuilder()
                    .setColor(Color.BLUE).setTitle("Ticket Panel")
                    .appendDescription("If you need any assistance in any of the following areas, do not hesitate to open a ticket!\n\n" +
                            "**Support** - open a support ticket if you require any assistance or have any questions you'd like to ask.\n" +
                            "**Appeal** - open an appeal ticket if you would like to appeal a punishment.\n" +
                            "**Buy** - open a buy ticket if you would like to purchase any of our products.");

            // Buttons
            Button supportButton = Button.success("support-ticket-button", "Support");
            Button appealButton = Button.danger("appeal-ticket-button", "Appeal");
            Button buyButton = Button.primary("buy-ticket-button", "Buy");

            TextChannel target = (TextChannel) channel;
            target.sendMessageEmbeds(ticketPanel.build())
                    .setActionRow(supportButton, appealButton, buyButton).queue();

            // Reply
            event.deferReply(true)
                    .addEmbeds(Common.embed("Successfully sent the ticket panel.", Color.GREEN)).queue();
            return;
        }

        // Shutdown command
        if (command.equalsIgnoreCase("shutdown")) {
            event.deferReply(true).addEmbeds(Common.embed("Shutting down the bot. Goodbye!", Color.YELLOW)).queue();
            event.getJDA().shutdown();
        }
    }
}










