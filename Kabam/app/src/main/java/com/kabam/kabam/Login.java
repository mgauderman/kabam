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
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Ayush on 11/16/15.
 */
public class Login extends FragmentActivity {

    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

    public void onLoginClick(View view) {
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();

        if (email.length() == 0)
            displayErrorMessage("Enter an email address!");
        else if (password.length() == 0)
            displayErrorMessage("Enter a password!");
        else {
            progressDialog = ProgressDialog.show(Login.this, "", "Logging In...", true);

            ParseUser.logInInBackground(email, password, new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    progressDialog.dismiss();
                    if (user == null) {
                        Log.d("Login", "Login failed with error: " + e.getMessage());
                        displayErrorMessage("There was an error while logging you in: " + e.getMessage() + ". Please try again later.");
                    } else if (user.getBoolean("emailVerified")) {
                        Log.d("Login", "Login succeeded!");
                        finishLogin();
                    } else {
                        Log.d("Login", "Login unsuccessful! Please validate email.");
                        displayErrorMessage("Please validate email to use Kabam!");
                    }
                }
            });
        }
    }

    private void displayErrorMessage(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(Login.this).create();
        alertDialog.setTitle("Login Error");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void onSignupClick(View view) {
        Intent signupIntent = new Intent(Login.this, Signup.class);
        Login.this.startActivity(signupIntent);
    }

    public void onFacebookLoginClick(View view) {
        progressDialog = ProgressDialog.show(Login.this, "", "Logging In...", true);

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
                                finishLogin();
                            }
                        }
                    });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,first_name,last_name,email");
                    request.setParameters(parameters);
                    request.executeAsync();
                } else {
                    Log.d("Facebook Login", "Logging in using old Facebook-linked account.");
                    finishLogin();
                }
            }
        });
    }

    public void onForgotPasswordClick(View view) {
        Intent resetPasswordIntent = new Intent(Login.this, ResetPassword.class);
        Login.this.startActivity(resetPasswordIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void finishLogin() {
        finish();
    }
}
