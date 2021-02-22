package johnrobert.delinila.jesusreigns.fragments;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableReference;
import com.google.firebase.functions.HttpsCallableResult;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import johnrobert.delinila.jesusreigns.LoginActivity;
import johnrobert.delinila.jesusreigns.ProfileActivity;
import johnrobert.delinila.jesusreigns.R;
import johnrobert.delinila.jesusreigns.objects.Song;
import johnrobert.delinila.jesusreigns.viewholders.SongsViewHolder;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongFragment extends Fragment {


    public SongFragment() {
        // Required empty public constructor
    }

    private RecyclerView recyclerView;
    private EditText searchField;
    private ImageButton searchBtn;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();

    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private CollectionReference songs = mFirestore.collection("songs");
    private FirestoreRecyclerAdapter adapter;

    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    private HttpsCallableReference getUserRecord = mFunctions.getHttpsCallable("getUserRecord");

    private class MyChrome extends WebChromeClient {

        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyChrome() {}

        public Bitmap getDefaultVideoPoster()
        {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView()
        {
            ((FrameLayout)getActivity().getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getActivity().getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            getActivity().setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
        {
            if (this.mCustomView != null)
            {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getActivity().getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)getActivity().getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getActivity().getWindow().getDecorView().setSystemUiVisibility(3846);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song, container, false);

        searchField = view.findViewById(R.id.search_field);
        searchBtn = view.findViewById(R.id.search_btn);

        recyclerView = view.findViewById(R.id.result_list);
        recyclerView.setHasFixedSize(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), VERTICAL, false));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        searchBtn.setOnClickListener(view1 -> {
            String searchText = searchField.getText().toString();
            songSearch(searchText);
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
                songSearch(searchText);
            }
        });

        songSearch(null);

        return view;
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

    private void songSearch(String songTitle) {
        if (adapter != null) {
            adapter.stopListening();
        }
        Query searchQuery = songs.orderBy("title", Query.Direction.ASCENDING);
        if (songTitle != null && songTitle.length() > 0) {
            searchQuery = songs.whereGreaterThanOrEqualTo("title", songTitle).whereLessThan("title", getEndCode(songTitle));
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
                View view = LayoutInflater.from(getActivity())
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
                holder.itemView.setOnClickListener(v -> {
                    showSong(model);
                });
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void showSong(Song model) {
        View dialogView = View.inflate(getActivity(), R.layout.layout_details_song, null);
        LinearLayout linearLayout = dialogView.findViewById(R.id.linearLayout);
        LinearLayout linearLayout2 = dialogView.findViewById(R.id.linearLayout2);
        Button edit = dialogView.findViewById(R.id.edit);
        Button delete = dialogView.findViewById(R.id.delete);
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
                            if (photoURL != null && getActivity() != null) {
                                Glide.with(getActivity().getBaseContext())
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

        DialogSheet dialogSheet = new DialogSheet(getActivity())
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
        }
        key.setText("Key: " + model.getNote());
        if (model.getPersonal_note() != null && model.getPersonal_note().trim().length() > 0) {
            cardView.setVisibility(View.VISIBLE);
            note.setText(model.getPersonal_note());
        }

        edit.setOnClickListener(vv -> {
            dialogSheet.dismiss();
            editSong(model);
        });

        delete.setOnClickListener(vv -> new MaterialDialog.Builder(getActivity())
                .title("Delete song")
                .content("Are you sure do you want to delete the song "+model.getTitle()+"?")
                .positiveText("Yes")
                .onPositive((dialog, which) -> {
                    dialogSheet.dismiss();
                    songs.document(model.getId()).delete()
                            .addOnCompleteListener(task -> {
                                if (task.getException() != null) {
                                    Toasty.error(getActivity(), task.getException().getMessage(), Toasty.LENGTH_SHORT, true).show();
                                    return;
                                }
                                Toasty.success(getActivity(), "You have deleted the song successfully", Toasty.LENGTH_SHORT, true).show();
                            });
                }).negativeText("No")
                .onNegative((dialog, which) -> dialog.dismiss()).show());
    }
    
    private void editSong(Song editSong) {
        View dialogView = View.inflate(getActivity(), R.layout.layout_new_song, null);
        DialogSheet dialogSheet = new DialogSheet(getActivity())
                .setTitle("Edit Song")
                .setView(dialogView)
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .setCancelable(true);
        dialogSheet.show();

        String[] COUNTRIES = new String[] {"Fast", "Slow"};
        String[] KEYS = new String[] {"Ab", "A", "A#", "Bb" ,"B", "C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", "G", "G#"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        getActivity(),
                        R.layout.dropdown_menu_popup_item,
                        COUNTRIES);

        ArrayAdapter<String> adapter2 =
                new ArrayAdapter<>(
                        getActivity(),
                        R.layout.dropdown_menu_popup_item,
                        KEYS);

        AutoCompleteTextView speed, note;
        speed = dialogView.findViewById(R.id.filled_exposed_dropdown);
        note = dialogView.findViewById(R.id.filled_exposed_dropdown2);
        speed.setAdapter(adapter);
        note.setAdapter(adapter2);

        Button submit = dialogView.findViewById(R.id.submit);
        TextInputEditText title, artist, personal_note, yt_link;
        TextInputLayout title_layout, speed_layout, note_layout;

        title = dialogView.findViewById(R.id.title);
        artist = dialogView.findViewById(R.id.artist);
        personal_note = dialogView.findViewById(R.id.personal_note);
        yt_link = dialogView.findViewById(R.id.yt_link);
        title_layout = dialogView.findViewById(R.id.layout_title);
        speed_layout = dialogView.findViewById(R.id.layout_speed);
        note_layout = dialogView.findViewById(R.id.layout_note);

        title.setText(editSong.getTitle());
        artist.setText(editSong.getArtist());
        personal_note.setText(editSong.getPersonal_note());
        yt_link.setText(editSong.getLink());
        speed.setText(editSong.getSpeed());
        note.setText(editSong.getNote());
        submit.setText("Update");
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
            new MaterialDialog.Builder(getActivity())
                    .title("Confirm")
                    .content("Are you sure do you want to update the song " + editSong.getTitle() + "?")
                    .positiveText("Yes")
                    .onPositive((dialog, which) -> {

                        Song song = new Song();
                        song.setTitle(title.getText().toString());
                        song.setArtist(artist.getText().toString());
                        song.setSpeed(speed.getText().toString());
                        song.setNote(note.getText().toString());
                        song.setPersonal_note(personal_note.getText().toString());
                        song.setLink(yt_link.getText().toString());
                        song.setUid(mUser.getUid());
                        song.setDisplayName(mUser.getDisplayName());

                        songs.document(editSong.getId()).set(song).addOnCompleteListener(task -> {
                            if (task.getException() != null) {
                                Toasty.error(getActivity(), task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                                return;
                            }
                            Toasty.success(getActivity(), "Song updated successfully", Toasty.LENGTH_SHORT, true).show();
                        });
                        dialogSheet.dismiss();
                    }).negativeText("No")
                    .onNegative((dialog, which) -> dialog.dismiss()).show();
        });
    }

    private boolean isEmpty(Editable text) {
        return text == null || text.length() == 0;
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

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null){
            adapter.stopListening();
        }
    }
}
