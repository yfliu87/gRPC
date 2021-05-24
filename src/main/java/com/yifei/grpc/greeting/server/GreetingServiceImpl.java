package com.yifei.grpc.greeting.server;

import com.yifei.greet.*;
import io.grpc.stub.StreamObserver;

public class GreetingServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        Greeting greet = request.getGreeting();
        String firstName = greet.getFirstName();

        GreetResponse response = GreetResponse.newBuilder().setResult("hello " + firstName).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        Greeting greet = request.getGreeting();
        String firstName = greet.getFirstName();

        for (int i = 0; i < 10; i++) {
            GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                    .setResult("response: " + i + ", hello " + firstName)
                    .build();

            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }
}
