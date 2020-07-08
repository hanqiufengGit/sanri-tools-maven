可以选择先把项目运行起来，先要安装第三方包，在 /src/resources/ 目录下使用 cmd 执行 install.txt 中的三行命令

然后项目使用的是 jetty 应用服务器，运行 maven 中的 jetty 插件即可。

如果打开项目有报错，排除包没有下载下来的问题，一般下载完包后就可以直接运行 `mvn jetty:run` 


本项目基于 servlet 开发，所有的 servlet 保存在 src/com/sanri/app/servlet 目录，自定义的路径映射框架模仿 springmvc 

目前写得比较完善的工具有

* 数据表工具 `SqlClientServlet`
* 代码生成 `CodeGenerateServlet`，`MybatisGeneratorServlet`
* 取名工具 `TranslateServlet`
* kafka 数据查看工具 `KafkaServlet`
* redis 数据监控 `RedisConnectServlet`
* 响应数据模拟工具 `RandomDataServlet`
* swaggerui工具 `SwaggerServlet`
* webservice工具 `WSCallServlet`
* 身份证工具  没有后端

项目基于文件系统，由 `FileManagerServlet` 统一管理配置文件信息和临时文件信息 ，这两种文件的路径配置在  `function.open.properties` 文件中

由 `ConfigCenter` 统一解析 `*.properties` 配置文件，这是一颗配置树

2020/05/18 最近添加了一些接口的测试,写在 requests 目录中,读者可以在这里看到我的接口测试过程,后续应该所有的接口都会在这里进行测试