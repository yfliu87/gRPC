package com.yifei.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.yifei.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

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

        Document blog = this.collection.find(eq("_id", new ObjectId(blogId))).first();

        if (blog == null) {
            System.out.println("No blog found");
            responseObserver.onError(Status.NOT_FOUND.withDescription("No blog with requested id found").asRuntimeException());
        } else {
            System.out.println("Blog found and send response now");

            Blog response = Blog.newBuilder()
                    .setId(blog.getString("_id"))
                    .setAuthorId(blog.getString("author_id"))
                    .setTitle(blog.getString("title"))
                    .setContent(blog.getString("content"))
                    .build();

            responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(response).build());
            responseObserver.onCompleted();
        }
    }
}
