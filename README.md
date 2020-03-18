# AppiumWeChatAuto
1、如果要实现微信公众号文章的自动采集就必须实现微信公众号文章的自动浏览操作；

2、此工程是基于Appium和Java来实现的Android微信客户端自动化浏览的操作，基于微信6.6.7版本，版本的历史文章列表页面入口都是一致的，历史文章列表只有一种显示形式，比较容易做自动化浏览操作处理。

3、使用AndroidStudio创建项目主要是为了可以直接运行APK到设备或虚拟机上查看所需的设备信息，运行Android工程查看设备信息的时候Edit Configurations切换到app，运行自动化脚本的时候切换到AppiumAutoScan。支持按最近一周，一个月，一年或爬取所有历史文章，checkTimeLimit()传入不同限制时间类型的参数即可。

4、不做Android开发的可以下载Eclipse IDE，在Eclipse下运行Java程序还比较方便，拷贝以下工程源码中的三份文件即可。

* java-client-3.1.0.jar 
* selenium-server-standalone-2.44.0.jar  
* AppiumWeChatAuto/appiumauto/src/main/java/com/example/AppiumAutoScan.java 

Eclipse IDE下载地址： http://www.eclipse.org/downloads/packages/

Java版本和对应的Eclipse IDE版本参考：http://wiki.eclipse.org/Eclipse/Installation

5、完整说明参考个人博文，包括详细的运行环境配置和遇到的问题及解决方法：[微信公众号爬虫：微信公众号浏览自动化](https://www.chenwenguan.com/wechat-browse-automation/)

对应的微信公众号文章爬取JavaWeb服务端实现参考：[微信公众号爬虫：服务端公众号文章数据采集](https://www.chenwenguan.com/wechat-spider-server/)

