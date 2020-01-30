可以选择先把项目运行起来，先要安装第三方包，在 /src/resources/ 目录下使用 cmd 执行 install.txt 中的三行命令

然后项目使用的是 jetty 开发，运行 maven 中的 jetty 插件即可。



本项目基于 servlet 开发，所有的 servlet 保存在 src/com/sanri/app/servlet 目录，自定义的路径映射框架模访 springmvc 

目前写得比较完善的工具有

* 数据表工具 `SqlClientServlet`
* 代码生成 `CodeGenerateServlet`，`MybatisGeneratorServlet`
* 取名工具 `TranslateServlet`
* kafka 数据查看工具 `KafkaServlet`

项目基于文件系统，由 `FileManagerServlet` 统一管理配置文件信息和临时文件信息 ，这两种文件的路径配置在  `function.open.properties` 文件中

由 `ConfigCenter` 统一解析 `*.properties` 配置文件，这是一颗配置树