package com.teinvdlugt.android.luckytv;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class LuckyAdapter extends RecyclerView.Adapter<LuckyAdapter.ViewHolder> {

    private List<Entry> data;
    private Context context;

    public LuckyAdapter(Context context, List<Entry> data) {
        this.context = context;
        this.data = data;
    }

    public LuckyAdapter(Context context) {
        this.context = context;
        this.data = new ArrayList<>();
    }

    public List<Entry> getData() {
        return data;
    }

    public void setData(List<Entry> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public LuckyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(LuckyAdapter.ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTV, dateTV;
        private ImageView imageView;
        private Entry entry;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTV = (TextView) itemView.findViewById(R.id.title);
            dateTV = (TextView) itemView.findViewById(R.id.date);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, VideoActivity.class)
                            .putExtra(VideoActivity.ENTRY_EXTRA, entry));
                }
            });
        }

        public void bind(Entry data) {
            this.entry = data;
            titleTV.setText(data.getTitle());
            dateTV.setText(data.getDate());
            Picasso.with(context).load(data.getImageUrl()).into(imageView);
        }
    }
}
