
package com.gan.common;

public class GanException extends RuntimeException {

    public GanException() {
    }

    public GanException(String message) {
        super(message);
    }

    /**
     * 丢出一个异常
     *
     * @param message
     */
    public static void fail(String message) {
        throw new GanException(message);
    }

}
