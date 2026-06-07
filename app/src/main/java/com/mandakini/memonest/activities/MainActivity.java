package com.mandakini.memonest.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText edtSearch;
    private RecyclerView recyclerDrafts;
    private Button btnAddDraft;
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
                        .setMessage("Are you sure you want to delete \"" +
                                draft.getTitle() + "\" ?")
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
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, draft.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, draft.getTitle() + "\n\n" + draft.getContent());
                startActivity(Intent.createChooser(shareIntent, "Share Draft"));
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
            Intent intent =
                    new Intent(
                            MainActivity.this,
                            NewDraftActivity.class
                    );
            startActivity(intent);
        });

        btnBulk.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    BulkDeleteActivity.class
            );

            startActivity(intent);
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