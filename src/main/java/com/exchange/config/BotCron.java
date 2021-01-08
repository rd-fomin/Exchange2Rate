package com.exchange.config;

public class BotCron {
    private final String cronGetUpdate;
    private final String cronSendNotify;

    public BotCron(String cronGetUpdate, String cronSendNotify) {
        this.cronGetUpdate = cronGetUpdate;
        this.cronSendNotify = cronSendNotify;
    }

    public String getCronGetUpdate() {
        return cronGetUpdate;
    }

    public String getCronSendNotify() {
        return cronSendNotify;
    }

}
