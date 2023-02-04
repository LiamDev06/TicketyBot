package me.liamhbest.tickety.managers;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.dv8tion.jda.api.entities.TextChannel;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ActivityManager {

    /**
     * Key: Channel id
     * Value: Last activity timestamp (in milliseconds)
     */
    private final @NonNull Long2LongMap lastActivity; // Avoid boxing by using fastutil's Long2LongMap

    public ActivityManager() {
        this.lastActivity = new Long2LongOpenHashMap();
    }

    public void updateActivity(TextChannel channel) {
        this.lastActivity.put(channel.getIdLong(), System.currentTimeMillis());
    }

    public long getLastActivity(long channelId) {
        return this.lastActivity.get(channelId);
    }

    public void removeChannel(TextChannel channel) {
        this.lastActivity.remove(channel.getIdLong());
    }

    public @NonNull Long2LongMap getLastActivity() {
        return this.lastActivity;
    }
}
