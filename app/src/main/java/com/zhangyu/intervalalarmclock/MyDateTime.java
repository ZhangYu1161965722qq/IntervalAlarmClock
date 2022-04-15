package com.zhangyu.intervalalarmclock;

public class MyDateTime {
    private String num;
    private String dateTime;
    private String mark;
    private boolean flagCheckbox;

    public MyDateTime(long num, String dateTime, String mark, boolean flagCheckbox) {
        this.num = String.valueOf(num);
        this.dateTime = dateTime;
        this.mark = mark;
        this.flagCheckbox = flagCheckbox;
    }

    public String getNum() {
        return num;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getMark(){
        return mark;
    }

    public boolean getFlagCheckbox(){
        return flagCheckbox;
    }

}
