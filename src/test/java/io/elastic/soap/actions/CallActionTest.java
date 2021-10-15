package io.elastic.soap.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.elastic.api.EventEmitter;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.InitParameters;
import io.elastic.api.Message;
import io.elastic.soap.AppConstants;
import io.elastic.soap.TestCallback;
import io.elastic.soap.exceptions.ComponentException;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Disabled
// The test actually works! Should be enabled and run (given that)
// https://www.ebi.ac.uk/europepmc/webservices/test/soap?wsdl service is still available
// Is disabled because we can't rely on an external SOAP service in the tests. It might die
public class CallActionTest {

  private static final Logger logger = LoggerFactory.getLogger(EventEmitter.class);

  private static EventEmitter eventEmitter;
  private static TestCallback onData;
  private static TestCallback onError;

  @BeforeAll
  public static void initConfig() {
    onData = new TestCallback();
    onError = new TestCallback();

    eventEmitter = new EventEmitter.Builder()
        .onData(onData)
        .onError(onError)
        .onSnapshot(new TestCallback())
        .onRebound(new TestCallback())
        .onHttpReplyCallback(new TestCallback())
        .build();
  }

  @AfterEach
  public void resetTest() {
    onData.reset();
    onError.reset();
  }

  @Test
  @DisplayName("SOAP Fault checked. Should emit fault as message")
  public void callAction() {
    JsonObject body = Json.createObjectBuilder().add("getBookXML",
            Json.createObjectBuilder().add("source", "bla").build())
        .build();

    JsonObject cfg = Json.createObjectBuilder()
        .add(AppConstants.BINDING_CONFIG_NAME, "WSCitationImplPortBinding")
        .add(AppConstants.OPERATION_CONFIG_NAME, "getBookXML")
        .add(AppConstants.WSDL_CONFIG_NAME,
            "https://www.ebi.ac.uk/europepmc/webservices/test/soap?wsdl")
        .add("emitSoapFault", true)
        .add("auth",
            Json.createObjectBuilder().add("type", "No Auth")
                .add("basic", Json.createObjectBuilder().add("username", "")
                    .add("password", "")
                    .build())
        )
        .build();

    CallAction callAction = new CallAction();
    InitParameters initParameters = new InitParameters.Builder().configuration(cfg).build();
    callAction.init(initParameters);

    Message msg = new Message.Builder().body(body).build();

    ExecutionParameters executionParameters = new ExecutionParameters.Builder(msg, eventEmitter)
        .configuration(cfg).build();
    callAction.execute(executionParameters);

    String expectedJsonSoapFault =
        "{\"Fault\":{\"faultcode\":\"S:Server\",\"faultstring\":\"java.lang.NullPointerException\",\"faultactor\":null}}";

    assertEquals(0, onError.getCalls().size());

    assertEquals(1, onData.getCalls().size());
    assertEquals(expectedJsonSoapFault,
        ((Message) onData.getCalls().get(0)).getBody().toString());
  }

  @Test
  @DisplayName("SOAP Fault unchecked. Should throw fault as platform error")
  public void callActionThrowSoapFault() {
    JsonObject body = Json.createObjectBuilder().add("getBookXML",
            Json.createObjectBuilder().add("source", "bla").build())
        .build();

    JsonObject cfg = Json.createObjectBuilder()
        .add(AppConstants.BINDING_CONFIG_NAME, "WSCitationImplPortBinding")
        .add(AppConstants.OPERATION_CONFIG_NAME, "getBookXML")
        .add(AppConstants.WSDL_CONFIG_NAME,
            "https://www.ebi.ac.uk/europepmc/webservices/test/soap?wsdl")
        .add("emitSoapFault", false)
        .add("auth",
            Json.createObjectBuilder().add("type", "No Auth")
                .add("basic", Json.createObjectBuilder().add("username", "")
                    .add("password", "")
                    .build())
        )
        .build();

    CallAction callAction = new CallAction();
    InitParameters initParameters = new InitParameters.Builder().configuration(cfg).build();
    callAction.init(initParameters);

    Message msg = new Message.Builder().body(body).build();

    ExecutionParameters executionParameters = new ExecutionParameters.Builder(msg, eventEmitter)
        .configuration(cfg).build();

    ComponentException componentException = assertThrows(ComponentException.class, () -> {
      callAction.execute(executionParameters);
    });

    String expectedExceptionMessage = "Server has responded with SOAP fault. Code: S:Server. Reason: java.lang.NullPointerException";

    assertEquals(0, onData.getCalls().size());
    assertEquals(expectedExceptionMessage, componentException.getMessage());
  }
}
