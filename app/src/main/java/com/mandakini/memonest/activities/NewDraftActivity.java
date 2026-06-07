package com.mandakini.memonest.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mandakini.memonest.R;
import com.mandakini.memonest.database.DraftDao;
import com.mandakini.memonest.models.Draft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;

public class NewDraftActivity extends AppCompatActivity {

    private EditText edtTitle, edtContent;
    private Button btnSave, btnGallery, btnCamera;
    private ImageView imgPreview;

    private DraftDao draftDao;
    private FirebaseFirestore firestore;

    private String selectedImagePath = "";

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Void> cameraLauncher;

    private static final int CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_draft);

        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        btnSave = findViewById(R.id.btnSave);
        btnGallery = findViewById(R.id.btnGallery);
        btnCamera = findViewById(R.id.btnCamera);
        imgPreview = findViewById(R.id.imgPreview);

        draftDao = new DraftDao(this);
        firestore = FirebaseFirestore.getInstance();

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

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        selectedImagePath = saveBitmapToInternalStorage(bitmap);

                        if (!selectedImagePath.isEmpty()) {
                            imgPreview.setImageURI(Uri.fromFile(new File(selectedImagePath)));
                            imgPreview.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_CODE
                );

            } else {
                cameraLauncher.launch(null);
            }
        });

        btnSave.setOnClickListener(v -> saveDraft());
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
            draft.setId((int) result);
            uploadDraftToFirestore(draft);
        } else {
            Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadDraftToFirestore(Draft draft) {
        Map<String, Object> draftMap = new HashMap<>();
        draftMap.put("title", draft.getTitle());
        draftMap.put("content", draft.getContent());
        draftMap.put("imageUri", draft.getImageUri());
        draftMap.put("createdAt", draft.getCreatedAt());
        draftMap.put("isUploaded", 1);

        final boolean[] completed = {false};

        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(() -> {
            if (!completed[0]) {
                completed[0] = true;

                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Saved Offline")
                        .setMessage("You are currently offline. This draft was saved to SQLite, but it was not uploaded to Firebase.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> finish())
                        .show();
            }
        }, 3000);

        firestore.collection("drafts")
                .add(draftMap)
                .addOnSuccessListener(documentReference -> {
                    if (completed[0]) return;

                    completed[0] = true;
                    draftDao.updateUploadStatus(draft.getId(), 1);                    Toast.makeText(
                            this,
                            "Draft saved and uploaded",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {
                    if (completed[0]) return;

                    completed[0] = true;

                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Saved Offline")
                            .setMessage("Firebase upload failed. This draft was saved to SQLite only.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> finish())
                            .show();
                });
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities =
                cm.getNetworkCapabilities(network);
        if (capabilities == null) return false;
        return capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) &&
                capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                );
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

            if (inputStream != null) {
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                inputStream.close();
            }

            outputStream.flush();
            outputStream.close();

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            Toast.makeText(this, "Image save failed", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    private String saveBitmapToInternalStorage(Bitmap bitmap) {
        try {
            File folder = new File(getFilesDir(), "draft_images");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File imageFile = new File(folder, "camera_" + System.currentTimeMillis() + ".jpg");

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);

            outputStream.flush();
            outputStream.close();

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            Toast.makeText(this, "Camera image save failed", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            cameraLauncher.launch(null);

        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}