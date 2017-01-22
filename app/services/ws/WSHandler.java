package services.ws;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.util.List;

import javax.net.ssl.SSLContext;

/**
 * User: zhouxc Date: 12-11-26 Time: 下午6:14
 */
public interface WSHandler {

    public static final String GET = "get";
    public static final String POST = "post";

    //use ssl
    public WSHandler useSSL(SSLContext sslContext, X509HostnameVerifier hostnameVerifier);

    //use credentials
    public WSHandler useCredentialsProvider(CredentialsProvider credentialsProvider);

    public WSHandler addParam(String name, Object value);

    public WSHandler addParams(List<NameValuePair> params);

    public WSHandler addHeader(Header header);

    List<NameValuePair> getParams();

    List<NameValuePair> getUrlParams();

    public WSHandler buildPostBody(String body);

    public <T> T process(Processor<T> processor);

    public WSHandler post();

    public WSHandler ignoreResponse();

    public WSHandler postASYNC();

    public WSHandler get();

    public WSHandler getASYNC();

    public void file(File file);

    public WSHandler addUrlParams(Object... params);

    //retry times after failed
    public WSHandler retry(int times);

    public WSHandler retry(int times, AbstractWSHandler.RetryConditionWithResponse retryConditionWithResponse);

    //use pool or not, default true
    public WSHandler usePool(boolean flag);

    public WSHandler setEntity(HttpEntity httpEntity);

    public WSHandler setContentType(ContentType contentType);

    public WSHandler setConnectTimeout(int timeout);

    public WSHandler setSocketTimeout(int timeout);

    public WSHandler setHttpContext(HttpContext httpContext);
}
