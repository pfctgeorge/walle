package services.ws;


import org.apache.http.HttpResponse;

/**
 * User: zhouxc Date: 12-11-26 Time: 下午6:11
 */
public interface Processor<T> {

    T process(HttpResponse response);
}
