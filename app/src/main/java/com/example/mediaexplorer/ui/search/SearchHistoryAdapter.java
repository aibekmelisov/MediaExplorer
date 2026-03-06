package com.example.mediaexplorer.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediaexplorer.R;
import com.example.mediaexplorer.data.local.SearchHistoryEntity;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.VH> {

    public interface Listener {
        void onClick(String query);
    }

    private final Listener listener;
    private final List<SearchHistoryEntity> items = new ArrayList<>();

    public SearchHistoryAdapter(Listener l) {
        listener = l;
    }

    public void setItems(List<SearchHistoryEntity> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SearchHistoryEntity e = items.get(position);
        h.title.setText(e.query);
        h.itemView.setOnClickListener(v -> listener.onClick(e.query));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
        }
    }

    public SearchHistoryEntity getItem(int position) {
        return items.get(position);
    }

    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) return;
        items.remove(position);
        notifyItemRemoved(position);
    }
}