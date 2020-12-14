package io.elastic.soap.actions;

import static io.elastic.soap.actions.SoapReplyAction.CONTENT_TYPE;
import static io.elastic.soap.actions.SoapReplyAction.HEADER_CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.elastic.api.EventEmitter;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.HttpReply;
import io.elastic.api.InitParameters;
import io.elastic.api.Message;
import io.elastic.soap.AppConstants;
import io.elastic.soap.TestCallback;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import net.joshka.junit.json.params.JsonFileSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Disabled
public class SoapReplyTest {

  private static final Logger logger = LoggerFactory.getLogger(EventEmitter.class);

  private static EventEmitter eventEmitter;
  private static TestCallback onData;
  private static TestCallback onError;
  private static TestCallback onHttpReply;

  @BeforeAll
  public static void initConfig() {
    onData = new TestCallback();
    onError = new TestCallback();
    onHttpReply = new TestCallback();

    eventEmitter = new EventEmitter.Builder()
        .onData(onData)
        .onError(onError)
        .onSnapshot(new TestCallback())
        .onRebound(new TestCallback())
        .onHttpReplyCallback(onHttpReply)
        .build();
  }

  @AfterEach
  public void resetTest() {
    onData.reset();
    onError.reset();
    onHttpReply.reset();
  }


  @ParameterizedTest
  @JsonFileSource(resources = "/soapResponseJsonSample.json")
  @DisplayName("Send SOAP Response message")
  public void soapReplyAction(JsonObject body) {
    SoapReplyAction soapReplyAction = new SoapReplyAction();

    JsonObject cfg = Json.createObjectBuilder()
        .add(AppConstants.BINDING_CONFIG_NAME, "ProposalResponseServiceSoap")
        .add(AppConstants.OPERATION_CONFIG_NAME, "SetProposalResponse")
        .add(AppConstants.WSDL_CONFIG_NAME, "src/test/resources/ProposalResponseService.wsdl")
        .add("auth",
            Json.createObjectBuilder().add("type", "No Auth")
                .add("basic", Json.createObjectBuilder().add("username", "")
                    .add("password", "")
                    .build())
        )
        .build();
    InitParameters initParameters = new InitParameters(cfg);
    soapReplyAction.init(initParameters);

    JsonObject headers = Json.createObjectBuilder()
        .add("reply_to", "testReplyId")
        .add(HEADER_CONTENT_TYPE, CONTENT_TYPE)
        .build();
    Message msg = new Message.Builder().body(body).headers(headers).build();

    ExecutionParameters executionParameters = new ExecutionParameters.Builder(msg, eventEmitter)
        .configuration(cfg).build();
    soapReplyAction.execute(executionParameters);

    String expectedDataSoapString =
        "\"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\\\"http://schemas.xmlsoap.org/soap/envelope/\\\"><SOAP-ENV:Header/><SOAP-ENV:Body xmlns=\\\"http://www.newmarketinc.com\\\"><SetProposalResponseResponse>\\n    <SetProposalResponseResult>\\n        <OBSResult>\\n            <ResponseError>testError</ResponseError>\\n            <ResponseCode>testCode</ResponseCode>\\n        </OBSResult>\\n    </SetProposalResponseResult>\\n</SetProposalResponseResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>\"";

    String expectedHttpSoapString =
        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<SOAP-ENV:Header/>"
            + "<SOAP-ENV:Body xmlns=\"http://www.newmarketinc.com\">"
            + "<SetProposalResponseResponse>\n"
            + "    <SetProposalResponseResult>\n"
            + "        <OBSResult>\n"
            + "            <ResponseError>testError</ResponseError>\n"
            + "            <ResponseCode>testCode</ResponseCode>\n"
            + "        </OBSResult>\n"
            + "    </SetProposalResponseResult>\n"
            + "</SetProposalResponseResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>";

    assertEquals(0, onError.getCalls().size());

    assertEquals(1, onHttpReply.getCalls().size());
    assertEquals(expectedHttpSoapString, new BufferedReader(
        new InputStreamReader(((HttpReply) onHttpReply.getCalls().get(0)).getContent()))
        .lines()
        .collect(Collectors.joining(System.lineSeparator())));

    assertEquals(1, onData.getCalls().size());
    assertEquals(expectedDataSoapString,
        ((Message) onData.getCalls().get(0)).getBody().getJsonString("SoapResponse").toString());
  }

