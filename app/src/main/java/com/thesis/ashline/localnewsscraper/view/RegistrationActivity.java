package com.thesis.ashline.localnewsscraper.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.thesis.ashline.localnewsscraper.R;
import com.thesis.ashline.localnewsscraper.adapter.CountrySpinnerAdapter;
import com.thesis.ashline.localnewsscraper.model.Country;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends ActionBarActivity {

    private Spinner countrySpinner;
    private List<Country> countryList;
    private CountrySpinnerAdapter adapter;
    private JSONArray countryArray;
    private EditText txtUsername;
    private EditText txtPhone;
    private Spinner spinner;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPhone = (EditText) findViewById(R.id.txtPhone);
        spinner = (Spinner) findViewById(R.id.country_spinner);
        activity = this;
        txtUsername.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    hideKeyboard();
                    textView.clearFocus();
                    spinner.requestFocus();
                    spinner.performClick();
                }
                return true;
            }
        });
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean phoneIsValid = false;
                boolean usernameIsValid = false;
                if (!validatePhoneNumber()) {
                    txtPhone.setError("Please enter a valid phone number without country code");
                } else {
                    phoneIsValid = true;
                }
                if (txtUsername.getText().length() == 0) {
                    txtUsername.setError("Please enter at least one letter");
                } else {
                    usernameIsValid = true;
                }
                if (usernameIsValid == true && phoneIsValid == true) {

                    String username = txtUsername.getText().toString();
                    String countryCode = ((Country) spinner.getSelectedItem()).code;
                    String phonenumber = txtPhone.getText().toString();
                    Intent intent = new Intent(activity, LoadingActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("phone", countryCode + phonenumber);
                    intent.putExtra("mode", LoadingActivity.REGISTER_MODE
                    );
                    startActivity(intent);
                }
            }
        });
        initCountrySpinner();
    }

    private boolean validatePhoneNumber() {
        String expression = "^[0-9-1+]{10,15}$";
        CharSequence inputStr = ((Country) spinner.getSelectedItem()).code + txtPhone.getText();
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        return (matcher.matches()) ? true : false;
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

    private String loadJSONFromFile() {
        String json = null;
        try {

            InputStream is = getResources().openRawResource(R.raw.country_codes);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void initCountrySpinner() {
        countrySpinner = (Spinner) findViewById(R.id.country_spinner);
        countryList = new ArrayList<Country>();
        try {
            JSONObject data = new JSONObject(loadJSONFromFile());
            countryArray = data.getJSONArray("data");

            for (int i = 0; i < countryArray.length(); i++) {
                Country country = new Country();
                JSONObject obj = countryArray.getJSONObject(i);
                country.name = obj.getString("name");
                country.code = obj.getString("code");
                country.abbreviation = obj.getString("abbreviation");

                countryList.add(country);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new CountrySpinnerAdapter(countryList, this);
        countrySpinner.setAdapter(adapter);
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
