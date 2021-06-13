package com.yifei.grpc.interceptor.server;

import io.grpc.*;

import java.util.Base64;
import java.util.logging.Logger;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class BasicAuthInterceptor implements ServerInterceptor {

    private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {};
    private static final String ADMIN_USER_CREDENTIALS = "admin:admin";
    private static final Context.Key<String> USER_ID_CTX_KEY = Context.key("userId");
    private static final String ADMIN_USER_ID = "admin";
    private static final Logger logger = Logger.getLogger(BasicAuthInterceptor.class.getName());

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String basicAuthInfo = headers.get(Metadata.Key.of("authorization", ASCII_STRING_MARSHALLER));

        if (basicAuthInfo == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("Basic authentication info is missing in metadata"), headers);
            return NOOP_LISTENER;
        }

        if (validRequest(basicAuthInfo)) {
            Context ctx = Context.current().withValue(USER_ID_CTX_KEY, ADMIN_USER_ID);
            return Contexts.interceptCall(ctx, call, headers, next);
        } else {
            logger.info("verification failed - unauthorized request");
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid request"), headers);
            return NOOP_LISTENER;
        }
    }

    private boolean validRequest(String basicAuthInfo) {
        String token = basicAuthInfo.substring("Basic ".length()).trim();
        byte[] byteArray = Base64.getDecoder().decode(token.getBytes());
        return ADMIN_USER_CREDENTIALS.equals(new String(byteArray));
    }
}
