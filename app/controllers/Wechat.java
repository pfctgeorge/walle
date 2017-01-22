package controllers;

import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import models.R;
import models.exceptions.BizException;
import play.data.validation.Required;
import play.mvc.Before;

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

    @Before
    public static void checkEventFromWechat(@Required String timestamp, @Required String nonce, @Required String signature) {
        if (timestamp == null || nonce == null || signature == null) {
            badRequest();
        }
        String token = R.WECHAT_OPEN_TOKEN;
        List<String> signatureTokens = new ArrayList<>();
        signatureTokens.add(token);
        signatureTokens.add(timestamp);
        signatureTokens.add(nonce);
        Collections.sort(signatureTokens);
        if (signature == null || !signature.equals(sha1Hex(StringUtils.join(signatureTokens, "")))) {
            throw new BizException(R.ERROR.SIGN_ERROR);
        }
    }
}
