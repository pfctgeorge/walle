package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;

import inject.DefaultModule;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import redis.clients.jedis.ShardedJedis;

/**
 * Created by pfctgeorge on 17/1/22.
 */
public class R {

    public static final Injector inject = Guice.createInjector(new DefaultModule());

    public static final ThreadLocal<ShardedJedis> shardedJedisTL = new ThreadLocal<>();
    public static final ThreadLocal<ShardedJedis> jedisTL = new ThreadLocal<>();

    public static final ObjectMapper sharedMapper = new ObjectMapper();

    static {
        try {
            sharedMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            sharedMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public static class REDIS_CACHE_KEY {

        public static final String WECHAT_OPEN_ACCESS_TOKEN = "_walle_c_k_wechat_open_access_token";

    }

    public static class WECHAT_URL {

        public static final String SEND_MESSAGE_URL = Play.configuration.getProperty("wechat.send.message.url");
        public static final String REQUEST_OPEN_ACCESS_TOKEN_URL = Play.configuration.getProperty("wechat.request.access.token.url");

    }

    public static final String WECHAT_OPEN_TOKEN = Play.configuration.getProperty("wechat.open.token");

    public static enum ERROR {
        SIGN_ERROR(601);
        int code;

        ERROR(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return Messages.get("error.info." + name());
        }
    }


}
