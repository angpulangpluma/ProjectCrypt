package com.dlsu.getbetter.getbetter.activities;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.activities.HealthCenterActivity;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.sql.SQLException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button registerUserBtn;

    private SystemSessionManager systemSessionManager;
    private DataAdapter getBetterDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        systemSessionManager = new SystemSessionManager(getApplicationContext());

        emailInput = (TextInputEditText)findViewById(R.id.email_input);
        passwordInput = (TextInputEditText)findViewById(R.id.password_input);

        emailInputLayout = (TextInputLayout) findViewById(R.id.email_input_layout);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.password_input_layout);
        Button signInBtn = (Button) findViewById(R.id.sign_in_btn);
//        registerUserBtn = (Button)findViewById(R.id.register_user_btn);

        signInBtn.setOnClickListener(this);
        initializeDatabase();
//        emailInput.setError("Test");
//        passwordInput.setError(null);
    }

    private void initializeDatabase() {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch(SQLException e ){
            e.printStackTrace();
        }
    }

    private boolean checkLogin(String email, String password) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        boolean result = getBetterDb.checkLogin(email, password);
        getBetterDb.closeDatabase();


        return result;
    }

    @Override
    public void onClick(View v) {

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        View focusView = null;
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        if (email.trim().length() > 0 && password.trim().length() > 0) {

//            if (email.contains("@")) {
//
//                emailInputLayout.setError("Invalid email");
//                emailInput.requestFocus();
//
//            } else {

                if (checkLogin(email, password)) {

                    systemSessionManager.createUserSession(email);
//                Intent intent = new Intent(this, HomeActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//
//                finish();

                    Intent intent = new Intent(this, HealthCenterActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    finish();


                } else {

                    emailInputLayout.setError("Invalid email or password");
                    emailInput.requestFocus();

                }

//            }

        } else {

            if (email.trim().length() == 0) {

                emailInputLayout.setError("Email Required");
                focusView = emailInput;
                focusView.requestFocus();

            } else if (password.trim().length() == 0) {

                passwordInputLayout.setError("Password Required");
                focusView = passwordInput;
                focusView.requestFocus();

//            passwordInput.setError("Please enter a password!");

//            Toast.makeText(getApplicationContext(),
//                    "Please enter Email and Password",
//                    Toast.LENGTH_LONG).show();

            }
        }

    }
}
