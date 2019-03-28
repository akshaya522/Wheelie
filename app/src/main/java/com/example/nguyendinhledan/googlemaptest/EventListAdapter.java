package com.example.nguyendinhledan.googlemaptest;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventListAdapter extends ArrayAdapter<Event> {
    private static final String TAG = "EventListAdapter";

    private Context mContext;
    private int mResource;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public EventListAdapter(Context context, int resource, ArrayList objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    private static class ViewHolder {
        TextView name;
        TextView description;
        ImageView img;
        TextView slot;
        TextView carpark;
        TextView time;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        setupImageLoader();

        String name = getItem(position).getName();
        String description = getItem(position).getDescription();
        String img = getItem(position).getImg();
        int slot = getItem(position).getSlots();
        int carpark = getItem(position).getNumberOfCarpark();
        Date date_start = getItem(position).getDatetimeStart();

        final View result;

        ViewHolder holder;

        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.event_name);
            holder.description = (TextView) convertView.findViewById(R.id.event_description);
            holder.img = (ImageView) convertView.findViewById(R.id.event_image);
            holder.slot = (TextView) convertView.findViewById(R.id.slots);
            holder.carpark = (TextView) convertView.findViewById(R.id.number_carpark);
            holder.time = (TextView) convertView.findViewById(R.id.time_remaining);

            result = convertView;
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        int fallback = mContext.getResources().getIdentifier("@drawable/event_failed", null, mContext.getPackageName());

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(fallback)
                .showImageOnFail(fallback)
                .showImageOnLoading(fallback).build();

        imageLoader.displayImage(img, holder.img, options);
        Log.d(TAG, "getView: loading image");
        holder.name.setText(name);
        holder.description.setText(description);
        holder.slot.setText(String.valueOf(slot));
        holder.carpark.setText(String.valueOf(carpark));
        if (date_start.before(Calendar.getInstance().getTime())){
            holder.time.setText(R.string.happen);
            holder.time.setTextColor(Color.RED);
        }
        else{
            if (date_start.getDay() - Calendar.getInstance().getTime().getDay() > 0){
                holder.time.setText(String.format("%d day", date_start.getDay() - Calendar.getInstance().getTime().getDay()));
            }
            else{
                if (date_start.getHours() - Calendar.getInstance().getTime().getHours() > 0){
                    holder.time.setText(String.format("%d hours", date_start.getHours() - Calendar.getInstance().getTime().getHours()));
                }
                else {
                    holder.time.setText(String.format("%d minutes", date_start.getMinutes() - Calendar.getInstance().getTime().getMinutes()));
                }
            }
            holder.time.setTextColor(Color.BLACK);
        }

        return convertView;


    }

    private void setupImageLoader(){
        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP
    }
}
