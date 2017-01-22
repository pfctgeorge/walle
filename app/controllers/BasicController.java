package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;

import models.R;
import models.exceptions.BizException;
import play.Logger;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Util;
import redis.clients.jedis.ShardedJedis;
import services.ifaces.WechatService;

/**
 * Created by pfctgeorge on 17/1/22.
 */
public class BasicController extends Controller {

    @Inject
    protected static WechatService wechatService;

    private static final String TIME_STAT_KEY = "_time_stat_key_";

    @Before
    public static void controllerInit() {
        request.args.put(TIME_STAT_KEY, System.currentTimeMillis());
    }

    @Finally
    public static void controllerEnd() {
        try {
            long time = Long.parseLong(request.args.get(TIME_STAT_KEY).toString());
            long executionTime = System.currentTimeMillis() - time;
            Logger.info(request.actionMethod + " " + request.url + " spent " + executionTime + "ms" + " ua : " + getUserAgent());
        } catch (Throwable ignored) {
        }
    }

    @Finally
    public static void clean() {
        ShardedJedis shardedJedis = R.shardedJedisTL.get();
        if (shardedJedis != null) {
            shardedJedis.close();
        }
        R.shardedJedisTL.remove();
    }

    @Catch(value = BizException.class, priority = 0)
    protected static void handleHttpException(BizException e) {
        if (e.getStatusCode() != 4101 && e.getStatusCode() != 601) {
            Logger.error(e.getMessage(), e);
        }
        renderJSON(toJson(getErrorApiResponse(e.getStatusCode(), e.getMessage())));
    }

    @Catch(value = Throwable.class, priority = 1)
    protected static void handleUnexpectedException(Throwable e) throws Throwable {
        throw e;
    }

    @Util
    public static String getUserAgent() {
        Object ua = request.headers.get("user-agent");
        return ua != null ? ua.toString() : "";
    }

    @Util
    public static ApiResponse getApiResponse() {
        return new ApiResponse();
    }

    @Util
    public static ApiResponse getApiResponse(Object data) {
        ApiResponse apiResponse=getApiResponse();
        apiResponse.setResponse(data);
        return apiResponse;
    }

    @Util
    public static ApiResponse getBadArgumentApiResponse(String message) {
        ApiResponse apiResponse = getApiResponse();
        apiResponse.getResponseHeader().status = 400;
        apiResponse.getResponseHeader().msg = message;
        return apiResponse;
    }

    @Util
    public static ApiResponse getErrorApiResponse(int code, String message) {
        ApiResponse apiResponse = getApiResponse();
        apiResponse.getResponseHeader().status = code;
        apiResponse.getResponseHeader().msg = message;
        return apiResponse;
    }

    @Util
    public static String toJson(Object o) {
        try {
            return R.sharedMapper.writeValueAsString(o);
        } catch (Exception e) {
            Logger.error(e, e.getMessage());
            throw new BizException(500);
        }
    }

    protected static class ApiResponse {

        static final ObjectMapper RESPONSE_OBJECT_MAPPER = R.sharedMapper;
        private static final ObjectWriter RESPONSE_OBJECT_WRITER = RESPONSE_OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

        public Object getResponse() {
            return response;
        }

        public void setResponse(Object response) {
            this.response = response;
        }

        public ResponseHeader getResponseHeader() {
            return responseHeader;
        }

        public void setResponseHeader(ResponseHeader responseHeader) {
            this.responseHeader = responseHeader;
        }

        public static class ResponseHeader {

            public int status = 200;
            public String msg = null;
            public String version = null;

            public String toString() {
                return ToStringBuilder.reflectionToString(this);
            }
        }

        private Object response = Collections.emptyMap();
        private ResponseHeader responseHeader = new ResponseHeader();

        public String buildJsonResponse() {
            return buildJsonResponse(false);
        }

        public String buildJsonResponse(boolean pretty) {
            try {
                String json;
                if (pretty) {
                    json = RESPONSE_OBJECT_WRITER.writeValueAsString(this);
                } else {
                    json = RESPONSE_OBJECT_MAPPER.writeValueAsString(this);
                }
                return json;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
