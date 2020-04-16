package com.example.translation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.MalformedURLException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTranslateClick(View view) {
        EditText e1 = (EditText) findViewById(R.id.editText);
        if (!isEmpty(e1)) {
            Toast.makeText(this, "Getting Translations", Toast.LENGTH_SHORT).show();
//            new SaveTheFeed().execute();
            new GetXMLData().execute();
        } else {
            Toast.makeText(this, "Please enter valid word to translate", Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean isEmpty(EditText e1) {
        return e1.getText().toString().trim().length() == 0;
    }

    class SaveTheFeed extends AsyncTask<Void, Void, Void> {
        String jsonString = "";
        String result = "";


        @Override
        protected Void doInBackground(Void... voids) {

            EditText translateEditText = (EditText) findViewById(R.id.editText);
            String wordsToTranslate = translateEditText.getText().toString();
            wordsToTranslate = wordsToTranslate.replace(" ", "+");
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost("http://newjustin.com/translateit.php?action=translations&english_words="
                    + wordsToTranslate);

            httpPost.setHeader("Content-type", "application/json");
            InputStream inputStream = null;
            try {
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                jsonString = sb.toString();
                JSONObject jObject = new JSONObject(jsonString);
                JSONArray jArray = jObject.getJSONArray("translations");
                outputTranslations(jArray);


            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText(result);
        }

        protected void outputTranslations(JSONArray jArray) {
            String [] languages = {"arabic", "chinese", "danish", "dutch", "french", "german", "italian", "portuguese", "russian", "spanish"};
            try {
                for(int i=0; i<jArray.length(); i++) {
                    JSONObject translationObject = jArray.getJSONObject(i);
                    result = result + languages[i] + ":" + translationObject.getString(languages[i]) + "\n";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class GetXMLData extends AsyncTask<Void, Void, Void> {

        String stringToPrint = "";
        @Override
        protected Void doInBackground(Void... voids) {
            String xmlString = "";
            String wordsToTranslate = "";
            EditText editText = (EditText) findViewById(R.id.editText);
            wordsToTranslate = editText.getText().toString();
            wordsToTranslate = wordsToTranslate.replace(" ", "+");
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost("http://newjustin.com/translateit.php?action=translations&english_words=" + wordsToTranslate);
            httpPost.setHeader("Content-type", "text/xml");
            InputStream inputStream = null;

            try {
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = reader.readLine())!=  null){
                    sb.append(line);
                }
                xmlString = sb.toString();
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(xmlString));
                int eventType = xpp.getEventType();
                while(eventType != XmlPullParser.END_DOCUMENT) {
                    if((eventType == XmlPullParser.START_TAG) && (!xpp.getName().equals("translations"))){
                        stringToPrint = stringToPrint + xpp.getName();
                    } else if(eventType == XmlPullParser.TEXT) {
                        stringToPrint = stringToPrint + xpp.getText() + "\n";
                    }
//                    tells to go to next element in XML doc
                    eventType = xpp.next();
                }

            }catch (MalformedURLException e){
                e.printStackTrace();
            } catch(UnsupportedEncodingException e){
                e.printStackTrace();
            }catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText(stringToPrint);
        }
    }


}
