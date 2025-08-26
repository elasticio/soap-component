package io.elastic.soap.providers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import io.elastic.soap.AppConstants;
import io.elastic.soap.compilers.JaxbCompiler;
import io.elastic.soap.services.WSDLService;
import io.elastic.soap.services.impls.HttpWSDLService;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.json.Json;
import javax.json.JsonObject;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BodyMetaProviderTest {

  private static String[] arrayOfDirsToDelete = {"src/com", "src/io"};

  @AfterAll
  public static void cleanup() throws IOException {
    for (final String dirName : Arrays.asList(arrayOfDirsToDelete)) {
      final File dir = new File(dirName);
      if (dir.exists()) {
        FileUtils.deleteDirectory(dir);
      }
    }

    FileUtils.cleanDirectory(new File(AppConstants.GENERATED_RESOURCES_DIR));
  }

  public static Definitions getDefinitions(final String wsdlPath) {
    final WSDLParser parser = new WSDLParser();
    return parser.parse(wsdlPath);
  }

  @Test
  public void testBodyMetaForXCurrenciesWsdl() throws Throwable {
    final String wsdlUrl = "src/test/resources/xcurrencies.wsdl";
    final Definitions definitions = getDefinitions(wsdlUrl);
    final JsonObject config = Json.createObjectBuilder()
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

    final BodyMetaProvider provider = new BodyMetaProvider();
    final WSDLService service = spy(new HttpWSDLService());
    provider.setWsdlService(service);
    doReturn(definitions).when(service).getWSDL(any(JsonObject.class));
    JaxbCompiler.generateAndLoadJaxbStructure(wsdlUrl);
    JaxbCompiler.putToCache("http://www.xignite.com/xcurrencies.asmx?WSDL", AppConstants.GENERATED_RESOURCES_DIR);

    final JsonObject object = provider.getMetaModel(config);
    Assertions.assertNotNull(object.get("in"));
    Assertions.assertNotNull(object.get("out"));
  }

  @Test
  public void testBodyMetaForCapitalsWsdl() throws Throwable {
    final String wsdlUrl = "src/test/resources/capitals.wsdl";
    final Definitions definitions = getDefinitions(wsdlUrl);
    final JsonObject config = Json.createObjectBuilder()
            .add(AppConstants.BINDING_CONFIG_NAME, "CountryInfoServiceSoapBinding")
            .add(AppConstants.OPERATION_CONFIG_NAME, "CapitalCity")
            .add(AppConstants.WSDL_CONFIG_NAME,
                    "http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso?wsdl")
        .add("auth",
            Json.createObjectBuilder().add("type", "No Auth")
                .add("basic", Json.createObjectBuilder().add("username", "")
                    .add("password", "")
                    .build())
        )
        .build();

    final BodyMetaProvider provider = new BodyMetaProvider();
    final WSDLService service = spy(new HttpWSDLService());
    provider.setWsdlService(service);
    doReturn(definitions).when(service).getWSDL(any(JsonObject.class));
    JaxbCompiler.generateAndLoadJaxbStructure(wsdlUrl);
    JaxbCompiler.putToCache("http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso?wsdl", AppConstants.GENERATED_RESOURCES_DIR);

    final JsonObject object = provider.getMetaModel(config);
    Assertions.assertNotNull(object.get("in"));
    Assertions.assertNotNull(object.get("out"));
    final JsonObject inSchema = object.getJsonObject("in");
    final JsonObject properties = inSchema.getJsonObject("properties");
    Assertions.assertTrue(properties.containsKey("CapitalCity"));
  }
}
