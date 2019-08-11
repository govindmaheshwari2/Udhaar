package com.govind.udhaar;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Detail extends AppCompatActivity {


    private static final String TAG="Detail";
    private static final String DB_URL="jdbc:mysql://192.168.1.123/udhaar";
    private static final String USER="user";
    private static final String PASS="root";

    private RecyclerView detailList;
    detailAdapter adapter;
    ArrayList<String> infoS=new ArrayList<>(),infoR=new ArrayList<>(),time=new ArrayList<>(),time2=new ArrayList<>(),info=new ArrayList<>(),reason=new ArrayList<>();
    ArrayList<String> infonew=new ArrayList<>();
    private String username;
    LinearLayout AmountPopup,RemovePopup, MainButton;
    Button AddAmount;
    EditText EditAmount,RemoveUser,txtReason;
    private int position;
    ActivityOptions options;
    private SlidrInterface slidrInterface;
    private String user1,user2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.Name) {
            RemoveUser.setText(username);
            RemovePopup.setVisibility(View.VISIBLE);
            slidrInterface.lock();
            MainButton.setVisibility(View.GONE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(AmountPopup.getVisibility()==View.VISIBLE){
                AmountPopup.setVisibility(View.INVISIBLE);
                slidrInterface.unlock();
                MainButton.setVisibility(View.VISIBLE);
            }else if(RemovePopup.getVisibility()==View.VISIBLE){
                RemovePopup.setVisibility(View.INVISIBLE);
                slidrInterface.unlock();
                MainButton.setVisibility(View.VISIBLE);
            }else{
                Intent intent=new Intent(this,MainActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent,options.toBundle());
                }else {
                    startActivity(intent);
                }
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void UpdateList(){
        try {
            adapter = new detailAdapter(infoR, infoS, time2, time,reason, this);
            detailList.setAdapter(adapter);
            detailList.setLayoutManager(new LinearLayoutManager(this));
            detailList.scrollToPosition(info.size() - 1);
            save();
        }catch (Exception e){
            Log.d("UpdateError",e.getMessage());
        }
    }

    private void save(){
        TinyDB tinydb = new TinyDB(this);
        tinydb.putListString(username,info);
    }

    private void retrieve(){
        TinyDB tinydb = new TinyDB(this);
        info=tinydb.getListString(username);
        Arrange();
    }

    private void Arrange(){
        try{
            infoR.clear();
            infoS.clear();
            time.clear();
            time2.clear();
            reason.clear();
        for(int i=0;i<info.size();i++){
            Log.d("mysql",info.get(i));
            if(info.get(i).contains("S")){
                infoS.add((info.get(i).replace("S","")).substring((info.get(i)).indexOf("--")+2));
                time.add(info.get(i).substring((info.get(i)).indexOf("*")+1,(info.get(i)).indexOf("--")));
                reason.add(info.get(i).substring(0,(info.get(i)).indexOf("*")));
                time2.add("");
                infoR.add("");
            }else if(info.get(i).contains("R")){
                infoR.add((info.get(i).replace("R","")).substring((info.get(i)).indexOf("--")+2));
                time2.add(info.get(i).substring((info.get(i)).indexOf("*")+1,(info.get(i)).indexOf("--")));
                reason.add(info.get(i).substring(0,(info.get(i)).indexOf("*")));
                time.add("");
                infoS.add("");
            }
        }
        }catch (Exception e){
            Log.d("error",e.getMessage());
        }
        UpdateList();
    }

    private void setMainValue(int value){
        try {
            TinyDB tinydb = new TinyDB(this);
            ArrayList<String> amount = tinydb.getListString("amount");
            amount.set(position,"Rs."+(Integer.parseInt(amount.get(position).substring(3)) + value));
            tinydb.putListString("amount", amount);
        }catch (Exception e){
            Log.d("error:",e.getMessage());
        }
    }

    public void Pay(View v){
        if(AmountPopup.getVisibility()==View.VISIBLE){
            return;
        }
        AddAmount.setText("Add");
        AmountPopup.setVisibility(View.VISIBLE);
        MainButton.setVisibility(View.GONE);
        slidrInterface.lock();
    }

    public void Receive(View v){
        if(AmountPopup.getVisibility()==View.VISIBLE){
            return;
        }
        AddAmount.setText("Subtract");
        AmountPopup.setVisibility(View.VISIBLE);
        MainButton.setVisibility(View.GONE);
        slidrInterface.lock();
    }

    public void AddAmount(View v){
        if(EditAmount.getText().toString().equalsIgnoreCase("")){
            Snackbar.make(v, "Enter Amount", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date());
        String Reason=txtReason.getText().toString().trim();
        if(AddAmount.getText().toString().equalsIgnoreCase("Add")){
            info.add(Reason+"*"+date+"--"+EditAmount.getText().toString()+"S");
            setMainValue(Integer.parseInt(EditAmount.getText().toString()));
        }else{
            info.add(Reason+"*"+date+"--"+EditAmount.getText().toString()+"R");
            setMainValue(-1*Integer.parseInt(EditAmount.getText().toString()));
        }
        Arrange();
        update update=new update();
        update.execute("");
        EditAmount.setText("");
        txtReason.setText("");
        AmountPopup.setVisibility(View.INVISIBLE);
        hideKeyboard(this);
        slidrInterface.unlock();
        MainButton.setVisibility(View.VISIBLE);
    }

    public void removeUser(View v){
        TinyDB tinydb = new TinyDB(this);
        ArrayList<String> amount = tinydb.getListString("amount");
        ArrayList<String> user = tinydb.getListString("user");
        user.remove(position);
        amount.remove(position);
        tinydb.putListString("user",user);
        tinydb.putListString("amount",amount);
        tinydb.putListString(username,new ArrayList<String>());
        hideKeyboard(this);
        Snackbar.make(v, "Remove Successfully", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        hideKeyboard(this);
        slidrInterface.unlock();
        MainButton.setVisibility(View.VISIBLE);
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void UpdateUser(View v){
        if(RemoveUser.getText().toString().equalsIgnoreCase("")){
            Snackbar.make(v, "Enter Name", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
        if(RemoveUser.getText().toString().equalsIgnoreCase(username)){
            RemovePopup.setVisibility(View.INVISIBLE);
            slidrInterface.unlock();
            MainButton.setVisibility(View.VISIBLE);
            return;
        }
        TinyDB tinydb = new TinyDB(this);
        ArrayList<String> user = tinydb.getListString("user");
        user.set(position,RemoveUser.getText().toString());
        tinydb.putListString("user",user);
        tinydb.putListString(user.get(position),info);
        tinydb.putListString(username,new ArrayList<String>());
        username=RemoveUser.getText().toString();
        setTitle(username);
        hideKeyboard(this);
        RemovePopup.setVisibility(View.INVISIBLE);
        slidrInterface.unlock();
        MainButton.setVisibility(View.VISIBLE);
    }

    private void transition(){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Explode explode=new Explode();
            explode.setDuration(300);
            getWindow().setEnterTransition(explode);
        }

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        slidrInterface=Slidr.attach(this);
        slidrInterface.unlock();
        Intent intent=getIntent();
        position= intent.getIntExtra("position",-1);
        username=intent.getStringExtra("username");
        user1=intent.getStringExtra("user1");
        user2=intent.getStringExtra("user2");
        setTitle(username);
        transition();
        detailList=(RecyclerView) findViewById(R.id.detailList);
        AmountPopup=(LinearLayout)findViewById(R.id.AmountPopup);
        RemovePopup=(LinearLayout)findViewById(R.id.RemovePopup);
        MainButton=(LinearLayout)findViewById(R.id.MainButton);
        RemoveUser=(EditText)findViewById(R.id.RemoveUser);
        retrieve();
        connection con=new connection();
        con.execute("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            options= ActivityOptions.makeSceneTransitionAnimation(this);
        }
        EditAmount=(EditText)findViewById(R.id.EditAmount);
        txtReason=(EditText)findViewById(R.id.txtReason);
        AddAmount=(Button)findViewById(R.id.AddAmount);
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

    private class connection extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn= DriverManager.getConnection(DB_URL,USER,PASS);
                if(conn==null){
                    Log.d("mysql","Error");
                }else{
                    String query="select Sender,Receive,amount,info,Date from detail where Sender="+user1+" and Receive="+user2+" or Sender="+user2+" and Receive="+user1+" order by date;";
                    Statement stmt=conn.createStatement();
                    ResultSet result=stmt.executeQuery(query);
                    infonew.clear();
                    while (result.next()){
                        if((result.getInt(1))==Integer.parseInt(user1))
                            infonew.add(result.getString(4)+"*"+result.getString(5)+"--"+result.getString(3)+"S");
                        else
                            infonew.add(result.getString(4)+"*"+result.getString(5)+"--"+result.getString(3)+"R");
                    }
                    if(info.size()==infonew.size()) {
                      //  Arrange();
                    }else if(infonew.size()>info.size()){
                        info.clear();
                        info.addAll(infonew);
                        Arrange();
                    } else {
                        Log.d("mysql2",""+info.size()+" "+infonew.size());
                        update update=new update();
                        update.execute("");
                    }

                }
            }catch (Exception e){
                Log.d("mysql",e.getMessage());
            }
            return null;
        }
    }

    private class update extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn= DriverManager.getConnection(DB_URL,USER,PASS);
                if(conn==null){
                    Log.d("mysql","Error");
                }else{
                    String query="";
                    int total=infonew.size();
                    for(int i=total;i<info.size();i++) {
                        if(time.get(i).equalsIgnoreCase("")) {
                            query= "insert into detail values('" + time2.get(i) + "'," + user2 + "," + user1 + "," + infoR.get(i) + ",'"+reason.get(i)+"');";
                        }else{
                            query= "insert into detail values('" + time.get(i) + "'," + user1 + "," + user2 + "," + infoS.get(i) + ",'"+reason.get(i)+"');";
                        }

                        Statement stmt = conn.createStatement();
                        stmt.executeUpdate(query);
                        stmt.close();
                        Log.d("mysql","done");
                        infonew.add("");
                    }
                }
            }catch (Exception e){
                Log.d("mysql",e.getMessage());
            }
            return null;
        }
    }

}
