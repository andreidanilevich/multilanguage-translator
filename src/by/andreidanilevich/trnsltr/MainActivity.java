package by.andreidanilevich.trnsltr;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

	SharedPreferences mSettings;
	public static final String APP_PREFERENCES = "mysettings";
	public static final String APP_PREFERENCES_COUNTER1 = "lang_input";
	public static final String APP_PREFERENCES_COUNTER2 = "lang_output";
	String lang_input = "ru", lang_output = "en";

	private AdView adView;
	protected static final int RESULT_SPEECH_TO_TEXT = 100;

	RelativeLayout list_lay;
	ListView lv;
	EditText etUP, etDOWN; // поля ввода и вывода
	ImageView btn_lang_input, btn_lang_output; // кнопки языков
	TextToSpeech tts; // озвучка текста
	ProgressBar pb; // появляется на кнопке
	TranslateME tm; // класс переводчика

	String[] lang = new String[] { "EN (USA, UK)", "ES (Spain)", "IT (Italy)",
			"FR (France)", "DE (Germany)", "RU (Russian)", "KZ (Kazakh)",
			"UA (Ukrainian)" };
	String[] lang2 = new String[] { "en", "es", "it", "fr", "de", "ru", "kk",
			"uk" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// reclama--------------------------------------
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-7651167515650002/7611760174");
		adView.setAdSize(AdSize.BANNER);
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout_reklama);
		layout.addView(adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
		// -------------------------------------------------

		mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		if (mSettings.contains(APP_PREFERENCES_COUNTER1)) {
			lang_input = mSettings.getString(APP_PREFERENCES_COUNTER1, "ru");
		}
		if (mSettings.contains(APP_PREFERENCES_COUNTER2)) {
			lang_output = mSettings.getString(APP_PREFERENCES_COUNTER2, "en");
		}

		pb = (ProgressBar) findViewById(R.id.pb);
		btn_lang_output = (ImageView) findViewById(R.id.btn_lang_output);
		btn_lang_input = (ImageView) findViewById(R.id.btn_lang_input);
		btn_lang_set();

		list_lay = (RelativeLayout) findViewById(R.id.list_lay);
		lv = (ListView) findViewById(R.id.lv);
		etUP = (EditText) findViewById(R.id.etUP);
		etDOWN = (EditText) findViewById(R.id.etDOWN);
		etDOWN.setKeyListener(null); // запретим ввод в нижнее поле
	}

	public void btn_lang_set() { // покажем знаки языков
		// ------------ INPUT
		if (lang_input.equals("en")) {
			btn_lang_input.setImageResource(R.drawable.lang_en);
		}
		if (lang_input.equals("es")) {
			btn_lang_input.setImageResource(R.drawable.lang_es);
		}
		if (lang_input.equals("it")) {
			btn_lang_input.setImageResource(R.drawable.lang_it);
		}
		if (lang_input.equals("fr")) {
			btn_lang_input.setImageResource(R.drawable.lang_fr);
		}
		if (lang_input.equals("de")) {
			btn_lang_input.setImageResource(R.drawable.lang_de);
		}
		if (lang_input.equals("ru")) {
			btn_lang_input.setImageResource(R.drawable.lang_ru);
		}
		if (lang_input.equals("kk")) {
			btn_lang_input.setImageResource(R.drawable.lang_kz);
		}
		if (lang_input.equals("uk")) {
			btn_lang_input.setImageResource(R.drawable.lang_ua);
		}
		// ------------ OUTPUT
		if (lang_output.equals("en")) {
			btn_lang_output.setImageResource(R.drawable.lang_en);
		}
		if (lang_output.equals("es")) {
			btn_lang_output.setImageResource(R.drawable.lang_es);
		}
		if (lang_output.equals("it")) {
			btn_lang_output.setImageResource(R.drawable.lang_it);
		}
		if (lang_output.equals("fr")) {
			btn_lang_output.setImageResource(R.drawable.lang_fr);
		}
		if (lang_output.equals("de")) {
			btn_lang_output.setImageResource(R.drawable.lang_de);
		}
		if (lang_output.equals("ru")) {
			btn_lang_output.setImageResource(R.drawable.lang_ru);
		}
		if (lang_output.equals("kk")) {
			btn_lang_output.setImageResource(R.drawable.lang_kz);
		}
		if (lang_output.equals("uk")) {
			btn_lang_output.setImageResource(R.drawable.lang_ua);
		}
	}

	public void btn_record(View v) { // распознавание речи старт и проверка
		if (tts != null) {
			tts.stop();
			tts.shutdown();
			tts = null;
		}
		Intent speechIntent = new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak please");
		try {
			startActivityForResult(speechIntent, RESULT_SPEECH_TO_TEXT);
		} catch (ActivityNotFoundException a) { // распознование не
												// поддерживается
			Toast toast = Toast.makeText(getApplicationContext(),
					"speech to text not supported on this device..",
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
		}
	}

	@Override
	// распознование речи - рузультат
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RESULT_SPEECH_TO_TEXT && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			etUP.setText(matches.get(0));
		}
	}

	public void btn_speak_UP(View v) { // нажата кнопка UP
		speak_now("etUP");
	}

	public void btn_speak_DOWN(View v) { // нажата кнопка DOWN
		speak_now("etDOWN");
	}

	public void speak_now(final String up_or_down) { // синтез речи

		if (tts != null) {
			tts.shutdown();
			tts = null;
		}

		tts = new TextToSpeech(this, new OnInitListener() {
			Locale loc;

			@Override
			public void onInit(int status) { // инициализация
				if (status == TextToSpeech.SUCCESS) {

					pb.setVisibility(View.VISIBLE);

					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							pb.setVisibility(View.INVISIBLE);

						}
					}, 1500);

					tts.setEngineByPackageName("com.svox.pico");

					if (up_or_down.equals("etDOWN")) {
						// если нижнее поле
						if (lang_output.equals("en")) {
							loc = Locale.ENGLISH;
						}
						if (lang_output.equals("fr")) {
							loc = Locale.FRANCE;
						}
						if (lang_output.equals("it")) {
							loc = Locale.ITALIAN;
						}
						if (lang_output.equals("es")) {
							loc = new Locale("spa", "ESP");
						}
						if (lang_output.equals("de")) {
							loc = Locale.GERMAN;
						}
						if (lang_output.equals("ru")) {
							loc = new Locale("ru", "RU");
						}
						if (lang_output.equals("kk")) {
							loc = new Locale("kk", "KZ");
						}
						if (lang_output.equals("uk")) {
							loc = new Locale("ukr", "UK");
						}
					} else { // если верхнее поле
						if (lang_input.equals("en")) {
							loc = Locale.ENGLISH;
						}
						if (lang_input.equals("fr")) {
							loc = Locale.FRANCE;
						}
						if (lang_input.equals("it")) {
							loc = Locale.ITALIAN;
						}
						if (lang_input.equals("es")) {
							loc = new Locale("spa", "ESP");
						}
						if (lang_input.equals("de")) {
							loc = Locale.GERMAN;
						}
						if (lang_input.equals("ru")) {
							loc = new Locale("ru", "RU");
						}
						if (lang_input.equals("kk")) {
							loc = new Locale("kk", "KZ");
						}
						if (lang_input.equals("uk")) {
							loc = new Locale("ukr", "UK");
						}
					}

					int res_tts = tts.setLanguage(loc);
					if (res_tts == TextToSpeech.LANG_MISSING_DATA
							|| res_tts == TextToSpeech.LANG_NOT_SUPPORTED) { // ошибка_этого_языка

						Toast toast = Toast
								.makeText(
										getApplicationContext(),
										"this language is not available on your device ....",
										Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
						toast.show();

						if (tts != null) {
							tts.shutdown();
							tts = null;
						}

					} else {
						tts.setPitch(7 / 10);
						tts.setSpeechRate(1);

						if (up_or_down.equals("etDOWN")) {
							// если нижнее поле
							tts.speak(etDOWN.getText().toString(),
									TextToSpeech.QUEUE_FLUSH, null);
						} else { // если врехнее поле
							tts.speak(etUP.getText().toString(),
									TextToSpeech.QUEUE_FLUSH, null);
						}

					}
				} else { // текст то голос не поддерживается
					Toast toast = Toast.makeText(getApplicationContext(),
							"text to speech not supported on your device..",
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					toast.show();
					if (tts != null) {
						tts.shutdown();
						tts = null;
					}
				}
			}
		});
	}

	public void btn_tr_it(View v) { // кнопка перевести!

		if (tts != null) {
			tts.shutdown();
		}

		if (pb.getVisibility() != View.VISIBLE) { // если проверка не происходит
													// в настоящий момент
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			if (!etUP.getText().toString().trim().equals("")) {

				if (networkInfo != null && networkInfo.isConnected()) {// интернет
																		// есть
					String user_text = "";
					String temp_text = etUP.getText().toString()
							.trim()
							// _замены_символов+++++++++++++++++++++++!!!!!
							.replace("^", "").replace("«", "").replace("»", "")
							.replace("&", "").replace("\"", "")
							.replace("'", "");

					try { // переведем источник в utf-8 для линка

						user_text = URLEncoder.encode(temp_text.toString(),
								"UTF-8");
						if (user_text != null) { // текст есть
							pb.setVisibility(View.VISIBLE);
							// формируем и передаем линк
							// =======================>>>>>>>
							tm = new TranslateME();
							tm.execute("http://translate.google.com/translate_a/single?client=t"
									+ "&sl=" + lang_input // язык источника
									+ "&tl=" + lang_output // язык результата
									+ "&dt=t&q=" + user_text);

						} else { // текста нет
							Toast toast = Toast.makeText(
									getApplicationContext(),
									"nothing to translate..",
									Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
							toast.show();
						}
					} catch (Exception e) { // ошибка кодировки
						Toast toast = Toast.makeText(getApplicationContext(),
								"error check the input text..",
								Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
						toast.show();
					}
				} else { // интернета нет
					Toast toast = Toast.makeText(getApplicationContext(),
							"error internet connection..", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					toast.show();
				}
			}
		}
	}

	// перевод онлайн
	private class TranslateME extends AsyncTask<String, Void, String> {

		String ansver, temp;
		HttpURLConnection conn;

		@Override
		protected String doInBackground(String... urls) {

			try { // установим соединение
				URL url = new URL(urls[0]);
				conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000);
				conn.setConnectTimeout(15000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("User-Agent", "Mozilla/5.0");
				conn.setDoInput(true);
				conn.connect();
			} catch (Exception e) { // ошибка интернета
				tm.cancel(true);
				Log.i("chat", "ошибка подключения = " + e.getMessage());
			}

			try { // получим результат в потоке
				InputStream is = conn.getInputStream();
				// Reader rd = new InputStreamReader(is, "UTF-8");
				// char[] buffer = new char[3000];
				// rd.read(buffer);
				// temp = new String(buffer);

				BufferedReader br = new BufferedReader(new InputStreamReader(
						is, "UTF-8"));
				StringBuilder sb = new StringBuilder();
				String bfr_st = null;
				while ((bfr_st = br.readLine()) != null) {
					sb.append(bfr_st);
				}

				temp = sb.toString();

				is.close();
			} catch (Exception e) { // ошибка данных
				tm.cancel(true);
				Log.i("chat", "ошибка данных = " + e.getMessage() + "\n" + temp);
			}

			if (isCancelled())
				return null; // проверим на отмену !!!
			// ----------------------------------------------------------------------------------------------------------------------
			ansver = temp.toString().trim();
			List<Integer> massiv = new ArrayList<Integer>();

			try { // занесем в массив все номера кавычек из ответа
				for (int i = 0; i < temp.length(); i++) {
					char myChar = temp.charAt(i);
					if (Character.toString(myChar).equals("\"")) {
						massiv.add(i);
					} // получим номера кавычек
				}
			} catch (Exception e) {
				tm.cancel(true);
			}

			if (isCancelled())
				return null; // проверим на отмену !!!
			// -------------------------------------------------------------------------------------------------------------
			try { // формируем валидный ответ

				StringBuilder temp_ansver = new StringBuilder();
				if (massiv != null) {

					for (int i = 0; i < massiv.size() - 2; i = i + 4) {

						String valid_string = temp.substring(massiv.get(i) + 1,
								massiv.get(i + 1));

						if (valid_string.substring(valid_string.length() - 2,
								valid_string.length()).equals("\\n")) {
							valid_string = valid_string.substring(0,
									valid_string.length() - 2);

						}

						temp_ansver.append(valid_string.replace("\\n", "")
								+ "\n");
					}
				}
				// уберем последний перенос
				ansver = temp_ansver.toString().substring(0,
						temp_ansver.length() - 1);

			} catch (Exception e) {
				tm.cancel(true);
			}
			// -------------------------------------------------------------------------------------------------------------
			if (isCancelled())
				return null; // проверим на отмену !!!
			return ansver;
		}

		protected void onCancelled() { // если была ошибка и отмена
			Toast toast = Toast.makeText(getApplicationContext(),
					"transmission error..", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
			etDOWN.setText("");
			pb.setVisibility(View.INVISIBLE);
		}

		protected void onPostExecute(String result) { // получен какойто
														// результат
			if (result != null) { // если не пустой )))
				etDOWN.setText(result);
			} else { // если пустой (((
				etDOWN.setText("");
				Toast toast = Toast.makeText(getApplicationContext(),
						"no result..", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();
			}
			pb.setVisibility(View.INVISIBLE);
		}
	}

	public void btn_clear(View v) {
		if (tts != null) {
			tts.shutdown();
		}
		etUP.setText("");
		etDOWN.setText("");
	}

	public void btn_paste(View v) { // кнопка вставить из буффера
		ClipboardManager paste = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		if (paste.hasText()) {
			Toast toast = Toast.makeText(getApplicationContext(), "inserted..",
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
			etUP.setText(paste.getText().toString().trim()
					// _замены_символов+++++++++++++++++++++++!!!!!
					.replace("^", "").replace("«", "").replace("»", "")
					.replace("&", "").replace("\"", "").replace("'", ""));
		}
	}

	public void btn_copy(View v) { // кнопка копировать в буффер
		ClipboardManager copy = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		copy.setText(etDOWN.getText().toString());
		if (copy.hasText()) {
			Toast toast = Toast.makeText(getApplicationContext(), "copied..",
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
		}
	}

	public void btn_share(View v) { // кнопка расшарить
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, "Origin (" + lang_input + "): "
				+ etUP.getText().toString() + "\n- - - - -\nResult ("
				+ lang_output + "): " + etDOWN.getText().toString());
		share.putExtra(Intent.EXTRA_SUBJECT, "The translation result:");
		startActivity(Intent.createChooser(share, "The translation result:"));
	}

	public void btn_lang_input(View v) { // кнопка выбора языка источника
		if (tts != null) {
			tts.shutdown();
		}
		ArrayAdapter<String> lang_adapter = new ArrayAdapter<String>(this,
				R.layout.lv, lang);
		lv.setAdapter(lang_adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				list_lay.setVisibility(View.INVISIBLE);
				lang_input = lang2[position];
				etDOWN.setText("");
				btn_tr_it(null);
				write_lang();
			}
		});
		list_lay.setVisibility(View.VISIBLE);
	}

	public void btn_lang_output(View v) {// кнопка выбора языка перевода
		if (tts != null) {
			tts.shutdown();
		}
		ArrayAdapter<String> lang_adapter = new ArrayAdapter<String>(this,
				R.layout.lv, lang);
		lv.setAdapter(lang_adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				list_lay.setVisibility(View.INVISIBLE);
				lang_output = lang2[position];
				etDOWN.setText("");
				btn_tr_it(null);
				write_lang();
			}
		});
		list_lay.setVisibility(View.VISIBLE);
	}

	public void lv_close(View v) {
		list_lay.setVisibility(View.INVISIBLE);
	}

	protected void onPause() { // при выходе оборвем звук
		super.onPause();
		if (tts != null) {
			tts.shutdown();
		}
	}

	public void onBackPressed() {
		// если открыт выбор языка - закроем его
		if (list_lay.getVisibility() == View.VISIBLE) {
			list_lay.setVisibility(View.INVISIBLE);
		} else {
			// если воспроизводиться текст - оборвем
			if (tts != null) {
				tts.shutdown();
				tts = null;
			} else { // значит на выход
				AlertDialog.Builder quitDialog = new AlertDialog.Builder(
						MainActivity.this);
				quitDialog.setMessage("Exit program?").setCancelable(true);
				quitDialog.setPositiveButton("OK", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
				quitDialog.show();
			}
		}
	}

	public void btn_reverse(View v) {
		if (tts != null) {
			tts.shutdown();
		}
		String tmp_l = lang_input;
		lang_input = lang_output;
		lang_output = tmp_l;
		etUP.setText(etDOWN.getText().toString());
		write_lang();
		btn_tr_it(null);
	}

	public void write_lang() { // запишем языки
		Editor editor = mSettings.edit();
		editor.putString(APP_PREFERENCES_COUNTER1, lang_input);
		editor.putString(APP_PREFERENCES_COUNTER2, lang_output);
		editor.commit();
		btn_lang_set();
	}
}
