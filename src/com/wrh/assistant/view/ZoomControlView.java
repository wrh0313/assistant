package com.wrh.assistant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.wrh.assistant.R;

public class ZoomControlView extends RelativeLayout implements OnClickListener {

	private Button mBtnZoomIn;
	private Button mBtnZoomOut;
	private float maxZoomLevel;
	private float minZoomLevel;
	private MapView mMapView;

	public ZoomControlView(Context context) {
		this(context, null, 0);
	}

	public ZoomControlView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ZoomControlView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	private void init() {
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.zoom_controls_layout, null);
		mBtnZoomIn = (Button) view.findViewById(R.id.zoomIn);
		mBtnZoomOut = (Button) view.findViewById(R.id.zoomOut);
		mBtnZoomIn.setOnClickListener(this);
		mBtnZoomOut.setOnClickListener(this);
		addView(view);
	}

	@Override
	public void onClick(View v) {
		if (mMapView == null) {
			throw new NullPointerException(
					"you should call setMapView(MapView mapView) at first");
		}
		switch (v.getId()) {
		case R.id.zoomIn:
			mMapView.getMap().setMapStatus(MapStatusUpdateFactory.zoomIn());
			refreshZoomButtonStatus(mMapView.getMap().getMapStatus().zoom);
			break;
		case R.id.zoomOut:
			mMapView.getMap().setMapStatus(MapStatusUpdateFactory.zoomOut());
			refreshZoomButtonStatus(mMapView.getMap().getMapStatus().zoom);
			break;

		default:
			break;
		}
	}
	/***
	 * 根据MapView的缩放级别更新缩放按钮的状态，当达到最大缩放级别，设置mBtnZoomIn 
     * 为不能点击，反之设置mBtnZoomOut 
	 * @param level
	 */
	public void refreshZoomButtonStatus(float level){
		if(mMapView == null){  
            throw new NullPointerException("you should call setMapView(MapView mapView) at first");  
        }  
		if(level > minZoomLevel && level < maxZoomLevel){  
            if(!mBtnZoomOut.isEnabled()){  
            	mBtnZoomOut.setEnabled(true);  
            }  
            if(!mBtnZoomIn.isEnabled()){   
            	mBtnZoomIn.setEnabled(true);  
            }  
        }  
        else if(level == minZoomLevel ){  
        	mBtnZoomOut.setEnabled(false);  
        }  
        else if(level == maxZoomLevel){  
        	mBtnZoomIn.setEnabled(false);  
        }  
	}

	/***
	 * 设置MapView
	 * 
	 * @param mapView
	 */
	public void setMapView(MapView mapView) {
		this.mMapView = mapView;

		maxZoomLevel = mMapView.getMap().getMaxZoomLevel();
		minZoomLevel = mMapView.getMap().getMinZoomLevel();
	}
}
