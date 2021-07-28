package com.yusuffahrudin.masuyamobileapp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.yusuffahrudin.masuyamobileapp.HomeActivity;
import com.yusuffahrudin.masuyamobileapp.LoginActivity;

import java.util.HashMap;

/**
 * Created by yusuf fahrudin on 22-05-2017.
 */

public class SessionManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private DataHelper db;

    private static final String pref_name = "crudpref";
    private static final String is_login = "islogin";
    public static final String kunci_email = "keyemail";
    public static final String level = "level";
    public static final String kota = "kota";
    public static final String kdkota = "kdkota";
    public static final String underCost = "undercost";
    public static final String underBottomSP = "underbottomsp";
    public static final String underBottomSO = "underbottomso";

    @SuppressLint("CommitPrefEdits")
    public SessionManager(Context context) {
        this.context = context;
        db = new DataHelper(context);
        pref = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createSession(String user, String level, String kota, String kdkota, String underCost, String underBottomSP, String underBottomSO){
        editor.putBoolean(SessionManager.is_login, true);
        editor.putString(SessionManager.kunci_email, user);
        editor.putString(SessionManager.level, level);
        editor.putString(SessionManager.kota, kota);
        editor.putString(SessionManager.kdkota, kdkota);
        editor.putString(SessionManager.underCost, underCost);
        editor.putString(SessionManager.underBottomSP, underBottomSP);
        editor.putString(SessionManager.underBottomSO, underBottomSO);
        editor.commit();
    }

    public void checkLogin(){
        if (!this.is_login()){

            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }else {


            System.out.println("HERE");
            Intent i = new Intent(context, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public String checkloginforapk(){
        if (!this.is_login()){
            return "belum login";
        }
        else{
//            String msg = ;
            return "sudah login";
        }
    }

    private boolean is_login() {
        return pref.getBoolean(is_login, false);
    }

    public void logout(){
        editor.clear();
        editor.commit();
        Intent i = new Intent(context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();
        user.put(pref_name, pref.getString(pref_name, null));
        user.put(kunci_email, pref.getString(kunci_email, null));
        user.put(level, pref.getString(level, null));
        user.put(kota, pref.getString(kota, null));
        user.put(kdkota, pref.getString(kdkota, null));
        user.put(underCost, pref.getString(underCost, null));
        user.put(underBottomSP, pref.getString(underBottomSP, null));
        user.put(underBottomSO, pref.getString(underBottomSO, null));
        return user;
    }

    public void cekversiapk(){

    }


}
