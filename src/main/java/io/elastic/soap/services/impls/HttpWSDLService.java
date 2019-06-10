package io.elastic.soap.services.impls;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import io.elastic.soap.services.WSDLService;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.json.JsonObject;
import java.io.IOException;
import java.io.InputStream;

import static io.elastic.soap.utils.Utils.createGet;
import static io.elastic.soap.utils.Utils.getWsdlUrl;


public class HttpWSDLService implements WSDLService {

    @Override
    public Definitions getWSDL(final JsonObject config) throws IOException {
        try (final CloseableHttpClient client = HttpClients.createDefault()) {
            final HttpGet get = createGet(config);
            final CloseableHttpResponse response = client.execute(get);
            final int code = response.getStatusLine().getStatusCode();
            if (code != 200) {
                throw new HttpResponseException(code, String.format("Invalid response code: %d, for url: %s", code, getWsdlUrl(config)));
            }
            final InputStream is = response.getEntity().getContent();
            final WSDLParser reader = new WSDLParser();
            return reader.parse(is);
        }
    }
}