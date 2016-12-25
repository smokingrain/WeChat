package com.xk.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	
	public static final SimpleDateFormat HOUR_MIN = new SimpleDateFormat("HH:mm");//显示小时，分钟
	private static final String JUST_NOW = "刚刚";
	private static final String A_WEEK_AGO = "一周前";
	
	private static final String[] WEEK_NAMES = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
	
	
	public static String getChatTime(Long time) {
		if(null == time) {
			return "";
		}
		Long now = System.currentTimeMillis();
		Long today = getStartTime();
		Long severDay = 7 * 24 * 60 * 60 * 1000L ;
		if(now - time < 15 * 1000) {//十五秒内的消息
			return JUST_NOW;
		}else if(time > today) {//今天内的消息
			return HOUR_MIN.format(new Date(time));
		}else if(now - time < severDay) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(time);
			int dayinweek = calendar.get(Calendar.DAY_OF_WEEK);
			return WEEK_NAMES[dayinweek - 1];
		}else {
			return A_WEEK_AGO;
		}
		
		
	}
	
	public static Long getStartTime(){  
        Calendar todayStart = Calendar.getInstance();  
        todayStart.set(Calendar.HOUR, 0);  
        todayStart.set(Calendar.MINUTE, 0);  
        todayStart.set(Calendar.SECOND, 0);  
        todayStart.set(Calendar.MILLISECOND, 0);  
        return todayStart.getTime().getTime();  
    }  
	
}
