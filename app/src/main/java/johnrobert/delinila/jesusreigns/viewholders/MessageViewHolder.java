package johnrobert.delinila.jesusreigns.viewholders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import johnrobert.delinila.jesusreigns.R;
import johnrobert.delinila.jesusreigns.utilities.JRMImageView;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    public TextView yourText, senderName, senderText;
    public LinearLayout senderLayout;
    public CircleImageView senderPhoto;
    public CardView messageImageHolder;
    public JRMImageView messageImage;

    public MessageViewHolder(View itemView) {
        super(itemView);
        yourText = itemView.findViewById(R.id.replytxt);
        senderLayout = itemView.findViewById(R.id.toolbar);
        senderName = itemView.findViewById(R.id.name);
        senderText = itemView.findViewById(R.id.messagetxt);
        senderPhoto = itemView.findViewById(R.id.circleImageView);
        messageImageHolder = itemView.findViewById(R.id.messageImageHolder);
        messageImage = itemView.findViewById(R.id.messageImage);
    }
}