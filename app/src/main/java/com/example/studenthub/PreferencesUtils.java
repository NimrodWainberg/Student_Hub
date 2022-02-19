package com.example.studenthub;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class  PreferencesUtils {
private String  FILE_NAME;
private SharedPreferences  mPrefs;

private String KEY_TOKENS = "key_tokens";
private String KEY_MY_TOKEN = "key_my_token";

        public PreferencesUtils(Context context) {
           // mPrefs = context.getSharedPreferences(Context.MODE_PRIVATE);
        }

       public  void  addNewToken(String token) {
        List<String> tokens = getTokens();
        if (!tokens.contains(token)) {
            tokens.add(token);
        }
         saveTokens(tokens);
        }

        public void  saveTokens(List<String> tokens) {
            String input = "";
            for ( String t : tokens) {
                input += t + "|";
            }
            mPrefs.edit().putString(KEY_TOKENS, input).apply();
        }

        public List<String> getTokens() {
            String tokensString = mPrefs.getString(KEY_TOKENS, "");
            if (tokensString != null && !tokensString.equals("")) {
                return new ArrayList<>(Arrays.asList(tokensString.split("|")));
            }
            return null;
        }

        void  saveMyToken(String token) {
             mPrefs.edit().putString(KEY_MY_TOKEN, token).apply();
        }

        public String  getMyToken()  {
        String  token = mPrefs.getString(KEY_MY_TOKEN, null);
          if(token != null && !token.isEmpty()) {
                    return token;
                }else {
              return null;
          }
        }
        }