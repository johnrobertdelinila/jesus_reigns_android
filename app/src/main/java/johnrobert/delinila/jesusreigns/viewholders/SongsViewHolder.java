package johnrobert.delinila.jesusreigns.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import johnrobert.delinila.jesusreigns.R;

public class SongsViewHolder extends RecyclerView.ViewHolder {

    View mView;
    public ImageView icon_right;
    public SongsViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
        icon_right = itemView.findViewById(R.id.iconRight);
    }

    public void setDetails(String songName, String songArtist, String albumCover){

        TextView song_name = mView.findViewById(R.id.name_text);
        TextView song_artist = mView.findViewById(R.id.status_text);
        ImageView song_album = mView.findViewById(R.id.profile_image);

        song_name.setText(songName);
        song_artist.setText(songArtist);

//        Glide.with(ctx).load(albumCover).into(song_album);
    }
}
