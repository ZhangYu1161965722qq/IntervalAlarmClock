package com.zhangyu.intervalalarmclock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyDateTimeAdapter extends RecyclerView.Adapter<MyDateTimeAdapter.ViewHolder>{

    private List<MyDateTime> myDateTimeList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView dateTimeId;
        TextView dateTime;
        CheckBox dateTimeMark;

        public ViewHolder(View view){
            super(view);
            dateTimeId = view.findViewById(R.id.textView_DateTime_Num);
            dateTime = view.findViewById(R.id.textView_DateTime);
            dateTimeMark = view.findViewById(R.id.checkBox_DateTime_mark);
        }
    }

    public MyDateTimeAdapter(List<MyDateTime> dateTimeList){
        myDateTimeList = dateTimeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.datetime_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyDateTime myDateTime = myDateTimeList.get(position);
        holder.dateTimeId.setText(myDateTime.getNum());
        holder.dateTime.setText(myDateTime.getDateTime());
        holder.dateTimeMark.setText(myDateTime.getMark());
        holder.dateTimeMark.setChecked(myDateTime.getFlagCheckbox());
    }

    @Override
    public int getItemCount() {
        return myDateTimeList.size();
    }
}


