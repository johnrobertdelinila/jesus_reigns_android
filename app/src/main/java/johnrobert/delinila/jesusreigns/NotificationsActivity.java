package johnrobert.delinila.jesusreigns;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableReference;
import com.google.firebase.functions.HttpsCallableResult;
import com.marcoscg.dialogsheet.DialogSheet;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import johnrobert.delinila.jesusreigns.objects.Notification;
import johnrobert.delinila.jesusreigns.objects.People;
import johnrobert.delinila.jesusreigns.objects.Song;
import johnrobert.delinila.jesusreigns.viewholders.NotificationViewHolder;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

public class NotificationsActivity extends AppCompatActivity {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = auth.getCurrentUser();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirestoreRecyclerAdapter adapter;

    // Firebase Functions
    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    private HttpsCallableReference deleteAllNotification = mFunctions.getHttpsCallable("deleteAllNotification");
    private HttpsCallableReference showLineUpSong = mFunctions.getHttpsCallable("showLineUpSong");
    private HttpsCallableReference getUserRecord = mFunctions.getHttpsCallable("getUserRecord");

    // Firestore
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private CollectionReference songs = mFirestore.collection("songs");
    private CollectionReference lineup = mFirestore.collection("lineup");

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;

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
        setContentView(R.layout.activity_notifications);
        refreshLayout=findViewById(R.id.refreshLayout);
        recyclerView = findViewById(R.id.recyclerView);

        refreshLayout.setRefreshing(true);

        refreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> refreshLayout.setRefreshing(false), 1000));

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this, VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        if (mUser != null) {
            CollectionReference notifications = firestore.collection("users").document(mUser.getUid()).collection("notifications");
            Query searchQuery = notifications.orderBy("timestamp", Query.Direction.DESCENDING);

            SnapshotParser<Notification> snapshotParser = snapshot -> {
                Notification notification = snapshot.toObject(Notification.class);
                Boolean isResolved = (Boolean) snapshot.get("isResolved");
                Boolean isSeen = (Boolean) snapshot.get("isSeen");
                notification.setId(snapshot.getId());
                notification.setResolved(isResolved);
                notification.setSeen(isSeen);
                return notification;
            };

            FirestoreRecyclerOptions<Notification> options = new FirestoreRecyclerOptions.Builder<Notification>()
                    .setQuery(searchQuery, snapshotParser)
                    .build();

            adapter = new FirestoreRecyclerAdapter<Notification, NotificationViewHolder>(options) {
                @NonNull
                @Override
                public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(NotificationsActivity.this)
                            .inflate(R.layout.item_notification, parent, false);

                    return new NotificationViewHolder(view);
                }

                @Override
                public void onError(@NonNull FirebaseFirestoreException e) {
                    if (e.getMessage() != null) {
                        Log.e("adapter", e.getMessage());
                    }
                    super.onError(e);
                }

                @Override
                public void onDataChanged() {
                    if (getItemCount() <= 0) {
                        findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.default_item).setVisibility(View.GONE);
                    }
                    refreshLayout.setRefreshing(false);
                    super.onDataChanged();
                }

                @Override
                protected void onBindViewHolder(@NonNull NotificationViewHolder holder, int position, @NonNull Notification notification) {
                    Glide.with(NotificationsActivity.this)
                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                            .load(notification.getUser_metadata().get("image"))
                            .into(holder.image);

                    holder.title.setText(notification.getTitle());
                    holder.body.setText(notification.getContent());

                    if(notification.getCollection().equalsIgnoreCase("liked your post")){
                        holder.type_image.setImageResource(R.drawable.ic_favorite_red_24dp);
                    }else if(notification.getCollection().equalsIgnoreCase("commented on your post")){
                        holder.type_image.setImageResource(R.drawable.ic_comment_blue);
                    }else if(notification.getCollection().equalsIgnoreCase("notifications")){
                        holder.type_image.setImageResource(R.drawable.ic_person_add_yellow_24dp);
                        if (notification.getSeen() != null && notification.getSeen()) {
                            holder.type_image.setColorFilter(getResources().getColor(R.color.seenColor));
                        }else {
                            holder.type_image.setColorFilter(getResources().getColor(R.color.unSeenLineup));
                        }
                    }else if(notification.getCollection().equalsIgnoreCase("accepted your friend request")){
                        holder.type_image.setImageResource(R.drawable.ic_person_green_24dp);
                    }

                    holder.itemView.setOnClickListener(v -> {
                        if (!refreshLayout.isRefreshing()) {
                            showLineUpSong(notification.getDoc_id(), notification.getUser_metadata().get("uid"), notification.getResolved(), notification.getId());
                        }
                    });

                }
            };
            adapter.startListening();
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onPause() {
        if (adapter != null) {
            adapter.stopListening();
        }
        super.onPause();
    }

    @Override
    protected void onStart() {
        if (adapter != null) {
            adapter.startListening();
        }
        super.onStart();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    public void back(View view) {
        onBackPressed();
    }

    public void clearAll(View view) {
        new DialogSheet(this)
                .setTitle("Clear all")
                .setMessage("Are you sure do you want to clear all notifications?")
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .setCancelable(true)
                .setPositiveButton("Yes", v -> deleteAll())
                .setNegativeButton("No", v -> {})
                .show();
    }

    private void deleteAll() {
        if (mUser != null) {
            refreshLayout.setRefreshing(true);
            Map<String, Object> data = new HashMap<>();
            data.put("uid", mUser.getUid());
            deleteAllNotification.call(data)
                    .addOnSuccessListener(httpsCallableResult -> {
                        Toasty.success(NotificationsActivity.this, "Cleared all notifications", Toasty.LENGTH_SHORT, true).show();
                        refreshLayout.setRefreshing(false);
                    })
                    .addOnFailureListener(e -> {
                        Toasty.error(NotificationsActivity.this, e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                        refreshLayout.setRefreshing(false);
                    });
        }
    }

    private void showLineUpSong(String docId, String creatorUid, Boolean isResolved, String notificationId) {
        refreshLayout.setRefreshing(true);
        Map<String, Object> data = new HashMap<>();
        data.put("uid", mUser.getUid());
        data.put("docId", docId);

        showLineUpSong.call(data)
                .addOnCompleteListener(task -> {
                    refreshLayout.setRefreshing(false);
                    if (task.getException() != null) {
                        Toasty.error(NotificationsActivity.this, task.getException().getMessage(), Toasty.LENGTH_SHORT, true).show();
                        return;
                    }

                    HttpsCallableResult result = task.getResult();
                    if (result == null) {
                        Toasty.error(NotificationsActivity.this, "Result is null", Toasty.LENGTH_SHORT, true).show();
                        return;
                    }

                    Map<String, Object> updateSeen = new HashMap<>();
                    updateSeen.put("isSeen", true);
                    firestore.collection("users").document(mUser.getUid()).collection("notifications").document(notificationId)
                            .update(updateSeen);

                    Map<String, Object> output = (Map<String, Object>) result.getData();

                    Map<String, Map<String, String>> lineup_songs = (Map<String, Map<String, String>>) output.get("lineup_songs");
                    List<Map<String, String>> peopleList0 = (List<Map<String, String>>) output.get("peopleList");
                    List<People> peopleList = new ArrayList<>();
                    String userResponse = (String) output.get("userResponse");
                    for (Map<String, String> people: peopleList0) {
                        People people1 = new People(people.get("displayName"), people.get("uid"), people.get("ministry"), people.get("photoURL"));
                        peopleList.add(people1);
                    }

                    showSummary(true, lineup_songs, peopleList, docId, creatorUid, isResolved, userResponse);
                });
    }

    private void showSummary(boolean isHaveEvent, Map<String, Map<String, String>> lineup_songs, List<People> peopleList,
                             String docId, String creatorUid, Boolean isResolved, String response) {
        View dialogView = View.inflate(NotificationsActivity.this, R.layout.layout_lineup_review, null);
        DialogSheet dialogSheet = new DialogSheet(this);

        dialogSheet
                .setView(dialogView)
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .setCancelable(true);

        if (response == null || response.equals("decline")) {
            dialogSheet
                .setNegativeButton("DECLINE", v -> response("decline", docId))
                .setPositiveButton("ACCEPT", v -> response("accept", docId))
                .setNeutralButton("MAYBE", v -> {});
        }

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
        TextView heading = dialogView.findViewById(R.id.heading_label);
        TextView msg = dialogView.findViewById(R.id.invited_msg);
        TextView event = dialogView.findViewById(R.id.event_button);

        ExpandableLayout expandableLayout = dialogView.findViewById(R.id.expandable_layout);
        event.setOnClickListener(v -> expandableLayout.toggle(true));

        FloatingActionButton fab = dialogView.findViewById(R.id.message_fab);

        heading.setText("Invitation");
        msg.setVisibility(View.VISIBLE);

        if (response != null) {
            msg.setText(String.format("You %s this lineup invitation", response));
            if (response.equals("accept")) {
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(v -> {
                    Intent intent = new Intent(NotificationsActivity.this, ChatActivity.class);
                    intent.putExtra("peopleList", (Serializable) peopleList);
                    intent.putExtra("lineupId", docId);
                    startActivity(intent);
                });
            }
        }

        initViewLineup("Prelude", prelude, 0, lineup_songs, peopleList);
        initViewLineup("Fellowshipping", fellowshipping, 0, lineup_songs, peopleList);
        initViewLineup("Fast Song", fast_song, 0, lineup_songs, peopleList);
        initViewLineup("Slow Song", slow_song, 0, lineup_songs, peopleList);
        initViewLineup("Communion Song", communion_song, 0, lineup_songs, peopleList);

        if (isHaveEvent) {
            container_event.setVisibility(View.VISIBLE);
            initViewEvent("activity", activityy, 0, lineup_songs, peopleList);
            initViewEvent("rehearsal", rehearsal, 0, lineup_songs, peopleList);
            for (People people: peopleList) {
                if (!people.getUid().equals(creatorUid)) {
                    populateChipGroup(people.getDisplayName(), people.getUid(), person, -1, peopleList);
                }
            }
        }
    }

    private void response(String strResponse, String docId) {

        new MaterialDialog.Builder(NotificationsActivity.this)
                .title("Response")
                .content("Are you sure do you want to " + strResponse + " this lineup invitation?")
                .positiveText("Yes")
                .onPositive((dialog, which) -> {
                    if (strResponse.equals("accept")) {
                        sendResponse(strResponse, docId);
                    }else {
                        // TODO: Show the reason UI for declining
                    }
                    Toasty.success(NotificationsActivity.this, "Your response to the invitation has been sent.", Toasty.LENGTH_LONG, true).show();
                }).negativeText("NOT SURE")
                .onNegative((dialog, which) -> dialog.dismiss()).show();
    }

    private void initViewEvent(String type, ChipGroup chipGroup, int drawable, Map<String, Map<String, String>> lineup_songs, List<People> peopleList) {
        if (lineup_songs.get(type) != null && lineup_songs.get(type).size() > 1) {
            Map<String, String> value = lineup_songs.get(type);
            String date = value.get("date");
            String time = value.get("time");
            populateChipGroup(date, null, chipGroup, R.drawable.ic_calendar_today_24px, peopleList);
            populateChipGroup(time, null, chipGroup, R.drawable.ic_access_time_24px, peopleList);
        }
    }

    private void initViewLineup(String type, ChipGroup chipGroup, int drawable, Map<String, Map<String, String>> lineup_songs, List<People> peopleList) {
        if (lineup_songs.get(type) != null) {
            Map<String, String> value = lineup_songs.get(type);
            String id = value.get("id");
            String title = value.get("title");
            String[] titles = title.split(", ");
            String[] ids = id.split(Pattern.quote(MainActivity.splitter));
            if (ids.length > 1) {
                for (int i = 0; i < titles.length; i++) {
                    String title_e = titles[i];
                    String id_e = ids[i];
                    populateChipGroup(title_e, id_e, chipGroup, drawable, peopleList);
                }
            }else {
                populateChipGroup(title, id, chipGroup, drawable, peopleList);
            }
        }
    }

    private void populateChipGroup(String title, String id, ChipGroup chipGroup, int drawable, List<People> peopleList) {
        if (title.trim().equalsIgnoreCase("select date") || title.trim().equalsIgnoreCase("select time")) {
            return;
        }
        View vv = LayoutInflater.from(NotificationsActivity.this).inflate(R.layout.layout_chip, null);
        Chip chip = vv.findViewById(R.id.chip);
        if (id.equals(mUser.getUid())) {
            chip.setText(" You ");
            chip.setTypeface(null, Typeface.BOLD_ITALIC);
        }else {
            chip.setText(title);
        }
        chip.setTag(id);
        chip.setOnCloseIconClickListener(v1 -> chipGroup.removeView(vv));
        chip.setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(NotificationsActivity.this);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage(getString(R.string.one_moment));
            if (drawable == 0) {
                progressDialog.show();
                songs.document(id).get()
                        .addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if (task.getException() != null) {
                                Toasty.error(NotificationsActivity.this, task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                                return;
                            }
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot == null) {
                                Toasty.info(NotificationsActivity.this, "Sorry, this song is not available anymore", Toasty.LENGTH_LONG, true).show();
                                return;
                            }
                            Song song = documentSnapshot.toObject(Song.class);
                            song.setId(documentSnapshot.getId());
                            showSong(song);
                        });
            }else {
                // TODO: Show people details
            }
        });

        chip.setCloseIconVisible(false);
        chip.setCheckedIconVisible(false);
        if (drawable == 0) {
            chip.setChipIconVisible(false);
            chip.setChipBackgroundColorResource(R.color.chipColor);
            chip.setTextAppearance(R.style.TextAppearance_MaterialComponents_Body1);
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setElevation(2);
            Typeface typeface = ResourcesCompat.getFont(NotificationsActivity.this, R.font.nunito_semibold);
            chip.setTypeface(typeface);
        }else if (drawable == -1) {
            chip.setTextAppearance(R.style.TextAppearance_MaterialComponents_Body1);
            Glide.with(this)
                    .load(findPeople(id, peopleList))
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

    private String findPeople(String uid, List<People> peopleList) {
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

    private void showSong(Song model) {
        View dialogView = View.inflate(NotificationsActivity.this, R.layout.layout_details_song, null);
        LinearLayout linearLayout = dialogView.findViewById(R.id.linearLayout);
        LinearLayout linearLayout2 = dialogView.findViewById(R.id.linearLayout2);
        dialogView.findViewById(R.id.edit).setVisibility(View.GONE);
        dialogView.findViewById(R.id.delete).setVisibility(View.GONE);
        if (model.getUid() != null && mUser != null && !model.getUid().equalsIgnoreCase(mUser.getUid())) {
            TextView name = dialogView.findViewById(R.id.name);
            CircleImageView profilePic = dialogView.findViewById(R.id.profile_pic);
            Map<String, Object> data = new HashMap<>();
            data.put("uid", model.getUid());
            getUserRecord.call(data)
                    .addOnCompleteListener(task -> {
                        if (task.getException() != null) {
                            Log.e("Error", task.getException().getMessage());
                            return;
                        }
                        HttpsCallableResult result = task.getResult();
                        if (result == null) {
                            return;
                        }
                        Map<String, String> userRecord = (Map<String, String>) result.getData();
                        if (userRecord != null) {
                            String displayName = userRecord.get("displayName");
                            String photoURL = userRecord.get("photoURL");
                            if (displayName != null) {
                                name.setText(displayName);
                            }
                            if (photoURL != null) {
                                Glide.with(getBaseContext())
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(photoURL)
                                        .into(profilePic);
                            }
                        }
                    });
        }else {
            linearLayout2.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);
        }

        DialogSheet dialogSheet = new DialogSheet(NotificationsActivity.this)
                .setView(dialogView)
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .setCancelable(true);
        dialogSheet.show();

        TextView artist, tempo, key, title, note;
        WebView webView = dialogView.findViewById(R.id.webView);
        CardView cardView = dialogView.findViewById(R.id.card_note);
        artist = dialogView.findViewById(R.id.artist);
        tempo = dialogView.findViewById(R.id.tempo);
        key = dialogView.findViewById(R.id.key);
        title = dialogView.findViewById(R.id.title);
        note = dialogView.findViewById(R.id.text_note);

        if (model.getLink() != null && model.getLink().length() > 0 && getVideoId(model.getLink()) != null) {
            webView.setVisibility(View.VISIBLE);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setAppCacheEnabled(true);
            webView.setWebChromeClient(new WebChromeClient());
            String videoId = getVideoId(model.getLink());
            String url = "<iframe class=\"youtube-player\" width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/"+ videoId +"\" frameborder=\"0\" allowfullscreen></iframe>";
            webView.loadData(url, "text/html", "utf-8");
        }

        title.setText(model.getTitle());
        artist.setText(model.getArtist());
        if (model.getSpeed() != null) {
            tempo.setText(" " + model.getSpeed() + " ");
            if (model.getSpeed().equalsIgnoreCase("FAST")) {
                tempo.setBackground(getResources().getDrawable(R.drawable.gradient_1));
            }else {
                tempo.setBackground(getResources().getDrawable(R.drawable.gradient_2));
            }
        }
        key.setText("Key: " + model.getNote());
        if (model.getPersonal_note() != null && model.getPersonal_note().trim().length() > 0) {
            cardView.setVisibility(View.VISIBLE);
            note.setText(model.getPersonal_note());
        }
    }

    private String getVideoId(String url) {
        String[] sepa_url = url.split("=");
        if (sepa_url.length > 1) {
            return sepa_url[1].trim();
        }else {
            String[] sepa_url2 = url.split(".be/");
            if (sepa_url2.length > 1) {
                return sepa_url2[1].trim();
            }else {
                return null;
            }
        }
    }

    private void declineInvitation(String strReason, String docId, String strResponse) {
        Map<String, Object> reason = new HashMap<>();
        Map<String, String> userResponse = new HashMap<>();
        userResponse.put(mUser.getUid(), strReason);
        reason.put("reason", reason);
        lineup.document(docId).set(reason, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.getException() != null) {
                        Toasty.error(NotificationsActivity.this, task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                        return;
                    }
                    sendResponse(strResponse, docId);
                });
    }

    private void sendResponse(String strResponse, String docId) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> userResponse = new HashMap<>();
        userResponse.put(mUser.getUid(), strResponse);
        response.put("response", userResponse);
        lineup.document(docId).set(response, SetOptions.merge());
    }


}
