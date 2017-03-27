package com.xk.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import com.xk.bean.ContactsStruct;
import com.xk.bean.User;
import com.xk.bean.WeChatSign;
import com.xk.chatlogs.ChatLog;
import com.xk.chatlogs.ChatLogCache;
import com.xk.ui.items.ContactItem;
import com.xk.ui.items.ConvItem;
import com.xk.ui.items.TypeItem;
import com.xk.ui.main.MainWindow;
import com.xk.uiLib.MyList;

public class WeChatUtil {

	private static Timer timer;
	
	/**
	 * 用途：置顶取消置顶
	 * @date 2017年2月10日
	 * @param cs 用户对象
	 * @param op 0 取消置顶，1 置顶
	 * @return
	 */
	public static boolean OPlog(ContactsStruct cs, int op){
		Map<String, String> params = new HashMap<>();
		params.put("pass_ticket", Constant.sign.pass_ticket);
		params.put("lang", "zh_CN");
		Map<String, Object> body = new HashMap<>();
		Map<String, Object> bodyInner = new HashMap<String, Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		body.put("BaseRequest", bodyInner);
		body.put("CmdId", 3);
		body.put("OP", op);
		body.put("RemarkName", cs.RemarkName);
		body.put("UserName", cs.UserName);
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.postBody(Constant.OP_LOG, params, JSONUtil.toJson(body));
			Map<String, Object> rstMap= JSONUtil.fromJson(result);
			Map<String, Object> obj = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != obj && new Integer(0).equals(obj.get("Ret"))) {
				System.out.println("置顶成功！！");
				return true;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}
	
	/**
	 * 用途：上传聊天图片
	 * @date 2017年1月5日
	 * @param file
	 * @return
	 */
	private static String uploadImage(File file) {
		HTTPUtil hu = HTTPUtil.getInstance();
		String name = file.getName();
		String minaType = Constant.imgTypes.get(name.substring(name.lastIndexOf(".") + 1).toLowerCase());
		Long flen = file.length();
		Date lastModify = new Date(file.lastModified());
		Map<String, Object> bodyInner = new HashMap<String, Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		Map<String, Object> req = new HashMap<String, Object>();
		req.put("BaseRequest", bodyInner);
		req.put("ClientMediaId", System.currentTimeMillis());
		req.put("TotalLen", flen);
		req.put("StartPos", 0);
		req.put("DataLen", flen);
		req.put("MediaType", 4);
		Map<String, String> params = new HashMap<String, String>();
		params.put("f", "json");
		params.put("id", "WU_FILE_" + Constant.file_index++);
		params.put("type", minaType);
		params.put("lastModifiedDate", lastModify.toString());
		params.put("size", String.valueOf(flen));
		params.put("mediatype", name.toLowerCase().endsWith(".gif") ? "doc" : "pic");
		params.put("uploadmediarequest", JSONUtil.toJson(req));
		params.put("webwx_data_ticket", hu.getCookie("webwx_data_ticket"));
		params.put("pass_ticket", Constant.sign.pass_ticket);
		Map<String, File> files = new HashMap<String, File>();
		files.put("filename", file);
		String result = hu.httpPostFile(Constant.UPLOAD_MEDIA, params, files);
		Map<String, Object> rst = JSONUtil.fromJson(result);
		if(null != rst) {
			return (String) rst.get("MediaId");
		}
		return null;
	}
	
