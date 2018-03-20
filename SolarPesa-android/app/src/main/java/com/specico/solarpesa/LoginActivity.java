package com.specico.solarpesa;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.specico.solarpesa.Utils.BCrypt;
import com.specico.solarpesa.Utils.Constants;
import com.specico.solarpesa.Utils.LoaderUtility;
import com.specico.solarpesa.Utils.OtpView;
import com.specico.solarpesa.Utils.SharedPrefUtil;
import com.specico.solarpesa.models.User;

import java.util.concurrent.TimeUnit;

import io.saeid.fabloading.LoadingView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnContinue, btnNext, btnResend, btnLogin;
    MaterialEditText etMobile, etPassword;
    OtpView etCode;
    TextView tvResendTimer;
    ImageButton btnBack;
    SignInButton btnGoogle;
    FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String verificationId;
    PhoneAuthProvider.ForceResendingToken mToken;

    String phone;

    private GoogleSignInClient mGoogleSignInClient;
    final int RC_SIGN_IN = 9001;
    GoogleSignInAccount account;

    LoaderUtility loaderUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); //initialize Facebook SDK
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnContinue = findViewById(R.id.btnContinue);
        btnNext = findViewById(R.id.btnNext);
        btnResend = findViewById(R.id.btnResend);
        btnLogin = findViewById(R.id.btnLogin);
        etMobile = findViewById(R.id.etMobile);
        etCode = findViewById(R.id.etCode);
        etPassword = findViewById(R.id.etPassword);
        tvResendTimer = findViewById(R.id.resend_timer);
        btnBack = findViewById(R.id.back_btn);
        btnGoogle = findViewById(R.id.google_login);
        btnGoogle.setSize(SignInButton.SIZE_WIDE);

        initializeMobileEditText();

        loaderUtility = new LoaderUtility((LoadingView)findViewById(R.id.loading_view));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loaderUtility.startLoader();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        initializeVerificationCallback(); //Firebase callback for verification

        btnContinue.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnResend.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnBack.setOnClickListener(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                account = task.getResult(ApiException.class);
                Toast.makeText(this, ""+account.getEmail(), Toast.LENGTH_SHORT).show();
                googleAccountExist(account.getEmail());

            } catch (ApiException e) {
                loaderUtility.stopLoader();
                // Google Sign In failed, update UI appropriately
                Log.w("google", "Google sign in failed", e);
            }
        }
    }

    private void googleAccountExist(final String email)
    {
        final boolean[] exist = new boolean[1];
        Query societyQuery = FirebaseDatabase.getInstance().getReference().child("users").orderByKey();
        societyQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Firebase","onDataChange");
                String storedPwd="";
                User user = new User();
                for (DataSnapshot usersSnap: dataSnapshot.getChildren()) {
                    user = usersSnap.getValue(User.class);
                    assert user != null;
                    Log.d("email",user.getEmail());
                    if(email.equals(user.getEmail())) {
                        exist[0] = true;
                        //Log.d("signIn",""+exist[0]);
                        break;
                    }
                }
                Log.d("signIn",""+exist[0]);
                if(exist[0]) {
                    firebaseAuthWithGoogle(account, user);
                }
                else {
                    Intent intent = new Intent(LoginActivity.this,ProfileActivity.class);
                    intent.putExtra("account",account);
                    intent.putExtra("signInMethod","google");
                    startActivity(intent);
                    loaderUtility.stopLoader();
                    finishAffinity();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Firebase","onCancelled");
                loaderUtility.stopLoader();
            }
        });

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account, final User user) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Firebase", "signInWithCredential-Google:success");
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    String uid = firebaseUser.getUid();
                    //Toast.makeText(LoginActivity.this, "Google Sign In Success", Toast.LENGTH_SHORT).show();
                    SharedPrefUtil.setBooleanPreference(LoginActivity.this, Constants.LOGIN_STATUS,true);
                    SharedPrefUtil.setStringPreference(LoginActivity.this, Constants.LOGIN_UID,uid);
                    user.saveUserToSharedPref(LoginActivity.this);

                    startActivity(new Intent(LoginActivity.this, MainActivity.class).putExtra("signInMethod","google"));
                    loaderUtility.stopLoader();
                    finishAffinity();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Firebase", "signInWithCredential-Google:failure", task.getException());
                    loaderUtility.stopLoader();
