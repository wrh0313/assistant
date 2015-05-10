package com.wrh.assistant.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.SearchResult.ERRORNO;
import com.baidu.mapapi.search.core.VehicleInfo;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteLine.TransitStep;
import com.baidu.mapapi.search.route.TransitRouteLine.TransitStep.TransitRouteStepType;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.wrh.assistant.R;
import com.wrh.assistant.model.BusLineUid;
import com.wrh.assistant.model.BusLinesUid;
import com.wrh.assistant.model.BusRouteLineDetail;
import com.wrh.assistant.model.BusRouteLineInfo;
import com.wrh.assistant.model.TransitResult;
import com.wrh.assistant.utils.TimeAndDistanceUtil;

public class BusRouteLinesActivity extends Activity implements OnClickListener,
		OnItemClickListener {
	private Context mContext = null;
	private ImageView mBackUp = null;
	private ListView mLinesList = null;
	private LinesAdapter mAdapter = null;
	private TransitRouteResult mResult;
	private List<TransitRouteLine> mTransitRouteLines;
	private List<BusRouteLineInfo> mBusRouteLineInfos;
	private List<BusLinesUid> mUidsList;
	
	public static final String KEY_BUSLINESUID = "buslinesuid";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_routelines);

		mContext = this;
		initData();
		initViews();
	}

	

	private void initData() {
		mResult = TransitResult.getResult();
		mTransitRouteLines = mResult.getRouteLines();
		List<TransitStep> allStep = null;
		VehicleInfo vehicleInfo = null;
		mBusRouteLineInfos = new ArrayList<BusRouteLineInfo>();
		mUidsList = new ArrayList<BusLinesUid>();
		List<BusLineUid> uidList = null;
		BusRouteLineInfo info = null;
		TransitRouteStepType stepType = null;
		StringBuilder outlineSb = null;
		BusLinesUid uids = null;
		BusLineUid uid = null;
		int stationNumSb = 0;
		int onFootSb = 0;

		for (TransitRouteLine routeLine : mTransitRouteLines) {
			allStep = routeLine.getAllStep();
			outlineSb = new StringBuilder();
			stationNumSb = 0;
			onFootSb = 0;
			info = new BusRouteLineInfo();
			uids = new BusLinesUid();
			uidList = new ArrayList<BusLineUid>();
			info.setDuration(TimeAndDistanceUtil.parseTime(routeLine
					.getDuration()));
			info.setStartNode(routeLine.getStarting());
			info.setEndNode(routeLine.getTerminal());
			info.setSteps(allStep);
			for (int i = 0; i < allStep.size(); i++) {
				TransitStep step = allStep.get(i);
				vehicleInfo = step.getVehicleInfo();
				if (vehicleInfo != null) {
					appendRouteOutline(outlineSb, vehicleInfo.getTitle());
					stationNumSb = appendStationNum(stationNumSb,
							vehicleInfo.getPassStationNum());
				}
				stepType = step.getStepType();
				if (stepType == TransitRouteStepType.WAKLING) {
					onFootSb = appendOnFoot(onFootSb, step.getDistance());

				}
				if (stepType == TransitRouteStepType.BUSLINE
						|| stepType == TransitRouteStepType.SUBWAY) {
					uid = new BusLineUid();
					uid.setPosition(i + 1);
					uid.setUid(vehicleInfo.getUid());
					uidList.add(uid);
				}
			}
			uids.setUidList(uidList);
			info.setRouteOutlineStr(outlineSb.toString());
			info.setStationNum(stationNumSb + "站");
			info.setOnFoot(TimeAndDistanceUtil.parseDistance(onFootSb));
			mBusRouteLineInfos.add(info);
			mUidsList.add(uids);
		}

	}

	private int appendOnFoot(int onFoot, int foot) {
		return onFoot = onFoot + foot;
	}

	private int appendStationNum(int stationNum, int num) {
		return stationNum = stationNum + num;
	}

	private void appendRouteOutline(StringBuilder sb, String routeStr) {
		if (routeStr != null) {
			if (sb.length() == 0) {
				sb.append(routeStr);
			} else {
				sb.append(" ——> ");
				sb.append(routeStr);
			}

		}
	}

	private void initViews() {
		mBackUp = (ImageView) findViewById(R.id.busRouteLinesBackUp);
		mLinesList = (ListView) findViewById(R.id.busRouteLinesList);
		mBackUp.setOnClickListener(this);
		mAdapter = new LinesAdapter(mContext, mTransitRouteLines);
		mLinesList.setAdapter(mAdapter);
		mLinesList.setOnItemClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.busRouteLinesBackUp:
			finish();
			break;
		default:
			break;
		}
	}

	

	
	public class LinesAdapter extends BaseAdapter {

		private List<TransitRouteLine> mTransitRouteLines;
		private LayoutInflater inflater;

		public LinesAdapter(Context context,
				List<TransitRouteLine> transitRouteLines) {
			inflater = LayoutInflater.from(context);
			this.mTransitRouteLines = transitRouteLines;
		}

		@Override
		public int getCount() {
			return mTransitRouteLines.size();
		}

		@Override
		public TransitRouteLine getItem(int position) {
			return mTransitRouteLines.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.route_lines_list_item,
						null);
				holder = new ViewHolder();
				holder.routeOutlineTxt = (TextView) convertView
						.findViewById(R.id.routeOutlineTxt);
				holder.routeListItemDurationTxt = (TextView) convertView
						.findViewById(R.id.routeListItemDurationTxt);
				holder.routeListItemStationNumTxt = (TextView) convertView
						.findViewById(R.id.routeListItemStationNumTxt);
				holder.routeListItemOnFootTxt = (TextView) convertView
						.findViewById(R.id.routeListItemOnFootTxt);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.routeOutlineTxt.setText(mBusRouteLineInfos.get(position)
					.getRouteOutlineStr());
			holder.routeListItemDurationTxt.setText(mBusRouteLineInfos.get(
					position).getDuration());
			holder.routeListItemStationNumTxt.setText(mBusRouteLineInfos.get(
					position).getStationNum());
			holder.routeListItemOnFootTxt.setText(mBusRouteLineInfos.get(
					position).getOnFoot());
			return convertView;
		}

		class ViewHolder {
			private TextView routeOutlineTxt;
			private TextView routeListItemDurationTxt;
			private TextView routeListItemStationNumTxt;
			private TextView routeListItemOnFootTxt;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position > -1) {
			BusLinesUid busLinesUid = mUidsList.get(position);
			BusRouteLineInfo busLineInfo = mBusRouteLineInfos.get(position);
			BusRouteLineDetail.setBusRouteLineInfo(busLineInfo);
			Intent intent = new Intent(mContext,
					BusRouteLineDetailActivity.class);
			intent.putExtra(KEY_BUSLINESUID, busLinesUid);
			startActivity(intent);
		}
	}
}
