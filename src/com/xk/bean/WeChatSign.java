package com.xk.bean;

import java.util.Map;


/**
 * 用途：微信登陆信物
 *
 * @author xiaokui
 * @date 2017年1月3日
 */
public class WeChatSign {

	public String skey;
	public String wxsid;
	public String wxuin;
	public String pass_ticket;
	public String deviceid = "e" + System.currentTimeMillis();
	public String synckey;
	public Map<String, Object> syncKeyOringe;
}
