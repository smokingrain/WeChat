package com.xk.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.util.Args;

import com.xk.uiLib.ICallable;
import com.xk.uiLib.ICallback;

public class InputStreamHookBody extends InputStreamBody implements ICallable<Long>{

	private ICallback<Long> callBack;
	private Long size = -1L;
	
	public InputStreamHookBody(InputStream in, ContentType contentType,
			String filename) {
		super(in, contentType, filename);
		try {
			size = (long) in.available();
		} catch (IOException e) {
			size = -1L;
		}
	}
	

	public InputStreamHookBody(InputStream in, ContentType contentType) {
		this(in, contentType, null);
	}



	public InputStreamHookBody(InputStream in, String filename) {
		this(in, ContentType.DEFAULT_BINARY, filename);
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
	public void setCallBack(ICallback<Long> callBack) {
		this.callBack = callBack;
	}

}
