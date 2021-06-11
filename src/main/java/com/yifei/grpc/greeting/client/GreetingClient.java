package com.yifei.grpc.greeting.client;

import com.yifei.greet.*;
import com.yifei.grpc.interceptor.client.AppClientInterceptor;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {
    public static void main(String[] args) throws SSLException {
        System.out.println("starting gRPC client...");
        new GreetingClient().run();
    }

    public void run() throws SSLException {
        ManagedChannel channel = NettyChannelBuilder
                .forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
                .build();

        unary(channel);

        serverStreaming(channel);

        clientStreaming(channel);

        withDeadline(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void unary(ManagedChannel channel) {
        System.out.println("\nUnary...");

        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel).withInterceptors(new AppClientInterceptor());
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Yifei")
                .setLastName("Liu")
                .build();

        GreetRequest request = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        GreetResponse response = syncClient.withCompression("gzip").greet(request);
        System.out.println("Unary Greeting response: " + response.getResult());
    }

    private void serverStreaming(ManagedChannel channel) {
        System.out.println("\nServer streaming ...");

        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel).withInterceptors(new AppClientInterceptor());

        GreetManyTimesRequest request = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Yifei").setLastName("Liu").build())
                .build();

        syncClient.withCompression("gzip").greetManyTimes(request).forEachRemaining(response -> {
            System.out.println(response.getResult());
        });
    }

    private void clientStreaming(ManagedChannel channel) {
        System.out.println("\nClient streaming ...");

        CountDownLatch latch = new CountDownLatch(1);

        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel).withInterceptors(new AppClientInterceptor());

        StreamObserver<LongGreetRequest> requestStreamObserver =
                asyncClient.withCompression("gzip").longGreet(new StreamObserver<LongGreetResponse>() {
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

    private void withDeadline(ManagedChannel channel) {
        System.out.println("\nWith Deadline");

        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel)
                .withDeadline(Deadline.after(50, TimeUnit.MILLISECONDS))
                .withInterceptors(new AppClientInterceptor());

        try {
            GreetWithDeadlineResponse response = syncClient.withCompression("gzip").greetWithDeadline(
                    GreetWithDeadlineRequest.newBuilder().setGreeting(
                            Greeting.newBuilder().setFirstName("Yifei").build()
                    ).build());

            System.out.println("With deadline response: " + response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.println("Deadline exceeded");
            } else {
                e.printStackTrace();
            }
        }
    }
}
