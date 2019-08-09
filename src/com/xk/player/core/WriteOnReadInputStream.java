package com.xk.player.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.impl.AvalonLogger;

import com.xk.uiLib.ICallback;


/**
 * 读写同时进行，缓存流
 * @author kui.xiao
 *
 */
public abstract class WriteOnReadInputStream extends InputStream {

	private static final String TEMP_DIR = "try_temp";
	private long available = 0;
	private RandomAccessFile raf;
	private InputStream source;
	private MappedByteBuffer read;
	private MappedByteBuffer write;
	private long buffered = 0;
	private long bufferLimit = 40960l;
	private long markPoint = -1;
	private long markLimit = -1;
	private long length;
	private boolean closed = false;
	private ReentrantLock lock;
	private Condition cond;
	private ICallback<Double> proceCall;
	
	
	public WriteOnReadInputStream(InputStream source, long length, ICallback<Double> proceCall) throws IOException {
		if(null == source) {
			throw new NullPointerException("source is null");
		}
		this.source = source;
		this.length = length;
		this.available = length;
		this.proceCall = proceCall;
		lock = new ReentrantLock();
		cond = lock.newCondition();
		init(length);
	}
	
	/**
	 * 初始化数据
	 * @param available
	 * @throws IOException
	 * @author kui.xiao
	 */
	private void init(long available) throws IOException {
		File file = new File(TEMP_DIR);
		if(!file.exists()) {
			file.mkdirs();
		}
		File target = new File(file, "music" + System.currentTimeMillis() + ".xtemp");
		target.createNewFile();
		target.deleteOnExit();
		raf = new RandomAccessFile(target, "rw");
		FileChannel fc = raf.getChannel();
		read = fc.map(FileChannel.MapMode.READ_WRITE, 0, available);
		write = fc.map(FileChannel.MapMode.READ_WRITE, 0, available);
		bufferData(target);
	}
	
	
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		if(read.position() + len > buffered && buffered < length){
			lock.lock();
			try {
				System.out.println("try lock");
				cond.await(100 * 1000, TimeUnit.MILLISECONDS);
				System.out.println("read unlocked");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new IOException("await failed!", e);
			}finally{
				lock.unlock();
			}
		}
		if(null == b){
			throw new IOException("buffer is null!");
		}
		if(len > available){
			len = available();
		}
		read.get(b, off, len);
		available -= len;
		return len;
	}

	@Override
	public long skip(long pos) throws IOException {
		int loc = read.position();
		if(pos - loc < 0|| pos + loc > available()){
			return -1;
		}
		System.out.println("skip " + pos + " bytes");
		read.position(read.position() + (int)pos);
		available -= pos;
		return pos;
	}

	@Override
	public int available() throws IOException {
		if(closed) {
			return -1;
		}
		return (int) available;
	}

	@Override
	public void close() throws IOException {
		this.raf.close();
		super.close();
		this.closed = true;
	}

	@Override
	public synchronized void mark(int readlimit) {
		
		this.markPoint = read.position();
		this.markLimit = readlimit;
		System.out.println("mark : " + readlimit + ", markPoint : " + markPoint);
	}

	@Override
	public synchronized void reset() throws IOException {
		System.out.println("reset markPoint : " + markPoint + "");
		if(markPoint < 0) {
			available = length;
			read.position(0);
			return;
		}
		int readPosition = read.position();
		if(readPosition - markPoint > markLimit || readPosition < markPoint) {
			System.out.println("ignore reset....");
			return;
		}
		available = length - markPoint;
		read.position((int) markPoint);
		
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	/**
	 * 缓存数据
	 * @param target
	 * @author kui.xiao
	 */
	private void bufferData(final File target) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				byte[] buffer = new byte[20480];
				int len = 0;
				try {
					while(!closed && (len = source.read(buffer, 0, buffer.length))>= 0) {
						buffered += len;
						//进度回调
						if(null != proceCall) {
							proceCall.callback(buffered / (double) length);
						}
						//缓存够了要通知
						if(buffered - read.position() > bufferLimit) {
							lock.lock();
							try {
								cond.signalAll();
							} catch (Exception e) {
								
							}finally{
								lock.unlock();
							}
						}
						write.put(buffer, 0, len);
					}
					System.out.println("download over!");
					if(!closed) {
						onDownloadEnd(target);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					try {
						close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} finally {
					//缓存过程完毕要通知，不管是正常还是异常完毕
					lock.lock();
					try {
						cond.signalAll();
					} catch (Exception e) {
						
					}finally{
						lock.unlock();
					}
					try {
						source.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}
		}).start();
	}
	
	
	@Override
	public int read() throws IOException {
		byte[] buffer = new byte[1];
		if(read(buffer) == 1) {
			return buffer[0];
		}
		return -1;
	}
	
	public void reStart() throws IOException {
		if(closed) {
			throw new IOException("closed stream");
		}
		this.available = length;
		this.markPoint = -1;
		this.markLimit = -1;
		read.position(0);
	}

	public abstract void onDownloadEnd(File file);
	
}
