package id.ac.president.choreco.system.exception;

public class SignalProcessorException extends Exception{
    private String code;

    public SignalProcessorException(String code, String message) {
        super(message);
        this.setCode(code);
    }

    public SignalProcessorException(String code, String message, Throwable cause) {
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
