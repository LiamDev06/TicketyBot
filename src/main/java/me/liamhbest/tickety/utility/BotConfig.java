package me.liamhbest.tickety.utility;

public class BotConfig {

    private String token;
    private int activityTimeout; // in seconds, the time to check for no activity and then delete ticket
    private String ticketTranscriptChannelId;
    private String pathToTranscriptsDirectory;
    private Database database;
    private Support support;
    private Appeal appeal;
    private Buy buy;

    // Database information
    public static class Database {
        private String ip, user, password, database;

        public String getIp() {
            return ip;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public String getDatabase() {
            return database;
        }
    }

    // Support ticket information
    public static class Support {
        private String roleId, category;

        public String getRoleId() {
            return roleId;
        }

        public String getCategory() {
            return category;
        }
    }

    // Appeal ticket information
    public static class Appeal {
        private String roleId, category;

        public String getRoleId() {
            return roleId;
        }

        public String getCategory() {
            return category;
        }
    }

    // Buy ticket information
    public static class Buy {
        private String roleId, category;

        public String getRoleId() {
            return roleId;
        }

        public String getCategory() {
            return category;
        }
    }

    public String getToken() {
        return token;
    }

    public Appeal getAppealTicket() {
        return appeal;
    }

    public Support getSupportTicket() {
        return support;
    }

    public Buy getBuyTicket() {
        return buy;
    }

    public int getActivityTimeout() {
        return activityTimeout;
    }

    public String getTicketTranscriptChannelId() {
        return ticketTranscriptChannelId;
    }

    public String getPathToTranscriptsDirectory() {
        return pathToTranscriptsDirectory;
    }

    public Database getDatabase() {
        return database;
    }
}