	/**
	 * 用途：发送聊天图片
	 * @date 2017年1月5日
	 * @param img
	 * @param to
	 * @return
	 */
	public static ChatLog sendImg(File img, String to) {
		String mediaId = uploadImage(img);
		if(null == mediaId) {
			return null;
		}
		boolean gif = img.getName().toLowerCase().endsWith(".gif");
		Map<String, String> params = new HashMap<String, String>();
		params.put("fun", gif ? "sys" : "async");
		params.put("f", "json");
		try {
			params.put("pass_ticket", URLEncoder.encode(Constant.sign.pass_ticket, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Object> body = new HashMap<String, Object>();
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
		msgMap.put("MediaId", mediaId);
		msgMap.put("Type", gif ? 47 : 3);
		msgMap.put("ToUserName", to);
		msgMap.put("FromUserName", Constant.user.UserName);
		msgMap.put("LocalID", cur);
		body.put("Msg", msgMap);
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.postBody(gif ? Constant.SEND_GIF : Constant.SEND_IMG, params, JSONUtil.toJson(body));
			Map<String, Object> rstMap= JSONUtil.fromJson(result);
			Map<String, Object> obj = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != obj && new Integer(0).equals(obj.get("Ret"))) {
				ChatLog log = new ChatLog();
				log.createTime = System.currentTimeMillis();
				log.toId = to;
				log.fromId = Constant.user.UserName;
				log.msgid = rstMap.get("MsgID").toString();
				log.newMsgId = Long.parseLong(rstMap.get("LocalID").toString());
				log.msgType = 3;
				log.img = ImageCache.getChatImage(log.msgid, img);
				log.content = "[图片]";
				return log;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
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
				log.newMsgId = Long.parseLong(rstMap.get("MsgID").toString());
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
			Map<String, Object> rstMap = JSONUtil.fromJson(result);
			Map<String, Object> baseResponse = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != baseResponse && new Integer(0).equals(baseResponse.get("Ret"))) {
				List<Map<String, Object>> contactList = (List<Map<String, Object>>) rstMap.get("MemberList");
				if(null != contactList) {
					for(Map<String, Object> cmap : contactList) {
						ContactsStruct convs = ContactsStruct.fromMap(cmap);
						boolean top = convs.ContactFlag == 2049 || convs.ContactFlag == 2051;
						if(top) {
							window.addConversition(convs);
						}
						Constant.contacts.put(convs.UserName, convs);
						String headUrl = Constant.BASE_URL + convs.HeadImgUrl;
						convs.head = ImageCache.getUserHeadCache(convs.UserName, headUrl, null, 50, 50);
						String nick = convs.NickName;
						String remark = convs.RemarkName;
						String name = (null == remark || remark.trim().isEmpty()) ? nick : remark; 
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
	
	/**
	 * 用途：发心跳包，获取微信状态是否有新消息
	 * @date 2016年12月30日
	 * @param conItem
	 */
	public static void syncData(final TypeItem conItem) {
		if(null != timer) {
			return;
		}
		final HTTPUtil hu = HTTPUtil.getInstance();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("_", System.currentTimeMillis() + "");
				params.put("r", (System.currentTimeMillis() + 91136) + "");
				params.put("uin", Constant.sign.wxuin);
				try {
					params.put("sid", URLEncoder.encode(Constant.sign.wxsid, "UTF-8"));
					params.put("skey", URLEncoder.encode(Constant.sign.skey, "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				params.put("deviceid", Constant.sign.deviceid);
				params.put("synckey", Constant.sign.synckey);
				try {
					String rst = hu.getJsonfromURL2(Constant.SYNC_CHECK, params);
					if(null != rst && rst.contains("window.synccheck=")) {
						String result = rst.replace("window.synccheck=", "");
						System.out.println("checksync + " + result);
						Map<String, String> map = JSONUtil.toBean(result, JSONUtil.getCollectionType(Map.class, String.class, String.class));
						if("0".equals(map.get("retcode")) ) {
							String selector = map.get("selector");
							try {
								Integer sele = Integer.parseInt(selector);
								if(sele > 0) {
									webwxsync(conItem);
								}
								
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}else if("1101".equals(map.get("retcode"))) {
							System.out.println("已在其它端登陆！！");
							System.exit(0);
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
		}, 1000, 1000);
	}
	
	
	/**
	 * 用途：同步到数据的时候刷新数据
	 * @date 2016年12月30日
	 * @param conItem
	 */
	private static void webwxsync(final TypeItem conItem) {
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String,Object> bodyMap = new HashMap<String,Object>();
		Map<String,Object> bodyInner = new HashMap<String,Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		bodyMap.put("BaseRequest", bodyInner);
		bodyMap.put("SyncKey", Constant.sign.syncKeyOringe);
		bodyMap.put("rr", System.currentTimeMillis() / 1000 * -1);
		Map<String, String> params = new HashMap<String, String>();
		params.put("sid", Constant.sign.wxsid);
		params.put("lang", "zh_CN");
		params.put("skey", Constant.sign.skey);
		params.put("pass_ticket", Constant.sign.pass_ticket);
		try {
			final MainWindow main = MainWindow.getInstance();
			String result =  hu.postBody(Constant.GET_STATUS, params, JSONUtil.toJson(bodyMap));
			Map<String, Object> rst = JSONUtil.fromJson(result);
			List<Map<String, Object>> modContactList = (List<Map<String, Object>>) rst.get("ModContactList");
			if(null != modContactList) {
				changeUser(modContactList);
			}
			Map<String, Object> BaseResponse = (Map<String, Object>) rst.get("BaseResponse");
			if(new Integer(0).equals(BaseResponse.get("Ret"))) {
				Integer msgCount = (Integer) rst.get("AddMsgCount");
				if(null != msgCount && msgCount > 0) {
					List<Map<String, Object>> AddMsgList = (List<Map<String, Object>>) rst.get("AddMsgList");
					for(Map<String, Object> msg : AddMsgList) {
						Integer MsgType = (Integer) msg.get("MsgType");
						String Content = (String) msg.get("Content");
						String ToUserName = (String) msg.get("ToUserName");
						String FromUserName = (String) msg.get("FromUserName");
						if(51 == MsgType) {
							String StatusNotifyUserName = (String) msg.get("StatusNotifyUserName");
							if(null != StatusNotifyUserName && !main.syncGroup) {
								String[] spl = StatusNotifyUserName.split(",");
								List<String> groups = Arrays.asList(spl);
								WeChatUtil.loadGroups(conItem, groups, main);
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										main.lists.get(conItem).flush();
									}
								});
								main.syncGroup = true;
							}
						}else if(1 == MsgType || 3 == MsgType || 47 == MsgType) {
							if(Constant.FILTER_USERS.contains(FromUserName)) {
								System.out.println("忽略特殊用户信息！！" + Content);
							}else if(FromUserName.equals(Constant.user.UserName)){
								ChatLog log = ChatLog.fromMap(msg);
								if(null != log) {
									ChatLogCache.saveLogs(ToUserName, log);
									main.flushChatView(ToUserName, false );
									System.out.println("来自手机端自己的消息：" + Content);
								}
								
							}else if(FromUserName.startsWith("@@")) {
								ChatLog log = ChatLog.fromMap(msg);
								if(null != log) {
									ChatLogCache.saveLogs(FromUserName, log);
//									String[] splt = Content.split(":<br/>");
//									String sender = ContactsStruct.getGroupMember(splt[0], Constant.contacts.get(FromUserName));
//									String ctt = splt[1].replace("<br/>", "\n");
//									if(ctt.contains("@" + Constant.user.NickName)) {
//										String detail = ctt.replace("@" + Constant.user.NickName, "");
//										String reply = "什么情况?";
//										if(!detail.trim().isEmpty()) {
//											reply = AutoReply.call(detail, sender);
//										}
//										
//										ChatLog replyLog = WeChatUtil.sendMsg(reply, FromUserName);
//										if(null != replyLog) {
//											ChatLogCache.saveLogs(FromUserName, replyLog);
//										}
//									}
									main.flushChatView(FromUserName, true);
								}
								
								
							}else {
								ChatLog log = ChatLog.fromMap(msg);
								if(null != log) {
									ChatLogCache.saveLogs(FromUserName, log);
									String sender = ContactsStruct.getContactName(Constant.contacts.get(FromUserName));
									String ctt = Content.replace("<br/>", "\n");
									System.out.println(sender + " 说：" + ctt);
//									if(!Constant.noReply.contains(FromUserName)) {
//										String reply = AutoReply.call(ctt, sender);
//										ChatLog replyLog = WeChatUtil.sendMsg(reply, FromUserName);
//										if(null != replyLog) {
//											ChatLogCache.saveLogs(FromUserName, replyLog);
//										}
//										
//									}
									main.flushChatView(FromUserName, true);
								}
								
							}
						}
					}
				}
			}
			Map<String, Object> SyncKey = (Map<String, Object>) rst.get("SyncKey");
			WeChatUtil.flushSyncKey(SyncKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 用途：修改用户
	 * @date 2017年2月13日
	 * @param userList
	 */
	private static void changeUser(List<Map<String, Object>> userList) {
		final MainWindow mw = MainWindow.getInstance();
		for(Map<String, Object> map : userList) {
			int ContactFlag = (int) map.get("ContactFlag");
			String user = (String) map.get("UserName");
			mw.topUser(user, ContactFlag == 2049 ? 1 : 0);
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				mw.convers.flush();
			}
		});
		
	}
}
