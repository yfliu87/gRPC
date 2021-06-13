package com.yifei.grpc.greeting.server;

import com.yifei.grpc.interceptor.server.BasicAuthInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.File;
import java.io.IOException;

public class GreetingServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello gRPC");

        Server server = ServerBuilder.forPort(50051)
                .addService(ServerInterceptors.intercept(new GreetingServiceImpl(), new BasicAuthInterceptor()))
                .addService(ProtoReflectionService.newInstance())
                .useTransportSecurity(new File("ssl/server.crt"), new File("ssl/server.pem"))
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            System.out.println("Received shutdown request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();
    }
}
