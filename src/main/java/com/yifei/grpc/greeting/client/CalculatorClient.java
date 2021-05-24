package com.yifei.grpc.greeting.client;

import com.yifei.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClient {
    public static void main(String[] args) {
        System.out.println("Starting calculator client...");
        new CalculatorClient().run();
    }

    public void run() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncClient =
                CalculatorServiceGrpc.newBlockingStub(channel);

        this.sum(syncClient);

        this.primeNumberDecomposition(syncClient);

        System.out.println("\nShutting down channel");
        channel.shutdown();
    }

    private void sum(CalculatorServiceGrpc.CalculatorServiceBlockingStub syncClient) {
        CalculateRequest request = CalculateRequest.newBuilder()
                .setLeftOperator(3)
                .setRightOperator(10)
                .build();

        CalculateResponse response = syncClient.sum(request);
        System.out.println("\n" + request.getLeftOperator() + " + " +
                request.getRightOperator() + " = " +
                response.getResult() + "\n");
    }

    private void primeNumberDecomposition(CalculatorServiceGrpc.CalculatorServiceBlockingStub syncClient) {
        PrimeNumberDecompositionRequest request =
                PrimeNumberDecompositionRequest.newBuilder()
                        .setNumber(120)
                        .build();

        System.out.println("\n" + request.getNumber() + " decomposition results: ");
        syncClient.primeNumberDecomposition(request).forEachRemaining(response -> {
            System.out.print(response.getResult() + ",");
        });
        System.out.println();
    }
}
