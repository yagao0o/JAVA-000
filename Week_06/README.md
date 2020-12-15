# Week06 作业
## Day11 作业
**1、(选做)尝试使用Lambda/Stream/Guava优化之前作业的代码。**

**2、(选做)尝试使用Lambda/Stream/Guava优化工作中编码的代码。**

> 1+2，对于Stream和lombok之前用的都比较多，stream里面也经常用lambda表达式；之前对guava了解不多，趁这个机会了解熟悉一下Guava


**3、(选做)根据课上提供的材料，系统性学习一遍设计模式，并在工作学习中思考如何用设计模式解决问题。**
> 设计模式是用来解决特定的设计问题的，第一步是需要对设计模式整体有概念，了解熟悉各个模式的优劣、使用场景，做到能对症下药。同时对业务来说，也是在不断变更的，其适用的设计模式也会随之变更（当然也有一部分设计模式是用于处理变更的）。软件和系统的设计者使用设计模式需要对设计模式和具体业务都较为了解和熟悉，否则很容易模式变为反模式。

**4、(选做)根据课上提供的材料，深入了解Google和Alibaba编码规范，并根据这些规范，检查自己写代码是否符合规范，有什么可以改进的。**  
> 阿里编程规约发布后就一直在关注，其中代码风格这部分通过IDEA插件进行日常检查，这部分我的思考是这样的：首先需要团队整体认同规约的必要性，日常开发中未必时刻按照规约开发，在代码提交前，通过工具扫描提示后按要求修改，多次修改后就会自然在编码环节依照规约开发，代码风格比较适用此部分。对于偏设计的规范，这部分需要团队的技术leader有这方面思考和认识，在开发过程中的讨论中，有意的去引导团队去按照规约的设计模式来进行设计，这个讨论即可以是在设计环节，也可以是在review环节。

## Day12 作业

**1、(选做):基于课程中的设计原则和最佳实践，分析是否可以将自己负责的业务系统进行数据库设计或是数据库服务器方面的优化。**  
>   之前在运营商行业，业务系统里面存在大量的存储过程，绝大部分是历史遗留问题，很多代码存在了N年（大于我在这个团队的时间）。同时修改的难度不仅在于数据库设计，而且在于多个业务系统的设计，多个业务系统相互关联，甚至直接数据源连接其他系统数据库来操作。从我的角度来说，第一步需要将各系统边界定义清楚，系统交互严禁直连数据库，系统进行微服务改造。然后才轮到数据库层面的优化，然后将存储过程在系统改造时逐步迁移到业务应用中操作。  
另外，数据中存在大量的冗余字段，并且经常会有冗余字段不一致的问题，团队的处理方法是由负责数据的团队，定时同步。这个地方是需要改进的，目前没有想到特别好的方法。

**2、(必做):基于电商交易场景(用户、商品、订单)，设计一套简单的表结构，提交DDL的SQL文件到Github(后面2周的作业依然要是用到这个表结构)。**  
> 考虑到简单的表结构，用户暂时用一张大表来表示，实际可以拆分为三个表：用户基本信息、密码、收货地址。货物表也未考虑快照等问题，可以按表格式复制一张表，添加快照时间来保存快照信息;货物表中的价格单位为分。订单中包含多个货物，拆分为两张表，订单表及订单详情，订单仅添加一个基本状态，暂不考虑支付等情形。
- [MySQL版本](./mysql_ddl.sql)
```SQL
CREATE TABLE `t_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account` varchar(32) NOT NULL,
  `nick_name` varchar(32) NOT NULL,
  `password` varchar(32) NOT NULL,
  `address` varchar(128) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `t_user_UN` (`account`)
);

CREATE TABLE `t_good` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `price` int(11) DEFAULT NULL,
  `is_delete` smallint(5) unsigned NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE `t_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `state` smallint(5) unsigned NOT NULL DEFAULT '0',
  `is_delete` smallint(5) unsigned NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE `t_order_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` int(11) NOT NULL,
  `good_id` int(11) NOT NULL,
  `good_amount` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);
