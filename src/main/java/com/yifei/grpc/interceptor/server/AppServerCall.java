package com.yifei.grpc.interceptor.server;

import io.grpc.ForwardingServerCall;
import io.grpc.MethodDescriptor;

import java.util.logging.Logger;

public class AppServerCall<ReqT, RespT> extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {

    private static final Logger logger = Logger.getLogger(AppServerCall.class.getName());

    public AppServerCall(io.grpc.ServerCall<ReqT, RespT> delegate) {
        super(delegate);
    }

    @Override
    protected io.grpc.ServerCall<ReqT, RespT> delegate() {
        return super.delegate();
    }

    @Override
    public MethodDescriptor<ReqT, RespT> getMethodDescriptor() {
        return super.getMethodDescriptor();
    }

    @Override
    public void sendMessage(RespT message) {
        logger.info("Message from Service -> Client : " + message);
        super.sendMessage(message);
    }
}
