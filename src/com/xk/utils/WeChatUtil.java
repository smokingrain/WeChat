package com.xk.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import com.xk.bean.ContactsStruct;
import com.xk.bean.User;
import com.xk.bean.WeChatSign;
import com.xk.chatlogs.ChatLog;
import com.xk.ui.items.ContactItem;
import com.xk.ui.items.ConvItem;
import com.xk.ui.items.TypeItem;
import com.xk.ui.main.MainWindow;
import com.xk.uiLib.MyList;

public class WeChatUtil {

	
	/**
	 * 用途：发送消息
	 * @date 2016年12月15日
	 * @param msg
	 * @param to
	 * @param sign
	 * @param user
	 */
	public static ChatLog sendMsg(String msg, String to) {
		Map<String, String> params = new HashMap<>();
		params.put("lang", "zh_CN");
		params.put("pass_ticket", Constant.sign.pass_ticket);
		Map<String, Object> body = new HashMap<>();
		Map<String, Object> bodyInner = new HashMap<String, Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		body.put("BaseRequest", bodyInner);
		body.put("Scene", 0);
		Map<String, Object> msgMap = new HashMap<>();
		long cur = System.currentTimeMillis();
		msgMap.put("ClientMsgId", cur);
		msgMap.put("Content", msg);
		msgMap.put("Type", 1);
		msgMap.put("ToUserName", to);
		msgMap.put("FromUserName", Constant.user.UserName);
		msgMap.put("LocalID", cur);
		body.put("Msg", msgMap);
		
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.postBody(Constant.SEND_MSG, params, JSONUtil.toJson(body));
			Map<String, Object> rstMap= JSONUtil.fromJson(result);
			Map<String, Object> obj = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != obj && new Integer(0).equals(obj.get("Ret"))) {
				System.out.println("msg : " + msg +"-> 发送成功！！");
				ChatLog log = new ChatLog();
				log.createTime = System.currentTimeMillis();
				log.toId = to;
				log.fromId = Constant.user.UserName;
				log.msgid = rstMap.get("MsgID").toString();
				log.newMsgId = Long.parseLong(rstMap.get("LocalID").toString());
				log.msgType = 1;
				log.content = msg;
				return log;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 用途：加载会话
	 * @date 2016年12月14日
	 * @param ctItem
	 */
	public static List<String> loadConvers(TypeItem ctItem, MainWindow window) {
		List<String> allGroups = new ArrayList<String>();
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String,Map<String,String>> bodyMap = new HashMap<String,Map<String,String>>();
		Map<String,String> bodyInner = new HashMap<String,String>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		bodyMap.put("BaseRequest", bodyInner);
		
		try {
			String result = hu.postBody(Constant.GET_INIT.replace("{TIME}", System.currentTimeMillis() + ""), JSONUtil.toJson(bodyMap));
			Map<String, Object> rstMap = JSONUtil.fromJson(result);
			Map<String, Object> baseResponse = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != baseResponse && new Integer(0).equals(baseResponse.get("Ret"))) {
				List<Map<String, Object>> contactList = (List<Map<String, Object>>) rstMap.get("ContactList");
				if(null != contactList) {
					for(Map<String, Object> cmap : contactList) {
						ContactsStruct convs = ContactsStruct.fromMap(cmap);
						window.addConversition(convs);
						
						if(convs.UserName.indexOf("@@") > -1) {
							allGroups.add(convs.UserName);
						}
					}
					System.out.println("convers loaded!!");
					Map<String, Object> SyncKey = (Map<String, Object>) rstMap.get("SyncKey");
					flushSyncKey(SyncKey);
					
					Constant.user = User.fromMap( (Map<String, Object>) rstMap.get("User"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("获取会话失败");
			System.exit(0);
		}
		return allGroups;
	}
	
	/**
	 * 用途：刷新同步key
	 * @date 2016年12月15日
	 * @param SyncKey
	 * @param sign
	 */
	public static void flushSyncKey(Map<String, Object> SyncKey) {
		if(null == SyncKey){
			return ;
		}
		List<Map<String, Integer>> List = (java.util.List<Map<String, Integer>>) SyncKey.get("List");
		StringBuffer sb = new StringBuffer();
		for(Map<String, Integer> v : List) {
			Integer Key = v.get("Key");
			Integer Val = v.get("Val");
			sb.append(Key).append("_").append(Val).append("|");
		}
		try {
			Constant.sign.synckey = URLEncoder.encode(sb.substring(0, sb.length() - 1), "UTF-8");//sb.substring(0, sb.length() - 1);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Constant.sign.syncKeyOringe = SyncKey;
	}
	
	/**
	 * 用途：开启消息通知
	 * @date 2016年12月14日
	 */
	public static void startNotify(){
		HTTPUtil hu = HTTPUtil.getInstance();
		String url = Constant.STATUS_NOTIFY + "?lang=zh_CN&pass_ticket=" + Constant.sign.pass_ticket;
		Map<String, Object> body = new HashMap<String, Object>();
		Map<String, Object> BaseRequest = new HashMap<>();
		BaseRequest.put("DeviceID", Constant.sign.deviceid);
		BaseRequest.put("Sid", Constant.sign.wxsid);
		BaseRequest.put("Skey", Constant.sign.skey);
		BaseRequest.put("Uin", Constant.sign.wxuin);
		body.put("BaseRequest", BaseRequest);
		body.put("ClientMsgId", System.currentTimeMillis());
		body.put("Code", 3);
		body.put("FromUserName", Constant.user.UserName);
		body.put("ToUserName", Constant.user.UserName);
		
		try {
			String result = hu.postBody(url, JSONUtil.toJson(body));
			Map<String, Object> rst = JSONUtil.fromJson(result);
			Map<String, Object> BaseResponse = (Map<String, Object>) rst.get("BaseResponse");
			if(new Integer(0).equals(BaseResponse.get("Ret"))) {
				System.out.println("消息提醒成功！！！！");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 用途：抓取联系人,返回所有群组
	 * @date 2016年12月14日
	 * @param ctItem
	 */
	public static List<String> loadContacts(TypeItem ctItem, MainWindow window) {
		List<String> allGroups = new ArrayList<String>();
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String, String> params = new HashMap<String, String>();
		params.put("pass_ticket", Constant.sign.pass_ticket);
		params.put("r", System.currentTimeMillis() + "");
		params.put("seq", "0");
		params.put("skey", Constant.sign.skey);
		params.put("lang", "zh_CN");
		
		try {
			String result = hu.readJsonfromURL2(Constant.GET_CONTACT, params);
			System.out.println(result);
			Map<String, Object> rstMap = JSONUtil.fromJson(result);
			Map<String, Object> baseResponse = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != baseResponse && new Integer(0).equals(baseResponse.get("Ret"))) {
				List<Map<String, Object>> contactList = (List<Map<String, Object>>) rstMap.get("MemberList");
				if(null != contactList) {
					for(Map<String, Object> cmap : contactList) {
						ContactsStruct convs = ContactsStruct.fromMap(cmap);
						Constant.contacts.put(convs.UserName, convs);
						String headUrl = Constant.BASE_URL + convs.HeadImgUrl;
						convs.head = ImageCache.getUserHeadCache(convs.UserName, headUrl, null, 50, 50);
						String nick = convs.NickName;
						String remark = convs.RemarkName;
						String name = (null == remark || remark.trim().isEmpty()) ? nick : remark; 
						System.out.println("load contact " + name + "   " + convs.UserName);
						ContactItem ci = new ContactItem(convs, false, name);
						MyList list = window.lists.get(ctItem);
						list.addItem(ci);
						if(convs.UserName.indexOf("@@") > -1) {
							allGroups.add(convs.UserName);
						}
					}
					System.out.println("load contacts over!!");
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allGroups;
	}
	
	
	/**
	 * 用途：抓取聊天图片
	 * @date 2016年12月20日
	 * @return
	 */
	public static ImageLoader loadImage(String msgId, String type) {
		String url = Constant.LOAD_IMG;
		Map<String, String> params = new HashMap<String, String>();
		params.put("MsgID", msgId);
		try {
			params.put("skey", URLEncoder.encode(Constant.sign.skey, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(null != type && !type.trim().isEmpty()) {
			params.put("type", type);
		}
		HTTPUtil hu = HTTPUtil.getInstance();
		InputStream in = hu.getInput(url, params);
		try {
			
			ImageLoader load = new ImageLoader();
			load.load(in);
			return load;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(null != in) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	
	/**
	 * 用途：退出微信
	 * @date 2016年12月20日
	 * @param sign
	 */
	public static void exitWeChat() {
		String url = Constant.LOGOUT_URL.replace("{SKEY}", Constant.sign.skey);
		Map<String, String> params = new HashMap<String, String>();
		params.put("sid", Constant.sign.wxsid);
		params.put("uin", Constant.sign.wxuin);
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			hu.readJsonfromURL2(url, params);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 用途：抓取群组
	 * @date 2016年12月14日
	 * @param ctItem
	 */
	public static void loadGroups(TypeItem ctItem, List<String> groups, MainWindow window) {
		if(null == groups || groups.size() == 0) {
			return ;
		}
		
		List<Map<String, String>> gs = new ArrayList<>();
		for(String name : groups) {
			Map<String, String> map = new HashMap<>();
			map.put("UserName", name);
			map.put("ChatRoomId", "");
			gs.add(map);
		}
		
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String, Object> bodyMap = new HashMap<String, Object>();
		Map<String,String> bodyInner = new HashMap<String,String>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		bodyMap.put("BaseRequest", bodyInner);
		bodyMap.put("Count", gs.size());
		bodyMap.put("List", gs);
		
		try {
			String url = Constant.GET_GROUPS.replace("{TIME}", System.currentTimeMillis() + "").replace("{TICKET}", Constant.sign.pass_ticket);
			String result = hu.postBody(url, JSONUtil.toJson(bodyMap));
			Map<String, Object> rstMap = JSONUtil.fromJson(result);
			Map<String, Object> baseResponse = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != baseResponse && new Integer(0).equals(baseResponse.get("Ret"))) {
				List<Map<String, Object>> contactList = (List<Map<String, Object>>) rstMap.get("ContactList");
				if(null != contactList) {
					for(Map<String, Object> cmap : contactList) {
						ContactsStruct convs = ContactsStruct.fromMap(cmap);
						Constant.contacts.put(convs.UserName, convs);
						String headUrl = Constant.BASE_URL + convs.HeadImgUrl;
						convs.head = ImageCache.getUserHeadCache(convs.UserName, headUrl, null, 50, 50);
						String nick = convs.NickName;
						String remark = convs.RemarkName;
						String name = (null == remark || remark.trim().isEmpty()) ? nick : remark; 
						System.out.println("load group " + name + convs.UserName);
						ContactItem ci = new ContactItem(convs, false, name);
						MyList list = window.lists.get(ctItem);
						list.addItem(ci);
					}
					System.out.println("load Group over!!");
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