  @ParameterizedTest
  @JsonFileSource(resources = "/soapResponseJsonSample.json")
  @DisplayName("Send SOAP Response message with no validation")
  public void soapReplyActionNoValid(JsonObject body) {
    SoapReplyAction soapReplyAction = new SoapReplyAction();

    JsonObject cfg = Json.createObjectBuilder()
        .add(AppConstants.BINDING_CONFIG_NAME, "ProposalResponseServiceSoap")
        .add(AppConstants.OPERATION_CONFIG_NAME, "SetProposalResponse")
        .add(AppConstants.WSDL_CONFIG_NAME, "src/test/resources/ProposalResponseService.wsdl")
        .add("auth",
            Json.createObjectBuilder().add("type", "No Auth")
                .add("basic", Json.createObjectBuilder().add("username", "")
                    .add("password", "")
                    .build())
        )
        .add(AppConstants.VALIDATION, AppConstants.VALIDATION_DISABLED)
        .build();
    InitParameters initParameters = new InitParameters(cfg);
    soapReplyAction.init(initParameters);

    JsonObject headers = Json.createObjectBuilder()
        .add("reply_to", "testReplyId")
        .add(HEADER_CONTENT_TYPE, CONTENT_TYPE)
        .build();
    Message msg = new Message.Builder().body(body).headers(headers).build();

    ExecutionParameters executionParameters = new ExecutionParameters.Builder(msg, eventEmitter)
        .configuration(cfg).build();
    soapReplyAction.execute(executionParameters);

    String expectedDataSoapString =
        "\"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\\\"http://schemas.xmlsoap.org/soap/envelope/\\\">"
            + "<SOAP-ENV:Header/>"
            + "<SOAP-ENV:Body xmlns=\\\"http://www.newmarketinc.com\\\">"
            + "<SetProposalResponseResponse>\\n"
            + "    <SetProposalResponseResult>\\n"
            + "        <OBSResult>\\n"
            + "            <ResponseError>testError</ResponseError>\\n"
            + "            <ResponseCode>testCode</ResponseCode>\\n"
            + "        </OBSResult>\\n"
            + "    </SetProposalResponseResult>\\n"
            + "</SetProposalResponseResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>\"";

    String expectedHttpSoapString =
        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<SOAP-ENV:Header/>"
            + "<SOAP-ENV:Body xmlns=\"http://www.newmarketinc.com\">"
            + "<SetProposalResponseResponse>\n"
            + "    <SetProposalResponseResult>\n"
            + "        <OBSResult>\n"
            + "            <ResponseError>testError</ResponseError>\n"
            + "            <ResponseCode>testCode</ResponseCode>\n"
            + "        </OBSResult>\n"
            + "    </SetProposalResponseResult>\n"
            + "</SetProposalResponseResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>";

    assertEquals(0, onError.getCalls().size());

    assertEquals(1, onHttpReply.getCalls().size());
    assertEquals(expectedHttpSoapString, new BufferedReader(
        new InputStreamReader(((HttpReply) onHttpReply.getCalls().get(0)).getContent()))
        .lines()
        .collect(Collectors.joining(System.lineSeparator())));

    assertEquals(1, onData.getCalls().size());
    assertEquals(expectedDataSoapString,
        ((Message) onData.getCalls().get(0)).getBody().getJsonString("SoapResponse").toString());
  }


