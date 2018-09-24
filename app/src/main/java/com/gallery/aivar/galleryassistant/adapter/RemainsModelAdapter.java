package com.gallery.aivar.galleryassistant.adapter;

import android.content.Context;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gallery.aivar.galleryassistant.R;
import com.gallery.aivar.galleryassistant.pojo.RemainsModel;

import java.util.List;

import static com.gallery.aivar.galleryassistant.R.layout.item_layout;

public class RemainsModelAdapter extends BaseAdapter {

    private List<RemainsModel> list;
    private LayoutInflater layoutInflater;

    public RemainsModelAdapter(Context context, List<RemainsModel> list) {
        this.list = list;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = layoutInflater.inflate(item_layout, parent, false);
        }

        RemainsModel remainsModel = getRemainsModel(position);

        TextView textView = (TextView) view.findViewById(R.id.textView);
        TextView textView2 = (TextView) view.findViewById(R.id.textView2);
        textView.setText(remainsModel.getParty());
        textView2.setText(remainsModel.getRemain());

        return view;
    }

    private RemainsModel getRemainsModel(int position){
        return (RemainsModel) getItem(position);
    }
}
