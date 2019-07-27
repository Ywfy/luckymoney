# 《Spring Boot进阶之Web进阶》学习笔记

仍然是Luckymoney项目，只是进行了一些扩展学习

## 表单验证

实现表单验证非常简单，只需要两步就行

（注：本方法针对前台传来的内容可以封装为一个Entity对象）

一、对Entity对象要校验的属性添加注解

```java
@Entity
public class Luckymoney {

    ...

    @DecimalMin(value = "0.01", message = "红包最低不能低于0.01元")
    private BigDecimal money;
    
    ...

```

代码示例这里用了@DecimalMin，显然也就是限定Decimal类型的最小值

除此之外，还有很多校验注解，比如很常用的@NotNull

二、在要触发校验的Controller方法添加@Valid注解

```
/**
* 创建红包（发红包）
*/
@PostMapping("/luckymoneys")
public Luckymoney create(@Valid Luckymoney luckymoney, BindingResult 		bindingResult) {

        Luckymoney luckymoney = new Luckymoney();

        if(bindingResult.hasErrors()){
            logger.info(bindingResult.getFieldError().getDefaultMessage());
            return null;
        }

        return repository.save(luckymoney);
}
```

使用Luckymoney对象来自动封装前台传来的参数，在Luckymoney传参前添加@Valid标签，就可以实现我们第一步要求的校验

BindingResult对象会封装校验的结果信息



## 使用AOP处理请求

AOP，面向切面编程

Springboot使用AOP也非常简单

一、添加maven依赖

```java
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

二、创建切面类

```java
@Aspect
@Component
public class HttpAspect {
    @Before("execution(public * com.imooc.luckymoney.controller.LuckymoneyController.*(..))")
    public void log(){ 
    	System.out.println("111111");
    }
}
```

类上面注意要添加@Aspect和@Component

在切面方法上添加下述注解之一

* @Before ：执行execution内述方法之前执行log方法
* @After ：执行execution内述方法之后执行log方法
* @AfterReturning：在最后要返回内容给前台之前执行log方法

execution内匹配方法的写法其实跟一般方法的方法头写法差不多，只是

* 可以用 * 号表示任意内容，比如示例中返回值位置是"*",这意味着可以是任何返回值
* 方法名要写全类名（当然"*"适用）
* 方法传参可以用 .. 来代替，这代表任意参数适用

这样子配完了后，项目运行时，当运行到execution内匹配的方法，就会触发log方法在相应时机运行



当针对同一个切面有很多个切面方法时，

```java
@Before("execution(public * com.imooc.luckymoney.controller.LuckymoneyController.*(..))")
```

比如针对上述execution内的方法有很多个切面方法时，那我们就要写很多次这个execution(xxx)，

这不仅麻烦，而且不利于后期维护

这里可以利用@Pointcut来解决，如下

```java
@Aspect
@Component
public class HttpAspect {

    private static final Logger logger = LoggerFactory.getLogger(HttpAspect.class);

    @Pointcut("execution(public * com.imooc.luckymoney.controller.LuckymoneyController.*(..))")
    public void log(){ }

    @Before("log()")
    public void doBefore(){
        logger.info("111111");
    }

    @After("log()")
    public void doAfter(){
        logger.info("2222222222");
    }

    @AfterReturning(returning = "object", pointcut = "log()")
    public void doAfterReturning(Object object){
        logger.info("response={}", object);
    }
}
```

使用@Pointcut专门声明一个方法作为切面，后续针对这个切面的就不用写execution(xxx)那一大串了，直接写方法名就行了



若我们希望在切面方法中获得请求的相关信息，比如请求的URL、IP、类方法等。可以通过使用JoinPoint。

这里请注意，是

```java
import org.aspectj.lang.JoinPoint; //大写的P

