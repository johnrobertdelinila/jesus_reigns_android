package johnrobert.delinila.jesusreigns;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MinistryActivity extends AppCompatActivity implements View.OnLongClickListener {

    private ExtendedFloatingActionButton fab;
    private CardView technical, multimedia, band, vocals, dance;
    private List<CardView> cardViews = new ArrayList<>();
    private String ministry;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

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
        setContentView(R.layout.activity_ministry);
        init();
        fab.hide();

        if (mUser != null) {
            mUser.getIdToken(true)
                    .addOnCompleteListener(task1 -> {
                        GetTokenResult tokenResult = task1.getResult();
                        if (tokenResult != null) {
                            Map<String, Object> claims = tokenResult.getClaims();

                        }
                    });
        }


        for (CardView cardView: cardViews) {
            cardView.setOnLongClickListener(this);
        }

        fab.setOnClickListener(v -> new MaterialDialog.Builder(MinistryActivity.this)
                .title("Confirmation")
                .content("Are you sure you\'re in the Ministry of " + ministry + "?")
                .positiveText("Yes")
                .onPositive((dialog, which) -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("ministry", ministry);
                    data.put("bio", "I am the Son of God");
                    mFunctions.getHttpsCallable("setCustomClaims").call(data)
                            .addOnCompleteListener(task -> {
                                if (task.getException() != null) {
                                    Toasty.error(MinistryActivity.this, task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                                    return;
                                }
                                MainActivity.startActivity(MinistryActivity.this, true);
                                finish();
                            });
                    dialog.dismiss();
                }).negativeText("Cancel")
                .onNegative((dialog, which) -> dialog.dismiss()).show());

        new Handler().postDelayed(() -> Toasty.info(MinistryActivity.this, "TAP and HOLD the ministry you will select", Toasty.LENGTH_SHORT, true).show(), 700);
    }

    public void back(View view) {
        onBackPressed();
    }

    public void elevate(View view) {
    }

    private void init() {
        fab = findViewById(R.id.fab);
        technical = findViewById(R.id.technical);
        multimedia = findViewById(R.id.multimedia);
        band = findViewById(R.id.band);
        vocals = findViewById(R.id.vocals);
        dance = findViewById(R.id.dance);
        cardViews.add(technical); cardViews.add(multimedia); cardViews.add(band); cardViews.add(vocals); cardViews.add(dance);
    }

    @Override
    public boolean onLongClick(View v) {
        CardView clickedCardView = (CardView) v;

        for (CardView cardView: cardViews) {
            if (clickedCardView.getId() != cardView.getId()) {
                cardView.setElevation(0);
            }else {
                clickedCardView.setElevation(12f);
            }
        }
        ministry = ((TextView)((LinearLayout) clickedCardView.getChildAt(0)).getChildAt(1)).getText().toString();
        Toasty.success(MinistryActivity.this, "You selected Ministry of " + ministry, Toasty.LENGTH_SHORT, true).show();
        fab.show();
        return false;
    }
}