  @ParameterizedTest
  @JsonFileSource(resources = "/soapResponseJsonSample2.json")
  @DisplayName("Send SOAP Response message")
  public void soapReplyAction2(JsonObject body) {
    SoapReplyAction soapReplyAction = new SoapReplyAction();

    JsonObject cfg = Json.createObjectBuilder()
        .add(AppConstants.BINDING_CONFIG_NAME, "WeatherSoap")
        .add(AppConstants.OPERATION_CONFIG_NAME, "GetCityForecastByZIP")
        .add(AppConstants.WSDL_CONFIG_NAME, "src/test/resources/Weather.wsdl")
        .add("auth",
            Json.createObjectBuilder().add("type", "No Auth")
                .add("basic", Json.createObjectBuilder().add("username", "")
                    .add("password", "")
                    .build())
        )
        .build();

    InitParameters initParameters = new InitParameters(cfg);
    soapReplyAction.init(initParameters);

    JsonObject headers = Json.createObjectBuilder()
        .add("reply_to", "testReplyId")
        .add(HEADER_CONTENT_TYPE, CONTENT_TYPE)
        .build();
    Message msg = new Message.Builder().body(body).headers(headers).build();

    ExecutionParameters executionParameters = new ExecutionParameters.Builder(msg, eventEmitter)
        .configuration(cfg).build();
    soapReplyAction.execute(executionParameters);

    String expectedDataSoapString =
        "\"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\\\"http://schemas.xmlsoap.org/soap/envelope/\\\"><SOAP-ENV:Header/><SOAP-ENV:Body xmlns=\\\"http://ws.cdyne.com/WeatherWS/\\\"><GetCityForecastByZIPResponse>\\n    <GetCityForecastByZIPResult>\\n        <Success>false</Success>\\n        <ResponseText>incididunt ut eiusmod ex magna</ResponseText>\\n        <State>ullamco est</State>\\n        <City>fugiat aute</City>\\n        <WeatherStationCity>velit non</WeatherStationCity>\\n        <ForecastResult>\\n            <Forecast>\\n                <WeatherID>0</WeatherID>\\n                <Desciption>irure culpa consequat</Desciption>\\n            </Forecast>\\n            <Forecast>\\n                <Date>1970-01-01T00:00:00.123Z</Date>\\n                <WeatherID>123</WeatherID>\\n            </Forecast>\\n            <Forecast>\\n                <WeatherID>123</WeatherID>\\n            </Forecast>\\n        </ForecastResult>\\n    </GetCityForecastByZIPResult>\\n</GetCityForecastByZIPResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>\"";

    String expectedHttpSoapString =
        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<SOAP-ENV:Header/>"
            + "<SOAP-ENV:Body xmlns=\"http://ws.cdyne.com/WeatherWS/\">"
            + "<GetCityForecastByZIPResponse>\n"
            + "    <GetCityForecastByZIPResult>\n"
            + "        <Success>false</Success>\n"
            + "        <ResponseText>incididunt ut eiusmod ex magna</ResponseText>\n"
            + "        <State>ullamco est</State>\n"
            + "        <City>fugiat aute</City>\n"
            + "        <WeatherStationCity>velit non</WeatherStationCity>\n"
            + "        <ForecastResult>\n"
            + "            <Forecast>\n"
            + "                <WeatherID>0</WeatherID>\n"
            + "                <Desciption>irure culpa consequat</Desciption>\n"
            + "            </Forecast>\n"
            + "            <Forecast>\n"
            + "                <Date>1970-01-01T00:00:00.123Z</Date>\n"
            + "                <WeatherID>123</WeatherID>\n"
            + "            </Forecast>\n"
            + "            <Forecast>\n"
            + "                <WeatherID>123</WeatherID>\n"
            + "            </Forecast>\n"
            + "        </ForecastResult>\n"
            + "    </GetCityForecastByZIPResult>\n"
            + "</GetCityForecastByZIPResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>";

    assertEquals(0, onError.getCalls().size());

    assertEquals(1, onHttpReply.getCalls().size());
    assertEquals(expectedHttpSoapString, new BufferedReader(
        new InputStreamReader(((HttpReply) onHttpReply.getCalls().get(0)).getContent()))
        .lines()
        .collect(Collectors.joining(System.lineSeparator())));

    assertEquals(1, onData.getCalls().size());
    assertEquals(expectedDataSoapString,
        ((Message) onData.getCalls().get(0)).getBody().getJsonString("SoapResponse").toString());
  }
}
