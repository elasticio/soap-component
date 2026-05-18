[![CircleCI](https://circleci.com/gh/elasticio/soap-component.svg?style=svg)](https://circleci.com/gh/elasticio/soap-component)
# SOAP Component

## Table of Contents
* [Description](#description)
* [Core Concepts: Server vs Client](#core-concepts-server-vs-client)
* [Requirements](#requirements)
* [How it Works](#how-it-works)
* [Credentials](#credentials)
* [Triggers](#triggers)
    * [Receive SOAP Request](#receive-soap-request)
* [Actions](#actions)
    * [Call](#call)
    * [Soap Reply](#soap-reply)
* [Known Limitations](#known-limitations)
* [API and Documentation links](#api-and-documentation-links)
* [License](#license)

## Description
The SOAP Component enables seamless integration with SOAP-based Web Services. It supports both consuming external services and exposing your own SOAP endpoints.

### Completeness Matrix
![image](https://user-images.githubusercontent.com/36419533/65602890-eddfab80-dfa4-11e9-8d76-bd758aafa403.png)

---

## Core Concepts: Server vs Client

Understanding how to use this component depends on whether you want to **call** a service or **provide** a service.

| Feature | Role | Purpose | Typical Usage |
| :--- | :--- | :--- | :--- |
| **Trigger: Receive SOAP Request** | **Server** | Exposes a Webhook URL that accepts SOAP XML. | Acting as a SOAP endpoint for external systems. |
| **Action: Call** | **Client** | Sends a SOAP request to an external WSDL URI. | Fetching data from a 3rd party SOAP service. |
| **Action: Soap Reply** | **Responder** | Sends the HTTP response back to the Trigger caller. | Returning a "Result" to the system that called your flow. |

---

## Requirements
The component supports the following SOAP protocol versions:
* SOAP 1.1
* SOAP 1.2

The component supports the following WSDL styles:
* RPC/Literal
* Document/Encoded
* Document/Literal

#### Environment variables
* `EIO_REQUIRED_RAM_MB` - The recommended value for allocated memory is `2048MB`.

---

## How it Works

### Acting as a SOAP Client (Calling an external service)
1.  Locate and select the SOAP component and choose the **Call** action.
2.  Provide the WSDL URL (e.g., `http://dneonline.com/calculator.asmx?WSDL`).
3.  Select the **Binding** and **Operation** in that exact order.
4.  Map your JSON data to the generated input fields.
5.  The component converts your JSON to XML, sends it to the server, and returns the response as JSON.

### Acting as a SOAP Server (Exposing your own endpoint)
1.  Use the **Receive SOAP Request** trigger. This provides a Webhook URL for your flow.
2.  Provide a WSDL. The trigger uses this as a **Contract** to know what XML structure to expect.
3.  Select the **Binding** and **Operation** in that exact order.
4.  When an external system sends XML to your Webhook, the trigger validates it and converts it to JSON.
5.  Use the **Soap Reply** action at the end of your flow to return a response to the original caller.

---

## Credentials

The component functionally supports:
*   **No Auth**
*   **Basic Auth**

### Username (Basic Auth)
The username required for the Basic authorization header in the SOAP request.

### Password (Basic Auth)
The password required for the Basic authorization header in the SOAP request.

> [!IMPORTANT]
> Although the UI may display additional authentication types such as API Key or HMAC, these are **not supported** for outgoing calls in the current version.

---

## Triggers

### Receive SOAP Request
A webhook trigger that receives SOAP requests and validates the message body against the provided WSDL.

#### Input Field Descriptions
* **WSDL URI** - Publicly accessible URL of the WSDL.
* **Binding** - Select one of the bindings described in the WSDL.
* **Operation** - Select an operation available for the chosen binding.
* **Validation** - If `Enabled`, the SOAP body is validated against the WSDL schema.

#### Example Usage
**WSDL URI:** `http://www.dneonline.com/calculator.asmx?wsdl` | **Operation:** `Add`

**Incoming XML from User:**
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <Add xmlns="http://tempuri.org/">
      <intA>10</intA>
      <intB>20</intB>
    </Add>
  </soap:Body>
</soap:Envelope>
```

**Trigger Output (JSON):**
```json
{
  "Add": {
    "intA": 10,
    "intB": 20
  }
}
```

---

## Actions

### Call
Executes a call to an external SOAP service over HTTP.

#### Input Field Descriptions
* **WSDL URI** - Publicly accessible URL of the WSDL.
* **Binding** - Select one of the bindings described in the WSDL.
* **Operation** - Select an operation available for the chosen binding.
* **Request Timeout** - The timeout period in milliseconds (1-1,140,000). Defaults to `60,000`.

#### SOAP Fault Handling
If the server returns a SOAP Fault, the component emits a platform exception by default. 
Example of a SOAP 1.2 Fault converted to JSON:
```json
{
  "Fault": {
    "faultcode": "S:Server",
    "reason": "Server error java.lang.NullPointerException"
  }
}
```

---

### Soap Reply
Sends an HTTP response back to the original caller of the **Receive SOAP Request** trigger.

#### Input Data Example:
If your flow calculated a sum of `30`, you would map it to the **Soap Reply** input:
```json
{
  "AddResponse": {
    "AddResult": 30
  }
}
```

#### Output Data Example (XML sent back to caller):
```xml
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <AddResponse xmlns="http://tempuri.org/">
            <AddResult>30</AddResult>
        </AddResponse>
    </soap:Body>
</soap:Envelope>
```

---

## Known Limitations
*   **WSDL Support:** This version includes improved support for complex WSDLs and external XSD schemas via network-based fetching.
*   **Unsupported Styles:** RPC/SOAP-Encoded styles are not supported.
*   **Namespaces:** Namespaces are currently ignored during JSON conversion.
*   **SOAP Headers:** Custom SOAP headers are not yet supported.
*   **Message Format:** Multipart message formats are not supported; only the first part is processed.
*   **Sample Retrieval:** The "Retrieve Sample" feature may not always represent actual runtime behavior.

## API and Documentation links
* [Apache Axis2](http://axis.apache.org/axis2/java/core/)
* [FasterXML JsonSchemaGenerator](https://github.com/FasterXML/jackson-module-jsonSchema)

## License
© [Elastic.io GmbH](https://elastic.io)
