# ChatGPT 接口

## 插件

### lombok

model 使用@Data 注解
@Data 注解包含@Getter,@Setter,@ToString 这 3 个注解的功能，一般使用@Data 注解即可

日志使用@Slf4j 注解

## 常用工具

### HuTool

小而全的 Java 工具类库: https://www.hutool.cn/docs/#/

### MyBatis-Plus

MyBatis 的增强工具: https://baomidou.com/pages/24112f/

util 下的`MPGenerator`为代码生成工具，可直接执行 main 方法

## 配置

log.aspect.enable controller 层切面日志

## model 层

vo：View Object，视图层，其作用是将指定页面的展示数据封装起来。
entity: 数据库对应
model: 其他实体

## 接口文档

API 文档地址 http://localhost:8080/doc.html

使用到 knife4j 文档
https://doc.xiaominfo.com/knife4j/documentation/description.html
参数使用 Map
https://doc.xiaominfo.com/knife4j/documentation/dynamicRequestDescription.html

## 发布

当前项目进行了 jar 依赖和配置文件进行了提出，所以在以来修改和配置文件修改后需要重新上传
打包后的文件在`target/p5wGPT`文件下，
相关文件说明：

1. config 是存放配置文件的地方
2. repository 是存放依赖报的地方
3. run.sh 是启动脚本

新环境下可直接将 p5wGPT 上传到服务器，执行脚本`sh run.sh start`即可启动

发布新版本 jar 包，如未修改配置文件和依赖，可直接将`p5wGPT`下 jar 包上传到服务器，其和`p5wGPT`下 jar 包是一样的
如修改了配置文件则需要上传 `config`
修改或新增了依赖则需要上传 `repository`

重启使用命令`sh run.sh restart`,执行时会对当前 jar 包进行备份


## 优化点

1、段落分段，pdf表格
2、prompt优化
3、向量数据库，相似度限制

[基于序列建模的文本分割模型](https://modelscope.cn/models/damo/nlp_bert_document-segmentation_chinese-base/summary)
[ChatGPT 提示的艺术：制作清晰有效咒语](https://github.com/wikieden/Awesome-ChatGPT-Prompts-CN/blob/main/ChatGpt-receipt.md)
