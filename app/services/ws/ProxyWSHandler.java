package services.ws;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Created by zhouxichao on 14/12/15.
 */
public class ProxyWSHandler extends CommonWSHandler {

    private String proxyHost;
    private int proxyPort;
    private String scheme;

    private CredentialsProvider credentialsProvider;

    @Override
    protected HttpClientBuilder getHttpClientBuilder() {
        HttpClientBuilder builder = super.getHttpClientBuilder();
        addProxyToHttpClientBuilder(builder);
        return builder;
    }

    @Override
    protected HttpClientBuilder getHttpClientBuilderWithPooling() {
        HttpClientBuilder builder = super.getHttpClientBuilderWithPooling();
        addProxyToHttpClientBuilder(builder);
        return builder;
    }

    private void addProxyToHttpClientBuilder(HttpClientBuilder builder) {
        if (credentialsProvider != null) {
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }

        RequestConfig.Builder requestConfigBuilder = getDefaultRequestConfigBuilder();
        HttpHost httpHost;
        if (StringUtils.isNotBlank(scheme)) {
            httpHost = new HttpHost(proxyHost, proxyPort);
        } else {
            httpHost = new HttpHost(proxyHost, proxyPort, scheme);
        }
        requestConfigBuilder.setProxy(httpHost);
        builder.setDefaultRequestConfig(requestConfigBuilder.build());
    }

    public ProxyWSHandler(String mainUrl, String proxyHost, int proxyPort) {
        super(mainUrl);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public ProxyWSHandler(String mainUrl, String proxyHost, int proxyPort, CredentialsProvider credentialsProvider) {
        super(mainUrl);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.credentialsProvider = credentialsProvider;
    }

    public ProxyWSHandler(String mainUrl, String proxyHost, int proxyPort, String scheme) {
        super(mainUrl);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.scheme = scheme;
    }

    public ProxyWSHandler(String mainUrl, String proxyHost, int proxyPort, String scheme, CredentialsProvider credentialsProvider) {
        super(mainUrl);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.scheme = scheme;
        this.credentialsProvider = credentialsProvider;
    }

}
