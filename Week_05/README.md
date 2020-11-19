# Week05 作业
## Day9 作业
**1. (选做)使Java里的动态代理，实现一个简单的AOP。**

**2. (必做)写代码实现Spring Bean的装配，方式越多越好(XML、Annotation都可以),提交到Github。**
- xml 普通装配
```XML
<bean id="m3" class="com.qingyi.demo.Model">
    <property name="brand" value="Tesla" />
    <property name="powerType" value="1" />
    <property name="modelName" value="model 3"/>
</bean>

<bean id="myTeslan" class="com.qingyi.demo.Car">
    <property name="number" value="鲁AD1***2" />
    <property name="model" ref="m3"/>
</bean>
```
- xml Autowire byType
```XML
<bean id="m3" class="com.qingyi.demo.Model">
    <property name="brand" value="Tesla" />
    <property name="powerType" value="1" />
    <property name="modelName" value="model 3"/>
</bean>

<bean id="myTeslan" class="com.qingyi.demo.Car"  autowire="byType">
    <property name="number" value="鲁AD1***2" />
</bean>
```

- xml Autowire byName
```XML
<bean id="model" class="com.qingyi.demo.Model">
    <property name="brand" value="Tesla" />
    <property name="powerType" value="1" />
    <property name="modelName" value="model 3"/>
</bean>

<bean id="myTeslan" class="com.qingyi.demo.Car"  autowire="byType">
    <property name="number" value="鲁AD1***2" />
</bean>
```

- 注解 Autowire
```XML
<context:annotation-config/>
<bean id="model" class="com.qingyi.demo.Model">
    <property name="brand" value="Tesla" />
    <property name="powerType" value="1" />
    <property name="modelName" value="model 3"/>
</bean>

<bean id="myTeslan" class="com.qingyi.demo.Car">
    <property name="number" value="鲁AD1***2" />
</bean>
```
```Java
@Data
public class Car {
    String number;
    @Autowired
    Model model;
}
```

- 注解 Resource
```XML
<context:annotation-config/>
<bean id="m3" class="com.qingyi.demo.Model">
    <property name="brand" value="Tesla" />
    <property name="powerType" value="1" />
    <property name="modelName" value="model 3"/>
</bean>

<bean id="myTeslan" class="com.qingyi.demo.Car">
    <property name="number" value="鲁AD1***2" />
</bean>
```
```Java
@Data
public class Car {
    String number;
    @Resource(name = "m3")
    Model model;
}
```
**3. (选做)实现一个Spring XML自定义配置，配置一组Bean，例如Student/Klass/School。**
```Java
// 模型
@Data
public class Model {
    String brand;
    String modelName;
    int powerType;
}

@Data
public class Car {
    String number;
    Model model;
}

@Data
public class Garage {
    List<Car> myCars;
    String location;
}
```
```XML
<context:annotation-config/>
<bean id="model" class="com.qingyi.demo.Model">
    <property name="brand" value="长城" />
    <property name="powerType" value="0" />
    <property name="modelName" value="C30"/>
</bean>
<bean id="m3" class="com.qingyi.demo.Model">
    <property name="brand" value="Tesla" />
    <property name="powerType" value="1" />
    <property name="modelName" value="model 3"/>
</bean>
<bean id="myTeslan" class="com.qingyi.demo.Car">
    <property name="number" value="鲁AD1***2" />
    <property name="model" ref="m3"/>
</bean>
<bean id="oldCar" class="com.qingyi.demo.Car" autowire="byName">
    <property name="number" value="鲁A6***U" />
</bean>
<bean id="myGarage" class="com.qingyi.demo.Garage">
    <property name="location" value="山东济南" />
    <property name="myCars">
        <list>
            <ref bean="oldCar"/>
            <ref bean="myTeslan"/>
        </list>
    </property>
</bean>

```

## Day10 作业
**1. (选做)总结一下，单例的各种写法，比较它们的优劣。**

**2. (选做)maven/spring 的 profile 机制，都有什么用法?**

**3. (必做)给前面课程提供的 Student/Klass/School 实现自动配置和 Starter。**

[项目链接](./my-homework-starter/)，主要代码参考：  

```Java
package com.qingyi;

import com.qingyi.demo.Car;
import com.qingyi.demo.Garage;
import com.qingyi.demo.Model;
import com.qingyi.prop.SpringBootPropertiesConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

/**
 * Spring boot starter configuration.
 * @author Luyz
 */
@Configuration
@EnableConfigurationProperties(SpringBootPropertiesConfiguration.class)
@ConditionalOnProperty(prefix = "com.qingyi", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class SpringBootConfiguration {

    private final SpringBootPropertiesConfiguration props;
    @Bean("model3")
    public Model getModel3() {
        Model model = new Model();
        model.setBrand("Tesla");
        model.setModelName("model 3");
        model.setPowerType(1);
        return model;
    }

    @Bean("c30")
    public Model getC30() {
        Model model = new Model();
        model.setBrand("长城");
        model.setModelName("C30");
        model.setPowerType(0);
        return model;
    }

    @Bean("littleBlue")
    @Autowired()
    public Car getLittleBlue(Model model3){
        Car car = new Car();
        car.setModel(model3);
        car.setNumber("鲁AD1***2");
        return car;
    }

    @Bean("oldCar")
    @Autowired()
    public Car getOldCar(Model c30){
        Car car = new Car();
        car.setModel(c30);
        car.setNumber("鲁A6***U");
        return car;
    }


    @Bean("garage")
    @Autowired
    public Garage getMyGarage(Car littleBlue, Car oldCar) {
        Garage garage = new Garage();
        System.out.println(props.getProps().keySet());
        garage.setMyCars(new ArrayList<>());
        garage.getMyCars().add(littleBlue);
        garage.getMyCars().add(oldCar);
        return garage;
    }
}
```
引用方式
```XML
<dependency>
    <groupId>com.qingyi</groupId>
    <artifactId>my-homework-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```
**4. (选做)总结 Hibernate 与 MyBatis 的各方面异同点。**

**5. (选做)学习 MyBatis-generator 的用法和原理，学会自定义 TypeHandler 处理复杂类型。**

**6. (必做)研究一下 JDBC 接口和数据库连接池，掌握它们的设计和用法:**
1. 使用 JDBC 原生接口，实现数据库的增删改查操作。 
2. 使用事务，PrepareStatement 方式，批处理方式，改进上述操作。 
3. 配置 Hikari 连接池，改进上述操作。提交代码到 Github。

Error creating bean with name 'getMyGarage' defined in class path resource [com/qingyi/SpringBootConfiguration.class]: Unsatisfied dependency expressed through method 'getMyGarage' parameter 0: Ambiguous argument values for parameter of type [com.qingyi.demo.Car] - did you specify the correct bean references as arguments?