package johnrobert.delinila.jesusreigns;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 2700;
    private final List<String> permissions = new ArrayList<>();

    // Firebase Auth
    private FirebaseAuth mAuth;
    // Google Sign In
    private GoogleSignInClient mGoogleSignInClient;

    private LoginButton loginButton;
    private CallbackManager callbackManager;

    private TextView facebookBtn;

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
        setContentView(R.layout.activity_login);

        permissions.add("email");
        permissions.add("public_profile");
        /*permissions.add("user_location");
        permissions.add("user_age_range");
        permissions.add("user_birthday");*/

        mAuth = FirebaseAuth.getInstance();

        initFacebookLogin();
        initGoogleLogin();

        findViewById(R.id.google).setOnClickListener(v -> signIn());
        findViewById(R.id.facebook).setOnClickListener(v -> loginButton.performClick());

        Typeface poppins_light = Typeface.createFromAsset(getAssets(), "poppins_extralight.ttf");
        ((TextView) findViewById(R.id.ministry)).setTypeface(poppins_light);
        Typeface poppins_semi = Typeface.createFromAsset(getAssets(), "poppins_semibold.ttf");
        ((TextView) findViewById(R.id.one)).setTypeface(poppins_semi);

//        showHashDevKey();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    private void initFacebookLogin() {
        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(permissions);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("Access Token", String.valueOf(loginResult.getAccessToken()));
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toasty.error(LoginActivity.this, error.getMessage(), Toasty.LENGTH_LONG, true).show();
            }
        });

    }

    private void initGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct, ProgressDialog progressDialog) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        AuthResult authResult = task.getResult();
                        if (authResult == null) {
                            progressDialog.dismiss();
                            Toasty.error(LoginActivity.this, "Authentication failed.", Toasty.LENGTH_LONG, true).show();
                            return;
                        }
                        FirebaseUser user = authResult.getUser();
                        if (user == null) {
                            progressDialog.dismiss();
                            Toasty.error(LoginActivity.this, "No user found.", Toasty.LENGTH_LONG, true).show();
                            return;
                        }
                        user.getIdToken(true)
                                .addOnCompleteListener(task1 -> {
                                    GetTokenResult tokenResult = task1.getResult();
                                    if (tokenResult != null) {
                                        progressDialog.dismiss();
                                        Map<String, Object> claims = tokenResult.getClaims();
                                        String ministry = (String) claims.get("ministry");
                                        if (ministry == null) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                startActivity(new Intent(LoginActivity.this, MinistryActivity.class));
                                            } else {
                                                startActivity(new Intent(LoginActivity.this, MinistryLowActivity.class));
                                            }
                                        }else {
                                            MainActivity.startActivity(LoginActivity.this, true);
                                            finish();
                                        }
                                    }else {
                                        progressDialog.dismiss();
                                        Toasty.error(LoginActivity.this, "Token expired. Please login again.", Toasty.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        // If sign in fails, display a message to the user.
                        progressDialog.dismiss();
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            Toasty.error(LoginActivity.this, task.getException().getMessage(), Toasty.LENGTH_LONG).show();
                        }else {
                            Toasty.error(LoginActivity.this, "Authentication Failed.", Toasty.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setTitle("Signing In");
            progressDialog.setMessage(getString(R.string.one_moment));
            progressDialog.show();

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null) {
                    Toasty.error(LoginActivity.this, "Authentication Failed.", Toasty.LENGTH_LONG).show();
                }else {
                    firebaseAuthWithGoogle(account, progressDialog);
                }
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                progressDialog.dismiss();
                if (e.getMessage() != null) {
                    Toasty.error(LoginActivity.this, e.getMessage(), Toasty.LENGTH_LONG).show();
                }else {
                    Toasty.error(LoginActivity.this, "Authentication Failed.", Toasty.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Signing In");
        progressDialog.setMessage(getString(R.string.one_moment));
        progressDialog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        AuthResult authResult = task.getResult();
                        if (authResult == null) {
                            progressDialog.dismiss();
                            Toasty.error(LoginActivity.this, "Authentication failed.", Toasty.LENGTH_LONG, true).show();
                            return;
                        }
                        FirebaseUser user = authResult.getUser();
                        if (user == null) {
                            progressDialog.dismiss();
                            Toasty.error(LoginActivity.this, "No user found.", Toasty.LENGTH_LONG, true).show();
                            return;
                        }
                        user.getIdToken(true)
                                .addOnCompleteListener(task1 -> {
                                    GetTokenResult tokenResult = task1.getResult();
                                    if (tokenResult != null) {
                                        progressDialog.dismiss();
                                        Map<String, Object> claims = tokenResult.getClaims();
                                        String ministry = (String) claims.get("ministry");
                                        if (ministry == null) {
                                            startActivity(new Intent(LoginActivity.this, MinistryActivity.class));
                                        }else {
                                            MainActivity.startActivity(LoginActivity.this, true);
                                            finish();
                                        }
                                    }else {
                                        progressDialog.dismiss();
                                        Toasty.error(LoginActivity.this, "Token expired. Please login again.", Toasty.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        // If sign in fails, display a message to the user.
                        progressDialog.dismiss();
                        Toasty.error(LoginActivity.this, "Authentication Failed.", Toasty.LENGTH_SHORT, true).show();
                    }
                });
    }

    private void showHashDevKey() {
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
                    "johnrobert.delinila.jesusreigns",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException ignored) {

        } catch (NoSuchAlgorithmException ignored) {

        }
    }

    public void showPolicyUrl(View view) {

        WebView wv = new WebView(this);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });
        wv.setWebChromeClient(new WebChromeClient());
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl(getString(R.string.policyUrl));

        new MaterialDialog.Builder(this)
                .title("Privacy Policy")
                .customView(wv, false)
                .positiveText("I AGREE")
                .onPositive((dialog, which) -> dialog.dismiss()).show();
    }

    public void showTermsConditionUrl(View view) {

        WebView wv = new WebView(this);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });
        wv.setWebChromeClient(new WebChromeClient());
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl(getString(R.string.termsUrl));

        new MaterialDialog.Builder(this)
                .title("Terms & conditions")
                .customView(wv, false)
                .positiveText("I AGREE")
                .onPositive((dialog, which) -> dialog.dismiss()).show();
    }
}
