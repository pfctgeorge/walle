package models.exceptions;

import models.R;
import play.mvc.Http;

/**
 * Created by pfctgeorge on 17/1/22.
 */
public class BizException extends RuntimeException {

    int statusCode;
    String msg;

    public BizException(R.ERROR rError) {
        super(rError == null ? null : rError.getMessage());
        this.statusCode = rError == null ? Http.StatusCode.INTERNAL_ERROR : rError.getCode();
        this.msg = rError == null ? null : rError.getMessage();
    }

    public BizException(int statusCode, String msg){
        super(msg);
        this.statusCode = statusCode;
        this.msg = msg;
    }

    public BizException(String msg){
        super(msg);
        this.msg = msg;
        this.statusCode = Http.StatusCode.INTERNAL_ERROR;
    }

    public BizException(int statusCode){
        super();
        this.statusCode = statusCode;
    }

    public int getStatusCode(){
        return statusCode;
    }

    public String getMsg(){
        return msg;
    }

}
