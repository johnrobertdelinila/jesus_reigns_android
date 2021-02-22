package johnrobert.delinila.jesusreigns;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class MinistryLowActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText question;
    String ministry;
    private ProgressDialog mDialog;
    String question_intent;
    private FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

    public void back(View view) {
        onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ministry_low);


        Spinner spinner = findViewById(R.id.spinner);
        question=findViewById(R.id.question);

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,R.layout.spinner_item,getResources().getStringArray(R.array.ministries));
        arrayAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        spinner.setAdapter(arrayAdapter);

        if(!TextUtils.isEmpty(question_intent)){
            question.setText(question_intent);
        }else{
            question.setText("");
        }

        spinner.setOnItemSelectedListener(this);

        mDialog=new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
    }

    public void goMain(View view) {
        if(mCurrentUser!=null) {

            if (TextUtils.isEmpty(ministry)) {
                Snackbar.make(findViewById(R.id.layout), "Select a subject", Snackbar.LENGTH_SHORT).show();
                return;
            }

            new DialogSheet(this)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure that \""+ ministry +"\" is your Ministry?")
                    .setPositiveButton("Yes", v -> {

                        mDialog.show();
                        Map<String, Object> data = new HashMap<>();
                        data.put("ministry", ministry);
                        data.put("bio", "May you be Happy, may you be Healthy, may you be at Peace.");
                        mFunctions.getHttpsCallable("setCustomClaims").call(data)
                                .addOnCompleteListener(task -> {
                                    mDialog.dismiss();
                                    if (task.getException() != null) {
                                        Toasty.error(MinistryLowActivity.this, task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                                        return;
                                    }
                                    MainActivity.startActivity(MinistryLowActivity.this, true);
                                    finish();
                                });

                    })
                    .setNegativeButton("No", v -> {

                    })
                    .setRoundedCorners(true)
                    .setColoredNavigationBar(true)
                    .show();

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ministry = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Snackbar.make(findViewById(R.id.layout),"Select a subject",Snackbar.LENGTH_SHORT).show();
    }
}
