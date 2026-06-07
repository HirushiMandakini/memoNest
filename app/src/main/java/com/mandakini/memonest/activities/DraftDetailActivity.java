package com.mandakini.memonest.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mandakini.memonest.R;
import com.mandakini.memonest.database.DraftDao;
import com.mandakini.memonest.models.Draft;

public class DraftDetailActivity extends AppCompatActivity {

    private TextView txtDetailTitle, txtDetailContent, txtDetailDate;
    private ImageView imgDetail;
    private Button btnEdit, btnDelete;

    private DraftDao draftDao;
    private Draft draft;
    private int draftId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_detail);

        txtDetailTitle = findViewById(R.id.txtDetailTitle);
        txtDetailContent = findViewById(R.id.txtDetailContent);
        txtDetailDate = findViewById(R.id.txtDetailDate);
        imgDetail = findViewById(R.id.imgDetail);

        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        draftDao = new DraftDao(this);

        draftId = getIntent().getIntExtra("draft_id", -1);

        if (draftId == -1) {
            Toast.makeText(this, "Draft not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDraft();

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(DraftDetailActivity.this, EditDraftActivity.class);
            intent.putExtra("draft_id", draftId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (draftId != -1) {
            loadDraft();
        }
    }

    private void loadDraft() {
        draft = draftDao.getDraftById(draftId);

        if (draft == null) {
            Toast.makeText(this, "Draft not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtDetailTitle.setText(draft.getTitle());
        txtDetailContent.setText(draft.getContent());
        txtDetailDate.setText(draft.getCreatedAt());

        if (draft.getImageUri() != null && !draft.getImageUri().isEmpty()) {
            try {
                imgDetail.setVisibility(View.VISIBLE);
                imgDetail.setImageURI(Uri.parse(draft.getImageUri()));
            } catch (Exception e) {
                imgDetail.setVisibility(View.GONE);
                Toast.makeText(this, "Image cannot be loaded", Toast.LENGTH_SHORT).show();
            }
        } else {
            imgDetail.setVisibility(View.GONE);
        }
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Draft")
                .setMessage("Are you sure you want to delete this draft?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = draftDao.deleteDraft(draftId);

                    if (result > 0) {
                        Toast.makeText(this, "Draft Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Delete Failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}