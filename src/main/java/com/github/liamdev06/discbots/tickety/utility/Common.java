package com.github.liamdev06.discbots.tickety.utility;

import com.github.liamdev06.discbots.tickety.managers.ActivityManager;
import com.github.liamdev06.discbots.tickety.database.Database;
import com.github.liamdev06.discbots.tickety.database.Ticket;
import com.github.liamdev06.discbots.tickety.database.enums.TicketType;
import com.github.liamdev06.discbots.tickety.database.objects.TicketMessage;
import com.github.liamdev06.discbots.tickety.utility.exceptions.TicketCreationException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.Locale;

public class Common {

    private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Quickly build an Embedded message
     *
     * @param content the full description that can be seen in the embed
     * @param color the color of the embed
     * @return a message embed
     */
    public static MessageEmbed embed(String content, Color color) {
        return new EmbedBuilder()
                .setColor(color)
                .appendDescription(content)
                .build();
    }

    /**
     * Using an algorithm to generate Alphanumeric ticket id with character length 6
     *
     * @return 6 char long randomly generated id
     */
    public static String generateNewTicketId() {
        StringBuilder builder = new StringBuilder();

        // Run the loop 6 times and append a random letter or number
        for (int i = 0; i < 6; i++) {
            int randomIndex = RANDOM.nextInt(ALPHANUMERIC.length());
            builder.append(ALPHANUMERIC.charAt(randomIndex));
        }

        return builder.toString();
    }

    /**
     * Create a transcript file of all messages sent in a ticket. Each log message includes...
     *   - message id
     *   - date and time with time zone
     *   - who sent the message and their current discriminator
     *   - the actual message content
     *
     * @param jda JDA instance
     * @param ticket the database ticket object for which the transcript log is being creator for
     * @param config the bot configuration
     * @return the transcript file with content
     * @throws Exception will be thrown if an IO operation does not function properly
     */
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
            if (user == null) {
                continue;
            }

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

    /**
     * Create a new ticket which includes...
     *   - creating the database ticket document
     *   - creating the ticket channel in the guild
     *   - sending a default ticket information embed which includes a ticket close button
     *
     * @param database the database manager
     * @param activityManager activity manager to set the first the activity
     * @param config the bot configuration
     * @param user the ticket creator
     * @param guild the discord server the ticket was created in
     * @throws TicketCreationException will be thrown if the ticket could not be fully created/properly created
     */
    public static void createTicket(Database database, ActivityManager activityManager, BotConfig config, User user, Guild guild) throws TicketCreationException {
        if (guild == null) {
            throw new TicketCreationException("The guild cannot be null!");
        }

        // Create ticket
        String ticketId = Common.generateNewTicketId();
        Ticket ticket = new Ticket(database, ticketId, user.getIdLong(), TicketType.SUPPORT);
        TicketType type = ticket.getTicketType();
        String displayName = type.getDisplayName();

        // Create channel
        final long creatorId = ticket.getUserCreatorId();
        final Member member = guild.getMemberById(creatorId);
        if (member == null) {
            database.deleteTicket(ticket);
            throw new TicketCreationException("Could not find a ticket creation member with ID " + creatorId + " in this discord!");
        }

        final Role role = guild.getRoleById(type.getRoleId());
        if (role == null) {
            database.deleteTicket(ticket);
            throw new TicketCreationException("Could not find a role with the specified config id!");
        }

        String categoryId;
        if (type == TicketType.APPEAL) {
            categoryId = config.getAppealTicket().getCategory();
        } else if (type == TicketType.BUY) {
            categoryId = config.getBuyTicket().getCategory();
        } else {
            categoryId = config.getSupportTicket().getCategory();
        }

        TextChannel channel = guild.createTextChannel(displayName.toLowerCase() + "-" + ticketId, guild.getCategoryById(categoryId))
                .setNSFW(false)
                .setTopic(displayName + " ticket for " + member.getEffectiveName())
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_ATTACH_FILES), null)
                .addPermissionOverride(role, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_ATTACH_FILES), null)
                .complete();

        // Add to last activity and set channel id
        activityManager.updateActivity(channel);
        ticket.setTicketChannelId(channel.getIdLong()).save();

        // Messages
        MessageEmbed embed = new EmbedBuilder().setColor(Color.GREEN)
                .setTitle(displayName + " Ticket")
                .appendDescription("Thank you for creating a " + displayName.toLowerCase() + " ticket. A member of our staff will shortly be with you.")
                .build();

        Button closeTicketButton = Button.primary("close-ticket-button", "Close");
        channel.sendMessage("<@&" + ticket.getTicketType().getRoleId() + "> <@" + ticket.getUserCreatorId() + ">")
                .addEmbeds(embed)
                .addActionRow(closeTicketButton).queue();
    }
}









