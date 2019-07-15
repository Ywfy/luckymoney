# luckymoney
初学SpringBoot项目,慕课网《2小时快速上手Spring Boot》学习笔记

## SpringBoot初识
现在SpringBoot很火，但是不要一看SpringBoot就觉得好难，就抗拒去学习、使用它。其实它非常容易，它简化了以往使用Spring工作中太多的配置，使用起来非常爽。

## 构造SpringBoot项目
两种方法：
* 使用IDEA，会有SpringBoot项目创建引导，非常方便
* Eclipse，可以去SpringBoot的[官网](https://start.spring.io/)下载包导入，也可以干脆使用STS(此处推荐若是习惯Eclipse的使用STS会更加方便)

## 项目配置
springBoot项目配置，靠修改application.properties,或者新建application.yml也行
以下是yml格式
```
server:
  port: 8081
  servlet:
    context-path: /luckymoney

```
此处定义了访问端口号，和项目访问路径
http://localhost:8081//luckymoney

将参数定义在配置文件中，然后在代码中引用参数，是非常常见的
在application.yml中
```
minMoney: 1
```
在代码中
```
/**
 * @Controller + @ResponseBody = @RestController
 */
@RestController
public class HelloController {
  
  @Value("${minMoney}")
  private BigDecimal minMoney;
  
  @GetMapping("/hello")
  public String say(){
    return "minMoney: " + minMoney;
  }
```

不过这种方式当变量有很多时，就要在Controller中写非常多的@Value，这是不可取的，此时有另外一种方法
在application.yml中
```
limit:
  minMoney: 0.01
  maxMoney: 9999
  description: 最少要发${limit.minMoney}元, 最多可以发${limit.maxMoney}元
```
创建配置类
```
@Component
@ConfigurationProperties(prefix = "limit")
public class LimitConfig {

    private BigDecimal minMoney;

    private BigDecimal maxMoney;

    private String description;

    public BigDecimal getMinMoney() {
        return minMoney;
    }

    public void setMinMoney(BigDecimal minMoney) {
        this.minMoney = minMoney;
    }

    public BigDecimal getMaxMoney() {
        return maxMoney;
    }

    public void setMaxMoney(BigDecimal maxMoney) {
        this.maxMoney = maxMoney;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
```
重点就在于@ConfigurationProperties(prefix = "limit")，前缀要对应上。类中的属性也要一一对应

使用时
```
@RestController
public class HelloController {

    @Autowired
    private LimitConfig limitConfig;
    
    @PostMapping("/say")
    public String say(){
      return "说明：" + limitConfig.getDescription();
    }
```

一般在开发时，会至少配置两个环境，如开发环境、生产环境（毕竟一般来说是会不一样的）

在SpringBoot中可以很方便的配置

![图片无法加载](https://github.com/Ywfy/luckymoney/blob/master/appli.png)

重点只需要在application.yml中配置
```
spring:
  profiles:
    active: prod
```
以上就是使用生产环境,即使用application-prod.yml配置文件
```
spring:
  profiles:
    active: dev
```
这就是使用开发环境,即使用application-dev.yml配置文件

也可以不用修改配置文件，直接在java运行时键入相应的配置选择项就行
```
java -jar -Dspring.profiles.active=prod target/luckymoney-0.0.1-xxxxx.jar
```

## Controller参数获取方式
* @PathVariable --> 从URL获取参数
* @RequestParam --> 从body中获取参数
```
    @PutMapping("/luckymoneys/{id}")
    public Luckymoney updateById(@PathVariable("id")Integer id,
                                 @RequestParam("receiver")String receiver){
        Optional<Luckymoney> optional = repository.findById(id);
        if (optional.isPresent()){
            Luckymoney luckymoney = optional.get();
            luckymoney.setId(id);
            luckymoney.setReceiver(receiver);
            return repository.save(luckymoney);
        }
        return null;
    }
```

## 数据库操作
SpringBoot使用Jpa组件，操作起来真的非常简单，非常爽

1、配置数据源
```
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/luckymoney?characterEncoding=utf-8
    username: xxxx
    password: xxxxxxxxxxx

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```
此处com.mysql.cj.jdbc.Driver是MySQL的新版驱动，推荐使用
下面的ddl-auto是设置项目启动时，对数据库的操作
* 若为create，则每次启动都会重置数据库，也就是数据库表会被删除，然后全部重建
* 若为update，则无改变
show-sql是设置控制台是否显示sql执行过程的

2、写Entity类
```
@Entity
public class Luckymoney {

    @Id
    @GeneratedValue
    private Integer id;

    private BigDecimal money;

    private String producer;

    private String receiver;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
```
此处重点强调 @Id和@GeneratedValue,@Id表示我这个是主键，@GeneratedValue表示自增

3、写DAO
```
public interface LuckymoneyRepository extends JpaRepository<Luckymoney, Integer> {
}
```
你没有看错，DAO就是这么简单，只需要继承JpaRepository<Entity类，主键数据类型>就OK了

4、编写Contrller
```
@RestController
public class LuckymoneyController {

    @Autowired
    private LuckymoneyRepository repository;
    
     /**
     * 获取红包列表
     */
    @GetMapping("/luckymoneys")
    public List<Luckymoney> list(){
        return repository.findAll();
    }
}
```
不用疑惑findAll方法哪里来，这全是Jpa帮我们写好的。

此处提一提repository.save()方法
不管是新增还是更新，都是调用save方法，它们的区别就在于是否有传入主键

新增代码
```
@PostMapping("/luckymoneys")
    public Luckymoney create(@RequestParam("producer")String Producer,
                             @RequestParam("money")BigDecimal money) {
        Luckymoney luckymoney = new Luckymoney();
        luckymoney.setProducer(Producer);
        luckymoney.setMoney(money);
        return repository.save(luckymoney);
    }
```

更新代码
```
@PutMapping("/luckymoneys/{id}")
    public Luckymoney updateById(@PathVariable("id")Integer id,
                                 @RequestParam("receiver")String receiver){
        Optional<Luckymoney> optional = repository.findById(id);
        if (optional.isPresent()){
            Luckymoney luckymoney = optional.get();
            luckymoney.setId(id);
            luckymoney.setReceiver(receiver);
            return repository.save(luckymoney);
        }
        return null;
    }
```

## 事务
就一个标签，@Transactional
不过记住，这个事务指的是数据库的事务，所以必须底层数据库支持事务才行

对于MySQL，必须使用InnoDB引擎，MyISAM是不支持事务的

