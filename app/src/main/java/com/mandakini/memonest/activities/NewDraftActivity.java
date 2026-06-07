//package com.mandakini.memonest.activities;
//
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.mandakini.memonest.R;
//import com.mandakini.memonest.database.DraftDao;
//import com.mandakini.memonest.models.Draft;
//
//public class NewDraftActivity extends AppCompatActivity {
//
//    private EditText edtTitle, edtContent;
//    private Button btnSave, btnGallery;
//    private ImageView imgPreview;
//
//    private DraftDao draftDao;
//    private String selectedImageUri = "";
//
//    private ActivityResultLauncher<String> galleryLauncher;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_new_draft);
//
//        edtTitle = findViewById(R.id.edtTitle);
//        edtContent = findViewById(R.id.edtContent);
//        btnSave = findViewById(R.id.btnSave);
//        btnGallery = findViewById(R.id.btnGallery);
//        imgPreview = findViewById(R.id.imgPreview);
//
//        draftDao = new DraftDao(this);
//
//        galleryLauncher = registerForActivityResult(
//                new ActivityResultContracts.GetContent(),
//                uri -> {
//                    if (uri != null) {
//                        selectedImageUri = uri.toString();
//                        imgPreview.setImageURI(uri);
//                        imgPreview.setVisibility(View.VISIBLE);
//                    }
//                }
//        );
//
//        btnGallery.setOnClickListener(v -> {
//            galleryLauncher.launch("image/*");
//        });
//
//        btnSave.setOnClickListener(v -> saveDraft());
//    }
//
//    private void saveDraft() {
//        String title = edtTitle.getText().toString().trim();
//        String content = edtContent.getText().toString().trim();
//
//        if (title.isEmpty()) {
//            edtTitle.setError("Title required");
//            return;
//        }
//
//        if (content.isEmpty()) {
//            edtContent.setError("Message required");
//            return;
//        }
//
//        Draft draft = new Draft();
//        draft.setTitle(title);
//        draft.setContent(content);
//        draft.setImageUri(selectedImageUri);
//        draft.setCreatedAt(String.valueOf(System.currentTimeMillis()));
//        draft.setIsUploaded(0);
//
//        long result = draftDao.insertDraft(draft);
//
//        if (result > 0) {
//            Toast.makeText(this, "Draft Saved", Toast.LENGTH_SHORT).show();
//            finish();
//        } else {
//            Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
//        }
//    }
//}



package com.mandakini.memonest.activities;

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

public class NewDraftActivity extends AppCompatActivity {

    private EditText edtTitle, edtContent;
    private Button btnSave, btnGallery;
    private ImageView imgPreview;

    private DraftDao draftDao;
    private String selectedImagePath = "";

    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_draft);

        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        btnSave = findViewById(R.id.btnSave);
        btnGallery = findViewById(R.id.btnGallery);
        imgPreview = findViewById(R.id.imgPreview);

        draftDao = new DraftDao(this);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImagePath = copyImageToInternalStorage(uri);

                        if (!selectedImagePath.isEmpty()) {
                            imgPreview.setImageURI(Uri.fromFile(new File(selectedImagePath)));
                            imgPreview.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveDraft());
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
            Toast.makeText(this, "Image save failed", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    private void saveDraft() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if (title.isEmpty()) {
            edtTitle.setError("Title required");
            return;
        }

        if (content.isEmpty()) {
            edtContent.setError("Message required");
            return;
        }

        Draft draft = new Draft();
        draft.setTitle(title);
        draft.setContent(content);
        draft.setImageUri(selectedImagePath);
        draft.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        draft.setIsUploaded(0);

        long result = draftDao.insertDraft(draft);

        if (result > 0) {
            Toast.makeText(this, "Draft Saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
        }
    }
}