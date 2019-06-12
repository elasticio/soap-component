package io.elastic.soap.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elastic.soap.AppConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;

import static junit.framework.Assert.assertTrue;

public class UtilsTest {

    private static JsonObject configHttpNoAuth;
    private static JsonObject configHttpBasicAuth;
    private static JsonObject configHttpsNoAuth;
    private static JsonObject configHttpsBasicAuth;
    private static String elementName;
    private static String elementNameUnderscored;

    @BeforeAll
    public static void initTest() {
        elementName = "getBank";
        elementNameUnderscored = "getFirst_rate_Bank";
        configHttpNoAuth = Json.createObjectBuilder()
                .add(AppConstants.BINDING_CONFIG_NAME, "XigniteCurrenciesSoap")
                .add(AppConstants.OPERATION_CONFIG_NAME, "GetCurrencyIntradayChartCustom")
                .add(AppConstants.WSDL_CONFIG_NAME, "http://www.xignite.com/xcurrencies.asmx?WSDL")
                .add("auth",
                        Json.createObjectBuilder().add("type", "No Auth")
                                .add("basic", Json.createObjectBuilder().add("username", "")
                                        .add("password", "")
                                        .build())
                )
                .build();

        configHttpBasicAuth = Json.createObjectBuilder()
                .add(AppConstants.BINDING_CONFIG_NAME, "XigniteCurrenciesSoap")
                .add(AppConstants.OPERATION_CONFIG_NAME, "GetCurrencyIntradayChartCustom")
                .add(AppConstants.WSDL_CONFIG_NAME, "http://www.xignite.com/xcurrencies.asmx?WSDL")
                .add("auth",
                        Json.createObjectBuilder().add("type", "Basic Auth")
                                .add("basic", Json.createObjectBuilder().add("username", "Leadtributor")
                                        .add("password", "Leadtributor")
                                        .build())
                )
                .build();

        configHttpsNoAuth = Json.createObjectBuilder()
                .add(AppConstants.BINDING_CONFIG_NAME, "BLZServiceSOAP11Binding")
                .add(AppConstants.OPERATION_CONFIG_NAME, "getBank")
                .add(AppConstants.WSDL_CONFIG_NAME,
                        "https://www.thomas-bayer.com/axis2/services/BLZService?wsdl")
                .add("auth",
                        Json.createObjectBuilder().add("type", "No Auth")
                                .add("basic", Json.createObjectBuilder().add("username", "")
                                        .add("password", "")
                                        .build())
                )
                .build();

        configHttpsBasicAuth = Json.createObjectBuilder()
                .add(AppConstants.BINDING_CONFIG_NAME, "BLZServiceSOAP11Binding")
                .add(AppConstants.OPERATION_CONFIG_NAME, "getBank")
                .add(AppConstants.WSDL_CONFIG_NAME,
                        "https://www.thomas-bayer.com/axis2/services/BLZService?wsdl")
                .add("auth",
                        Json.createObjectBuilder().add("type", "Basic Auth")
                                .add("basic", Json.createObjectBuilder().add("username", "Leadtributor")
                                        .add("password", "Leadtributor")
                                        .build())
                )
                .build();
    }

    @Test
    public void getConfiguredObjectMapper() {
        assertTrue(Utils.getConfiguredObjectMapper() instanceof ObjectMapper);
    }

    @Test
    public void getBinding() {
        assertTrue("XigniteCurrenciesSoap".equals(Utils.getBinding(configHttpNoAuth)));
    }

    @Test
    public void getOperation() {
        assertTrue("GetCurrencyIntradayChartCustom".equals(Utils.getOperation(configHttpBasicAuth)));
    }

    @Test
    public void getWithUpperFirstLetter() {
        assertTrue("GetBank".equals(Utils.convertStringToUpperCamelCase(elementName)));
    }

    @Test
    public void getWithUpperFirstLetterUnderscore() {
        assertTrue(
                "GetFirstRateBank".equals(Utils.convertStringToUpperCamelCase(elementNameUnderscored)));
    }

    @Test
    public void getHttpWsdlUrl() {
        assertTrue(
                "http://www.xignite.com/xcurrencies.asmx?WSDL".equals(Utils.getWsdlUrl(configHttpNoAuth)));
    }

    @Test
    public void getHttpsWsdlUrl() {
        assertTrue(
                "https://www.thomas-bayer.com/axis2/services/BLZService?wsdl".equals(Utils.getWsdlUrl(
                        configHttpsNoAuth)));
    }

    @Test
    public void isBasicAuth() {
        assertTrue(Utils.isBasicAuth(configHttpBasicAuth));
    }

    @Test
    public void getBasicAuthHeader() {
        assertTrue("Basic TGVhZHRyaWJ1dG9yOkxlYWR0cmlidXRvcg=="
                .equals(Utils.getBasicAuthHeader(configHttpBasicAuth)));
    }

    @Test
    public void getUsername() {
        assertTrue("Leadtributor".equals(Utils.getUsername(configHttpBasicAuth)));
    }

    @Test
    public void getPassword() {
        assertTrue("Leadtributor".equals(Utils.getPassword(configHttpBasicAuth)));
    }


}
