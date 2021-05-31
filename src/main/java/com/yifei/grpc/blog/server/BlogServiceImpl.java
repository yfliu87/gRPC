package com.yifei.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.yifei.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.LinkedList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase mongoDatabase = mongoClient.getDatabase("blogDB");
    private MongoCollection<Document> collection = mongoDatabase.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
        System.out.println("Received create blog request");

        Blog blog = request.getBlog();
        Document doc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        collection.insertOne(doc);

        String id = doc.get("_id").toString();
        System.out.println("blog id: " + id);

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id).build())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
        System.out.println("Received read blog request");

        String blogId = request.getBlogId();

        Document result = this.collection.find(eq("_id", new ObjectId(blogId))).first();

        if (result == null) {
            System.out.println("No blog found");
            responseObserver.onError(Status.NOT_FOUND.withDescription("No blog with requested id found").asRuntimeException());
        } else {
            System.out.println("Blog found and send response now");

            responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(documentToBlog(result)).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        System.out.println("Received update blog request");

        String blogId = request.getBlog().getId();

        Document result = this.collection.find(eq("_id", new ObjectId(blogId))).first();

        if (result == null) {
            System.out.println("No blog found");
            responseObserver.onError(Status.NOT_FOUND.withDescription("No blog with requested id found").asRuntimeException());
        } else {
            System.out.println("Blog found and update now");

            Document replacement = new Document()
                    .append("_id", new ObjectId(blogId))
                    .append("author_id", request.getBlog().getAuthorId())
                    .append("title", request.getBlog().getTitle())
                    .append("content", request.getBlog().getContent());

            this.collection.replaceOne(eq("_id", result.getString("_id")), replacement);

            responseObserver.onNext(UpdateBlogResponse.newBuilder().setBlog(documentToBlog(replacement)).build());
            responseObserver.onCompleted();
        }
    }

    private Blog documentToBlog(Document document) {
        return Blog.newBuilder()
                .setId(document.getObjectId("_id").toString())
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .build();
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {
        System.out.println("Received delete blog request");

        String blogId = request.getBlogId();

        DeleteResult result = this.collection.deleteOne(eq("_id", new ObjectId(blogId)));

        if (result.getDeletedCount() == 0) {
            System.out.println("No blog found to delete");
            responseObserver.onError(Status.NOT_FOUND.withDescription("No blog with requested id found").asRuntimeException());
        } else {
            System.out.println("Blog found and delete now");

            responseObserver.onNext(DeleteBlogResponse.newBuilder().setBlogId(blogId).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void batchListBlog(BatchListBlogRequest request, StreamObserver<BatchListBlogResponse> responseObserver) {
        System.out.println("Received batch list blog request");

        List<Blog> blogs = new LinkedList<>();

        this.collection.find().limit(request.getCount()).iterator().forEachRemaining(
                doc -> blogs.add(documentToBlog(doc))
        );

        responseObserver.onNext(BatchListBlogResponse.newBuilder().addAllBlog(blogs).build());
        responseObserver.onCompleted();
    }

    @Override
    public void streamListBlog(StreamListBlogRequest request, StreamObserver<StreamListBlogResponse> responseObserver) {
        System.out.println("Received stream list blog request");

        this.collection.find().iterator().forEachRemaining(
                doc -> responseObserver.onNext(StreamListBlogResponse.newBuilder().setBlog(documentToBlog(doc)).build())
        );
        responseObserver.onCompleted();
    }
}
