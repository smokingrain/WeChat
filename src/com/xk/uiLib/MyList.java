package com.xk.uiLib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.xk.uiLib.listeners.ItemEvent;
import com.xk.uiLib.listeners.ItemListener;
import com.xk.uiLib.listeners.ItemSelectionEvent;
import com.xk.uiLib.listeners.ItemSelectionListener;



public class MyList<T extends ListItem> extends Composite {

	public static final int CLICK_FOCUS = 1;//响应鼠标点击  -- 单击事件,获得焦点
	public static final int CLICK_SINGLE = 2;//响应鼠标点击  -- 单击事件,选中
	public static final int CLICK_DOUBLE = 3;//响应鼠标点击  -- 双击事件
	public static final int BAR_WIDTH=8;//滚动条宽度
	private static final int BAR_ARROW_HEIGHT=8;
	private List<ItemListener<T>> itemListeners=new ArrayList<ItemListener<T>>();
	private List<ItemSelectionListener<T>> selectionListeners=new ArrayList<ItemSelectionListener<T>>();
	private int allHeight=0;
	private int width;
	private int height;
	private List<T>items=new CopyOnWriteArrayList<T>();
	protected T selected=null;
	private T focused=null;
	protected Canvas back;
	private boolean showScroll=false;//是否需要滚动条
	private int barY=0;//滚动条位置
	private int barHeight=0;//滚动条高度
	private int startY=0;//子组件开始渲染位置
	private STATE state=STATE.NORMAL;//滚动条状态
	private int downY=0;//鼠标按下位置(相对滚动条bar)
	private int itemLimit = -1;
	private int mask = 255;//模糊背景
	private boolean simpleSelect=false;//单击选中
	private SortItem sort = new SortItem();
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public MyList(Composite parent, int width,int height) {
		super(parent, SWT.NONE);
		this.width=width;
		this.height=height;
		this.setSize(width, height);
		back=new Canvas(this, SWT.DOUBLE_BUFFERED);
		back.setBounds(0, 0, width, height);
		back.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent paintevent) {
				render(paintevent.gc);
				
			}
		});
		initEvent();
	}
	
	public void setMask(int mask){
		this.mask=mask;
	}
	
	/**
	 * 设置单击选中
	 * @param simple 
	 */
	public void setSimpleSelect(boolean simple){
		this.simpleSelect=simple;
	}
	
	/**
	 * 
	 * 用途：初始化各种事件
	 * 
	 * @date 2016年9月29日
	 */
	private void initEvent(){
		back.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				if(e.x<width-BAR_WIDTH){
					if(simpleSelect){
						checkSelection(e, CLICK_SINGLE);
					}else{
						checkFocus(e, CLICK_FOCUS);
					}
					
				}
				state=STATE.NORMAL;
				downY=0;
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				if(showScroll){
					Rectangle rect=new Rectangle(width-BAR_WIDTH-1, barY+BAR_ARROW_HEIGHT, BAR_WIDTH, barY+barHeight);
					if(rect.contains(e.x,e.y)){
						state=STATE.DRAGING;
						downY=e.y-BAR_ARROW_HEIGHT-barY;//
						return;
					}
				}
				if(e.y>height-BAR_ARROW_HEIGHT){
					int temp=barY+1;
					if(temp+barHeight+2*BAR_ARROW_HEIGHT>height){
						temp=height-barHeight-2*BAR_ARROW_HEIGHT;
					}
					barY=temp;
					double per=(double)barY/(height-barHeight-2*BAR_ARROW_HEIGHT);//滚动百分比
					startY=(int) (0-(allHeight-height)*per);
					back.redraw();
				}else if(e.y<BAR_ARROW_HEIGHT){
					int temp=barY-1;
					if(temp<0){
						temp=0;
					}
					barY=temp;
					double per=(double)barY/(height-barHeight-2*BAR_ARROW_HEIGHT);//滚动百分比
					startY=(int) (0-(allHeight-height)*per);
					back.redraw();
				}
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if(e.x<width-BAR_WIDTH){
					if(!simpleSelect){
						checkSelection(e, CLICK_DOUBLE);
					}
				}
			}
		});
		back.addMouseMoveListener(new MouseMoveListener() {
			
			@Override
			public void mouseMove(MouseEvent e) {
				if(showScroll&&STATE.DRAGING.equals(state)){
					int temp=e.y-downY-BAR_ARROW_HEIGHT;
					if(temp<0){
						temp=0;
					}else if(temp+barHeight+2*BAR_ARROW_HEIGHT>height){
						temp=height-barHeight-2*BAR_ARROW_HEIGHT;
					}
					barY=temp;
					double per=(double)barY/(height-barHeight-2*BAR_ARROW_HEIGHT);//滚动百分比
					startY=(int) (0-(allHeight-height)*per);
					back.redraw();
				}
				
			}
		});
		back.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseScrolled(MouseEvent e) {
				if(!showScroll){
					return;
				}
				int temp=barY-e.count;
				if(temp<0){
					temp=0;
				}else if(temp+barHeight+2*BAR_ARROW_HEIGHT>height){
					temp=height-barHeight-2*BAR_ARROW_HEIGHT;
				}
				barY=temp;
				double per=(double)barY/(height-barHeight-2*BAR_ARROW_HEIGHT);//滚动百分比
				startY=(int) (0-(allHeight-height)*per);
				back.redraw();
				
			}
		});
		//这个体验有点坑，在输入内容的时候，鼠标全选然后发现失去焦点了。。。好尴尬
