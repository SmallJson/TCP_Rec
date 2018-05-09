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
    /**
     * 以下四个是调试程序时的统计量信息
     */
    //发送出去的ACK报文数量
    public static volatile int ackCount = 0;
    //加入阻塞队列的Ack报文数量
    public static  volatile int ackTotal = 0;
    //从阻塞队列中获取的数据包
    public  static  volatile int ackQueue = 0;
    //收到的数据报文的数量
    public static volatile int recTotal = 0;

    /**
     * 接收端延迟确认的策略:
     * 定时时间到，不管是否超过缓冲阈值，发送ACK报文，并且清空缓冲区计数
     * 超过缓冲阈值，不管是否定时时间到，发送ACK报文，并且让定时时间失效
     */
    //1.定时发送ACK报文时间间隔
    public long time = 1000L;
    //2.接收端缓冲报文阈值，超过该阈值则发送ack报文
    public int cacheSize = 20;
    //当前缓冲的ACK报文大小
    public int existPacketSize = 0;
    //清空定时时间的标准
    public boolean ackFlag =false;

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
        //序列为0的ack报文垫底
        cachePacktet.add(new Packet(true,0,0L, Contans.returnLinkDelay));
        initServerSocket();
        initThread();
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

        //开启一个定时报告统计信息的调度任务
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("cachePacket="+cachePacktet.size()+",delayQueueSize="+delayQueue.size()
                        +",ackCount="+ackCount+",recTotal="+recTotal+",ackTotal="+ackTotal+",ackQueue="+ackQueue
                        +",fisrtSkipNum="+getSkipNum()+",lastNum="+cachePacktet.last().seq);
            }
        },30000L,5000L);

        //开启一个ACK反馈机制
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(ackFlag){
                    //定时时间到，但是已经触发缓冲区溢出的条件，不做定时反馈
                    ackFlag = false;
                }else{
                    //没有触发缓冲区溢出条件，所以做定时反馈
                    if(cachePacktet.last().seq !=0)
                        arrivePacket(null);
                    existPacketSize = 0;
                }
            }
        },0L,time);
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
            //新来的报文，缓冲计数+1
            existPacketSize++;
        }
        if(packet != null && existPacketSize < 20){
            //缓冲区未溢出不发送ACK报文
            return ;
        }

        if(packet != null){
            //缓冲区溢出发送ACK报文,设置标志位
            ackFlag = true;
            existPacketSize = 0;
        }

        //获取第一个不连续点
        Packet dataPacket = getSkipPacket();

        //ack报文
        Packet ackPacket = null;
        if(packet != null){
            //缓冲区溢出报告ACK报文
            ackPacket =new Packet(false,dataPacket.seq,packet.startTime,System.currentTimeMillis(), Contans.returnLinkDelay);
        }
        else{
            //定时时间到报告ACK报文
            ackPacket =new Packet(false,dataPacket.seq,dataPacket.startTime,System.currentTimeMillis(), Contans.returnLinkDelay);
        }

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

    /**
     * 或许第一个序号不连续的报文
     * @return
     */
    public Packet getSkipPacket(){
        ArrayList<Packet> seqList = new ArrayList<Packet>();

        //int [] seqNum = new int [cachePacktet.size()];
        Packet ackPacket = cachePacktet.last();
        int i = 0;
        for(Packet pk : cachePacktet){
            seqList.add(pk);
        }
        //System.out.println(seqNum);
        for(int index = 0; index < seqList.size()-1;index++){
            if(seqList.get(index).seq != seqList.get(index+1).seq -1){
                //找到第一个不连续点
                ackPacket = seqList.get(index);
                break;
            }
        }
        return ackPacket;
    }
    private  void sendAck(Packet packet){
            //sendClient.recACK(packet);
           SocketUtil.sendPacketToRouter(packet);
            ackCount++;
            LogUtil.writerToFile(3,"ack:"+packet.toString());
         //   System.out.println(packet);
    }

}
