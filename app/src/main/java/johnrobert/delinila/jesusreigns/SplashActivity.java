package johnrobert.delinila.jesusreigns;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.multidex.MultiDex;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.Map;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        setContentView(R.layout.activity_splash);

        ImageView logoImage = findViewById(R.id.logo);

        /*findViewById(R.id.appname).animate()
                .alpha(1.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        findViewById(R.id.appname).setVisibility(View.VISIBLE);
                    }
                })
                .start();*/

        new Handler().postDelayed(() -> {
            if(FirebaseAuth.getInstance().getCurrentUser() == null || !isOnline()){
                Intent in = new Intent(SplashActivity.this, LoginActivity.class);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    // Only for Oreo and newer versions the transition
                    // To ensure the smoothness of the animation
                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(SplashActivity.this, logoImage, "splash");
                    startActivity(in, activityOptionsCompat.toBundle());
                    new Handler().postDelayed(this::finishAfterTransition, 3000);
                }else {
                    startActivity(in);
                    finish();
                }
            }else{
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.getIdToken(true)
                        .addOnCompleteListener(task1 -> {
                            if (task1.getException() != null) {
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                finish();
                            }else {
                                GetTokenResult tokenResult = task1.getResult();
                                if (tokenResult != null) {
                                    Map<String, Object> claims = tokenResult.getClaims();
                                    String ministry = (String) claims.get("ministry");
                                    if (ministry == null) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            startActivity(new Intent(SplashActivity.this, MinistryActivity.class));
                                        } else {
                                            startActivity(new Intent(SplashActivity.this, MinistryLowActivity.class));
                                        }
                                        finish();

                                    }else {
                                        MainActivity.startActivity(SplashActivity.this, false);
                                        finish();
                                    }
                                }
                            }
                        });
            }
        }, 1200);

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


}
