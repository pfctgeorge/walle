package services.ws;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import models.R;


public class JsonProcessor implements Processor<JsonNode> {

    private static final Logger logger = LoggerFactory.getLogger(JsonProcessor.class);
    private static final Processor<JsonNode> _instance = new JsonProcessor();

    public static Processor<JsonNode> getInstance() {
        return _instance;
    }

    protected JsonProcessor() {

    }

    @Override
    public JsonNode process(HttpResponse response) {
        int httpStatus = response.getStatusLine().getStatusCode();
        if (httpStatus >= HttpStatus.SC_BAD_REQUEST) {
            throw new RuntimeException("fetch data error! status : " + httpStatus);
        }
        try (InputStream inputStream = response.getEntity().getContent()) {
            String respContent = IOUtils.toString(inputStream, "UTF-8");
            logger.info("response : " + respContent);
            try {
                return R.sharedMapper.readValue(respContent, JsonNode.class);
            } catch (Exception e) {
                logger.error(respContent, e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException("parse response result error! ", e);
        }
    }
}
