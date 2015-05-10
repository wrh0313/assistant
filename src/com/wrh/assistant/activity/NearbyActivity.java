package com.wrh.assistant.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult.ERRORNO;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.wrh.assistant.R;
import com.wrh.assistant.view.NearbyQCheckItem;
import com.wrh.assistant.view.NearbyQCheckItem.OnClickItemListener;

public class NearbyActivity extends Activity implements OnClickItemListener {
	private PoiSearch mPoiSearch;
	private LinearLayout mNearbyQCheck;
	private LocationClient mLocationClient;
	private MyLocationListener mLocationListener;
	private OnGetPoiResult mOnGetPoiResultListener;
	private LatLng mLatLng = null;
	private Context mContext;
	private static final String[] GOOUT = { "[出行]", "公交站", "加油站", "停车场" };
	private static final String[] LIFE = { "[生活]", "银行", "超市", "厕所" };
	private static final String[] LEISURE = { "[休闲]", "网吧", "KTV", "洗浴" };
	private ProgressDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby);
		mContext = this;
		initViews();
		initLocation();
		initPoiSearch();

	}

	private void initPoiSearch() {
		mPoiSearch = PoiSearch.newInstance();
		mOnGetPoiResultListener = new OnGetPoiResult();
		mPoiSearch.setOnGetPoiSearchResultListener(mOnGetPoiResultListener);
	}

	private void initLocation() {
		mLocationClient = new LocationClient(this);
		mLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mLocationListener);

		LocationClientOption option = new LocationClientOption();
		option.setAddrType("all");
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setScanSpan(3000);

		mLocationClient.setLocOption(option);
		mLocationClient.start();

		GetLocationDialog();

	}

	private void GetLocationDialog() {
		mDialog = new ProgressDialog(mContext);
		mDialog.setMessage(" 正在获取位置信息... ");
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setIndeterminate(true);
		mDialog.show();
	}

	private void initViews() {
		mNearbyQCheck = (LinearLayout) findViewById(R.id.nearbyQuickCheck);

		addNearbyQCheckItem(GOOUT);
		addNearbyQCheckItem(LIFE);
		addNearbyQCheckItem(LEISURE);
	}

	private void addNearbyQCheckItem(String[] item) {
		NearbyQCheckItem nearbyQCheckItem = new NearbyQCheckItem(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		nearbyQCheckItem.setLayoutParams(params);
		nearbyQCheckItem.setItemText(item[0], item[1], item[2], item[3]);
		nearbyQCheckItem.setOnClickItemListener(this);
		mNearbyQCheck.addView(nearbyQCheckItem);

	}

	@Override
	public void onItemClick(View v) {
		if (v.getClass() == TextView.class) {
			if (mLatLng == null) {
				Toast.makeText(mContext, " 无法获取当前位置,请检查网络  ", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			TextView tvChild = (TextView) v;
			PoiNearbySearchOption option = new PoiNearbySearchOption();
			option.location(mLatLng);
			option.keyword(GOOUT[1]);
			option.pageNum(10);
			option.radius(3000);
			mPoiSearch.searchNearby(option);
			// PoiCitySearchOption option = new PoiCitySearchOption();
			// option.city("梅州");
			// option.keyword(GOOUT[1]);
			// option.pageNum(10);
			// mPoiSearch.searchInCity(option);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 停止定位
		mLocationClient.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 释放POI检索实例
		mPoiSearch.destroy();
	}

	// POI检索监听器
	public class OnGetPoiResult implements OnGetPoiSearchResultListener {

		@Override
		public void onGetPoiDetailResult(PoiDetailResult result) {
			// Log.i("wrh", "datail: " + result.toString());
		}

		@Override
		public void onGetPoiResult(PoiResult result) {
			Toast.makeText(mContext, "result:" + result.getTotalPageNum(),
					Toast.LENGTH_LONG).show();
			Log.i("wrh", "result: " + result.error);
			if (result != null && result.error == ERRORNO.NO_ERROR) {
				for (PoiInfo poiInfo : result.getAllPoi()) {
					Log.i("wrh", poiInfo.name);
					Log.i("wrh", poiInfo.address);
					Log.i("wrh", "" + poiInfo.type);
				}
			}
		}

	}

	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// 获取经纬度
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
			if (location == null) {
				Toast.makeText(mContext, " 无法获取位置信息，请检查网络！ ", Toast.LENGTH_LONG)
						.show();
				return;
			}
			mLatLng = new LatLng(location.getLatitude(),
					location.getLongitude());
		}
	}
}
