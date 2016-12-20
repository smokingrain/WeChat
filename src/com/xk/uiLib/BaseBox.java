package com.xk.uiLib;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.StackLayout;

public class BaseBox extends MessageBox implements ICallback{

	protected Object result;
	protected Shell shell;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public BaseBox(Shell parent, int style) {
		super(parent, style);
		createContents(280, 360);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(int x,int y) {
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	//重要。这样就可以重写swt组件
	public void checkSubclass(){}
	//去掉原始open方法
	public int open(){
		return 0;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents(int x,int y) {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(280, 360);
		shell.setText(getText());
		StackLayout stackLayout=new StackLayout();
		shell.setLayout(stackLayout);
	}
	
	public void add(Composite com){
		StackLayout lo=(StackLayout)(shell.getLayout());
		lo.topControl=com;
		if(com instanceof ICallable){
			ICallable dc=(ICallable) com;
			dc.setCallBack(this);
		}
	}
	public void setText(String text){
		shell.setText(text);
	}
	
	public Shell getShell(){
		return shell;
	}

	@Override
	public Object callback(Object obj) {
		result=obj;
		shell.dispose();
		return result;
	}
}