//		back.addMouseTrackListener(new MouseTrackAdapter() {
//
//			@Override
//			public void mouseEnter(MouseEvent mouseevent) {
//				setFocus();//让自己获得焦点
//			}
//
//			@Override
//			public void mouseExit(MouseEvent mouseevent) {
//			}
//			
//		});
	}
	
	public void scrollToBottom() {
		if (!showScroll) {
			return;
		}
		barY = height - barHeight - 2 * BAR_ARROW_HEIGHT;
		double per = (double) barY / (height - barHeight - 2 * BAR_ARROW_HEIGHT);// 滚动百分比
		startY = (int) (0 - (allHeight - height) * per);
	}
	
	private void checkFocus(MouseEvent e, int type){
		int realY=e.y+Math.abs(startY);//得到真实Y位移
		int itemHeight=0;
		int index=0;
		for(T item:items){
			itemHeight+=item.getHeight();
			if(itemHeight>realY){
				if(item.oncliek(e, itemHeight, index, type)){
					focusItem(item);
				}
				return;
			}
			index++;
		}
	}
	
	/**
	 * 检查选总item
	 * @param y
	 */
	private void checkSelection(MouseEvent e, int type){
		int realY=e.y+Math.abs(startY);//得到真实Y位移
		int itemHeight=0;
		int index=0;
		for(T item:items){
			itemHeight+=item.getHeight();
			if(itemHeight>realY){
				if(item.oncliek(e, itemHeight, index, type)){
					select(item,false);
				}
				return;
			}
			index++;
		}
	}
	
	private void focusItem(T item){
		if(null!=focused){
			focused.unFocus();
		}
		item.focus();
		focused=item;
		back.redraw();
	}
	
	public void select(int index,boolean sync){
		select(items.get(index),sync);
	}
	
	
	
	public void select(T item,boolean sync){
		if(null!=selected){//清除上一个选择的item
			selected.unSelect();
		}
		item.select();
		countHeight();//重新计算整个高度
		int itemHeight=0;
		for(T it:items){
			itemHeight+=it.getHeight();
			if(it.equals(item)){
				if(itemHeight+startY<0){//在本组件之上
					startY+=Math.abs(itemHeight+startY)+it.getHeight();
					double per=Math.abs((double)startY)/(allHeight-height);
					barY=(int) ((height-2*BAR_ARROW_HEIGHT-barHeight)*per);
				}else if(itemHeight+startY-it.getHeight()>height){//在本组件之下
					startY-=itemHeight-height+startY;
					double per=Math.abs((double)startY)/(allHeight-height);
					barY=(int) ((height-2*BAR_ARROW_HEIGHT-barHeight)*per);
				}
				break;
			}
		}
		if(sync){
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					back.redraw();
				}
			});
		}else{
			back.redraw();
		}
		ItemSelectionEvent<T> even=new ItemSelectionEvent<T>();
		even.item=item;
		even.itemHeights=itemHeight;
		even.source=this;
		for(ItemSelectionListener<T> listener:selectionListeners){
			listener.selected(even);	
		}
	}
	
	/**
	 * 渲染界面
	 * @param gc
	 */
	private void render(GC gc){
		boolean adv=gc.getAdvanced();
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);
		Color backup=gc.getForeground();
		Color white=new Color(null, 0Xff,0Xff,0Xff);
		//绘制半透明背景
		gc.setForeground(white);
		gc.setAlpha(mask);
		gc.fillRoundRectangle(0, 0, width, height, 3, 3);
		gc.setAlpha(255);
		gc.setForeground(backup);
		drawItems(gc,allHeight);
		
		if(showScroll){
			//描边
			gc.setForeground(white);
			gc.setBackground(white);
			LineAttributes attributes =new LineAttributes(1f, SWT.CAP_SQUARE, SWT.JOIN_ROUND, SWT.LINE_SOLID, new float[] { 5, 3, }, 0, 10);
    		gc.setLineAttributes(attributes);
			Path path=new Path(null);
			path.addRectangle(width-BAR_WIDTH-1.4f, barY+BAR_ARROW_HEIGHT-0.2f, BAR_WIDTH, barHeight-1.1f);
			gc.drawPath(path);
			gc.drawPolygon(new int[]{width-BAR_WIDTH,BAR_WIDTH-2,width-2,BAR_WIDTH-2,width-BAR_WIDTH/2-1,1});
			gc.drawPolygon(new int[]{width-BAR_WIDTH,height-BAR_WIDTH+1,width-2,height-BAR_WIDTH+1,width-BAR_WIDTH/2-1,height-2});
			
			//两端顶点以及滚动条
			Color color=new Color(null, 0X96,0X96,0X96);
			gc.setForeground(color);
			gc.setBackground(color);
			gc.fillPolygon(new int[]{width-BAR_WIDTH,BAR_WIDTH-1,width-1,BAR_WIDTH-1,width-BAR_WIDTH/2-1,1});
			gc.fillRectangle(width-BAR_WIDTH-1, barY+BAR_ARROW_HEIGHT, BAR_WIDTH, barHeight-1);
			gc.fillPolygon(new int[]{width-BAR_WIDTH,height-BAR_WIDTH+1,width-1,height-BAR_WIDTH+1,width-BAR_WIDTH/2-1,height-1});
			
			path.dispose();
			color.dispose();
			
		}
		white.dispose();
		backup.dispose();
		gc.setAdvanced(adv);
		gc.dispose();
	}

	/**
	 * 绘制每一个单位
	 * @param gc
	 * @param allHeight
	 */
	private void drawItems(GC gc,int allHeight){
		int nowY=startY;//开始渲染，每渲染一个子组件，位置叠加！
		int index=0;
		for(T item:items){
			index++;
			if(nowY+item.getHeight()<0){//在我之上的不渲染
				nowY+=item.getHeight();
				continue;
			}
			if(nowY>height){//在我之下的全部不渲染
				break;
			}
			gc.setClipping(0, nowY, width, item.getHeight());//限制每个item只能在自己的区域绘制，在其他区域的绘制将被剪切
			item.draw(gc, nowY,width,index);
			nowY+=item.getHeight();
		}
		gc.setClipping(0,0,width,height);
	}
	
	/**
	 * 计算滚动条bar高度
	 * @param allHeight
	 */
	private void countBarHeight(int allHeight){
		int clientAreaHeight=height-2*BAR_ARROW_HEIGHT;
		double per=((double)height)/allHeight;
		barHeight=(int) (clientAreaHeight*per);
		if(barHeight<10){
			barHeight=10;
		}
	}
	
	/**
	 * 计算总高度，用来判断是否显示滚动条
	 */
	private void countHeight(){
		allHeight=0;
		focused=null;
		selected=null;
		for(T it:items){
			allHeight+=it.getHeight();
			if(it.selected){
				selected=it;
			}
			if(it.focused){
				focused=it;
			}
		}
		if(allHeight>height){
			showScroll=true;
			countBarHeight(allHeight);
		}else{
			showScroll=false;
			barHeight=0;
			barY=0;
			startY=0;
		}
	}
	
	public void addItem(Integer index, T item) {
		if(null != item && null != index){
			checkLimit();
			items.sort(sort);
			items.add(index, item);
			item.setParent(this);
			countHeight();
		}
	}
	
	public void addItem(T item) {
		if(null!=item){
			checkLimit();
			items.add(item);
			items.sort(sort);
			item.setParent(this);
			countHeight();
		}
	}
	
	/**
	 * 用途：检查是不是到了单元格个数限制
	 * @date 2017年1月11日
	 */
	private void checkLimit() {
		if(itemLimit > 0 && items.size() >= itemLimit) {
			items.remove(0);
		}
	}
	
	public void clearAll(){
		items.clear();
		countHeight();
	}
	
	
	/**
	 * 用途：更改一个单元格的位置
	 * @date 2017年1月11日
	 * @param t
	 * @param index
	 */
	public T changeItemIndex(T t, int index) {
		if(null == t) {
			return null;
		}
		if(items.indexOf(t) < 0) {
			return null;
		}
		items.remove(t);
		items.add(index, t);
		return t;
	}
	
	/**
	 * 移除指定位置的item
	 * 
	 * @param index
	 */
	public T removeItem(int index) {
		if (index < items.size() && index >= 0) {
			T removed = items.get(index);
			ItemEvent<T> event = new ItemEvent<T>();
			event.index = index;
			event.item = removed;
			for (ItemListener<T> listener : itemListeners) {
				listener.itemRemove(event);
			}
			items.remove(index);
			removed.setParent(null);
			countHeight();
			return removed;
		}
		return null;
	}

	public T removeItem(T item) {
		if (null != item) {
			int index = items.indexOf(item);
			return removeItem(index);
		}
		return null;
	}
	
	public List<T> getItems(){
		return Collections.unmodifiableList(items);
	}
	
	
	public void addItemListener(ItemListener<T> listener){
		itemListeners.add(listener);
	}
	
	public boolean remove(ItemSelectionListener<T> o) {
		return selectionListeners.remove(o);
	}

	public void add(ItemSelectionListener<T> element) {
		selectionListeners.add(element);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void flush(){
		if(!isDisposed()){
			back.redraw();
		}
	}
	
	public T getFocus(){
		return focused;
	}
	
	public T getSelection(){
		return selected;
	}
	
	public int getSelectIndex() {
		if(null == selected) {
			return -1;
		}
		return items.indexOf(selected);
	}

	public int getItemCount(){
		return items.size();
	}
	
	public void setItemLimit(int num) {
		itemLimit = num;
	}
	
	private enum STATE{
		NORMAL , DRAGING
	}
	
	private class SortItem implements Comparator<T> {

		@Override
		public int compare(T o1, T o2) {
			return o1.compareTo(o2);
		}
		
	}
}
