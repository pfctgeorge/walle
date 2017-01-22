package services.ws;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import models.R;

/**
 * User: zhouxc Date: 13-2-20 Time: 下午5:55
 */
public abstract class AbstractWSHandler implements WSHandler {

    private static Logger logger = LoggerFactory.getLogger(AbstractWSHandler.class);
    private String body = null;
    private boolean ignoreResponse = false;
    private URI uri;
    protected List<NameValuePair> params = new ArrayList<>();
    protected List<NameValuePair> urlParams = new ArrayList<>();
    protected List<Header> headers = new ArrayList<>();
    private HttpResponse response = null;
    private File file = null;
    protected HttpEntity httpEntity;
    protected ContentType contentType;
    protected HttpContext httpContext;
    //http or https, default http
    protected X509HostnameVerifier hostnameVerifier;
    protected SSLContext sslContext;
    private int retryTimes = 3;
    //when get response, check whether need retry
    private RetryConditionWithResponse retryConditionWithResponse = IGNORE_RETRY_WITH_ANY_RESPONSE;
    private static RetryConditionWithResponse IGNORE_RETRY_WITH_ANY_RESPONSE = new RetryConditionWithResponse() {
        @Override
        public boolean condition(HttpResponse response) {
            return false;
        }
    };
    //use pool or not
    private boolean usePool = false;

    private int connectTimeout = 1000;
    private int socketTimeout = 1000;

    private CredentialsProvider credentialsProvider;

    @Override
    public WSHandler buildPostBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public List<NameValuePair> getParams() {
        return params;
    }

    @Override
    public List<NameValuePair> getUrlParams() {
        return urlParams;
    }


