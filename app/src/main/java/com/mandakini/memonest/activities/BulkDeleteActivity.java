package com.mandakini.memonest.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mandakini.memonest.R;
import com.mandakini.memonest.adapters.BulkDraftAdapter;
import com.mandakini.memonest.database.DraftDao;
import com.mandakini.memonest.models.Draft;

import java.util.List;

public class BulkDeleteActivity extends AppCompatActivity {

    private TextView btnBackBulk, txtSelectedCount;
    private RecyclerView recyclerBulkDrafts;
    private Button btnDeleteSelected;

    private DraftDao draftDao;
    private BulkDraftAdapter adapter;
    private List<Draft> draftList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_delete);

        btnBackBulk = findViewById(R.id.btnBackBulk);
        txtSelectedCount = findViewById(R.id.txtSelectedCount);
        recyclerBulkDrafts = findViewById(R.id.recyclerBulkDrafts);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);

        draftDao = new DraftDao(this);
        draftList = draftDao.getAllDrafts();

        recyclerBulkDrafts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BulkDraftAdapter(draftList, count -> {
            txtSelectedCount.setText(count + " selected");
        });

        recyclerBulkDrafts.setAdapter(adapter);

        btnBackBulk.setOnClickListener(v -> finish());

        btnDeleteSelected.setOnClickListener(v -> {
            List<Integer> selectedIds = adapter.getSelectedIds();

            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "Please select drafts", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Delete Selected Drafts")
                    .setMessage("Are you sure you want to delete " + selectedIds.size() + " selected drafts?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        draftDao.deleteMultipleDrafts(selectedIds);
                        Toast.makeText(this, "Selected drafts deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}