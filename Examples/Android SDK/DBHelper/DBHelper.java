package DBHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "foo.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TAG = DBHelper.class.getSimpleName();

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(TAG, "constructor");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS TestTable(id int NOT NULL, text varchar(40))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Update database from version  " + oldVersion
                + " to " + newVersion + ", which remove all old records");
		onCreate(db);
	}

}