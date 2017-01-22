package models.exceptions;

/**
 * Created by pfctgeorge on 17/1/22.
 */
public class BizException extends RuntimeException {

    int statusCode;
    String msg;

}
