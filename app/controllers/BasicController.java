package controllers;

import com.google.inject.Inject;

import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Util;
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

    @Util
    public static String getUserAgent() {
        Object ua = request.headers.get("user-agent");
        return ua != null ? ua.toString() : "";
    }
}
