package com.wrh.assistant.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.search.core.SearchResult.ERRORNO;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption.DrivingPolicy;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.wrh.assistant.R;
import com.wrh.assistant.model.DrivingResult;
import com.wrh.assistant.model.TransitResult;
import com.wrh.assistant.model.WalkingResult;

public class RouteActivity extends Activity implements OnClickListener {
	private Button mSearchBtn = null;
	private Button mStartPoint = null;
	private Button mEndPoint = null;
	private ImageView mBackUp = null;
	private ImageView mCarImg = null;
	private ImageView mBusImg = null;
	private ImageView mOnFootImg = null;
	private Context mContext = null;
	private BDLocation mLocation = null;
	private RoutePlanSearch mSearch = null;
	private LocationClient mLocationClient = null;
	private MyLocationListener mLocationListener = null;
	private OnGetRouteResult mOnGetRouteResultListener = null;
	private ProgressDialog mDialog;
	private String sCity = null;
	private String eCity = null;
	private String sAddress = null;
	private String eAddress = null;
	private static final int MODE_CAR = 1;
	private static final int MODE_BUS = 2;
	private static final int MODE_ONFOOT = 3;
	private int mCurrentMode = MODE_BUS;
	public static final int REQ_CODE_STARTPOINT = 1;
	public static final int REQ_CODE_ENDPOINT = 2;
	public static final String KEY_LOCATION = "location";
	public static final String KEY_TRANSIT_ROUTE_RESULT = "transitRouteResult";
	public static final int RSP_CODE_DRIVING_ROUTE = 3;
	public static final int RSP_CODE_WALKING_ROUTE = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route);

		mContext = this;
		initLocation();
		initRoutePlanSearch();
		initViews();
	}

	// 初始化界面
	private void initViews() {
		mSearchBtn = (Button) findViewById(R.id.routeSearchBtn);
		mStartPoint = (Button) findViewById(R.id.routeStartPoint);
		mEndPoint = (Button) findViewById(R.id.routeEndPoint);
		mBackUp = (ImageView) findViewById(R.id.routeBackUp);
		mCarImg = (ImageView) findViewById(R.id.routeCarImg);
		mBusImg = (ImageView) findViewById(R.id.routeBusImg);
		mOnFootImg = (ImageView) findViewById(R.id.routeOnFootImg);

		mSearchBtn.setOnClickListener(this);
		mStartPoint.setOnClickListener(this);
		mEndPoint.setOnClickListener(this);
		mBackUp.setOnClickListener(this);
		mCarImg.setOnClickListener(this);
		mBusImg.setOnClickListener(this);
		mOnFootImg.setOnClickListener(this);
		// 默认查询的交通方式为公交查询
		setTMode(MODE_BUS);
	}

	// 初始化定位
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

	}

	// 初始化路线查询
	private void initRoutePlanSearch() {
		mSearch = RoutePlanSearch.newInstance();
		mOnGetRouteResultListener = new OnGetRouteResult();
		mSearch.setOnGetRoutePlanResultListener(mOnGetRouteResultListener);
	}

	// 清除交通方式状态
	private void clearTMode() {
		switch (mCurrentMode) {
		case MODE_CAR:
			mCarImg.setImageResource(R.drawable.route_icon_car);
			break;
		case MODE_BUS:
			mBusImg.setImageResource(R.drawable.route_icon_bus);
		case MODE_ONFOOT:
			mOnFootImg.setImageResource(R.drawable.route_icon_onfoot);

		default:
			break;
		}
	}

	// 设置交通方式
	private void setTMode(int mode) {
		switch (mode) {
		case MODE_CAR:
			clearTMode();
			mCarImg.setImageResource(R.drawable.route_icon_car_hl);
			break;
		case MODE_BUS:
			clearTMode();
			mBusImg.setImageResource(R.drawable.route_icon_bus_hl);
			break;
		case MODE_ONFOOT:
			clearTMode();
			mOnFootImg.setImageResource(R.drawable.route_icon_onfoot_hl);
			break;
		default:
			break;
		}
		mCurrentMode = mode;
	}

	// 路线查询
	private void routeSearch(int mode) {
		switch (mode) {
		case MODE_CAR:
			drivingSearch();
			break;
		case MODE_BUS:
			transitSearch();
			break;
		case MODE_ONFOOT:
			walkingSearch();
			break;

		default:
			break;
		}
	}

	// 公交查询
	private void transitSearch() {
		PlanNode stNode = PlanNode.withCityNameAndPlaceName(sCity, sAddress);
		PlanNode enNode = PlanNode.withCityNameAndPlaceName(eCity, eAddress);
		TransitRoutePlanOption option = new TransitRoutePlanOption();
		option.from(stNode);
		option.city(sCity);
		option.to(enNode);
		mSearch.transitSearch(option);
		// 弹出等待框
		popDialog(false, " 正在查找公交路线...");

	}

	private void popDialog(boolean cancelable, String message) {
		mDialog = new ProgressDialog(mContext);
		mDialog.setCancelable(cancelable);
		mDialog.setMessage(message);
		mDialog.show();
	}

	// 驾车路线查询
	private void drivingSearch() {
		PlanNode stNode = PlanNode.withCityNameAndPlaceName(sCity, sAddress);
		PlanNode enNode = PlanNode.withCityNameAndPlaceName(eCity, eAddress);
		DrivingRoutePlanOption option = new DrivingRoutePlanOption();
		option.from(stNode);
		option.policy(DrivingPolicy.ECAR_TIME_FIRST);
		option.to(enNode);
		mSearch.drivingSearch(option);
		// 弹出等待框
		popDialog(false, " 正在查找驾车路线...");
	}

	// 步行查询
	private void walkingSearch() {
		PlanNode stNode = PlanNode.withCityNameAndPlaceName(sCity, sAddress);
		PlanNode enNode = PlanNode.withCityNameAndPlaceName(eCity, eAddress);
		WalkingRoutePlanOption option = new WalkingRoutePlanOption();
		option.from(stNode);
		option.to(enNode);
		mSearch.walkingSearch(option);

		popDialog(false, " 正在查询步行路线... ");
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
		// 释放检索实例
		mSearch.destroy();
	}

	public class OnGetRouteResult implements OnGetRoutePlanResultListener {

		@Override
		public void onGetDrivingRouteResult(DrivingRouteResult result) {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
			if (result != null && result.error == ERRORNO.NO_ERROR) {
				DrivingResult.setDrivingRouteResult(result);
				setResult(RSP_CODE_DRIVING_ROUTE);
				finish();
			} else {
				Toast.makeText(mContext, "没有找到路线！", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onGetTransitRouteResult(TransitRouteResult result) {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
			if (result != null && result.error == ERRORNO.NO_ERROR) {
				Intent i = new Intent(mContext, BusRouteLinesActivity.class);
				TransitResult.setResult(result);
				TransitResult.setCity(sCity);
				startActivity(i);
			} else {
				Toast.makeText(mContext, "没有找到路线！", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onGetWalkingRouteResult(WalkingRouteResult result) {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
			if (result != null && result.error == ERRORNO.NO_ERROR) {
				WalkingResult.setWalkingRouteResult(result);
				setResult(RSP_CODE_WALKING_ROUTE);
				finish();
			} else {
				Toast.makeText(mContext, "没有找到路线！", Toast.LENGTH_SHORT).show();
			}
		}

	}

	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// // 获取经纬度
			// mLatLng = new LatLng(location.getLatitude(),
			// location.getLongitude());
			mLocation = location;
		}

	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.routeCarImg:
			setTMode(MODE_CAR);
			break;
		case R.id.routeBusImg:
			setTMode(MODE_BUS);
			break;
		case R.id.routeOnFootImg:
			setTMode(MODE_ONFOOT);
			break;
		case R.id.routeStartPoint:
			intent = new Intent(mContext, RoutePointActivity.class);
			intent.putExtra(KEY_LOCATION, mLocation);
			startActivityForResult(intent, REQ_CODE_STARTPOINT);
			break;
		case R.id.routeEndPoint:
			intent = new Intent(mContext, RoutePointActivity.class);
			// intent.putExtra(KEY_LOCATION, mLocation);
			startActivityForResult(intent, REQ_CODE_ENDPOINT);
			break;
		case R.id.routeBackUp:
			finish();
			break;
		case R.id.routeSearchBtn:
			if (TextUtils.isEmpty(sCity) || TextUtils.isEmpty(sAddress)) {
				Toast.makeText(mContext, "起始点不祥，请重新输入！", Toast.LENGTH_SHORT)
						.show();
				return;
			} else if (TextUtils.isEmpty(eCity) || TextUtils.isEmpty(eAddress)) {
				Toast.makeText(mContext, "终点不祥，请重新输入！", Toast.LENGTH_SHORT)
						.show();
				return;
			} else if (!sCity.equals(eCity)) {
				Toast.makeText(mContext, "暂时不支持跨城市查询!", Toast.LENGTH_SHORT)
						.show();
				return;
			} else if (sAddress.equals(eAddress)) {
				Toast.makeText(mContext, "起点和终点位置一致！", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			routeSearch(mCurrentMode);
			break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case RoutePointActivity.RSP_CODE_LOCATION:
			if (requestCode == REQ_CODE_STARTPOINT) {
				if (data != null) {
					sCity = data.getStringExtra(RoutePointActivity.KEY_CITY);
					sAddress = data
							.getStringExtra(RoutePointActivity.KEY_ADDRESS);
					Log.i("wrh", "sCity: " + sCity + " sAddress:" + sAddress);
					String address = sCity + sAddress;
					mStartPoint.setText(address);
				}
			} else if (requestCode == REQ_CODE_ENDPOINT) {
				if (data != null) {
					eCity = data.getStringExtra(RoutePointActivity.KEY_CITY);
					eAddress = data
							.getStringExtra(RoutePointActivity.KEY_ADDRESS);
					Log.i("wrh", "eCity: " + eCity + " eAddress:" + eAddress);
					String address = eCity + eAddress;
					mEndPoint.setText(address);
				}
			}
			break;
		case RoutePointActivity.RSP_CODE_MY_LOCATON:
			if (mLocation == null) {
				Toast.makeText(mContext, " 无法获取当前位置，请检查网络... ",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (requestCode == REQ_CODE_STARTPOINT) {
				mStartPoint.setText("我的位置");
				sCity = mLocation.getCity();
				sAddress = mLocation.getDistrict() + mLocation.getStreet();
				Log.i("wrh", "sCity: " + sCity + " sAddress:" + sAddress);
			} else if (requestCode == REQ_CODE_ENDPOINT) {
				mEndPoint.setText("我的位置");
				eCity = mLocation.getCity();
				eAddress = mLocation.getDistrict() + mLocation.getStreet();
				Log.i("wrh", "eCity: " + eCity + " eAddress:" + eAddress);
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
	}

}
