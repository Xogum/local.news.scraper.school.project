package com.thesis.ashline.localnewsscraper.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.thesis.ashline.localnewsscraper.R;

public class RegistrationActivity extends ActionBarActivity {

    private EditText txtUsername;
    private EditText txtEmail;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtEmail = (EditText) findViewById(R.id.txtEmail);

        activity = this;

        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean emailIsValid = isValidEmail();
                boolean usernameIsValid = false;

                if (!emailIsValid) {
                    txtEmail.setError("Please enter a valid email address");
                }

                if (txtUsername.getText().length() == 0) {
                    txtUsername.setError("Please enter at least one letter");
                } else {
                    usernameIsValid = true;
                }

                if (usernameIsValid == true && emailIsValid == true) {

                    String username = txtUsername.getText().toString();
                    String email = txtEmail.getText().toString();
                    Intent intent = new Intent(activity, LoadingActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("email", email);
                    intent.putExtra("mode", LoadingActivity.REGISTER_MODE
                    );
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private boolean isValidEmail() {
        CharSequence email = txtEmail.getText();
        if (TextUtils.isEmpty(email)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_import) {
//            showImportActivity();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
