package controllers;

import org.apache.commons.lang.StringUtils;

/**
 * Created by pfctgeorge on 17/1/22.
 */
public class Wechat extends BasicController {

    public static void handleEvent(String echostr) throws Exception {
        if (StringUtils.isNotEmpty(echostr)) {
            renderText(echostr);
        }
        renderJSON(wechatService.handleEvent(request.params.get("body")));
    }

}
