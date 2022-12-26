package me.liamhbest.tickety.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.RandomStringUtils;

import java.awt.*;

public class Common {

    public static MessageEmbed embed(String content, Color color) {
        return new EmbedBuilder().setColor(color).appendDescription(content).build();
    }

    public static String generateNewTicketId() {
        return RandomStringUtils.randomAlphanumeric(6).toLowerCase();
    }

}
