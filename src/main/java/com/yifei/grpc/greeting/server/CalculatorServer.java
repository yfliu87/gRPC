package com.yifei.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class CalculatorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        new CalculatorServer().start();
    }

    public void start() throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051).addService(new CalculatorServiceImpl()).build();

        server.start();
        System.out.println("Calculator server started");

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("Received shutdown request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();
    }
}
