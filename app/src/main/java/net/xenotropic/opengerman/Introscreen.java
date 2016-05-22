package net.xenotropic.opengerman;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.divideandconquer.Eula;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class Introscreen extends Activity {

	Activity mainActivity = this;
	MyDB dba;
	private String last_category_clicked = null;
	private Button last_button_clicked = null;
	private Button reverse_button = null;
	private View main_view = null;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		eulaCheck();
		SharedPreferences prefs = getSharedPreferences("quiz_storage", MODE_PRIVATE);
		String initialized = prefs.getString("initialized", "");
		String yes = "yes";
		if (!(yes.equals(initialized))) {
			//Log.w("flashcards oncreate", "calling showwaitfordb");
			//showWaitForDb();
			Editor e = prefs.edit();
			e.putString("intialized", "yes");
		}

		dba = new MyDB(this);
		dba.open();

		initializeDisplay();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	;

	private void styleButton(Button b) {
		// b.setPadding(left, top, right, bottom)
		b.setPadding(30, 0, 30, 10);
		b.setTextSize(18);

	}

	public void onPause() {
		dba.close();
		super.onPause();
	}

	public void onResume() {
		dba.open();
		if (last_category_clicked != null) {
			String score_for_category = dba.getScoreForCategory(last_category_clicked);
			last_button_clicked.setText(last_category_clicked + " (" + score_for_category + "%)");
		}
		super.onResume();
	}

	public void onDestroy() {
		dba.close();
		super.onDestroy();
	}

	private void initializeDisplay() {
		TableLayout table_layout = new TableLayout(this);
		TableRow top_row = new TableRow(this);
		table_layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		table_layout.setOrientation(LinearLayout.VERTICAL);

        TextView app_title = new TextView(this);
        app_title.setText(R.string.app_name);
        app_title.setTextSize(45);
        app_title.setTextColor(Color.WHITE);
        app_title.setPadding(0, 5, 0, 10);
        app_title.setGravity(Gravity.CENTER);
        app_title.setTypeface( null, Typeface.BOLD);

		TextView category_title = new TextView(this);
		category_title.setText(R.string.categories_text);
		category_title.setTextSize(20);
        category_title.setTextColor(Color.DKGRAY);
        category_title.setPadding(0, 5, 0, 10);
        category_title.setGravity(Gravity.CENTER);


        Drawable bkgnd_image = ContextCompat.getDrawable(getApplicationContext(), R.drawable.alexturm_vert);
		ImageView bk_view = new ImageView(getApplicationContext());
		bk_view.setImageDrawable(bkgnd_image);
		bk_view.setScaleType(ImageView.ScaleType.CENTER_CROP);

		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int screen_width = display.getWidth();
		int buttons_width = (int) (screen_width / 1.5);


		top_row.addView(category_title);
		top_row.setGravity(Gravity.CENTER);

		TableRow reverse_row = new TableRow(this);
		reverse_button = new Button(this);
		reverse_button.setOnClickListener(new ReverseListener(this));
		reverse_button.setText(R.string.reverse_button);
		styleButton(reverse_button);
		reverse_button.setGravity(Gravity.CENTER);
		reverse_button.setMaxWidth(buttons_width);
		reverse_row.setGravity(Gravity.CENTER);
		reverse_row.addView(reverse_button);

        table_layout.addView(app_title);
		table_layout.addView(reverse_row);
		table_layout.addView(top_row);
		String[] categories = dba.getCategories();

		for (int i = 0; i < categories.length; i++) {
			Button category_button = new Button(getApplicationContext());
			Button category_reset_button = new Button(getApplicationContext());
			category_button.setText(categories[i] + " (" + dba.getScoreForCategory(categories[i]) + "%)");
			category_button.setTag(categories[i]);
			category_button.setOnClickListener(new StartQuizListener(this, categories[i]));
			category_button.setMaxWidth(buttons_width);
			styleButton(category_button);
			category_reset_button.setText("Reset");
			category_reset_button.setOnClickListener(new ResetQuizListener(this, categories[i]));
			TableRow table_row = new TableRow(getApplicationContext());
			table_row.setGravity(Gravity.CENTER);
			table_row.addView(category_button);
			table_row.addView(category_reset_button);
			table_layout.addView(table_row);
		}

		ScrollView scroll_view = new ScrollView(getApplicationContext());
		FrameLayout frame_layout = new FrameLayout(getApplicationContext());
        frame_layout.addView( bk_view );
        frame_layout.addView( table_layout );
        scroll_view.addView( frame_layout );
		this.setContentView( scroll_view );
		main_view = scroll_view;
		SharedPreferences prefs = getSharedPreferences("quiz_storage", MODE_PRIVATE);
		String quiz_direction = prefs.getString("direction", "");
		if (quiz_direction == Constants.QUIZZED_REVERSE) setDirection(true, reverse_button);
		else setDirection(false, reverse_button);
	}

	private void showWaitForDb() {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.dbload);
		setProgressBarVisibility(true);
	}

	protected void startQuiz(String category, Button view_clicked) {
		last_button_clicked = view_clicked;
		last_category_clicked = category;
		Toast.makeText(getApplicationContext(),
				"Quizzing " + category + "!", Toast.LENGTH_SHORT).show();
		Intent startQuiz = new Intent(this, Quizzer.class);
		startQuiz.putExtra("category", category);
		startActivity(startQuiz);
	}

	protected void resetScore(String category) {
		dba.resetScore(category);
		Button b = (Button) main_view.findViewWithTag(category);
		b.setText(category + " (0%)");
	}

	protected void setDirection(boolean fwd_dir, Button rev_btn) {
		SharedPreferences prefs = getSharedPreferences("quiz_storage", MODE_PRIVATE);
		Editor e = prefs.edit();
		if (fwd_dir) {
			dba.setQuizDirection(Constants.QUIZZED_FORWARD);
			rev_btn.setText(R.string.reverse_button);
			e.putString("direction", Constants.QUIZZED_FORWARD);
		} else {
			dba.setQuizDirection(Constants.QUIZZED_REVERSE);
			rev_btn.setText(R.string.reverse_button_opposite);
			e.putString("direction", Constants.QUIZZED_REVERSE);
		}
		e.commit();
		rev_btn.invalidate();

		String[] categories = dba.getCategories();
		Button b;
		for (int i = 0; i < categories.length; i++) {
			b = (Button) main_view.findViewWithTag(categories[i]);
			b.setText(categories[i] + " (" + dba.getScoreForCategory(categories[i]) + "%)");
		}
		main_view.invalidate();
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Introscreen Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://net.xenotropic.opengerman/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Introscreen Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://net.xenotropic.opengerman/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}

	private class StartQuizListener implements OnClickListener {

		private String category;
		private Introscreen app;

		public StartQuizListener(Introscreen f, String c) {
			category = c;
			app = f;
		}

		public void onClick(View v) {
			app.startQuiz(category, (Button) v);

		}
	}

	;

	private class ResetQuizListener implements OnClickListener {

		private String category;
		private Introscreen app;

		private ResetQuizListener(Introscreen f, String c) {
			category = c;
			app = f;
		}

		public void onClick(View v) {
			new AlertDialog.Builder(app)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Reset " + category)
					.setMessage("Reset score for " + category + "?")
					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							app.resetScore(category);
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

						}
					})
					.show();
		}
	}

	private class ReverseListener implements OnClickListener {

		private boolean forward_direction = true;
		private Introscreen app;

		private ReverseListener(Introscreen f) {
			app = f;
		}

		public void onClick(View v) {
			app.setDirection(forward_direction, (Button) v);
			forward_direction = !forward_direction;
		}
	}

	private void eulaCheck() {
		Eula.show(this);
	}

}
