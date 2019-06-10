package io.elastic.soap.providers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import io.elastic.soap.AppConstants;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonObject;

import io.elastic.soap.services.WSDLService;
import io.elastic.soap.services.impls.HttpWSDLService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

public class OperationModelProviderTest {

  @Spy
  private static OperationModelProvider providerUt1 = spy(new OperationModelProvider());

  @Spy
  private static OperationModelProvider providerUt2 = spy(new OperationModelProvider());

  private final static String WSDL_URL_1 = "src/test/resources/xcurrencies.wsdl";
  private final static String WSDL_URL_2 = "src/test/resources/BLZService.wsdl";
  @Spy
  private static WSDLService wsdlService1 = spy(new HttpWSDLService());
  @Spy
  private static WSDLService wsdlService2 = spy(new HttpWSDLService());
  private static Definitions definitionsUt1;
  private static Definitions definitionsUt2;
  private static JsonObject config1;
  private static JsonObject config2;
  private static Set<String> operations;

  @BeforeAll
  public static void initConfig() throws IOException {
    providerUt1.setWsdlService(wsdlService1);
    providerUt2.setWsdlService(wsdlService2);
    definitionsUt1 = OperationModelProviderTest.getDefinitions(WSDL_URL_1);
    doReturn(definitionsUt1).when(wsdlService1).getWSDL(any(JsonObject.class));
    definitionsUt2 = OperationModelProviderTest.getDefinitions(WSDL_URL_2);
    doReturn(definitionsUt2).when(wsdlService2).getWSDL(any(JsonObject.class));
    config1 = Json.createObjectBuilder()
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
    config2 = Json.createObjectBuilder()
        .add(AppConstants.BINDING_CONFIG_NAME, "BLZServiceSOAP11Binding")
        .add(AppConstants.OPERATION_CONFIG_NAME, "getBank")
        .add(AppConstants.WSDL_CONFIG_NAME,
            "http://www.thomas-bayer.com/axis2/services/BLZService?wsdl")
        .add("auth",
            Json.createObjectBuilder().add("type", "No Auth")
                .add("basic", Json.createObjectBuilder().add("username", "")
                    .add("password", "")
                    .build())
        )
        .build();

    operations = new HashSet<>(Arrays.asList(new String[]{
        "ListCurrencies",
        "ListActiveCurrencies",
        "ListOfficialRates",
        "GetUnitOfAccount",
        "ConvertRealTimeValue",
        "ConvertHistoricalValue",
        "GetRealTimeForwardRate",
        "GetRealTimeCrossRateAsString",
        "GetLatestCrossRate",
        "GetLatestCrossRates",
        "GetRealTimeCrossRate",
        "GetRealTimeCrossRateGMT",
        "GetRawCrossRate",
        "GetRawCrossRates",
        "GetRealTimeCrossRates",
        "GetHistoricalCrossRateTables",
        "GetHistoricalCrossRateTablesBidAsk",
        "GetCurrencyReport",
        "GetHistoricalCrossRateTable",
        "GetHistoricalCrossRateTableBidAsk",
        "GetRealTimeCrossRateTable",
        "GetRealTimeCrossRateTableWithBidAsk",
        "GetAllCrossRatesForACurrency",
        "GetRealTimeCrossRateTableAsHTML",
        "GetSimpleRealTimeCrossRateTableAsHTML",
        "GetHistoricalCrossRateTableAsHTML",
        "GetHistoricalCrossRate",
        "GetHistoricalCrossRates",
        "GetHistoricalCrossRateBidAsk",
        "GetHistoricalCrossRatesBidAsk",
        "GetHistoricalCrossRatesRange",
        "GetHistoricalCrossRatesBidAskRange",
        "GetHistoricalCrossRatesAsOf",
        "GetHistoricalCrossRatesBidAskAsOf",
        "GetOfficialCrossRate",
        "GetOfficialCrossRates",
        "GetOfficialCrossRateBidAsk",
        "GetOfficialCrossRatesBidAsk",
        "GetMutipleHistoricalCrossRates",
        "GetAverageHistoricalCrossRates",
        "GetAverageHistoricalCrossRate",
        "GetHistoricalMonthlyCrossRatesRange",
        "GetCrossRateChange",
        "GetCurrencyChartCustom",
        "GetCurrencyChartCustomBinary",
        "GetCurrencyChart",
        "GetCurrencyChartBinary",
        "GetCurrencyIntradayChart",
        "GetCurrencyIntradayChartCustomBinary",
        "GetCurrencyIntradayChartCustom",
        "GetChartDesign",
        "GetTick",
        "GetTicks",
        "GetHistoricalTicks",
        "GetHistoricalHighLow",
        "GetIntradayHighLow"
    }));
  }

  // Mock real remote WSDL call method in order to use local WSDL resource
  public static Definitions getDefinitions(final String wsdlPath) {
    final WSDLParser parser = new WSDLParser();
    return parser.parse(wsdlPath);
  }

  @Test
  public void compareModelSize1() {

    // Here we have a list of 56 operations
    assertEquals(providerUt1.getSelectModel(config1).size(), 56);
  }

  @Test
  public void findKeyValues1() {
    assertTrue(providerUt1.getSelectModel(config1).keySet().containsAll(operations));
  }

  @Test
  public void compareModelSize2() {
    // Only 1 operation is in the list
    assertEquals(providerUt2.getSelectModel(config2).size(), 1);
  }

  @Test
  public void findKeyValues2() {
    assertEquals(providerUt2.getSelectModel(config2).getString("getBank"),
        "getBank");
  }
}
