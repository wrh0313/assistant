package com.wrh.assistant.utils;

import java.text.DecimalFormat;

public class TimeAndDistanceUtil {
	public static final int ONE_MINUTE = 1 * 60;
	public static final int ONE_HOUR = ONE_MINUTE * 60;
	public static final int ONE_DAY = ONE_HOUR * 24;
	public static final int ONE_KILOMETER = 1000;
	public static final DecimalFormat decimalFormat = new DecimalFormat(".0");

	public static String parseTime(int seconds) {

		int days = 0;
		int hours = 0;
		int minutes = 0;
		int temp = 0;
		if (seconds < ONE_MINUTE) {
			minutes = 1;
		} else if (seconds > ONE_MINUTE && seconds < ONE_HOUR) {
			minutes = seconds / ONE_MINUTE;
			if (seconds % ONE_MINUTE > 0) {
				minutes = minutes + 1;
			}
		} else if (seconds > ONE_HOUR && seconds < ONE_DAY) {
			hours = seconds / ONE_HOUR;
			temp = seconds % ONE_HOUR;
			if (temp > ONE_MINUTE) {
				minutes = temp / ONE_MINUTE;
			} else {
				minutes = 1;
			}
		} else if (seconds > ONE_DAY) {
			days = seconds / ONE_DAY;
			temp = seconds % ONE_DAY;
			if (temp > ONE_HOUR) {
				hours = temp / ONE_HOUR;
				temp = temp % ONE_HOUR;
				if (temp > ONE_MINUTE) {
					minutes = temp / ONE_MINUTE;

				} else {
					minutes = 1;
				}
			} else if (temp > ONE_MINUTE) {
				minutes = temp / ONE_MINUTE;
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("约");
		if (days > 0) {
			sb.append(days);
			sb.append("天");
		}
		if (hours > 0) {
			sb.append(hours);
			sb.append("小时");
		}
		if (minutes > 0) {
			sb.append(minutes);
			sb.append("分钟");
		}
		return sb.toString();
	}

	public static String parseDistance(int m) {
		String result = null;
		if (m < ONE_KILOMETER) {
			result = "步行" + m + "米";
		} else if (m > ONE_KILOMETER) {
			String dis = decimalFormat.format(((double) m / ONE_KILOMETER));
			result = "步行" + dis + "公里";
		}
		return result;
	}
}
