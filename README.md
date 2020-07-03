# 9420 开发工具包
---
sanri-tools-maven 是一个开源的用于企业开发的工具包，重点想解决项目开发中一些比较麻烦的问题

根据表和模板生成相应代码；一些身份证，企业代码，车架号的验证与生成； kafka ,zookeeper 的数据监控等

博客地址: https://blog.csdn.net/sanri1993/article/details/98664034

---


## 工具理念

1. 轻量级,只依赖于文件系统
2. 小工具,大作用,减少模板代码的手工编写
3. 自定义框架,加快项目启动速度 ,目前项目启动时间为 600 ms 左右

## 已经有的工具

已经存在的工具可以在 /src/main/resources/com/sanri/config/tools.properties 中查看

1. [方法或变量取名](helps/取名工具.md)
2. [数据提取](helps/数据提取.md)
3. [生份证号码生成与验证](helps/身份证验证与生成工具.md)
4. [kafka  监控和 offset 设置,支持新旧版本 kafka](helps/kafka消费监控.md)
5. [zookeeper 数据监控](helps/zookeeper监控.md)
6. [模板代码生成,根据列字段 ](helps/模板代码生成.md)
7. [列字段比较 ](helps/字段比较.md)
8. [数据库表字段,注释,名称查询,及后续模板代码操作](helps/数据表处理工具.md)
9. [webservice 调试工具,只要输入 wsdl 地址,自动解析并构建 xml 消息](helps/webservice调用.md) 
10. [下划线转驼峰,驼峰转下划线工具](helps/数据提取.md)
11. 图片转 base64 ,base64 转图片
12. SQL 客户端,已经支持 mysql,postgresql,oracle ; 可自定义实现其它数据库 
   * 表结构查询
   * pojo,xml  生成
   * 项目模板代码生成
   * 数据导出

13. [数据表处理工具（SQL 客户端升级版 ）](helps/数据表处理工具.md)
  * 可以根据变量自定义模板
  * 由多个模板组成一种方案
  * 单表使用模板生成，然后生成多种模板的代码后统一下载
  * 单表使用方案生成
  * 多表使用方案生成

14. 增加聊天功能(可以学下 websocket 怎么用)
    * 保存历史消息，针对当前 session 标签页而言
    * 目前只能群聊
    * 只支持单独 tomcat7 以上部署，用 maven  tomcat 插件是不行的

15. [增加 redis 数据监控功能](helps/redis数据监控工具.md)
   * 可以搜索 key 信息
   * 反序列化查看 key 数据,使用项目的实体进行反序列化,可以看查看到所有的实体列

16. 增加响应数据模拟工具 

## 扩展自己的工具

* 除前端交互 servlet 必须写在 com.sanri.app.servlet 包中以外,其它随便自己定制
* servlet 中的代码由于框架 javassist 的原因 ,不支持 java8 的 lambada 表达式
* 数据表元数据信息保存在 InitJdbcConnections.CONNECTIONS 信息中
* 配置信息统一使用 ConfigCenter 进行读取，保存的是配置树结构 
* 文件系统配置信息统一管理接口 FileManagerServlet 
* 目录结构说明 com.sanri
   + algorithm 写的算法存放目录
   + app 所有工具信息
      - servlet 存放所有与前端交互的 servlet 
   + deginmodel 设计模式学习
   + frame 本项目自定义框架
   + initexec 初始化执行目录；放入本目录的文件，在启动的时候会查找 @PostConstruct 注解的方法执行

## 如何搭建环境 

1. 通过git下载源码
2. 安装部分第三方包到 maven 仓库，源 jar 包已经放到 /src/main/resources 下面
   
   2.1 如何安装请参考 https://www.cnblogs.com/yadongliang/p/9829760.html
3. 修改部分配置
   - function.open.properties 用于配置临时文件路径和产生的配置路径 
   - tools.properties  配置当前环境可以展示哪些工具，里面是所有工具的配置信息
   - jdbcdefault.properties 项目初始化时加载的默认 jdbc 连接 ,可将你的数据库配置到这里
   - mapper_jdbc_java.properties  这个是生成 java 实体类时，数据库类型映射到 java 类型
   - db_mapper_mybatis_type.properties  这个是数据库类型映射到 mybatis 类型的映射表
4. `mvn jetty:run`
5. 注意事项
   
   5.1 需要1.8 以上的 jdk ,前端需 chrome es6 以上
   
   5.2 项目所在路径不能有中文，不然会启动失败

**或者你想更快的运行起来**

下载 release 的 tomcat  版本 

https://github.com/sanri1993/sanri-tools-maven/releases

然后可以直接像运行 tomcat 项目，直接运行



