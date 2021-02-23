package com.xk.ui.main.chat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.jcodec.common.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.xk.bean.Imoj;
import com.xk.ui.items.TypeItem;
import com.xk.ui.main.FloatWindow;
import com.xk.uiLib.FakeTooltips;
import com.xk.uiLib.MyList;
import com.xk.uiLib.MyText;
import com.xk.uiLib.MyText.DeleteListener;
import com.xk.uiLib.listeners.ItemSelectionEvent;
import com.xk.uiLib.listeners.ItemSelectionListener;
import com.xk.uiLib.listeners.ListListener;
import com.xk.utils.HTTPUtil;
import com.xk.utils.ImojCache;
import com.xk.utils.JSONUtil;

public class ImojWindow extends FloatWindow {

	private ChatComp cc;
	
	private static class WindowHolder {
		private static ImojWindow INSTANCE = new ImojWindow();
	}
	
	public static ImojWindow getInstance() {
		return WindowHolder.INSTANCE;
	}
	
	public void setCc(ChatComp cc) {
		this.cc = cc;
	}
	
	public void init(int x, int y){
		kill();
		createContents();
		shell.setLocation(x, y);
	}
	
	@Override
	protected void createContents() {
		shell = new Shell(SWT.FILL_WINDING);
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.addShellListener(new ShellAdapter() {

			@Override
			public void shellDeactivated(ShellEvent shellevent) {
				kill();
			}
			
		});
		
		MyList<TypeItem> typeList = new MyList<TypeItem>(shell, 50, 215);
		typeList.setSimpleSelect(true);
		typeList.setLocation(0, 0);
		typeList.setMask(10);
		typeList.setBackground(SWTResourceManager.getColor(0xC0, 0xC0, 0xC0));
		
		Composite container = new Composite(shell, SWT.NONE);
		container.setLocation(50, 0);
		container.setSize(30 * 15 + MyList.BAR_WIDTH, 215);
		container.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		StackLayout layout = new StackLayout();
		container.setLayout(layout);
		
		MyList<ImojItem> imojList = new MyList<>(container, 30 * 15 + MyList.BAR_WIDTH, 215);
		imojList.setSimpleSelect(true);
		imojList.setLocation(0, 0);
		for(List<Imoj> imj : ImojCache.imjs) {
			ImojItem itm = new ImojItem(imj, cc);
			imojList.addItem(itm);
		}
		layout.topControl = imojList;
		
		Composite searchComp = new Composite(container, SWT.NONE);
		searchComp.setSize(30 * 15 + MyList.BAR_WIDTH, 215);
		searchComp.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		Image search=SWTResourceManager.getImage(ImojWindow.class, "/images/search.png");
		MyText mtext = new MyText(searchComp, SWT.SINGLE);
		mtext.setBounds(18, 10, 30 * 14, 25);
		mtext.setInnerImage(search);
		mtext.setForeground(SWTResourceManager.getColor(0, 0, 0));
		
		
		MyList<PictureItem> pictures = new MyList<PictureItem>(searchComp, 30 * 15 + MyList.BAR_WIDTH, 180);
		pictures.setSimpleSelect(true);
		pictures.setLocation(0, 36);
		pictures.add(new ListListener() {
			
			@Override
			public void onMouseExit() {
				FakeTooltips tips = FakeTooltips.getInstance();
				if(tips.inited()) {
					tips.hide();
				}
			}
			
			@Override
			public void onMouseEnter() {
			}
		});
		
		mtext.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode==SWT.CR || e.keyCode == SWT.KEYPAD_CR){
					processSearch(mtext, pictures);
				}
			}
			
		});
		
		mtext.addDeleteListener(new DeleteListener() {
			
			@Override
			public void deleteClicked() {
				processSearch(mtext, pictures);
			}
		});
		
		Control[] items = new Control[2];
		items[0] = imojList;
		items[1] = searchComp;
		
		Image imojSel = SWTResourceManager.getImage(ImojWindow.class, "/images/imojSel.png");
		Image imoj = SWTResourceManager.getImage(ImojWindow.class, "/images/imoj.png");
		TypeItem imojItem = new TypeItem(imoj, imojSel);
		Image picSel = SWTResourceManager.getImage(ImojWindow.class, "/images/picSel.png");
		Image pic = SWTResourceManager.getImage(ImojWindow.class, "/images/pic.png");
		TypeItem picItem = new TypeItem(pic, picSel);
		
		typeList.addItem(imojItem);
		typeList.addItem(picItem);
		typeList.add(new ItemSelectionListener<TypeItem>() {
			
			@Override
			public void selected(ItemSelectionEvent<TypeItem> e) {
				if(null != layout.topControl) {
					layout.topControl.setVisible(false);
				}
				layout.topControl = items[e.index];
				layout.topControl.setVisible(true);
			}
		});
		typeList.select(0, false);
		
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent disposeevent) {
				PictureItem.loadPicture.shutdown();
				FakeTooltips.getInstance().hide();
			}
		});
	}
	
	public void processSearch(MyText mtext, MyList<PictureItem> pictures) {
		String query = mtext.getText().trim();
		if(query.isEmpty()) {
			return;
		}
		mtext.setEnabled(false);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					pictures.clearAll();
					Map<String, String> params = new HashMap<String, String>();
					params.put("types", "search");
					params.put("action", "searchpic");
					params.put("wd", query.trim());
					params.put("limit", "60");
					params.put("offset", "0");
					
					String json = HTTPUtil.getInstance("picture").readJsonfromURL2("https://www.52doutu.cn/api/", params);
					Map<String, Object> map = JSONUtil.fromJson(json);
					Object rows = map.get("rows");
					List<Map<String, String>> urls = new ArrayList<Map<String, String>>();
					if(null != rows && rows instanceof List) {
						urls.addAll((List<Map<String, String>>) rows);
					}
					
					List<String> imgList = new ArrayList<String>();
					
					if(!urls.isEmpty()) {
						for(Map<String, String> urlMap : urls) {
							String url = urlMap.get("url");
							if (!StringUtils.isEmpty(url)) {
								imgList.add(url);
								if (imgList.size() >= 7) {
									PictureItem item = new PictureItem(cc, imgList);
									pictures.addItem(item);
									imgList.clear();
								}
							}
						}
					} else {
						String html = HTTPUtil.getInstance("picture").getHtml("http://www.adoutu.com/search?keyword=" + URLEncoder.encode(query.trim(), "utf-8"));
						if(StringUtils.isEmpty(html)) {
							return;
						}
						Document doc = Jsoup.parse(html);
						Element resultList = doc.getElementById("search-results-picture");
						Elements imgs = resultList.getElementsByTag("img");
						
						for(Element img : imgs) {
							String url = img.attr("src");
							if (!StringUtils.isEmpty(url)) {
								imgList.add(url);
								if (imgList.size() >= 7) {
									PictureItem item = new PictureItem(cc, imgList);
									pictures.addItem(item);
									imgList.clear();
								}
							}
						}
					}
					
					if (!imgList.isEmpty()) {
						PictureItem item = new PictureItem(cc, imgList);
						pictures.addItem(item);
						imgList.clear();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							pictures.flush();
							if(!mtext.isDisposed()) {
								mtext.setEnabled(true);
							}
						}
					});
				}
				
			}
		}).start();
	}
	
}
