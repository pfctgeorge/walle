package services.ws;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by zhouxichao on 14/11/20. default web service handler
 */
public class CommonWSHandler extends AbstractWSHandler {

    private String mainUrl;

    public CommonWSHandler(String mainUrl) {
        this.mainUrl = mainUrl;
    }

    @Override
    protected URI buildUri(List<NameValuePair> urlParams) throws URISyntaxException {
        URIBuilder builder = new URIBuilder();
        //handle like this: www.baidu.com/w?wd=1 -> wd=1 => query, www.baidu.com/w => http://www.baidu.com/w
        if (mainUrl.contains("?")) {
            String query = mainUrl.substring(mainUrl.indexOf("?") + 1);
            builder.addParameters(URLEncodedUtils.parse(query, Consts.UTF_8));
            mainUrl = mainUrl.substring(0, mainUrl.indexOf("?"));
        }
        if (!mainUrl.contains("://")) {
            mainUrl = "http://" + mainUrl;
        }
        builder.setPath(mainUrl);
        builder.addParameters(urlParams);
        return builder.build();
    }
}
