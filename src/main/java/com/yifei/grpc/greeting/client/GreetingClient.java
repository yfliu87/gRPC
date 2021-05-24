package com.yifei.grpc.greeting.client;

import com.yifei.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {
    public static void main(String[] args) {
        System.out.println("starting gRPC client...");
        new GreetingClient().run();
    }

    public void run() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        unary(channel);

        serverStreaming(channel);

        clientStreaming(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void unary(ManagedChannel channel) {
        System.out.println("\nUnary...");

        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel);
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Yifei")
                .setLastName("Liu")
                .build();

        GreetRequest request = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        GreetResponse response = syncClient.greet(request);
        System.out.println("Unary Greeting response: " + response.getResult());
    }

    private void serverStreaming(ManagedChannel channel) {
        System.out.println("\nServer streaming ...");

        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetManyTimesRequest request = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Yifei").setLastName("Liu").build())
                .build();

        syncClient.greetManyTimes(request).forEachRemaining(response -> {
            System.out.println(response.getResult());
        });
    }

    private void clientStreaming(ManagedChannel channel) {
        System.out.println("\nClient streaming ...");

        CountDownLatch latch = new CountDownLatch(1);

        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        StreamObserver<LongGreetRequest> requestStreamObserver =
                asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
                    @Override
                    public void onNext(LongGreetResponse value) {
                        System.out.println("Response from server: ");
                        System.out.println(value.getResult());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println("Server error");
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Server completed response");
                        latch.countDown();
                    }
                });

        sendRequest(requestStreamObserver, "Yifei");
        sendRequest(requestStreamObserver, "Enjie");
        sendRequest(requestStreamObserver, "Daniel");
        sendRequest(requestStreamObserver, "Natalie");

        requestStreamObserver.onCompleted();

        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(StreamObserver<LongGreetRequest> requestStreamObserver, String name) {
        System.out.println("Sending request for " + name);

        requestStreamObserver.onNext(
                LongGreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder().setFirstName(name).build())
                        .build());
    }
}
