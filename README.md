[![CircleCI](https://circleci.com/gh/elasticio/soap-component.svg?style=svg)](https://circleci.com/gh/elasticio/soap-component)
# SOAP Component
## Table of Contents
* [Description](#description)
   * [Purpose](#purpose)
   * [Completeness Matrix](#completeness-matrix)
   * [How it works](#how-it-works)
        * [Step 1](#step-1)
        * [Step 2](#step-2)
        * [Step 3](#step-3)
        * [Step 4](#step-4)
        * [Step 5](#step-5)
        * [Step 6](#step-6)
   * [Requirements](#requirements)
   * [Environment variables](#environment-variables)
* [Credentials](#credentials)
    * [Type](#type)
    * [Username](#username-basic-auth-type)
    * [Password](#password-basic-auth-type)
* [Triggers](#actions)
   * [Receive SOAP Request](#receive-soap-request)
     * [Input fields description](#input-fields-description)
     * [Example of usage](#example-of-usage)
     * [Known Limitations](#known-limitations)
* [Actions](#actions)
   * [Call](#call)
     * [Input fields description](#input-fields-description)
     * [SOAP Fault](#soap-fault)
     * [Input Json Schema](#input-json-schema)
     * [Output Json Schema](#output-json-schema)
     * [Additional info](#additional-info)
   * [Soap Reply](#soap-reply)
     * [Input fields description](#input-fields-description)
     * [Input json schema](#input-json-schema)
     * [Output json schema](#output-json-schema)
     * [Current limitations](#current-limitations))
* [API and Documentation links](#api-and-documentation-links)
* [License](#license)

## Description
The SOAP Component enables seamless integration with SOAP-based Web Services within an Open Integration Hub (OIH) flow.

### Purpose
As a robust integration platform, OIH provides the capability to invoke SOAP Web Services over HTTP, ensuring compatibility with legacy and enterprise systems.

### Completeness Matrix
![image](https://user-images.githubusercontent.com/36419533/65602890-eddfab80-dfa4-11e9-8d76-bd758aafa403.png)

[SOAP component completeness matrix](https://docs.google.com/spreadsheets/d/1bNDN_E9kBgeKrSu-NWDp3Zsrf6V7ud8hi2HPKlPCmcQ)

### How it works

#### Step 1
Locate and select the SOAP component from the component repository.
![Step 1](https://user-images.githubusercontent.com/13310949/43515103-5de72b58-958a-11e8-88ce-5870003867a1.png)

#### Step 2
Create new credentials or select an existing set.
![Step 2](https://user-images.githubusercontent.com/13310949/43514620-3c2b9efa-9589-11e8-9d9e-c82b1d66e5eb.png)

#### Step 3
Specify the WSDL URL, then select the binding and operation. **The sequence of selection is critical.**
![Step 3](https://user-images.githubusercontent.com/13310949/43522182-365e9fbe-95a1-11e8-8226-3e3679afbe17.png)

#### Step 4
Configure the input data and click "Continue".
![Step 4](https://user-images.githubusercontent.com/13310949/43514773-9036472a-9589-11e8-83d6-95759f1a2cc9.png)

#### Step 5
Retrieve a sample response or add one manually.
![Step 5: Retrieve sample](https://user-images.githubusercontent.com/13310949/43514839-bace8e16-9589-11e8-92d2-e54890472dbb.png)

#### Step 6
Review the retrieved sample result.
![Step 6: Retrieve sample result](https://user-images.githubusercontent.com/13310949/43515232-aca5be76-958a-11e8-95a0-c723f9323e4f.png)

### Requirements
The component supports the following SOAP protocol versions:
* SOAP 1.1
* SOAP 1.2

The component supports the following WSDL styles:
* RPC/Literal
* Document/Encoded
* Document/Literal

#### Environment variables
* `EIO_REQUIRED_RAM_MB` - The recommended value for allocated memory is `2048MB`.

## Credentials

### Type
> [!IMPORTANT]
> Although the UI may display additional authentication types such as **API Key Auth** or **HMAC**, these are **not supported** by the current version of the component.

The component functionally supports:
* **No Auth**
* **Basic Auth**

### Username (Basic Auth)
The username required for the Basic authorization header in the SOAP request.

### Password (Basic Auth)
The password required for the Basic authorization header in the SOAP request.

> [!NOTE]
> Errors will not be thrown immediately upon providing invalid credentials, as the credentials do not contain the WSDL URL. Authentication errors (e.g., `401 Unauthorized`) are typically encountered during the sample retrieval step or at runtime.

## Triggers

### Receive SOAP Request
A webhook trigger that receives SOAP requests and validates the message body against the provided WSDL.

#### Input Field Descriptions
* **WSDL URI** - Publicly accessible URL of the WSDL.
* **Binding** - Select one of the bindings described in the WSDL.
* **Operation** - Select an operation available for the chosen binding.
* **Validation** - If set to `Enabled`, the SOAP body will be validated against the WSDL; if `Disabled`, validation is skipped.
#### Example Usage

##### Configuration:
* **WSDL URI**: `http://www.dneonline.com/calculator.asmx?wsdl`
* **Binding**: `CalculatorSoap12`
* **Operation**: `Add`
* **Validation**: `Enabled`

##### Request Body:
```xml
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <Add xmlns="http://tempuri.org/">
      <intA>1</intA>
      <intB>1</intB>
    </Add>
  </soap:Body>
</soap:Envelope>
```

##### Output:
```json
{
  "Add": {
    "intA": "1",
    "intB": "1"
  }
}
```

#### Known Limitations
1. **Namespace Handling**: Namespaces are currently ignored. A SOAP body containing multiple tags with the same name but different namespaces may be processed incorrectly.
2. **SOAP Headers**: Custom SOAP headers are not yet supported.
3. **Sample Retrieval**: The "Retrieve Sample" feature may not always represent the actual runtime behavior of the component.

## Actions

### Call
Executes a call to a SOAP service over HTTP using a publicly accessible WSDL URL.

> [!IMPORTANT]
> This action only supports **Basic Authorization**. Other authentication types visible in the credentials UI (such as API Key or HMAC) are not supported for outgoing calls.

#### Input Field Descriptions
* **WSDL URI** - Publicly accessible URL of the WSDL.
* **Binding** - Select one of the bindings described in the WSDL.
* **Operation** - Select an operation available for the chosen binding.
* **Request Timeout** - The timeout period in milliseconds (1-1,140,000) for waiting on a server response. Defaults to `60,000` (60 seconds).

#### SOAP Fault Handling
A SOAP fault carries error information within a SOAP message. This component handles SOAP faults by emitting a platform exception. All SOAP faults should comply with the [W3C SOAP Fault standard](https://www.w3.org/TR/soap12-part1/#soapfault).

Example of a SOAP 1.1 Fault:
```json
{
  "Fault": {
    "faultcode": "S:Server",
    "faultstring": "Server error java.lang.NullPointerException",
    "faultactor": null
  }
}
```

Example of a SOAP 1.2 Fault:
```json
{
  "Fault": {
    "faultcode": "S:Server",
    "reason": "Server error java.lang.NullPointerException"
  }
}
```

#### Input JSON Schema
The component does not use a static input schema. Instead, it is dynamically generated based on the specific WSDL, binding, and operation configured. [Apache Axis2](http://axis.apache.org/axis2/java/core/) and [FasterXML JsonSchemaGenerator](https://github.com/FasterXML/jackson-module-jsonSchema) are used internally to generate this metadata.

#### Output JSON Schema
The output JSON schema is generated dynamically in the same manner as the input schema.

## Additional Information

> [!WARNING]
> Configuration fields must be specified exactly in the order listed below to avoid configuration errors:
> 1. WSDL URI
> 2. Binding
> 3. Operation

### Soap Reply
Wraps and returns input data as a SOAP response based on the provided SOAP metadata.

#### Input Field Descriptions
* **WSDL URI** - Publicly accessible URL of the WSDL.
* **Binding** - Select one of the bindings described in the WSDL.
* **Operation** - Select an operation available for the chosen binding.

#### Input/Output JSON Schema
The JSON schemas for this action are generated dynamically. Please refer to the **Call** action section for more details on the generation process.

#### Input Data Example:
```json
{
  "AddResponse": {
    "AddResult": 3
  }
}
```

#### Output Data Example:
```xml
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <soap:Body>
        <AddResponse xmlns="http://example.org/">
            <AddResult>3</AddResult>
        </AddResponse>
    </soap:Body>
</soap:Envelope>
```

### Known Limitations
The following limitations apply to this component:

* **Unsupported Styles**: RPC/SOAP-Encoded styles are not supported. While some legacy services still use these styles, modern implementations favor Document/Literal for better interoperability.
* **External Schemas**: Only self-contained WSDLs are supported. WSDLs referencing external XSD schemas are not compatible with this version.
* **Advanced Features**: The following are currently not supported:
    * WS-Security headers
    * WS-Addressing
    * Custom SOAP headers
* **Public Accessibility**: The WSDL and associated schemas must be accessible via a public URL. File uploads are not supported.
* **Message Format**: Multipart message formats are not supported; only the first part of a request element is processed.
* **Error Handling**: The "Emit SOAP Faults Instead of Throwing an Error" feature has not been fully validated against all possible SOAP fault scenarios.

## API and Documentation Links
* [Apache Axis2](http://axis.apache.org/axis2/java/core/)
* [FasterXML JsonSchemaGenerator](https://github.com/FasterXML/jackson-module-jsonSchema)

## License
© [Elastic.io GmbH](https://elastic.io)
