package com.example.mediaexplorer.ui.details;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaexplorer.R;
import com.example.mediaexplorer.data.remote.dto.CastDto;
import com.example.mediaexplorer.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.VH> {

    public interface OnActorClickListener {
        void onActorClick(CastDto actor);
    }

    private final List<CastDto> items = new ArrayList<>();
    private final OnActorClickListener listener;

    public CastAdapter(OnActorClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CastDto> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cast, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CastDto c = items.get(position);

        holder.txtName.setText(c.name != null ? c.name : "-");
        holder.txtRole.setText(c.character != null ? c.character : "-");

        String url = c.profilePath != null
                ? Constants.TMDB_IMAGE_BASE_URL + c.profilePath
                : null;

        Glide.with(holder.imgActor.getContext())
                .load(url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.imgActor);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onActorClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgActor;
        TextView txtName, txtRole;

        VH(@NonNull View itemView) {
            super(itemView);
            imgActor = itemView.findViewById(R.id.imgActor);
            txtName = itemView.findViewById(R.id.txtName);
            txtRole = itemView.findViewById(R.id.txtRole);
        }
    }
}