package com.wrh.assistant.model;

public class BusRouteLineDetail {
	private static BusRouteLineInfo busRouteLineInfo;

	public static BusRouteLineInfo getBusRouteLineInfo() {
		return busRouteLineInfo;
	}

	public static void setBusRouteLineInfo(BusRouteLineInfo busRouteLineInfo) {
		BusRouteLineDetail.busRouteLineInfo = busRouteLineInfo;
	}

}
