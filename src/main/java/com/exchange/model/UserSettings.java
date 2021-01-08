package com.exchange.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "user_settings", schema = "public", catalog = "exchange_rate")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class UserSettings implements Cloneable {
    private int id;
    private int userId;
    private Map<String, Boolean> currencyCode = Map.copyOf(Map.ofEntries(
            Map.entry("CHF", false),
            Map.entry("KZT", false),
            Map.entry("ZAR", false),
            Map.entry("INR", false),
            Map.entry("CNY", false),
            Map.entry("UZS", false),
            Map.entry("AUD", false),
            Map.entry("KRW", false),
            Map.entry("JPY", false),
            Map.entry("PLN", false),
            Map.entry("GBP", false),
            Map.entry("MDL", false),
            Map.entry("BYN", false),
            Map.entry("AMD", false),
            Map.entry("HUF", false),
            Map.entry("TRY", false),
            Map.entry("TJS", false),
            Map.entry("HKD", false),
            Map.entry("EUR", false),
            Map.entry("DKK", false),
            Map.entry("USD", false),
            Map.entry("CAD", false),
            Map.entry("BGN", false),
            Map.entry("NOK", false),
            Map.entry("RON", false),
            Map.entry("SGD", false),
            Map.entry("AZN", false),
            Map.entry("CZK", false),
            Map.entry("KGS", false),
            Map.entry("SEK", false),
            Map.entry("TMT", false),
            Map.entry("BRL", false),
            Map.entry("UAH", false),
            Map.entry("XDR", false)
    ));
    private Map<String, String> currencyValue = Map.of();

    public UserSettings() {}

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "public.user_settings_id_seq")
    public int getId() {
        return id;
    }

    public UserSettings setId(int id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "user_id")
    public int getUserId() {
        return userId;
    }

    public UserSettings setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    @Basic
    @Type(type = "jsonb")
    @Column(name = "currency_code", length = 546)
    public Map<String, Boolean> getCurrencyCode() {
        return currencyCode;
    }

    public UserSettings setCurrencyCode(Map<String, Boolean> currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    @Basic
    @Type(type = "jsonb")
    @Column(name = "currency_value")
    public Map<String, String> getCurrencyValue() {
        return currencyValue;
    }

    public UserSettings setCurrencyValue(Map<String, String> currencyValue) {
        this.currencyValue = currencyValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSettings userSettings = (UserSettings) o;
        return id == userSettings.id && userId == userSettings.userId && currencyCode.equals(userSettings.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, currencyCode);
    }

    @Override
    public UserSettings clone() throws CloneNotSupportedException {
        return (UserSettings) super.clone();
    }
}
