package com.sanri.app.dtos.kafka;

import java.io.Serializable;

public class MBeanInfo {
    private double fifteenMinute;
    private double fiveMinute;
    private double meanRate;
    private double oneMinute;
    private String mBean;

    public MBeanInfo() {
    }

    public MBeanInfo( String mBean,double fifteenMinute, double fiveMinute, double meanRate, double oneMinute) {
        this.fifteenMinute = fifteenMinute;
        this.fiveMinute = fiveMinute;
        this.meanRate = meanRate;
        this.oneMinute = oneMinute;
        this.mBean = mBean;
    }

    public double getFifteenMinute() {
        return fifteenMinute;
    }

    public double getFiveMinute() {
        return fiveMinute;
    }

    public double getMeanRate() {
        return meanRate;
    }

    public double getOneMinute() {
        return oneMinute;
    }

    public String getmBean() {
        return mBean;
    }

    /**
     * 合并多个 broker 的数据
     * @param mBeanInfo
     */
    public void addData(MBeanInfo mBeanInfo) {
        this.fifteenMinute += mBeanInfo.fifteenMinute;
        this.fiveMinute += mBeanInfo.fiveMinute;
        this.meanRate += mBeanInfo.meanRate;
        this.oneMinute += mBeanInfo.oneMinute;
    }
}
