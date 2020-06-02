package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText uFirstname,uLastname,uEmail,uMobile,uState,uCity,uPassword,uCpassword;
    Button uRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;

AwesomeValidation awesomeValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uFirstname   = findViewById(R.id.firstname);
        uLastname      = findViewById(R.id.lastname);
        uEmail   = findViewById(R.id.email);
        uMobile      = findViewById(R.id.mobile);
        uState= findViewById(R.id.state);
        uCity   = findViewById(R.id.city);
        uPassword     = findViewById(R.id.password);
        uCpassword      = findViewById(R.id.cpassword);
        uRegisterBtn=findViewById(R.id.btn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progress);

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
awesomeValidation =new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(this, R.id.firstname, RegexTemplate.NOT_EMPTY,R.string.invalidename);
        awesomeValidation.addValidation(this, R.id.lastname, RegexTemplate.NOT_EMPTY,R.string.invalidelastname);
        awesomeValidation.addValidation(this, R.id.email, Patterns.EMAIL_ADDRESS,R.string.invalidateemail);
        awesomeValidation.addValidation(this, R.id.mobile, "[5-9]{1}[0-9]{9}$",R.string.invalite_mobile);
        awesomeValidation.addValidation(this, R.id.state, RegexTemplate.NOT_EMPTY,R.string.invalide_state);
        awesomeValidation.addValidation(this, R.id.city, RegexTemplate.NOT_EMPTY,R.string.invalide_city);
        awesomeValidation.addValidation(this, R.id.password, ".{6,}",R.string.invalide_password);
        awesomeValidation.addValidation(this, R.id.cpassword ,R.id.password,R.string.invalide_C_password);

        uRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String FistName = uFirstname.getText().toString().trim();
                final String LastName = uLastname.getText().toString().trim();
                final String Email = uEmail.getText().toString().trim();
                final String Mobile = uMobile.getText().toString().trim();
                final String State = uState.getText().toString().trim();
                final String City = uCity.getText().toString().trim();
                final String Password = uPassword.getText().toString().trim();
                final String ConfirmPassword = uCpassword.getText().toString().trim();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("userdetails",0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username",uFirstname.getText().toString());
                editor.commit();
              /*  if(TextUtils.isEmpty(Email))
                {
                    uEmail.setError("Email is Required ");
                    return;
                }

                if(TextUtils.isEmpty(Password))
                {
                    uPassword.setError("Password is Required.");
                    return;
                }

                if(Password.length() < 6)
                {
                    uPassword.setError("Password Must be >= 6 Characters");
                    return;
                }
    */
                if (awesomeValidation.validate()) {
                    progressBar.setVisibility(View.VISIBLE);

                    // register the user in firebase

                    fAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                // send verification link

                                FirebaseUser fuser = fAuth.getCurrentUser();
                                fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show();

                                    }


                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Email not sent " + e.getMessage());
                                    }
                                });

                                Toast.makeText(MainActivity.this, "User Created Sucessfully", Toast.LENGTH_SHORT).show();

                                userID = fAuth.getCurrentUser().getUid();
                                DocumentReference documentReference = fStore.collection("users").document(userID);
                                Map<String, Object> user = new HashMap<>();
                                user.put("fName", FistName);
                                user.put("email", Email);
                                user.put("lastname", LastName);
                                user.put("Mobile", Mobile);
                                user.put("State", State);
                                user.put("city", City);
                                user.put("password", Password);
                                user.put("cpassword", ConfirmPassword);
                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: user Profile is created for " + userID);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: " + e.toString());
                                    }
                                });
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                if(task.isSuccessful())
                                {
                                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                    startActivity(intent);
                                }

                            } else {
                                Toast.makeText(MainActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }

                        }
                    });
                }
            }
        });





    }
}