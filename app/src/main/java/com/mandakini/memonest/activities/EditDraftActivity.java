package com.mandakini.memonest.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.mandakini.memonest.R;
import com.mandakini.memonest.database.DraftDao;
import com.mandakini.memonest.models.Draft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class EditDraftActivity extends AppCompatActivity {

    private EditText edtEditTitle, edtEditContent;
    private Button btnUpdate, btnChangeImage;
    private ImageView imgPreview;

    private DraftDao draftDao;
    private Draft draft;
    private int draftId;
    private String selectedImagePath = "";

    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_draft);

        edtEditTitle = findViewById(R.id.edtEditTitle);
        edtEditContent = findViewById(R.id.edtEditContent);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        imgPreview = findViewById(R.id.imgPreview);

        draftDao = new DraftDao(this);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImagePath = copyImageToInternalStorage(uri);

                        if (!selectedImagePath.isEmpty()) {
                            imgPreview.setVisibility(View.VISIBLE);
                            imgPreview.setImageURI(Uri.fromFile(new File(selectedImagePath)));
                        }
                    }
                }
        );

        draftId = getIntent().getIntExtra("draft_id", -1);

        if (draftId == -1) {
            Toast.makeText(this, "Draft not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDraft();

        btnChangeImage.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnUpdate.setOnClickListener(v -> updateDraft());
    }

    private void loadDraft() {
        draft = draftDao.getDraftById(draftId);

        if (draft == null) {
            Toast.makeText(this, "Draft not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtEditTitle.setText(draft.getTitle());
        edtEditContent.setText(draft.getContent());

        selectedImagePath = draft.getImageUri();

        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            File imageFile = new File(selectedImagePath);

            if (imageFile.exists()) {
                imgPreview.setVisibility(View.VISIBLE);
                imgPreview.setImageURI(Uri.fromFile(imageFile));
            } else {
                imgPreview.setVisibility(View.GONE);
            }
        } else {
            imgPreview.setVisibility(View.GONE);
        }
    }

    private String copyImageToInternalStorage(Uri uri) {
        try {
            File folder = new File(getFilesDir(), "draft_images");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File imageFile = new File(folder, "draft_" + System.currentTimeMillis() + ".jpg");

            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            Toast.makeText(this, "Image update failed", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    private void updateDraft() {
        String title = edtEditTitle.getText().toString().trim();
        String content = edtEditContent.getText().toString().trim();

        if (title.isEmpty()) {
            edtEditTitle.setError("Title required");
            return;
        }

        if (content.isEmpty()) {
            edtEditContent.setError("Message required");
            return;
        }

        draft.setTitle(title);
        draft.setContent(content);
        draft.setImageUri(selectedImagePath);

        int result = draftDao.updateDraft(draft);

        if (result > 0) {
            Toast.makeText(this, "Draft Updated", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(EditDraftActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
        }
    }
}