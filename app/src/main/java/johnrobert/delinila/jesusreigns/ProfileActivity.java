package johnrobert.delinila.jesusreigns;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ProfileActivity extends AppCompatActivity {

    private TextView logout, save_details;
    private ImageView backIcon;
    private CircleImageView profileImage;
    private ProfileActivity activity;
    private TextInputEditText name, nickname, email, bio, address;
    private RelativeLayout toolbar;

    // Firebase Auth
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    // Firebase Firestore
    private FirebaseFirestore mFirestore;
    // Firebase Functions
    private FirebaseFunctions mFunctions;
    private HttpsCallableReference setCustomClaims;
    // Firebase Storage
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private StorageReference pictures = mStorage.getReference().child("profile_pictures");

    private String downloadUrl = null;
    private boolean isImageUpdated = false;

    private static final int PICK_IMAGE =100 ;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
        setContentView(R.layout.activity_profile);
        init();
        triggers();
        showUserDetails(mUser);
    }

    private void init() {
        logout = findViewById(R.id.logout);
        mAuth = FirebaseAuth.getInstance();
        backIcon = findViewById(R.id.back);
        profileImage = findViewById(R.id.profile_pic);
        mUser = mAuth.getCurrentUser();
        activity = this;
        name = findViewById(R.id.name);
        nickname = findViewById(R.id.nickname);
        email = findViewById(R.id.email);
        bio = findViewById(R.id.bio);
        address = findViewById(R.id.address);
        mFirestore = FirebaseFirestore.getInstance();
        save_details = findViewById(R.id.update);
        mFunctions = FirebaseFunctions.getInstance();
        setCustomClaims = mFunctions.getHttpsCallable("setCustomClaims");
        toolbar = findViewById(R.id.relativeLayout);
    }

    private void showUserDetails(FirebaseUser mUser) {
        if (mUser != null) {
            if (mUser.getPhotoUrl() != null) {
                // User image
                Glide.with(activity.getBaseContext())
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                        .load(highQuality(mUser.getPhotoUrl()))
                        .into(profileImage);
            }
            if (mUser.getDisplayName() != null) {
                name.setText(mUser.getDisplayName());
            }
            if (mUser.getEmail() != null) {
                email.setText(mUser.getEmail());
            }
            if (MainActivity.nickname != null) {
                nickname.setText(MainActivity.nickname);
            }
            if (MainActivity.bio != null) {
                bio.setText(MainActivity.bio);
            }
            if (MainActivity.address != null) {
                address.setText(MainActivity.address);
            }
        }
    }

    private String highQuality(Uri uri) {
        String photoPath = uri.toString();
        if (!photoPath.trim().equals("")) {
            if (photoPath.startsWith("https://graph.facebook.com")) {
                photoPath = photoPath.concat("?height=150");
            }else if (photoPath.startsWith("https://pbs.twimg.com")) {
                photoPath = photoPath.replace("_normal", "").trim();
            }else if (photoPath.contains("googleusercontent.com")) {
                photoPath = photoPath.replace("s96-c", "s400-c").trim();
            }
        }
        return photoPath;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void triggers() {
        logout.setOnClickListener(v -> new MaterialDialog.Builder(ProfileActivity.this)
                .title("Logout")
                .content("Are you sure do you want to logout?")
                .positiveText("Yes")
                .onPositive((dialog, which) -> {
                    mAuth.signOut();
                    LoginManager.getInstance().logOut();
                    dialog.dismiss();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                }).negativeText("No")
                .onNegative((dialog, which) -> dialog.dismiss()).show());
        backIcon.setOnClickListener(v -> onBackPressed());
        profileImage.setOnLongClickListener(v -> {
            if (mUser.getPhotoUrl() != null) {
                startActivity(new Intent(ProfileActivity.this, ImagePreviewActivity.class)
                        .putExtra("url",highQuality(mUser.getPhotoUrl())));
            }
            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK && data != null){
            Uri tempImageUri = data.getData();
            if (tempImageUri != null) {
                // start crop activity
                UCrop.Options options = new UCrop.Options();
                options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                options.setCompressionQuality(100);
                options.setShowCropGrid(true);

                UCrop.of(tempImageUri, Uri.fromFile(new File(getCacheDir(), "jrm_user_profile_picture.png")))
                        .withAspectRatio(1, 1)
                        .withOptions(options)
                        .start(ProfileActivity.this);
            }
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                Uri imageUri = UCrop.getOutput(data);
                uploadToStorage(mUser.getUid(), imageUri);
                isImageUpdated = true;
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Log.e("Error", "Crop error:" + UCrop.getError(data).getMessage());
            }
        }
    }

    private void uploadToStorage(String uid, Uri imageUri) {
        Toasty.warning(this, getResources().getString(R.string.uploading), Toasty.LENGTH_SHORT, true).show();
        StorageReference user_profile = pictures.child(uid + ".png");
        user_profile.putFile(imageUri).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                user_profile.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        profileImage.setImageURI(imageUri);
                        Toasty.info(ProfileActivity.this, "Profile picture uploaded, click \'Save details\' button to apply changes", Toasty.LENGTH_LONG,true).show();
                        downloadUrl = uri.toString();
                    }).addOnFailureListener(e -> Toasty.error(ProfileActivity.this, "Profile photo updated click Save details to apply but unable to compress: "+e.getLocalizedMessage(), Toasty.LENGTH_LONG,true).show());
            }else{
                Log.e("Error","listen",task.getException());
                Toasty.error(activity, task.getException().getMessage(), Toasty.LENGTH_SHORT, true).show();
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void changeProfilePicture(View view) {
        if(isOnline()){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE);
        }else{
            Toasty.error(ProfileActivity.this, "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
        }
    }

    public void showNotifications(View view) {
        startActivity(new Intent(ProfileActivity.this, NotificationsActivity.class));
    }

    public void saveDetails(View view) {
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            return;
        }
        if (isOnline()) {
            updateDetails();
        }else {
            Toasty.info(ProfileActivity.this, "Please connect to the internet first", Toasty.LENGTH_SHORT, true).show();
        }
    }

    private void updateDetails() {
        Toasty.warning(this, getResources().getString(R.string.updating), Toasty.LENGTH_SHORT, true).show();
        String str_email = email.getText().toString();
        UserProfileChangeRequest buildProfile = buildProfile();
        if ((str_email.length() > 0 && isEmailValid(email.getText()) && mAuth.getCurrentUser().getEmail() != null && !str_email.equals(mAuth.getCurrentUser().getEmail())) && buildProfile != null) {
            mUser.updateEmail(str_email)
                    .addOnCompleteListener(task -> {
                        if (task.getException() != null) {
                            showError(task.getException().getMessage());
                            return;
                        }
                        mUser.updateProfile(buildProfile)
                                .addOnCompleteListener(task2 -> {
                                    Log.e("STATUS", "EMAIL AND UPLOADED");
                                    if (task2.getException() != null) {
                                        showError(task2.getException().getMessage());
                                        return;
                                    }
                                    updateCustomClaims();
                                });
                    });
        }else if (buildProfile != null) {
            mUser.updateProfile(buildProfile)
                    .addOnCompleteListener(task2 -> {
                        Log.e("STATUS", "UPLOADED");
                        if (task2.getException() != null) {
                            showError(task2.getException().getMessage());
                            return;
                        }
                        updateCustomClaims();
                    });
        }else if ((str_email.length() > 0 && isEmailValid(email.getText()) && mAuth.getCurrentUser().getEmail() != null && !str_email.equals(mAuth.getCurrentUser().getEmail()))) {
            mUser.updateEmail(str_email)
                    .addOnCompleteListener(task -> {
                        Log.e("STATUS", "EMAIL UPDATED");
                        if (task.getException() != null) {
                            showError(task.getException().getMessage());
                            return;
                        }
                        updateCustomClaims();
                    });
        }else {
            Log.e("STATUS", "CUSTOM CLAIMS DIRECT");
            updateCustomClaims();
        }
    }

    private void updateCustomClaims() {
        String str_nickname = nickname.getText().toString();
        String str_bio = bio.getText().toString();
        String str_address = address.getText().toString();

        Map<String, Object> data = new HashMap<>();
        data.put("nickname", str_nickname);
        data.put("bio", str_bio);
        data.put("address", str_address);
        data.put("ministry", MainActivity.ministry);
        if (!str_nickname.equals(MainActivity.nickname) || !str_bio.equals(MainActivity.bio) || !str_address.equals(MainActivity.address)) {
            setCustomClaims.call(data)
                    .addOnCompleteListener(task -> {
                        if (task.getException() != null) {
                            showError(task.getException().getMessage());
                            return;
                        }
                        // Finish
                        updateCurrentUser();
                        Toasty.success(activity, "Details updated successfully", Toasty.LENGTH_LONG, true).show();
                    });
        }else {
            // Finish
            updateCurrentUser();
            Toasty.success(activity, "Details updated successfully", Toasty.LENGTH_LONG, true).show();
        }
    }

    private UserProfileChangeRequest buildProfile() {
        String str_name = name.getText().toString();
        UserProfileChangeRequest profileUpdates;
        if ((str_name.length() > 0 && !str_name.equals(mUser.getDisplayName())) && (downloadUrl != null && isImageUpdated)) {
            Log.e("STATUS", "CHANGE NAME AND UPLOAD...");
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(str_name)
                    .setPhotoUri(Uri.parse(downloadUrl))
                    .build();
        }else if (downloadUrl != null && isImageUpdated) {
            Log.e("STATUS", "GONNA UPLOAD...");
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(downloadUrl))
                    .build();
        }else if (str_name.length() > 0 && !str_name.equals(mUser.getDisplayName())) {
            Log.e("STATUS", "CHANGE NAME");
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(str_name)
                    .build();
        }else {
            Log.e("STATUS", "NO UPDATES");
            profileUpdates = null;
        }
        return profileUpdates;
    }

    private void updateCurrentUser() {
        if (mAuth.getCurrentUser() != null) {
            isImageUpdated = false;
            mAuth.getCurrentUser().getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (task.getException() != null) {
                            return;
                        }
                        GetTokenResult result = task.getResult();
                        if (result != null) {
                            Map<String, Object> claims = result.getClaims();
                            MainActivity.nickname = (String) claims.get("nickname");
                            MainActivity.bio = (String) claims.get("bio");
                            MainActivity.address = (String) claims.get("address");
                        }
                    });
        }
    }

    private void showError(String err) {
        Toasty.error(ProfileActivity.this, err, Toasty.LENGTH_LONG, true).show();
    }

    private boolean isEmailValid(Editable text) {
        return text != null && text.length() != 0 && Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }
}
