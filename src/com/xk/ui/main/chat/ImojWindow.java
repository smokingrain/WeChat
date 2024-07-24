package com.xk.ui.main.chat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Combo;
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
import com.xk.utils.song.SongLocation;

public class ImojWindow extends FloatWindow {
	
	public static ExecutorService loadPicture = Executors.newFixedThreadPool(10);

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
		
		Combo type = new Combo(searchComp, SWT.READ_ONLY);
		type.setBounds(18 + 30 * 12, 10, 30 * 2, 25);
		type.add("逗比");
		type.add("DIY斗图");
		type.add("爱斗图");
		type.add("斗图啦");
		
		type.select(0);
		
		Image search=SWTResourceManager.getImage(ImojWindow.class, "/images/search.png");
		MyText mtext = new MyText(searchComp, SWT.SINGLE);
		mtext.setBounds(18, 10, 30 * 11, 25);
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
					processSearch(mtext, pictures, type.getSelectionIndex());
				}
			}
			
		});
		
		mtext.addDeleteListener(new DeleteListener() {
			
			@Override
			public void deleteClicked() {
				processSearch(mtext, pictures, type.getSelectionIndex());
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
				loadPicture.shutdownNow();
				FakeTooltips.getInstance().hide();
			}
		});
	}
	
	public void processSearch(MyText mtext, MyList<PictureItem> pictures, int type) {
		String query = mtext.getText().trim();
		if(query.isEmpty()) {
			return;
		}
		mtext.setEnabled(false);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ImojSearcher searcher = null;
				switch(type) {
				case 0:
					searcher = new DoubiSearcher();
					break;
				case 1:
					searcher = new ADoutuSearcher();
					break;
				case 3:
					searcher = new DoutulaSearcher();
					break;
					default:
						searcher = new DIYDoutuSearcher();
						break;
					
				}
				try {
					loadPicture.shutdownNow();
					loadPicture = Executors.newFixedThreadPool(10);
					searcher.search(query, pictures);
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
	private interface ImojSearcher{
		public List<String> search(String text, MyList<PictureItem> pictures);
	}
	
	private class DoubiSearcher implements ImojSearcher{

		@Override
		public List<String> search(String query, MyList<PictureItem> pictures) {
			pictures.clearAll();
			List<String> imgList = new ArrayList<String>();
			String html = null;
			try {
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("Web-Agent", "web");
				html = HTTPUtil.getInstance("picture")
						.getHtml(headers, "https://www.dbbqb.com/api/search/json?start=0&w=" + URLEncoder.encode(query.trim(), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return imgList;
			}
			if(StringUtils.isEmpty(html)) {
				return imgList;
			}
			List<Map<String, Object>> array = JSONUtil.toBean(html, JSONUtil.getCollectionType(List.class, Map.class));
			if(null == array || array.size() == 0) {
				return imgList;
			}
			
			for(Map<String, Object> img : array) {
				String url = "https://image.dbbqb.com/" + img.get("path");
				if (!StringUtils.isEmpty(url)) {
					imgList.add(url);
					if (imgList.size() >= 7) {
						PictureItem item = new PictureItem(cc, imgList);
						pictures.addItem(item);
						imgList.clear();
					}
				}
			}
			
			if (!imgList.isEmpty()) {
				PictureItem item = new PictureItem(cc, imgList);
				pictures.addItem(item);
				imgList.clear();
			}
			return imgList;
		}
		
	}
	
	private class DIYDoutuSearcher implements ImojSearcher {

		@Override
		public List<String> search(String query, MyList<PictureItem> pictures) {
			pictures.clearAll();
			List<String> imgList = new ArrayList<String>();
			String html = null;
			try {
				html = HTTPUtil.getInstance("picture").getHtml("https://www.diydoutu.com/tag/" + URLEncoder.encode(query.trim(), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return imgList;
			}
			if(StringUtils.isEmpty(html)) {
				return imgList;
			}
			Document doc = Jsoup.parse(html);
			Elements elements = doc.getElementsByClass("no-gutters");
			if(null == elements || elements.size() == 0) {
				return imgList;
			}
			Element resultList = elements.get(0);
			Elements imgs = resultList.getElementsByTag("img");
			
			for(Element img : imgs) {
				String url = img.attr("data-src");
				if (!StringUtils.isEmpty(url)) {
					imgList.add(url);
					if (imgList.size() >= 7) {
						PictureItem item = new PictureItem(cc, imgList);
						pictures.addItem(item);
						imgList.clear();
					}
				}
			}
			
			if (!imgList.isEmpty()) {
				PictureItem item = new PictureItem(cc, imgList);
				pictures.addItem(item);
				imgList.clear();
			}
			return imgList;
		}
		
	}
	
	private class ADoutuSearcher implements ImojSearcher{

		@Override
		public List<String> search(String query, MyList<PictureItem> pictures) {
			pictures.clearAll();
			
			
			List<String> imgList = new ArrayList<String>();
			
			String html = null;
			try {
				html = HTTPUtil.getInstance("picture").getHtml("http://www.adoutu.com/search?keyword=" + URLEncoder.encode(query.trim(), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return imgList;
			}
			if(StringUtils.isEmpty(html)) {
				return imgList;
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
			
			if (!imgList.isEmpty()) {
				PictureItem item = new PictureItem(cc, imgList);
				pictures.addItem(item);
				imgList.clear();
			}
			return imgList;
		}
		
	}
	
	private class DoutulaSearcher implements ImojSearcher{

		@Override
		public List<String> search(String query, MyList<PictureItem> pictures) {
			pictures.clearAll();
			
			
			List<String> imgList = new ArrayList<String>();
			
			String html = null;
			try {
				html = HTTPUtil.getInstance("picture").getHtml("https://www.pkdoutu.com/search?keyword=" + URLEncoder.encode(query.trim(), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return imgList;
			}
			if(StringUtils.isEmpty(html)) {
				return imgList;
			}
			Document doc = Jsoup.parse(html);
			if(null == doc) {
				return imgList;
			}
			Elements pics = doc.getElementsByClass("random_picture");
			if(pics.size() == 0) {
				return imgList;
			}
			Element resultList = pics.first();
			Elements imgs = resultList.getElementsByTag("img");
			
			for(Element img : imgs) {
				String url = img.attr("data-original");
				if (!StringUtils.isEmpty(url)) {
					imgList.add(url);
					if (imgList.size() >= 7) {
						PictureItem item = new PictureItem(cc, imgList);
						pictures.addItem(item);
						imgList.clear();
					}
				}
			}
			
			if (!imgList.isEmpty()) {
				PictureItem item = new PictureItem(cc, imgList);
				pictures.addItem(item);
				imgList.clear();
			}
			return imgList;
		}
		
	}
}
