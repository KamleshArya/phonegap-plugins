package com.phonegap.plugins.dropbox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

public class Dropbox extends Plugin {
    private static final String LOG_TAG = "DropboxPlugin";

    // Replace this with your consumer key and secret assigned by Dropbox.
    // Note that this is a really insecure way to do this, and you shouldn't
    // ship code which contains your key & secret in such an obvious way.
    // Obfuscation is good.
    final static private String CONSUMER_KEY = "xxx";
    final static private String CONSUMER_SECRET = "xxx";

    private DropboxAPI api = new DropboxAPI();

    final static public String ACCOUNT_PREFS_NAME = "prefs";
    final static public String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static public String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    private boolean mLoggedIn;
    private Config mConfig;

    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";
        
        try {
            if (action.equals("login")) {
                if (args.length() > 0) {
                    JSONObject obj = args.getJSONObject(0);
                    login(obj.getString("username"), obj.getString("password"));
                }
            }  
            else if (action.equals("logout")) {
            }
            else if (action.equals("accountInfo")) {
            }
            return new PluginResult(status, result);
        } catch (JSONException e) {
            e.printStackTrace();
            return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
        }
    }

    private void login(final String username, final String password) {
        final Dropbox me = this;
        if (api.isAuthenticated()) {
            Runnable runnable = new Runnable() {
                public void run() {
                    // If we're already authenticated, we don't need to get the login info
                    LoginAsyncTask loginTask = new LoginAsyncTask(me, null, null, me.getConfig());
                    loginTask.execute();            
                };
            };
            this.ctx.runOnUiThread(runnable);
        } else {
            Runnable runnable = new Runnable() {
                public void run() {
                    // It's good to do Dropbox API (and any web API) calls in a separate thread,
                    // so we don't get a force-close due to the UI thread stalling.
                    LoginAsyncTask loginTask = new LoginAsyncTask(me, username, password, me.getConfig());
                    loginTask.execute();
                };
            };
            this.ctx.runOnUiThread(runnable);
        }
        
    }

    
    protected Config getConfig() {
        if (mConfig == null) {
            mConfig = api.getConfig(null, false);
            // TODO On a production app which you distribute, your consumer
            // key and secret should be obfuscated somehow.
            mConfig.consumerKey=CONSUMER_KEY;
            mConfig.consumerSecret=CONSUMER_SECRET;
            mConfig.server="api.dropbox.com";
            mConfig.contentServer="api-content.dropbox.com";
            mConfig.port=80;
        }
        return mConfig;
    }
    
    public void setConfig(Config conf) {
        mConfig = conf;
    }
    
    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     * 
     * @return Array of [access_key, access_secret], or null if none stored
     */
    public String[] getKeys() {
        SharedPreferences prefs = this.ctx.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
            String[] ret = new String[2];
            ret[0] = key;
            ret[1] = secret;
            return ret;
        } else {
            return null;
        }
    }
    
    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    public void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = this.ctx.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }
    
    public void clearKeys() {
        SharedPreferences prefs = this.ctx.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }       

    /**
     * This lets us use the Dropbox API from the LoginAsyncTask
     */
    public DropboxAPI getAPI() {
        return api;
    }

    /**
     * Convenience function to change UI state based on being logged in
     */
    public void setLoggedIn(boolean loggedIn) {
        mLoggedIn = loggedIn;
    }
}
