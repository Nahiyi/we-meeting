# Netty 核心进阶知识点整理

## 1. 启动与配置 (Bootstrap、Configuration)

### `handler()` 与 `childHandler()` 的区别
在 `ServerBootstrap` 中，这两个方法的职责截然不同：
*   **`handler()`**：
    *   **作用对象**：`ServerSocketChannel`（父通道，负责监听端口）。
    *   **运行线程**：**BossGroup**。
    *   **场景**：通常用于添加 `LoggingHandler` 监控连接接入情况，或者服务器启动过程的日志。
*   **`childHandler()`**：
    *   **作用对象**：`SocketChannel`（子通道，负责实际的数据读写）。
    *   **运行线程**：**WorkerGroup**。
    *   **场景**：绝大多数业务逻辑（编解码、心跳、业务处理）都在这里配置。
*   **客户端注意**：客户端 `Bootstrap` 只有 `handler()` 方法，因为它没有父子通道之分。

---

## 2. Handler 体系与设计模式

### 三大核心 Handler 的区别
| 类名 | 处理方向 | 内存管理 (Release) | 泛型支持 | 适用场景 |
| :--- | :--- | :--- | :--- | :--- |
| **ChannelInboundHandlerAdapter** | 入站 (In) | **手动** (需 `ReferenceCountUtil.release`) | 无 | 中间件、自定义解码器基类、透传处理 |
| **SimpleChannelInboundHandler** | 入站 (In) | **自动** (方法结束后自动 release) | **有** | 业务逻辑末端、处理特定类型消息 |
| **ChannelDuplexHandler** | 双向 (In+Out) | **手动** | 无 | 心跳检测、状态监控、统计流量 |

### `@Sharable` 注解与单例模式
*   **判断标准**：Handler 是否**有状态**（即是否持有属于当前连接的成员变量）。
    *   **有状态**（如编解码器的累积缓存、计数器）：必须每次 `new`，**不可共享**。
    *   **无状态**（如鉴权逻辑、引用 Spring Bean）：可以做成单例。
*   **强制性**：如果复用同一个 Handler 实例但**不加** `@Sharable` 注解，Netty 会在第二次添加时抛出异常。这是 Netty 的防御机制，防止非线程安全的代码被误用。

---

## 3. 内存管理与引用计数 (Reference Counting)

### `SimpleChannelInboundHandler` 的透传陷阱
*   **机制**：该类会在 `channelRead0` 执行完毕后自动释放消息内存（引用计数 -1）。
*   **问题**：如果在 `channelRead0` 中调用 `ctx.fireChannelRead(msg)` 透传给下一个 Handler，而没有做额外处理，方法结束后内存会被释放，导致下一个 Handler 访问非法内存。
*   **解决**：若需在 `SimpleChannelInboundHandler` 中透传消息，**必须先调用 `msg.retain()`**。
    ```java
    // 正确写法
    ctx.fireChannelRead(msg.retain());
    ```

---

## 4. 心跳检测机制 (Heartbeat)

### `IdleStateHandler` 的工作原理
*   **构造参数**：`readerIdleTime` (读空闲)、`writerIdleTime` (写空闲)、`allIdleTime` (所有空闲)。
*   **职责**：它只负责**计时**和**触发事件**，**不负责关闭连接**。当超时发生时，它会向下游发送 `IdleStateEvent`。

### 处理空闲事件的正确姿势
通常配合 `ChannelDuplexHandler`（或 InboundHandler）在 `userEventTriggered` 方法中处理：
```java
@Override
public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
        // 1. 识别心跳事件并处理（如关闭连接或发送 Ping）
        ctx.close();
    } else {
        // 2. 【关键】非心跳事件必须透传！
        // 否则 SSL 握手等其他事件会在此中断，导致 Bug。
        super.userEventTriggered(ctx, evt);
    }
}
```

---

## 5. 状态存储 (State Management)

### `AttributeMap` vs `ThreadLocal`
*   **ThreadLocal (禁止使用)**：Netty 是 Reactor 模型，一个 EventLoop 线程会处理成百上千个 Channel。使用 ThreadLocal 会导致不同 Channel 的数据串台，或数据丢失。
*   **Channel.attr() (必须使用)**：
    *   **机制**：数据绑定在 `Channel` 对象本身上，随连接存活。
    *   **用法**：使用 `static final AttributeKey` 作为键。
    ```java
    // 定义 Key
    public static final AttributeKey<String> USER_ID = AttributeKey.valueOf("uid");
    // 存
    ctx.channel().attr(USER_ID).set("1001");
    // 取
    String uid = ctx.channel().attr(USER_ID).get();
    ```

---

## 6. WebSocket 协议实战

### URL 参数与路由
*   **握手阶段 (HTTP)**：可以携带参数（如 `ws://host/chat?uid=1`）。
    *   **获取方式**：在 WebSocket 握手 Handler 之前，添加自定义 Handler 拦截 `FullHttpRequest`，解析 `req.uri()` 并将参数存入 `Channel.attr()`。
*   **传输阶段 (WebSocket Frame)**：连接建立后，**没有** URL、Header、Method 的概念，只有二进制帧。
*   **如何路由**：必须在**应用层**定义子协议（如 JSON 格式）。
    *   *Payload 示例*：`{ "type": "LOGIN", "data": {...} }`
    *   服务器解析 JSON 中的 `type` 字段，分发到不同的 Service 处理。