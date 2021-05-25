package com.yifei.grpc.calculator.server;

import com.yifei.calculator.*;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(CalculateRequest request, StreamObserver<CalculateResponse> responseObserver) {
        int leftOperator = request.getLeftOperator();
        int rightOperator = request.getRightOperator();

        CalculateResponse response = CalculateResponse.newBuilder()
                .setResult(leftOperator + rightOperator)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
        int number = request.getNumber();
        int k = 2;

        while(number > 1) {
            if (number % k == 0) {
                PrimeNumberDecompositionResponse response = PrimeNumberDecompositionResponse.newBuilder().setResult(k).build();
                responseObserver.onNext(response);
                number /= k;
            } else {
                k++;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<AverageRequest> average(StreamObserver<AverageResponse> responseObserver) {
        StreamObserver<AverageRequest> streamObserverRequest = new StreamObserver<AverageRequest>() {

            int sum = 0;
            int count = 0;

            @Override
            public void onNext(AverageRequest value) {
                sum += value.getNumber();
                count++;
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Received error from client");
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(AverageResponse.newBuilder().setResult(1.0*sum/count).build());
                responseObserver.onCompleted();
            }
        };

        return streamObserverRequest;
    }
}
