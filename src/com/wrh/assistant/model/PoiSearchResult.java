package com.wrh.assistant.model;

import com.baidu.mapapi.search.poi.PoiResult;

public class PoiSearchResult {
	private static PoiResult poiResult;

	public static PoiResult getPoiResult() {
		return poiResult;
	}

	public static void setPoiResult(PoiResult poiResult) {
		PoiSearchResult.poiResult = poiResult;
	}

}
