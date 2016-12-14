package com.xk.bean;

public class MemberStruct {

	public Long Uin;
	public String UserName;
	public String NickName;
	public Integer AttrStatus;
	public String PYInitial;
	public String PYQuanPin;
	public String RemarkPYInitial;
	public String RemarkPYQuanPin;
	public Integer MemberStatus;
	public String DisplayName;
	public String KeyWord;
	
	public MemberStruct() {
		
	}
	
	public MemberStruct(Long uin, String userName, String nickName, Integer attrStatus, String pYInitial,
			String pYQuanPin, String remarkPYInitial, String remarkPYQuanPin, Integer memberStatus, String displayName,
			String keyWord) {
		super();
		Uin = uin;
		UserName = userName;
		NickName = nickName;
		AttrStatus = attrStatus;
		PYInitial = pYInitial;
		PYQuanPin = pYQuanPin;
		RemarkPYInitial = remarkPYInitial;
		RemarkPYQuanPin = remarkPYQuanPin;
		MemberStatus = memberStatus;
		DisplayName = displayName;
		KeyWord = keyWord;
	}
	
}
