package com.xk.utils.interfaces;

import java.io.File;

import com.xk.uiLib.ICallback;


public interface ICMDHandler {

	public void handle(String content, String user, ICallback<File> fileCall, ICallback<String> textCall, ICallback<File> imgCall);
	
}
