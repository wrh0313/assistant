package com.wrh.assistant.model;

import com.baidu.mapapi.search.route.DrivingRouteResult;

public class DrivingResult {
	private static DrivingRouteResult drivingRouteResult;

	public static DrivingRouteResult getDrivingRouteResult() {
		return drivingRouteResult;
	}

	public static void setDrivingRouteResult(
			DrivingRouteResult drivingRouteResult) {
		DrivingResult.drivingRouteResult = drivingRouteResult;
	}

}
