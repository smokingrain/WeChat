package com.xk.bean;

import java.util.List;
import java.util.Map;

public class WeChatSign {

	public String skey;
	public String wxsid;
	public String wxuin;
	public String pass_ticket;
	public String deviceid = "e" + System.currentTimeMillis();
	public String synckey;
	public Map<String, Object> syncKeyOringe;
}
