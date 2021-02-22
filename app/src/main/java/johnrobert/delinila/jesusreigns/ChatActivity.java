package johnrobert.delinila.jesusreigns;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import johnrobert.delinila.jesusreigns.objects.Message;
import johnrobert.delinila.jesusreigns.objects.Notification;
import johnrobert.delinila.jesusreigns.objects.People;
import johnrobert.delinila.jesusreigns.viewholders.MessageViewHolder;
import johnrobert.delinila.jesusreigns.viewholders.NotificationViewHolder;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

public class ChatActivity extends AppCompatActivity {

    private EditText message;
    private Button send;
    private RecyclerView recyclerView;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = auth.getCurrentUser();

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference lineup = firestore.collection("lineup");
    private FirestoreRecyclerAdapter adapter;

    private List<People> peopleList;
    private String docId;

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
        setContentView(R.layout.activity_chat);

        if (mUser == null) {
            finish();
        }else {
            CircleImageView imageView=findViewById(R.id.currentProfile);
            Glide.with(ChatActivity.this)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                    .load(mUser.getPhotoUrl())
                    .into(imageView);

            Intent i = getIntent();
            peopleList = (List<People>) i.getSerializableExtra("peopleList");
            docId = i.getStringExtra("lineupId");
        }

        CollectionReference message_ref = lineup.document(docId).collection("message");

        message = findViewById(R.id.message);
        send = findViewById(R.id.send);
        recyclerView = findViewById(R.id.recycler_view);
        setUpRecyclerView(message_ref);

        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    send.setEnabled(true);
                }else {
                    send.setEnabled(false);
                }
            }
        });
        message.setOnClickListener(v -> new Handler().postDelayed(() -> recyclerView.smoothScrollToPosition(0), 300));
        send.setOnClickListener(v -> {
            sendMessage(message.getText().toString(), message_ref);
            message.setText("");
        });

        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());
    }

    public void onPreviewImage(String imageUri, String name) {
        Intent intent = new Intent(this, ImagePreviewSave.class)
                .putExtra("url", imageUri)
                .putExtra("uri", "")
                .putExtra("sender_name", name);
        startActivity(intent);
        overridePendingTransitionExit();
    }

    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private void setUpRecyclerView(CollectionReference message_ref) {
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(VERTICAL);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(false);

        SnapshotParser<Message> snapshotParser = snapshot -> {
            Message message = snapshot.toObject(Message.class);
            message.setMsgId(snapshot.getId());
            return message;
        };

        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(message_ref.orderBy("timestamp", Query.Direction.DESCENDING), snapshotParser)
                .build();

        adapter = new FirestoreRecyclerAdapter<Message, MessageViewHolder>(options) {
            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(ChatActivity.this)
                        .inflate(R.layout.item_message, parent, false);

                return new MessageViewHolder(view);
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
                    send.setText("Send");
                }else {
                    findViewById(R.id.default_item).setVisibility(View.GONE);
                    send.setText("Reply");
                }
                super.onDataChanged();
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull Message message) {
                if (message.getUid().equals(mUser.getUid())) {
                    holder.senderLayout.setVisibility(View.GONE);
                    if (message.getMessage() != null) {
                        holder.yourText.setVisibility(View.VISIBLE);
                    }else {
                        holder.yourText.setVisibility(View.GONE);
                    }
                    holder.yourText.setText(message.getMessage());
                    if (message.getImgUrl() != null) {
                        holder.messageImageHolder.setVisibility(View.VISIBLE);
                        if (message.getImgUrl().equals("loading")) {
                            holder.messageImage.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
                        }else {
                            Glide.with(ChatActivity.this)
                                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.placeholder))
                                    .load(message.getImgUrl())
                                    .into(holder.messageImage);
                            holder.messageImageHolder.setOnClickListener(v -> onPreviewImage(message.getImgUrl(), message.getMessage()));
                        }
                    }else {
                        holder.messageImageHolder.setVisibility(View.GONE);
                    }
                }else {
                    holder.yourText.setVisibility(View.GONE);
                    holder.senderLayout.setVisibility(View.VISIBLE);
                    holder.senderText.setText(message.getMessage());
                    Map<String, String> sender = findPeople(message.getUid());
                    holder.senderName.setText(sender.get("displayName"));
                    Glide.with(getBaseContext())
                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                            .load(sender.get("photoURL"))
                            .into(holder.senderPhoto);
                }
            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (/*lastVisiblePosition == +1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))*/true) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void sendMessage(String message, CollectionReference message_ref) {
        Message msg = new Message();
        msg.setMessage(message);
        msg.setUid(mUser.getUid());
        message_ref.add(msg);
    }

    private Map<String, String> findPeople(String uid) {
        if (peopleList.size() > 0) {
            for (People people: peopleList) {
                if (people.getUid().equalsIgnoreCase(uid)) {
                    Map<String, String> peoplee = new HashMap<>();
                    peoplee.put("displayName", people.getDisplayName());
                    peoplee.put("photoURL", people.getPhotoURL());
                    return peoplee;
                }
            }
            return null;
        }else {
            return null;
        }
    }

    public void showNotifications(View view) {
        startActivity(new Intent(ChatActivity.this, NotificationsActivity.class));
    }

    public void SendPicture(View view) {
        Intent intent = new Intent(ChatActivity.this, SendImageActivity.class);
        intent.putExtra("lineupId", docId);
        startActivity(intent);
    }
}