//                    Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initializeVerificationCallback() {
        mCallbacks  = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.d("Firebase","onVerificationCompleted");
                etMobile.setVisibility(View.GONE);
                btnContinue.setVisibility(View.GONE);
                etPassword.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.VISIBLE);
                loaderUtility.stopLoader();

                //signIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.d("Firebase",e.getMessage());
                loaderUtility.stopLoader();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                Log.d("Firebase","onCodeSent");
                verificationId=s;
                mToken=forceResendingToken;
                Log.d("Firebase VerificationId",s);

                etMobile.setVisibility(View.GONE);
                btnContinue.setVisibility(View.GONE);
                etCode.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
                findViewById(R.id.resend_box).setVisibility(View.VISIBLE);
                tvResendTimer.setVisibility(View.VISIBLE);
                btnResend.setVisibility(View.GONE);

                new CountDownTimer(60000, 1000) { //60 secs timer with 1 sec tick

                    public void onTick(long millisUntilFinished) {
                        long seconds = millisUntilFinished / 1000;
                        if(seconds<10)
                            tvResendTimer.setText("00:0" + seconds);
                        else
                            tvResendTimer.setText("00:" + seconds);
                    }

                    public void onFinish() {
                        tvResendTimer.setVisibility(View.GONE);
                        btnResend.setVisibility(View.VISIBLE);
                    }

                }.start();

                loaderUtility.stopLoader();
            }
        };
    }

    private void initializeMobileEditText() {
        Selection.setSelection(etMobile.getText(), etMobile.getText().length());

        etMobile.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                {
                    etMobile.setText("+91");
                    Selection.setSelection(etMobile.getText(), etMobile.getText().length());
                }
            }
        });


        etMobile.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().startsWith("+91")){
                    etMobile.setText("+91");
                    Selection.setSelection(etMobile.getText(), etMobile.getText().length());

                }

            }
        });
    }



    private void signIn(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Firebase", "signInWithCredential:success");

                    FirebaseUser user = task.getResult().getUser();


                    if(null == user.getDisplayName() || user.getDisplayName().equals("")) {
                        startActivity(new Intent(LoginActivity.this, ProfileActivity.class).putExtra("signInMethod", "phone"));
                        finishAffinity();
                    }
                    else {
                        etCode.setVisibility(View.GONE);
                        btnNext.setVisibility(View.GONE);
                        findViewById(R.id.resend_box).setVisibility(View.GONE);
                        etPassword.setVisibility(View.VISIBLE);
                        btnLogin.setVisibility(View.VISIBLE);
                    }
                    loaderUtility.stopLoader();

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("Firebase", "signInWithCredential:failure", task.getException());
                    loaderUtility.stopLoader();
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        etCode.simulateDeletePress();
                        etCode.setError("Wrong OTP. Enter correct OTP");
                        etCode.requestFocus();
                    }
                }
            }
        });
    }

    private void sendCode(String phone)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void resendCode(String phone, PhoneAuthProvider.ForceResendingToken token)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,
                token);        // OnVerificationStateChangedCallbacks
    }

    @Override
    public void onClick(View view) {

        switch(view.getId())
        {
            case R.id.btnContinue:
                if(validateMobileNumber())
                {
                    phone=etMobile.getText().toString();
                    sendCode(phone);
                    loaderUtility.startLoader();
                }
                break;
            case R.id.btnNext:
                if (etCode.getOTP().length()!=6) {
                    etCode.setError("Enter 6 digit OTP");
                }
                else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, etCode.getOTP());
                    signIn(credential);
                    loaderUtility.startLoader();
                }
                break;
            case R.id.btnResend:
                resendCode(phone,mToken);
                break;
            case R.id.btnLogin:
                String password = etPassword.getText().toString();
                if(password.length()<6)
                {
                    etPassword.requestFocus();
                    etPassword.setError(getString(R.string.password_minimum_6_length));
                }
                else
                {
                    loaderUtility.startLoader();
                    loginPhone(phone,password);
                }
                break;

            case R.id.back_btn :
                etMobile.setVisibility(View.VISIBLE);
                btnContinue.setVisibility(View.VISIBLE);
                etCode.setVisibility(View.GONE);
                btnNext.setVisibility(View.GONE);
                findViewById(R.id.resend_box).setVisibility(View.GONE);
                break;
        }
    }

    private void loginPhone(final String phone, final String password) {
        Query societyQuery = FirebaseDatabase.getInstance().getReference().child("users").orderByKey();
        societyQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Firebase","onDataChange");
                String storedPwd="", uid="";
                User user = new User();
                for (DataSnapshot usersSnap: dataSnapshot.getChildren()) {
                    user = usersSnap.getValue(User.class);

                    assert user != null;
                    if(phone.equals(user.getPhone()))
                    {
                        storedPwd = user.getPassword();
                        uid = usersSnap.getKey();
                        break;
                    }
                }
                if(storedPwd.equals(""))
                    Log.d("FirebaseLogin",phone+" not matched");
                else {
                    if (checkPassword(password, storedPwd)) {
                        Log.d("FirebaseLogin", phone + " verified");
                        //Toast.makeText(LoginActivity.this, "verified", Toast.LENGTH_SHORT).show();
                        SharedPrefUtil.setBooleanPreference(LoginActivity.this, Constants.LOGIN_STATUS,true);
                        SharedPrefUtil.setStringPreference(LoginActivity.this, Constants.LOGIN_UID,uid);
                        user.saveUserToSharedPref(LoginActivity.this);

                        startActivity(new Intent(LoginActivity.this, MainActivity.class).putExtra("signInMethod","phone"));
                        loaderUtility.stopLoader();
                        finishAffinity();
                    }
                    else
                        Log.d("FirebaseLogin", phone + " not verified");
                    loaderUtility.stopLoader();
                    etPassword.setError(getString(R.string.password_not_correct));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Firebase","onCancelled");
                loaderUtility.stopLoader();
                Toast.makeText(LoginActivity.this, "Server Error, Try Again", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private boolean validateMobileNumber() {
        if (TextUtils.isEmpty(etMobile.getText()) || etMobile.getText().toString().equals("+91")) {
            etMobile.requestFocus();
            etMobile.setError(getString(R.string.this_field_required));
            return false;
        }
        else if(etMobile.getText().length()!=13)
        {
            etMobile.requestFocus();
            etMobile.setError(getString(R.string.ten_digit_mobile_number));
            return false;
        }
        else
            return true;
    }

    public static boolean checkPassword(String password_plaintext, String stored_hash) {
        boolean password_verified = false;

        if(null == stored_hash || !stored_hash.startsWith("$2a$"))
            throw new IllegalArgumentException("Invalid hash provided for comparison");

        password_verified = BCrypt.checkpw(password_plaintext, stored_hash);

        return(password_verified);
    }
}
