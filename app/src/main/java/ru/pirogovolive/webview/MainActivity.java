package ru.pirogovolive.webview;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
//TODO update AdView library
import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class MainActivity extends Activity {
	WebView wv;
	String adv_string;
	private AdView adView;
	String THIS_FILE="MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		adView = (AdView) this.findViewById(R.id.adView);
		(new Thread() {
			public void run() {
				Looper.prepare();
				adView.loadAd(new AdRequest());
			}
		}).start();
		adv_string = setAdv();
		wv = (WebView) findViewById(R.id.webView1);
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				//Log.d(THIS_FILE, "start shouldOverrideUrlLoading");
				view.loadUrl(url);
				return true;
			}
		});
		WebSettings settings = wv.getSettings();
		settings.setDefaultTextEncodingName("utf-8");
		wv.getSettings().setBuiltInZoomControls(true);
		//Log.d(THIS_FILE, "loading index.html");
		wv.loadUrl("file:///android_asset/index.htm");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		// Destroy the AdView.
		if (adView != null) {
			adView.destroy();
		}

		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (wv.isFocused() && wv.canGoBack()) {
			wv.goBack();
		} else {
			super.onBackPressed();
			finish();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}

	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.id.action_search:
				search();
				return true;
		}
		return true;
	}

	private LinearLayout container;
	private Button nextButton, closeButton;
	private EditText findBox;
	private boolean initialSearch = true;
	//search
	public void search(){

	container = (LinearLayout)findViewById(R.id.layoutId);


		nextButton = new Button(this);
    nextButton.setText("Search");
    nextButton.setOnClickListener(new View.OnClickListener(){

		@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
		@Override
		public void onClick(View v){
			if (initialSearch) {
				wv.findAllAsync(findBox.getText().toString());
				nextButton.setText("Next");
				initialSearch = false;
			} else {
				wv.findNext(true);
			}
		}
	});
    container.addView(nextButton);

	closeButton = new Button(this);
    closeButton.setText("Close");
    closeButton.setOnClickListener(new View.OnClickListener(){
		@Override
		public void onClick(View v){
			container.removeAllViews();
			wv.clearMatches();

		}
	});
    container.addView(closeButton);

	findBox = new EditText(this);
findBox.setMinEms(30);
findBox.setSingleLine(true);
findBox.setHint("Search");

findBox.setOnKeyListener(new View.OnKeyListener(){
		public boolean onKey(View v, int keyCode, KeyEvent event){
			if((event.getAction() == KeyEvent.ACTION_DOWN) && ((keyCode == KeyEvent.KEYCODE_ENTER))){
				wv.findAll(findBox.getText().toString());

				try{
					Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
					m.invoke(wv, true);
				}catch(Exception ignored){}
			}
			return false;
		}
	});

container.addView(findBox);
}

	static String FileAdv = "/data/mppss/adv";
	static String adr = "http://pirogovolive.ru/and_ad/index.php?app=3";

	String adv_from_file;
	String[] advert_serv;

	private String setAdv() {
		// TextView adv = (TextView) namepopuoLayout.findViewById(R.id.textadv);
		TextView adv = (TextView) findViewById(R.id.adv_text_los);
		String[] adv1 = getNewAdvFromServer();// Advertize_block(adv_from_file);
		// Log.d("Notification", "adv1=" + adv1.toString() + " adv01" +
		// adv1[0]);

		if (adv1[0] != "") {
			adv.setText(Html.fromHtml(adv1[0]));
		} else {
			adv.setText(Html
					.fromHtml("<a href='http://pirogovolive.ru/content/zdes-mogla-byt-vasha-reklama'>Здесь могла бы быть ваша реклама</a>"));
		}
		adv.setMovementMethod(LinkMovementMethod.getInstance());
		return adv1[0];
	}

	public String[] getNewAdvFromServer() {
		// ����������� � ������� ��� ��������� ����������
		new RequestTask().execute(adr);
		// ���������� ������ �� �����
		adv_from_file = ReadFile(FileAdv).toString();
		// Log.d("Notification","rff=" + adv_from_file + " size=" +
		// adv_from_file.length());
		String[] adv = Advertize_block();
		return adv;

	}

	public String[] Advertize_block() {
		String[] advert_file = adv_from_file.split("\\|\\|");
		Random r = new Random();
		int li = r.nextInt(advert_file.length);

		String[] advert_one = advert_file[li].split("\\|");
		return advert_one;
	}

	// �������� ������ ����� � ������ � ���� ��������� ����������
	private void WriteToFile(String message, String file, String dir) {
		FileWriter f;
		if (dir != "") {
			File advdir = new File(Environment.getExternalStorageDirectory()
					+ dir);
			advdir.mkdir();
		}
		try {
			f = new FileWriter(Environment.getExternalStorageDirectory() + file);

			f.write(message);
			f.flush();
			f.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private StringBuilder ReadFile(String file_adr) {

		File sdcard = Environment.getExternalStorageDirectory();
		// Get the text file
		File file = new File(sdcard, file_adr);

		// Read text from file
		StringBuilder text = new StringBuilder();

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				text.append(line);
				// text.append('\n');
			}
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}
		return text;
	}

	class RequestTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... uri) {

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			String responseString = null;

			try {
				response = httpclient.execute(new HttpGet(uri[0]));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();

					Log.d("Notification", "res=" + responseString + " size="
							+ responseString.length());

					if (!adv_from_file.equals(responseString)) {
						WriteToFile(responseString, FileAdv, "/data/mppss/");
						// Log.d("Notification", "samesame but diff");
						adv_from_file = ReadFile(FileAdv).toString();

					}

				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				// TODO Handle problems..
			} catch (IOException e) {
				// TODO Handle problems..
			}
			return responseString;
		}
	}
}
