package com.github.zukarusan.choreco.component.exception;

public class SoundException extends Exception{
    private String code;

    public SoundException(String code, String message) {
        super(message);
        this.setCode(code);
    }

    public SoundException(String code, String message, Throwable cause) {
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
