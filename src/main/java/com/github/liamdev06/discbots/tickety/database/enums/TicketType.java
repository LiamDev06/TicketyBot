package com.github.liamdev06.discbots.tickety.database.enums;

import com.github.liamdev06.discbots.tickety.TicketyBot;

public enum TicketType {

    SUPPORT("Support", TicketyBot.get().getConfig().getSupportTicket().getRoleId()),
    APPEAL("Appeal", TicketyBot.get().getConfig().getAppealTicket().getRoleId()),
    BUY("Buy", TicketyBot.get().getConfig().getBuyTicket().getRoleId());

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