    protected RequestConfig.Builder getDefaultRequestConfigBuilder() {
        return RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).setConnectionRequestTimeout(1000);
    }

    protected HttpClientBuilder getHttpClientBuilder() {
        HttpClientBuilder builder;
        if (sslContext != null) {
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            builder = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory);
        } else {
            builder = HttpClientBuilder.create();
        }
        RequestConfig defaultRequestConfig = getDefaultRequestConfigBuilder().build();
        return builder.setDefaultRequestConfig(defaultRequestConfig);
    }

    protected final static Map<String, PoolingHttpClientConnectionManager> cachedHttpsConnectionManagerMap = new HashMap<>();
    protected static PoolingHttpClientConnectionManager httpConnectionManager;

    protected HttpClientBuilder getHttpClientBuilderWithPooling() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (sslContext == null) {
            if (httpConnectionManager == null) {
                httpConnectionManager = new PoolingHttpClientConnectionManager();
                httpConnectionManager.setMaxTotal(65);
                //every target host is a route, just host, exclude path and query
                httpConnectionManager.setDefaultMaxPerRoute(10);
            }
            builder.setConnectionManager(httpConnectionManager);
        } else {
            String cacheKey = sslContext.hashCode() + "|" + (hostnameVerifier == null ? 0 : hostnameVerifier.hashCode());
            if (cachedHttpsConnectionManagerMap.get(cacheKey) == null) {
                SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
                Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslConnectionSocketFactory).build();
                PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
                connectionManager.setMaxTotal(65);
                connectionManager.setDefaultMaxPerRoute(5);
                cachedHttpsConnectionManagerMap.put(cacheKey, connectionManager);
            }
            builder.setConnectionManager(cachedHttpsConnectionManagerMap.get(cacheKey));
        }
        RequestConfig defaultRequestConfig = getDefaultRequestConfigBuilder().build();
        builder.setDefaultRequestConfig(defaultRequestConfig);
        return builder;
    }

    abstract protected URI buildUri(List<NameValuePair> urlParams) throws URISyntaxException;

    public void file(File file) {
        this.file = file;
    }

    @Override
    public WSHandler useSSL(SSLContext sslContext, X509HostnameVerifier hostnameVerifier) {
        this.sslContext = sslContext;
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    @Override
    public WSHandler ignoreResponse() {
        this.ignoreResponse = true;
        return this;
    }

    @Override
    public WSHandler setEntity(HttpEntity httpEntity) {
        this.httpEntity = httpEntity;
        return this;
    }

    @Override
    public WSHandler setContentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public WSHandler addParam(String name, Object value) {
        this.params.add(new BasicNameValuePair(name, value.toString()));
        return this;
    }

    public WSHandler addParams(List<NameValuePair> params) {
        this.params.addAll(params);
        return this;
    }

    public WSHandler addHeader(Header header) {
        this.headers.add(header);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T process(Processor<T> processor) {
        if (ignoreResponse) {
            throw new RuntimeException("ignoreResponse is true, you can't call any processor");
        }
        try {
            return processor.process(response);
        } finally {
            if (!(processor instanceof BareResponseProcessor)) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }


    @Override
    public WSHandler post() {
        try {
            HttpPost request = postForm();
            logger.debug("url : " + this.uri.toString());
            if (body != null) {
                logger.debug("post body : " + this.body);
            }
            if (params != null) {
                logger.debug("param: " + R.sharedMapper.writeValueAsString(this.params));
            }
            executeWithRetry(request);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 同时有params, body和file时，只能发送一种参数，优先级是file < body < params
     */
    public HttpPost postForm() throws Exception {
        this.uri = buildUri(this.urlParams);
        HttpPost httpPost = new HttpPost(this.uri);
        if (httpEntity != null) {
            httpPost.setEntity(httpEntity);
        } else if (this.params != null && !this.params.isEmpty()) {
            UrlEncodedFormEntity encodedFormEntity = new UrlEncodedFormEntity(this.params, Charset.forName("UTF-8"));
            httpPost.setEntity(encodedFormEntity);
        } else if (this.body != null) {
            StringEntity entity = new StringEntity(this.body);
            if (contentType == null) {
                contentType = ContentType.APPLICATION_FORM_URLENCODED;
            }
            entity.setContentType(contentType.getMimeType());
            httpPost.setEntity(entity);
        } else if (this.file != null) {
            FileEntity entity = null;
            if (contentType == null) {
                entity = new FileEntity(this.file);
            } else {
                entity = new FileEntity(this.file, contentType);
            }
            httpPost.setEntity(entity);
        }

        return httpPost;
    }

    @Override
    public WSHandler postASYNC() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WSHandler get() {
        try {
            HttpGet httpGet = getForm();
            logger.info("url : " + this.uri.toString());
            logger.debug("url : " + this.uri.toString());
            executeWithRetry(httpGet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private HttpGet getForm() throws Exception {
        HttpGet httpGet = new HttpGet();
        if (this.params != null && this.params.size() > 0) {
            this.urlParams.addAll(this.params);
        }
        this.uri = buildUri(this.urlParams);
        httpGet.setURI(this.uri);
        return httpGet;
    }

    @Override
    public WSHandler getASYNC() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WSHandler addUrlParams(Object... params) {
        if (params.length % 2 != 0) {
            throw new RuntimeException("parameters not match!");
        }
        for (int i = 0; i < params.length; i = i + 2) {
            this.urlParams.add(new BasicNameValuePair(params[i].toString(), params[i + 1].toString()));
        }
        return this;
    }

    @Override
    public WSHandler retry(int times) {
        if (times > -1) {
            this.retryTimes = times;
        }
        return this;
    }

    @Override
    public WSHandler retry(int times, RetryConditionWithResponse retryConditionWithResponse) {
        if (times > -1) {
            this.retryTimes = times;
            this.retryConditionWithResponse = retryConditionWithResponse;
        }
        return this;
    }

    @Override
    public WSHandler usePool(boolean flag) {
        this.usePool = flag;
        return this;
    }

    /**
     * Determines the timeout in milliseconds until a connection is established. A timeout value of
     * zero is interpreted as an infinite timeout. <p/> A timeout value of zero is interpreted as an
     * infinite timeout. A negative value is interpreted as undefined (system default). <p/>
     */
    public WSHandler setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    public WSHandler setHttpContext(HttpContext httpContext) {
        this.httpContext = httpContext;
        return this;
    }

    /**
     * Defines the socket timeout (<code>SO_TIMEOUT</code>) in milliseconds, which is the timeout
     * for waiting for data  or, put differently, a maximum period inactivity between two
     * consecutive data packets). <p/> A timeout value of zero is interpreted as an infinite
     * timeout. A negative value is interpreted as undefined (system default). <p/> Default:
     * <code>-1</code>
     */
    public WSHandler setSocketTimeout(int timeout) {
        this.socketTimeout = timeout;
        return this;
    }

    public WSHandler useCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    /**
     * retry after execute failed
     */
    private void executeWithRetry(HttpRequestBase requestBase, int alreadyRetryTimes)
        throws Exception {
        try {
            HttpClientBuilder httpClientBuilder;
            if (usePool) {
                httpClientBuilder = getHttpClientBuilderWithPooling();
            } else {
                httpClientBuilder = getHttpClientBuilder();
            }
            if (credentialsProvider != null) {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            HttpClient httpClient = httpClientBuilder.build();
            requestBase.setHeaders(headers.toArray(new Header[headers.size()]));
            if (contentType != null) {
                requestBase.setHeader(HttpHeaders.CONTENT_TYPE, contentType.getMimeType());
            }
            this.response = httpClient.execute(requestBase, httpContext);
            if (this.retryConditionWithResponse != null) {
                if (this.retryConditionWithResponse.condition(this.response)) {
                    throw new RuntimeException();
                }
            }
            if (ignoreResponse && this.response != null) {
                EntityUtils.consumeQuietly(this.response.getEntity());
            }
        } catch (Exception e) {
            if (alreadyRetryTimes < retryTimes) {
                logger.warn("execute error with url {} {} times, and i will try again", requestBase.getURI(), alreadyRetryTimes);
                Thread.sleep(100);
                requestBase.reset();
                executeWithRetry(requestBase, alreadyRetryTimes + 1);
            } else {
                String causeMsg = "";
                try {
                    causeMsg = IOUtils.toString(this.response.getEntity().getContent());
                    EntityUtils.consume(this.response.getEntity()); //release conn when error
                } catch (Exception ine) {
                    //NOOP
                }
                throw new RuntimeException(causeMsg, e);
            }
        }
    }

    private void executeWithRetry(HttpRequestBase requestBase) throws Exception {
        executeWithRetry(requestBase, 0);
    }

    /**
     * retry condition with response
     */
    public interface RetryConditionWithResponse {

        public boolean condition(HttpResponse response);
    }

}
