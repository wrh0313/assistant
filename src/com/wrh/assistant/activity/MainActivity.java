package com.wrh.assistant.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMyLocationClickListener;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
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
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.PoiInfo.POITYPE;
import com.baidu.mapapi.search.poi.PoiResult;
import com.wrh.assistant.R;
import com.wrh.assistant.listener.MyOrientationListener;
import com.wrh.assistant.listener.MyOrientationListener.OnOrientationListener;
import com.wrh.assistant.model.DrivingResult;
import com.wrh.assistant.model.PoiSearchResult;
import com.wrh.assistant.model.WalkingResult;
import com.wrh.assistant.view.ZoomControlView;

public class MainActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private Context mContext = null;
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private ZoomControlView mZoomControlView = null;
	private Button mNearbyBtn = null;
	private Button mRouteBtn = null;
	private Button mShareBtn = null;
	private Button mButton;
	private boolean mWindowShow;
	private InfoWindow mInfoWindow;
	private LatLng mLatLng;
	private View sharePopuView;
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
	private static final String[] SHARETEXT = { "微信好友", "微信朋友圈", "新浪微博", "QQ空间" };
	private static final int[] SHAREIMG = { R.drawable.sns_weixin_icon,
			R.drawable.sns_weixin_timeline_icon, R.drawable.sns_sina_icon,
			R.drawable.sns_qzone_icon };

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
		initPopuView();
	}

	private void initPopuView() {
		sharePopuView = getLayoutInflater().inflate(R.layout.share_popu, null);
		GridView shareGridView = (GridView) sharePopuView
				.findViewById(R.id.shareGridView);
		ShareAdapter adapter = new ShareAdapter();
		shareGridView.setAdapter(adapter);
		shareGridView.setOnItemClickListener(this);
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
		// 初始化我的位置弹出按钮
		mButton = new Button(getApplicationContext());
		mButton.setTextColor(getResources().getColor(R.color.black));
		mButton.setBackgroundResource(R.drawable.layout_shadow);
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
		//初始化分享按钮空间
		mShareBtn = (Button) findViewById(R.id.shareBtn);
		mShareBtn.setOnClickListener(this);
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

		mBaiduMap.setOnMyLocationClickListener(new OnMyLocationClickListener() {

			@Override
			public boolean onMyLocationClick() {
				if (!mWindowShow) {
					mBaiduMap.showInfoWindow(mInfoWindow);
					mWindowShow = true;
				} else {
					mBaiduMap.hideInfoWindow();
					mWindowShow = false;
				}

				return true;
			}
		});

		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi mapPoi) {
				return false;
			}

			@Override
			public void onMapClick(LatLng latLng) {
				if (mWindowShow) {
					mBaiduMap.hideInfoWindow();
					mWindowShow = false;
				}
			}
		});

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
			overlay.addToMap();
			overlay.zoomToSpan();
			for (OverlayOptions options : overlay.getOverlayOptions()) {
				mBaiduMap.addOverlay(options);
			}

		} else if (resultCode == RouteActivity.RSP_CODE_WALKING_ROUTE) {
			WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
			overlay.setData(WalkingResult.getWalkingRouteResult()
					.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
			for (OverlayOptions options : overlay.getOverlayOptions()) {
				mBaiduMap.addOverlay(options);
			}

		} else if (resultCode == NearbyActivity.RSP_CODE_POI_RESULT) {
			PoiResult result = PoiSearchResult.getPoiResult();
			mBaiduMap.clear();
			PoiOverlay poiOverlay = new MyPoiOverlay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(poiOverlay);
			poiOverlay.setData(result);
			poiOverlay.addToMap();
			poiOverlay.zoomToSpan();
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
			// 我的位置弹出窗覆盖物
			mLatLng = new LatLng(location.getLatitude(),
					location.getLongitude());
			mButton.setText(" " + location.getAddrStr() + " ");
			mInfoWindow = new InfoWindow(mButton, mLatLng, -47);

			if (isFristLocation) {
				Toast.makeText(
						mContext,
						" addr:" + location.getAddrStr() + " stNumber:"
								+ location.getStreetNumber(),
						Toast.LENGTH_SHORT).show();

				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mLatLng);
				mBaiduMap.animateMapStatus(msu);
				isFristLocation = false;
				// 将经纬度保存到SharedPreferences中
				saveLatlng(mLatLng);

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
			startActivityForResult(i, 0);
			break;
		case R.id.routeBtn:
			mBaiduMap.clear();
			i = new Intent(this, RouteActivity.class);
			startActivityForResult(i, 0);
			break;
		case R.id.shareBtn:
			PopupWindow popupWindow = new PopupWindow(sharePopuView,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			popupWindow.setFocusable(true);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setBackgroundDrawable(new BitmapDrawable());
			popupWindow.showAtLocation(mShareBtn, Gravity.CENTER, 0, 0);
			//设置透明度
			final Window window=getWindow();
	        final WindowManager.LayoutParams wl = window.getAttributes();
	        wl.alpha=0.5f;
	        window.setAttributes(wl);
	        popupWindow.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss() {
					wl.alpha = 1.0f;
					window.setAttributes(wl);
				}
			});
			
		default:
			break;
		}
	}

	public class ShareAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return SHAREIMG.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.item_share_popu, null);
				holder = new ViewHolder();
				holder.img = (ImageView) convertView
						.findViewById(R.id.itemShareImg);
				holder.txt = (TextView) convertView
						.findViewById(R.id.itemShareText);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.img.setImageResource(SHAREIMG[position]);
			holder.txt.setText(SHARETEXT[position]);
			return convertView;
		}

		class ViewHolder {
			private ImageView img;
			private TextView txt;
		}

	}

	public class MyPoiOverlay extends PoiOverlay {
		PoiInfo info;
		InfoWindow infoWindow;
		Button button;

		public MyPoiOverlay(BaiduMap baiduMap) {
			super(baiduMap);
			button = new Button(getApplicationContext());
			button.setBackgroundResource(R.drawable.layout_shadow);
			button.setTextColor(getResources().getColor(R.color.black));
		}

		@Override
		public boolean onPoiClick(int index) {
			info = getPoiResult().getAllPoi().get(index);
			infoWindow = new InfoWindow(button, info.location, -47);

			if (mWindowShow) {
				mBaiduMap.hideInfoWindow();
				mWindowShow = false;
			} else {
				if (info.type == POITYPE.BUS_STATION) {
					button.setText(info.name + " (公交站)");
				} else {
					button.setText(info.name);
				}

				mBaiduMap.showInfoWindow(infoWindow);
				mWindowShow = true;
			}
			return super.onPoiClick(index);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Toast.makeText(mContext, position + " item click", Toast.LENGTH_SHORT)
				.show();
	}

}
