package io.elastic.soap.compilers.model;

/**
 * Class represents a structure for storing data about correlations of WSDL's binding, operation,
 * and corresponding generated class name and body element name. Since these values can have
 * different values. E.g.: Binding: BLZServiceSOAP12Binding Operation: getBank Element Name: getBank
 * Element Type: tns:getBankType
 */
public class SoapBodyDescriptor {

    private String bindingName;
    private String operationName;
    private String requestBodyElementName;
    private String responseBodyElementName;
    private String requestBodyClassName;
    private String responseBodyClassName;
    private String requestBodyPackageName;
    private String responseBodyPackageName;
    private String requestBodyNameSpace;
    private String responseBodyNameSpace;
    private String soapAction;
    private String soapEndPoint;

    public String getRequestBodyElementName() {
        return requestBodyElementName;
    }

    public String getResponseBodyElementName() {
        return responseBodyElementName;
    }

    public String getRequestBodyClassName() {
        return requestBodyClassName;
    }

    public String getResponseBodyClassName() {
        return responseBodyClassName;
    }

    public String getRequestBodyPackageName() {
        return requestBodyPackageName;
    }

    public String getResponseBodyPackageName() {
        return responseBodyPackageName;
    }

    public String getRequestBodyNameSpace() {
        return requestBodyNameSpace;
    }

    public String getResponseBodyNameSpace() {
        return responseBodyNameSpace;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public String getSoapEndPoint() {
        return soapEndPoint;
    }

    public String getBindingName() {
        return bindingName;
    }

    public SoapBodyDescriptor(final String requestBodyElementName,
                              final String responseBodyElementName,
                              final String requestBodyClassName,
                              final String responseBodyClassName,
                              final String requestBodyPackageName,
                              final String responseBodyPackageName,
                              final String requestBodyNameSpace,
                              final String responseBodyNameSpace,
                              final String operationName,
                              final String soapAction,
                              final String soapEndPoint,
                              final String bindingName) {
        this.requestBodyElementName = requestBodyElementName;
        this.responseBodyElementName = responseBodyElementName;
        this.requestBodyClassName = requestBodyClassName;
        this.responseBodyClassName = responseBodyClassName;
        this.requestBodyPackageName = requestBodyPackageName;
        this.responseBodyPackageName = responseBodyPackageName;
        this.requestBodyNameSpace = requestBodyNameSpace;
        this.responseBodyNameSpace = responseBodyNameSpace;
        this.operationName = operationName;
        this.soapAction = soapAction;
        this.soapEndPoint = soapEndPoint;
        this.bindingName = bindingName;
    }

    private SoapBodyDescriptor() {
    }

    public static class Builder {

        private String requestBodyElementName;
        private String responseBodyElementName;
        private String requestBodyClassName;
        private String responseBodyClassName;
        private String requestBodyPackageName;
        private String responseBodyPackageName;
        private String requestBodyNameSpace;
        private String responseBodyNameSpace;
        private String operationName;
        private String soapAction;
        private String soapEndPoint;
        private String bindingName;

        public Builder setRequestBodyElementName(final String requestBodyElementName) {
            this.requestBodyElementName = requestBodyElementName;
            return this;
        }

        public Builder setResponseBodyElementName(final String responseBodyElementName) {
            this.responseBodyElementName = responseBodyElementName;
            return this;
        }

        public Builder setRequestBodyClassName(final String requestBodyClassName) {
            this.requestBodyClassName = requestBodyClassName;
            return this;
        }

        public Builder setResponseBodyClassName(final String responseBodyClassName) {
            this.responseBodyClassName = responseBodyClassName;
            return this;
        }

        public Builder setRequestBodyPackageName(final String requestBodyPackageName) {
            this.requestBodyPackageName = requestBodyPackageName;
            return this;
        }

        public Builder setResponseBodyPackageName(final String responseBodyPackageName) {
            this.responseBodyPackageName = responseBodyPackageName;
            return this;
        }

        public Builder setRequestBodyNameSpace(final String requestBodyNameSpace) {
            this.requestBodyNameSpace = requestBodyNameSpace;
            return this;
        }

        public Builder setResponseBodyNameSpace(final String responseBodyNameSpace) {
            this.responseBodyNameSpace = responseBodyNameSpace;
            return this;
        }

        public Builder setOperationName(final String operationName) {
            this.operationName = operationName;
            return this;
        }

        public Builder setSoapAction(final String soapAction) {
            this.soapAction = soapAction;
            return this;
        }

        public Builder setSoapEndPoint(final String soapEndPoint) {
            this.soapEndPoint = soapEndPoint;
            return this;
        }

        public Builder setBindingName(final String bindingName) {
            this.bindingName = bindingName;
            return this;
        }

        public SoapBodyDescriptor build() {
            return new SoapBodyDescriptor(requestBodyElementName, responseBodyElementName,
                    requestBodyClassName, responseBodyClassName, requestBodyPackageName,
                    responseBodyPackageName, requestBodyNameSpace, responseBodyNameSpace, operationName,
                    soapAction, soapEndPoint, bindingName);
        }
    }

    @Override
    public String toString() {
        return "SoapBodyDescriptor{" +
                "requestBodyElementName='" + requestBodyElementName + '\'' +
                ", responseBodyElementName='" + responseBodyElementName + '\'' +
                ", requestBodyClassName='" + requestBodyClassName + '\'' +
                ", responseBodyClassName='" + responseBodyClassName + '\'' +
                ", requestBodyPackageName='" + requestBodyPackageName + '\'' +
                ", responseBodyPackageName='" + responseBodyPackageName + '\'' +
                ", requestBodyNameSpace='" + requestBodyNameSpace + '\'' +
                ", responseBodyNameSpace='" + responseBodyNameSpace + '\'' +
                ", operationName='" + operationName + '\'' +
                ", soapAction='" + soapAction + '\'' +
                ", bindingName='" + bindingName + '\'' +
                '}';
    }
}