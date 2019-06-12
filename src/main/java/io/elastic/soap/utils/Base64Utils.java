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
    private static final String BASIC_AUTH_TYPE = "Basic" + " ";

    /**
     * Builds auth header starting from 'Basic ' and containing base64 encoded credentials
     */
    protected static final String getBasicAuthHeader(JsonObject configuration) {
        return BASIC_AUTH_TYPE + getEncodedString(configuration);
    }

    /**
     * Builds base64 encoded credentials string in format username:password
     */
    private static final String getEncodedString(JsonObject configuration) {
        final String username = Utils.getUsername(configuration);
        final String password = Utils.getPassword(configuration);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(username)
                .append(DELIMITER)
                .append(password);

        return Base64.getEncoder()
                .encodeToString(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
    }
}
