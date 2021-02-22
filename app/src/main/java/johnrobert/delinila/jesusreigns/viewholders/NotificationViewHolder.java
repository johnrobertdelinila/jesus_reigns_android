package johnrobert.delinila.jesusreigns.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import johnrobert.delinila.jesusreigns.R;

public class NotificationViewHolder extends RecyclerView.ViewHolder {
    public View mView;
    public CircleImageView image;
    public ImageView type_image;
    public TextView title,body;

    public NotificationViewHolder(View itemView) {
        super(itemView);

        mView=itemView;
        image = mView.findViewById(R.id.image);
        type_image = mView.findViewById(R.id.type_image);
        title = mView.findViewById(R.id.title);
        body = mView.findViewById(R.id.body);

    }
}
