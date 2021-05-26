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

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {
        StreamObserver<LongGreetRequest> streamObserverRequest = new StreamObserver<LongGreetRequest>() {

            StringBuilder buffer = new StringBuilder();

            @Override
            public void onNext(LongGreetRequest value) {
                buffer.append("Hello " + value.getGreeting().getFirstName() + "\n");
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Received error from client: " + t.getMessage());

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(LongGreetResponse.newBuilder().setResult(buffer.toString()).build());
                responseObserver.onCompleted();
            }
        };

        return streamObserverRequest;
    }

    @Override
    public void greetWithDeadline(GreetWithDeadlineRequest request, StreamObserver<GreetWithDeadlineResponse> responseObserver) {
        String req = request.getGreeting().getFirstName();

        try {
            Thread.sleep(100);

            responseObserver.onNext(GreetWithDeadlineResponse.newBuilder()
                    .setResult("Hello " + req).build());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
