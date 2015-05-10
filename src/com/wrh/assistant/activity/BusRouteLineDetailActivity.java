package com.wrh.assistant.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineResult.BusStation;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.SearchResult.ERRORNO;
import com.wrh.assistant.R;
import com.wrh.assistant.model.BusLineUid;
import com.wrh.assistant.model.BusLinesUid;
import com.wrh.assistant.model.BusRouteLineDetail;
import com.wrh.assistant.model.BusRouteLineInfo;
import com.wrh.assistant.model.TransitResult;

public class BusRouteLineDetailActivity extends Activity implements
		OnClickListener {
	private Context mContext = null;
	private BusLineSearch mBusLineSearch = null;
	private OnGetBusLineSearchResult mBusLineSearchResultListener = null;
	private ImageView mBackUp;
	private TextView mRouteOutlineDTxt;
	private TextView mRouteDurationDTxt;
	private TextView mRouteStationNumDTxt;
	private TextView mRouteOnFootDTxt;
	private ListView mDetailList;
	private DetailAdapter mAdapter;
	private BusRouteLineInfo mBusRouteLineInfo;
	private BusLinesUid mBusLinesUid;
	private boolean isExpand = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_routeline_detail);
		mContext = this;
		getData();
		initBusLineSearch();
		initViews();

	}

	private void getData() {
		Intent i = getIntent();
		BusRouteLineInfo busRouteLineInfo = BusRouteLineDetail
				.getBusRouteLineInfo();
		BusLinesUid busLinesUid = (BusLinesUid) getIntent()
				.getSerializableExtra(BusRouteLinesActivity.KEY_BUSLINESUID);
		if (busLinesUid != null) {
			mBusLinesUid = busLinesUid;
		}
		if (busRouteLineInfo != null) {
			mBusRouteLineInfo = busRouteLineInfo;
		}

	}

	// private void askStationList() {
	// if (mBusLinesUid != null) {
	// for (BusLineUid uid : mBusLinesUid.getUidList()) {
	// mBusLineSearch.searchBusLine(new BusLineSearchOption().city(
	// TransitResult.getCity()).uid(uid.getUid()));
	// }
	// }
	// }

	private void initBusLineSearch() {
		mBusLineSearch = BusLineSearch.newInstance();
		// mBusLineSearchResultListener = new OnGetBusLineSearchResult();
		// mBusLineSearch
		// .setOnGetBusLineSearchResultListener(mBusLineSearchResultListener);
		// askStationList();
	}

	private void initViews() {
		mBackUp = (ImageView) findViewById(R.id.busRouteLineDetailBackUp);
		mRouteOutlineDTxt = (TextView) findViewById(R.id.routeOutlineDTxt);
		mRouteDurationDTxt = (TextView) findViewById(R.id.routeListItemDurationDTxt);
		mRouteOnFootDTxt = (TextView) findViewById(R.id.routeListItemOnFootDTxt);
		mRouteStationNumDTxt = (TextView) findViewById(R.id.routeListItemStationNumDTxt);
		setRouteOutline();

		mDetailList = (ListView) findViewById(R.id.busLineDetailList);
		mAdapter = new DetailAdapter(mContext);
		mDetailList.setAdapter(mAdapter);
		mBackUp.setOnClickListener(this);
	}

	private void setRouteOutline() {
		if (mBusRouteLineInfo != null) {
			mRouteOutlineDTxt.setText(mBusRouteLineInfo.getRouteOutlineStr());
			mRouteDurationDTxt.setText(mBusRouteLineInfo.getDuration());
			mRouteOnFootDTxt.setText(mBusRouteLineInfo.getOnFoot());
			mRouteStationNumDTxt.setText(mBusRouteLineInfo.getStationNum());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBusLineSearch.destroy();
	}

	public class OnGetBusLineSearchResult implements
			OnGetBusLineSearchResultListener {

		@Override
		public void onGetBusLineResult(BusLineResult result) {
			if (result != null && result.error == ERRORNO.NO_ERROR) {
				// List<String> stationList = null;
				// for (BusLineUid lineUid : mBusLinesUid.getUidList()) {
				// if (lineUid.getUid() == result.getUid()) {
				// stationList = new ArrayList<String>();
				// for (BusStation station : result.getStations()) {
				// stationList.add(station.getTitle());
				// }
				// Toast.makeText(mContext, stationList.toString(),
				// Toast.LENGTH_SHORT).show();
				// lineUid.setStationList(stationList);
				// }
				// }
			}
		}
	}

	public class DetailAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private static final int TYPE_START = 0;
		private static final int TYPE_END = 3;
		private static final int TYPE_WALKING = 1;
		private static final int TYPE_BUS = 2;

		public DetailAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mBusRouteLineInfo.getSteps().size() + 2;
		}

		@Override
		public int getViewTypeCount() {
			return 4;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0) {
				return TYPE_START;
			}
			if (position == getCount() - 1) {
				return TYPE_END;
			}
			switch (mBusRouteLineInfo.getSteps().get(position - 1)
					.getStepType()) {
			case WAKLING:
				return TYPE_WALKING;
			case BUSLINE:
				return TYPE_BUS;
			case SUBWAY:
				return TYPE_BUS;
			}
			return -1;
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				switch (getItemViewType(position)) {
				case TYPE_START:
					convertView = inflater.inflate(
							R.layout.detail_list_item_start, null);
					holder.startTxt = (TextView) convertView
							.findViewById(R.id.detailStartTxt);
					break;
				case TYPE_END:
					convertView = inflater.inflate(
							R.layout.detail_list_item_end, null);
					holder.endTxt = (TextView) convertView
							.findViewById(R.id.detailEndTxt);
					break;
				case TYPE_WALKING:
					convertView = inflater.inflate(
							R.layout.detail_list_item_on_foot, null);
					holder.onFootTxt = (TextView) convertView
							.findViewById(R.id.detailOnFootTxt);
					break;
				case TYPE_BUS:
					convertView = inflater.inflate(
							R.layout.detail_list_item_bus, null);
					holder.beginPointTxt = (TextView) convertView
							.findViewById(R.id.detailBeginPoint);
					holder.endPointTxt = (TextView) convertView
							.findViewById(R.id.detailEndPoint);
					holder.lineTitleTxt = (TextView) convertView
							.findViewById(R.id.detailLineTitle);
					holder.stationNumBtn = (Button) convertView
							.findViewById(R.id.detailStationNum);
					break;

				default:
					break;
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			switch (getItemViewType(position)) {
			case TYPE_START:
				holder.startTxt.setText(mBusRouteLineInfo.getStartNode()
						.getTitle());
				break;
			case TYPE_END:
				holder.endTxt
						.setText(mBusRouteLineInfo.getEndNode().getTitle());
				break;
			case TYPE_WALKING:
				holder.onFootTxt.setText(mBusRouteLineInfo.getSteps()
						.get(position - 1).getInstructions());
				break;
			case TYPE_BUS:
				final String beginStr = mBusRouteLineInfo.getSteps()
						.get(position - 1).getEntrace().getTitle();
				final String endStr = mBusRouteLineInfo.getSteps()
						.get(position - 1).getExit().getTitle();
				holder.beginPointTxt.setText(beginStr);
				holder.endPointTxt.setText(endStr);
				holder.lineTitleTxt.setText(mBusRouteLineInfo.getSteps()
						.get(position - 1).getInstructions());
				holder.stationNumBtn.setText(mBusRouteLineInfo.getSteps()
						.get(position - 1).getVehicleInfo().getPassStationNum()
						+ "站");
				final TextView tv = holder.lineTitleTxt;
				holder.stationNumBtn.setOnClickListener(null);
				holder.stationNumBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						expandOrHideStation(position, beginStr, endStr, tv);
					}

				});
				break;

			default:
				break;
			}
			return convertView;
		}

		class ViewHolder {
			private TextView startTxt;
			private TextView endTxt;
			private TextView onFootTxt;
			private TextView beginPointTxt;
			private TextView endPointTxt;
			private TextView lineTitleTxt;
			private Button stationNumBtn;
		}

	}
	
	private void expandOrHideText(TextView tv,BusLineUid busLineUid){
		if (isExpand) {
			tv.setText(tv.getText()
					+ stationListToString(busLineUid
							.getStationList()));
			isExpand = false;
		} else {
			tv.setText(tv
					.getText()
					.toString()
					.replace(
							stationListToString(busLineUid
									.getStationList()), ""));
			isExpand = true;
		}
	}
	
	private void expandOrHideStation(int position, final String beginStr,
			final String endStr, final TextView tv) {
		for (final BusLineUid busLineUid : mBusLinesUid.getUidList()) {
			if (busLineUid.getPosition() == position) {
				if (busLineUid.getStationList() != null) {
					expandOrHideText(tv, busLineUid);
				} else {
					mBusLineSearch
							.setOnGetBusLineSearchResultListener(new OnGetBusLineSearchResultListener() {

								@Override
								public void onGetBusLineResult(
										BusLineResult result) {
									if (result != null
											&& result.error == ERRORNO.NO_ERROR) {
										List<String> stationList = new ArrayList<String>();
										for (BusStation station : result
												.getStations()) {
											stationList.add(station.getTitle());
										}
										busLineUid.setStationList(getStations(
												stationList, beginStr, endStr));
										expandOrHideText(tv, busLineUid);

									} else {

									}
								}
							});
					mBusLineSearch.searchBusLine(new BusLineSearchOption()
							.city(TransitResult.getCity()).uid(
									busLineUid.getUid()));
				}
			}
		}
	}

	private List<String> getStations(List<String> stationsList,
			String beginStr, String endStr) {
		int beginIndex = stationsList.indexOf(beginStr);
		if (beginIndex == -1) {
			if (beginStr.endsWith("站")) {
				beginStr = beginStr.replace("站", "");
				beginIndex = stationsList.indexOf(beginStr);
				beginIndex++;
			}
		}
		int endIndex = stationsList.indexOf(endStr);
		if (endIndex == -1) {
			if (endStr.endsWith("站")) {
				endStr = endStr.replace("站", "");
				endIndex = stationsList.indexOf(endStr);
			}
		}
		return stationsList.subList(beginIndex, endIndex);
	}

	private String stationListToString(List<String> stationList) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("\n");
		for (String str : stationList) {
			sb.append(str).append("\n");
		}
		return sb.toString();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.busRouteLineDetailBackUp:
			finish();
			break;

		default:
			break;
		}
	}
}
