package com.rohith.lifestyle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Objects;

public class loginPage extends AppCompatActivity {

    private ProgressDialog gDialog;

    private EditText gEmail,gPassword;
    private Button gLogin,gGoogle;
    private TextView gRegister;

    private String EMAIL,PASSWORD,UID;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;

    private GoogleSignInClient gGoogleSignInClient;
    private GoogleApiClient gGoogleApiClient;
    private static final int RC_SIGN_IN = 123;
    private HashMap<String,String> instanceMap = new HashMap<>();
    private DatabaseReference gUserInstanceRef;
    private String GNAME,GEMAIL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        gDialog = new ProgressDialog(this);
        gDialog.setTitle("Please wait");
        gDialog.setMessage("Signing in......");

        googleSignInInstance();

        gEmail = (EditText)findViewById(R.id.login_email);
        gPassword = (EditText)findViewById(R.id.login_password);
        gLogin = (Button) findViewById(R.id.login_btn);
        gGoogle = (Button) findViewById(R.id.login_google);
        gRegister = (TextView) findViewById(R.id.login_register);

        gRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRegister = new Intent(loginPage.this,signUpPage.class);
                startActivity(toRegister);
            }
        });

        gLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gDialog.show();
                EMAIL = gEmail.getText().toString();
                PASSWORD = gPassword.getText().toString();

                if (TextUtils.isEmpty(EMAIL) || TextUtils.isEmpty(PASSWORD))
                {
                    Toast.makeText(loginPage.this, "Enter Valid Info.", Toast.LENGTH_SHORT).show();
                }else{
                    mAuth.signInWithEmailAndPassword(EMAIL,PASSWORD).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                gDialog.dismiss();
                                toDashboard();
                            }else{
                                gDialog.dismiss();
                                Toast.makeText(loginPage.this, "Error Occured.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });

        gGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }


    private void googleSignInInstance() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        gGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    private void signIn() {
        Intent signInIntent = gGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {

            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        gDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final String UID = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid();
                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(loginPage.this);
                            if (acct != null) {
                                GNAME = acct.getDisplayName();
                                GEMAIL = acct.getEmail();
                            }
                            createUserInstance(GNAME,GEMAIL,UID);
                        } else {
                            gDialog.dismiss();
                            Toast.makeText(loginPage.this, "Some Error.", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }
    private void createUserInstance(String GNAME, String GEMAIL , final String uid) {
        instanceMap.put("USER_NAME",GNAME);
        instanceMap.put("EMAIL",GEMAIL);
        gUserInstanceRef = mDatabase.getReference().child("USERS");
        gUserInstanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(uid)) {
                    gDialog.dismiss();
                    toDashboard();
                }
                else
                {
                    gDialog.dismiss();
                    gUserInstanceRef.child(uid).setValue(instanceMap);
                    toDashboard();
                    Toast.makeText(loginPage.this, "Account created Successfully.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void toDashboard() {
        Intent toDashboard = new Intent(loginPage.this,dashboardPage.class);
        startActivity(toDashboard);
        finish();
    }

}