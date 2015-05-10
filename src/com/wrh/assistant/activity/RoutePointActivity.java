package com.wrh.assistant.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView.LayoutParams;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wrh.assistant.R;
import com.wrh.assistant.model.LPoint;
import com.wrh.assistant.utils.DBHelper;

public class RoutePointActivity extends Activity implements OnClickListener {
	private Context mContext = null;
	private DBHelper mDBHelper = null;
	private ImageView mBackUp = null;
	private EditText mPointCityEdt = null;
	private EditText mPointAddrEdt = null;
	private Button mSureBtn = null;
	private Button mLocationBtn = null;
	private ListView mLocationList = null;
	private PointAdapter mAdapter = null;
	// private BDLocation mLocation = null;
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_CITY = "city";
	public static final int RSP_CODE_LOCATION = 10;
	public static final int RSP_CODE_MY_LOCATON = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_point);

		mContext = this;
		mDBHelper = new DBHelper(mContext);
		getData();
		initViews();
	}

	private void getData() {
		// Intent i = getIntent();

		// BDLocation location =
		// i.getParcelableExtra(RouteActivity.KEY_LOCATION);
		// if (location != null) {
		// Log.i("wrh", "routePointActivity location is not null");
		// mLocation = location;
		// }

	}

	private void initViews() {
		mBackUp = (ImageView) findViewById(R.id.routePointBackUp);
		mPointCityEdt = (EditText) findViewById(R.id.pointCityEdt);
		mPointAddrEdt = (EditText) findViewById(R.id.pointAddrEdt);
		mSureBtn = (Button) findViewById(R.id.routePointSureBtn);
		mLocationBtn = (Button) findViewById(R.id.myLocation);
		mLocationList = (ListView) findViewById(R.id.locationList);

		mBackUp.setOnClickListener(this);
		mSureBtn.setOnClickListener(this);
		mLocationBtn.setOnClickListener(this);

		Cursor c = mDBHelper.query();
		mAdapter = new PointAdapter(mContext, R.layout.location_list_item, c);
		TextView footView = new TextView(mContext);
		LayoutParams params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		footView.setText("清除所有");
		footView.setGravity(Gravity.CENTER);
		footView.setPadding(0, 20, 0, 20);
		mLocationList.addFooterView(footView);
		mLocationList.setAdapter(mAdapter);
		footView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDBHelper.delete();
				mAdapter.changeCursor(mDBHelper.query());
				Toast.makeText(mContext, "删除成功！", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 释放数据库资源
		mDBHelper.Close();
	}

	private void addToDB(String city, String address) {
		String selection = "city=? and address=?";
		String[] selectionArgs = new String[] { city, address };
		Cursor c = mDBHelper.query(null, selection, selectionArgs, null, null,
				null);
		if (c.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put(DBHelper.CITY, city);
			values.put(DBHelper.ADDRESS, address);
			mDBHelper.insert(values);
		}
	}

	@Override
	public void onClick(View v) {
		Intent i;
		switch (v.getId()) {
		case R.id.routePointBackUp:
			finish();
			break;
		case R.id.routePointSureBtn:
			if (TextUtils.isEmpty(mPointCityEdt.getText())) {
				Toast.makeText(mContext, "请输入城市名称", Toast.LENGTH_SHORT).show();
				return;
			}
			if (TextUtils.isEmpty(mPointAddrEdt.getText())) {
				Toast.makeText(mContext, "请输入地址", Toast.LENGTH_SHORT).show();
				return;
			}
			String city = mPointCityEdt.getText().toString().trim();
			if (!city.endsWith("市")) {
				city = city + "市";
			}
			String address = mPointAddrEdt.getText().toString().trim();
			addToDB(city, address);
			i = new Intent();
			i.putExtra(KEY_CITY, city);
			i.putExtra(KEY_ADDRESS, address);
			setResult(RSP_CODE_LOCATION, i);
			finish();
			break;
		case R.id.myLocation:
			setResult(RSP_CODE_MY_LOCATON);
			finish();
			break;

		default:
			break;
		}
	}

	public class PointAdapter extends CursorAdapter {

		private int resourceId;
		private List<LPoint> datas = new ArrayList<LPoint>();

		public PointAdapter(Context context, int resource, Cursor c) {
			super(context, c);
			resourceId = resource;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			ViewHolder holder = new ViewHolder();
			View v = LayoutInflater.from(context).inflate(resourceId, null);
			holder.lPointBtn = (Button) v.findViewById(R.id.LPoint);
			v.setTag(holder);
			return v;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();
			final LPoint point = new LPoint();
			String city = cursor
					.getString(cursor.getColumnIndex(DBHelper.CITY));
			String address = cursor.getString(cursor
					.getColumnIndex(DBHelper.ADDRESS));
			point.setCity(city);
			point.setAddress(address);
			datas.add(point);
			holder.lPointBtn.setText(address);
			holder.lPointBtn.setOnClickListener(null);
			holder.lPointBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String city = point.getCity();
					String address = point.getAddress();
					mPointCityEdt.setText(city);
					mPointAddrEdt.setText(address);
					Intent i = new Intent();
					i.putExtra(KEY_CITY, city);
					i.putExtra(KEY_ADDRESS, address);
					setResult(RSP_CODE_LOCATION, i);
					finish();
				}
			});
		}

		class ViewHolder {
			private Button lPointBtn;
		}

	}

}
