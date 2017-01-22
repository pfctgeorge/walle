package services.ws;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.io.InputStream;

import play.Logger;

/**
 * Created by pfctgeorge on 15/10/22.
 */
public class LogOnlyProcessor implements Processor<String> {

    private static final Processor _instance = new JsonProcessor();

    public static Processor getInstance() {
        return _instance;
    }

    protected LogOnlyProcessor() {
    }

    @Override
    public String process(HttpResponse response) {
        int httpStatus = response.getStatusLine().getStatusCode();
        if (httpStatus >= HttpStatus.SC_BAD_REQUEST) {
            throw new RuntimeException("fetch data error! status : " + httpStatus);
        }
        try (InputStream inputStream = response.getEntity().getContent()) {
            String respContent = IOUtils.toString(inputStream, "UTF-8");
            Logger.info("statusCode: " + httpStatus + " response : " + respContent);
        } catch (Exception e) {
            throw new RuntimeException("parse response result error! ", e);
        }
        return "";
    }
}
