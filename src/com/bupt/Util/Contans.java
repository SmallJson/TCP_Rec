package com.bupt.Util;

public class Contans {
    //前向链路的传输延迟
    public static final  long forwardLinkDelay = 100;

    //方向链路的传输延迟
    public static final long returnLinkDelay = 100;

    //需要发包的总大小
    public static final int totalCount = 3000;

    //发送端IP地址
    public static final String sendIP = "127.0.0.1";
    //发送端监听端口
    public static final int sendPort = 10005;

    //路由器IP地址
    public static final String routeIP = /*"127.0.0.1"*/"10.108.245.100";
    //路由器监听端口
    public static final int routePort =10006;
    //接收端IP地址
    public static final String recIP = "127.0.0.1";
    //接收端端口
    public static final  int recPort = 10007;
}
