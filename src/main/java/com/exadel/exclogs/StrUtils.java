package com.exadel.exclogs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Utilities to work with strings.
 */
public class StrUtils {
    
    /**
     * Save string to file.
     */
    static void saveStr(String fname, String text) throws IOException {
        Files.writeString(Paths.get(fname), text);
    }

    /**
     * Load string from file.
     */
    static String loadUrl(String url) throws MalformedURLException, IOException {
        try (Scanner scanner = new Scanner(new URL(url).openStream(),
                StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    /**
     * Load string from file.
     */
    static String loadStr(String fname) {
        try {
            return Files.readString(Paths.get(fname));
        } catch (IOException e) {
            return null;
        }
    }    
}
