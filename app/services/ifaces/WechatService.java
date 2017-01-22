package services.ifaces;

/**
 * Created by pfctgeorge on 17/1/22.
 */
public interface WechatService {

    String handleEvent(String body) throws Exception;

    void pushWechatTextToUser(String openId, String content) throws Exception;
}
