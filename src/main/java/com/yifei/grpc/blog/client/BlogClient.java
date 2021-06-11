package com.yifei.grpc.blog.client;

import com.yifei.blog.*;
import com.yifei.grpc.interceptor.client.AppClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import javax.net.ssl.SSLException;
import java.io.File;

public class BlogClient {

    public static void main(String[] args) throws SSLException {
        System.out.println("Starting log client ...");

        new BlogClient().run();
    }

    public void run() throws SSLException {
        ManagedChannel channel = NettyChannelBuilder
                .forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
                .build();

        BlogServiceGrpc.BlogServiceBlockingStub syncClient = BlogServiceGrpc.newBlockingStub(channel).withInterceptors(new AppClientInterceptor());

        Blog blog = Blog.newBuilder()
                .setAuthorId("Yifei")
                .setTitle("gRPC blog")
                .setContent("my first gRPC blog")
                .build();

        CreateBlogRequest createBlogRequest = CreateBlogRequest.newBuilder().setBlog(blog).build();
        CreateBlogResponse createBlogResponse = syncClient.withCompression("gzip").createBlog(createBlogRequest);
        System.out.println("Create blog response: " + createBlogResponse.toString());

        ReadBlogRequest readBlogRequest = ReadBlogRequest.newBuilder().setBlogId(createBlogResponse.getBlog().getId()).build();
        ReadBlogResponse readBlogResponse = syncClient.withCompression("gzip").readBlog(readBlogRequest);
        System.out.println("Read blog response: " + readBlogResponse.toString());

        Blog updateBlog = Blog.newBuilder()
                .setId(createBlogResponse.getBlog().getId())
                .setAuthorId("Enjie")
                .setTitle("kids blog")
                .setContent("my kids journal")
                .build();

        UpdateBlogRequest updateBlogRequest = UpdateBlogRequest.newBuilder().setBlog(updateBlog).build();
        UpdateBlogResponse updateBlogResponse = syncClient.withCompression("gzip").updateBlog(updateBlogRequest);
        System.out.println("Update blog response: " + updateBlogResponse.toString());

        DeleteBlogRequest deleteBlogRequest = DeleteBlogRequest.newBuilder().setBlogId(createBlogResponse.getBlog().getId()).build();
        DeleteBlogResponse deleteBlogResponse = syncClient.withCompression("gzip").deleteBlog(deleteBlogRequest);
        System.out.println("Delete blog response: " + deleteBlogResponse.toString());

        BatchListBlogRequest batchListBlogRequest = BatchListBlogRequest.newBuilder().setCount(3).build();
        BatchListBlogResponse batchListBlogResponse = syncClient.withCompression("gzip").batchListBlog(batchListBlogRequest);
        batchListBlogResponse.getBlogList().forEach(b -> System.out.println(b.toString()));

        StreamListBlogRequest streamListBlogRequest = StreamListBlogRequest.newBuilder().build();
        syncClient.withCompression("gzip").streamListBlog(streamListBlogRequest).forEachRemaining(
                streamListBlogResponse -> System.out.println(streamListBlogResponse.toString())
        );

    }

}
