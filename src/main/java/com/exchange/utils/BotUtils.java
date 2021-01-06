package com.exchange.utils;

import com.exchange.model.CurrencyCurs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

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
import java.util.stream.Collectors;

@Component
public class BotUtils {
    private static final Logger LOGGER = LogManager.getLogger(BotUtils.class);
    private static final Map<String, String> MAP_WITH_CHAR_CODES = Map.copyOf(Map.ofEntries(
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

    private BotUtils() {}

    public static String getFlagUnicode(String charCode) {
        return BotUtils.MAP_WITH_CHAR_CODES.get(charCode);
    }

    public static Optional<CurrencyCurs> getCurrencyCursFromSite() {
        try {
            URL url = new URL("http://www.cbr.ru/scripts/XML_daily.asp");
            var urlConnection = (HttpURLConnection) url.openConnection();
            try (
                    var inputStream = urlConnection.getInputStream();
                    var bufReader = new BufferedReader(new InputStreamReader(inputStream, "windows-1251"))
            ) {
                var exchangeRateLine = new StringReader(bufReader.lines().collect(Collectors.joining()));
                var jaxbContext = JAXBContext.newInstance(CurrencyCurs.class);
                var unmarshaller = jaxbContext.createUnmarshaller();
                var currencyCurs = (CurrencyCurs) unmarshaller.unmarshal(exchangeRateLine);
                return Optional.of(currencyCurs);
            }
        } catch (MalformedURLException e) {
            LOGGER.error("Something went wrong with connection: " + e.getMessage());
        } catch (JAXBException e) {
            LOGGER.error("Something went wrong with unmarshal: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return Optional.empty();
    }

}
