package com.xk.uiLib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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



public class MyList extends Composite {

	public static final int BAR_WIDTH=8;//滚动条宽度
	private static final int BAR_ARROW_HEIGHT=8;
	private List<ItemListener> itemListeners=new ArrayList<ItemListener>();
	private List<ItemSelectionListener> selectionListeners=new ArrayList<ItemSelectionListener>();
	private int allHeight=0;
	private int width;
	private int height;
	private List<ListItem>items=new ArrayList<ListItem>();
	protected ListItem selected=null;
	protected int selectIndex=-1;
	private ListItem focused=null;
	protected Canvas back;
	private boolean showScroll=false;//是否需要滚动条
	private int barY=0;//滚动条位置
	private int barHeight=0;//滚动条高度
	private int startY=0;//子组件开始渲染位置
	private STATE state=STATE.NORMAL;//滚动条状态
	private int downY=0;//鼠标按下位置(相对滚动条bar)
	
	private int mask=0;//模糊背景
	private boolean simpleSelect=false;//单击选中
	
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
				if(e.x<width-BAR_WIDTH){
					if(simpleSelect){
						checkSelection(e);
					}else{
						checkFocus(e);
					}
					
				}else if(e.y>height-BAR_ARROW_HEIGHT){
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
						checkSelection(e);
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
		back.addMouseTrackListener(new MouseTrackAdapter() {

			@Override
			public void mouseEnter(MouseEvent mouseevent) {
				setFocus();//让自己获得焦点
			}

			@Override
			public void mouseExit(MouseEvent mouseevent) {
			}
			
		});
	}
	
	private void checkFocus(MouseEvent e){
		int realY=e.y+Math.abs(startY);//得到真实Y位移
		int itemHeight=0;
		int index=0;
		for(ListItem item:items){
			itemHeight+=item.getHeight();
			if(itemHeight>realY){
				if(item.oncliek(e,itemHeight,index)){
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
	private void checkSelection(MouseEvent e){
		int realY=e.y+Math.abs(startY);//得到真实Y位移
		int itemHeight=0;
		int index=0;
		for(ListItem item:items){
			itemHeight+=item.getHeight();
			if(itemHeight>realY){
				if(item.oncliek(e,itemHeight,index)){
					select(item,false);
				}
				return;
			}
			index++;
		}
	}
	
	private void focusItem(ListItem item){
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
	
	
	
	public void select(ListItem item,boolean sync){
		if(null!=selected){//清除上一个选择的item
			selected.unSelect();
		}
		item.select();
		countHeight();//重新计算整个高度
		int itemHeight=0;
		for(ListItem it:items){
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
		ItemSelectionEvent even=new ItemSelectionEvent();
		even.item=item;
		even.itemHeights=itemHeight;
		even.source=this;
		for(ItemSelectionListener listener:selectionListeners){
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
		for(ListItem item:items){
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
		selectIndex=-1;
		focused=null;
		selected=null;
		int index=0;
		for(ListItem it:items){
			allHeight+=it.getHeight();
			if(it.selected){
				selectIndex=index;
				selected=it;
			}
			if(it.focused){
				focused=it;
			}
			index++;
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
	
	public void addItem(ListItem item){
		if(null!=item){
			items.add(item);
			item.setParent(this);
			countHeight();
		}
	}
	
	public void clearAll(){
		items.clear();
		countHeight();
	}
	
	public void removeItem(int index){
		if(index<items.size()&&index>=0){
			ListItem removed=items.get(index);
			ItemEvent event=new ItemEvent();
			event.index=index;
			event.item=removed;
			for(ItemListener listener:itemListeners){
				listener.itemRemove(event);
			}
			items.remove(index);
			removed.setParent(null);
			countHeight();
			back.redraw();
		}
	}
	
	public void removeItem(ListItem item){
		if(null!=item){
			int index=items.indexOf(item);
			removeItem(index);
		}
	}
	
	public List<ListItem> getItems(){
		return Collections.unmodifiableList(items);
	}
	
	
	public void addItemListener(ItemListener listener){
		itemListeners.add(listener);
	}
	
	public boolean remove(ItemSelectionListener o) {
		return selectionListeners.remove(o);
	}

	public void add(ItemSelectionListener element) {
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
	
	public ListItem getFocus(){
		return focused;
	}
	
	public ListItem getSelection(){
		return selected;
	}
	
	public int getSelectIndex() {
		return selectIndex;
	}

	public int getItemCount(){
		return items.size();
	}
	
	private enum STATE{
		NORMAL , DRAGING
	}
}
