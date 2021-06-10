package com.yifei.grpc.calculator.client;

import com.yifei.calculator.*;
import com.yifei.grpc.interceptor.client.AppClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {
    public static void main(String[] args) throws SSLException {
        System.out.println("Starting calculator client...");
        new CalculatorClient().run();
    }

    public void run() throws SSLException {
        ManagedChannel channel = NettyChannelBuilder
                .forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
                .build();

        this.sum(channel);

        this.primeNumberDecomposition(channel);

        this.average(channel);

        this.max(channel);

        this.squareRoot(channel);

        System.out.println("\nShutting down channel");
        channel.shutdown();
    }

    private void squareRoot(ManagedChannel channel) {
        System.out.println("\nSquare Root");

        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncClient =
                CalculatorServiceGrpc.newBlockingStub(channel).withInterceptors(new AppClientInterceptor());

        try {
            SquareRootResponse response = syncClient.squareRoot(SquareRootRequest.newBuilder().setNumber(-100).build());
            System.out.println("Square root response: " + response.getResult());
        } catch (RuntimeException e) {
            System.out.println("Server side exception");
            e.printStackTrace();
        }
    }

    private void max(ManagedChannel channel) {
        System.out.println("\nMax Calculation");

        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel).withInterceptors(new AppClientInterceptor());

        StreamObserver<MaxRequest> streamObserver = asyncClient.max(new StreamObserver<MaxResponse>() {
            @Override
            public void onNext(MaxResponse value) {
                System.out.println("Max value from server: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Received error from server");
            }

            @Override
            public void onCompleted() {
                System.out.println("Server sent complete message");
            }
        });

        Arrays.asList(1,5,3,6,2,20).stream().forEach(value -> {
            System.out.println("Sending " + value + " to server");

            streamObserver.onNext(MaxRequest.newBuilder().setNumber(value).build());

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        streamObserver.onCompleted();
    }

    private void average(ManagedChannel channel) {
        System.out.println("\nAverage Calculation");

        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel).withInterceptors(new AppClientInterceptor());

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<AverageRequest> streamObserver = asyncClient.average(new StreamObserver<AverageResponse>() {
            @Override
            public void onNext(AverageResponse value) {
                System.out.println("Response from server: ");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Server error");
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server completed response");
                latch.countDown();
            }
        });

        sendRequest(streamObserver, 1);
        sendRequest(streamObserver, 2);
        sendRequest(streamObserver, 3);
        sendRequest(streamObserver, 4);

        streamObserver.onCompleted();

        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(StreamObserver<AverageRequest> streamObserver, int value) {
        System.out.println("Send average request: " + value);

        streamObserver.onNext(AverageRequest.newBuilder().setNumber(value).build());
    }

    private void sum(ManagedChannel channel) {
        System.out.println("\nSum Calculation");

        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncClient =
                CalculatorServiceGrpc.newBlockingStub(channel).withInterceptors(new AppClientInterceptor());

        CalculateRequest request = CalculateRequest.newBuilder()
                .setLeftOperator(3)
                .setRightOperator(10)
                .build();

        CalculateResponse response = syncClient.sum(request);
        System.out.println(request.getLeftOperator() + " + " +
                request.getRightOperator() + " = " +
                response.getResult());
    }

    private void primeNumberDecomposition(ManagedChannel channel) {
        System.out.println("\nPrime number decomposition");

        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncClient =
                CalculatorServiceGrpc.newBlockingStub(channel);

        PrimeNumberDecompositionRequest request =
                PrimeNumberDecompositionRequest.newBuilder()
                        .setNumber(120)
                        .build();

        System.out.println(request.getNumber() + " decomposition results: ");
        syncClient.primeNumberDecomposition(request).forEachRemaining(response -> {
            System.out.print(response.getResult() + ",");
        });
        System.out.println();
    }
}
