package com.yifei.grpc.interceptor.client;

import io.grpc.*;

import java.util.logging.Logger;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class AppClientInterceptor implements ClientInterceptor {

    private static final Logger logger = Logger.getLogger(AppClientInterceptor.class.getName());

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        logger.info("======= [Client Interceptor] : Invoking Remote Method - " + method.getFullMethodName());

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void sendMessage(ReqT message) {
                logger.info(String.format("Sending method '%s' message '%s'%n", method.getFullMethodName(), message.toString()));
                super.sendMessage(message);
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                logger.info(AppClientInterceptor.class.getSimpleName());

                ClientCall.Listener<RespT> listener = new ForwardingClientCallListener<RespT>() {
                    @Override
                    protected Listener<RespT> delegate() {
                        return responseListener;
                    }

                    @Override
                    public void onMessage(RespT message) {
                        logger.info(String.format("Received message '%s'%n", message.toString()));
                        super.onMessage(message);
                    }
                };

                headers.put(Metadata.Key.of("md", ASCII_STRING_MARSHALLER), "metadata of client request");

                super.start(listener, headers);
            }
        };
    }
}
