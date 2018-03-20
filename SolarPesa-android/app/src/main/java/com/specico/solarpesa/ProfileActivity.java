package com.specico.solarpesa;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.specico.solarpesa.Utils.BCrypt;
import com.specico.solarpesa.Utils.Constants;
import com.specico.solarpesa.Utils.LoaderUtility;
import com.specico.solarpesa.Utils.SharedPrefUtil;
import com.specico.solarpesa.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.saeid.fabloading.LoadingView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    MaterialEditText etPassword, etName, etEmail, etPhone, etAddress;
    Button btnUpload, btnContinue;
    ImageButton btnBack;
    ImageView imgProfile;

    Uri imageUri, downloadUrl;
    String imgRef;

    Spinner spinnerState, spinnerDistrict;
    TextView tvDistrict;

    String societyID;
    HashMap<String,List<String>> stateMap;
    ArrayList<String> societyList;
    List<String> districtList;
    String gender="", state, district;

    ArrayAdapter<String> adapter;

    FirebaseAuth mAuth;
    int SIGN_IN;
    final int GOOGLE_SIGN_IN = 1;
    final int PHONE_SIGN_IN = 0;

    LoaderUtility loaderUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etMobile);
        btnUpload = findViewById(R.id.btnUpload);
        btnContinue = findViewById(R.id.btnContinue);
        btnBack = findViewById(R.id.back_btn);
        imgProfile = findViewById(R.id.imgProfile);
        spinnerState = findViewById(R.id.spinnerState);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        tvDistrict = findViewById(R.id.tvDistrict);
        etAddress = findViewById(R.id.etAddress);

        btnUpload.setOnClickListener(this);
        btnContinue.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        loaderUtility = new LoaderUtility((LoadingView) findViewById(R.id.loading_view));

        mAuth = FirebaseAuth.getInstance();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if(extras.getString("signInMethod") != null) {

                if (extras.getString("signInMethod").equals("google")) {
                    SIGN_IN = GOOGLE_SIGN_IN;
                    etPhone.setVisibility(View.VISIBLE);
                    etPassword.setVisibility(View.GONE);
                    initializeMobileEditText();
                }
                else if (extras.getString("signInMethod").equals("phone")) {
                    SIGN_IN = PHONE_SIGN_IN;
                    etPhone.setVisibility(View.GONE);
                    etPassword.setVisibility(View.VISIBLE);
                }
            }
            if(extras.get("account") != null) {
                GoogleSignInAccount account = (GoogleSignInAccount) extras.get("account");
                assert account != null;
                firebaseAuthWithGoogle(account);
                etName.setText(account.getDisplayName());
                etEmail.setText(account.getEmail());
                downloadUrl = account.getPhotoUrl();
                Glide.with(ProfileActivity.this)
                        .load(downloadUrl)
                        .centerCrop()
                        .into(imgProfile);
                Log.d("image", downloadUrl.toString());
                Toast.makeText(this, "" + account.getEmail(), Toast.LENGTH_SHORT).show();

            }
        }
        districtList = new ArrayList<>();
        stateMap = new HashMap<>();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(adapter);

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                state = adapterView.getItemAtPosition(i).toString();
                if(null!=stateMap.get(state)) {
                    ArrayAdapter<String> adapterD = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_item, stateMap.get(state));
                    adapterD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDistrict.setAdapter(adapterD);
                    tvDistrict.setVisibility(View.VISIBLE);
                    spinnerDistrict.setVisibility(View.VISIBLE);
                }
                TextView tvError = findViewById(R.id.tvStateError);
                tvError.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                district = adapterView.getItemAtPosition(i).toString();
                TextView tvError = findViewById(R.id.tvDistrictError);
                tvError.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        Query stateQuery = FirebaseDatabase.getInstance().getReference().child("state");

        stateQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Firebase","onDataChange");
                districtList.clear();
                stateMap.clear();
                for (DataSnapshot stateSnap: dataSnapshot.getChildren()) {
                    Log.d("key",stateSnap.getKey());
                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                    districtList = stateSnap.getValue(t);
                    assert districtList != null;
                    districtList.add(0,"Select District");
                    stateMap.put(stateSnap.getKey(),districtList);
                    Log.d("district",districtList.toString());
                }
                Log.d("stateMap",stateMap.toString());


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Firebase","onCancelled");
            }
        });
    }

    private void initializeMobileEditText() {
        Selection.setSelection(etPhone.getText(), etPhone.getText().length());

        etPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                {
                    etPhone.setText("+91");
                    Selection.setSelection(etPhone.getText(), etPhone.getText().length());
                }
            }
        });


        etPhone.addTextChangedListener(new TextWatcher() {

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
                    etPhone.setText("+91");
                    Selection.setSelection(etPhone.getText(), etPhone.getText().length());

                }

            }
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Firebase", "signInWithCredential-Google:success");
//                    FirebaseUser user = mAuth.getCurrentUser();
                    //Toast.makeText(ProfileActivity.this, "Google Sign In Success", Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Firebase", "signInWithCredential-Google:failure", task.getException());
