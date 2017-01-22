package services.strategies.async;

import org.apache.http.entity.ContentType;

import services.ws.CommonWSHandler;
import services.ws.LogOnlyProcessor;
import services.ws.WSHandler;

/**
 * Created by pfctgeorge on 15/10/23.
 */
public class WechatPostStrategy implements AsyncExecuteStrategy {

    private String postBody;
    private String accessToken;
    private String url;

    public WechatPostStrategy(String url, String accessToken, String postBody) {
        this.postBody = postBody;
        this.accessToken = accessToken;
        this.url = url;
    }

    @Override
    public void execute() {
        WSHandler wsHandler = new CommonWSHandler(url);
        wsHandler.usePool(false);
        wsHandler.addUrlParams("access_token", accessToken);
        wsHandler.setContentType(ContentType.APPLICATION_JSON);
        wsHandler.buildPostBody(postBody);
        wsHandler.post();
        wsHandler.process(LogOnlyProcessor.getInstance());
    }

    @Override
    public String toString() {
        return "WechatPostStrategy{" +
               "postBody='" + postBody + '\'' +
               ", accessToken='" + accessToken + '\'' +
               ", url='" + url + '\'' +
               '}';
    }
}