```
[以下题目均使用homework项目完成](./homework)

**3、(选做):尽可能多的从“常见关系数据库”中列的清单，安装运行，并使用上一题的SQL测试简单的增删改查。**
- 环境1: MySQL 8.0.22
[全部代码](./homework/src/main/java/com/qingyi/week6/homework/controller/HomeworkController.java)
```Java
@RequestMapping(value = "/testUserOperation", method = RequestMethod.POST)
public void addUser() {
    User user;
    // 添加
    for (int i = 0; i < 5; i++) {
        user = getTestUser();
        userService.save(user);
    }

    try {
        Thread.sleep(5000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    // 查询
    List<User> fetchUsers = userService.list();
    log.info("total user =" + fetchUsers.size());
    // 修改操作,取最后一个改
    user = fetchUsers.get(fetchUsers.size() - 1);
    userService.update(new UpdateWrapper<User>().eq("id", user.getId()).set("address", "addr:" + System.currentTimeMillis()));

    fetchUsers = userService.list();
    log.info("total user = " + fetchUsers.size());

    //删除操作
    if (fetchUsers.size() > 0) {
        userService.removeById(fetchUsers.get(0).getId());
    }

    fetchUsers = userService.list();
    log.info("Now, total user = " + fetchUsers.size());
}
```
- 环境2: Oracle 12c，建DDL与MySQL略有不同，Mybatis对于主键id处理需要改一下Sequence，此处略

**4、(选做):基于上一题，尝试对各个数据库测试100万订单数据的增删改查性能。**  
// TODO: 完成一个测试模块，分别模拟100万个增删改查的测试（分为两部分，1仅测试主表 2订单主表+详情表）
// 分别对Oracle、MySQL（InnoDB、myisam、archive、memory），进行测试【包含了下一题】

- 100万订单新增，（每次新增1000条，1000次）：
  - MySQL InnoDB **821秒**
```TXT
2020-11-26 10:52:39.698  INFO 62233 --- [nio-8080-exec-1] c.q.w.h.controller.HomeworkController    : start: 250468516534121
2020-11-26 11:06:20.654  INFO 62233 --- [nio-8080-exec-1] c.q.w.h.controller.HomeworkController    : end: 251289503764326
2020-11-26 11:06:20.654  INFO 62233 --- [nio-8080-exec-1] c.q.w.h.controller.HomeworkController    : 总消耗时间：820987230205
```

- 1万订单查询（每次查100条，查100次）：
  - MySQL InnoDB **1429秒** 额……
  ```
  2020-11-26 16:58:31.204  INFO 67304 --- [nio-8080-exec-1] c.q.w.h.controller.HomeworkController    : start: 272420883105965
  2020-11-26 17:22:20.106  INFO 67304 --- [nio-8080-exec-1] c.q.w.h.controller.HomeworkController    : end: 273849840179380
  2020-11-26 17:22:20.107  INFO 67304 --- [nio-8080-exec-1] c.q.w.h.controller.HomeworkController    : 总消耗时间：1428957073415
  ```


**5、(选做):尝试对MySQL不同引擎下测试100万订单数据的增删改查性能。**  
// TODO: InnoDB、myisam、archive、memory（memory不知道是否能支持100万订单数据）

**6、(选做):模拟1000万订单数据，测试不同方式下导入导出(数据备份还原) MySQL的速度，包括jdbc程序处理和命令行处理。思考和实践，如何提升处理效率。**  
// TODO: jdbc处理思路，使用线程池多线程分片处理。 命令行处理的话，一个是直接用dump工具，另一个考虑是否可以使用binlog直接导入

**7、(选做):对MySQL配置不同的数据库连接池(DBCP、C3P0、Druid、Hikari)， 测试增删改查100万次，对比性能，生成报告。**  
// TODO: 基于SpringBoot配置不同的依赖及连接池进行测试