//                    Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.btnUpload:
                Intent imageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imageIntent.setType("image/*");
                startActivityForResult(imageIntent, 1);
                break;
            case R.id.btnContinue:

                if(SIGN_IN == PHONE_SIGN_IN)
                    loginPhoneAuth();
                else if(SIGN_IN == GOOGLE_SIGN_IN)
                    loginGoogleAuth();
                break;

            case R.id.back_btn :
                onBackPressed();
                break;
        }
    }

    private void loginGoogleAuth() {
        final String uId, name, email, photo, address, district, state;
        final String phone;
        final User user;
        boolean validation = true;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        uId = firebaseUser.getUid();

        phone = etPhone.getText().toString();
        name = etName.getText().toString();
        email = etEmail.getText().toString();
        address = etAddress.getText().toString();
        district = this.district;
        state = this.state;

        validation = checkValidationGoogleAuth(etPhone,etName,etEmail,etAddress,district,state);
        if(!validation)
            return;

        if(validation) {
            loaderUtility.startLoader();

            if(null==downloadUrl) {
                user = new User(phone, name, email, address,district,state);
            }
            else {
                photo = downloadUrl.toString();
                user = new User(phone, "", name, photo, email, address,district,state);
            }

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseUser", "Profile Updated Success");
                    } else {
                        Log.d("FirebaseUser", "Profile not Updated");
                    }
                }
            });

            FirebaseDatabase.getInstance().getReference().child("users").child(uId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseDatabase", "User details Updated Success");
                        Toast.makeText(ProfileActivity.this, "User details Updated Success", Toast.LENGTH_SHORT).show();

                        SharedPrefUtil.setStringPreference(ProfileActivity.this, Constants.LOGIN_UID, mAuth.getCurrentUser().getUid());
                        SharedPrefUtil.setBooleanPreference(ProfileActivity.this,Constants.LOGIN_STATUS,true);
                        user.saveUserToSharedPref(ProfileActivity.this);

                        startActivity(new Intent(ProfileActivity.this,MainActivity.class));
                        loaderUtility.stopLoader();
                        finishAffinity();

                    }
                    else
                        Log.d("FirebaseDatabase", "User details not Updated");
                    loaderUtility.stopLoader();
                }
            });
        }
    }

    private boolean checkValidationGoogleAuth(MaterialEditText etPhone, MaterialEditText etName, MaterialEditText etEmail, MaterialEditText etAddress, String district, String state) {
        boolean validation = true;
        if(validateMobileNumber(etPhone)) {
            validation = false;
        }
        else if(TextUtils.isEmpty(etName.getText())) {
            etName.requestFocus();
            etName.setError(getString(R.string.name_cannot_be_empty));
            validation = false;
        }
        else if(TextUtils.isEmpty(etEmail.getText()) || !isEmailValid(etEmail.getText())) {
            etEmail.requestFocus();
            etEmail.setError(getString(R.string.not_valid_email));
            validation = false;
        }
        else if(TextUtils.isEmpty(etAddress.getText())) {
            etAddress.requestFocus();
            etAddress.setError(getString(R.string.address_cannot_be_empty));
            validation = false;
        }
        else if(state.equals("") || state == "Select State") {
            TextView tvError = findViewById(R.id.tvStateError);
            tvError.setVisibility(View.VISIBLE);
            validation = false;
        }
        else if(district.equals("") || district == "Select District") {
            TextView tvError = findViewById(R.id.tvDistrictError);
            tvError.setVisibility(View.VISIBLE);
            validation = false;
        }
        else validation = true;

        return validation;
    }

    private boolean validateMobileNumber(MaterialEditText etPhone) {
        if (TextUtils.isEmpty(etPhone.getText()) || etPhone.getText().toString().equals("+91")) {
            etPhone.requestFocus();
            etPhone.setError(getString(R.string.this_field_required));
            return true;
        }
        else if(etPhone.getText().length()!=13)
        {
            etPhone.requestFocus();
            etPhone.setError(getString(R.string.ten_digit_mobile_number));
            return true;
        }
        else
            return false;
    }

    private void loginPhoneAuth() {
        final String uId, password, name, email, photo, address, state, district;
        final String phone;
        final User user;
        boolean validation = true;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        phone = firebaseUser.getPhoneNumber();
        Log.d("user", phone+"");

        uId = firebaseUser.getUid();

        password = etPassword.getText().toString();
        name = etName.getText().toString();
        email = etEmail.getText().toString();
        address = etAddress.getText().toString();
        state = this.state;
        district = this.district;

        String hashedPwd = hashPassword(password, 12);

        validation = checkValidationPhoneAuth(etPassword,etName,etEmail,etAddress,district,state);
        if(!validation)
            return;

        if(validation) {
            loaderUtility.startLoader();

            if(null==downloadUrl) {
                user = new User(phone, hashedPwd, name, email, address, district, state);
            }
            else {
                photo = downloadUrl.toString();
                user = new User(phone, hashedPwd, name, photo, email, address, district, state);
            }

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseUser", "Profile Updated Success");
                    } else {
                        Log.d("FirebaseUser", "Profile not Updated");
                    }
                }
            });

            FirebaseDatabase.getInstance().getReference().child("users").child(uId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseDatabase", "User details Updated Success");
                        Toast.makeText(ProfileActivity.this, "User details Updated Success", Toast.LENGTH_SHORT).show();

                        SharedPrefUtil.setBooleanPreference(ProfileActivity.this,Constants.LOGIN_STATUS,true);
                        SharedPrefUtil.setStringPreference(ProfileActivity.this, Constants.LOGIN_UID,uId);
                        user.saveUserToSharedPref(ProfileActivity.this);

                        startActivity(new Intent(ProfileActivity.this,MainActivity.class));
                        loaderUtility.stopLoader();
                        finishAffinity();
                    }
                    else
                        Log.d("FirebaseDatabase", "User details not Updated");
                    loaderUtility.stopLoader();
                }
            });
        }
    }

    private boolean checkValidationPhoneAuth(MaterialEditText etPassword, MaterialEditText etName, MaterialEditText etEmail, MaterialEditText etAddress, String state, String district) {
        boolean validation = true;
        if(etPassword.getText().length()<6) {
            etPassword.requestFocus();
            etPassword.setError(getString(R.string.password_minimum_6_length));
            validation = false;
        }
        else if(TextUtils.isEmpty(etName.getText())) {
            etName.requestFocus();
            etName.setError(getString(R.string.name_cannot_be_empty));
            validation = false;
        }
        else if(TextUtils.isEmpty(etEmail.getText()) || !isEmailValid(etEmail.getText())) {
            etEmail.requestFocus();
            etEmail.setError(getString(R.string.not_valid_email));
            validation = false;
        }
        else if(TextUtils.isEmpty(etAddress.getText())) {
            etAddress.requestFocus();
            etAddress.setError(getString(R.string.address_cannot_be_empty));
            validation = false;
        }
        else if(state.equals("") || state == "Select State") {
            TextView tvError = findViewById(R.id.tvStateError);
            tvError.setVisibility(View.VISIBLE);
            validation = false;
        }
        else if(district.equals("") || district == "Select District") {
            TextView tvError = findViewById(R.id.tvDistrictError);
            tvError.setVisibility(View.VISIBLE);
            validation = false;
        }
        else validation = true;

        return validation;
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1 && resultCode==RESULT_OK && null!=data)
        {
            imageUri = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            assert imageUri != null;
            Cursor cursor = getContentResolver().query(imageUri,filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imgProfile.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            Uri file = imageUri;
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            imgRef = "/images/"+user.getUid()+"/"+file.getLastPathSegment();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imgRef);
            UploadTask uploadTask = storageRef.putFile(file);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d("Storage","error "+exception.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d("Storage",downloadUrl.toString());

                }
            });
        }
    }

    public static String hashPassword(String password_plaintext, int workload) {
        String salt = BCrypt.gensalt(workload);
        System.out.println("salt:" + salt);
        String hashed_password = BCrypt.hashpw(password_plaintext, salt);

        return(hashed_password);
    }
}
