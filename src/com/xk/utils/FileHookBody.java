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
	
	private long start = 0;
	
	private long size = 0;
	
	public FileHookBody(File file, ContentType contentType) {
		super(file, contentType);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 只上传部分文件内容
	 * @param file
	 * @param contentType
	 * @param start
	 * @param size
	 */
	public FileHookBody(File file, ContentType contentType,String filename, long start, long size) {
		super(file, contentType, filename);
		this.start = start;
		this.size = size;
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
			if(start > 0) {
				in.skip(start);
			}
			byte[] tmp = new byte[4096];
			int l;
			while ((l = in.read(tmp)) != -1) {
				current += l;
				if(size > 0 && current > size) {
					l -= current - size;
				}
				out.write(tmp, 0, l);
				if(null != callBack) {
					callBack.callback(new Long(l));
				}
				if(current >= size) {
					break;
				}
			}
			out.flush();
		} finally {
			in.close();
		}
	}

	@Override
	public long getContentLength() {
		if(this.size > 0) {
			return size;
		}
		return super.getContentLength();
	}


	@Override
	public void setCallBack(ICallback<Long> callBack) {
		this.callBack = callBack;
	}

}
