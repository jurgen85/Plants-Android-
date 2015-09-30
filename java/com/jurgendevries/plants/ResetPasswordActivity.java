package com.jurgendevries.plants;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ResetPasswordActivity extends ActionBarActivity {

    @InjectView(R.id.emailField) EditText mEmail;
    @InjectView(R.id.resetPasswordButton) Button mResetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.inject(this);

        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();

                if (email.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);
                    builder.setMessage(getString(R.string.reset_password_error_message))
                            .setTitle(getString(R.string.reset_password_error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                // Succes
                                Toast toast = Toast.makeText(ResetPasswordActivity.this, getString(R.string.password_reset_send_message), Toast.LENGTH_LONG);
                                toast.show();
                                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }  else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);
                                builder.setMessage(e.getMessage())
                                        .setTitle(getString(R.string.reset_password_error_title))
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });

                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reset_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
