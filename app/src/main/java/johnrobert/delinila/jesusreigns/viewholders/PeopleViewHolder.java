package johnrobert.delinila.jesusreigns.viewholders;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import johnrobert.delinila.jesusreigns.R;

public class PeopleViewHolder extends RecyclerView.ViewHolder {

    View mView;
    CheckBox checkBox;
    public PeopleViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
        checkBox = itemView.findViewById(R.id.checkbox);
    }

    public void setDetails(Activity ctx, String userName, String userStatus, String userImage){

        TextView user_name = mView.findViewById(R.id.name_text);
        TextView user_status = mView.findViewById(R.id.status_text);
        ImageView user_image = mView.findViewById(R.id.profile_image);

        user_name.setText(userName);
        user_status.setText(userStatus);

        Glide.with(ctx)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(userImage)
                .into(user_image);
    }

    private String highQuality(String photoPath) {
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
