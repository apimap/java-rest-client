import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.client.RestClientConfiguration;
import io.apimap.client.exception.ApiRequestFailedException;
import io.apimap.client.exception.IllegalApiContentException;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseRestClientTest {
    @Test
    void getResource_didFailConnectionError(){
        RestClientConfiguration configuration = new RestClientConfiguration();
        SurrogateBaseRestClient client = new SurrogateBaseRestClient(configuration);

        assertThrows(HttpHostConnectException.class,
                ()->{
                    ApiDataRestEntity object = new ApiDataRestEntity();
                    client.postResource(new HttpPost(new java.net.URI("http://localhost:1")), object, ApiDataRestEntity.class);
                });
    }

    @Test
    void getResource_didFail5xxError() throws IOException {
        RestClientConfiguration configuration = new RestClientConfiguration();

        StatusLine status = mock(StatusLine.class);
        when(status.getStatusCode()).thenReturn(500);

        HttpEntity entity = mock(HttpEntity.class);
        String initialString = "";
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(initialString.getBytes()));

        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        when(httpResponse.getStatusLine()).thenReturn(status);
        when(httpResponse.getEntity()).thenReturn(entity);

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        when(httpClient.execute(any())).thenReturn(httpResponse);

        SurrogateBaseRestClient client = new SurrogateBaseRestClient(configuration, httpClient);

        assertThrows(ApiRequestFailedException.class,
                ()->{
                    ApiDataRestEntity object = new ApiDataRestEntity();
                    client.postResource(new HttpPost(new java.net.URI("http://localhost:8080")), object, ApiDataRestEntity.class);
                });
    }

    @Test
    void createResource_didFail4xxError() throws IOException {
        RestClientConfiguration configuration = new RestClientConfiguration();

        StatusLine status = mock(StatusLine.class);
        when(status.getStatusCode()).thenReturn(401);

        HttpEntity entity = mock(HttpEntity.class);
        String initialString = "";
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(initialString.getBytes()));

        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        when(httpResponse.getStatusLine()).thenReturn(status);
        when(httpResponse.getEntity()).thenReturn(entity);

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        when(httpClient.execute(any())).thenReturn(httpResponse);

        SurrogateBaseRestClient client = new SurrogateBaseRestClient(configuration, httpClient);

        assertThrows(IllegalApiContentException.class,
                ()->{
                    ApiDataRestEntity object = new ApiDataRestEntity();
                    client.postResource(new HttpPost(new java.net.URI("http://localhost:8080")), object, ApiDataRestEntity.class);
                });
    }
}
