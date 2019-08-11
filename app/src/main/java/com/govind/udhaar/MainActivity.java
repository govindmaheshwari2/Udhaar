package com.govind.udhaar;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.support.v7.widget.SearchView;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG="MainActivity";
    private static final String DB_URL="jdbc:mysql://192.168.1.123/udhaar";
    private static final String USER="user";
    private static final String PASS="root";

    private ArrayList<String> mUser=new ArrayList<>();
    private ArrayList<String> mAmount=new ArrayList<>();
    private ArrayList<String> mPhone=new ArrayList<>();

    private Context context;
    RecyclerView recyclerView;
    private ListAdapter adapter;
    LinearLayout AddPopup,AmountPopup;
    EditText AddUsername,EditAmount;
    TextView username;
    private int currentUser;
    private AdView mAdView;


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.search_bar, menu);
        final MenuItem refresh=menu.findItem(R.id.Refresh);
        refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                retrieve();
                connection con=new connection();
                con.execute("");
                return true;
            }
        });
        MenuItem item=menu.findItem(R.id.Search);
        SearchView searchView=(SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                ArrayList<Model> newList=new ArrayList<>();
                for(int i=0;i<mUser.size();i++){
                    if(mUser.get(i).toLowerCase().contains(s)){
                        Model model=new Model(mUser.get(i),mAmount.get(i));
                        newList.add(model);
                    }
                }
                adapter.filter(newList);
                return true;
            }
        });

        return true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        if(AddPopup.getVisibility()==View.VISIBLE){
            AddPopup.setVisibility(View.INVISIBLE);
        }else if(AmountPopup.getVisibility()==View.VISIBLE){
            AmountPopup.setVisibility(View.INVISIBLE);
        }else{
            moveTaskToBack(true);
        }
        return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void UpdateData(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Model> arrayList=new ArrayList<>();
                for(int i=0;i<mUser.size();i++){
                    Log.d("mysql",mUser.get(i)+" "+mAmount.get(i));
                    Model model=new Model(mUser.get(i),mAmount.get(i));
                    arrayList.add(model);
                }
                adapter=new ListAdapter(arrayList,context);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                ClickEvent();
                LongClickEvent();
                save();
            }
        });

    }

    private void save(){
        TinyDB tinydb = new TinyDB(this);
        tinydb.putListString("user",new ArrayList<String>());
        tinydb.putListString("amount",new ArrayList<String>());
        tinydb.putListString("user",mUser);
        tinydb.putListString("amount",mAmount);
    }

    private void retrieve(){
        TinyDB tinydb = new TinyDB(this);
        mUser=tinydb.getListString("user");
        mAmount=tinydb.getListString("amount");
        UpdateData();
    }

    int click=0;
    public void ClickEvent(){

        adapter.setOnClickListen(new OnClickListener() {
            @Override
            public void onClick(int i) {
                if(click==1){
                    click=0;
                    return;
                }
                if(AddPopup.getVisibility()==View.VISIBLE || AmountPopup.getVisibility()==View.VISIBLE){
                    return;
                }
                ArrayList<Model> modelList=new ArrayList<>(adapter.getModelList());
                int pos=mUser.indexOf(modelList.get(i).getUsername());

                Intent intent=new Intent(adapter.View(), Detail.class);
                intent.putExtra("username",mUser.get(pos));
                intent.putExtra("position",pos);
                intent.putExtra("user2",mPhone.get(i));
                intent.putExtra("user1",getInfo());

                adapter.View().startActivity(intent);

            }
        });

    }

    public void LongClickEvent(){
        adapter.setOnLongClick(new OnLongClick() {
            @Override
            public void onClick(int i) {
                if(AddPopup.getVisibility()==View.VISIBLE || AmountPopup.getVisibility()==View.VISIBLE){
                    return;
                }
                click=1;
                ArrayList<Model> modelList=new ArrayList<>(adapter.getModelList());
                int pos=mUser.indexOf(modelList.get(i).getUsername());
                username.setText(mUser.get(pos));
                EditAmount.setHint(mAmount.get(pos));
                currentUser=i;
                AmountPopup.setVisibility(View.VISIBLE);

            }
        });

    }


    public void AddAmount(View v){
        if(EditAmount.getText().toString().equalsIgnoreCase("")){
            Snackbar.make(v, "Enter Amount", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
        int amount=Integer.parseInt(EditAmount.getText().toString());
        int currentAmount=Integer.parseInt(mAmount.get(currentUser).substring(3));
        mAmount.set(currentUser,"Rs."+(amount+currentAmount));
        TinyDB tinydb = new TinyDB(this);
        ArrayList<String> info=tinydb.getListString(mUser.get(currentUser));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd|HH:mm");
        String date = sdf.format(new Date());
        info.add("*"+date+"--"+EditAmount.getText().toString()+"S");
        tinydb.putListString(mUser.get(currentUser),info);
        UpdateData();
        AmountPopup.setVisibility(View.INVISIBLE);
        EditAmount.setText("");
        Snackbar.make(v, "Updated!", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        hideKeyboard(this);
    }

    public void SubAmount(View v){
        if(EditAmount.getText().toString().equalsIgnoreCase("")){
            Snackbar.make(v, "Enter Amount", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
        int amount=Integer.parseInt(EditAmount.getText().toString());
        int currentAmount=Integer.parseInt(mAmount.get(currentUser).substring(3));
        mAmount.set(currentUser,"Rs."+(currentAmount-amount));
        TinyDB tinydb = new TinyDB(this);
        ArrayList<String> info=tinydb.getListString(mUser.get(currentUser));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd|HH:mm");
        String date = sdf.format(new Date());
        info.add("*"+date+"--"+EditAmount.getText().toString()+"R");
        tinydb.putListString(mUser.get(currentUser),info);
        UpdateData();
        AmountPopup.setVisibility(View.INVISIBLE);
        EditAmount.setText("");
        Snackbar.make(v, "Updated!", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        hideKeyboard(this);
    }

    public void AddUser(View v){
        if(AddUsername.getText().toString().equalsIgnoreCase("")){
            Snackbar.make(v, "Enter Username", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
        String Amount;
        Amount="0";
        mUser.add(0,AddUsername.getText().toString());
        mAmount.add(0,Amount);
        UpdateData();
        Snackbar.make(v, "Added", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        AddPopup.setVisibility(View.INVISIBLE);
        AddUsername.setText("");
        hideKeyboard(this);
    }

    public void chip10(View v){
        chip(10);
    }

    public void chip50(View v){
        chip(50);
    }
    public void chip100(View v){
        chip(100);
    }
    public void chip1000(View v){
        chip(1000);
    }
    private void chip(int value){
        if(EditAmount.getText().toString().equalsIgnoreCase("")){
            EditAmount.setText(value+"");
        }else{
        EditAmount.setText((Integer.parseInt(EditAmount.getText().toString())+value)+"");
        }
    }

    private String getInfo(){
        TinyDB tinydb = new TinyDB(this);
        String phone=tinydb.getString("phone");
        return phone;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MobileAds.initialize(this, "ca-app-pub-8868204021755122~3858568704");
        mAdView = findViewById(R.id.adView);
        context=this;
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        recyclerView=findViewById(R.id.RecycleView);
        retrieve();
        connection con=new connection();
        con.execute("");
        AddPopup=(LinearLayout)findViewById(R.id.AddPopup);
        AmountPopup=(LinearLayout)findViewById(R.id.AmountPopup);
        username=(TextView)findViewById(R.id.username);
        EditAmount=(EditText)findViewById(R.id.EditAmount);
        AddUsername=(EditText)findViewById(R.id.txtUsername);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AddPopup.getVisibility()==View.VISIBLE || AmountPopup.getVisibility()==View.VISIBLE){
                    return;
                }
                AddPopup.setVisibility(View.VISIBLE);
            }
        });

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private class connection extends AsyncTask<String,String,String>
    {

        @Override
        protected String doInBackground(String... strings) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn= DriverManager.getConnection(DB_URL,USER,PASS);
                if(conn==null){
                    Log.d("mysql","Error");
                }else{
                    String name=getInfo();
                    String query="SELECT username,phone from login where Phone in(SELECT Receive FROM detail WHERE sender="+name+") or Phone in(SELECT sender FROM detail WHERE Receive="+name+")";
                    Statement stmt=conn.createStatement();
                    ResultSet resultSet=stmt.executeQuery(query);
                    mUser.clear();
                    mAmount.clear();
                   while (resultSet.next()){
                       int take,recevie;
                       mPhone.add(resultSet.getString(2));
                       mUser.add(resultSet.getString(1));
                       String q="Select sum(amount) from detail where sender="+name+" and Receive="+resultSet.getString(2)+";";
                       Statement stmt2=conn.createStatement();
                       ResultSet resultSet1=stmt2.executeQuery(q);
                       if(resultSet1.next()){
                           take=resultSet1.getInt(1);
                       }else{
                           take=0;
                       }
                      String q2="Select sum(amount) from detail where sender="+resultSet.getString(2)+" and Receive="+name+";";
                       Statement stmt3=conn.createStatement();
                       ResultSet resultSet2=stmt3.executeQuery(q2);
                       if(resultSet2.next()){
                           recevie=resultSet2.getInt(1);
                       }else{
                           recevie=0;
                       }
                       mAmount.add(take-recevie+"");
                   }
                   UpdateData();
                    Log.d("mysql","Successfully Save");
                }
            }catch (Exception e){
                Log.d("mysqlerror",e.getMessage());
            }
            return null;
        }
    }
}
