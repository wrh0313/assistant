package com.wrh.assistant.model;

import com.baidu.mapapi.search.route.WalkingRouteResult;

public class WalkingResult {
	private static WalkingRouteResult walkingRouteResult;

	public static WalkingRouteResult getWalkingRouteResult() {
		return walkingRouteResult;
	}

	public static void setWalkingRouteResult(
			WalkingRouteResult walkingRouteResult) {
		WalkingResult.walkingRouteResult = walkingRouteResult;
	}

}
