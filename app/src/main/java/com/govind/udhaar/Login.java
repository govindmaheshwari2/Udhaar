package com.govind.udhaar;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Login extends AppCompatActivity {

    private static final String DB_URL="jdbc:mysql://192.168.1.123/udhaar";
    private static final String USER="user";
    private static final String PASS="root";

    EditText username,email,password,repassword,phone,usernameLogin,passwordLogin;
    ScrollView LoginLayout,SignupLayout;

    public void Signup(View v){
        if(username.getText().toString().equalsIgnoreCase("") || email.getText().toString().equalsIgnoreCase("")||password.getText().toString().equalsIgnoreCase("")||phone.getText().toString().equalsIgnoreCase("")){
            Snackbar.make(v, "Enter Detail", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
        if(password.getText().toString().length()<6){
            Snackbar.make(v, "Min. Length of Password is 6", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
        if(password.getText().toString().contentEquals(repassword.getText().toString())){
            connection co=new connection();
            co.execute("");
        }else{
            Snackbar.make(v, "Password Does not Match", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
    }

    public void Login(View v){
        if(usernameLogin.getText().toString().equalsIgnoreCase("") || passwordLogin.getText().toString().equalsIgnoreCase("")){
            Snackbar.make(v, "Enter Detail", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
        LoginConnection login=new LoginConnection();
        login.execute("");

    }

    private void changeActivity(String phone){
        TinyDB tinydb = new TinyDB(this);
        tinydb.putString("username",usernameLogin.getText().toString());
        tinydb.putString("phone",phone);
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    private void checkSignup(){
        TinyDB tinydb = new TinyDB(this);
        String username=tinydb.getString("username");
        if(username.length()>0){
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkSignup();
        setContentView(R.layout.activity_login);
        username=(EditText)findViewById(R.id.username);
        email=(EditText)findViewById(R.id.Email);
        password=(EditText)findViewById(R.id.Password);
        repassword=(EditText)findViewById(R.id.Re_Password);
        phone=(EditText)findViewById(R.id.Phone);
        usernameLogin=(EditText)findViewById(R.id.usernameLogin);
        passwordLogin=(EditText)findViewById(R.id.PasswordLogin);
        SignupLayout=(ScrollView)findViewById(R.id.SignupLayout);
        LoginLayout=(ScrollView)findViewById(R.id.LoginLayout);
        TextView LoginLoader=(TextView)findViewById(R.id.LoginLoader);
        LoginLoader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignupLayout.setVisibility(View.GONE);
                LoginLayout.setVisibility(View.VISIBLE);
            }
        });
        TextView SignupLoader=(TextView)findViewById(R.id.SignupLoader);
        SignupLoader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignupLayout.setVisibility(View.VISIBLE);
                LoginLayout.setVisibility(View.GONE);
            }
        });
    }

    private class LoginConnection extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn= DriverManager.getConnection(DB_URL,USER,PASS);
                if(conn==null){
                    Log.d("mysql","Error");
                }else{
                    String name,pass;
                    name=usernameLogin.getText().toString();
                    pass=passwordLogin.getText().toString();
                    String query="select * from login where email='"+name+"' and password='"+pass+"';";
                    Statement stmt=conn.createStatement();
                    ResultSet result=stmt.executeQuery(query);
                    {
                        if(result.next()){
                            Snackbar.make(username, "Login successfully!", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            changeActivity(String.valueOf(result.getInt("Phone")));
                        }else{
                            Snackbar.make(username, "Enter Correct Detail!", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        }
                    }

                }
            }catch (Exception e){
                Log.d("mysql",e.getMessage());
            }
            return null;
        }
    }

    private class connection extends AsyncTask<String,String,String>
    {

        @Override
        protected void onPreExecute() {
            Log.d("mysql","Inserting");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("mysql","Inserted Successfully");
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn= DriverManager.getConnection(DB_URL,USER,PASS);
                if(conn==null){
                    Log.d("mysql","Error");
                }else{
                    String name,e,pass;
                    int ph;
                    name=username.getText().toString();
                    e=email.getText().toString();
                    pass=password.getText().toString();
                    ph=Integer.parseInt(phone.getText().toString());
                    String query="insert into login values('"+name+"','"+e+"','"+pass+"',"+ph+");";
                    Statement stmt=conn.createStatement();
                    stmt.executeUpdate(query);
                    Snackbar.make(username, "Sign up successfully!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    Log.d("mysql","Successfully Save");
                }
            }catch (Exception e){
                if((e.getMessage()).contains(username.getText().toString())){
                Snackbar.make(username, "Email ID already exist!", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                }else if((e.getMessage()).contains(phone.getText().toString())){
                    Snackbar.make(username, "Phone Number already exist!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
                Log.d("mysql2",e.getMessage());
            }
            return null;
        }
    }
}
