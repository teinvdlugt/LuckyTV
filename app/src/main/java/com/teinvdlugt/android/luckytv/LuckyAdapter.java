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

public class LuckyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_ENTRY = 0;
    public static final int VIEW_TYPE_PROGRESS_BAR = 1;

    private List<Entry> data;
    private Context context;
    private LoadNextYearListener loadNextYearListener;

    public interface LoadNextYearListener {
        void loadNextYear();
    }

    public LuckyAdapter(Context context, LoadNextYearListener loadNextYearListener) {
        this.context = context;
        this.data = new ArrayList<>();
        this.loadNextYearListener = loadNextYearListener;
    }

    public void setData(List<Entry> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void addItems(List<Entry> items) {
        this.data.addAll(items);
        notifyItemRangeInserted(data.size() - items.size(), items.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ENTRY) {
            return new EntryViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false));
        } else if (viewType == VIEW_TYPE_PROGRESS_BAR) {
            return new ProgressBarViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_progress_bar, parent, false));
        } else return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EntryViewHolder) {
            ((EntryViewHolder) holder).bind(data.get(position));
        } else if (holder instanceof ProgressBarViewHolder) {
            if (loadNextYearListener != null) loadNextYearListener.loadNextYear();
        }
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == data.size() ? VIEW_TYPE_PROGRESS_BAR : VIEW_TYPE_ENTRY;
    }

    class EntryViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTV, dateTV;
        private ImageView imageView;
        private Entry entry;

        public EntryViewHolder(View itemView) {
            super(itemView);
            titleTV = (TextView) itemView.findViewById(R.id.title);
            dateTV = (TextView) itemView.findViewById(R.id.date);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);

            itemView.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
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

    static class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        private View progressBar, textView;

        public ProgressBarViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
            textView = itemView.findViewById(R.id.thatWasIt_textView);
        }
    }
}
