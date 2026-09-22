package io.grpc.examples.helloworld;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple client that requests a greeting from the {@link HelloWorldServer}.
 *
 * <p>对应 gRPC 官网 Java 示例：examples/src/main/java/io/grpc/examples/helloworld/HelloWorldClient.java
 */
public class HelloWorldClient {
  private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

  private final GreeterGrpc.GreeterBlockingStub blockingStub;

  /**
   * Construct client for accessing HelloWorld server using the existing channel.
   */
  public HelloWorldClient(Channel channel) {
    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
    // shut it down.

    // Passing Channels to code like this should be considered a normal practice, not a hack.
    blockingStub = GreeterGrpc.newBlockingStub(channel);
  }

  /**
   * Say hello to server.
   */
  public void greet(String name) {
    logger.info("Will try to greet " + name + " ...");
    HelloRequest request = HelloRequest.newBuilder().setName(name).build();
    HelloReply response;
    try {
      response = blockingStub.withDeadlineAfter(5, TimeUnit.SECONDS).sayHello(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    logger.info("Greeting: " + response.getMessage());
  }

  /**
   * Greet server. If provided, the first element of {@code args} is the name to use in the
   * greeting. The second element is the target server.
   */
  public static void main(String[] args) throws Exception {
    // Access a service running on the local machine on port 50051
    String target = "localhost:50051";
    // Allow passing in the user name as the first command line argument.
    String name = "world";
    if (args.length > 0) {
      if ("--help".equals(args[0])) {
        System.err.println("Usage: [name [target]]");
        System.err.println("");
        System.err.println("  name    The name you wish to be greeted by. Defaults to " + name);
        System.err.println("  target  The server to connect to. Defaults to " + target);
        System.exit(1);
      }
      name = args[0];
    }
    if (args.length > 1) {
      target = args[1];
    }

    // Create a communication channel to the server, known as a Channel. Channels are thread-safe
    // and reusable. It is common to create channels at the beginning of your application and reuse
    // them until the application shuts down.
    //
    // For the example we allow the channel to use plaintext (insecure) because we are running it
    // locally. When running against a real server you should use TLS.
    ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
        .build();
    try {
      HelloWorldClient client = new HelloWorldClient(channel);
      client.greet(name);
    } finally {
      // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
      // resources the channel should be shut down when it will not be used. If you're using
      // a language that has a {@code finally} clause or {@code try-with-resources}, use that.
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
