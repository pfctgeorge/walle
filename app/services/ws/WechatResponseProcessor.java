package services.ws;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.http.HttpResponse;

import models.exceptions.WechatLoginException;

/**
 * Created by pfctgeorge on 15/8/10.
 */
public class WechatResponseProcessor extends JsonProcessor {

    private WechatResponseProcessor() {
        super();
    }

    @Override
    public JsonNode process(HttpResponse response) {
        JsonNode jsonNode = super.process(response);
        if (jsonNode.has("errcode") && jsonNode.path("errcode").asInt() != 0) {
            throw new WechatLoginException(jsonNode.path("errcode").asInt(), jsonNode.path("errmsg").asText());
        }
        return jsonNode;
    }
}
