package com.yifei.grpc.blog.server;

import com.yifei.grpc.interceptor.server.AppServerInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.File;
import java.io.IOException;

public class BlogServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        new BlogServer().start();
    }

    public void start() throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051)
                .addService(ServerInterceptors.intercept(new BlogServiceImpl(), new AppServerInterceptor()))
                .addService(ProtoReflectionService.newInstance())
                .useTransportSecurity(new File("ssl/server.crt"), new File("ssl/server.pem"))
                .build();

        server.start();
        System.out.println("Blog server started");

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("Received shutdown request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();
    }
}
