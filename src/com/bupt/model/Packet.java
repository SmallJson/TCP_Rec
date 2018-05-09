package com.bupt.model;

import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

//对应TCP包的实体类
public class Packet implements Delayed{
    /**
     * flag ==true，代表是数据报文
     * flag = false,代表是ack报文
     */
    public boolean flag;
    //数据报文的序列号
    public int seq;
    //ack报文的响应号
    public int ack;
    //数据报文离开发送端时间
    public  Long startTime;
    //数据报文到达接收端时间
    public Long arriveTime;
    //数据报文回到发送端时间
    public Long endTime;

    public Packet() {
    }

    public Long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }

    public static AtomicLong getAtomic() {
        return atomic;
    }

    public static void setAtomic(AtomicLong atomic) {
        Packet.atomic = atomic;
    }

    public long getN() {
        return n;
    }

    public void setN(long n) {
        this.n = n;
    }

    /**
     * 生成数据报文对应的构造方法
     * @param flag
     * @param seq 数据报文序号
     * @param startTime 离开发送端时间
     * @param delayTime 正向链路传播延迟
     */
    public Packet(boolean flag, int seq, Long startTime, Long delayTime){
        this.flag = flag;
        this.seq = seq;
        this.startTime = startTime;
        this.delayTime = System.currentTimeMillis() + delayTime;
        n  = atomic.getAndIncrement();
    }

    /**
     * @param flag
     * @param ack 相应报文序号
     * @param startTime 数据包离开发送端时间
     * @param arriveTime 数据报文到达接收端时间
     * @param delayTime 反向链路传播时延
     */
    public Packet(boolean flag,int ack, Long startTime, Long arriveTime,Long delayTime){
        this.flag = flag;
        this.ack = ack;
        this.startTime = startTime;
        this.arriveTime = arriveTime;
        this.delayTime = System.currentTimeMillis() + delayTime;
        n = atomic.getAndIncrement();
    }

    //代表数据报文对应的传输延迟
    public Long delayTime;

    //数据包的唯一顺序标识
    public static AtomicLong atomic = new AtomicLong(0);
    public long n ;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(Long arriveTime) {
        this.arriveTime = arriveTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    //计算数据包剩余的传播时延
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.delayTime-System.currentTimeMillis(),TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        if (other == this)
            return 0;
        if (other instanceof Packet)
        {
            Packet x = (Packet)other;
            long diff = this.delayTime - x.delayTime;
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;
            else if (n < x.n)
                return -1;
            else
                return 1;
        }
        long d = (getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS));
        return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
    }

    @Override
    public String toString() {
        return "Packet{" +
                "flag=" + flag +
                ", seq=" + seq +
                ", ack=" + ack +
                ", startTime=" + startTime +
                ", arriveTime=" + arriveTime +
                ", endTime=" + endTime +
                ", delayTime=" + delayTime +
                ", n=" + n +
                '}';
    }


    /**
     * seq，ack flag，delayTime这四个字段相等
     * 认定为是同一个数据报文段
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Packet)) return false;
        Packet packet = (Packet) o;
        return flag == packet.flag &&
                seq == packet.seq &&
                ack == packet.ack;
    }

    @Override
    public int hashCode() {

        return Objects.hash(flag+"", seq+"", ack+"");
    }
}
