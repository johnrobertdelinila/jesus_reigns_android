package johnrobert.delinila.jesusreigns;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.facebook.login.LoginManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableReference;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.marcoscg.dialogsheet.DialogSheet;
import com.tapadoo.alerter.Alerter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import johnrobert.delinila.jesusreigns.fragments.CalendarFragment;
import johnrobert.delinila.jesusreigns.fragments.DashboardFragment;
import johnrobert.delinila.jesusreigns.fragments.ProfileFragment;
import johnrobert.delinila.jesusreigns.fragments.SongFragment;
import johnrobert.delinila.jesusreigns.objects.LineUp;
import johnrobert.delinila.jesusreigns.objects.People;
import johnrobert.delinila.jesusreigns.objects.Song;
import johnrobert.delinila.jesusreigns.receivers.Config;
import johnrobert.delinila.jesusreigns.viewholders.PeopleAdapter;
import johnrobert.delinila.jesusreigns.viewholders.SongsViewHolder;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener {

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Intent resultIntent;

    private FloatingActionButton fab;
    private BottomAppBar bottomAppBar;
    private BottomNavigationView bottomNavigationView;
    public static MainActivity activity;
    public static Fragment mCurrentFragment = new DashboardFragment();
    private TextView textTitle;
    private CircleImageView profileIcon;
    private ProgressBar progressBar;

    public static final Integer[] gradients = new Integer[]{R.drawable.gradient_1, R.drawable.gradient_2, R.drawable.gradient_6, R.drawable.gradient_4, R.drawable.gradient_5,
            R.drawable.gradient_20, R.drawable.gradient_19, R.drawable.gradient_17, R.drawable.gradient_14, R.drawable.gradient_15,
            R.drawable.gradient_13, R.drawable.gradient_12, R.drawable.gradient_11};
    public static int random;

    // Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    // Firestore
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private CollectionReference songs = mFirestore.collection("songs");
    private CollectionReference lineup = mFirestore.collection("lineup");
    private CollectionReference users = mFirestore.collection("users");

    public static String nickname, bio, address, ministry;

    private ImageView lastClickIcon = null;
    private List<ImageView> lastClickIcons = new ArrayList<>();
    public final static String splitter = "*****";
    private Map<String, Map<String, String>> lineup_songs = new HashMap<>();
    private List<People> peopleList = new ArrayList<>();

    public static void startActivity(Context context) {
        Intent intent=new Intent(context,MainActivity.class);
        context.startActivity(intent);
    }

    public static void startActivity(Context context,boolean validate) {
        Intent intent=new Intent(context,MainActivity.class).putExtra("validate",validate);
        context.startActivity(intent);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            LoginManager.getInstance().logOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }else {
            // Update the custom claims every start
            mAuth.getCurrentUser().getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (task.getException() != null) {
                            return;
                        }
                        GetTokenResult result = task.getResult();
                        if (result != null) {
                            Map<String, Object> claims = result.getClaims();
                            if (claims.get("nickname") != null) {
                                nickname = (String) claims.get("nickname");
                            }
                            if (claims.get("bio") != null) {
                                bio = (String) claims.get("bio");
                            }
                            if (claims.get("address") != null) {
                                address = (String) claims.get("address");
                            }
                            if (claims.get("ministry") != null) {
                                ministry = (String) claims.get("ministry");
                            }else {
                                LoginManager.getInstance().logOut();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                            }
                        }
                        showUserDetails();
                    });


            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.REGISTRATION_COMPLETE));

            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.PUSH_NOTIFICATION));

        }
    }

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
        setContentView(R.layout.activity_main);

        init();
        triggers();

        boolean validate = getIntent().getBooleanExtra("validate",false);
        if (validate) {
            new Handler().postDelayed(() -> {
                if (mUser != null && mUser.getDisplayName() != null) {
                    Alerter.create(MainActivity.this)
                            .setTitle("Hi, " + mUser.getDisplayName())
                            .setText("Thanks for joining " + getString(R.string.app_name) +". We\'re happy and super excited to have you!")
                            .setIcon(R.drawable.alerter_ic_face)
                            .enableSwipeToDismiss()
                            .setDuration(7777)
                            .setIconColorFilter(getResources().getColor(R.color.colorBackground))
                            .setBackgroundColorRes(R.color.colorAccentt)
                            .show();
                }
            }, 1000);
            new Handler().postDelayed(this::askPermission, 5000);
        }else {
            askPermission();
        }

        random = new Random().nextInt(gradients.length);
        showFragment(new DashboardFragment());

        if (mUser != null) {
            sendRegistrationToken();
            firebaseMessagingService();
        }

    }

    private void showUserDetails() {
        if (mUser != null) {
            // User image
            if (mUser.getPhotoUrl() != null) {
                Glide.with(activity.getBaseContext())
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                        .load(mUser.getPhotoUrl())
                        .into(profileIcon);
            }
        }
    }


    public void showFragment(Fragment fragment) {

        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        mCurrentFragment=fragment;
    }

    private void init() {
        fab = findViewById(R.id.fab);
        bottomAppBar = findViewById(R.id.bar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        activity = this;
        textTitle = findViewById(R.id.text_title);
        profileIcon = findViewById(R.id.profile_icon);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        progressBar = findViewById(R.id.progressBar);
    }

    private void transformFab(int id) {

        MenuItem third = bottomNavigationView.getMenu().getItem(2);
        MenuItem fourth = bottomNavigationView.getMenu().getItem(3);
        MenuItem fifth = bottomNavigationView.getMenu().getItem(4);

        if (id == R.id.songbank && bottomAppBar.getFabAlignmentMode() != BottomAppBar.FAB_ALIGNMENT_MODE_END) {

            fourth.setIcon(getResources().getDrawable(R.drawable.ic_baseline_account_circle_24px));
            fourth.setCheckable(true);
            fourth.setTitle(getString(R.string.profile));
            fourth.setChecked(true);

            fifth.setIcon(null);
            fifth.setEnabled(false);
            fifth.setCheckable(false);
            fifth.setTitle(null);

            new Handler().postDelayed(() -> {
                new Handler().postDelayed(() -> {
                    third.setIcon(getResources().getDrawable(R.drawable.ic_calendar_today_24px));
                    third.setEnabled(true);
                }, 125);
                bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);

                fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.searchColor)));
                fab.setImageResource(R.drawable.ic_add_black);
                fab.setColorFilter(getResources().getColor(R.color.colorBackground));
                fab.setOnClickListener(v -> {
                    if (ministry != null && ministry.equalsIgnoreCase("vocals")) {

                        new DialogSheet(this)
                                .setTitle("Action")
                                .setMessage("What do you want to create?")
                                .setRoundedCorners(true)
                                .setColoredNavigationBar(true)
                                .setCancelable(true)
                                .setNeutralButton("CANCEL", vv -> {})
                                .setNegativeButton("NEW SONG", vv -> addNewSong(vv))
                                .setPositiveButton("CREATE LINE UP", vv -> addLineupSong(v))
                                .show();
                    }else {
                        addNewSong(v);
                    }
                });


            }, 100);

        }else if (id != R.id.songbank && bottomAppBar.getFabAlignmentMode() != BottomAppBar.FAB_ALIGNMENT_MODE_CENTER){
            third.setIcon(null);
            third.setEnabled(false);
            third.setCheckable(false);

            fourth.setIcon(getResources().getDrawable(R.drawable.ic_calendar_today_24px));
            fourth.setCheckable(true);
            fourth.setTitle(getString(R.string.calendar));

            fifth.setIcon(getResources().getDrawable(R.drawable.ic_baseline_account_circle_24px));
            fifth.setEnabled(true);
            fifth.setCheckable(true);
            fifth.setTitle(R.string.profile);
            new Handler().postDelayed(() -> {

                bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);

                fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab.setImageResource(R.drawable.ic_outline_center_focus_weak_24px);
                fab.setColorFilter(getResources().getColor(R.color.chipColor));

                fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LiveBarcodeScanningActivity.class)));

            }, 100);
        }

    }

    private void triggers() {
        AtomicReference<String> silentX = new AtomicReference<>();
        silentX.set(getString(R.string.calendar));
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.dashboard:
                    if (!textTitle.getText().toString().equalsIgnoreCase(getString(R.string.dashboard))) {
                        showFragment(new DashboardFragment());
                        mCurrentFragment = new DashboardFragment();
                        textTitle.setText(R.string.dashboard);
                        transformFab(item.getItemId());
                    }

                    return true;
                case R.id.profile:
                    if (!textTitle.getText().toString().equalsIgnoreCase(getString(R.string.profile))) {
                        showFragment(new ProfileFragment());
                        mCurrentFragment = new ProfileFragment();
                        textTitle.setText(R.string.profile);
                        transformFab(item.getItemId());
                    }

                    return true;
                case R.id.songbank:
                    if (!textTitle.getText().toString().equalsIgnoreCase(getString(R.string.songbank))) {
                        silentX.set(getString(R.string.profile));
                        showFragment(new SongFragment());
                        mCurrentFragment = new SongFragment();
                        textTitle.setText(R.string.songbank);
                        transformFab(item.getItemId());
                    }

                    return true;
                case R.id.invisible:
                    silentX.set(getString(R.string.calendar));
                    bottomNavigationView.setSelectedItemId(R.id.calendar);
                    return false;
                case R.id.calendar:

                    if (silentX.get().equalsIgnoreCase(getString(R.string.calendar)) && !textTitle.getText().toString().equalsIgnoreCase(getString(R.string.calendar))) {
                        transformFab(item.getItemId());
                        showFragment(new CalendarFragment());
                        mCurrentFragment = new CalendarFragment();
                        textTitle.setText(R.string.calendar);

                        return true;
                    }else if (silentX.get().equalsIgnoreCase(getString(R.string.profile))){
                        transformFab(item.getItemId());
                        silentX.set(getString(R.string.calendar));
                        bottomNavigationView.setSelectedItemId(R.id.profile);
                        return false;
                    }
            }
            return false;
        });

        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LiveBarcodeScanningActivity.class)));
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ProfileActivity.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // Only for Oreo and newer versions the transition
                // To ensure the smoothness of the animation
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, profileIcon, getString(R.string.my_profile));
                startActivity(intent, activityOptionsCompat.toBundle());
            }else {
                startActivity(intent);
            }
        });
    }

    private void validateMinistry(int id) {
        /*if (*//*ministry != null && (ministry.equalsIgnoreCase("vocals") || ministry.equalsIgnoreCase("band")) && id == R.id.songbank*//*true) {
            addIcon.setVisibility(View.VISIBLE);
        }else {
            addIcon.setVisibility(View.GONE);
        }

        if (ministry != null && (ministry.equalsIgnoreCase("vocals")) && id == R.id.songbank) {
            lineupIcon.setVisibility(View.VISIBLE);
        }else {
            lineupIcon.setVisibility(View.GONE);
        }*/
    }

    private void askPermission() {

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.isAnyPermissionPermanentlyDenied()){
                            Toasty.info(MainActivity.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toasty.LENGTH_LONG,true).show();
                        }else{
                            // TODO: Get the location of the user
                            getUserLocation();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();

    }

    private void getAddress(Location location) {
        Geocoder geocoder=new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;

        try {
            if (location != null) {
                addresses=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                if(addresses.size()>0){
                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();
                }
            }else {
                Log.e("location", "no location");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUserLocation() {

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e("location", "no location");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    getAddress(location);
                }
            }
        };
        LocationRequest locationRequest = new LocationRequest();

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        getAddress(location);
                    }else {
                        fusedLocationClient.requestLocationUpdates(locationRequest,
                                locationCallback,
                                Looper.getMainLooper());
                    }
                });
    }

    public void showNotifications(View view) {
        startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
    }

    public void addNewSong(View view) {
        View dialogView = View.inflate(MainActivity.this, R.layout.layout_new_song, null);
        DialogSheet dialogSheet = new DialogSheet(this)
                .setTitle("Add Song")
                .setRoundedCorners(false)
                .setView(dialogView)
                .setColoredNavigationBar(true)
                .setCancelable(true);
        dialogSheet.show();

        String[] COUNTRIES = new String[] {"Fast", "Slow"};
        String[] KEYS = new String[] {"Ab", "A", "A#", "Bb" ,"B", "C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", "G", "G#"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        MainActivity.this,
                        R.layout.dropdown_menu_popup_item,
                        COUNTRIES);

        ArrayAdapter<String> adapter2 =
                new ArrayAdapter<>(
                        MainActivity.this,
                        R.layout.dropdown_menu_popup_item,
                        KEYS);

        AutoCompleteTextView speed, note;
        speed = dialogView.findViewById(R.id.filled_exposed_dropdown);
        note = dialogView.findViewById(R.id.filled_exposed_dropdown2);
        speed.setAdapter(adapter);
        note.setAdapter(adapter2);

        TextView submit = dialogView.findViewById(R.id.submit);
        TextInputEditText title, artist, personal_note, yt_link;
        TextInputLayout title_layout, speed_layout, note_layout;

        title = dialogView.findViewById(R.id.title);
        artist = dialogView.findViewById(R.id.artist);
        personal_note = dialogView.findViewById(R.id.personal_note);
        yt_link = dialogView.findViewById(R.id.yt_link);
        title_layout = dialogView.findViewById(R.id.layout_title);
        speed_layout = dialogView.findViewById(R.id.layout_speed);
        note_layout = dialogView.findViewById(R.id.layout_note);

        submit.setOnClickListener(v -> {
            if (isEmpty(title.getText())) {
                title_layout.setError("Title must not be empty");
                return;
            }else {
                title_layout.setError(null);
            }
            if (isEmpty(speed.getText())) {
                speed_layout.setError("Tempo must not be empty");
                return;
            }else {
                speed_layout.setError(null);
            }
            if (isEmpty(note.getText())) {
                note_layout.setError("Key must not be empty");
                return;
            }else {
                note_layout.setError(null);
            }
            new MaterialDialog.Builder(MainActivity.this)
                    .title("Confirm")
                    .content("Are you sure do you want to add the song " + title.getText().toString() + "?")
                    .positiveText("Yes")
                    .onPositive((dialog, which) -> {
                        progressBar.setVisibility(View.VISIBLE);
                        Song song = new Song();
                        song.setTitle(title.getText().toString());
                        song.setArtist(artist.getText().toString());
                        song.setSpeed(speed.getText().toString());
                        song.setNote(note.getText().toString());
                        song.setPersonal_note(personal_note.getText().toString());
                        song.setLink(yt_link.getText().toString());
                        song.setUid(mUser.getUid());
                        song.setDisplayName(mUser.getDisplayName());

                        songs.add(song).addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.getException() != null) {
                                Toasty.error(MainActivity.this, task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                                return;
                            }
                            Toasty.success(MainActivity.this, "Song added successfully", Toasty.LENGTH_SHORT, true).show();
                        });
                        dialogSheet.dismiss();
                    }).negativeText("Not Yet")
                    .onNegative((dialog, which) -> dialog.dismiss()).show();
        });
    }

    private boolean isEmpty(Editable text) {
        return text == null || text.length() == 0;
    }

    private void saveLineUpSong() {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Map<String, String>> events = new HashMap<>();
        Map<String, List<String>> songs = new HashMap<>();
        List<String> uids = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> value: lineup_songs.entrySet()) {
            String key = value.getKey();
            Map<String, String> details = value.getValue();
            if (key.equalsIgnoreCase("activity") || key.equalsIgnoreCase("rehearsal")) {
                events.put(key, details);
            }else {
                if (details.get("id") != null && details.get("id").split(Pattern.quote(splitter)).length > 0) {
                    songs.put(key, Arrays.asList(details.get("id").split(Pattern.quote(splitter))));
                }
            }
        }

        for (People people: peopleList) {
            uids.add(people.getUid());
        }
        long tsLong = System.currentTimeMillis()/1000;
        String ts = Long.toString(tsLong);
        LineUp lineUp = new LineUp(events, songs, uids, mUser.getUid(), ts);

        Intent intent = new Intent(MainActivity.this, SendAnimateActivity.class);
        intent.putExtra("message", "You\'ve successfully created Line up songs");
        startActivity(intent);

        lineup.add(lineUp)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.getException() != null) {
                        Toasty.error(activity, task.getException().getMessage(), Toasty.LENGTH_SHORT, true).show();
                        return;
                    }
                });
    }

    private void showSummary(boolean isHaveEvent) {
        View dialogView = View.inflate(MainActivity.this, R.layout.layout_lineup_review, null);
        DialogSheet dialogSheet = new DialogSheet(this);

        dialogSheet
                .setView(dialogView)
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .setPositiveButton("SUBMIT", true, v -> {
                    new MaterialDialog.Builder(MainActivity.this)
                            .title("Confirm")
                            .content("Are you sure do you want to create this lineup?")
                            .positiveText("Yes")
                            .onPositive((dialog, which) -> saveLineUpSong()).negativeText("No")
                            .onNegative((dialog, which) -> dialog.dismiss()).show();
                })
                .setNegativeButton("I FORGOT SOMETHING", v -> addLineupSong(null))
                .setCancelable(false);
        dialogSheet.show();

        LinearLayout container_event, prelude_linear, fellowshipping_linear, fast_linear, slow_linear, communion_linear;
        ChipGroup prelude, fellowshipping, fast_song, slow_song, communion_song, activityy, rehearsal, person;
        container_event = dialogView.findViewById(R.id.container_event);
        prelude = dialogView.findViewById(R.id.chip_group_prelude);
        fellowshipping = dialogView.findViewById(R.id.chip_group_fellowshipping);
        fast_song = dialogView.findViewById(R.id.chip_group_fast);
        slow_song = dialogView.findViewById(R.id.chip_group_slow);
        communion_song = dialogView.findViewById(R.id.chip_group_communion);
        activityy = dialogView.findViewById(R.id.chip_group_activity);
        rehearsal = dialogView.findViewById(R.id.chip_group_rehearsal);
        person = dialogView.findViewById(R.id.chip_group_persons);

        initViewLineup("Prelude", prelude, 0);
        initViewLineup("Fellowshipping", fellowshipping, 0);
        initViewLineup("Fast Song", fast_song, 0);
        initViewLineup("Slow Song", slow_song, 0);
        initViewLineup("Communion Song", communion_song, 0);

        if (isHaveEvent) {
            container_event.setVisibility(View.VISIBLE);
            initViewEvent("activity", activityy, 0);
            initViewEvent("rehearsal", rehearsal, 0);
            for (People people: peopleList) {
                populateChipGroup(people.getDisplayName(), people.getUid(), person, -1);
            }
        }
    }

    private void initViewEvent(String type, ChipGroup chipGroup, int drawable) {
        if (lineup_songs.get(type) != null && lineup_songs.get(type).size() > 1) {
            Map<String, String> value = lineup_songs.get(type);
            String date = value.get("date");
            String time = value.get("time");
            populateChipGroup(date, null, chipGroup, R.drawable.ic_calendar_today_24px);
            populateChipGroup(time, null, chipGroup, R.drawable.ic_access_time_24px);
        }
    }

    private void initViewLineup(String type, ChipGroup chipGroup, int drawable) {
        if (lineup_songs.get(type) != null) {
            Map<String, String> value = lineup_songs.get(type);
            String id = value.get("id");
            String title = value.get("title");
            String[] titles = title.split(", ");
            String[] ids = id.split(Pattern.quote(splitter));
            if (ids.length > 1) {
                for (int i = 0; i < titles.length; i++) {
                    String title_e = titles[i];
                    String id_e = ids[i];
                    populateChipGroup(title_e, id_e, chipGroup, drawable);
                }
            }else {
                populateChipGroup(title, id, chipGroup, drawable);
            }
        }
    }

    private void populateChipGroup(String title, String id, ChipGroup chipGroup, int drawable) {
        if (title.trim().equalsIgnoreCase("select date") || title.trim().equalsIgnoreCase("select time")) {
            return;
        }
        View vv = LayoutInflater.from(activity).inflate(R.layout.layout_chip, null);
        Chip chip = vv.findViewById(R.id.chip);
        chip.setText(title);
        chip.setTag(id);
        chip.setOnCloseIconClickListener(v1 -> {
                /*for (People people: peopleList) {
                    if (people.getUid().equalsIgnoreCase(user.getUid())) {
                        peopleList.remove(people);
                        break;
                    }
                }*/
            chipGroup.removeView(vv);
        });

        chip.setCloseIconVisible(false);
        chip.setCheckedIconVisible(false);
        if (drawable == 0) {
            chip.setChipIconVisible(false);
            chip.setChipBackgroundColorResource(R.color.colorAccentt);
            chip.setTextColor(getResources().getColor(R.color.colorBackground));
        }else if (drawable == -1) {
            Glide.with(this)
                    .load(findPeople(id))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            chip.setChipIcon(resource);
                            return false;
                        }
                    }).preload();
        } else {
            chip.setChipIcon(getDrawable(drawable));
        }
        ((LinearLayout)chipGroup.getParent()).setVisibility(View.VISIBLE);
        chipGroup.addView(chip);
    }

    private String findPeople(String uid) {
        if (peopleList.size() > 0) {
            for (People people: peopleList) {
                if (people.getUid().equalsIgnoreCase(uid)) {
                    return people.getPhotoURL();
                }
            }
            return null;
        }else {
            return null;
        }
    }

    public void addLineupSong(View view) {
        View dialogView = getLayoutInflater().inflate(R.layout.layout_lineup_songs, null);
        TextView prelude_text, fellowshipping_text, fast_text, slow_text, communion_text;
        CardView card_prelude, card_fellowshipping, card_fast, card_slow, card_communion;

        prelude_text = dialogView.findViewById(R.id.text_prelude);
        fellowshipping_text = dialogView.findViewById(R.id.text_fellowshipping);
        fast_text = dialogView.findViewById(R.id.text_fast_song);
        slow_text = dialogView.findViewById(R.id.text_slow_song);
        communion_text = dialogView.findViewById(R.id.text_communion_song);

        card_prelude = dialogView.findViewById(R.id.card_prelude);
        card_fellowshipping = dialogView.findViewById(R.id.card_fellowshipping);
        card_fast = dialogView.findViewById(R.id.card_fast);
        card_slow = dialogView.findViewById(R.id.card_slow);
        card_communion = dialogView.findViewById(R.id.card_communion);

        MaterialButton create_event = dialogView.findViewById(R.id.create_event);
        LinearLayout container = dialogView.findViewById(R.id.container);
        MaterialButton dismiss_event = dialogView.findViewById(R.id.dismiss_event);
        CircleImageView add_person = dialogView.findViewById(R.id.add_person);
        ChipGroup chipGroup = dialogView.findViewById(R.id.chip_group);

        CardView activity_date, activity_time, rehearsal_date, rehearsal_time;
        activity_date = dialogView.findViewById(R.id.select_date_activity);
        activity_time = dialogView.findViewById(R.id.select_time_activity);
        rehearsal_date = dialogView.findViewById(R.id.select_date_rehearsal);
        rehearsal_time = dialogView.findViewById(R.id.select_time_rehearsal);

        new MaterialDialog.Builder(activity)
                .title("Create Lineup Songs")
                .cancelable(false)
                .customView(dialogView, true)
                .positiveText("REVIEW")
                .onPositive((dialog, which) -> {

                    CardView parentFast = ((CardView) fast_text.getParent());
                    CardView parentSlow = ((CardView) slow_text.getParent());

                    if (parentFast.getVisibility() == View.GONE || fast_text.getText() == null) {
                        Toasty.warning(activity, "Fast song must not be empty", Toasty.LENGTH_LONG, true).show();
                        return;
                    }else {
                        Map<String, String> song = new HashMap<>();
                        song.put("title", fast_text.getText().toString());
                        song.put("id", fast_text.getTag().toString());
                        lineup_songs.put("Fast Song", song);
                    }

                    if (parentSlow.getVisibility() == View.GONE || slow_text.getText() == null) {
                        Toasty.warning(activity, "Slow song must not be empty", Toasty.LENGTH_LONG, true).show();
                        return;
                    }else {
                        Map<String, String> song = new HashMap<>();
                        song.put("title", slow_text.getText().toString());
                        song.put("id", slow_text.getTag().toString());
                        lineup_songs.put("Slow Song", song);
                    }

                    CardView parentPrelude = ((CardView) prelude_text.getParent());
                    CardView parentFellowshipping = ((CardView) fellowshipping_text.getParent());
                    CardView parentCommunion = ((CardView) communion_text.getParent());

                    if (parentPrelude.getVisibility() == View.VISIBLE && prelude_text.getText() != null && prelude_text.getText().length() > 0) {
                        Map<String, String> song = new HashMap<>();
                        song.put("title", prelude_text.getText().toString());
                        song.put("id", prelude_text.getTag().toString());
                        lineup_songs.put("Prelude", song);
                    }
                    if (parentFellowshipping.getVisibility() == View.VISIBLE && fellowshipping_text.getText() != null && fellowshipping_text.getText().length() > 0){
                        Map<String, String> song = new HashMap<>();
                        song.put("title", fellowshipping_text.getText().toString());
                        song.put("id", fellowshipping_text.getTag().toString());
                        lineup_songs.put("Fellowshipping", song);
                    }
                    if (parentCommunion.getVisibility() == View.VISIBLE && communion_text.getText() != null && communion_text.getText().length() > 0) {
                        Map<String, String> song = new HashMap<>();
                        song.put("title", communion_text.getText().toString());
                        song.put("id", communion_text.getTag().toString());
                        lineup_songs.put("Communion Song", song);
                    }

                    Map<String, String> input = new HashMap<>();
                    input.put("date", ((TextView) activity_date.getChildAt(0)).getText().toString());
                    input.put("time", ((TextView) activity_time.getChildAt(0)).getText().toString());
                    lineup_songs.put("activity", input);
                    Map<String, String> input2 = new HashMap<>();
                    input2.put("date", ((TextView) rehearsal_date.getChildAt(0)).getText().toString());
                    input2.put("time", ((TextView) rehearsal_time.getChildAt(0)).getText().toString());
                    lineup_songs.put("rehearsal", input2);

                    showSummary(create_event.getVisibility() == View.GONE);

                    if (create_event.getVisibility() == View.GONE) {
                        lineup_songs.put("isHaveEvent", new HashMap<>());
                    }else {
                        lineup_songs.remove("isHaveEvent");
                    }

                }).negativeText("Cancel")
                .onNegative((dialog, which) -> dialog.dismiss()).show();

        create_event.setOnClickListener(v -> {
            if (card_slow.getVisibility() == View.VISIBLE && card_fast.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
            }else {
                Toasty.info(activity, "Add song for Fast and Slow to create an event", Toasty.LENGTH_SHORT, true).show();
            }
        });

        dismiss_event.setOnClickListener(v -> {
            container.setVisibility(View.GONE);
            create_event.setVisibility(View.VISIBLE);
        });

        add_person.setOnClickListener(v -> showPeople(chipGroup));

        rehearsal_date.setOnClickListener(v -> handleDateButton((TextView) ((CardView) v).getChildAt(0)));
        rehearsal_time.setOnClickListener(v -> handleTimeButton((TextView) ((CardView) v).getChildAt(0)));

        activity_date.setOnClickListener(v -> handleDateButton((TextView) ((CardView) v).getChildAt(0)));
        activity_time.setOnClickListener(v -> handleTimeButton((TextView) ((CardView) v).getChildAt(0)));

        if (view == null) {
            for (Map.Entry<String, Map<String, String>> song : lineup_songs.entrySet()) {
                String type = song.getKey();
                Map<String, String> detail_song = song.getValue();
                if (type.equalsIgnoreCase("Prelude")) {
                    prelude_text.setText(detail_song.get("title"));
                    prelude_text.setTag(detail_song.get("id"));
                    ((CardView) prelude_text.getParent()).setVisibility(View.VISIBLE);
                    ((LinearLayout)((CardView) prelude_text.getParent()).getParent()).getChildAt(1).setVisibility(View.GONE);
                }
                if (type.equalsIgnoreCase("Fellowshipping")) {
                    fellowshipping_text.setText(detail_song.get("title"));
                    fellowshipping_text.setTag(detail_song.get("id"));
                    ((CardView) fellowshipping_text.getParent()).setVisibility(View.VISIBLE);
                    ((LinearLayout)((CardView) fellowshipping_text.getParent()).getParent()).getChildAt(1).setVisibility(View.GONE);
                }
                if (type.equalsIgnoreCase("Fast Song")) {
                    fast_text.setText(detail_song.get("title"));
                    fast_text.setTag(detail_song.get("id"));
                    ((CardView) fast_text.getParent()).setVisibility(View.VISIBLE);
                    ((LinearLayout)((CardView) fast_text.getParent()).getParent()).getChildAt(1).setVisibility(View.GONE);
                }
                if (type.equalsIgnoreCase("Slow Song")) {
                    slow_text.setText(detail_song.get("title"));
                    slow_text.setTag(detail_song.get("id"));
                    ((CardView) slow_text.getParent()).setVisibility(View.VISIBLE);
                    ((LinearLayout)((CardView) slow_text.getParent()).getParent()).getChildAt(1).setVisibility(View.GONE);
                }
                if (type.equalsIgnoreCase("Communion Song")) {
                    communion_text.setText(detail_song.get("title"));
                    communion_text.setTag(detail_song.get("id"));
                    ((CardView) communion_text.getParent()).setVisibility(View.VISIBLE);
                    ((LinearLayout)((CardView) communion_text.getParent()).getParent()).getChildAt(1).setVisibility(View.GONE);
                }
                if (type.equalsIgnoreCase("activity")) {
                    ((TextView) activity_date.getChildAt(0)).setText(detail_song.get("date"));
                    ((TextView) activity_time.getChildAt(0)).setText(detail_song.get("time"));
                }
                if (type.equalsIgnoreCase("rehearsal")) {
                    ((TextView) rehearsal_date.getChildAt(0)).setText(detail_song.get("date"));
                    ((TextView) rehearsal_time.getChildAt(0)).setText(detail_song.get("time"));
                }

                chipGroup.removeAllViews();
                for (People user: peopleList) {
                    View vv = LayoutInflater.from(activity).inflate(R.layout.layout_chip, null);
                    Chip chip = vv.findViewById(R.id.chip);
                    chip.setText(user.getDisplayName());
                    chip.setOnCloseIconClickListener(v1 -> {
                        for (People people: peopleList) {
                            if (people.getUid().equalsIgnoreCase(user.getUid())) {
                                peopleList.remove(people);
                                break;
                            }
                        }
                        chipGroup.removeView(vv);
                    });
                    Glide.with(this)
                            .load(user.getPhotoURL())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                    chip.setChipIcon(resource);
                                    return false;
                                }
                            }).preload();
                    chipGroup.addView(chip);
                }

                if (card_slow.getVisibility() == View.VISIBLE && card_fast.getVisibility() == View.VISIBLE) {
                    create_event.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);
                }
            }
        }else {
            lineup_songs.clear();
            peopleList.clear();
        }

        card_prelude.setOnLongClickListener(this);
        card_fellowshipping.setOnLongClickListener(this);
        card_fast.setOnLongClickListener(this);
        card_slow.setOnLongClickListener(this);
        card_communion.setOnLongClickListener(this);

        dialogView.findViewById(R.id.prelude).setOnClickListener(v -> showSongs("Prelude", prelude_text));
        dialogView.findViewById(R.id.fellowshipping).setOnClickListener(v -> showSongs("Fellowshipping", fellowshipping_text));
        dialogView.findViewById(R.id.fast_song).setOnClickListener(v -> showSongs("Fast", fast_text));
        dialogView.findViewById(R.id.slow_song).setOnClickListener(v -> showSongs("Slow", slow_text));
        dialogView.findViewById(R.id.communion_song).setOnClickListener(v -> showSongs("Communion", communion_text));

        card_prelude.setOnClickListener(v -> showSongs("Prelude", prelude_text));
        card_fellowshipping.setOnClickListener(v -> showSongs("Fellowshipping", fellowshipping_text));
        card_fast.setOnClickListener(v -> showSongs("Fast", fast_text));
        card_slow.setOnClickListener(v -> showSongs("Slow", slow_text));
        card_communion.setOnClickListener(v -> showSongs("Communion", communion_text));

    }

    private void handleDateButton(TextView text_date) {
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, date) -> {

            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(Calendar.YEAR, year);
            calendar1.set(Calendar.MONTH, month);
            calendar1.set(Calendar.DATE, date);
            String dateText = DateFormat.format("EEEE, MMM d, yyyy", calendar1).toString();

            text_date.setText(dateText);
        }, YEAR, MONTH, DATE);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();

    }

    private void handleTimeButton(TextView time_text) {
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR);
        int MINUTE = calendar.get(Calendar.MINUTE);
        boolean is24HourFormat = DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, hour, minute) -> {
            Calendar calendar1 = Calendar.getInstance();

            calendar1.set(Calendar.HOUR, hour);
            calendar1.set(Calendar.MINUTE, minute);
            String dateText = DateFormat.format("h:mm a", calendar1).toString();
            time_text.setText(dateText);
        }, HOUR, MINUTE, is24HourFormat);

        timePickerDialog.show();

    }

    private void showPeople(ChipGroup chipGroup) {
        View dialogView = View.inflate(MainActivity.this, R.layout.layout_show_people, null);

        TextView text_title = dialogView.findViewById(R.id.heading_label);
        RecyclerView recyclerView = dialogView.findViewById(R.id.result_list);
        EditText searchField = dialogView.findViewById(R.id.search_field);
        ImageButton searchBtn = dialogView.findViewById(R.id.search_btn);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        text_title.setText("Invite Person");

        DialogSheet dialogSheet = new DialogSheet(this);

        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        HttpsCallableReference listAllUsers = mFunctions.getHttpsCallable("listAllUsers");
        AtomicReference<PeopleAdapter> peopleAdapter = new AtomicReference<>();

        listAllUsers.call()
                .addOnCompleteListener(task -> {
                    if (task.getException() != null) {
                        Toasty.error(activity, task.getException().getMessage(), Toasty.LENGTH_SHORT, true).show();
                        dialogSheet.dismiss();
                        return;
                    }
                    HttpsCallableResult result = task.getResult();
                    if (result == null) {
                        Toasty.info(activity, "No users yet.", Toasty.LENGTH_SHORT, true).show();
                        dialogSheet.dismiss();
                        return;
                    }

                    dialogView.findViewById(R.id.progressBar).setVisibility(View.GONE);

                    List<Object> usersList = (List<Object>) result.getData();
                    List<People> peoples = new ArrayList<>();
                    for (Object userData: usersList) {
                        Map<String, Object> user = (Map<String, Object>) userData;
                        String uid = (String) user.get("uid");

                        if (mUser.getUid().equals(uid)) {
                            continue;
                        }

                        String displayName = (String) user.get("displayName");
                        String photoURL = (String) user.get("photoURL");
                        String ministry = null;
                        Map<String, String> customClaims = (Map<String, String>) user.get("customClaims");
                        if (customClaims != null) {
                            ministry = customClaims.get("ministry");
                        }
                        People people = new People(displayName, uid, ministry, photoURL);
                        peoples.add(people);
                    }

                    peopleAdapter.set(new PeopleAdapter(peoples, activity, peopleList));

                    searchBtn.setOnClickListener(view1 -> {
                        String searchText = searchField.getText().toString();
                        peopleSearch(searchText, peopleAdapter.get(), recyclerView);
                    });

                    searchField.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String searchText = searchField.getText().toString();
                            peopleSearch(searchText, peopleAdapter.get(), recyclerView);
                        }
                    });

                    peopleSearch(null, peopleAdapter.get(), recyclerView);


                });

        dialogSheet
                .setView(dialogView)
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .setCancelable(false)
                .setNegativeButton("CANCEL", true, v -> {})
                .setPositiveButton("INVITE", true, v -> {
                    chipGroup.removeAllViews();
                    peopleList = peopleAdapter.get().getClickPeople();
                    for (People user: peopleList) {
                        View vv = LayoutInflater.from(activity).inflate(R.layout.layout_chip, null);
                        Chip chip = vv.findViewById(R.id.chip);
                        chip.setText(user.getDisplayName());
                        chip.setOnCloseIconClickListener(v1 -> {
                            for (People people: peopleList) {
                                if (people.getUid().equalsIgnoreCase(user.getUid())) {
                                    peopleList.remove(people);
                                    break;
                                }
                            }
                            chipGroup.removeView(vv);
                        });
                        Glide.with(this)
                                .load(user.getPhotoURL())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                        chip.setChipIcon(resource);
                                        return false;
                                    }
                                }).preload();
                        chipGroup.addView(chip);
                    }
                });
        dialogSheet.show();
    }

    private void showSongs(String title, TextView textTitle) {
        View dialogView = View.inflate(MainActivity.this, R.layout.layout_show_songs, null);
        DialogSheet dialogSheet = new DialogSheet(this)
                .setView(dialogView)
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .setPositiveButton("DONE", true, v -> {
                    if (lastClickIcon != null) {
                        String[] value = lastClickIcon.getTransitionName().split(Pattern.quote(splitter));
                        textTitle.setText(value[0]);
                        textTitle.setTag(value[1]);
                        ((CardView) textTitle.getParent()).setVisibility(View.VISIBLE);
                        ((LinearLayout)((CardView) textTitle.getParent()).getParent()).getChildAt(1).setVisibility(View.GONE);
                    }else if (lastClickIcons.size() > 0) {
                        StringBuilder stringBuilder = new StringBuilder();
                        StringBuilder stringBuilder1 = new StringBuilder();
                        int i = 0;
                        for (ImageView lastClickIconn: lastClickIcons) {
                            String[] value = lastClickIconn.getTransitionName().split(Pattern.quote(splitter));
                            stringBuilder.append(value[0]);
                            stringBuilder1.append(value[1]);
                            if (i != lastClickIcons.size() - 1) {
                                stringBuilder.append(", ");
                                stringBuilder1.append(splitter);
                            }
                            i++;
                        }
                        textTitle.setText(stringBuilder.toString());
                        textTitle.setTag(stringBuilder1.toString());
                        ((CardView) textTitle.getParent()).setVisibility(View.VISIBLE);
                        ((LinearLayout)((CardView) textTitle.getParent()).getParent()).getChildAt(1).setVisibility(View.GONE);
                    }
                })
                .setCancelable(true);
        dialogSheet.show();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        CollectionReference songs = mFirestore.collection("songs");
        FirestoreRecyclerAdapter adapter = null;

        TextView text_title = dialogView.findViewById(R.id.heading_label);
        RecyclerView recyclerView = dialogView.findViewById(R.id.result_list);
        EditText searchField = dialogView.findViewById(R.id.search_field);
        ImageButton searchBtn = dialogView.findViewById(R.id.search_btn);

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        text_title.setText("Select " + title + " Song");
        searchBtn.setOnClickListener(view1 -> {
            String searchText = searchField.getText().toString();
            songSearch(searchText, adapter, recyclerView, title);
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = searchField.getText().toString();
                songSearch(searchText, adapter, recyclerView, title);
            }
        });

        songSearch(null, adapter, recyclerView, title);
    }

    private int charCodeAt(String string, int index) {
        return (int) string.charAt(index);
    }

    private String fromCharCode(int... codePoints) {
        StringBuilder builder = new StringBuilder(codePoints.length);
        for (int codePoint : codePoints) {
            builder.append(Character.toChars(codePoint));
        }
        return builder.toString();
    }

    private String getEndCode(String songTitle) {
        int strlength = songTitle.length();
        String strFrontCode = songTitle.substring(0, strlength-1);
        String strEndCode = songTitle.substring(strlength-1, strlength);

        String startcode = songTitle;
        String endcode= strFrontCode + fromCharCode(charCodeAt(strEndCode, 0) + 1);

        return endcode;
    }

    private void peopleSearch(String searchText, PeopleAdapter adapter, RecyclerView recyclerView) {
        recyclerView.setAdapter(adapter);
    }

    private void songSearch(String songTitle, FirestoreRecyclerAdapter adapter, RecyclerView recyclerView, String title) {
        lastClickIcon = null;
        lastClickIcons.clear();
        if (adapter != null) {
            adapter.stopListening();
        }
        Query searchQuery = songs.whereEqualTo("speed", getSpeed(title)).orderBy("title", Query.Direction.ASCENDING);
        if (songTitle != null && songTitle.length() > 0) {
            searchQuery = songs.whereEqualTo("speed", getSpeed(title)).whereGreaterThanOrEqualTo("title", songTitle).whereLessThan("title", getEndCode(songTitle));
        }

        SnapshotParser<Song> snapshotParser = snapshot -> {
            Song song = snapshot.toObject(Song.class);
            song.setId(snapshot.getId());
            return song;
        };

        FirestoreRecyclerOptions<Song> options = new FirestoreRecyclerOptions.Builder<Song>()
                .setQuery(searchQuery, snapshotParser)
                .build();

        adapter = new FirestoreRecyclerAdapter<Song, SongsViewHolder>(options) {
            @NonNull
            @Override
            public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(activity)
                        .inflate(R.layout.item_song, parent, false);

                return new SongsViewHolder(view);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                if (e.getMessage() != null) {
                    Log.e("adapter", e.getMessage());
                }
                super.onError(e);
            }

            @Override
            protected void onBindViewHolder(@NonNull SongsViewHolder holder, int position, @NonNull Song model) {
                holder.setDetails(model.getTitle(), model.getArtist(), null);
                holder.icon_right.setImageDrawable(getDrawable(R.drawable.ic_add_circle_outline_24px));
                holder.icon_right.getLayoutParams().height = 50;
                holder.icon_right.getLayoutParams().width = 50;
                holder.icon_right.setTransitionName(model.getTitle() + splitter + model.getId());
                holder.icon_right.setTag("ADD");
                holder.itemView.setOnClickListener(v -> {
                    if (lastClickIcon != null && (!title.equalsIgnoreCase("Fast") && !title.equalsIgnoreCase("Slow"))) {
                        lastClickIcon.setImageDrawable(getDrawable(R.drawable.ic_add_circle_outline_24px));
                        lastClickIcon.setColorFilter(getResources().getColor(R.color.color_icon_right));
                        lastClickIcon.setTag("ADD");
                        lastClickIcon = null;
                    }
                    if (holder.icon_right.getTag().equals("CHECK")) {
                        holder.icon_right.setImageDrawable(getDrawable(R.drawable.ic_add_circle_outline_24px));
                        holder.icon_right.setColorFilter(getResources().getColor(R.color.color_icon_right));
                        holder.icon_right.setTag("ADD");
                        if ((!title.equalsIgnoreCase("Fast") && !title.equalsIgnoreCase("Slow"))) {
                            lastClickIcon = null;
                        }else {
                            lastClickIcons.remove(holder.icon_right);
                        }
                    }else {
                        holder.icon_right.setImageDrawable(getDrawable(R.drawable.ic_check_white_24dp));
                        holder.icon_right.setColorFilter(getResources().getColor(R.color.green_bottom));
                        holder.icon_right.setTag("CHECK");
                        if ((!title.equalsIgnoreCase("Fast") && !title.equalsIgnoreCase("Slow"))) {
                            lastClickIcon = holder.icon_right;
                        }else {
                            lastClickIcons.add(holder.icon_right);
                        }
                    }
                });
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private String getSpeed(String song) {
        if (song.equalsIgnoreCase("Prelude")) {
            return "Slow";
        }else if (song.equalsIgnoreCase("Fellowshipping")) {
            return "Fast";
        }else if (song.equalsIgnoreCase("Fast")) {
            return "Fast";
        }else if (song.equalsIgnoreCase("Slow")) {
            return "Slow";
        }else if (song.equalsIgnoreCase("Communion Song")) {
            return "Slow";
        }else {
            return "Fast";
        }
    }

    @Override
    public boolean onLongClick(View v) {
        v.setVisibility(View.GONE);
        ((LinearLayout)v.getParent()).getChildAt(1).setVisibility(View.VISIBLE);
        String str = ((TextView)((LinearLayout)v.getParent()).getChildAt(0)).getText().toString().trim();
        String final_str = str.substring(0, str.length() - 1);
        lineup_songs.remove(final_str);
        return false;
    }

    private void sendRegistrationToken() {
        if (mUser != null) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    InstanceIdResult taskResult = task.getResult();
                    if (taskResult == null) {
                        return;
                    }
                    String fcmToken = taskResult.getToken();
                    Map<String, Object> data = new HashMap<>();
                    data.put("fcmToken", fcmToken);
                    users.document(mUser.getUid()).set(data, SetOptions.merge()).addOnFailureListener(e -> {});
                }else if (task.getException() != null) {
                    Log.e("TOKEN", task.getException().getMessage());
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
    }

    private void showAlert(final Intent resultIntent, Intent intent) {

        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        String name = intent.getStringExtra("name");
        String from_image = intent.getStringExtra("from_image");
        String message = intent.getStringExtra("message");
        String from_id = intent.getStringExtra("from_id");
        String timestamp = intent.getStringExtra("timestamp");
        String reply_for = intent.getStringExtra("reply_for");
        String image = intent.getStringExtra("image");
        String reply_image = intent.getStringExtra("reply_image");

        String f_id = intent.getStringExtra("f_id");
        String f_name = intent.getStringExtra("f_name");
        String f_email = intent.getStringExtra("f_email");
        String f_token = intent.getStringExtra("f_token");
        String f_image = intent.getStringExtra("f_image");

        String user_id=intent.getStringExtra("user_id");
        String post_id=intent.getStringExtra("post_id");
        String post_desc=intent.getStringExtra("post_desc");
        String admin_id=intent.getStringExtra("admin_id");

        String channel=intent.getStringExtra("channel");
        String version=intent.getStringExtra("version");
        String improvements=intent.getStringExtra("improvements");
        String link=intent.getStringExtra("link");
        String question_id=intent.getStringExtra("question_id");
        String question_timestamp=intent.getStringExtra("question_timestamp");
        String notification_type=intent.getStringExtra("notification_type");

        String community = intent.getStringExtra("community");

        resultIntent.putExtra("community", community);

        resultIntent.putExtra("title", title);
        resultIntent.putExtra("body", body);
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("from_image", from_image);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("from_id", from_id);
        resultIntent.putExtra("timestamp", timestamp);
        resultIntent.putExtra("reply_for", reply_for);
        resultIntent.putExtra("image", image);
        resultIntent.putExtra("reply_image", reply_image);

        resultIntent.putExtra("f_id", f_id);
        resultIntent.putExtra("f_name", f_name);
        resultIntent.putExtra("f_email", f_email);
        resultIntent.putExtra("f_image", f_image);
        resultIntent.putExtra("f_token", f_token);

        resultIntent.putExtra("user_id", user_id);
        resultIntent.putExtra("post_id", post_id);
        resultIntent.putExtra("post_desc", post_desc);
        resultIntent.putExtra("admin_id", admin_id);

        resultIntent.putExtra("channel", channel);
        resultIntent.putExtra("version", version);
        resultIntent.putExtra("improvements", improvements);
        resultIntent.putExtra("link", link);
        resultIntent.putExtra("question_id", question_id);
        resultIntent.putExtra("question_timestamp", question_timestamp);
        resultIntent.putExtra("notification_type", notification_type);

        Alerter.create(MainActivity.this)
                .setTitle(title)
                .setText(body)
                .enableSwipeToDismiss()
                .setDuration(4000)//4sec
                .enableVibration(true)
                .setBackgroundColorRes(R.color.colorAccentt)
                .setProgressColorRes(R.color.colorPrimaryy)
                .setTitleAppearance(R.style.AlertTextAppearance_Title)
                .setTextAppearance(R.style.AlertTextAppearance_Text)
                .setOnClickListener(view -> startActivity(resultIntent)).show();


    }

    private void firebaseMessagingService() {

        FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("OnBroadcastReceiver", "received");

                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    Log.i("OnBroadcastReceiver", "push_received");

                    String click_action = intent.getStringExtra("click_action");

                    switch (click_action) {
                        case "TARGATLINEUPSONGS":

                            resultIntent = new Intent(MainActivity.this, MainActivity.class);

                            showAlert(resultIntent, intent);

                            break;
                        /*case "com.amsavarthan.hify.TARGETNOTIFICATIONREPLY":

                            resultIntent = new Intent(MainActivity.this, NotificationReplyActivity.class);

                            showAlert(resultIntent, intent);

                            break;
                        case "com.amsavarthan.hify.TARGETNOTIFICATION_IMAGE":

                            resultIntent = new Intent(MainActivity.this, NotificationImage.class);

                            showAlert(resultIntent, intent);


                            break;
                        case "com.amsavarthan.hify.TARGETNOTIFICATIONREPLY_IMAGE":

                            resultIntent = new Intent(MainActivity.this, NotificationImageReply.class);

                            showAlert(resultIntent, intent);

                            break;
                        case "com.amsavarthan.hify.TARGET_FRIENDREQUEST":
                        case "REPORT USER":

                            resultIntent = new Intent(MainActivity.this, FriendProfile.class);

                            showAlert(resultIntent, intent);


                            break;
                        case "com.amsavarthan.hify.TARGET_COMMENT":

                            resultIntent = new Intent(MainActivity.this, CommentsActivity.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "com.amsavarthan.hify.TARGET_UPDATE":

                            resultIntent = new Intent(MainActivity.this, UpdateAvailable.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "com.amsavarthan.hify.TARGET_LIKE":
                        case "REPORT POST":

                            resultIntent = new Intent(MainActivity.this, SinglePostView.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "com.amsavarthan.hify.TARGET_FORUM":

                            resultIntent = new Intent(MainActivity.this, AnswersActivity.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "SHOW COMMUNITY":
                            resultIntent = new Intent(MainActivity.this, ApproveActivity.class);
                            showAlert(resultIntent,intent);
                            break;*/
                        default:

                            resultIntent = null;
                            break;
                    }

                }
            }
        };
    }

}
