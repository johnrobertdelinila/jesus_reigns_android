package johnrobert.delinila.jesusreigns.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class JRMImageView extends ImageView {

    public JRMImageView(Context context) {
        super(context);
    }

    public JRMImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JRMImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getDrawable();
        if (d != null) {
            int w = MeasureSpec.getSize(widthMeasureSpec);
            int h = w * d.getIntrinsicHeight() / d.getIntrinsicWidth();
            setMeasuredDimension(w, h);
        }
        else super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}