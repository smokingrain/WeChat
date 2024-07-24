package com.xk.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



import org.apache.http.client.ClientProtocolException;
import org.dom4j.Element;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.jsoup.helper.StringUtil;

import com.xk.bean.ContactsStruct;
import com.xk.bean.MemberStruct;
import com.xk.bean.User;
import com.xk.chatlogs.ChatLog;
import com.xk.chatlogs.ChatLogCache;
import com.xk.ui.items.TypeItem;
import com.xk.ui.main.MainWindow;
import com.xk.uiLib.ICallback;
import com.xk.utils.chain.SysMsgChain;
import com.xk.utils.song.SongLocation;

public class WeChatUtil {
	
	private static final Long CHUNK_SIZE = 524288L;

	private static Timer timer;
	
	private static ExecutorService service = Executors.newFixedThreadPool(4);
	
	private static ExecutorService chatRobot = Executors.newFixedThreadPool(4);
	
	public static void addRandomReply(String ctt, String FromUserName, String user) {
		chatRobot.submit(new Runnable() {
			int random = new Random().nextInt(11);
			@Override
			public void run() {
				try {
					Thread.sleep((long) ((random + 0.5) * 1000));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ICallback<String> textCall = new ICallback<String>() {

					@Override
					public String callback(String obj) {
						ChatLog replyLog = ChatLog.createSimpleLog(obj, FromUserName);
						WeChatUtil.sendLog(replyLog, null, null);
						ChatLogCache.saveLogs(FromUserName, replyLog);
						MainWindow.getInstance().flushChatView(FromUserName, true);
						return null;
					}
				};
				
				ICallback<File> imgCall = new ICallback<File>() {

					@Override
					public File callback(File obj) {
						ChatLog log = ChatLog.createImageLog(obj, FromUserName);
						log.persent = 100;
						WeChatUtil.sendLog(log, null, null);
						ChatLogCache.saveLogs(FromUserName, log);
						MainWindow.getInstance().flushChatView(FromUserName, true);
						return null;
					}
				};
				
				ICallback<File> fileCall = new ICallback<File>() {

					@Override
					public File callback(File obj) {
						ChatLog log = ChatLog.createFileLog(obj, FromUserName);
						WeChatUtil.sendLog(log, null, null);
						ChatLogCache.saveLogs(FromUserName, log);
						MainWindow.getInstance().flushChatView(FromUserName, true);
						return null;
					}
				};
				
				AutoReply.call(ctt, user, fileCall, textCall, imgCall);
				
			}
		});
	}
	
	
	public static void sendLog(final ChatLog log, final ICallback process, final ICallback callBack) {
		service.submit(new Runnable() {
			
			@Override
			public void run() {
				if(1 == log.msgType) {
					sendMsg(log, callBack);
				} else if(3 == log.msgType) {
					sendImg(log, process, callBack);
				} else if(6 == log.msgType) {
					sendFile(log, process, callBack);
				}
			}
		});
	}
	
	/**
	 * 修改群名称
	 * 作者 ：肖逵
	 * 时间 ：2024年7月15日 下午7:18:25
	 * @param topic
	 * @param roomName
	 * @return
	 */
	public static String modifyRoomName(String topic, String roomName) {
		Map<String, String> params = new HashMap<>();
		params.put("pass_ticket", Constant.sign.pass_ticket);
		params.put("fun", "modtopic");
		
		Map<String, Object> body = new HashMap<>();
		Map<String, Object> bodyInner = new HashMap<String, Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		body.put("BaseRequest", bodyInner);
		body.put("ChatRoomName", roomName);
		body.put("NewTopic", topic);
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.postBody(String.format(Constant.UPDATE_ROOM, Constant.HOST), params, JSONUtil.toJson(body));
			Map<String, Object> rstMap= JSONUtil.fromJson(result);
			Integer MemberCount = Integer.parseInt(String.valueOf(rstMap.get("MemberCount")));
			return MemberCount >= 0 ? topic : null;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 管理踢人
	 * 作者 ：肖逵
	 * 时间 ：2024年7月15日 下午7:22:14
	 * @param members
	 * @param roomName
	 * @return
	 */
	public static boolean kickRoomMember(List<String> members, String roomName) {
		Map<String, String> params = new HashMap<>();
		params.put("pass_ticket", Constant.sign.pass_ticket);
		params.put("fun", "delmember");
		
		Map<String, Object> body = new HashMap<>();
		Map<String, Object> bodyInner = new HashMap<String, Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		body.put("BaseRequest", bodyInner);
		body.put("ChatRoomName", roomName);
		body.put("DelMemberList", String.join(",", members));
		
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.postBody(String.format(Constant.UPDATE_ROOM, Constant.HOST), params, JSONUtil.toJson(body));
			Map<String, Object> rstMap= JSONUtil.fromJson(result);
			Integer MemberCount = Integer.parseInt(String.valueOf(rstMap.get("MemberCount")));
			return MemberCount.equals(new Integer(0));
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
	 * 拉人进群
	 * 作者 ：肖逵
	 * 时间 ：2024年7月15日 下午7:22:25
	 * @param members
	 * @param roomName
	 * @return
	 */
	public static boolean addRoomMember(List<String> members, String roomName) {
		Map<String, String> params = new HashMap<>();
		params.put("pass_ticket", Constant.sign.pass_ticket);
		params.put("fun", "addmember");
		
		Map<String, Object> body = new HashMap<>();
		Map<String, Object> bodyInner = new HashMap<String, Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		body.put("BaseRequest", bodyInner);
		body.put("ChatRoomName", roomName);
		body.put("AddMemberList", String.join(",", members));
		
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.postBody(String.format(Constant.UPDATE_ROOM, Constant.HOST), params, JSONUtil.toJson(body));
			Map<String, Object> rstMap= JSONUtil.fromJson(result);
			Integer MemberCount = Integer.parseInt(String.valueOf(rstMap.get("MemberCount")));
			return MemberCount > 0;
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
	 * 创建群聊
	 * 作者 ：肖逵
	 * 时间 ：2024年7月15日 下午7:01:58
	 * @param members
	 * @param roomName
	 * @return
	 */
	public static String createChatRoom(List<String> members, String topic) {
		Map<String, String> params = new HashMap<>();
		params.put("pass_ticket", Constant.sign.pass_ticket);
		params.put("r", String.valueOf(System.currentTimeMillis()));
		
		Map<String, Object> body = new HashMap<>();
		Map<String, Object> bodyInner = new HashMap<String, Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		body.put("BaseRequest", bodyInner);
		body.put("MemberCount", members.size());
		List<Map<String, String>> memberList = new ArrayList<Map<String,String>>();
		members.forEach(str -> {
			Map<String, String> member = new HashMap<String, String>();
			member.put("UserName", str);
			memberList.add(member);
		});
		body.put("MemberList", memberList);
		body.put("Topic", null == topic ? "" : topic);
		
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.postBody(String.format(Constant.CREATE_ROOM, Constant.HOST), params, JSONUtil.toJson(body));
			Map<String, Object> rstMap= JSONUtil.fromJson(result);
			String chatRoomName = String.valueOf(rstMap.get("ChatRoomName"));
			return chatRoomName;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
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
			String result = hu.postBody(String.format(Constant.OP_LOG, Constant.HOST), params, JSONUtil.toJson(body));
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
	private static String uploadFile(File file, ICallback<Long> callBack, String fromUserName, String toUserName, String mediaType) {
		HTTPUtil hu = HTTPUtil.getInstance();
		String name = file.getName();
		String type = Constant.mediaTypes.get(name.substring(name.lastIndexOf(".") + 1).toLowerCase());
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
		req.put("UploadType", 2);
		req.put("FileMd5", Md5.MD5(file));
		req.put("FromUserName", fromUserName);
		req.put("ToUserName", toUserName);
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", file.getName());
		params.put("id", "WU_FILE_" + Constant.file_index++);
		params.put("type", null == type ? "application/octet-stream" : type);
		//Mon Apr 29 2019 14:05:40 GMT 0800 (中国标准时间)
		
		params.put("lastModifiedDate", new SimpleDateFormat("E MMM d yyyy HH:mm:ss ", Locale.US).format(lastModify) + "GMT 0800 (中国标准时间)");
		params.put("size", String.valueOf(flen));
		params.put("mediatype", mediaType);
		params.put("uploadmediarequest", JSONUtil.toJson(req));
		params.put("webwx_data_ticket", hu.getCookie("webwx_data_ticket"));
		try {
			params.put("pass_ticket", URLDecoder.decode(Constant.sign.pass_ticket, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, File> files = new HashMap<String, File>();
		files.put("filename", file);
		
		int chunks = new BigDecimal(flen).divide(new BigDecimal(CHUNK_SIZE), 0, BigDecimal.ROUND_UP).intValue();
		long total = 0;
		for(int chunk = 0; chunk < chunks; chunk++) {
			long start = chunk * CHUNK_SIZE;
            long size = Math.min(CHUNK_SIZE, flen - start);
            total += size;
            params.put("chunks", String.valueOf(chunks));
            params.put("chunk", String.valueOf(chunk));
			String result = hu.httpPostFile(String.format(Constant.UPLOAD_MEDIA, Constant.HOST), params, files, callBack, start, size);
			System.out.println(result);
			Map<String, Object> rst = JSONUtil.fromJson(result);
			if(null != rst) {
				String mediaId =  (String) rst.get("MediaId");
				if(!StringUtil.isBlank(mediaId)) {
					return mediaId;
				}
			}
		}
		System.out.println("total = " + total + ", flen = " + flen);
		return null;
	}
	
	/**
	 * 用途：发送文件
	 * @param file
	 * @param to
	 * @return
	 * @author xiaokui
	 */
	public static ChatLog sendFile(ChatLog log, ICallback<Long> process, ICallback<ChatLog> callBack) {
		String mediaId = uploadFile(log.file, process, Constant.user.UserName, log.toId, "doc");
		if(null == mediaId) {
			callBack.callback(null);
			return null;
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("fun", "async");
		params.put("f", "json");
		try {
			params.put("pass_ticket", URLEncoder.encode(Constant.sign.pass_ticket, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element root = XMLUtils.createElement("appmsg");
		root.addAttribute("appid", "wxeb7ec651dd0aefa9");
		root.addAttribute("sdkver", "");
		root.addElement("title").setText(log.file.getName());
		root.addElement("des");
		root.addElement("action");
		root.addElement("type").setText("6");
		root.addElement("content");
		root.addElement("url");
		root.addElement("rowurl");
		Element appattach = root.addElement("appattach");
		appattach.addElement("totallen").setText(String.valueOf(log.file.length()));
		appattach.addElement("attachid").setText(mediaId);
		root.addElement("extinfo");
		
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
		msgMap.put("Type", 6);
		msgMap.put("ToUserName", log.toId);
		msgMap.put("FromUserName", Constant.user.UserName);
		msgMap.put("LocalID", cur);
		msgMap.put("Content", root.asXML());
		body.put("Msg", msgMap);
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.postBody(String.format(Constant.SEND_FILE, Constant.HOST), params, JSONUtil.toJson(body));
			Map<String, Object> rstMap= JSONUtil.fromJson(result);
			Map<String, Object> obj = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != obj && new Integer(0).equals(obj.get("Ret"))) {
				log.msgid = rstMap.get("MsgID").toString();
				log.newMsgId = Long.parseLong(rstMap.get("LocalID").toString());
				callBack.callback(log);
				WeChatUtil.statusNotify(Constant.user.UserName, log.toId);
				return log;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 用途：发送聊天图片
	 * @date 2017年1月5日
	 * @param ChatLog
	 * @param process 上传进度
	 * @param callBack 发送成功回调
	 * @return
	 */
	public static ChatLog sendImg(ChatLog log, ICallback<Long> process, ICallback<ChatLog> callBack) {
		boolean gif = log.file.getName().toLowerCase().endsWith(".gif");
		String mediaId = uploadFile(log.file, process, Constant.user.UserName, log.toId, gif ? "doc" : "pic");
		if(null == mediaId) {
			callBack.callback(null);
			return null;
		}
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
		msgMap.put("ToUserName", log.toId);
		msgMap.put("FromUserName", Constant.user.UserName);
		msgMap.put("LocalID", cur);
		body.put("Msg", msgMap);
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.postBody(gif ? String.format(Constant.SEND_GIF, Constant.HOST) : String.format(Constant.SEND_IMG, Constant.HOST), params, JSONUtil.toJson(body));
			Map<String, Object> rstMap= JSONUtil.fromJson(result);
			Map<String, Object> obj = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != obj && new Integer(0).equals(obj.get("Ret"))) {
				log.msgid = rstMap.get("MsgID").toString();
				log.newMsgId = Long.parseLong(rstMap.get("LocalID").toString());
				if(null != callBack) {
					callBack.callback(log);
				}
				WeChatUtil.statusNotify(Constant.user.UserName, log.toId);
				return log;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(null != callBack) {
			callBack.callback(null);
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
	public static ChatLog sendMsg(ChatLog log, ICallback callBack) {
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
		msgMap.put("Content", log.content);
		msgMap.put("Type", 1);
		msgMap.put("ToUserName", log.toId);
		msgMap.put("FromUserName", Constant.user.UserName);
		msgMap.put("LocalID", cur);
		body.put("Msg", msgMap);
		
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.postBody(String.format(Constant.SEND_MSG, Constant.HOST), params, JSONUtil.toJson(body));
			Map<String, Object> rstMap= JSONUtil.fromJson(result);
			Map<String, Object> obj = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != obj && new Integer(0).equals(obj.get("Ret"))) {
				System.out.println("msg : " + log.content +"-> 发送成功！！");
				log.msgid = rstMap.get("MsgID").toString();
				log.newMsgId = Long.parseLong(rstMap.get("MsgID").toString());
				if(null != callBack) {
					callBack.callback(log);
				}
				return log;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(null != callBack) {
			callBack.callback(null);
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
			String result = hu.postBody(String.format(Constant.GET_INIT, Constant.HOST).replace("{TIME}", System.currentTimeMillis() + ""), JSONUtil.toJson(bodyMap));
			System.out.println("init result : " + result);
			Map<String, Object> rstMap = JSONUtil.fromJson(result);
			Map<String, Object> baseResponse = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != baseResponse && new Integer(0).equals(baseResponse.get("Ret"))) {
				List<Map<String, Object>> contactList = (List<Map<String, Object>>) rstMap.get("ContactList");
				if(null != contactList) {
					for(Map<String, Object> cmap : contactList) {
						ContactsStruct convs = ContactsStruct.fromMap(cmap);
						window.addConversition(convs);
						if(Constant.contacts.containsKey(convs.UserName)) {
							ContactsStruct old = Constant.contacts.get(convs.UserName);
							System.out.println(convs.UserName + " " + convs.NickName + " " + convs.RemarkName);
							old.fixMissProps(convs);
						}else {
							Constant.contacts.put(convs.UserName, convs);
						}
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
		String url = String.format(Constant.STATUS_NOTIFY, Constant.HOST) + "?lang=zh_CN&pass_ticket=" + Constant.sign.pass_ticket;
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
	public static List<String> loadContacts() {
		List<String> allGroups = new ArrayList<String>();
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String, String> params = new HashMap<String, String>();
		params.put("pass_ticket", Constant.sign.pass_ticket);
		params.put("r", System.currentTimeMillis() + "");
		params.put("seq", "0");
		params.put("skey", Constant.sign.skey);
		params.put("lang", "zh_CN");
		
		try {
			String result = hu.readJsonfromURL2(String.format(Constant.GET_CONTACT, Constant.HOST), params);
			Map<String, Object> rstMap = JSONUtil.fromJson(result);
			Map<String, Object> baseResponse = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != baseResponse && new Integer(0).equals(baseResponse.get("Ret"))) {
				List<Map<String, Object>> contactList = (List<Map<String, Object>>) rstMap.get("MemberList");
				if(null != contactList) {
					for(Map<String, Object> cmap : contactList) {
						ContactsStruct convs = ContactsStruct.fromMap(cmap);
						boolean top = convs.ContactFlag == 2049 || convs.ContactFlag == 2051;
						if(top) {
							MainWindow.getInstance().addConversition(convs);
						}
						if(Constant.contacts.containsKey(convs.UserName)) {
							ContactsStruct old = Constant.contacts.get(convs.UserName);
							System.out.println(convs.UserName + " " + convs.NickName + " " + convs.RemarkName);
							old.fixMissProps(convs);
						}else {
							Constant.contacts.put(convs.UserName, convs);
						}
//						String headUrl = Constant.BASE_URL + convs.HeadImgUrl;
//						convs.head = ImageCache.getUserHeadCache(convs.UserName, headUrl, null, 50, 50);
					}
					System.out.println("load contacts over!!count = " + Constant.contacts.size());
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
	 * 抓取视频
	 * 作者 ：肖逵
	 * 时间 ：2016年12月30日 下午8:19:09
	 * @param msgId
	 * @param callback
	 * @return
	 */
	public static File loadVoice(String msgId, ICallback<Integer> callback) {
		String url = String.format(Constant.LOAD_VOICE, Constant.HOST);
		Map<String, String> params = new HashMap<String, String>();
		params.put("msgid", msgId);
		try {
			params.put("skey", URLEncoder.encode(Constant.sign.skey, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "*/*");
		headers.put("Accept-Encoding", "identity;q=1, *;q=0");
		headers.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
		headers.put("DNT", "1");
		headers.put("Host", Constant.HOST);
		headers.put("Range", "bytes=0-");
		SongLocation in = hu.getInputStream(url, params, headers);
		if(null != in) {
			File file = new File("temp", System.currentTimeMillis() + ".mp3");
			file.getParentFile().mkdirs();
			return FileUtils.saveStream(file, in.input, callback, in.length);
		} else {
			if(null != callback ) {
				callback.callback(-1);
			}
		}
		return null;
	}
	
	public static SongLocation loadHttpVideo(String msgId, ICallback<Integer> callback){
		String url = String.format(Constant.LOAD_VIDEO, Constant.HOST);
		Map<String, String> params = new HashMap<String, String>();
		params.put("msgid", msgId);
		try {
			params.put("skey", URLEncoder.encode(Constant.sign.skey, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "*/*");
		headers.put("Accept-Encoding", "identity;q=1, *;q=0");
		headers.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
		headers.put("DNT", "1");
		headers.put("Host", Constant.HOST);
		headers.put("Range", "bytes=0-");
		return hu.getInputStream(url, params, headers);
	}
	
	/**
	 * 抓取视频
	 * 作者 ：肖逵
	 * 时间 ：2016年12月30日 下午8:16:09
	 * @param msgId
	 * @param callback
	 * @return
	 */
	public static File loadVideo(String msgId, ICallback<Integer> callback){
		SongLocation in = loadHttpVideo(msgId, callback);
		if(null != in) {
			File file = new File("temp", System.currentTimeMillis() + ".mp4");
			file.getParentFile().mkdirs();
			return FileUtils.saveStream(file, in.input, callback, in.length);
		} else {
			if(null != callback ) {
				callback.callback(-1);
			}
		}
		return null;
		
	}
	
	/**
	 * 用途：抓取聊天图片
	 * @date 2016年12月20日
	 * @return
	 */
	public static ImageLoader loadImage(String msgId, String type) {
		String url = String.format(Constant.LOAD_IMG, Constant.HOST);
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
		SongLocation in = hu.getInput(url, params);
		try {
			
			ImageLoader load = new ImageLoader();
			load.load(in.input);
			return load;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(null != in) {
				try {
					in.response.close();
					in.input.close();
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
		String url = String.format(Constant.LOGOUT_URL, Constant.HOST).replace("{SKEY}", Constant.sign.skey);
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
	public static void loadGroups(List<String> groups) {
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
			String url = String.format(Constant.GET_GROUPS, Constant.HOST).replace("{TIME}", System.currentTimeMillis() + "").replace("{TICKET}", Constant.sign.pass_ticket);
			String result = hu.postBody(url, JSONUtil.toJson(bodyMap));
			Map<String, Object> rstMap = JSONUtil.fromJson(result);
			Map<String, Object> baseResponse = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != baseResponse && new Integer(0).equals(baseResponse.get("Ret"))) {
				List<Map<String, Object>> contactList = (List<Map<String, Object>>) rstMap.get("ContactList");
				if(null != contactList) {
					for(Map<String, Object> cmap : contactList) {
						ContactsStruct convs = ContactsStruct.fromMap(cmap);
						if(Constant.contacts.containsKey(convs.UserName)) {
							ContactsStruct old = Constant.contacts.get(convs.UserName);
							System.out.println(convs.UserName + " " + convs.NickName + " " + convs.RemarkName);
							old.fixMissProps(convs);
						}else {
							Constant.contacts.put(convs.UserName, convs);
						}
//						String headUrl = Constant.BASE_URL + convs.HeadImgUrl;
//						convs.head = ImageCache.getUserHeadCache(convs.UserName, headUrl, null, 50, 50);
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
	 */
	public static void syncData() {
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
					String rst = hu.getJsonfromURL2(String.format(Constant.SYNC_CHECK, Constant.HOST), params);
					if(null != rst && rst.contains("window.synccheck=")) {
						String result = rst.replace("window.synccheck=", "");
						System.out.println("checksync + " + result);
						Map<String, String> map = JSONUtil.toBean(result, JSONUtil.getCollectionType(Map.class, String.class, String.class));
						if("0".equals(map.get("retcode")) ) {
							String selector = map.get("selector");
							try {
								Integer sele = Integer.parseInt(selector);
								if(sele > 0) {
									webwxsync();
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
	
	
	public static void acceptFriends(ChatLog log) {
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String,Object> bodyMap = new HashMap<String,Object>();
		Map<String,Object> bodyInner = new HashMap<String,Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		bodyMap.put("BaseRequest", bodyInner);
		bodyMap.put("Opcode", 3);
		bodyMap.put("SceneListCount", 1);
		bodyMap.put("VerifyContent", "");
		bodyMap.put("SceneList", new int[]{33});
		bodyMap.put("VerifyUserList", new Map[]{new HashMap<String, String>(){{
			put("Value", log.recommendInfo.get("UserName").toString());
			put("VerifyUserTicket", log.recommendInfo.get("Ticket").toString());
		}}});
		bodyMap.put("VerifyUserListSize", 1);
		bodyMap.put("skey", Constant.sign.skey);
		Map<String, String> params = new HashMap<String, String>();
		try {
			params.put("pass_ticket", URLEncoder.encode(Constant.sign.pass_ticket, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		params.put("r", String.valueOf(System.currentTimeMillis()));
		try {
			String result =  hu.postBody(String.format(Constant.VERIFY_USER, Constant.HOST), params, JSONUtil.toJson(bodyMap));
			System.out.println(result);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 用途：同步到数据的时候刷新数据
	 * @date 2016年12月30日
	 * @param conItem
	 */
	private static void webwxsync() {
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
			SysMsgChain chain = new SysMsgChain();
			String result =  hu.postBody(String.format(Constant.GET_STATUS, Constant.HOST), params, JSONUtil.toJson(bodyMap));
			Map<String, Object> rst = JSONUtil.fromJson(result);
			List<Map<String, Object>> modContactList = (List<Map<String, Object>>) rst.get("ModContactList");
			if(null != modContactList) {
				changeUser(modContactList);
			}
			Map<String, Object> BaseResponse = (Map<String, Object>) rst.get("BaseResponse");
			System.out.println(JSONUtil.toJson(rst));
			if(new Integer(0).equals(BaseResponse.get("Ret"))) {
				Integer msgCount = (Integer) rst.get("AddMsgCount");
				if(null != msgCount && msgCount > 0) {
					List<Map<String, Object>> AddMsgList = (List<Map<String, Object>>) rst.get("AddMsgList");
					for(Map<String, Object> msg : AddMsgList) {
						ChatLog log = ChatLog.fromMap(msg);
						chain.fromMap(log, msg);
					}
				}
			}
			Map<String, Object> SyncKey = (Map<String, Object>) rst.get("SyncKey");
			flushSyncKey(SyncKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * 撤回消息
	 * @param log
	 * @author xiaokui
	 */
	public static void revokeMsg(ChatLog log) {
		if(!Constant.user.UserName.equals(log.fromId) || log.recalled) {
			return;
		}
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String,Object> bodyMap = new HashMap<String,Object>();
		Map<String,Object> bodyInner = new HashMap<String,Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		bodyMap.put("BaseRequest", bodyInner);
		bodyMap.put("ClientMsgId", System.currentTimeMillis());
		bodyMap.put("SvrMsgId", log.msgid);
		bodyMap.put("ToUserName", log.toId);
		Map<String, String> params = new HashMap<String, String>();
		params.put("pass_ticket", Constant.sign.pass_ticket);
		try {
			String result =  hu.postBody(String.format(Constant.REVOKE_MSG, Constant.HOST), params, JSONUtil.toJson(bodyMap));
			System.out.println(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 提醒已读未读
	 * @param from
	 * @param to
	 * @author xiaokui
	 */
	public static void statusNotify(String from, String to) {
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String,Object> bodyMap = new HashMap<String,Object>();
		Map<String,Object> bodyInner = new HashMap<String,Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		bodyMap.put("BaseRequest", bodyInner);
		bodyMap.put("ClientMsgId", System.currentTimeMillis());
		bodyMap.put("Code", 1);
		bodyMap.put("FromUserName", from);
		bodyMap.put("ToUserName", to);
		Map<String, String> params = new HashMap<String, String>();
		params.put("pass_ticket", Constant.sign.pass_ticket);
		try {
			String result =  hu.postBody(String.format(Constant.STATUS_NOTIFY, Constant.HOST), params, JSONUtil.toJson(bodyMap));
			System.out.println(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void computeGroup(Map<String, List<ContactsStruct>> friends, String spell, ContactsStruct convs) {
		List<ContactsStruct> friend = friends.get(spell);
		if(null == friend) {
			friend = new ArrayList<ContactsStruct>();
			friends.put(spell, friend);
		}
		friend.add(convs);
		Collections.sort(friend, new Comparator<ContactsStruct>() {

			@Override
			public int compare(ContactsStruct o1, ContactsStruct o2) {
				String nick1 = o1.NickName;
				String remark1 = o1.RemarkName;
				String name1 = (null == remark1 || remark1.trim().isEmpty()) ? nick1 : remark1; 
				String nick2 = o2.NickName;
				String remark2 = o2.RemarkName;
				String name2 = (null == remark2 || remark2.trim().isEmpty()) ? nick2 : remark2; 
				return name1.compareTo(name2);
			}
			
		});
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
			List<Map<String, Object>> MemberList = (List<Map<String, Object>>) map.get("MemberList");
			String memberStr = JSONUtil.toJson(MemberList);
			List<MemberStruct> members = JSONUtil.toBean(memberStr, JSONUtil.getCollectionType(List.class, MemberStruct.class));
			ContactsStruct cs = Constant.getContact(user);
			if(cs == null) {
				continue;
			}
			cs.ContactFlag = ContactFlag;
			cs.MemberList = members;
			mw.topUser(cs, ContactFlag == 2049 ? 1 : 0);
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				mw.convers.flush();
			}
		});
		
	}
}
