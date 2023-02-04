package me.liamhbest.tickety.utility;

public class BotConfig {

    private String token;
    private String ticketTranscriptChannelId;
    private int activityTimeout; // in seconds, the time to check for no activity and then delete ticket
    private String pathToTranscriptsDirectory;
    private Database database;
    private Support support;
    private Appeal appeal;
    private Buy buy;

    // Database information
    public static class Database {
        private String ip, user, password, database;

        public String getIp() {
            return this.ip;
        }

        public String getUser() {
            return this.user;
        }

        public String getPassword() {
            return this.password;
        }

        public String getDatabase() {
            return this.database;
        }
    }

    // Support ticket information
    public static class Support {
        private String roleId, category;

        public long getRoleId() {
            return Long.parseLong(this.roleId);
        }

        public String getCategory() {
            return this.category;
        }
    }

    // Appeal ticket information
    public static class Appeal {
        private String roleId, category;

        public long getRoleId() {
            return Long.parseLong(this.roleId);
        }

        public String getCategory() {
            return this.category;
        }
    }

    // Buy ticket information
    public static class Buy {
        private String roleId, category;

        public long getRoleId() {
            return Long.parseLong(this.roleId);
        }

        public String getCategory() {
            return this.category;
        }
    }

    public String getToken() {
        return this.token;
    }

    public Appeal getAppealTicket() {
        return this.appeal;
    }

    public Support getSupportTicket() {
        return this.support;
    }

    public Buy getBuyTicket() {
        return this.buy;
    }

    public int getActivityTimeout() {
        return this.activityTimeout;
    }

    public String getTicketTranscriptChannelId() {
        return this.ticketTranscriptChannelId;
    }

    public String getPathToTranscriptsDirectory() {
        return this.pathToTranscriptsDirectory;
    }

    public Database getDatabase() {
        return this.database;
    }
}
