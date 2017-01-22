package services.ws;

import org.apache.http.HttpResponse;

/**
 * User: zhouxc Date: 13-2-21 Time: 上午10:25
 *
 * just return response
 *
 * <b>make sure close response after used</b>
 */
public class BareResponseProcessor implements Processor<HttpResponse> {

    private static final Processor<HttpResponse> _instance = new BareResponseProcessor();

    public static Processor<HttpResponse> getInstance() {
        return _instance;
    }

    private BareResponseProcessor() {

    }

    @Override
    public HttpResponse process(HttpResponse response) {
        return response;
    }
}
