package com.mandakini.memonest.adapters;

import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mandakini.memonest.R;
import com.mandakini.memonest.models.Draft;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DraftAdapter extends RecyclerView.Adapter<DraftAdapter.DraftViewHolder> {

    private List<Draft> draftList;
    private OnDraftClickListener listener;
    private boolean bulkMode = false;
    private List<Integer> selectedIds = new ArrayList<>();

    public interface OnDraftClickListener {
        void onView(Draft draft);
        void onEdit(Draft draft);
        void onDelete(Draft draft);
        void onShare(Draft draft);
        void onSelectionChanged(int count);
    }

    public DraftAdapter(List<Draft> draftList, OnDraftClickListener listener) {
        this.draftList = draftList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DraftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_draft, parent, false);
        return new DraftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DraftViewHolder holder, int position) {
        Draft draft = draftList.get(position);

        holder.txtTitle.setText(draft.getTitle());
        holder.txtContent.setText(draft.getContent());

        if (draft.getContent() != null && draft.getContent().length() > 70) {
            holder.txtReadMore.setVisibility(View.VISIBLE);
        } else {
            holder.txtReadMore.setVisibility(View.GONE);
        }

        try {
            Date date = new Date(Long.parseLong(draft.getCreatedAt()));
            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault());
            holder.txtDate.setText(sdf.format(date));
        } catch (Exception e) {
            holder.txtDate.setText(draft.getCreatedAt());
        }

        holder.checkSelect.setOnCheckedChangeListener(null);
        holder.checkSelect.setVisibility(bulkMode ? View.VISIBLE : View.GONE);
        holder.checkSelect.setChecked(selectedIds.contains(draft.getId()));

        holder.btnMore.setVisibility(bulkMode ? View.GONE : View.VISIBLE);

        if (draft.getImageUri() != null && !draft.getImageUri().isEmpty()) {
            try {
                File imageFile = new File(draft.getImageUri());
                if (imageFile.exists()) {
                    holder.imgDraftThumb.setVisibility(View.VISIBLE);
                    holder.imgDraftThumb.setImageURI(Uri.fromFile(imageFile));
                } else {
                    holder.imgDraftThumb.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                holder.imgDraftThumb.setVisibility(View.GONE);
            }
        } else {
            holder.imgDraftThumb.setVisibility(View.GONE);
        }

        if (draft.getIsUploaded() == 1) {
            holder.txtStatus.setText("Uploaded to Firestore");
            holder.txtStatus.setTextColor(0xFF2563EB);
        } else {
            holder.txtStatus.setText("Offline");
            holder.txtStatus.setTextColor(0xFF475569);
        }

        holder.itemView.setOnClickListener(v -> {
            if (bulkMode) {
                toggleSelection(draft.getId());
            } else {
                if (listener != null) listener.onView(draft);
            }
        });

        holder.checkSelect.setOnClickListener(v -> toggleSelection(draft.getId()));

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), holder.btnMore);
            popupMenu.getMenu().add("View");
            popupMenu.getMenu().add("Edit");
            popupMenu.getMenu().add("Delete");
            popupMenu.getMenu().add("Share");

            popupMenu.setOnMenuItemClickListener(item -> {
                if (listener == null) return false;

                String title = item.getTitle().toString();

                if (title.equals("View")) listener.onView(draft);
                else if (title.equals("Edit")) listener.onEdit(draft);
                else if (title.equals("Delete")) listener.onDelete(draft);
                else if (title.equals("Share")) listener.onShare(draft);

                return true;
            });

            popupMenu.show();
        });
    }

    private void toggleSelection(int id) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(Integer.valueOf(id));
        } else {
            selectedIds.add(id);
        }

        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size());
        }

        notifyDataSetChanged();
    }

    public void setBulkMode(boolean enabled) {
        bulkMode = enabled;
        selectedIds.clear();

        if (listener != null) {
            listener.onSelectionChanged(0);
        }

        notifyDataSetChanged();
    }

    public boolean isBulkMode() {
        return bulkMode;
    }

    public List<Integer> getSelectedIds() {
        return selectedIds;
    }

    @Override
    public int getItemCount() {
        return draftList == null ? 0 : draftList.size();
    }

    public void updateList(List<Draft> newList) {
        draftList = newList;
        notifyDataSetChanged();
    }

    public static class DraftViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkSelect;
        TextView txtStatus, txtTitle, txtContent, txtReadMore, txtDate, btnMore;
        ImageView imgDraftThumb;

        public DraftViewHolder(@NonNull View itemView) {
            super(itemView);

            checkSelect = itemView.findViewById(R.id.checkSelect);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtReadMore = itemView.findViewById(R.id.txtReadMore);
            txtDate = itemView.findViewById(R.id.txtDate);
            btnMore = itemView.findViewById(R.id.btnMore);
            imgDraftThumb = itemView.findViewById(R.id.imgDraftThumb);
        }
    }
}