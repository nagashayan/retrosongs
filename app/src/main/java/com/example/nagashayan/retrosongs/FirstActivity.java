package com.example.nagashayan.retrosongs;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    List<String> list;
    ArrayAdapter<String> adp;
    ProgressDialog m_dialog;
    private static String SERVER_URL = "http://10.0.2.2/langlist.php";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        //rl=(RelativeLayout) findViewById(R.id.rl);
        //grid =new GridView(this);
        grid = (GridView) findViewById(R.id.gridview);
        list=new ArrayList<String>();

        list.add("Kannada");
        list.add("Hindi");
        list.add("Telugu");
        list.add("Tamil");


        adp=new ArrayAdapter<String> (this,android.R.layout.simple_dropdown_item_1line,list);
        grid.setNumColumns(2);
        grid.setBackgroundColor(Color.CYAN);


        grid.setAdapter(adp);



        //rl.addView(grid);

         Log.v("list", list.toString());
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub

                Toast.makeText(getBaseContext(), list.get(arg2),
                        Toast.LENGTH_SHORT).show();
            }
        });
        m_dialog = new ProgressDialog(this);
              /*
     * Spawn a GetListTask thread. This thread will get the data from the
     * server in the background, so as not to block our main (UI) thread.
     */
        (new GetListTask()).execute((Object)null);
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

                String result =   (String) objResult;
                Log.v("got result",result);
                JSONParser parser=new JSONParser();
                Object obj = null;
                try {

                    try {
                        JSONArray jsonarr = new JSONArray(result);
                        //Log.v("jsonarr length","="+jsonarr.length());
                        String extraLangs = new String();
                        for(int i = 0; i < jsonarr.length(); i++){

                            org.json.JSONObject jsonobj = jsonarr.getJSONObject(i);

                            // get lang's
                            String lang=jsonobj.getString("lang");
                            Log.v("lang",lang);
                            adp.add(lang);
                            //xtraLangs. = lang;
                            //list.add(lang);
                        }
                        //adp.add(extraLangs);
                        adp.notifyDataSetChanged();
                        Log.v("list =",list.toString());
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
}