package com.example.googlelogindemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.googlelogindemo.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1231;
    private static final String TAG = MainActivity.class.getName();
    private ActivityMainBinding binding;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        setUpGoogleAPIClient();
        setUpListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUIVisibility(account);
    }

    private void setUpListeners() {
        binding.btnGoogleLogin.setOnClickListener(v -> {
            if (binding.btnGoogleLogin.getText().toString().equals("Google Login")) {
                signIn();
            } else {
                signOut();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void signOut() {
        googleSignInClient.signOut().addOnCompleteListener(task -> updateUIVisibility(null));
    }

    private void setUpGoogleAPIClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            updateUIVisibility(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode() + "\n" + e.getLocalizedMessage() + "\nMEssg: " + e.getMessage());
            updateUIVisibility(null);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUIVisibility(GoogleSignInAccount account) {
        if (account == null) {
            binding.btnGoogleLogin.setText("Google Login");
            binding.group.setVisibility(View.GONE);
        } else {
            binding.btnGoogleLogin.setText("Logout");
            binding.group.setVisibility(View.VISIBLE);
            updateData(account);
        }
    }

    private void updateData(GoogleSignInAccount account) {
        String personName = account.getDisplayName();
        String email = account.getEmail();
        binding.tvUserName.setText(personName);
        binding.tvEmail.setText(email);
        Uri personPhotoUri = account.getPhotoUrl();
        loadImage(personPhotoUri);
    }

    private void loadImage(Uri personPhotoUri) {
        if (personPhotoUri != null) {
            Glide.with(getApplicationContext()).load(personPhotoUri.toString())
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivProfilePic);
        }
    }
}