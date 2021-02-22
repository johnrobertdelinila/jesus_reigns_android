package johnrobert.delinila.jesusreigns.fragments;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import johnrobert.delinila.jesusreigns.ImagePreviewActivity;
import johnrobert.delinila.jesusreigns.MainActivity;
import johnrobert.delinila.jesusreigns.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    private TextView name,nickname,ministry,location,post,friend,bio,created;
    private CircleImageView profile_pic;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profile_pic = view.findViewById(R.id.profile_pic);
        name = view.findViewById(R.id.name);
        nickname = view.findViewById(R.id.username);
        ministry = view.findViewById(R.id.email);
        location = view.findViewById(R.id.location);
        post = view.findViewById(R.id.posts);
        friend = view.findViewById(R.id.friends);
        bio = view.findViewById(R.id.bio);

        activity = getActivity();

        showUserDetails(mUser);

        return view;
    }

    private void showUserDetails(FirebaseUser mUser) {
        if (mUser != null) {
            if (mUser.getPhotoUrl() != null) {
                // User image
                Glide.with(activity.getBaseContext())
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                        .load(highQuality(mUser.getPhotoUrl()))
                        .into(profile_pic);

                profile_pic.setOnLongClickListener(v -> {
                    if (mUser.getPhotoUrl() != null) {
                        startActivity(new Intent(getActivity(), ImagePreviewActivity.class)
                                .putExtra("url",highQuality(mUser.getPhotoUrl())));
                    }
                    return false;
                });
            }
            if (mUser.getDisplayName() != null) {
                name.setText(mUser.getDisplayName());
            }
            if (MainActivity.address != null) {
                location.setText(MainActivity.address);
            }
            if (MainActivity.ministry != null) {
//                ministry.setCompoundDrawablesWithIntrinsicBounds(showMinistry(MainActivity.ministry), 0, 0, 0);
                ministry.setText(MainActivity.ministry);
            }
            if (MainActivity.nickname != null) {
                nickname.setText(String.format(Locale.ENGLISH,"@%s", MainActivity.nickname));
            }
            if (MainActivity.bio != null) {
                bio.setText(MainActivity.bio);
            }
        }
    }

    private int showMinistry(String ministry) {
        if (ministry.equalsIgnoreCase("VOCALS")) {
            return R.drawable.ic_sing;
        }else if (ministry.equalsIgnoreCase("BAND")){
            return R.drawable.ic_band;
        }else if (ministry.equalsIgnoreCase("TECHNICAL")) {
            return R.drawable.ic_technical;
        }else if (ministry.equalsIgnoreCase("MULTIMEDIA")) {
            return R.drawable.ic_multimedia;
        }else {
            return R.drawable.ic_dance;
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

}