//不是
import org.aopalliance.intercept.Joinpoint;
```

导入JoinPoint包后，就可以这样写

```java
@Before("log()")
public void doBefore(JoinPoint joinPoint){

	ServletRequestAttributes attributes = 		     	 	 	 	 	 	 	 	 	 	 	(ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
	HttpServletRequest request = attributes.getRequest();

    //url
    logger.info("url={}", request.getRequestURL());

    //method
    logger.info("method={}", request.getMethod());

    //ip
    logger.info("ip={}", request.getRemoteAddr());

    //类方法
    logger.info("class_method={}", joinPoint.getSignature().getDeclaringTypeName() + "." 		+ joinPoint.getSignature().getName());

    //参数
    logger.info("args={}", joinPoint.getArgs());

}
```



## 统一异常处理

在软件开发过程中，异常是令人非常头疼的。而实际上有一套很好的设计方法来规范异常的处理。

现在软件开发往往是前后分离的形式，前端和后端的接口应该是稳定的，不管后端是否有抛异常，提供到前端的数据格式（一般即JSON内容格式）一定是不变的。为了实现这个目的，我们是这么做的：

一、定义返回前台类

后端返回前台的数据一般要有三个内容：

* 状态码
* 对应提示信息
* 实际数据内容

```java
public class Result<T> {

    //状态码
    private Integer code;

    //提示信息
    private String msg;

    //具体的内容
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
```

这里使用泛型是没有问题的，重点是确保http请求返回的最外层对象唯一



二、定义状态码Enum类

状态码和对应提示信息应该是规范的，而不能随意编写。我们通过Enum类来实现

```java
public enum ResultEnum {
    UNKNOWN_ERROR(-1, "未知错误"),
    SUCCESS(0, "成功"),
    TIGHT(100, "You are so tight"),
    THANKS(101, "Thank you!"),
    GOOD(102, "Good job!"),
    GOD(103, "Have you made a fortune?")
    ;

    private Integer code;

    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }


    @Override
    public String toString() {
        return "ResultEnum{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}

```



三、定义自己的异常

我们需要异常同样具备有一致的信息，即状态码、提示信息和实际内容（抛异常的话，实际内容一般为Null）

```java
public class LuckymoneyException extends RuntimeException {
    //提一个小点，抛出RuntimeException事务才能回滚，Exception是不会回滚的

    private Integer code;

    public LuckymoneyException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.code = resultEnum.getCode();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
```



四、将异常转化为返回前台类

虽然我们定义了自己的异常，但他的数据格式显然不会与我们之前定义的返回前台类一致，所以我们需要将异常的信息提取，然后最终返回前台类

实际上@ExceptionHandler就可以解决我们的这个问题

```java
@ControllerAdvice
public class ExceptionHandle {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandle.class);

    @ExceptionHandler(value = LuckymoneyException.class)
    @ResponseBody
    public Result handle(LuckymoneyException e){
        logger.error(e.getMessage());
        return ResultUtil.error(e.getCode(), e.getMessage());
    }
}
```

这样写之后，当LuckymoneyException被抛出Controller层之后，就会被捕获触发handle方法，将异常的信息提取到Result（即返回前台类）中

示例代码中，提取的过程使用了ResultUtil类，这是一个自定义的工具类，方便返回前台类的构造。

```java
public class ResultUtil {

    public static Result success(Object object){
        Result result = new Result();
        result.setCode(0);
        result.setMsg("success");
        result.setData(object);
        return result;
    }

    public static Result success(){
        return success(null);
    }

    public static Result error(Integer code, String msg){
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
```



## 单元测试

一个优秀的程序员要对自己的代码负责，每开发完一个模块都要进行单元测试，确保自己的代码运行准确无误。

单元测试实际上就是针对三个层进行测试

* API测试（实际上就是Controller层测试，不过注意要结合浏览器URL访问等信息，而不是说方法执行无误就OK了）
* Service层测试
* Dao层测试（一般Service层测试就足够了，严格就Dao层也一个个进行）

使用IDEA开发，进行单元测试实际上是非常方便的

比如，访问LuckymoneyService.java文件

右键findOne方法==》Go To ==》Test ==》Create New Test... ==》 Member栏勾选你想要测试的方法 ==》点击OK

此时会自动创建并来到对应测试类方法编写页面

```java
public class LuckymoneyServiceTest {

    @Test
    public void findOne() {
        
    }
}
```

标测试注解，和自动导入Service

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class LuckymoneyServiceTest {
    
    @Autowired
    private LuckymoneyService luckymoneyService;
    
    //编写测试代码，比较实际运行结果与预期是否相同，一般会使用
    //Assert.assertEquals(a,b)来比较
    @Test
    public void findOne() {

    }
}
```



API的测试实际上就是Service的测试方法需要加上实现模拟访问URL的内容

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LuckymoneyControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void list() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/luckymoneys"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("666"));
    }
}
```

通过@AutoConfigureMockMvc注解，和自动导入MockMvc对象，就可以利用该对象实现模拟访问

