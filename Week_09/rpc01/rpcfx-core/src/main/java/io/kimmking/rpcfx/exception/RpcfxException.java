package io.kimmking.rpcfx.exception;

import java.io.IOException;

/**
 * @author : Luyz
 * @date : 2020/12/16 11:15
 */
public class RpcfxException extends Exception {
    public RpcfxException() {
        super();
    }

    public RpcfxException(String message) {
        super(message);
    }

    public RpcfxException(String message, Throwable cause) {
        super(message, cause);
    }
}
