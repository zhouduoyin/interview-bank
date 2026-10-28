package com.interviewbank.service;

import com.interviewbank.domain.*;
import com.interviewbank.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SeedService {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final InterviewRecordRepository recordRepository;

    @Value("${interview-bank.data-dir:./data}")
    private String dataDir;

    @Transactional
    public void seedIfEmpty() {
        File marker = new File(dataDir, ".seeded");
        if (marker.exists()) return;
        if (questionRepository.count() > 0) {
            touch(marker);
            return;
        }

        Map<String, Category> cats = new LinkedHashMap<>();
        cats.put("Java 基础", category("Java 基础", "#8a6420", "集合、并发、JVM 等", 1));
        cats.put("Spring", category("Spring", "#3f5b45", "IoC、AOP、事务、Spring Boot", 2));
        cats.put("数据库", category("数据库", "#2e5a74", "MySQL、索引、事务、优化", 3));
        cats.put("Redis", category("Redis", "#b8432a", "缓存、分布式锁、数据结构", 4));
        cats.put("计算机网络", category("计算机网络", "#6b5b95", "TCP/IP、HTTP、HTTPS", 5));
        cats.put("操作系统", category("操作系统", "#7f5226", "进程线程、内存、IO", 6));
        cats.put("前端", category("前端", "#2f7d7a", "Vue、浏览器、工程化", 7));
        cats.put("算法与数据结构", category("算法与数据结构", "#5f7334", "手写题、复杂度", 8));
        cats.put("项目与场景", category("项目与场景", "#96445a", "系统设计、线上问题排查", 9));
        cats.put("HR / 软技能", category("HR / 软技能", "#665d4e", "自我介绍、离职原因、反问", 10));

        Instant now = Instant.now();
        List<Question> qs = new ArrayList<>();

        qs.add(q("HashMap 的底层结构与扩容机制？", cats.get("Java 基础"),
                "面试官追问：JDK 1.8 中 HashMap 为什么引入红黑树？扩容时链表节点是如何迁移的？",
                """
                        ## 一句话回答

                        JDK 1.8 的 HashMap = **数组 + 链表 + 红黑树**。默认容量 16、负载因子 0.75，元素数超过 `容量 × 0.75` 时扩容为原来的 2 倍。

                        ## 关键细节

                        - **下标计算**：`(n - 1) & hash`，要求容量是 2 的幂，等价于取模但更快。
                        - **哈希扰动**：`h ^ (h >>> 16)`，让高位参与运算，减少碰撞。
                        - **树化条件**：链表长度 ≥ 8 且数组长度 ≥ 64，否则优先扩容。
                        - **扩容迁移**：1.8 不再重新计算 hash，而是根据 `hash & oldCap` 把节点拆成 **lo / hi 两条链表**，分别落到原位置和 `原位置 + oldCap` 上。
                        - **线程不安全**：并发 put 可能丢数据；需要并发时用 `ConcurrentHashMap`。

                        ```java
                        // 1.8 扩容时的拆分逻辑（简化）
                        if ((e.hash & oldCap) == 0) {
                            // 留在低位链
                        } else {
                            // 移到 原索引 + oldCap
                        }
                        ```

                        ## 为什么树化阈值是 8？

                        哈希分布近似泊松分布，链表长度达到 8 的概率约 0.00000006，属于极小概率事件；阈值设 8 是为了在极端哈希冲突时兜底，而不是常态。
                        """,
                Difficulty.HARD, "集合,源码,HashMap", "字节跳动", "Java 后端",
                "可以顺着问：为什么负载因子是 0.75？-> 空间与冲突概率的折中。", true));

        qs.add(q("JVM 运行时内存区域怎么划分？", cats.get("Java 基础"),
                "哪些区域是线程私有的？哪些会 OOM？",
                """
                        ## 线程私有

                        - **程序计数器 PC**：当前字节码行号，唯一不会 OOM 的区域。
                        - **虚拟机栈**：方法调用栈帧，栈深度溢出抛 `StackOverflowError`。
                        - **本地方法栈**：Native 方法。

                        ## 线程共享

                        - **堆**：对象实例，分新生代（Eden + 2 个 Survivor）和老年代。
                        - **方法区 / 元空间**：类元信息、常量、静态变量（1.8 后移到本地内存）。
                        - **直接内存**：NIO 的 `DirectByteBuffer`，不受堆大小限制。

                        ## 常见 OOM

                        | 区域 | 异常 |
                        | --- | --- |
                        | 堆 | `OutOfMemoryError: Java heap space` |
                        | 元空间 | `Metaspace` |
                        | 线程栈 | `unable to create new native thread` |

                        > 记忆口诀：**两栈一计数私有，堆与方法区共享**。
                        """,
                Difficulty.MEDIUM, "JVM,内存模型", "美团", "Java 后端", null, false));

        qs.add(q("synchronized 与 ReentrantLock 有什么区别？", cats.get("Java 基础"),
                "什么场景下你会选择 ReentrantLock？",
                """
                        | 维度 | synchronized | ReentrantLock |
                        | --- | --- | --- |
                        | 实现 | JVM 层面 monitor 指令 | JDK 层面 AQS |
                        | 释放 | 自动释放 | 必须 `finally unlock()` |
                        | 公平性 | 非公平 | 可选公平锁 |
                        | 中断 | 不支持 | `lockInterruptibly()` |
                        | 超时 | 不支持 | `tryLock(timeout)` |
                        | 条件队列 | 一个 wait/notify | 多个 Condition |

                        **选择建议**：绝大多数场景直接用 synchronized（1.6 后有锁升级：偏向锁 → 轻量级锁 → 重量级锁，性能已足够）；需要可中断、超时、公平锁或多条件队列时用 ReentrantLock。

                        注意：`synchronized` 锁的是**对象**，静态方法锁的是 `Class` 对象。
                        """,
                Difficulty.MEDIUM, "并发,synchronized,AQS", null, null, null, false));

        qs.add(q("线程池的核心参数与拒绝策略？", cats.get("Java 基础"),
                "核心线程数怎么设置？任务队列满了之后会发生什么？",
                """
                        ## 七个核心参数

                        ```java
                        new ThreadPoolExecutor(
                            corePoolSize,      // 核心线程数
                            maximumPoolSize,   // 最大线程数
                            keepAliveTime,     // 非核心线程空闲存活时间
                            unit,
                            workQueue,         // 阻塞队列
                            threadFactory,     // 线程工厂（命名便于排查）
                            handler            // 拒绝策略
                        );
                        ```

                        ## 执行流程

                        1. 线程数 < core → 新建核心线程
                        2. 否则入队
                        3. 队列满且线程数 < max → 新建非核心线程
                        4. 队列满且线程数 = max → 触发拒绝策略

                        ## 四种拒绝策略

                        - `AbortPolicy`（默认）：抛 `RejectedExecutionException`
                        - `CallerRunsPolicy`：调用者线程执行，起到反压作用
                        - `DiscardPolicy`：静默丢弃
                        - `DiscardOldestPolicy`：丢掉队列头再提交

                        ## 参数怎么定

                        - CPU 密集型：`N + 1`
                        - IO 密集型：`N × (1 + 等待时间 / 计算时间)`，常见 2N 起步
                        - 最终以压测为准，队列建议**有界**，避免 OOM
                        """,
                Difficulty.HARD, "并发,线程池", "字节跳动", "Java 后端",
                "面试时被追问：为什么阿里规约禁止 Executors 创建线程池？答：队列无界 / 最大线程数无界，容易 OOM。", true));

        qs.add(q("Spring Bean 的生命周期是怎样的？", cats.get("Spring"),
                "BeanPostProcessor 在哪些阶段介入？",
                """
                        ## 完整流程

                        1. **实例化**：反射调用构造器
                        2. **属性填充**：依赖注入
                        3. **Aware 回调**：`BeanNameAware` / `BeanFactoryAware` / `ApplicationContextAware`
                        4. **前置处理**：`BeanPostProcessor.postProcessBeforeInitialization`
                        5. **初始化**：`@PostConstruct` → `InitializingBean.afterPropertiesSet` → `init-method`
                        6. **后置处理**：`postProcessAfterInitialization`（**AOP 代理在这里生成**）
                        7. **使用**
                        8. **销毁**：`@PreDestroy` → `DisposableBean.destroy` → `destroy-method`

                        ## 常考延伸

                        - 循环依赖：靠三级缓存 + 提前引用解决（仅支持单例 setter 注入）
                        - `@PostConstruct` 执行时依赖已经注入完毕
                        - BeanFactoryPostProcessor 在 Bean 实例化**之前**修改 BeanDefinition

                        > 记忆：**实例化 → 注入 → Aware → 初始化前后 → 使用 → 销毁**。
                        """,
                Difficulty.MEDIUM, "Spring,IoC,生命周期", null, null, null, false));

        qs.add(q("Spring 如何解决循环依赖？三级缓存分别存什么？", cats.get("Spring"),
                "构造器注入的循环依赖能解决吗？为什么？",
                """
                        ## 三级缓存

                        | 缓存 | 内容 |
                        | --- | --- |
                        | singletonObjects（一级） | 完全初始化好的 Bean |
                        | earlySingletonObjects（二级） | 提前暴露的早期引用 |
                        | singletonFactories（三级） | ObjectFactory，用于生成代理对象 |

                        ## 解决过程（A ↔ B）

                        1. 创建 A，实例化后把 A 的 `ObjectFactory` 放入三级缓存
                        2. 注入 B，发现 B 不存在 → 创建 B
                        3. B 注入 A 时，从三级缓存拿到工厂，生成早期引用并放入二级缓存
                        4. B 初始化完成，A 继续完成初始化

                        ## 为什么需要三级而不是两级？

                        如果 A 需要被 AOP 代理，提前暴露的必须是**代理对象**。三级缓存的工厂可以在需要时才调用 `getEarlyBeanReference` 生成代理，保证注入的是代理对象，同时避免所有 Bean 都提前代理。

                        ## 解决不了的情况

                        - **构造器注入**：对象还没实例化，无法提前暴露
                        - **prototype** 作用域
                        - `@Async` 方法产生的循环依赖（需要 `@Lazy`）

                        Spring Boot 2.6+ 默认禁止循环依赖，需要显式开启：
                        ```properties
                        spring.main.allow-circular-references=true
                        ```
                        """,
                Difficulty.HARD, "Spring,循环依赖,源码", "美团", "Java 后端", null, true));

        qs.add(q("@Transactional 在哪些情况下会失效？", cats.get("Spring"),
                "同类内部方法调用为什么事务不生效？",
                """
                        ## 六种典型失效场景

                        1. **同类内部调用**：`this.method()` 不走代理
                        2. **方法非 public**（Spring 5 之前）
                        3. **异常被 catch 掉**，没有抛到代理层
                        4. **抛出受检异常**：默认只回滚 `RuntimeException` 和 `Error`
                        5. **类没有被 Spring 管理**（自己 new 出来的对象）
                        6. **多线程调用**：事务绑定在 ThreadLocal 上

                        ## 解决办法

                        - 内部调用：注入自身代理 / 拆到另一个 Bean / `AopContext.currentProxy()`
                        - 受检异常：`@Transactional(rollbackFor = Exception.class)`
                        - 确认是 public 方法且被 Spring 管理

                        ## 事务传播行为常考

                        - `REQUIRED`（默认）：有事务就加入，没有就新建
                        - `REQUIRES_NEW`：挂起当前事务，新建独立事务
                        - `NESTED`：嵌套事务，靠 savepoint 实现
                        """,
                Difficulty.MEDIUM, "Spring,事务,AOP", "阿里巴巴", "Java 后端",
                "面试官很爱问：一个 service 方法里调用另一个 @Transactional 方法，怎么保证事务？", false));

        qs.add(q("MySQL 索引为什么使用 B+ 树而不是 B 树或红黑树？", cats.get("数据库"),
                "聚簇索引和二级索引的叶子节点分别存什么？",
                """
                        ## 为什么是 B+ 树

                        - **矮胖**：一个节点存更多索引项，3~4 层就能支撑千万级数据，磁盘 IO 次数少。
                        - **叶子节点有序且用链表相连**：范围查询、排序、分页效率高。
                        - **非叶子节点只存键**：同样大小的页能放更多索引，进一步降低树高。
                        - **查询稳定**：所有查询都要走到叶子节点，IO 次数一致。

                        对比：
                        - 红黑树：二叉结构，树高太高，磁盘 IO 多。
                        - B 树：非叶子节点也存数据，单节点能放的键更少，且范围查询要回溯。

                        ## 聚簇 vs 二级索引

                        - InnoDB 聚簇索引：叶子节点存**整行数据**，按主键组织。
                        - 二级索引：叶子节点存**主键值**，查非索引列需要**回表**。
                        - 覆盖索引：查询列都在索引里，可以避免回表。

                        > 所以建议使用自增主键：顺序写入，减少页分裂。
                        """,
                Difficulty.MEDIUM, "MySQL,索引,B+树", "腾讯", "后端开发", null, false));

        qs.add(q("一条 SQL 很慢，你会怎么排查和优化？", cats.get("数据库"),
                "explain 中哪些字段最关键？深分页怎么优化？",
                """
                        ## 排查步骤

                        1. 开启慢查询日志，定位具体 SQL
                        2. `EXPLAIN` 看执行计划：`type`、`key`、`rows`、`Extra`
                        3. 检查索引是否命中、是否发生隐式类型转换
                        4. 看数据量与区分度，必要时优化 SQL 或加索引

                        ## explain 关键字段

                        - `type`：`const > eq_ref > ref > range > index > ALL`，至少要到 range
                        - `key`：实际使用的索引
                        - `rows`：预估扫描行数
                        - `Extra`：`Using filesort` / `Using temporary` 需要警惕；`Using index` 表示覆盖索引

                        ## 常见优化手段

                        - 联合索引遵循**最左前缀**，把区分度高的列放前面
                        - 避免 `select *`，尽量走覆盖索引
                        - 深分页：`limit 1000000, 20` → 改为基于上次最大 id 的游标查询
                        ```sql
                        select * from t where id > 1000000 order by id limit 20;
                        ```
                        - 避免在索引列上做函数运算
                        - 大事务拆小，减少锁持有时间

                        ## 线上应急

                        先止血（限流、降级、kill 慢查询），再定位根因，最后补索引和监控。
                        """,
                Difficulty.HARD, "MySQL,索引,优化,explain", "阿里巴巴", "后端开发", null, true));

        qs.add(q("MySQL 事务隔离级别与 MVCC 实现原理？", cats.get("数据库"),
                "RR 级别下幻读被完全解决了吗？",
                """
                        ## 四种隔离级别

                        | 级别 | 脏读 | 不可重复读 | 幻读 |
                        | --- | --- | --- | --- |
                        | READ UNCOMMITTED | 有 | 有 | 有 |
                        | READ COMMITTED | 无 | 有 | 有 |
                        | REPEATABLE READ（默认） | 无 | 无 | InnoDB 基本解决 |
                        | SERIALIZABLE | 无 | 无 | 无 |

                        ## MVCC 三要素

                        1. **隐藏字段**：`DB_TRX_ID`（最近修改事务 id）、`DB_ROLL_PTR`（回滚指针）
                        2. **undo log**：版本链
                        3. **ReadView**：活跃事务 id 列表

                        可见性判断：从版本链头开始，找到第一个对当前 ReadView **可见**的版本。
                        - RC：每次 select 都生成新的 ReadView → 不可重复读
                        - RR：第一次 select 生成 ReadView 并复用 → 可重复读

                        ## 幻读

                        - 快照读：MVCC 解决
                        - 当前读：`select ... for update` / `update` / `insert`，靠 **Next-Key Lock**（记录锁 + 间隙锁）解决

                        ## 补充

                        RR 下如果先快照读、再当前读，仍可能看到“幻影”，严格说没有 100% 解决。
                        """,
                Difficulty.HARD, "MySQL,事务,MVCC,锁", "小红书", "后端开发", null, false));

        qs.add(q("缓存穿透、击穿、雪崩的区别与解决方案？", cats.get("Redis"),
                "布隆过滤器有什么缺点？",
                """
                        ## 三兄弟对比

                        | 问题 | 现象 | 解决方案 |
                        | --- | --- | --- |
                        | 穿透 | 查不存在的数据，请求全部打到 DB | 布隆过滤器、缓存空值（短 TTL） |
                        | 击穿 | 热点 key 过期瞬间大量请求打到 DB | 互斥锁重建、逻辑过期、热点永不过期 |
                        | 雪崩 | 大量 key 同时过期 / Redis 宕机 | 过期时间加随机值、多级缓存、限流降级、集群高可用 |

                        ## 布隆过滤器

                        - 位数组 + 多个哈希函数，判断“一定不存在”或“可能存在”
                        - 优点：空间小、查询快
                        - 缺点：**有误判、不支持删除**（可用 Counting Bloom Filter 变体）

                        ## 缓存一致性

                        - 常用：**先更新数据库，再删除缓存**（Cache Aside）
                        - 延迟双删 / 订阅 binlog（Canal）异步删除，降低不一致窗口
                        """,
                Difficulty.MEDIUM, "Redis,缓存,布隆过滤器", null, null, null, false));

        qs.add(q("Redis 分布式锁怎么实现？Redisson 的看门狗了解吗？", cats.get("Redis"),
                "锁过期但业务没执行完怎么办？",
                """
                        ## 最简实现

                        ```java
                        // 加锁：SET key value NX PX 30000
                        Boolean ok = redis.opsForValue().setIfAbsent(key, uuid, 30, TimeUnit.SECONDS);
                        // 解锁：Lua 保证原子性，先比较 value 再删除
                        ```

                        ## 三个必须解决的问题

                        1. **互斥**：`SET NX`
                        2. **防死锁**：过期时间
                        3. **误删别人的锁**：value 存唯一标识，解锁用 Lua 原子比较删除

                        ## Redisson 看门狗

                        - 默认锁 30s，后台线程每 10s（1/3 过期时间）续期一次
                        - 业务宕机则不再续期，锁最终自动释放
                        - 可重入：用 Hash 结构记录 `线程 id -> 重入次数`

                        ## 局限性

                        - 主从架构下，主节点加锁成功后宕机、锁未同步到从节点，可能被重复获取
                        - 强一致场景可以考虑 RedLock（仍存争议）或 ZooKeeper / etcd
                        """,
                Difficulty.HARD, "Redis,分布式锁,Redisson", "拼多多", "后端开发", null, true));

        qs.add(q("TCP 三次握手和四次挥手，为什么不能是两次 / 三次？", cats.get("计算机网络"),
                "TIME_WAIT 状态的作用是什么？",
                """
                        ## 三次握手

                        1. Client → Server：SYN, seq=x
                        2. Server → Client：SYN+ACK, seq=y, ack=x+1
                        3. Client → Server：ACK, ack=y+1

                        **为什么不是两次**：防止历史重复连接请求突然到达服务端，导致服务端建立无效连接、浪费资源；三次才能确认双方的收发能力都正常。

                        ## 四次挥手

                        1. Client → Server：FIN
                        2. Server → Client：ACK（此时服务端可能还有数据要发）
                        3. Server → Client：FIN
                        4. Client → Server：ACK

                        **为什么是四次**：TCP 是全双工，双方需要各自关闭发送方向；服务端收到 FIN 后先回 ACK，等数据发完再发自己的 FIN。

                        ## TIME_WAIT

                        - 主动关闭方进入，持续 **2MSL**（Linux 约 60s）
                        - 作用：保证最后一个 ACK 能到达；让旧连接的重复报文在网络中消失
                        - 过多时：调整内核参数、使用连接复用，而不是简单调小 2MSL
                        """,
                Difficulty.MEDIUM, "TCP,网络,握手", null, null, null, false));

        qs.add(q("HTTP/1.1、HTTP/2、HTTP/3 分别解决了什么问题？", cats.get("计算机网络"),
                "队头阻塞在 HTTP/2 里彻底解决了吗？",
                """
                        ## 演进主线

                        | 版本 | 关键特性 | 遗留问题 |
                        | --- | --- | --- |
                        | HTTP/1.1 | 长连接、管线化 | 应用层队头阻塞 |
                        | HTTP/2 | 二进制分帧、多路复用、HPACK 头压缩、服务端推送 | **TCP 层队头阻塞** |
                        | HTTP/3 | 基于 QUIC（UDP）、0-RTT、连接迁移 | 部署 / UDP 被封 |

                        ## 关键理解

                        - HTTP/2 的多路复用让多个请求共享一个 TCP 连接，但一个 TCP 包丢失会阻塞所有流。
                        - QUIC 在用户态实现可靠传输，每个流独立，丢包只影响单个流。
                        - HTTP/3 还内置 TLS1.3，握手更快。

                        ## 常考延伸

                        - HTTPS = HTTP + TLS，解决窃听、篡改、冒充
                        - 对称加密传输数据，非对称加密协商密钥，证书验证身份
                        """,
                Difficulty.MEDIUM, "HTTP,HTTPS,网络", null, null, null, false));

        qs.add(q("进程和线程有什么区别？协程又是什么？", cats.get("操作系统"),
                "线程切换为什么比进程切换快？",
                """
                        ## 核心区别

                        | 维度 | 进程 | 线程 |
                        | --- | --- | --- |
                        | 资源 | 独立地址空间、文件句柄 | 共享进程资源 |
                        | 切换开销 | 大（切换页表、刷新 TLB） | 小（只切换寄存器和栈） |
                        | 通信 | IPC（管道、消息队列、共享内存） | 直接读写共享变量，需加锁 |
                        | 崩溃影响 | 相互隔离 | 一个线程崩溃可能拖垮整个进程 |

                        ## 协程

                        - 用户态调度，切换不经过内核，开销更小
                        - 适合 IO 密集型高并发（Go goroutine、Kotlin coroutine、Java 虚拟线程）
                        - Java 21 虚拟线程：由 JVM 调度，阻塞时自动让出载体线程

                        ## 一句话

                        进程是**资源分配**的最小单位，线程是**CPU 调度**的最小单位。
                        """,
                Difficulty.EASY, "操作系统,进程,线程,协程", null, null, null, false));

        qs.add(q("Vue 3 的响应式原理是什么？和 Vue 2 有什么区别？", cats.get("前端"),
                "ref 和 reactive 有什么区别？",
                """
                        ## Vue 3：Proxy + effect

                        - 用 `Proxy` 拦截 `get` / `set` / `deleteProperty` 等操作
                        - `get` 时通过 `track` 收集依赖，`set` 时通过 `trigger` 触发更新
                        - 依赖存储在 `WeakMap<target, Map<key, Set<effect>>>` 中

                        ## 相比 Vue 2 的优势

                        - 可以监听**新增 / 删除属性**和数组索引
                        - 惰性递归：只在访问到嵌套对象时才代理，初始化更快
                        - 支持 `Map` / `Set` 等集合类型
                        - 不需要 `Vue.set`

                        ## ref vs reactive

                        - `ref`：包装任意值，通过 `.value` 访问，适合基本类型
                        - `reactive`：只能用于对象，直接访问属性
                        - `toRefs`：解构 reactive 时保持响应性

                        ```js
                        const state = reactive({ count: 0 })
                        effect(() => console.log(state.count)) // 依赖收集
                        state.count++ // 触发更新
                        ```
                        """,
                Difficulty.MEDIUM, "Vue,响应式,前端", null, "前端开发", null, false));

        qs.add(q("从输入 URL 到页面展示，中间发生了什么？", cats.get("前端"),
                "重排和重绘的区别？如何减少重排？",
                """
                        ## 全链路

                        1. **URL 解析**：补全协议、编码
                        2. **DNS 解析**：浏览器缓存 → 系统缓存 → hosts → 递归查询
                        3. **TCP / TLS 连接**：三次握手、证书校验
                        4. **发送 HTTP 请求**：请求行、请求头、请求体
                        5. **服务端处理并响应**
                        6. **浏览器解析渲染**：
                           - HTML → DOM 树
                           - CSS → CSSOM
                           - 合成 Render Tree
                           - Layout（重排）→ Paint（重绘）→ Composite（合成）
                        7. **JS 执行**：可能触发样式或 DOM 变更，引起再次布局

                        ## 性能优化点

                        - DNS 预解析、CDN、HTTP 缓存（强缓存 / 协商缓存）
                        - 减少关键渲染路径阻塞：CSS 放头部、JS 加 defer/async
                        - 图片懒加载、骨架屏、SSR

                        ## 重排 vs 重绘

                        - 重排：几何属性变化（宽高、位置），开销大
                        - 重绘：颜色、背景等不影响布局的属性
                        - 用 `transform` / `opacity` 做动画（走合成层），批量修改 DOM，使用 `DocumentFragment`
                        """,
                Difficulty.MEDIUM, "浏览器,渲染,性能优化", null, "前端开发", null, true));

        qs.add(q("手写一个 LRU 缓存", cats.get("算法与数据结构"),
                "要求 get 和 put 都是 O(1)，并说明为什么用双向链表。",
                """
                        ## 思路

                        `HashMap` 存储 key → 节点，双向链表维护访问顺序：
                        - `get`：命中则把节点移到链表头
                        - `put`：存在则更新并移到头部；不存在则插入头部，超容量时删除尾部节点

                        ## Java 实现

                        ```java
                        class LRUCache {
                            class Node {
                                int key, value;
                                Node prev, next;
                                Node(int k, int v) { key = k; value = v; }
                            }

                            private final int capacity;
                            private final Map<Integer, Node> map = new HashMap<>();
                            private final Node head = new Node(0, 0);
                            private final Node tail = new Node(0, 0);

                            public LRUCache(int capacity) {
                                this.capacity = capacity;
                                head.next = tail;
                                tail.prev = head;
                            }

                            public int get(int key) {
                                Node node = map.get(key);
                                if (node == null) return -1;
                                moveToHead(node);
                                return node.value;
                            }

                            public void put(int key, int value) {
                                Node node = map.get(key);
                                if (node != null) {
                                    node.value = value;
                                    moveToHead(node);
                                    return;
                                }
                                if (map.size() == capacity) {
                                    Node last = tail.prev;
                                    remove(last);
                                    map.remove(last.key);
                                }
                                Node fresh = new Node(key, value);
                                map.put(key, fresh);
                                addToHead(fresh);
                            }

                            private void moveToHead(Node node) {
                                remove(node);
                                addToHead(node);
                            }

                            private void remove(Node node) {
                                node.prev.next = node.next;
                                node.next.prev = node.prev;
                            }

                            private void addToHead(Node node) {
                                node.next = head.next;
                                node.prev = head;
                                head.next.prev = node;
                                head.next = node;
                            }
                        }
                        ```

                        ## 复杂度

                        时间 O(1)，空间 O(capacity)。

                        ## 为什么用双向链表

                        删除任意节点需要拿到前驱；单链表删除还要 O(n) 找前驱，无法满足 O(1)。哨兵头尾节点可以省去大量边界判断。
                        """,
                Difficulty.MEDIUM, "算法,手写,LRU,链表", "快手", "后端开发",
                "面试时先讲思路再写，写完主动举一个容量为 2 的例子走一遍。", true));

        qs.add(q("线上接口突然变慢，你会如何排查？", cats.get("项目与场景"),
                "请按“先止血、再定位、后复盘”的顺序回答。",
                """
                        ## 1. 先确认影响面（止血）

                        - 看监控：QPS、RT、错误率、CPU、内存、GC、连接数
                        - 必要时限流、降级、回滚最近一次发布
                        - 保留现场：dump 线程栈、堆、慢 SQL 日志

                        ## 2. 分层定位

                        | 层 | 排查手段 |
                        | --- | --- |
                        | 入口 | Nginx 日志、网关耗时 |
                        | 应用 | APM 链路追踪、Arthas `trace` / `profiler` |
                        | JVM | `jstat -gc`、GC 日志、`jstack` 看锁和线程池 |
                        | 数据库 | 慢查询日志、`show processlist`、连接池指标 |
                        | 缓存 / MQ | 命中率、RT、积压量 |

                        ## 3. 常见根因

                        - 慢 SQL / 索引失效 / 大事务
                        - 线程池打满或队列堆积
                        - 缓存击穿导致 DB 压力上涨
                        - Full GC / 内存泄漏
                        - 下游依赖超时，重试放大流量

                        ## 4. 复盘

                        - 补监控告警、加超时与熔断、压测回归
                        - 记录到题库：这次问题的现象 → 定位过程 → 根因 → 改进项

                        > 面试官想听的是**结构化思路**和**真实细节**，不要一上来就说“加索引”。
                        """,
                Difficulty.HARD, "系统设计,线上问题,排查", "阿里巴巴", "后端开发", null, true));

        qs.add(q("HR 问：你最大的缺点是什么？", cats.get("HR / 软技能"),
                "考察自我认知与表达，避免说“我太追求完美”。",
                """
                        ## 回答结构

                        1. 一个**真实但不致命**的缺点
                        2. 它具体造成过什么影响（有细节才可信）
                        3. 你采取了什么改进措施
                        4. 现在的改善结果

                        ## 示例

                        > 我早期做需求时容易一头扎进实现，忽略了和产品对齐边界，导致返工。后来我养成了在动手前先写一页“需求理解 + 验收标准”并发给相关方确认的习惯，返工明显减少。

                        ## 不要这样说

                        - “我太追求完美 / 太拼命”——显得不真诚
                        - 与岗位核心能力直接冲突的缺点（如后端说自己粗心、不爱沟通）
                        - 把优点包装成缺点

                        ## 同类问题

                        - 离职原因：聚焦**发展**，不吐槽前公司
                        - 职业规划：3 年内在技术深度上扎根，能独立负责模块
                        - 反问环节：团队的技术栈、业务方向、新人培养机制
                        """,
                Difficulty.EASY, "HR,软技能,表达", null, null,
                "这类题没有标准答案，准备好 2~3 个真实故事即可。", false));

        // 保存题目，并打散创建时间
        ZoneId zone = ZoneId.systemDefault();
        for (int i = 0; i < qs.size(); i++) {
            Question q = qs.get(i);
            q.setDemo(true);
            q.setCreatedAt(now.minus(20L - i, ChronoUnit.DAYS).plus(i, ChronoUnit.HOURS));
            q.setUpdatedAt(q.getCreatedAt());
            q.setNextReviewAt(q.getCreatedAt());
            questionRepository.save(q);
        }

        // 让部分题目呈现不同的复习状态
        Map<String, Object[]> states = new LinkedHashMap<>();
        states.put("HashMap 的底层结构与扩容机制？", new Object[]{Mastery.MASTERED, 4, 12, 3});
        states.put("JVM 运行时内存区域怎么划分？", new Object[]{Mastery.LEARNING, 1, 1, 2});
        states.put("synchronized 与 ReentrantLock 有什么区别？", new Object[]{Mastery.MASTERED, 3, 15, 10});
        states.put("线程池的核心参数与拒绝策略？", new Object[]{Mastery.LEARNING, 2, 2, 2});
        states.put("Spring 如何解决循环依赖？三级缓存分别存什么？", new Object[]{Mastery.LEARNING, 1, 1, 1});
        states.put("@Transactional 在哪些情况下会失效？", new Object[]{Mastery.MASTERED, 2, 10, 5});
        states.put("MySQL 索引为什么使用 B+ 树而不是 B 树或红黑树？", new Object[]{Mastery.LEARNING, 2, 3, 3});
        states.put("MySQL 事务隔离级别与 MVCC 实现原理？", new Object[]{Mastery.MASTERED, 5, 21, 12});
        states.put("缓存穿透、击穿、雪崩的区别与解决方案？", new Object[]{Mastery.MASTERED, 2, 8, 6});
        states.put("Redis 分布式锁怎么实现？Redisson 的看门狗了解吗？", new Object[]{Mastery.LEARNING, 1, 1, 1});
        states.put("HTTP/1.1、HTTP/2、HTTP/3 分别解决了什么问题？", new Object[]{Mastery.MASTERED, 1, 6, 4});
        states.put("进程和线程有什么区别？协程又是什么？", new Object[]{Mastery.MASTERED, 1, 30, 20});
        states.put("从输入 URL 到页面展示，中间发生了什么？", new Object[]{Mastery.LEARNING, 1, 2, 2});
        states.put("手写一个 LRU 缓存", new Object[]{Mastery.LEARNING, 2, 3, 2});
        states.put("HR 问：你最大的缺点是什么？", new Object[]{Mastery.MASTERED, 4, 45, 30});

        for (Question q : qs) {
            Object[] s = states.get(q.getTitle());
            if (s == null) {
                // 新题：7 道左右处于待复习状态
                q.setMastery(Mastery.NEW);
                q.setNextReviewAt(now.minus(1, ChronoUnit.HOURS));
                questionRepository.save(q);
                continue;
            }
            Mastery mastery = (Mastery) s[0];
            int reviewCount = (int) s[1];
            int interval = (int) s[2];
            int daysAgo = (int) s[3];
            q.setMastery(mastery);
            q.setReviewCount(reviewCount);
            q.setIntervalDays(interval);
            Instant last = now.minus(daysAgo, ChronoUnit.DAYS);
            q.setLastReviewedAt(last);
            q.setNextReviewAt(last.plus(interval, ChronoUnit.DAYS));
            questionRepository.save(q);

            // 生成与复习次数匹配的历史记录
            for (int k = 0; k < reviewCount; k++) {
                ReviewLog log = new ReviewLog();
                log.setQuestion(q);
                log.setRating(k == reviewCount - 1 && q.getMastery() == Mastery.LEARNING
                        ? Rating.HARD : Rating.GOOD);
                log.setPrevInterval(Math.max(interval - k, 0));
                log.setIntervalDays(interval);
                log.setCreatedAt(last.minus((long) k * (interval + 1), ChronoUnit.DAYS));
                reviewLogRepository.save(log);
            }
        }

        // 补充最近 7 天连续的复习记录，让热力图和连续打卡更好看
        Random random = new Random(20261028);
        List<Question> all = questionRepository.findAll();
        for (int day = 1; day <= 7; day++) {
            int count = 2 + random.nextInt(4);
            for (int i = 0; i < count; i++) {
                ReviewLog log = new ReviewLog();
                log.setQuestion(all.get(random.nextInt(all.size())));
                log.setRating(Rating.GOOD);
                log.setPrevInterval(1);
                log.setIntervalDays(2);
                Instant at = now.minus(day, ChronoUnit.DAYS)
                        .minus(random.nextInt(8), ChronoUnit.HOURS);
                log.setCreatedAt(at);
                reviewLogRepository.save(log);
            }
        }

        // 面试记录
        InterviewRecord r1 = new InterviewRecord();
        r1.setCompany("字节跳动");
        r1.setPosition("Java 后端开发工程师");
        r1.setRound("一面");
        r1.setInterviewDate(LocalDate.now().minusDays(7));
        r1.setResult(InterviewResult.PASS);
        r1.setChannel("内推");
        r1.setSalaryRange("30-40K · 15 薪");
        r1.setSelfRating(4);
        r1.setDemo(true);
        r1.setSummary("""
                ## 整体感受

                面试官很关注**底层原理**和**线上真实经验**，每个问题都会继续追问“为什么”。

                ## 答得好的

                - 线程池参数与执行流程，结合项目里异步导出的场景讲了队列选择
                - 慢 SQL 排查，讲了 explain + 覆盖索引 + 深分页优化

                ## 需要补的

                - B+ 树对比 B 树时，没有说清“非叶子节点存键”对树高的影响
                - 被问到项目 QPS 的具体数字，回答得比较模糊，需要提前准备数据

                ## 下一步

                1. 把项目里的性能数据整理成一页纸
                2. 复习 B+ 树与索引下推
                3. 准备 3 个线上问题的 STAR 故事
                """);
        r1.getQuestions().add(new InterviewQuestion(r1, byTitle(qs, "线程池的核心参数与拒绝策略？"), "结合异步导出任务讲了有界队列", 0));
        r1.getQuestions().add(new InterviewQuestion(r1, byTitle(qs, "MySQL 索引为什么使用 B+ 树而不是 B 树或红黑树？"), "答得一般，需要补树高和磁盘 IO", 1));
        r1.getQuestions().add(new InterviewQuestion(r1, byTitle(qs, "线上接口突然变慢，你会如何排查？"), "讲了监控、Arthas、慢 SQL 三板斧", 2));
        recordRepository.save(r1);

        InterviewRecord r2 = new InterviewRecord();
        r2.setCompany("小红书");
        r2.setPosition("后端开发工程师");
        r2.setRound("二面");
        r2.setInterviewDate(LocalDate.now().minusDays(3));
        r2.setResult(InterviewResult.PENDING);
        r2.setChannel("BOSS 直聘");
        r2.setSelfRating(3);
        r2.setDemo(true);
        r2.setSummary("""
                ## 面试内容

                偏业务场景设计：设计一个点赞系统，要求高并发、防刷、可扩展。

                ## 复盘

                - 一开始直接讲 Redis，没有先确认**量级和一致性要求**，被面试官打断
                - 应该先问：日活多少、峰值 QPS、是否允许短暂不一致
                - 方案本身没问题：Redis 计数 + 异步落库 + 用户维度去重，但表达顺序需要调整

                ## 待办

                - 整理一套“场景设计题”的回答模板：澄清需求 → 估算量级 → 核心链路 → 存储选型 → 异常与扩展
                - 复习 Redis 分布式锁的续期问题
                """);
        r2.getQuestions().add(new InterviewQuestion(r2, byTitle(qs, "MySQL 事务隔离级别与 MVCC 实现原理？"), "被追问 RR 下幻读是否完全解决", 0));
        r2.getQuestions().add(new InterviewQuestion(r2, byTitle(qs, "Redis 分布式锁怎么实现？Redisson 的看门狗了解吗？"), "讲了 SETNX + Lua + 看门狗续期", 1));
        recordRepository.save(r2);

        touch(marker);
    }

    private Question byTitle(List<Question> questions, String title) {
        return questions.stream().filter(q -> q.getTitle().equals(title)).findFirst()
                .orElseThrow(() -> new IllegalStateException("seed question missing: " + title));
    }

    private Category category(String name, String color, String desc, int sort) {
        Category c = new Category(name, color, desc, sort);
        c.setDemo(true);
        return categoryRepository.save(c);
    }

    private Question q(String title, Category category, String content, String answer,
                       Difficulty difficulty, String tags, String company, String position,
                       String note, boolean starred) {
        Question q = new Question();
        q.setTitle(title);
        q.setCategory(category);
        q.setContent(content);
        q.setAnswer(answer);
        q.setDifficulty(difficulty);
        if (tags != null && !tags.isBlank()) {
            q.setTags(new LinkedHashSet<>(Arrays.asList(tags.split(","))));
        }
        q.setSourceCompany(company);
        q.setSourcePosition(position);
        q.setNote(note);
        q.setStarred(starred);
        return q;
    }

    private void touch(File marker) {
        try {
            File parent = marker.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (!marker.exists()) marker.createNewFile();
        } catch (Exception ignored) {
        }
    }
}
