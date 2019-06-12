package io.elastic.soap.providers;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import io.elastic.soap.AppConstants;
import io.elastic.soap.services.WSDLService;
import io.elastic.soap.services.impls.HttpWSDLService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class BindingModelProviderTest {

    private final static String WSDL_URL_1 = "src/test/resources/xcurrencies.wsdl";
    private final static String WSDL_URL_2 = "src/test/resources/BLZService.wsdl";
    @Spy
    private static BindingModelProvider providersUt1 = spy(new BindingModelProvider());
    @Spy
    private static BindingModelProvider providersUt2 = spy(new BindingModelProvider());
    @Spy
    private static WSDLService wsdlService1 = spy(new HttpWSDLService());
    @Spy
    private static WSDLService wsdlService2 = spy(new HttpWSDLService());
    private static Definitions definitionsUt1;
    private static Definitions definitionsUt2;
    private static JsonObject config1;
    private static JsonObject config2;

    @BeforeAll
    public static void initConfig() throws IOException {

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
        providersUt1.setWsdlService(wsdlService1);
        providersUt2.setWsdlService(wsdlService2);
        definitionsUt1 = BindingModelProviderTest.getDefinitions(WSDL_URL_1);
        doReturn(definitionsUt1).when(wsdlService1).getWSDL(any(JsonObject.class));

        definitionsUt2 = BindingModelProviderTest.getDefinitions(WSDL_URL_2);
        doReturn(definitionsUt2).when(wsdlService2).getWSDL(any(JsonObject.class));

    }

    // Mock real remote WSDL call method in order to use local WSDL resource
    public static Definitions getDefinitions(final String wsdlPath) {
        final WSDLParser parser = new WSDLParser();
        return parser.parse(wsdlPath);
    }

    @Test
    public void compareModelSize1() {
        assertEquals(2, providersUt1.getSelectModel(config1).size(), String
                .format("expected %d but found actual %d", 2, providersUt1.getSelectModel(config1).size()));
    }

    @Test
    public void findKeyValues1() {
        assertEquals("XigniteCurrenciesSoap",
                providersUt1.getSelectModel(config1).getString("XigniteCurrenciesSoap"), String
                        .format("expected %s but found actual %s", "XigniteCurrenciesSoap",
                                providersUt1.getSelectModel(config1).getString("XigniteCurrenciesSoap"))
        );
        assertEquals("XigniteCurrenciesSoap12",
                providersUt1.getSelectModel(config1).getString("XigniteCurrenciesSoap12"),
                String
                        .format("expected %s but found actual %s", "XigniteCurrenciesSoap12",
                                providersUt1.getSelectModel(config1).getString("XigniteCurrenciesSoap12"))
        );
    }

    @Test
    public void compareModelSize2() {
        assertEquals(2, providersUt2.getSelectModel(config2).size(),
                String.format("expected %d but found actual %d", 2,
                        providersUt2.getSelectModel(config2).size()));
    }

    @Test
    public void findKeyValues2() {
        assertEquals("BLZServiceSOAP11Binding",
                providersUt2.getSelectModel(config2).getString("BLZServiceSOAP11Binding"), String
                        .format("expected %s but found actual %s", "BLZServiceSOAP11Binding",
                                providersUt2.getSelectModel(config2).getString("BLZServiceSOAP11Binding")));
        assertEquals("BLZServiceSOAP12Binding",
                providersUt2.getSelectModel(config2).getString("BLZServiceSOAP12Binding"), String
                        .format("expected %s but found actual %s", "BLZServiceSOAP12Binding", providersUt2.getSelectModel(config2).getString("BLZServiceSOAP12Binding")));
    }
}
