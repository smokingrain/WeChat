package com.xk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.Args;

import com.xk.uiLib.ICallable;
import com.xk.uiLib.ICallback;

public class FileHookBody extends FileBody implements ICallable<Long>{

	private ICallback<Long> callBack;
	
	public FileHookBody(File file, ContentType contentType) {
		super(file, contentType);
		// TODO Auto-generated constructor stub
	}

	public FileHookBody(File file) {
		super(file);
		// TODO Auto-generated constructor stub
	}

	public FileHookBody(File file, ContentType contentType, String filename) {
		super(file, contentType, filename);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		Args.notNull(out, "Output stream");
		InputStream in = getInputStream();
		Long current = 0L;
		try {
			byte[] tmp = new byte[4096];
			int l;
			while ((l = in.read(tmp)) != -1) {
				out.write(tmp, 0, l);
				current += l;
				if(null != callBack) {
					callBack.callback(current);
				}
			}
			out.flush();
		} finally {
			in.close();
		}
	}




	@Override
	public void setCallBack(ICallback<Long> callBack) {
		this.callBack = callBack;
	}

}
