/**
 * @author xiaokui
 * @版本 ：v1.0
 * @时间：2015-2-2下午4:05:20
 */
package com.xk.uiLib;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.xk.bean.ContactsStruct;
import com.xk.ui.items.ContactItem;
import com.xk.uiLib.listeners.ItemSelectionEvent;
import com.xk.uiLib.listeners.ItemSelectionListener;
import com.xk.utils.Constant;


/**
 * @项目名称：TicketGetter
 * @类名称：AutoCombo.java
 * @类描述：
 * @创建人：xiaokui
 * 时间：2015-2-2下午4:05:20
 */
public abstract class AutoCombo{
	//不响应文字变化
	private boolean holding = false;
	//不响应提示
	private boolean sleeping = true;
	private boolean isActive=false;
	private boolean inited=false;
	//表示任何情況下展示所有
	private boolean match = true;
	private Shell shell;
	private StyledText combo;
	private List list;
	private Map<String, ContactsStruct> items = new HashMap<String, ContactsStruct>();
	public AutoCombo(StyledText combo){
		this.combo=combo;
		open();
	}
	public void init(){
		if(inited){
			return;
		}
		this.combo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				AutoCombo.this.computeItens(combo.getText());
			}
			@Override
			public void focusLost(FocusEvent e) {
				AutoCombo.this.focusLost();
			}
		});
		this.combo.addVerifyListener(new VerifyListener() {
			
			@Override
			public void verifyText(VerifyEvent verifyevent) {
				if(holding) {
					return ;
				}
				sleeping = false;
				String nowText = combo.getText();
				if(verifyevent.text.isEmpty()) {
					if(verifyevent.end - verifyevent.start == nowText.length()) {
						AutoCombo.this.focusLost();
						return ;
					}
					if(verifyevent.start == 0) {
						nowText = nowText.substring(verifyevent.end);
					}else {
						nowText = nowText.substring(0, verifyevent.start) + nowText.substring(verifyevent.end);
					}
					
				}else {
					nowText += verifyevent.text;
				}
				AutoCombo.this.computeItens(nowText);
				
			}
		});
		inited=true;
	}
	
	public void open(Point loc,Point size){
		shell.setLocation(loc);
		shell.setSize(size);
		shell.open();
		shell.setVisible(true);
		OS.SetWindowPos(shell.handle , OS.HWND_TOPMOST, shell.getLocation().x , shell.getLocation().y , shell.getSize().x , shell.getSize().y , SWT.NULL);
	}
	
	private void initData(String text) {
		Control[] children = shell.getChildren();
		for(Control ctrl : children) {
			ctrl.dispose();
		}
		MyList<ContactItem> showList = new MyList<ContactItem>(shell, shell.getSize().x, shell.getSize().y);
		showList.setLocation(0, 0);
		showList.setSimpleSelect(true);
		Set<String> keys = items.keySet();
		String classReg = "\\<span\\sclass=\"emoji\\s\\w+\"\\>\\</span\\>";
		for(String key:keys){
			ContactsStruct cs = Constant.contacts.get(key);
			String nick = cs.NickName.replaceAll(classReg, "");
			String mark = cs.RemarkName.replaceAll(classReg, "");
			if(nick.toLowerCase().contains(text.toLowerCase()) 
					|| mark.toLowerCase().contains(text.toLowerCase())|| !match){
				System.out.println(cs.NickName);
				ContactItem ci = new ContactItem(cs, false, ContactsStruct.getContactsStructName(cs));
				showList.addItem(ci);
			}
		}
		showList.add(new ItemSelectionListener<ContactItem>(){

			@Override
			public void selected(ItemSelectionEvent<ContactItem> e) {
				ContactItem ci = e.item;
				if(null == ci) {
					return;
				}
				ContactsStruct cs = ci.getData();
				String name = cs.UserName;
				shell.setVisible(false);
				onSelect(name, cs);
				focusLost();
			}
			
		});
	}
	
	private void open(){
		shell=new Shell(SWT.FILL_WINDING);
		shell.setVisible(false);
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellDeactivated(ShellEvent e) {
//				shell.setVisible(false);
			}
			
		});
		
	}
	
	
	/**
	 *@author xiaokui
	 *@用途：
	 *@时间：2015-2-3下午6:06:01
	 */
	public void computeItens(String text) {
		if(null == text || text.trim().isEmpty()) {
			return;
		}
		items=(Map<String, ContactsStruct>) this.combo.getData("items");
		keyReleased(text);
	}
	private void focusLost(){
		if(!isActive&&OS.GetForegroundWindow()!=shell.handle){
			shell.setVisible(false);
		}
	}
	
	private void focusGained(){
		int length=this.combo.getText().length();
		this.combo.setSelection(new Point(length,length));
	}
	
	private void keyReleased(String text){
		if(sleeping || null == items) {
			return;
		}
		isActive=true;
		if(!shell.isVisible()){
			Point loc=this.combo.toDisplay(0, 0);
			Point csize=this.combo.getSize();
			loc.y=loc.y+csize.y;
			Point size=new Point(csize.x, 200);
			open(loc,size);
		}
		this.combo.setFocus();
		initData(text);
		isActive=false;
	}
	
	public void sleep() {
		this.sleeping = true;
	}
	
	public void setHolding(boolean hold) {
		this.holding = hold;
	}
	
	public void setMatch(boolean match) {
		this.match = match;
	}
	
	public abstract void onSelect(String key, ContactsStruct value);
}
