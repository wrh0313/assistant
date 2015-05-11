package com.wrh.assistant.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.wrh.assistant.R;
import com.wrh.assistant.listener.MyOrientationListener;
import com.wrh.assistant.listener.MyOrientationListener.OnOrientationListener;
import com.wrh.assistant.model.DrivingResult;
import com.wrh.assistant.model.WalkingResult;
import com.wrh.assistant.view.ZoomControlView;

public class MainActivity extends Activity implements OnClickListener {

	private Context mContext = null;
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private ZoomControlView mZoomControlView = null;
	private Button mNearbyBtn = null;
	private Button mRouteBtn = null;
	// 定位客户端
	private LocationClient mMyLocationClient;
	// 定位监听器
	private MyLocationListener mLocationListener;
	// 是否是第一次定位
	private volatile boolean isFristLocation = true;
	// 当前的位置模式
	private LocationMode mCurrentLocationMode = LocationMode.NORMAL;
	// 方向传感器监听器
	private MyOrientationListener mOrientationListener;
	//
	private float mCurrentOrientationX;
	// 自定义定位图层图片ID
	private static final int ID_ICON_FOLLOW = R.drawable.main_icon_follow;
	// 使用sharedPreferences保存上一次的经纬度
	private static final String FILENAME = "latlng";
	private static final String KEY_LAT = "lat";
	private static final String KEY_LNG = "lng";
	private BroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		this.mContext = this;

		initBroadcastReceiver();
		initViews();
		initLocation();
	}

	private void initBroadcastReceiver() {
		mReceiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		filter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		filter.addAction(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE);
		registerReceiver(mReceiver, filter);
	}

	private void initViews() {
		// 初始化地图控件
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 初始化缩放比例控件
		mZoomControlView = (ZoomControlView) findViewById(R.id.zoomControlView);
		mZoomControlView.setMapView(mMapView);
		mMapView.showZoomControls(false);
		mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {

			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {

			}

			@Override
			public void onMapStatusChangeFinish(MapStatus arg0) {
				mZoomControlView.refreshZoomButtonStatus(arg0.zoom);
			}

			@Override
			public void onMapStatusChange(MapStatus arg0) {
				
			}
		});
		// 初始化周边按钮控件
		mNearbyBtn = (Button) findViewById(R.id.nearbyBtn);
		mNearbyBtn.setOnClickListener(this);
		// 初始化路线查询按钮控件
		mRouteBtn = (Button) findViewById(R.id.routeBtn);
		mRouteBtn.setOnClickListener(this);
		// 获取上一次保存的经纬度
		MapStatusUpdate msu = null;
		LatLng latLng = getLatlng();
		if (latLng != null) {
			msu = MapStatusUpdateFactory.newLatLng(latLng);
			mBaiduMap.setMapStatus(msu);
		}
		// 设置缩放比例
		msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);

	}

	private void initLocation() {
		mMyLocationClient = new LocationClient(mContext);
		mLocationListener = new MyLocationListener();
		mMyLocationClient.registerLocationListener(mLocationListener);

		LocationClientOption option = new LocationClientOption();
		option.setAddrType("all");
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setScanSpan(3000);

		mMyLocationClient.setLocOption(option);

		// 初始化方向传感器
		mOrientationListener = new MyOrientationListener(mContext);
		mOrientationListener
				.setmOnOrientationListener(new OnOrientationListener() {

					@Override
					public void onOrientationChange(float x) {
						mCurrentOrientationX = x;
					}
				});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RouteActivity.RSP_CODE_DRIVING_ROUTE) {
			DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
			overlay.setData(DrivingResult.getDrivingRouteResult()
					.getRouteLines().get(0));
			for (OverlayOptions options : overlay.getOverlayOptions()) {
				mBaiduMap.addOverlay(options);
			}

		} else if (resultCode == RouteActivity.RSP_CODE_WALKING_ROUTE) {
			WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
			overlay.setData(WalkingResult.getWalkingRouteResult()
					.getRouteLines().get(0));
			for (OverlayOptions options : overlay.getOverlayOptions()) {
				mBaiduMap.addOverlay(options);
			}

		}

	}

	// 广播接收器
	public class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, intent.getAction(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	// 定位监听器
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {

			MyLocationData data = new MyLocationData.Builder()
					//
					.direction(mCurrentOrientationX)
					.accuracy(location.getRadius())//
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude())//
					.build();
			mBaiduMap.setMyLocationData(data);

			// 设置位置模式和自定义位置图层
			MyLocationConfiguration locationConfiguration = new MyLocationConfiguration(
					mCurrentLocationMode, true,
					BitmapDescriptorFactory.fromResource(ID_ICON_FOLLOW));
			mBaiduMap.setMyLocationConfigeration(locationConfiguration);

			if (isFristLocation) {
				Toast.makeText(
						mContext,
						" addr:" + location.getAddrStr() + " stNumber:"
								+ location.getStreetNumber(),
						Toast.LENGTH_SHORT).show();

				LatLng latLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
				mBaiduMap.animateMapStatus(msu);
				isFristLocation = false;
				// 将经纬度保存到SharedPreferences中
				saveLatlng(latLng);

				// Toast.makeText(mContext, location.getAddrStr(),
				// Toast.LENGTH_SHORT).show();
			}

		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 关闭定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mMyLocationClient.isStarted()) {
			mMyLocationClient.start();
		}
		// 开启方向传感器监听
		mOrientationListener.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 停止定位
		mBaiduMap.setMyLocationEnabled(false);
		mMyLocationClient.stop();
		// 停止方向传感器监听
		mOrientationListener.stop();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		mMapView = null;
		// 反注册广播接收器
		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	// 保存经纬度
	private void saveLatlng(LatLng latLng) {
		SharedPreferences sp = getSharedPreferences(FILENAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(KEY_LAT, String.valueOf(latLng.latitude));
		editor.putString(KEY_LNG, String.valueOf(latLng.longitude));
		editor.commit();
		Log.i("hrw", "saveLatLng-----");
	}

	// 获取经纬度
	private LatLng getLatlng() {
		SharedPreferences sp = getSharedPreferences(FILENAME,
				Context.MODE_PRIVATE);
		String latStr = sp.getString(KEY_LAT, "");
		String lngStr = sp.getString(KEY_LNG, "");
		double lat, lng;
		Log.i("hrw", "getLatlng-----");
		if (!TextUtils.isEmpty(latStr) && !TextUtils.isEmpty(lngStr)) {
			lat = Double.parseDouble(latStr);
			lng = Double.parseDouble(lngStr);
			return new LatLng(lat, lng);
		}
		return null;
	}

	@Override
	public void onClick(View v) {
		Intent i = null;
		switch (v.getId()) {
		case R.id.nearbyBtn:
			mBaiduMap.clear();
			i = new Intent(this, NearbyActivity.class);
			startActivity(i);
			break;
		case R.id.routeBtn:
			mBaiduMap.clear();
			i = new Intent(this, RouteActivity.class);
			startActivityForResult(i, 0);
			break;
		default:
			break;
		}
	}

}
