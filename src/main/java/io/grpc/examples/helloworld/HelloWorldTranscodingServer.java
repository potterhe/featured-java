package io.grpc.examples.helloworld;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.grpc.GrpcService;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import java.util.logging.Logger;

/**
 * 基于 Armeria 的 HTTP/JSON 转码服务器，是 grpc-gateway 在 Java 生态的等价实现。
 *
 * <p>与 grpc-gateway（独立反向代理进程）不同，Armeria 将转码逻辑内嵌在 gRPC 服务器中，
 * 单个端口 (8080) 同时提供三种能力：
 * <ul>
 *   <li>RESTful JSON —— 依据 helloworld.proto 中 {@code google.api.http} 注解自动转码</li>
 *   <li>标准 gRPC —— HTTP/2 + application/grpc，与官网示例客户端/grpcurl 兼容</li>
 *   <li>gRPC 反射 —— 供 grpcurl list/describe 使用</li>
 * </ul>
 */
public class HelloWorldTranscodingServer {
  private static final Logger logger = Logger.getLogger(HelloWorldTranscodingServer.class.getName());

  public static void main(String[] args) throws Exception {
    GrpcService grpcService = GrpcService.builder()
        // 复用官网示例中 Greeter 服务的实现
        .addService(new HelloWorldServer.GreeterImpl())
        // gRPC 反射（Armeria 只允许注册一个反射服务，v1 协议），供 grpcurl 等工具发现服务
        .addService(ProtoReflectionServiceV1.newInstance())
        // 开启 HTTP/JSON 转码：按 proto 中的 google.api.http 注解暴露 REST 端点
        .enableHttpJsonTranscoding(true)
        .build();

    Server server = Server.builder()
        .http(8080)
        .service(grpcService)
        .build();

    server.start().join();
    logger.info("Server started, listening on 8080 (REST + gRPC)");
    logger.info("Try: curl http://localhost:8080/v1/greeter/world");

    // 阻塞主线程直到 JVM 关闭时优雅停止服务器
    server.closeOnJvmShutdown().join();
  }
}
