package com.pluralsight.candycoded;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pluralsight.candycoded.DB.CandyContract;
import com.pluralsight.candycoded.DB.CandyCursorAdapter;
import com.pluralsight.candycoded.DB.CandyDbHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {
  private Candy[] candies;
  private CandyDbHelper candyDbHelper = new CandyDbHelper(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    SQLiteDatabase db = candyDbHelper.getWritableDatabase();
    Cursor cursor = db.rawQuery("SELECT * FROM candy", null);

    final CandyCursorAdapter adapter = new CandyCursorAdapter(this, cursor);
    ListView listView = (ListView)this.findViewById(R.id.list_view_candy);

    listView.setAdapter(adapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
        detailIntent.putExtra("position", i);
        startActivity(detailIntent);
      }
    });

    AsyncHttpClient client = new AsyncHttpClient();
    client.get("https://vast-brushlands-23089.herokuapp.com/main/api", new TextHttpResponseHandler() {
          @Override
          public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
            Log.e("AsyncHttpClient", "response = " + response);
          }

          @Override
          public void onSuccess(int statusCode, Header[] headers, String response) {
            Log.d("AsyncHttpClient", "response = " + response);
            Gson gson = new GsonBuilder().create();;
            candies = gson.fromJson(response, Candy[].class);

            addCandiesToDatabase(candies);

            SQLiteDatabase db = candyDbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM candy", null);
            //adapter.changeCursor(cursor);
          }
        });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return true;
  }
  // ***
  // TODO - Task 1 - Show Store Information Activity
  // ***

  private void addCandiesToDatabase(Candy[] candies) {
    SQLiteDatabase db = candyDbHelper.getWritableDatabase();

    for (Candy candy : candies) {
      ContentValues values = new ContentValues();
      values.put(CandyContract.CandyEntry.COLUMN_NAME_NAME, candy.name);
      values.put(CandyContract.CandyEntry.COLUMN_NAME_PRICE, candy.price);
      values.put(CandyContract.CandyEntry.COLUMN_NAME_DESC, candy.description);
      values.put(CandyContract.CandyEntry.COLUMN_NAME_IMAGE, candy.image);

      db.insert(CandyContract.CandyEntry.TABLE_NAME, null, values);
    }
  }

  /****
   * This Overrides the Toolbar menu and launches the info activity
   * when an item is clicked.
   */
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    Intent infoIntent = new Intent(this,InfoActivity.class);
    startActivity(infoIntent);
    return super.onOptionsItemSelected(item);
  }
}
