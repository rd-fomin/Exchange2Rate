package com.exchange.utils;

import com.exchange.model.ValCurs;
import com.exchange.model.Valute;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BotUtils {
    private static final Logger log = LogManager.getLogger(BotUtils.class);
    private static final Map<String, String> mapWithCharCodes = Map.copyOf(Map.ofEntries(
            Map.entry("CHF", "\uD83C\uDDE8\uD83C\uDDED"),
            Map.entry("KZT", "\uD83C\uDDF0\uD83C\uDDFF"),
            Map.entry("ZAR", "\uD83C\uDDFF\uD83C\uDDE6"),
            Map.entry("INR", "\uD83C\uDDEE\uD83C\uDDF3"),
            Map.entry("CNY", "\uD83C\uDDE8\uD83C\uDDF3"),
            Map.entry("UZS", "\uD83C\uDDFA\uD83C\uDDFF"),
            Map.entry("AUD", "\uD83C\uDDE6\uD83C\uDDFA"),
            Map.entry("KRW", "\uD83C\uDDF0\uD83C\uDDF7"),
            Map.entry("JPY", "\uD83C\uDDEF\uD83C\uDDF5"),
            Map.entry("PLN", "\uD83C\uDDF5\uD83C\uDDF1"),
            Map.entry("GBP", "\uD83C\uDDEC\uD83C\uDDE7"),
            Map.entry("MDL", "\uD83C\uDDF2\uD83C\uDDE9"),
            Map.entry("BYN", "\uD83C\uDDE7\uD83C\uDDFE"),
            Map.entry("AMD", "\uD83C\uDDE6\uD83C\uDDF2"),
            Map.entry("HUF", "\uD83C\uDDED\uD83C\uDDFA"),
            Map.entry("TRY", "\uD83C\uDDF9\uD83C\uDDF7"),
            Map.entry("TJS", "\uD83C\uDDF9\uD83C\uDDEF"),
            Map.entry("HKD", "\uD83C\uDDED\uD83C\uDDF0"),
            Map.entry("EUR", "\uD83C\uDDEA\uD83C\uDDFA"),
            Map.entry("DKK", "\uD83C\uDDE9\uD83C\uDDF0"),
            Map.entry("USD", "\uD83C\uDDFA\uD83C\uDDF8"),
            Map.entry("CAD", "\uD83C\uDDE8\uD83C\uDDE6"),
            Map.entry("BGN", "\uD83C\uDDE7\uD83C\uDDEC"),
            Map.entry("NOK", "\uD83C\uDDF3\uD83C\uDDF4"),
            Map.entry("RON", "\uD83C\uDDF7\uD83C\uDDF4"),
            Map.entry("SGD", "\uD83C\uDDF8\uD83C\uDDEC"),
            Map.entry("AZN", "\uD83C\uDDE6\uD83C\uDDFF"),
            Map.entry("CZK", "\uD83C\uDDE8\uD83C\uDDFF"),
            Map.entry("KGS", "\uD83C\uDDF0\uD83C\uDDEC"),
            Map.entry("SEK", "\uD83C\uDDF8\uD83C\uDDEA"),
            Map.entry("TMT", "\uD83C\uDDF9\uD83C\uDDF2"),
            Map.entry("BRL", "\uD83C\uDDE7\uD83C\uDDF7"),
            Map.entry("UAH", "\uD83C\uDDFA\uD83C\uDDE6"),
            Map.entry("XDR", "\uD83C\uDFF3️")
    ));
    private static final Map<Integer, Map<String, Valute>> users = new HashMap<>();
    private static final Map<Integer, Map<String, Valute>> tempUsers = new HashMap<>();
    private static Map<String, Valute> VALUTES;
    private static final InlineKeyboardMarkup inlineKeyboardMarkup;
    static {
        var valuteList = getValCursToday().orElseThrow().getValutes();
        VALUTES = listToMap(valuteList);
        var keyboardButtons = Lists.partition(valuteList, 2).stream()
                .map(valutes -> valutes.stream()
                        .map(valute -> new InlineKeyboardButton().setCallbackData(valute.getCharCode()))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        keyboardButtons.add(
                List.of(
                        new InlineKeyboardButton().setText("Cancel").setCallbackData("Cancel"),
                        new InlineKeyboardButton().setText("Done").setCallbackData("Done")
                )
        );
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboardButtons);
    }

    private BotUtils() {}

    @Scheduled(cron = "0 5 15 * * ?")
    public static void setVALUTES() {
        VALUTES = listToMap(getValCursToday().orElseThrow().getValutes());
        log.info(VALUTES);
    }

    public static void addOrUpdateUser(int id) {
        addOrUpdateUser(id, VALUTES);
    }

    private static void addOrUpdateUser(int id, Map<String, Valute> valuteList) {
        users.put(id, new TreeMap<>(valuteList));
    }

    public static Map<String, Valute> getValutesForUser(int id) {
        return users.get(id);
    }

    public static void addOrUpdateTempUser(int id) {
        Map<String, Valute> map = new TreeMap<>();
        users.get(id).forEach((key, value) -> {
            try {
                map.put(key, value.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        tempUsers.put(id, map);
    }

    public static Map<String, Valute> getValutesForTempUser(int id) {
        return tempUsers.get(id);
    }

    public static String getFlagUnicode(String charCode) {
        return mapWithCharCodes.get(charCode);
    }

    public static TreeMap<String, Valute> listToMap(List<Valute> valuteList) {
        return valuteList.stream()
                .collect(Collectors.toMap(
                        Valute::getCharCode,
                        Function.identity(),
                        (key, valute) -> valute,
                        TreeMap::new
                ));
    }

    public static String getValuteMapAsString(int id) {
        return users.get(id).values().stream()
                .filter(Valute::isSelected)
                .map(valute -> String.format("%s %s\n", getFlagUnicode(valute.getCharCode()), valute.getCharCode()))
                .collect(Collectors.joining());
    }

    public static void saveSettings(int id) {
        addOrUpdateUser(id, tempUsers.remove(id));
    }

    public static void cancelSettings(int id) {
        tempUsers.remove(id);
    }

    public static InlineKeyboardMarkup updateAndGetKeyboardButtons(int id) {
        inlineKeyboardMarkup.getKeyboard().forEach(inlineKeyboardButtons -> inlineKeyboardButtons
                .forEach(inlineKeyboardButton -> {
                    if (!inlineKeyboardButton.getCallbackData().equals("Done") && !inlineKeyboardButton.getCallbackData().equals("Cancel"))
                        inlineKeyboardButton.setText(String.format("%s %s %s",
                                getFlagUnicode(inlineKeyboardButton.getCallbackData()),
                                inlineKeyboardButton.getCallbackData(),
                                getValutesForTempUser(id).get(inlineKeyboardButton.getCallbackData()).isSelected() ? "✅" : "❌"));
                }));
        return inlineKeyboardMarkup;
    }

    private static Optional<ValCurs> getValCursToday() {
        try {
            URL url = new URL("http://www.cbr.ru/scripts/XML_daily.asp");
            var urlConnection = (HttpURLConnection) url.openConnection();
            var inputStream = urlConnection.getInputStream();
            try (var bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "windows-1251"))) {
                var exchangeRateLine = new StringReader(
                        bufferedReader.lines().collect(Collectors.joining())
                );
                var jaxbContext = JAXBContext.newInstance(ValCurs.class);
                var unmarshaller = jaxbContext.createUnmarshaller();
                var valCurs = (ValCurs) unmarshaller.unmarshal(exchangeRateLine);
                return Optional.of(valCurs);
            }
        } catch (MalformedURLException e) {
            log.error("Something went wrong with connection: " + e.getMessage());
        } catch (JAXBException e) {
            log.error("Something went wrong with unmarshal: " + e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return Optional.empty();
    }

}
