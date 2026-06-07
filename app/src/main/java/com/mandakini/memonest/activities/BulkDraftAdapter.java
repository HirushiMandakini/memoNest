package com.mandakini.memonest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mandakini.memonest.R;
import com.mandakini.memonest.models.Draft;

import java.util.ArrayList;
import java.util.List;

public class BulkDraftAdapter extends RecyclerView.Adapter<BulkDraftAdapter.BulkViewHolder> {

    private List<Draft> draftList;
    private List<Integer> selectedIds = new ArrayList<>();
    private OnSelectionChangeListener listener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    public BulkDraftAdapter(List<Draft> draftList, OnSelectionChangeListener listener) {
        this.draftList = draftList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BulkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bulk_draft, parent, false);
        return new BulkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BulkViewHolder holder, int position) {
        Draft draft = draftList.get(position);

        holder.txtBulkTitle.setText(draft.getTitle());
        holder.txtBulkContent.setText(draft.getContent());

        holder.checkDraft.setOnCheckedChangeListener(null);
        holder.checkDraft.setChecked(selectedIds.contains(draft.getId()));

        holder.checkDraft.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedIds.contains(draft.getId())) {
                    selectedIds.add(draft.getId());
                }
            } else {
                selectedIds.remove(Integer.valueOf(draft.getId()));
            }

            if (listener != null) {
                listener.onSelectionChanged(selectedIds.size());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            holder.checkDraft.setChecked(!holder.checkDraft.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return draftList == null ? 0 : draftList.size();
    }

    public List<Integer> getSelectedIds() {
        return selectedIds;
    }

    public static class BulkViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkDraft;
        TextView txtBulkTitle, txtBulkContent;

        public BulkViewHolder(@NonNull View itemView) {
            super(itemView);

            checkDraft = itemView.findViewById(R.id.checkDraft);
            txtBulkTitle = itemView.findViewById(R.id.txtBulkTitle);
            txtBulkContent = itemView.findViewById(R.id.txtBulkContent);
        }
    }
}