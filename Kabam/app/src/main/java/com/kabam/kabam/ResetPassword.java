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
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Ayush on 11/16/15.
 */
public class ResetPassword extends FragmentActivity {

    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishResetPassword();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onResetPasswordClick(View view) {
        progressDialog = ProgressDialog.show(ResetPassword.this, "", "Resetting Password...", true);
        String email = ((EditText) findViewById(R.id.email)).getText().toString();

        if (email.length() == 0) {
            Log.d("Reset Password", "No email address entered.");
            displayErrorMessage("Reset Password Error", "Please enter an email address!");
        } else {
            ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
                @Override
                public void done(ParseException e) {
                    progressDialog.dismiss();
                    if (e == null) {
                        displayErrorMessage("Reset Password", "Please check your email for instructions on how to reset your password!");
                        Log.d("Reset Password", "Reset Password Successful!");
                        finishResetPassword();
                    } else {
                        displayErrorMessage("Reset Password Error", "An error occurred while trying to reset your password. Please try again later.");
                        Log.d("Reset Password", "Error while resetting password: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void displayErrorMessage(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(ResetPassword.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void finishResetPassword() {
        finish();
    }
}
