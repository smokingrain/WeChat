package org.eclipse.wb.swt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.ImageLoader;

public class GifTransfer extends ByteArrayTransfer {

	private static GifTransfer _instance = new GifTransfer();
	
	private static final int CF_GIFFORMATID = 24;
	private static final String CF_GIFFORMAT = "CF_GIFFORMAT";
	
	
	private GifTransfer() {
		
	}
	
	public static GifTransfer getInstance() {
		return _instance;
	}
	
	
	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		if (object == null || !(object instanceof ImageLoader)) return;
		if (isSupportedType(transferData)) {
			ImageLoader loader = (ImageLoader) object;
			try (ByteArrayOutputStream out = new ByteArrayOutputStream();){
				loader.save(out, loader.format);
				byte[] buffer = out.toByteArray();
				super.javaToNative(buffer, transferData);
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
	}

	@Override
	protected Object nativeToJava(TransferData transferData) {
		if (isSupportedType(transferData)) {
			byte[] buffer = (byte[])super.nativeToJava(transferData);
			if (buffer == null) {
				return null;
			}
			ImageLoader loader = new ImageLoader();
			try (ByteArrayInputStream in = new ByteArrayInputStream(buffer);){
				loader.load(in);
			} catch (Exception e) {
				return null;
			}
			return loader;
		}
		return null;
	}

	@Override
	protected int[] getTypeIds() {
		return new int[]{CF_GIFFORMATID};
	}

	@Override
	protected String[] getTypeNames() {
		return new String[]{CF_GIFFORMAT};
	}

}
