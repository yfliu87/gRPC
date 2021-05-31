package com.yifei.grpc.blog.client;

import com.yifei.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {

    public static void main(String[] args) {
        System.out.println("Starting log client ...");

        new BlogClient().run();
    }

    public void run() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        BlogServiceGrpc.BlogServiceBlockingStub syncClient = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setAuthorId("Yifei")
                .setTitle("gRPC blog")
                .setContent("my first gRPC blog")
                .build();

        CreateBlogRequest createBlogRequest = CreateBlogRequest.newBuilder().setBlog(blog).build();
        CreateBlogResponse createBlogResponse = syncClient.createBlog(createBlogRequest);
        System.out.println("Create blog response: " + createBlogResponse.toString());

        ReadBlogRequest readBlogRequest = ReadBlogRequest.newBuilder().setBlogId(createBlogResponse.getBlog().getId()).build();
        ReadBlogResponse readBlogResponse = syncClient.readBlog(readBlogRequest);
        System.out.println("Read blog response: " + readBlogResponse.toString());
    }

}
