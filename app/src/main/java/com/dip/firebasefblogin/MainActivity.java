package com.dip.firebasefblogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private CallbackManager manager;
    private FirebaseAuth mfirebaseAuth;
    private static final String Tag="FacebookAuthentaction";
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;
    LoginButton login;
    TextView name,lname,fname;
    ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        login=(LoginButton) findViewById(R.id.login_button);
        name=(TextView)findViewById(R.id.textView1);
        lname=(TextView)findViewById(R.id.textView2);
        fname=(TextView)findViewById(R.id.textView3);
        image=(ImageView)findViewById(R.id.imageView);

        mfirebaseAuth=FirebaseAuth.getInstance();
        manager=CallbackManager.Factory.create();
        login.registerCallback(manager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(Tag,"onSuccess" + loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(Tag,"Cancle");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(Tag,"Error");
            }
        });
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user =firebaseAuth.getCurrentUser();
                if(user !=null){
                    updateUI(user);
                }
                else{
                    updateUI(null);
                    name.setText("");
                    image.setImageDrawable(null);
                }
            }
        };

        accessTokenTracker =new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken == null){
                    mfirebaseAuth.signOut();
                }
            }
        };
    }

    private void handleFacebookToken(AccessToken token){
        Log.d(Tag,"handelFacebookToken"+ token);
        AuthCredential credential= FacebookAuthProvider.getCredential(token.getToken());
        mfirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(Tag,"sign in with credential success");
                    FirebaseUser user=mfirebaseAuth.getCurrentUser();
                    updateUI(user);
                }
                else {
                    Log.d(Tag,"sign in with credential failure");
                    Toast.makeText(MainActivity.this,"Failure",Toast.LENGTH_LONG).show();
                    updateUI(null);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        manager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            name.setText(user.getDisplayName());
            lname.setText(user.getEmail());
            fname.setText(user.getPhoneNumber());
            if(user.getPhotoUrl() !=null){
                String photoUrl=user.getPhotoUrl().toString();
                photoUrl=photoUrl + "?type=large";
                Picasso.get().load(photoUrl).into(image);
            }
            else{
                Toast.makeText(this,"No Data",Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
           mfirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener !=null){
          mfirebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}