# featured-java

```shell
mvn compile exec:java -Dexec.mainClass=io.grpc.examples.helloworld.HelloWorldServer

# 列出服务（依赖反射）
grpcurl -plaintext localhost:50051 list

# 查看服务/消息定义
grpcurl -plaintext localhost:50051 describe helloworld.Greeter
grpcurl -plaintext localhost:50051 describe helloworld.HelloRequest

# 调用 SayHello（-d 传 JSON，格式为 包名.服务名/方法名）
grpcurl -plaintext -d '{"name":"world"}' localhost:50051 helloworld.Greeter/SayHello

# 服务端未开反射时，用 -proto 指定 proto 文件也可调用
grpcurl -plaintext -proto src/main/proto/helloworld.proto \
  -d '{"name":"proto-file"}' localhost:50051 helloworld.Greeter/SayHello
```