package id.ac.president.choreco.component.exception;

public class STFTException extends Exception{
    private String code;

    public STFTException(String code, String message) {
        super(message);
        this.setCode(code);
    }

    public STFTException(String code, String message, Throwable cause) {
        super(message, cause);
        this.setCode(code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
