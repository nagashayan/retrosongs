package com.example.nagashayan.retrosongs;

import android.content.Context;
import android.util.Log;
import android.widget.MediaController;

/**
 * Created by root on 28/2/15.
 */

public class MusicController extends MediaController {

    public MusicController(Context c){
        super(c);
    }

    public void hide(){}
   public void show(){
       super.show();
       Log.v("music controller", "in show");
   }
}
