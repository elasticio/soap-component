package io.elastic.soap.utils;

import javax.json.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Base64Utils {

    // Private constructor to prevent instantiation. Since utility classes should not be instantiated
    private Base64Utils() {
    }

    // Colon value
    private static final String DELIMITER = ":";
    private static final String BASIC_AUTH_TYPE = "Basic ";


    /**
     * Builds auth header starting from 'Basic ' and containing base64 encoded credentials
     */
    public static final String getBasicAuthHeader(final String username, final String password) {
        return BASIC_AUTH_TYPE + getEncodedString(username, password);
    }


    public static final String getEncodedString(final String username, final String password) {
        String content = username +
                DELIMITER +
                password;
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }


}
