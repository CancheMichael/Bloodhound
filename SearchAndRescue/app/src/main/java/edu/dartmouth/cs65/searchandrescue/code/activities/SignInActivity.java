/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;
import java.util.List;

import edu.dartmouth.cs65.searchandrescue.R;

//Signin activity utilizing Firebase UI
public class SignInActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_googlesignin);
        startSignInActivityFirebase();
    }

    //
    public void startSignInActivityFirebase() {
        //Add email, phone, and Google options
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        //Starts signin activity, adding custom theme and logo
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.LoginTheme)
                        .setLogo(R.drawable.logo2)
                        .build(),
                RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            //Sign in successful
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Intent partyIntent = new Intent(SignInActivity.this,
                        PartyActivity.class);
                partyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                partyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(partyIntent);
                finish();
            } else if(response == null) {
                //Ends activity on back
                finish();
            }
        }
    }
}
