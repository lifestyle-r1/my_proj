package com.rohith.lifestyle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class signUpPage extends AppCompatActivity {

    private ProgressDialog gDialog;

    private EditText gName,gEmail,gPassword,gRepassword;
    private Button gRegister;

    private String NAME,EMAIL,PASSWORD,REPASSWORD,UID;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;

    private HashMap<String,String> instanceMap = new HashMap<>();
    private DatabaseReference gUserInstanceRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        gDialog = new ProgressDialog(this);
        gDialog.setTitle("Please wait");
        gDialog.setMessage("Creating Account......");

        gEmail = (EditText)findViewById(R.id.register_email);
        gName = (EditText)findViewById(R.id.register_name);
        gPassword = (EditText)findViewById(R.id.register_password);
        gRepassword = (EditText)findViewById(R.id.register_repassword);
        gRegister = (Button)findViewById(R.id.register_btn);

        gRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gDialog.show();
                NAME = gName.getText().toString();
                EMAIL = gEmail.getText().toString();
                PASSWORD = gPassword.getText().toString();
                REPASSWORD = gRepassword.getText().toString();

                if (TextUtils.isEmpty(NAME) || TextUtils.isEmpty(EMAIL) || TextUtils.isEmpty(PASSWORD) || TextUtils.isEmpty(REPASSWORD))
                {
                    Toast.makeText(signUpPage.this, "Enter Valid Info.", Toast.LENGTH_SHORT).show();
                }
                else if (!PASSWORD.equals(REPASSWORD)){
                    Toast.makeText(signUpPage.this, "Passwords Doesn't Match.", Toast.LENGTH_SHORT).show();
                }else
                {
                    mAuth.createUserWithEmailAndPassword(EMAIL,PASSWORD).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                UID = task.getResult().getUser().getUid();
                                createUserInstance(NAME,EMAIL,UID);
                            }else{
                                gDialog.dismiss();
                                Toast.makeText(signUpPage.this, "Error Occured.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
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
                    Toast.makeText(signUpPage.this, "Account created Successfully.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void toDashboard() {
        Intent toDashboard = new Intent(signUpPage.this,dashboardPage.class);
        startActivity(toDashboard);
        finish();
    }
}