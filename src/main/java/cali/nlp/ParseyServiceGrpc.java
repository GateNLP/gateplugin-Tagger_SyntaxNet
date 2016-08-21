package cali.nlp;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.0.0)",
    comments = "Source: parsey_api.proto")
public class ParseyServiceGrpc {

  private ParseyServiceGrpc() {}

  public static final String SERVICE_NAME = "cali.nlp.ParseyService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<cali.nlp.ParseyApi.ParseyRequest,
      cali.nlp.ParseyApi.ParseyResponse> METHOD_PARSE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "cali.nlp.ParseyService", "Parse"),
          io.grpc.protobuf.ProtoUtils.marshaller(cali.nlp.ParseyApi.ParseyRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(cali.nlp.ParseyApi.ParseyResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ParseyServiceStub newStub(io.grpc.Channel channel) {
    return new ParseyServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ParseyServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ParseyServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static ParseyServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ParseyServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class ParseyServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void parse(cali.nlp.ParseyApi.ParseyRequest request,
        io.grpc.stub.StreamObserver<cali.nlp.ParseyApi.ParseyResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PARSE, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_PARSE,
            asyncUnaryCall(
              new MethodHandlers<
                cali.nlp.ParseyApi.ParseyRequest,
                cali.nlp.ParseyApi.ParseyResponse>(
                  this, METHODID_PARSE)))
          .build();
    }
  }

  /**
   */
  public static final class ParseyServiceStub extends io.grpc.stub.AbstractStub<ParseyServiceStub> {
    private ParseyServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ParseyServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ParseyServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ParseyServiceStub(channel, callOptions);
    }

    /**
     */
    public void parse(cali.nlp.ParseyApi.ParseyRequest request,
        io.grpc.stub.StreamObserver<cali.nlp.ParseyApi.ParseyResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PARSE, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ParseyServiceBlockingStub extends io.grpc.stub.AbstractStub<ParseyServiceBlockingStub> {
    private ParseyServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ParseyServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ParseyServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ParseyServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public cali.nlp.ParseyApi.ParseyResponse parse(cali.nlp.ParseyApi.ParseyRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PARSE, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ParseyServiceFutureStub extends io.grpc.stub.AbstractStub<ParseyServiceFutureStub> {
    private ParseyServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ParseyServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ParseyServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ParseyServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<cali.nlp.ParseyApi.ParseyResponse> parse(
        cali.nlp.ParseyApi.ParseyRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PARSE, getCallOptions()), request);
    }
  }

  private static final int METHODID_PARSE = 0;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ParseyServiceImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(ParseyServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PARSE:
          serviceImpl.parse((cali.nlp.ParseyApi.ParseyRequest) request,
              (io.grpc.stub.StreamObserver<cali.nlp.ParseyApi.ParseyResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    return new io.grpc.ServiceDescriptor(SERVICE_NAME,
        METHOD_PARSE);
  }

}
