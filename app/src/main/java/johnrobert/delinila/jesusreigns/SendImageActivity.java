package johnrobert.delinila.jesusreigns;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;

import es.dmoral.toasty.Toasty;
import johnrobert.delinila.jesusreigns.objects.Message;
import johnrobert.delinila.jesusreigns.utilities.AnimationUtil;

import static johnrobert.delinila.jesusreigns.receivers.Config.random;

public class SendImageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final int PLACE_PICKER_REQUEST =101 ;
    TextView mSend;
    private TextView username;
    private String user_id,current_id;
    private EditText message;
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private Uri imageUri;
    private ImageView imagePreview;
    private String image_;
    private StorageReference storageReference;
    private GeoDataClient mGeoDataClient;
    private Place place;
    private String name;
    private ProgressDialog mDialog;
    private String c_image,c_name;
    private TextView text;
    private String f_name;

    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private CollectionReference lineup = mFirestore.collection("lineup");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);

        imageUri=null;
        mGeoDataClient = Places.getGeoDataClient(this, null);

        imagePreview=findViewById(R.id.imagePreview);
        mSend=findViewById(R.id.send);
        message=findViewById(R.id.message);
        text=findViewById(R.id.text);
        CardView cardView = findViewById(R.id.card);

        imagePreview.setVisibility(View.GONE);
        text.setVisibility(View.VISIBLE);

        imagePreview.setOnLongClickListener(v -> {
            if(imageUri!=null){
                RemoveImage();
            }
            return true;
        });
        String lineupId = getIntent().getStringExtra("lineupId");
        CollectionReference message_ref = lineup.document(lineupId).collection("message");

        mSend.setOnClickListener(view -> {
            String message_ = message.getText().toString().trim().length() > 0 ? message.getText().toString() : null;
            if(imagePreview.getVisibility() == View.VISIBLE && imageUri != null){
                mSend.setEnabled(false);
                String msg_id = message_ref.document().getId();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("message").child(lineupId + MainActivity.splitter + msg_id + ".jpg");
                storageReference.putFile(imageUri)
                        .addOnFailureListener(e -> Toasty.error(SendImageActivity.this, "Image upload failed", Toasty.LENGTH_SHORT, true).show());
                Message msg = new Message();
                msg.setMessage(message_);
                msg.setUid(mUser.getUid());
                msg.setImgUrl("loading");
                message_ref.document(msg_id).set(msg);
                finish();
            }else{
                AnimationUtil.shakeView(cardView, SendImageActivity.this);
            }
        });
    }

    public void back(View view) {
        onBackPressed();
    }

    public void SelectImage(View view) {
        if(imageUri==null) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Pick an image..."), PICK_IMAGE);
        }
    }

    public void PreviewImage(View view) {
        Intent intent=new Intent(SendImageActivity.this, ImagePreviewActivity.class)
                .putExtra("url","")
                .putExtra("uri",imageUri.toString());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(data, this);

                new MaterialDialog.Builder(this)
                        .title("Location")
                        .content("Do you want to add the place's photo?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive((dialog, which) -> {
                            getPhotos(place.getId());
                            dialog.dismiss();
                        }).onNegative((dialog, which) -> dialog.dismiss()).show();

                message.setText("");
                String toastMsg = String.format("Place Name: %s\nAddress: %s\nLatLng: %s,%s"
                        ,place.getName()
                        ,place.getAddress()
                        ,place.getLatLng().latitude
                        ,place.getLatLng().longitude);
                message.setText(toastMsg);
            }
        }

        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                new MaterialDialog.Builder(this)
                        .title("Attachment")
                        .content("Do you want to crop the image?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                imageUri=data.getData();
                                //start crop activity
                                UCrop.Options options = new UCrop.Options();
                                options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                                options.setCompressionQuality(100);
                                options.setShowCropGrid(true);

                                UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), String.format("hify_%s.png", random()))))
                                        .withOptions(options)
                                        .start(SendImageActivity.this);

                                dialog.dismiss();
                            }
                        }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showRemoveButton();
                        imagePreview.setVisibility(View.VISIBLE);
                        text.setVisibility(View.GONE);
                        imageUri=data.getData();
                        Toasty.info(SendImageActivity.this, "You can long press to remove the attachment", Toasty.LENGTH_SHORT,true).show();
                        imagePreview.setImageURI(imageUri);
                        dialog.dismiss();
                    }
                }).show();
            }
        }

        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                showRemoveButton();
                imageUri = UCrop.getOutput(data);
                imagePreview.setVisibility(View.VISIBLE);
                text.setVisibility(View.GONE);
                imagePreview.setImageURI(imageUri);

            } else if (resultCode == UCrop.RESULT_ERROR) {
                Log.e("Error", "Crop error:" + UCrop.getError(data).getMessage());
            }
        }

    }

    private void showRemoveButton() {
        findViewById(R.id.location).setAlpha(1.0f);
        findViewById(R.id.location).animate().alpha(0.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                findViewById(R.id.location).setVisibility(View.GONE);
                findViewById(R.id.remove).setVisibility(View.VISIBLE);
                findViewById(R.id.remove).setAlpha(0.0f);
                findViewById(R.id.remove).animate().alpha(1.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        findViewById(R.id.remove).setVisibility(View.VISIBLE);
                    }
                }).start();
            }
        }).start();
    }

    private void getPhotos(String placeId) {
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(task -> {
            try {
                PlacePhotoMetadataResponse photos = task.getResult();
                PlacePhotoMetadataBuffer photoMetadataBuffer;

                photoMetadataBuffer = photos.getPhotoMetadata();
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                CharSequence attribution = photoMetadata.getAttributions();
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        Bitmap bitmap = photo.getBitmap();
                        imagePreview.setVisibility(View.VISIBLE);
                        text.setVisibility(View.GONE);
                        showRemoveButton();
                        imageUri = getImageUri(SendImageActivity.this, bitmap);
                        imagePreview.setImageURI(getImageUri(SendImageActivity.this, bitmap));
                    }
                });
            }catch (Exception ex){
                Toasty.info(SendImageActivity.this, "No image found for this place.", Toasty.LENGTH_SHORT,true).show();
            }
        });
    }

    public Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title" , null);
        return Uri.parse(path);
    }

    public void removeAttachment(View view) {
        RemoveImage();
    }

    public void RemoveImage() {
        new MaterialDialog.Builder(this)
                .title("Attachment")
                .content("Do you want to remove the attachment?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        imagePreview.setVisibility(View.GONE);
                        text.setVisibility(View.VISIBLE);
                        imageUri=null;
                        hideRemoveButton();
                        dialog.dismiss();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void hideRemoveButton() {
        findViewById(R.id.remove).setAlpha(1.0f);
        findViewById(R.id.remove).animate().alpha(0.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                findViewById(R.id.remove).setVisibility(View.GONE);
                findViewById(R.id.location).setVisibility(View.VISIBLE);
                findViewById(R.id.location).setAlpha(0.0f);
                findViewById(R.id.location).animate().alpha(1.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        findViewById(R.id.location).setVisibility(View.VISIBLE);
                    }
                }).start();
            }
        }).start();
    }

    public void shareLocation(View view) {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
            Toasty.error(this, "Some error occurred.", Toasty.LENGTH_SHORT,true).show();
            Log.e("Error",e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            Toasty.error(this, "Google play services not available", Toasty.LENGTH_SHORT,true).show();
            Log.e("Error",e.getMessage());
        }

    }

}
