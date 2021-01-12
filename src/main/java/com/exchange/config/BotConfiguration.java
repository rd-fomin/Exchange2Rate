package com.exchange.config;

public class BotConfiguration {
    private final String botUserName;
    private final String botToken;
    private final String botUrl;

    public BotConfiguration(String botUserName, String botToken, String botUrl) {
        this.botUserName = botUserName;
        this.botToken = botToken;
        this.botUrl = botUrl;
    }

    public String getBotUserName() {
        return botUserName;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUrl() {
        return botUrl;
    }

}
