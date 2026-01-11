package cn.clazs.easymeeting.constant;

public class Constants {
    
    // ==================== Redis Key 前缀 ====================
    
    /**
     * 验证码 key 前缀
     */
    public static final String REDIS_KEY_CHECK_CODE = "easymeeting:checkcode:";
    
    /**
     * 用户 token key 前缀 (token -> TokenUserInfoDto)
     */
    public static final String REDIS_KEY_TOKEN = "easymeeting:token:";
    
    /**
     * 用户ID对应token key 前缀 (userId -> token)
     */
    public static final String REDIS_KEY_USER_TOKEN = "easymeeting:user:token:";
    
    /**
     * 用户信息缓存 key 前缀
     */
    public static final String REDIS_KEY_USER_INFO = "easymeeting:user:info:";
    
    /**
     * 会议信息缓存 key 前缀
     */
    public static final String REDIS_KEY_MEETING = "easymeeting:meeting:";
    
    /**
     * 会议房间 key 前缀
     */
    public static final String REDIS_KEY_MEETING_ROOM = "easymeeting:meeting:room:";

    /**
     * 会议房间 key 邀请人前缀
     */
    public static final String REDIS_KEY_MEETING_INVITE = "easymeeting:meeting:invite";
    
    // ==================== 过期时间（秒） ====================
    
    /**
     * 验证码过期时间：5分钟
     */
    public static final int REDIS_EXPIRE_CHECK_CODE = 5 * 60;

    /**
     * 邀请失效时间
     */
    public static final int REDIS_EXPIRE_INVITE =  3*60;
    
    /**
     * Token 过期时间：7天（秒）
     * 注意：此值需与 JwtUtil.EXPIRE_TIME 保持一致
     */
    public static final int REDIS_EXPIRE_TOKEN = 7 * 24 * 60 * 60;
    
    /**
     * Token 过期时间：7天（毫秒）
     * 注意：此值需与 JwtUtil.EXPIRE_TIME 保持一致
     */
    public static final long REDIS_EXPIRE_TOKEN_MS = 7L * 24 * 60 * 60 * 1000;
    
    /**
     * 用户信息缓存过期时间：1小时
     */
    public static final int REDIS_EXPIRE_USER_INFO = 60 * 60;
    
    /**
     * 会议信息缓存过期时间：24小时
     */
    public static final int REDIS_EXPIRE_MEETING = 24 * 60 * 60;

    
    // ==================== 分页默认值 ====================
    
    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NO = 1;
    
    /**
     * 默认每页条数
     */
    public static final int DEFAULT_PAGE_SIZE = 10;
    
    /**
     * 最大每页条数
     */
    public static final int MAX_PAGE_SIZE = 100;
    
    // ==================== 验证码相关 ====================
    
    /**
     * 验证码长度
     */
    public static final int CHECK_CODE_LENGTH = 4;
    
    /**
     * 验证码图片宽度
     */
    public static final int CHECK_CODE_WIDTH = 100;
    
    /**
     * 验证码图片高度
     */
    public static final int CHECK_CODE_HEIGHT = 40;
    
    // ==================== 消息中间件配置 ====================
    
    /**
     * 消息处理通道类型：Redis
     */
    public static final String MESSAGE_CHANNEL_REDIS = "redis";
    
    /**
     * 消息处理通道类型：RabbitMQ
     */
    public static final String MESSAGE_CHANNEL_RABBITMQ = "rabbitmq";
    
    /**
     * 消息处理通道类型：Kafka
     */
    public static final String MESSAGE_CHANNEL_KAFKA = "kafka";
    
    /**
     * Redis Pub/Sub 消息频道前缀
     */
    public static final String REDIS_CHANNEL_MESSAGE = "easymeeting:channel:message";
    
    /**
     * Redis Pub/Sub WebRTC信令频道
     */
    public static final String REDIS_CHANNEL_WEBRTC = "easymeeting:channel:webrtc";
    
    /**
     * Redis Pub/Sub 会议房间频道前缀
     */
    public static final String REDIS_CHANNEL_MEETING_ROOM = "easymeeting:channel:room:";
    
    /**
     * Redis Pub/Sub 用户私聊频道前缀
     */
    public static final String REDIS_CHANNEL_USER = "easymeeting:channel:user:";
    
    // ==================== RabbitMQ 配置 ====================
    
    /**
     * RabbitMQ 交换机名称
     */
    public static final String RABBITMQ_EXCHANGE_MESSAGE = "easymeeting.message.exchange";
    
    /**
     * RabbitMQ 消息队列名称
     */
    public static final String RABBITMQ_QUEUE_MESSAGE = "easymeeting.message.queue";
    
    /**
     * RabbitMQ WebRTC信令队列
     */
    public static final String RABBITMQ_QUEUE_WEBRTC = "easymeeting.webrtc.queue";
    
    /**
     * RabbitMQ 路由键：群组消息
     */
    public static final String RABBITMQ_ROUTING_KEY_GROUP = "message.group";
    
    /**
     * RabbitMQ 路由键：私聊消息
     */
    public static final String RABBITMQ_ROUTING_KEY_USER = "message.user";
    
    /**
     * RabbitMQ 路由键：WebRTC信令
     */
    public static final String RABBITMQ_ROUTING_KEY_WEBRTC = "message.webrtc";
    
    // ==================== Kafka 配置 ====================
    
    /**
     * Kafka Topic：消息
     */
    public static final String KAFKA_TOPIC_MESSAGE = "easymeeting-message";
    
    /**
     * Kafka Topic：WebRTC信令
     */
    public static final String KAFKA_TOPIC_WEBRTC = "easymeeting-webrtc";
    
    /**
     * Kafka 消费者组ID
     */
    public static final String KAFKA_GROUP_ID = "easymeeting-group";
    
    // ==================== 消息配置属性名 ====================
    
    /**
     * 配置属性：消息处理通道类型
     * 用于 @ConditionalOnProperty(name = MESSAGE_HANDLE_CHANNEL, havingValue = "redis")
     */
    public static final String MESSAGE_HANDLE_CHANNEL = "messaging.handle.channel";

    // ==================== 消息发送类型 ====================
    public static final String ZERO_STR = "0";
    public static final String ONE_STR = "1";

}
