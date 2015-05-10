package com.wrh.assistant.model;

import java.io.Serializable;
import java.util.List;

public class BusLinesUid implements Serializable{
	private List<BusLineUid> uidList;

	public List<BusLineUid> getUidList() {
		return uidList;
	}

	public void setUidList(List<BusLineUid> uidList) {
		this.uidList = uidList;
	}

}
