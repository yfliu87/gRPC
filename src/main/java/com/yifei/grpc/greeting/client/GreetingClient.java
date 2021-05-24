package com.yifei.grpc.greeting.client;

import com.yifei.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient {
    public static void main(String[] args) {
        System.out.println("gRPC client");

        //unary();

        serverStreaming();
    }

    private static void unary() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel);
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Yifei")
                .setLastName("Liu")
                .build();

        GreetRequest request = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        GreetResponse response = syncClient.greet(request);
        System.out.println("Greeting response: " + response.getResult());

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private static void serverStreaming() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetManyTimesRequest request = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Yifei").setLastName("Liu").build())
                .build();

        syncClient.greetManyTimes(request).forEachRemaining(response -> {
            System.out.println(response.getResult());
        });
    }


}