7. 常用模板

   [常用配置信息，把所有内容复制到function.open.properties 配置的 data.config.path 路径中 ](https://github.com/sanri1993/resources/tree/master/sanri-tools-maven/sanritoolsconfig)

8. 隐私说明 

* 由于有些功能用到了个人帐号，我目前还是留在配置文件中，请勿用于非法用途
* 如果有能力，请用私人帐号代替我的帐号
* 此工具纯属个人爱好创作，请勿用于商业用途

### 如何交流、反馈、参与贡献？

* Git仓库：https://gitee.com/sanri/sanri-tools-maven
* 官方QQ群：645576465
* 技术讨论、二次开发等咨询、问题和建议，请移步到 QQ 群，我会在第一时间进行解答和回复
* 如需关注项目最新动态，请Watch、Star项目，同时也是对项目最好的支持
* 微信扫码并关注我，获得项目最新动态及更新提醒

![欢迎加入我的技术群](http://pic.yupoo.com/sanri1993/7e6b6fe8/b40d4a8c.jpg)

![我的微信](http://pic.yupoo.com/sanri1993/7e6b6fe8/b40d4a8c.jpg)



### 更新记录

#### 2019/09/13 更新

- 优化多表方案生成的时候，选表卡死问题
- 增加模板代码生成的时候，可以直接复制代码，直接复制高亮的代码会有问题
- 去掉了旧的 kafka 监控，目前只支持新版本 kafka ,而且配置暂时还没理解透彻对于需要用户密码的，如果需要配置，可以私信我。
- 站内聊天优化了滚动条

#### 2020/01/22 更新

- 优化了 kafka 监控，增加消息时间，所有分区数据查看并以消息时间倒序排序，每次刷新可以获取分区消息的变化量，增加 JSON 数据模拟发送
- 解决 kafka 创建时 sasl 验证模式问题，现可以直接在界面以不同验证方式创建 kafka 连接
- 数据表工具加入刷新功能，创建数据表后不用重新启动项目了
- 数据表代码生成增加 tk.mybats 生成完整可运行的 springboot 项目
- 解决取名工具无法创建业务的问题
- 增加快速建表，这是个简单功能，后续版本会加强

- 待解决：数据表增加表关系，根据表关系来生成代码; 快速建表加强，可以使用以前的表字段来快速建表

#### 2020/05/18 更新

- 增加快速建表功能,可以根据以前的表结构进行快速建表
- 引入类加载器内部功能,可以上传类来操作一些序列化,响应数据模拟功能
- 增加 redis 数据监控工具,查看 redis 拓扑结构
- 增加响应数据模拟工具,可用于在开发时定义好数据结构就可以快速模拟数据，方便给前端提供模拟结构

- 待优化:可能考虑在 tablehelp 中加入索引查看与新建功能,不知道这块的需求有多大
- 待优化:现在上传 DTO 的流程太过复杂,后面考虑上传单个 class 文件,或者直接动态编译 Java 类

#### 2020/?/? 更新

- 首页增加每个工具的使用说明
- 类加载器上传 dto 优化，可以上传单个 class 或单个 java 源文件; zip 上传优化可以不用关心 class 的层次结构 , zip2 后缀兼容以前的完整路径 
- 小说抓取代码优化，数据抓取已经封装注解，可以更方便的抓取数据，不仅仅是抓取小说
- redis 数据查看优化，hash list 结构数据为避免数据过大，添加 key 和范围查询
- 优化随机数据生成，使支持任意类型，除了树型结构
- 增加 swaggerui 文档生成，可以转 word 文档
- kafka 工具增加删除消费组功能,可以更好的配置 kafka 连接
- kafka 工具增加流量监控功能,可以监控 实时,每分钟,每5分钟,每 15 分钟的数据流量; 使用 mBean ,基于 jmxPort

### 演示效果图

![首页](http://pic.yupoo.com/sanri1993/81d03f16/30e994b3.png)

![数据表工具](http://pic.yupoo.com/sanri1993/209d5663/99e29b4b.png)

![模板代码生成](http://pic.yupoo.com/sanri1993/f3b022dd/22d80483.png)

![kafka 消费组列表](http://pic.yupoo.com/sanri1993/7c2f3fc5/fe5c9295.png)

![kafka 消息组消费的主题](http://pic.yupoo.com/sanri1993/87773619/81c9177e.png)

![kafka 消费组消费主题分区监控](http://pic.yupoo.com/sanri1993/8430b3a4/64991a8c.png)

![kafka 主题管理](http://pic.yupoo.com/sanri1993/d2c0764a/c38b3da9.png)

![kafka 分区数据查看](http://pic.yupoo.com/sanri1993/6b8e9dde/87e26451.png)

![kafka json 数据查看](http://pic.yupoo.com/sanri1993/53e12480/0ac54c1d.png)

![kafka 模拟数据发送](http://pic.yupoo.com/sanri1993/474271f9/16bc1259.png)

![redis 数据监控](http://pic.yupoo.com/sanri1993/57c7bdfe/ceb0b8b3.png)

![响应数据模拟](http://pic.yupoo.com/sanri1993/3f48b174/93797df0.png)