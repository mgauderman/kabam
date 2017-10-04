package com.kabam.kabam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.ParseException;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Ayush on 11/16/15.
 */
public class Signup extends FragmentActivity {

    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishSignup();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCreateAccountClick(View view) {
        String firstName = ((EditText) findViewById(R.id.firstName)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.lastName)).getText().toString();
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        String confirmPassword = ((EditText) findViewById(R.id.confirmPassword)).getText().toString();

        if (firstName.length() == 0)
            displayErrorMessage("Enter a first name!");
        else if (lastName.length() == 0)
            displayErrorMessage("Enter a last name!");
        else if (email.length() == 0)
            displayErrorMessage("Enter an email address!");
        else if (password.length() == 0)
            displayErrorMessage("Enter a password!");
        else if (password.length() != confirmPassword.length() || !password.equals(confirmPassword))
            displayErrorMessage("Ensure the password and confirm password match.");
        else {
            progressDialog = ProgressDialog.show(Signup.this, "", "Signing Up...", true);

            ParseUser user = new ParseUser();
            user.setUsername(email);
            user.setEmail(email);
            user.setPassword(password);
            user.put("first_name", firstName);
            user.put("last_name", lastName);
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    progressDialog.dismiss();
                    if (e == null) {
                        Log.d("SIGNUP", "Signed Up!");
                        finish();
                    } else {
                        displayErrorMessage("Unable to create a new account. Try again later!");
                        Log.d("SIGNUP", "Error signing up...");
                    }
                }
            });
        }
    }

    private void displayErrorMessage(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(Signup.this).create();
        alertDialog.setTitle("Signup Error");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void onFacebookLoginClick(View view) {
        progressDialog = ProgressDialog.show(Signup.this, "", "Logging In...", true);

        List<String> permissions = Arrays.asList("public_profile", "email");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            public void done(ParseUser user, ParseException err) {
                progressDialog.dismiss();
                if (user == null && err != null) {
                    Log.d("Facebook Login", "Unable to sign up using Facebook, with error message: " + err.getMessage());
                } else if (user.isNew()) {
                    Log.d("Facebook Login", "Creating new account using Facebook.");
                    GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                ParseUser.getCurrentUser().setUsername(object.getString("email"));
                                ParseUser.getCurrentUser().setEmail(object.getString("email"));
                                ParseUser.getCurrentUser().put("first_name", object.getString("first_name"));
                                ParseUser.getCurrentUser().put("last_name", object.getString("last_name"));
                                ParseUser.getCurrentUser().saveInBackground();
                            } catch (JSONException jsone) {
                                Log.d("JSON Exception", jsone.getMessage());
                            } finally {
                                finishSignup();
                            }
                        }
                    });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,first_name,last_name,email");
                    request.setParameters(parameters);
                    request.executeAsync();
                } else {
                    Log.d("Facebook Login", "Logging in using old Facebook-linked account.");
                    finishSignup();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void finishSignup() {
        finish();
    }
}
