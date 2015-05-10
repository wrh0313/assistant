package com.wrh.assistant.model;

import com.baidu.mapapi.search.route.TransitRouteResult;

public class TransitResult {
	private static TransitRouteResult result;
	private static String city;

	public static void setCity(String city) {
		TransitResult.city = city;
	}

	public static String getCity() {
		return city;
	}

	public static TransitRouteResult getResult() {
		return result;
	}

	public static void setResult(TransitRouteResult result) {
		TransitResult.result = result;
	}

}
