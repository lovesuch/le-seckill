### 技术方案

#### 1. 系统架构设计

##### 1.1 微服务分层架构

系统采用经典的微服务分层架构，分为以下几层：

- **Controller 层**: 处理HTTP请求，进行参数校验和响应封装。
- **Service 层**: 包含业务逻辑编排和事务管理。
- **Mapper 层**: 使用MyBatis Plus进行数据库访问。
- **Entity 层**: 数据库实体映射。
- **DTO 层**: 数据传输对象，用于入参/出参分离。

##### 1.2 技术选型

- **后端框架**: Spring Boot
- **ORM 工具**: MyBatis Plus
- **数据库**: MySQL 8.0+
- **消息队列**: Kafka（可选）
- **缓存**: Redis
- **认证与授权**: JWT Token, RBAC
- **实时通知**: SSE

#### 2. 数据库设计

##### 2.1 表结构

根据PRD文档中的数据模型概要，以下是主要表的详细设计：

###### 2.1.1 用户参与状态表 (user_participation)

```sql
CREATE TABLE user_participation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    participation_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_activity(user_id, activity_id)
);
```

###### 2.1.2 微信现金红包表 (wechat_red_packets)

```sql
CREATE TABLE wechat_red_packets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT '未发放',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_status(user_id, status)
);
```

###### 2.1.3 微商城优惠券表 (mall_coupons)

```sql
CREATE TABLE mall_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_code VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT '未发放',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_status(user_id, status)
);
```

##### 2.2 数据库迁移

使用Flyway进行数据库迁移，确保每次部署时数据库结构的一致性。

```sql
-- V1__Create_tables.sql
CREATE TABLE user_participation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    participation_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_activity(user_id, activity_id)
);

CREATE TABLE wechat_red_packets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT '未发放',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_status(user_id, status)
);

CREATE TABLE mall_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_code VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT '未发放',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_status(user_id, status)
);
```

#### 3. 技术实现

##### 3.1 微信现金红包发放

使用微信支付API进行现金红包的发放。在Service层调用微信支付接口，处理红包发放逻辑。

```java
@Service
public class WechatRedPacketService {

    @Autowired
    private WechatPayClient wechatPayClient;

    public void sendRedPacket(Long userId, BigDecimal amount) {
        // 调用微信支付API发放红包
        wechatPayClient.sendRedPacket(userId, amount);
    }
}
```

##### 3.2 微商城优惠券发放

在Service层调用微商城系统的接口，处理优惠券的发放逻辑。

```java
@Service
public class MallCouponService {

    @Autowired
    private MallCouponClient mallCouponClient;

    public void sendCoupon(Long userId, String couponCode) {
        // 调用微商城系统接口发放优惠券
        mallCouponClient.sendCoupon(userId, couponCode);
    }
}
```

##### 3.3 高并发处理

使用Redis进行限流和缓存，确保在高并发情况下系统的稳定运行。

```java
@Service
public class RateLimitService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public boolean tryAcquire(String key, int permits) {
        return redisTemplate.opsForValue().increment(key, 1L) <= permits;
    }
}
```

##### 3.4 用户唯一性限制

在数据库中记录用户的参与状态，并在用户再次尝试参与时返回错误信息。

```java
@Service
public class ParticipationService {

    @Autowired
    private UserParticipationMapper userParticipationMapper;

    public boolean participate(Long userId, Long activityId) {
        // 检查用户是否已经参与过该活动
        if (userParticipationMapper.selectCount(new QueryWrapper<UserParticipation>()
                .eq("user_id", userId)
                .eq("activity_id", activityId)) > 0) {
            return false;
        }
        // 记录用户的参与状态
        UserParticipation participation = new UserParticipation();
        participation.setUserId(userId);
        participation.setActivityId(activityId);
        userParticipationMapper.insert(participation);
        return true;
    }
}
```

#### 4. 实时通知

使用SSE实现实时通知，前端通过SseEmitter进行事件监听。

```java
@RestController
public class NotificationController {

    @Autowired
    private SseEmitterManager sseEmitterManager;

    @GetMapping("/notifications")
    public SseEmitter getNotifications() {
        return sseEmitterManager.createEmitter();
    }
}
```

#### 5. 安全与认证

使用JWT Token进行用户认证，并通过RBAC进行权限控制。

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        if (token != null && jwtTokenUtil.validateToken(token)) {
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, AuthorityUtils.NO_AUTHORITIES);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

#### 6. 总结

以上技术方案详细描述了系统架构、数据库设计、技术实现、实时通知和安全与认证等方面的内容。通过这些设计，确保系统的稳定运行、高并发处理能力和用户参与状态的唯一性限制。