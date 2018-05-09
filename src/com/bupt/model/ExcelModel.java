package com.bupt.model;

import java.util.ArrayList;

public class ExcelModel {
   public ArrayList<ArrayList<Object>> resultList = new ArrayList<>();

   public ExcelModel(){
       //窗口
       resultList.add(new ArrayList<Object>());
       //时间
       resultList.add(new ArrayList<Object>());
       //ack次数
       resultList.add(new ArrayList<Object>());
   }

   public void add(double cwd, Long time, Integer  ack){
       resultList.get(0).add(cwd);
       resultList.get(1).add(time);
       resultList.get(2).add(ack);
   }
}
