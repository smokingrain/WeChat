package com.xk.bean;

import java.util.Map;

import org.eclipse.swt.graphics.Image;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xk.utils.JSONUtil;

/**
 * 用途：登陆用户结构体
 *
 * @author xiaokui
 * @date 2017年1月3日
 */
public class User {

	public Long Uin;
	public String UserName;
	public String NickName;
	public String HeadImgUrl;
	public String RemarkName;
	public String PYInitial;
	public String PYQuanPin;
	public String RemarkPYInitial;
	public String RemarkPYQuanPin;
	public Integer HideInputBarFlag;
	public Integer StarFriend;
	public Integer Sex;
	public String Signature;
	public Integer AppAccountFlag;
	public Integer VerifyFlag;
	public Integer ContactFlag;
	public Integer WebWxPluginSwitch;
	public Integer HeadImgFlag;
	public Integer SnsFlag;
	
	@JsonIgnore
	public Image head;
	
	public User() {
		
	}
	
	public User(Long uin, String userName, String nickName, String headImgUrl, String remarkName, String pYInitial,
			String pYQuanPin, String remarkPYInitial, String remarkPYQuanPin, Integer hideInputBarFlag,
			Integer starFriend, Integer sex, String signature, Integer appAccountFlag, Integer verifyFlag,
			Integer contactFlag, Integer webWxPluginSwitch, Integer headImgFlag, Integer snsFlag) {
		super();
		Uin = uin;
		UserName = userName;
		NickName = nickName;
		HeadImgUrl = headImgUrl;
		RemarkName = remarkName;
		PYInitial = pYInitial;
		PYQuanPin = pYQuanPin;
		RemarkPYInitial = remarkPYInitial;
		RemarkPYQuanPin = remarkPYQuanPin;
		HideInputBarFlag = hideInputBarFlag;
		StarFriend = starFriend;
		Sex = sex;
		Signature = signature;
		AppAccountFlag = appAccountFlag;
		VerifyFlag = verifyFlag;
		ContactFlag = contactFlag;
		WebWxPluginSwitch = webWxPluginSwitch;
		HeadImgFlag = headImgFlag;
		SnsFlag = snsFlag;
	}
	
	public static User fromMap(Map<String, Object> map) {
		String json = JSONUtil.toJson(map);
		return JSONUtil.toBean(json, User.class);
	}
	
}
