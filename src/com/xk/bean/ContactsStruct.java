package com.xk.bean;

import java.util.List;
import java.util.Map;

import com.xk.utils.JSONUtil;

public class ContactsStruct {
	public Integer Uin;
	public String UserName;
	public String NickName;
	public String HeadImgUrl;
	public Integer ContactFlag;
	public Integer MemberCount;
	public List<MemberStruct> MemberList;
	public String RemarkName;
	public Integer HideInputBarFlag;
	public Integer Sex;
	public String Signature;
	public Integer VerifyFlag;
	public Integer OwnerUin;
	public String PYInitial;
	public String PYQuanPin;
	public String RemarkPYInitial;
	public String RemarkPYQuanPin;
	public Integer StarFriend;
	public Integer AppAccountFlag;
	public Integer Statues;
	public Integer AttrStatus;
	public String Province;
	public String City;
	public String Alias;
	public Integer SnsFlag;
	public Integer UniFriend;
	public String DisplayName;
	public Integer ChatRoomId;
	public String KeyWord;
	public String EncryChatRoomId;
	
	public ContactsStruct(){
		
	}
	
	
	public ContactsStruct(Integer uin, String userName, String nickName, String headImgUrl, Integer contactFlag,
			Integer memberCount, List<MemberStruct> memberList, String remarkName, Integer hideInputBarFlag,
			Integer sex, String signature, Integer verifyFlag, Integer ownerUin, String pYInitial, String pYQuanPin,
			String remarkPYInitial, String remarkPYQuanPin, Integer starFriend, Integer appAccountFlag, Integer statues,
			Integer attrStatus, String province, String city, String alias, Integer snsFlag, Integer uniFriend,
			String displayName, Integer chatRoomId, String keyWord, String encryChatRoomId) {
		super();
		Uin = uin;
		UserName = userName;
		NickName = nickName;
		HeadImgUrl = headImgUrl;
		ContactFlag = contactFlag;
		MemberCount = memberCount;
		MemberList = memberList;
		RemarkName = remarkName;
		HideInputBarFlag = hideInputBarFlag;
		Sex = sex;
		Signature = signature;
		VerifyFlag = verifyFlag;
		OwnerUin = ownerUin;
		PYInitial = pYInitial;
		PYQuanPin = pYQuanPin;
		RemarkPYInitial = remarkPYInitial;
		RemarkPYQuanPin = remarkPYQuanPin;
		StarFriend = starFriend;
		AppAccountFlag = appAccountFlag;
		Statues = statues;
		AttrStatus = attrStatus;
		Province = province;
		City = city;
		Alias = alias;
		SnsFlag = snsFlag;
		UniFriend = uniFriend;
		DisplayName = displayName;
		ChatRoomId = chatRoomId;
		KeyWord = keyWord;
		EncryChatRoomId = encryChatRoomId;
	}
	
	public static ContactsStruct fromMap(Map<String, Object> map) {
		String json = JSONUtil.toJson(map);
		ContactsStruct conv = JSONUtil.toBean(json, ContactsStruct.class);
		return conv;
	}
	
	public static String getGroupMember(String user, ContactsStruct con) {
		String name = "匿名";
		if(con.UserName.startsWith("@@")) {
			for(MemberStruct member : con.MemberList) {
				if(user.equals(member.UserName)) {
					if(null != member.DisplayName && !member.DisplayName.isEmpty()) {
						name = member.DisplayName.trim();
					}else {
						name = member.NickName;
					}
					break;
				}
			}
		}
		return name;
	}
	
	public static String getContactName(ContactsStruct con) {
		String name = "匿名";
		if (null != con.RemarkName && !con.RemarkName.trim().isEmpty()) {
			name = con.RemarkName;
		} else {
			name = con.NickName;
		}
		return name;
	}
}
