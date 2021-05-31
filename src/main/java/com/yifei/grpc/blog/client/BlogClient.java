package com.yifei.grpc.blog.client;

import com.yifei.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import java.io.File;

public class BlogClient {

    public static void main(String[] args) {
        System.out.println("Starting log client ...");

        new BlogClient().run();
    }

    public void run() {
        ManagedChannel channel = NettyChannelBuilder
                .forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
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

        Blog updateBlog = Blog.newBuilder()
                .setId(createBlogResponse.getBlog().getId())
                .setAuthorId("Enjie")
                .setTitle("kids blog")
                .setContent("my kids journal")
                .build();

        UpdateBlogRequest updateBlogRequest = UpdateBlogRequest.newBuilder().setBlog(updateBlog).build();
        UpdateBlogResponse updateBlogResponse = syncClient.updateBlog(updateBlogRequest);
        System.out.println("Update blog response: " + updateBlogResponse.toString());

    }

}
