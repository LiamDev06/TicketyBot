package me.liamhbest.tickety.managers;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class ActivityManager {

    //          Channel ID - Last Activity Millis
    private final Map<Long, Long> lastActivity;

    public ActivityManager() {
        this.lastActivity = new HashMap<>();
    }

    public void updateActivity(TextChannel channel) {
        lastActivity.put(channel.getIdLong(), System.currentTimeMillis());
    }

    public long getLastActivity(TextChannel channel) {
        return lastActivity.get(channel.getIdLong());
    }

    public void removeChannel(TextChannel channel) {
        lastActivity.remove(channel.getIdLong());
    }

    public void removeChannel(long id) {
        lastActivity.remove(id);
    }

    public Map<Long, Long> getLastActivity() {
        return lastActivity;
    }
}
