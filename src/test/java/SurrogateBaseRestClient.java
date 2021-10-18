import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRootObject;
import io.apimap.client.RestClientConfiguration;
import io.apimap.client.client.BaseRestClient;
import io.apimap.client.client.query.ApiQuery;
import io.apimap.client.client.query.CreateApiQuery;
import io.apimap.client.exception.ApiRequestFailedException;
import io.apimap.client.exception.IllegalApiContentException;
import io.apimap.client.exception.IncorrectTokenException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

public class SurrogateBaseRestClient extends BaseRestClient {

    public SurrogateBaseRestClient(RestClientConfiguration configuration, CloseableHttpClient httpClient) {
        super(configuration, httpClient);
    }

    public SurrogateBaseRestClient(RestClientConfiguration configuration) {
        super(configuration);
    }

    public ObjectMapper defaultObjectMapper() {
        return super.defaultObjectMapper();
    }

    public CloseableHttpClient defaultCloseableHttpClient() {
        return super.defaultCloseableHttpClient();
    }

    public void addApiQuery(ApiQuery query) {
        super.addApiQuery(query);
    }

    public URI performQueries(CloseableHttpClient client) throws IOException, ApiRequestFailedException, IllegalApiContentException, IncorrectTokenException {
        return super.performQueries(client);
    }

    public URI enumerateQueries(HttpGet request, ArrayList<ApiQuery> remainingQueries, CloseableHttpClient client, int queryCallstackDepth) throws IOException, ApiRequestFailedException, IllegalApiContentException, IncorrectTokenException {
        return super.enumerateQueries(request, remainingQueries, client, queryCallstackDepth);
    }

    public int deleteResource(HttpDelete deleteRequest) throws ApiRequestFailedException, IncorrectTokenException {
        return super.deleteResource(deleteRequest);
    }

    public <T> T getResource(HttpGet getRequest, Class<T> resourceClassType) throws ApiRequestFailedException, IncorrectTokenException {
        return super.getResource(getRequest, resourceClassType);
    }

    public <T> T putResource(HttpPut putRequest, Object content, Class<T> resourceClassType) throws ApiRequestFailedException, IncorrectTokenException {
        return super.putResource(putRequest, content, resourceClassType);
    }

    public <T> T postResource(HttpPost postRequest, Object content, Class<T> resourceClassType) throws IllegalApiContentException, IncorrectTokenException, HttpHostConnectException, ApiRequestFailedException {
        return super.postResource(postRequest, content, resourceClassType);
    }

    public int responsStatusCode(CloseableHttpResponse reponse) throws IOException, IncorrectTokenException {
        return super.responsStatusCode(reponse);
    }

    public <T> T responseResourceObject(CloseableHttpResponse reponse, Class<T> resourceClassType) throws IOException, IncorrectTokenException {
        return super.responseResourceObject(reponse, resourceClassType);
    }

    public String defaultAuthorizationHeaderValue(){
        return super.defaultAuthorizationHeaderValue();
    }
}
