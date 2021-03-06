package com.wrh.assistant.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.navisdk.util.common.NetworkUtils;
import com.wrh.assistant.R;
import com.wrh.assistant.model.PoiSearchResult;
import com.wrh.assistant.utils.NetWorkUtil;
import com.wrh.assistant.view.NearbyQCheckItem;
import com.wrh.assistant.view.NearbyQCheckItem.OnClickItemListener;

public class NearbyActivity extends Activity implements OnClickItemListener,
		OnClickListener {

	private Context mContext;
	private Button mReloadBtn;
	private TextView mHotelBtn;
	private ImageView mBackUp;
	private PoiSearch mPoiSearch;
	private TextView mDelicacyBtn;
	private LatLng mLatLng = null;
	private ListView mInterestList;
	private LinearLayout mReloadLayout;
	private LinearLayout mNearbyQCheck;
	private TextView mEntertainmentBtn;
	private InterestListAdapter mAdapter;
	private ProgressBar mUpdateProgressBar;
	private LocationClient mLocationClient;
	private MyLocationListener mLocationListener;
	private OnGetPoiResult mOnGetPoiResultListener;
	private static final String[] GOOUT = { "[出行]", "公交站", "加油站", "停车场" };
	private static final String[] LIFE = { "[生活]", "银行", "超市", "厕所" };
	private static final String[] LEISURE = { "[休闲]", "网吧", "KTV", "洗浴" };
	private ProgressDialog mDialog;
	private boolean isFirst = true;
	private String mSearchKeyword;
	private int mCurrentBtnClick;
	private static final int DELICACTBTN = 1;
	private static final int ENTERTAINMENTBTN = 2;
	private static final int HOTELBTN = 3;

	private static final String KEYWORD_DELICACT = "美食";
	private static final String KEYWORD_ENTERTAINMENT = "休闲娱乐";
	private static final String KEYWORD_HOTEL = "酒店";

	public static final int RSP_CODE_POI_RESULT = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby);
		mContext = this;
		initLocation();
		initPoiSearch();
		initViews();

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
		mDialog.show();
	}

	private void initViews() {
		mHotelBtn = (TextView) findViewById(R.id.nearbyHotelBtn);
		mDelicacyBtn = (TextView) findViewById(R.id.nearbyDelicacyBtn);
		mInterestList = (ListView) findViewById(R.id.neabyInterestList);
		mNearbyQCheck = (LinearLayout) findViewById(R.id.nearbyQuickCheck);
		mEntertainmentBtn = (TextView) findViewById(R.id.nearbyEntertainmentBtn);
		mUpdateProgressBar = (ProgressBar) findViewById(R.id.nearbyUpdateProgress);
		mReloadLayout = (LinearLayout) findViewById(R.id.nearbyReloadLayout);
		mReloadBtn = (Button) findViewById(R.id.nearbyReload);
		mBackUp = (ImageView) findViewById(R.id.nearbyBackUp);

		addNearbyQCheckItem(GOOUT);
		addNearbyQCheckItem(LIFE);
		addNearbyQCheckItem(LEISURE);

		mBackUp.setOnClickListener(this);
		mHotelBtn.setOnClickListener(this);
		mReloadBtn.setOnClickListener(this);
		mDelicacyBtn.setOnClickListener(this);
		mEntertainmentBtn.setOnClickListener(this);

		mAdapter = new InterestListAdapter(new ArrayList<PoiInfo>());
		mInterestList.setAdapter(mAdapter);

		showProgessBarVisible();
		showBtnStatus(DELICACTBTN);
		if (!NetworkUtils.isNetworkAvailable(mContext)) {
			showReloadLayoutVisible();
		}
		// setClickBtn(DELICACTBTN);
	}

	private void showProgessBarVisible() {
		mInterestList.setVisibility(View.INVISIBLE);
		mReloadLayout.setVisibility(View.INVISIBLE);
		mUpdateProgressBar.setVisibility(View.VISIBLE);
	}

	private void showInterestListVisible() {
		mReloadLayout.setVisibility(View.INVISIBLE);
		mUpdateProgressBar.setVisibility(View.INVISIBLE);
		mInterestList.setVisibility(View.VISIBLE);
	}

	private void showReloadLayoutVisible() {
		mUpdateProgressBar.setVisibility(View.INVISIBLE);
		mInterestList.setVisibility(View.INVISIBLE);
		mReloadLayout.setVisibility(View.VISIBLE);

	}

	private void setClickBtn(int typeBtn) {
		showBtnStatus(typeBtn);
		if (NetworkUtils.isNetworkAvailable(mContext) == false) {
			showReloadLayoutVisible();
			return;
		}
		askInterestData(typeBtn);
	}

	private void askInterestData(int typeBtn) {
		PoiNearbySearchOption option;
		switch (typeBtn) {
		case DELICACTBTN:
			option = new PoiNearbySearchOption();
			option.location(mLatLng);
			option.keyword("美食");
			option.pageNum(10);
			option.radius(5000);
			mSearchKeyword = KEYWORD_DELICACT;
			mPoiSearch.searchNearby(option);
			break;
		case ENTERTAINMENTBTN:
			option = new PoiNearbySearchOption();
			option.location(mLatLng);
			option.keyword("休闲娱乐");
			option.pageNum(10);
			option.radius(5000);
			mSearchKeyword = KEYWORD_ENTERTAINMENT;
			mPoiSearch.searchNearby(option);
			break;
		case HOTELBTN:
			option = new PoiNearbySearchOption();
			option.location(mLatLng);
			option.keyword("酒店");
			option.pageNum(10);
			option.radius(5000);
			mSearchKeyword = KEYWORD_HOTEL;
			mPoiSearch.searchNearby(option);
			break;

		default:
			break;
		}
	}

	// 清除状态
	private void clearBtnStatus(int typeBtn) {
		if (mCurrentBtnClick == typeBtn) {
			return;
		}
		switch (mCurrentBtnClick) {
		case DELICACTBTN:
			mDelicacyBtn.setTextColor(getResources().getColor(R.color.black));
			break;
		case ENTERTAINMENTBTN:
			mEntertainmentBtn.setTextColor(getResources().getColor(
					R.color.black));
			break;
		case HOTELBTN:
			mHotelBtn.setTextColor(getResources().getColor(R.color.black));
			break;

		default:
			break;
		}
	}

	// 设置点击之后的状态
	private void showBtnStatus(int typeBtn) {
		clearBtnStatus(typeBtn);
		switch (typeBtn) {
		case DELICACTBTN:
			clearBtnStatus(typeBtn);
			mDelicacyBtn.setTextColor(getResources()
					.getColor(R.color.dark_blue));
			mCurrentBtnClick = DELICACTBTN;
			break;
		case ENTERTAINMENTBTN:
			clearBtnStatus(typeBtn);
			mEntertainmentBtn.setTextColor(getResources().getColor(
					R.color.dark_blue));
			mCurrentBtnClick = ENTERTAINMENTBTN;
			break;
		case HOTELBTN:
			clearBtnStatus(typeBtn);
			mHotelBtn.setTextColor(getResources().getColor(R.color.dark_blue));
			mCurrentBtnClick = HOTELBTN;
			break;

		default:
			break;
		}
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
				Toast.makeText(mContext, " 无法获取当前位置,请检查网络  ",
						Toast.LENGTH_SHORT).show();
				return;
			}
			TextView tvChild = (TextView) v;
			PoiNearbySearchOption option = new PoiNearbySearchOption();
			option.location(mLatLng);
			option.keyword(tvChild.getText().toString().trim());
			option.pageNum(10);
			option.radius(3000);
			mPoiSearch.searchNearby(option);
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
			Log.i("hrw", "detail url: " + result.getDetailUrl());
			Log.i("hrw", "detail name:" + result.getName());
			Log.i("hrw", "detail uid:" + result.getUid());
		}

		@Override
		public void onGetPoiResult(PoiResult result) {
			Toast.makeText(mContext, "result:" + result.getTotalPageNum(),
					Toast.LENGTH_LONG).show();
			Log.i("wrh", "result: " + result.error);
			if (result != null && result.error == ERRORNO.NO_ERROR) {
				if (mSearchKeyword == KEYWORD_DELICACT
						|| mSearchKeyword == KEYWORD_ENTERTAINMENT
						|| mSearchKeyword == KEYWORD_HOTEL) {
					mSearchKeyword = "";
					mInterestList.smoothScrollToPosition(0);
					showInterestListVisible();
					mAdapter.addDatas(result.getAllPoi());
				} else {
					PoiSearchResult.setPoiResult(result);
					setResult(RSP_CODE_POI_RESULT);
					finish();
				}

			} else {
				Toast.makeText(mContext, " 亲，没有找到poi信息  ", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// 销毁等待对话框
			dismissDialog();

			if (location == null) {
				showReloadLayoutVisible();
				return;
			}

			// 获取经纬度
			mLatLng = new LatLng(location.getLatitude(),
					location.getLongitude());

			if (isFirst) {
				setClickBtn(DELICACTBTN);
				isFirst = false;
			}

		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nearbyDelicacyBtn:
			showProgessBarVisible();
			setClickBtn(DELICACTBTN);
			break;
		case R.id.nearbyEntertainmentBtn:
			showProgessBarVisible();
			setClickBtn(ENTERTAINMENTBTN);
			break;
		case R.id.nearbyHotelBtn:
			showProgessBarVisible();
			setClickBtn(HOTELBTN);
			break;
		case R.id.nearbyReload:
			mLocationClient.start();
			GetLocationDialog();
			showProgessBarVisible();
			setClickBtn(mCurrentBtnClick);
			break;
		case R.id.nearbyBackUp:
			finish();
			break;

		default:
			break;
		}
	}

	private void dismissDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	public class InterestListAdapter extends BaseAdapter {
		private List<PoiInfo> datas;

		public InterestListAdapter(List<PoiInfo> datas) {
			this.datas = datas;
		}

		public void addDatas(List<PoiInfo> datas) {
			if (this.datas != null) {
				this.datas.clear();
				this.datas.addAll(datas);
				notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			return datas.size();
		}

		@Override
		public PoiInfo getItem(int position) {
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.item_nearby_interest_list, null);
				holder.name = (TextView) convertView
						.findViewById(R.id.itemInterestName);
				holder.addr = (TextView) convertView
						.findViewById(R.id.itemInterestAddress);
				holder.tel = (TextView) convertView
						.findViewById(R.id.itemInterestTel);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.name.setText(getItem(position).name);
			holder.addr.setText(getItem(position).address);
			holder.tel.setText(getItem(position).phoneNum);
			return convertView;
		}

		class ViewHolder {
			private TextView name;
			private TextView addr;
			private TextView tel;
		}

	}
}
