package com.yifei.grpc.greeting.server;

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
}
