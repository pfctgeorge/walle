package services;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import models.R;
import play.Logger;
import play.Play;
import redis.clients.jedis.Jedis;
import services.ifaces.WechatService;
import services.strategies.async.WechatPostStrategy;
import services.ws.CommonWSHandler;
import services.ws.WSHandler;
import services.ws.WechatResponseProcessor;

/**
 * Created by pfctgeorge on 17/1/22.
 */
public class WechatServiceImpl extends BasicService implements WechatService {

    @Override
    public Object handleEvent(String body) throws Exception {
        Document doc;
        Logger.info("wechat event body :" + body);
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(IOUtils.toInputStream(body));
        Map<String, Object> params = convertXMLToMap(doc);
        System.out.println(params);
        String messageType = params.get("MsgType").toString();
        if ("event".equals(messageType)) {
            String event = params.get("Event").toString();
            if ("subscribe".equals(event)) {
                String openId = params.get("FromUserName").toString();
                Logger.info("start handle subscribe event.");
                return getWechatFormatXML(buildWechatTextResponse(openId, "Hello world!"));
            }
        }
        return "";
    }

    private Map<String, Object> buildWechatTextResponse(String openId, String text) {
        Map<String, Object> map = new HashMap<>();
        map.put("ToUserName", openId);
        map.put("FromUserName", R.WECHAT_DEV_ACCOUNT);
        map.put("CreateTime", System.currentTimeMillis() / 1000);
        map.put("MsgType", "text");
        map.put("Content", text);
        return map;
    }

    @Override
    public void pushWechatTextToUser(String openId, String content) throws Exception {
        Logger.info("start push wechat text.");

        Map<String, Object> postBody = new HashMap<>();
        postBody.put("touser", openId);
        postBody.put("msgtype", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("content", content);

        postBody.put("text", text);

        asyncService.execute(new WechatPostStrategy(R.WECHAT_URL.SEND_MESSAGE_URL, getAccessToken(), new String(R.sharedMapper.writeValueAsString(postBody).getBytes(), "ISO-8859-1")));
        Logger.info("wechat text: " + content + " pushed to " + openId);
    }

    private String getAccessToken() {
        String accessTokenInRedis = requestAccessTokenFromRedis();
        if (accessTokenInRedis == null) {
            return requestAccessTokenFromWechat();
        }
        return accessTokenInRedis;
    }

    private String requestAccessTokenFromRedis() {
        String cacheKey = R.REDIS_CACHE_KEY.WECHAT_OPEN_ACCESS_TOKEN;
        Jedis jedis = getJedisFromPool(cacheKey);
        return jedis.get(cacheKey);
    }

    private String requestAccessTokenFromWechat() {
        String appid = Play.configuration.getProperty("wechat.open.appid");
        String secret = Play.configuration.getProperty("wechat.open.app.secret");
        String grantType = "client_credential";
        WSHandler wsHandler = new CommonWSHandler(R.WECHAT_URL.REQUEST_OPEN_ACCESS_TOKEN_URL);
        wsHandler.addParam("appid", appid);
        wsHandler.addParam("secret", secret);
        wsHandler.addParam("grant_type", grantType);
        wsHandler.get();
        JsonNode response = wsHandler.process(WechatResponseProcessor.getInstance());
        String accessToken = response.path("access_token").asText();
        String cacheKey = R.REDIS_CACHE_KEY.WECHAT_OPEN_ACCESS_TOKEN;
        Jedis jedis = getJedisFromPool(cacheKey);
        jedis.setex(cacheKey, 110 * 60, accessToken);
        return accessToken;
    }

    private Map<String, Object> convertXMLToMap(Document doc) {
        Map<String, Object> result = new HashMap<>();
        NodeList nodeList = doc.getFirstChild().getChildNodes();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(i);
            if (node.getNodeName().startsWith("#")) {
                continue;
            }
            result.put(node.getNodeName(), node.getTextContent());
        }
        return result;
    }

    private String getWechatFormatXML(Map<String, Object> params) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element rootElement = doc.createElement("xml");
        doc.appendChild(rootElement);

        for (Map.Entry<String, Object> e : params.entrySet()) {
            Element element = doc.createElement(e.getKey());
            element.appendChild(doc.createTextNode(e.getValue().toString()));
            rootElement.appendChild(element);
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        return new String(writer.toString().getBytes(), "ISO8859-1");
    }
}
