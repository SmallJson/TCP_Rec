package com.bupt.Util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class LogUtil {
    public static boolean flag = false;

    public static BufferedWriter writerSend;
    public static BufferedWriter writerRec;
    public static BufferedWriter writerRouter;

    static{
        try {
            writerSend = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\logSend.txt")));
            writerRec = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\logRec.txt")));
            writerRouter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\logRouter.txt")));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //发送端日志答打印格式
    public static void sendLog(String content){
        if(flag){
            System.out.println("--------------发送端-------------------");
            System.out.println(content);
            System.out.println("--------------发送端-------------------");
        }
    }

    //接收端日志打印格式
    public static void recLog(String content){
        if(flag){
            System.out.println("--------------接收端-------------------");
            System.out.println(content);
            System.out.println("--------------接收端-------------------");
        }
    }

    //路由器行为
    public static void router(String content){
        if(flag){
            System.out.println("--------------路由器-------------------");
            System.out.println(content);
            System.out.println("--------------路由器-------------------");
        }
    }

    //文件形式记录关键日志信息
    public static void  writerToFile(int type,String content){
        switch (type){
            case 1:
                try {
                    //每行记录都换行
                    writerSend.write(content+"\r\n");
                    writerSend.flush();
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    //每行记录都换行
                    writerRec.write(content+"\r\n");
                    writerRec.flush();
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    //每行记录都换行
                    writerRouter.write(content+"\r\n");
                    writerRouter.flush();
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
             default:
                 break;
        }
    }
}
