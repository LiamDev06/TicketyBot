package me.liamhbest.tickety.listeners;

import me.liamhbest.tickety.TicketyBot;
import me.liamhbest.tickety.database.Database;
import me.liamhbest.tickety.database.Ticket;
import me.liamhbest.tickety.database.enums.TicketType;
import me.liamhbest.tickety.database.objects.TicketMessage;
import me.liamhbest.tickety.managers.ActivityManager;
import me.liamhbest.tickety.utility.BotConfig;
import me.liamhbest.tickety.utility.Common;
import me.liamhbest.tickety.utility.exceptions.TicketCreationException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ButtonListener extends ListenerAdapter {

    private final BotConfig config;
    private final ActivityManager activityManager;
    private final Database database;

    public ButtonListener(TicketyBot bot) {
        this.config = bot.getConfig();
        this.activityManager = bot.getActivityManager();
        this.database = bot.getDatabase();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Button button = event.getButton();
        User user = event.getUser();
        String id = button.getId();
        if (id == null) return;

        // Guild
        Guild guild = event.getGuild();
        if (guild == null) return;

        // Support ticket creation
        if (id.equalsIgnoreCase("support-ticket-button")) {
            String ticketId = Common.generateNewTicketId();

            // Create ticket
            Ticket ticket = new Ticket(ticketId, user.getIdLong(), TicketType.SUPPORT)
                    .setDatabaseInstance(database).setupLists();

            // Create channel
            TextChannel channel;
            try {
                channel = createTicketChannel(guild, ticket);
            } catch (TicketCreationException exception) {
                exception.printStackTrace();
                return;
            }

            // Add to last activity and set channel id
            activityManager.updateActivity(channel);
            ticket.setTicketChannelId(channel.getIdLong()).save();

            // Messages
            Button closeTicketButton = Button.primary("close-ticket-button", "Close");
            channel.sendMessage("<@&" + ticket.getTicketType().getRoleId() + "> <@" + ticket.getUserCreatorId() + ">")
                    .addEmbeds(defaultTicketEmbed(ticket.getTicketType()))
                    .addActionRow(closeTicketButton).queue();
            event.deferReply(true).setContent("A new support ticket was opened at <#" + channel.getId() + ">").queue();
        }

        // Appeal ticket creation
        if (id.equalsIgnoreCase("appeal-ticket-button")) {
            String ticketId = Common.generateNewTicketId();

            // Create ticket
            Ticket ticket = new Ticket(ticketId, user.getIdLong(), TicketType.APPEAL)
                    .setDatabaseInstance(database).setupLists();

            // Create channel
            TextChannel channel;
            try {
                channel = createTicketChannel(guild, ticket);
            } catch (TicketCreationException exception) {
                exception.printStackTrace();
                return;
            }

            // Add to last activity and set channel id
            activityManager.updateActivity(channel);
            ticket.setTicketChannelId(channel.getIdLong()).save();

            // Messages
            Button closeTicketButton = Button.primary("close-ticket-button", "Close");
            channel.sendMessage("<@&" + ticket.getTicketType().getRoleId() + "> <@" + ticket.getUserCreatorId() + ">")
                    .addEmbeds(defaultTicketEmbed(ticket.getTicketType()))
                    .addActionRow(closeTicketButton).queue();
            event.deferReply(true).setContent("A new appeal ticket was opened at <#" + channel.getId() + ">").queue();
        }

        // Buy ticket creation
        if (id.equalsIgnoreCase("buy-ticket-button")) {
            String ticketId = Common.generateNewTicketId();

            // Create ticket
            Ticket ticket = new Ticket(ticketId, user.getIdLong(), TicketType.BUY)
                    .setDatabaseInstance(database).setupLists();

            // Create channel
            TextChannel channel;
            try {
                channel = createTicketChannel(guild, ticket);
            } catch (TicketCreationException exception) {
                exception.printStackTrace();
                return;
            }

            // Add to last activity and set channel id
            activityManager.updateActivity(channel);
            ticket.setTicketChannelId(channel.getIdLong()).save();

            // Messages
            Button closeTicketButton = Button.primary("close-ticket-button", "Close");
            channel.sendMessage("<@&" + ticket.getTicketType().getRoleId() + "> <@" + ticket.getUserCreatorId() + ">")
                    .addEmbeds(defaultTicketEmbed(ticket.getTicketType()))
                    .addActionRow(closeTicketButton).queue();
            event.deferReply(true).setContent("A new buy ticket was opened at <#" + channel.getId() + ">").queue();
        }

        // Close ticket button
        if (id.equalsIgnoreCase("close-ticket-button")) {
            TextChannel channel = (TextChannel) event.getGuildChannel();
            String ticketId = channel.getName().replace("support-", "").replace("appeal-", "").replace("buy-", "");
            event.reply("").addEmbeds(Common.embed("**Delete!** Deleting this ticket in 3 seconds...", Color.YELLOW)).queue();
            activityManager.removeChannel(channel);

            // Find the ticket
            Ticket ticket = database.getTicket(ticketId);
            if (ticket == null) {
                channel.sendMessageEmbeds(Common.embed("Could not delete this ticket as the ticket database file is null!", Color.RED)).queue();
                return;
            }

            // Get the transcript channel
            TextChannel transcriptChannel = guild.getTextChannelById(config.getTicketTranscriptChannelId());

            if (transcriptChannel != null) {
                try {
                    // Create the file transcript
                    File file = createTranscriptFile(guild.getJDA(), ticket, config);
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

    public TextChannel createTicketChannel(Guild guild, Ticket ticket) throws TicketCreationException {
        TicketType ticketType = ticket.getTicketType();
        String name = ticketType.getDisplayName();
        Member member = guild.getMemberById(ticket.getUserCreatorId());
        if (member == null) throw new TicketCreationException("Could not find a ticket creation member with ID " + ticket.getUserCreatorId() + " in this discord!");

        Role role = guild.getRoleById(ticketType.getRoleId());
        if (role == null) throw new TicketCreationException("Could not find a role with the specified config id!");

        return guild.createTextChannel(name.toLowerCase() + "-" + ticket.getId(),
                        guild.getCategoryById(config.getBuyTicket().getCategory()))
                .setNSFW(false)
                .setTopic(name + " ticket for " + member.getEffectiveName())
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_ATTACH_FILES), null)
                .addPermissionOverride(role, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_ATTACH_FILES), null)
                .complete();
    }

    private MessageEmbed defaultTicketEmbed(TicketType ticketType) {
        String displayName = ticketType.getDisplayName();

        return new EmbedBuilder().setColor(Color.GREEN)
                .setTitle(displayName + " Ticket")
                .appendDescription("Thank you for creating a " + displayName.toLowerCase() + " ticket. A member of our staff will shortly be with you.")
                .build();
    }

    public static File createTranscriptFile(JDA jda, Ticket ticket, BotConfig config) throws Exception {
        File file = new File(config.getPathToTranscriptsDirectory() + "/" + ticket.getId() + ".log");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        FileWriter writer = new FileWriter(file, true);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);

        bufferedWriter.write("This is the transcript log file for " + ticket.getTicketType().getDisplayName() + " ticket id: " + ticket.getId() + ".");
        bufferedWriter.newLine();

        // Loop through all messages in history
        for (TicketMessage ticketMessage : ticket.getMessageHistory()) {
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime now = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(ticketMessage.getSentTimestamp()), zoneId);
            String date = dateFormat.format(now);
            String time = timeFormat.format(now);

            // User
            User user = jda.getUserById(ticketMessage.getUserId());
            if (user == null) continue;
            String memberName = user.getName();
            String discriminator = user.getDiscriminator();

            String content = "[" + ticketMessage.getMessageId() + " -==- " + date + " " + time + "-" + zoneId.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + "] "
                    + memberName + "#" + discriminator + ": " + '"' + ticketMessage.getContent() + '"';
            bufferedWriter.newLine();
            bufferedWriter.write(content);
            bufferedWriter.flush();
        }

        writer.close();
        bufferedWriter.close();
        return file;
    }
}








