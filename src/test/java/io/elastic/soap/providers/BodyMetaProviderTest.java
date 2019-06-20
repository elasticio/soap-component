package io.elastic.soap.providers;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import io.elastic.soap.AppConstants;
import io.elastic.soap.compilers.JaxbCompiler;
import io.elastic.soap.services.WSDLService;
import io.elastic.soap.services.impls.HttpWSDLService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class BodyMetaProviderTest {

    private static BodyMetaProvider provider;
    private static JsonObject config;
    private final static String WSDL_URL = "src/test/resources/xcurrencies.wsdl";

    @BeforeAll
    public static void beforeAll() throws Throwable {
        Definitions definitions = getDefinitions(WSDL_URL);
        config = Json.createObjectBuilder()
                .add(AppConstants.BINDING_CONFIG_NAME, "XigniteCurrenciesSoap")
                .add(AppConstants.OPERATION_CONFIG_NAME, "ListCurrencies")
                .add(AppConstants.WSDL_CONFIG_NAME, "http://www.xignite.com/xcurrencies.asmx?WSDL")
                .add("auth",
                        Json.createObjectBuilder().add("type", "No Auth")
                                .add("basic", Json.createObjectBuilder().add("username", "")
                                        .add("password", "")
                                        .build())
                )
                .build();

        provider = new BodyMetaProvider();
        WSDLService service = spy(new HttpWSDLService());
        provider.setWsdlService(service);
        doReturn(definitions).when(service).getWSDL(any(JsonObject.class));
        JaxbCompiler.generateAndLoadJaxbStructure(WSDL_URL);
        JaxbCompiler.putToCache("http://www.xignite.com/xcurrencies.asmx?WSDL", AppConstants.GENERATED_RESOURCES_DIR);
    }

    public static Definitions getDefinitions(final String wsdlPath) {
        final WSDLParser parser = new WSDLParser();
        return parser.parse(wsdlPath);
    }

    @Test
    public void testBodyMeta() {
        final JsonObject object = provider.getMetaModel(config);
        Assertions.assertNotNull(object.get("in"));
        Assertions.assertNotNull(object.get("out"));
    }
}