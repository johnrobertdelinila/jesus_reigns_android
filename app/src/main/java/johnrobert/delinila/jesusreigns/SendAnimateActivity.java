package johnrobert.delinila.jesusreigns;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;

import es.dmoral.toasty.Toasty;

public class SendAnimateActivity extends AppCompatActivity {

    private LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_animate);

        lottieAnimationView=findViewById(R.id.lottie);
//        lottieAnimationView.useHardwareAcceleration(true);

        lottieAnimationView.playAnimation();
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Toasty.success(SendAnimateActivity.this, getIntent().getStringExtra("message"), Toasty.LENGTH_LONG, true).show();
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
