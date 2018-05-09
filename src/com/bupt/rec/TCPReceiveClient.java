package com.bupt.rec;

import com.bupt.Util.Contans;
import com.bupt.Util.LogUtil;
import com.bupt.Util.SocketUtil;
import com.bupt.model.Packet;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

//TCP接受客户端
public class TCPReceiveClient {
    /**
     * 利用TreeSet来缓存接受的数据包
     * 达到排序以及去重的作用
     * TreeSet的排序规则按照报文序列号，从小到大排序
     */
    ConcurrentSkipListSet<Packet> cachePacktet = new ConcurrentSkipListSet<Packet>(new Comparator<Packet>() {
        @Override
        public int compare(Packet o1, Packet o2) {
            return o1.seq-o2.seq;
        }
    });

    //发送出去的ACK报文数量
    public static volatile int ackCount = 0;
    //加入阻塞队列的Ack报文数量
    public static  volatile int ackTotal = 0;
    //从阻塞队列中获取的数据包
    public  static  volatile int ackQueue = 0;
    //收到的数据报文的数量
    public static volatile int recTotal = 0;
    //接收端报文队列的缓存大小
    volatile int cacheSize = 5;

    //采用延迟确认的策略
    public long time = 500L;

    //存储ack数据包的延迟队列
    DelayQueue<Packet> delayQueue = new DelayQueue<>();

    //发送数据包的线程池,保证数据包是顺序出去的。
    ExecutorService  exector = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    //构造方法
    public TCPReceiveClient(){

    }

    public void init(){
        initServerSocket();
        initThread();
        //序列为0的ack报文垫底
        cachePacktet.add(new Packet(true,0,0L, Contans.returnLinkDelay));
        loopDelayQueue();
    }
    private void initServerSocket(){
        SocketUtil.initServerSocket();
    }
    private void initThread(){
        //开启一个接收线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("从路由器拿数据的线程");
                while(true){
                //    System.out.println("rec");
                    Packet packet = SocketUtil.getPacketFromRouter();
                    recTotal++;
                    if(packet != null)
                        arrivePacket(packet);
                }
            }
        }).start();

        //开启一个定时报告ACK信息的调度任务
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("cachePacket="+cachePacktet.size()+",delayQueueSize="+delayQueue.size()
                        +",ackCount="+ackCount+",recTotal="+recTotal+",ackTotal="+ackTotal+",ackQueue="+ackQueue
                        +",fisrtSkipNum="+getSkipNum()+",lastNum="+cachePacktet.last().seq);
            }
        },30000L,5000L);
    }

    //从延迟队列不间断拿包发送
    private void loopDelayQueue(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    Packet packet =null;
                    try {
                        packet = delayQueue.poll();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(packet == null){
                        continue;
                    }
                    final Packet pk = packet;
                    ackQueue++;
                    exector.submit(new Runnable() {
                        @Override
                        public void run() {
                            sendAck(pk);
                        }
                    });
                }
            }
        }).start();
    }


    /**
     * TreeSet存储接受到的数据报文
     */
    private void arrivePacket(Packet packet){
       /* if(packet == null)
            System.out.println("定时报告");
        else
          //  System.out.println(packet.toString());
        */
        //相同数据报文在丢弃，不同数据报文加入
        if (packet !=null && !cachePacktet.contains(packet)){
            cachePacktet.add(packet);
        }
        ArrayList<Integer> seqList = new ArrayList<Integer>();

        //int [] seqNum = new int [cachePacktet.size()];
        //获取第一个不连续点
        int ackNum = getSkipNum();
        //ack报文入队
        Packet ackPacket = null;
        if(packet != null)
            ackPacket =new Packet(false,ackNum,packet.startTime,System.currentTimeMillis(), Contans.returnLinkDelay);
        else
            ackPacket =new Packet(false,ackNum,null,System.currentTimeMillis(), Contans.returnLinkDelay);
        int preCount = delayQueue.size();
        delayQueue.put(ackPacket);
        int newCount = delayQueue.size();
        int deltaCount = newCount-preCount;
        if(deltaCount<=0){
            System.out.println("**********************");
            System.out.println("deltaCount="+deltaCount);
            System.out.println("**********************");
        }
        ackTotal++;
    }

    //找到缓存报文中第一个不连续点
    public int getSkipNum(){
        ArrayList<Integer> seqList = new ArrayList<Integer>();

        //int [] seqNum = new int [cachePacktet.size()];
        int ackNum = cachePacktet.last().seq;
        int i = 0;
        for(Packet pk : cachePacktet){
            seqList.add(pk.seq);
        }
        //System.out.println(seqNum);
        for(int index = 0; index < seqList.size()-1;index++){
            if(seqList.get(index) != seqList.get(index+1) -1){
                //找到第一个不连续点
                ackNum = index;
                break;
            }
        }
        return ackNum;
    }

    private  void sendAck(Packet packet){
            //sendClient.recACK(packet);
           SocketUtil.sendPacketToRouter(packet);
            ackCount++;
            LogUtil.writerToFile(3,"ack:"+packet.toString());
         //   System.out.println(packet);
    }

}
