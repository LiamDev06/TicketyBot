package me.liamhbest.tickety.database.enums;

import me.liamhbest.tickety.TicketyBot;
import me.liamhbest.tickety.utility.BotConfig;

public enum TicketType {

    SUPPORT("Support", Long.parseLong(TicketyBot.getInstance().getConfig().getSupportTicket().getRoleId())),
    APPEAL("Appeal", Long.parseLong(TicketyBot.getInstance().getConfig().getAppealTicket().getRoleId())),
    BUY("Buy", Long.parseLong(TicketyBot.getInstance().getConfig().getBuyTicket().getRoleId()));

    private final BotConfig config = TicketyBot.getInstance().getConfig();
    private final String displayName;
    private final long roleId;

    TicketType(String displayName, long roleId) {
        this.displayName = displayName;
        this.roleId = roleId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getRoleId() {
        return roleId;
    }
}
