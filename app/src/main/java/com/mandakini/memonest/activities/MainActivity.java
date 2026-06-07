package com.mandakini.memonest.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mandakini.memonest.R;
import com.mandakini.memonest.adapters.DraftAdapter;
import com.mandakini.memonest.database.DraftDao;
import com.mandakini.memonest.models.Draft;

import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText edtSearch;
    private RecyclerView recyclerDrafts;
    private Button btnAddDraft, btnDeleteSelectedHome;
    private TextView btnBulk;

    private DraftDao draftDao;
    private DraftAdapter adapter;
    private List<Draft> draftList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtSearch = findViewById(R.id.edtSearch);
        recyclerDrafts = findViewById(R.id.recyclerDrafts);
        btnAddDraft = findViewById(R.id.btnAddDraft);
        btnBulk = findViewById(R.id.btnBulk);
        btnDeleteSelectedHome = findViewById(R.id.btnDeleteSelectedHome);

        draftDao = new DraftDao(this);
        draftList = new ArrayList<>();

        recyclerDrafts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DraftAdapter(draftList, new DraftAdapter.OnDraftClickListener() {
            @Override
            public void onView(Draft draft) {
                Intent intent = new Intent(MainActivity.this, DraftDetailActivity.class);
                intent.putExtra("draft_id", draft.getId());
                startActivity(intent);
            }

            @Override
            public void onEdit(Draft draft) {
                Intent intent = new Intent(MainActivity.this, EditDraftActivity.class);
                intent.putExtra("draft_id", draft.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Draft draft) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Draft")
                        .setMessage("Are you sure you want to delete \"" + draft.getTitle() + "\"?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            draftDao.deleteDraft(draft.getId());
                            loadDrafts();

                            Toast.makeText(
                                    MainActivity.this,
                                    "Draft Deleted",
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onShare(Draft draft) {

                try {

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);

                    String shareText =
                            draft.getTitle()
                                    + "\n\n"
                                    + draft.getContent();

                    shareIntent.putExtra(
                            Intent.EXTRA_SUBJECT,
                            draft.getTitle()
                    );

                    shareIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            shareText
                    );

                    if (draft.getImageUri() != null &&
                            !draft.getImageUri().isEmpty()) {

                        File imageFile = new File(draft.getImageUri());

                        if (imageFile.exists()) {

                            Uri imageUri = FileProvider.getUriForFile(
                                    MainActivity.this,
                                    getPackageName() + ".provider",
                                    imageFile
                            );

                            shareIntent.setType("image/*");

                            shareIntent.putExtra(
                                    Intent.EXTRA_STREAM,
                                    imageUri
                            );

                            shareIntent.addFlags(
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );

                        } else {

                            shareIntent.setType("text/plain");
                        }

                    } else {

                        shareIntent.setType("text/plain");
                    }

                    startActivity(
                            Intent.createChooser(
                                    shareIntent,
                                    "Share Draft"
                            )
                    );

                } catch (Exception e) {

                    Toast.makeText(
                            MainActivity.this,
                            "Unable to share draft",
                            Toast.LENGTH_SHORT
                    ).show();

                    e.printStackTrace();
                }
            }

            @Override
            public void onSelectionChanged(int count) {
                if (adapter != null && adapter.isBulkMode()) {
                    btnBulk.setText(count > 0 ? count + " selected" : "Cancel");
                }
            }
        });

        recyclerDrafts.setAdapter(adapter);

        loadDrafts();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();

                if (keyword.isEmpty()) {
                    loadDrafts();
                } else {
                    List<Draft> searchResults = draftDao.searchDrafts(keyword);
                    adapter.updateList(searchResults);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnAddDraft.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewDraftActivity.class);
            startActivity(intent);
        });

        btnBulk.setOnClickListener(v -> {
            boolean newMode = !adapter.isBulkMode();

            adapter.setBulkMode(newMode);

            btnDeleteSelectedHome.setVisibility(newMode ? View.VISIBLE : View.GONE);
            btnBulk.setText(newMode ? "Cancel" : "Bulk Select");
        });

        btnDeleteSelectedHome.setOnClickListener(v -> {
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
                        loadDrafts();

                        adapter.setBulkMode(false);
                        btnDeleteSelectedHome.setVisibility(View.GONE);
                        btnBulk.setText("Bulk Select");

                        Toast.makeText(this, "Selected drafts deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadDrafts() {
        draftList = draftDao.getAllDrafts();
        adapter.updateList(draftList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDrafts();
    }
}