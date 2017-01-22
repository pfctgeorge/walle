package models.exceptions;

public class WechatLoginException extends RuntimeException {

    private int errorCode;
    private String errorMessage;

    public WechatLoginException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
