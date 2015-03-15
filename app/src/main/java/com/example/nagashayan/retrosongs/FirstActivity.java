package com.example.nagashayan.retrosongs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.parser.JSONParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FirstActivity extends ActionBarActivity {
    RelativeLayout rl;
    GridView grid;
    ArrayList<Grid> list;
    ArrayList<Integer> selectedlist;

    ProgressDialog m_dialog;
    GridAdapter gridAdt;

    private static String SERVER_URL;
    private static String SELECT_COLOR;
    private static int UNSELECT_COLOR;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        //take some values from settings class
        Settings settings = new Settings();
        SERVER_URL = settings.HOME_SERVER_URL;
        SELECT_COLOR = settings.SELECT_COLOR;
        UNSELECT_COLOR = settings.UNSELECT_COLOR;

        //init all basic ones
        grid = (GridView) findViewById(R.id.gridview);
        list=new ArrayList<Grid>();

        //init selected list
        selectedlist=new ArrayList<Integer>();
        m_dialog = new ProgressDialog(this);

        list.add(new Grid(1, "Kanada","myself","abcd"));
        list.add(new Grid(2, "Hindi","myself","abcd"));
        list.add(new Grid(3, "Telugu","myself","abcd"));
        list.add(new Grid(4, "Tamil","myself","abcd"));

        //create and set adapter
        gridAdt = new GridAdapter(this, list);

        grid.setAdapter(gridAdt);


         Log.v("list", "="+list+gridAdt);

              /*
     * Spawn a GetListTask thread. This thread will get the data from the
     * server in the background, so as not to block our main (UI) thread.
     */
        if(haveNetworkConnection()) {
            (new GetListTask()).execute((Object)null);
        }
        else{
            Toast.makeText(getApplicationContext(),"Turn on you internet",Toast.LENGTH_LONG).show();
        }


    }

    //selecting from grid
    public void gridPicked(View view){
        Log.v("gridpicked", view.getTag().toString());
        String listid = view.getTag().toString();
        int id = (int)list.get(Integer.parseInt(listid)).getID();
        Log.v("list at","="+id);


        if(selectedlist.contains(id)){
            Log.v("id exists","="+id);
            selectedlist.remove(id);
            View tv = (View) grid.getChildAt(Integer.parseInt(listid));
            tv.setBackgroundColor(UNSELECT_COLOR);

        }
        else{
            Log.v("new id","="+id);
            selectedlist.add(id);

            View tv = (View) grid.getChildAt(Integer.parseInt(listid));
            tv.setBackgroundColor(Color.parseColor(SELECT_COLOR));
        }

    }
    //when user clicks next button
    public void selected(View view){

        if(haveNetworkConnection()) {
            if (selectedlist.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Select one language atleast", Toast.LENGTH_LONG).show();
            } else {
                Log.v("gridpicked", selectedlist.toString());
                Intent i = new Intent(this, SecondActivity.class);
                i.putExtra("selectedlist", selectedlist.toString());
                startActivity(i);

            }
        }
        else{
            Toast.makeText(getApplicationContext(),"Turn on you internet",Toast.LENGTH_LONG).show();
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    //method to retrieve additional languages
    public String getLangList() {
        Log.v("inside", "getlang list");

        /*
                 * Let's construct the query string. It should be a key/value pair. In
                 * this case, we just need to specify the command, so no additional
                 * arguments are needed.
                 */
        String data = null;
        try {
            data = "command=" + URLEncoder.encode("getlang", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return executeHttpRequest(data);
    }

    private class GetListTask extends AsyncTask {

        /**
         * Let's make the http request and return the result as a String.
         */
        protected String doInBackground(Object... args) {
            Log.v("inside","background");
            return getLangList();
        }
        protected void onPreExecute(){

            super.onPreExecute();
            // initialize the dialog
            m_dialog.setTitle("Searching...");
            m_dialog.setMessage("Please wait while searching...");
            m_dialog.setIndeterminate(true);
            m_dialog.setCancelable(true);
            m_dialog.show();
        }
        /**
         * Parse the String result, and create a new array adapter for the list
         * view.
         */
        protected void onPostExecute(Object objResult) {
            // check to make sure we're dealing with a string
            if(objResult != null) {

                String result = (String) objResult;
                Log.v("got result", result);
                if (result != "" || result != null) {
                    Log.v("result","is not null");
                    JSONParser parser = new JSONParser();
                    Object obj = null;
                    try {

                        try {
                            JSONArray jsonarr = new JSONArray(result);
                            //Log.v("jsonarr length","="+jsonarr.length());
                            String extraLangs = new String();
                            for (int i = 0; i < jsonarr.length(); i++) {

                                org.json.JSONObject jsonobj = jsonarr.getJSONObject(i);

                                // get lang's
                                String lang = jsonobj.getString("lang");
                                Log.v("lang", lang);
                                // adp.add(new Grid(0, lang,"myself","abcd"));
                                list.add(new Grid(5 + i, lang, "myself", "abcd"));
                                //xtraLangs. = lang;
                                //list.add(lang);
                            }

                            //adp.add(extraLangs);
                            gridAdt.notifyDataSetChanged();
                            Log.v("list =", list.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // now we'll supply the data structure needed by this ListActivity
                        //  ArrayAdapter<String> newAdapter = new ArrayAdapter<String>(mainActivity, R.layout.song_list, responseList);
                        //mainActivity.setListAdapter(newAdapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            // close the dialog
            m_dialog.dismiss();
        }

    }
    private static String executeHttpRequest(String data) {
        Log.v("inside","execute http");
        String result = "";
        try {
            URL url = new URL(SERVER_URL);
            URLConnection connection = url.openConnection();

            // We need to make sure we specify that we want to provide input and
            // get output from this connection. We also want to disable caching,
            // so that we get the most up-to-date result. And, we need to
            // specify the correct content type for our data.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Send the POST data
            DataOutputStream dataOut = new DataOutputStream(connection.getOutputStream());
            dataOut.writeBytes(data);
            dataOut.flush();
            dataOut.close();

            // get the response from the server and store it in result
            DataInputStream dataIn = new DataInputStream(connection.getInputStream());
            String inputLine;
            while ((inputLine = dataIn.readLine()) != null) {
                result += inputLine;
            }
            dataIn.close();
        } catch (IOException e) {
        /*
         * In case of an error, we're going to return a null String. This
         * can be changed to a specific error message format if the client
         * wants to do some error handling. For our simple app, we're just
         * going to use the null to communicate a general error in
         * retrieving the data.
         */
            e.printStackTrace();
            result = null;
            Log.v("inside","executehttp"+e);


        }

        return result;
    }
    // check data connection enabled or not
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}