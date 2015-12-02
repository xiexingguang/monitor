# Monitor1.0版本说明

##1.0 实现的功能为：
      ###nsqd:模块监控
      1.1 目前只能监控一个集群环境下的所有nsqd节点的状态信息
      1.2 能够动态配置需要监控的topic，以及topic 阻塞消息，回退消息，超时消息的阀值
      1.3 监控nsqd,nsqlookup 节点是否down机

   目前报警级别只支持topic级别，nsqd级别，以及channel级别消息阀值报警以后如果有需求可以扩展.

   *待实现，ec-watcher 包zh支持超过阀值自动报警发邮件功能