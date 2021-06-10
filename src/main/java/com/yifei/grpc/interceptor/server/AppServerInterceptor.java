package com.yifei.grpc.interceptor.server;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;

import java.util.logging.Logger;

public class AppServerInterceptor implements io.grpc.ServerInterceptor {

    private static final Logger logger = Logger.getLogger(AppServerInterceptor.class.getName());

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        logger.info("======= [Server Interceptor] : Remote Method Invoked - " + call.getMethodDescriptor().getFullMethodName());

        ServerCall<ReqT, RespT> serverCall = new AppServerCall<>(call);
        return new AppServerCallListener<>(next.startCall(serverCall, headers));
    }
}
