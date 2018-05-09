package com.bupt.Util;

import com.bupt.model.Packet;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketUtil {

    //接收端的的ServerSocket
    public static ServerSocket recSocket;
    //路由器的socket地址
    public static Socket routerSocket;
    public static ObjectOutputStream obRouterWriter;
    public static ObjectInputStream obRouterReader;
    public static BufferedWriter writer;
    public static BufferedReader reader;

    //转发报文给路由器
    public static boolean sendPacketToRouter(Packet packet){
        Boolean result = true;
        try{
            //obRouterWriter.writeObject(packet);\
            writer.write(JacksonUtil.writeValueAsString(packet)+"\r\n");
            writer.flush();
            //System.out.println("接收端:"+packet);
        } catch (Exception e){
            System.out.println("发送端发送ACK报文失败");
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    //从路由器获取报文
    public static Packet getPacketFromRouter(){
        Packet packet = null;
        try {
            //packet = (Packet) obRouterReader.readObject();
            packet = JacksonUtil.readValue(reader.readLine(),Packet.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  packet;
    }


    public  static void initServerSocket(){
        //此处监听会阻塞线程
        System.out.println("接收端开始监听");
            try {
                recSocket = new ServerSocket(Contans.recPort);
                routerSocket = recSocket.accept();
                InputStream inputStream = routerSocket.getInputStream();
                OutputStream outputStream = routerSocket.getOutputStream();
                reader  = new BufferedReader(new InputStreamReader(inputStream));
                writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                //obRouterReader = new ObjectInputStream(inputStream);
                //obRouterWriter = new ObjectOutputStream(outputStream);
                System.out.println("接收端和路由器对接成功");
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("路由器监听端口失败1");
            }
    }

}
