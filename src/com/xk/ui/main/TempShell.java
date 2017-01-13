package com.xk.ui.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;  
import org.eclipse.swt.dnd.Clipboard;  
import org.eclipse.swt.dnd.DragSourceEvent;  
import org.eclipse.swt.dnd.DragSourceListener;  
import org.eclipse.swt.dnd.DropTargetEvent;  
import org.eclipse.swt.dnd.DropTargetListener;  
import org.eclipse.swt.dnd.ImageTransfer;  
import org.eclipse.swt.dnd.Transfer;  
import org.eclipse.swt.events.MouseEvent;  
import org.eclipse.swt.events.MouseListener;  
import org.eclipse.swt.events.MouseMoveListener;  
import org.eclipse.swt.events.PaintEvent;  
import org.eclipse.swt.events.PaintListener;  
import org.eclipse.swt.graphics.Cursor;  
import org.eclipse.swt.graphics.GC;  
import org.eclipse.swt.graphics.Image;  
import org.eclipse.swt.graphics.ImageData;  
import org.eclipse.swt.graphics.ImageLoader;  
import org.eclipse.swt.graphics.Point;  
import org.eclipse.swt.graphics.Rectangle;  
import org.eclipse.swt.widgets.Canvas;  
import org.eclipse.swt.widgets.Composite;  
import org.eclipse.swt.widgets.Display;  
import org.eclipse.swt.widgets.FileDialog;  
import org.eclipse.swt.widgets.MessageBox;  
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;  
public class TempShell extends Canvas implements MouseMoveListener,MouseListener,PaintListener,DragSourceListener,DropTargetListener {  
      
    private Image bi;  
    private Image get;  
    private int width,height;  
    private int startX,startY,endX,endY,tempX,tempY;  
    private Rectangle select=new Rectangle(0,0,0,0);//表示选中的区域  
    private Cursor cs;//表示一般情况下的鼠标状态  
    private States current=States.DEFAULT;// 表示当前的编辑状态  
    private Rectangle[] rec;//表示八个编辑点的区域  
    //下面四个常量,分别表示谁是被选中的那条线上的端点  
    public static final int START_X=1;  
    public static final int START_Y=2;  
    public static final int END_X=3;  
    public static final int END_Y=4;  
    private int currentX,currentY;//当前被选中的X和Y,只有这两个需要改变  
    private Point p=new Point(0,0);//当前鼠标移的地点  
    private boolean showTip=true;//是否显示提示.如果鼠标左键一按,则提示不再显了  
    private Rectangle rect=new Rectangle(0,0,0,0);  
    private Image bagground;  
    private Rectangle saveRect=new Rectangle(0,0,0,0);  
    private Rectangle cleanRect=new Rectangle(0,0,0,0);  
    private Rectangle copyRect=new Rectangle(0,0,0,0);  
    private boolean saveOk=false;  
    private boolean cleanOk=false;  
    private boolean copyOk=false;  
    private Image saveImage;  
    private Image cleanImage;  
    private Image copyImage;  
    private Image saveImages;  
    private Image cleanImages;  
    private Image copyImages;  
    private Image newSaveImages;  
    private Image newCleanImages;  
    private Image newCopyImages;  
    private Composite composite;  
    private int mouseState=10;//鼠标状态  
    private Clipboard clipboard;;  
      
    public TempShell(Composite composite,Image bi){  
        super(composite,SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);  
        this.composite=composite;  
        this.bi=bi;  
        this.cs=getDisplay().getSystemCursor(SWT.CURSOR_HAND);  
        clipboard = new Clipboard(getDisplay());  
         bagground= SWTResourceManager.getImage(TempShell.class, "/images/chat.png");
		copyImages=copyImage=SWTResourceManager.getImage(TempShell.class, "/images/chat.png");  
		cleanImages=cleanImage=SWTResourceManager.getImage(TempShell.class, "/images/chat.png");  
		saveImages=saveImage=SWTResourceManager.getImage(TempShell.class, "/images/chat.png");  
		newSaveImages=SWTResourceManager.getImage(TempShell.class, "/images/chat.png");  
		newCleanImages=SWTResourceManager.getImage(TempShell.class, "/images/chat.png");  
		newCopyImages=SWTResourceManager.getImage(TempShell.class, "/images/chat.png");  
        this.addMouseListener(this);  
        this.addMouseMoveListener(this);  
        this.addPaintListener(this);  
        /*DragSource ds=new DragSource(this,DND.DROP_MOVE); 
        ds.addDragListener(this); 
        DropTarget dt=new DropTarget(this,DND.DROP_MOVE); 
        dt.addDropListener(this);*/  
        initRecs();  
    }  
      
    private void initRecs(){  
        rec=new Rectangle[8];  
        for(int i=0;i<rec.length;i++){  
            rec[i]=new Rectangle(0,0,0,0);  
        }  
    }  
      
//  根据东南西北等八个方向决定选中的要修改的X和Y的座标  
    private void initSelect(States state){  
        switch(state){  
            case DEFAULT:  
                currentX=0;  
                currentY=0;  
                break;  
            case EAST:  
                System.out.println("EAST  endX="+endX+" startX="+startX+" START_X="+START_X+" END_X="+END_X);  
                currentX=(endX>startX?END_X:START_X);  
                currentY=0;  
                break;  
            case WEST:  
                System.out.println("WEST endX="+endX+" startX="+startX+" START_X="+START_X+" END_X="+END_X);  
                currentX=(endX>startX?START_X:END_X);  
                currentY=0;  
                break;  
            case NORTH:  
                System.out.println("NORTH");  
                currentX=0;  
                currentY=(startY>endY?END_Y:START_Y);  
                break;  
            case SOUTH:  
                System.out.println("SOUTH");  
                currentX=0;  
                currentY=(startY>endY?START_Y:END_Y);  
                break;  
            case NORTH_EAST:  
                System.out.println("EAST");  
                currentY=(startY>endY?END_Y:START_Y);  
                currentX=(endX>startX?END_X:START_X);  
                break;  
            case NORTH_WEST:  
                System.out.println("NORTH_WEST");  
                currentY=(startY>endY?END_Y:START_Y);  
                currentX=(endX>startX?START_X:END_X);  
                break;  
            case SOUTH_EAST:  
                System.out.println("SOUTH_EAST");  
                currentY=(startY>endY?START_Y:END_Y);  
                currentX=(endX>startX?END_X:START_X);  
                break;  
            case SOUTH_WEST:  
                System.out.println("SOUTH_WEST");  
                currentY=(startY>endY?START_Y:END_Y);  
                currentX=(endX>startX?START_X:END_X);  
                break;  
            default:  
                currentX=0;  
                currentY=0;  
                break;  
        }  
    }  
    public void mouseMove(MouseEvent me) {  
        doMouseMoved(me);  
        switch(mouseState){  
        case 1:  
            current=States.WEST;  
            break;  
        case 2:  
            current=States.EAST;  
            break;  
        case 3:  
            current=States.SOUTH;  
            break;  
        case 4:  
            current=States.NORTH;  
            break;  
        case 5:  
            current=States.NORTH_EAST;  
            break;  
        case 6:  
            current=States.SOUTH_EAST;  
            break;  
        case 7:  
            current=States.NORTH_WEST;  
            break;  
        case 8:  
            current=States.SOUTH_WEST;  
            break;  
        }  
        initSelect(current);  
        int x=me.x;  
        int y=me.y;  
        //Move(me.x, me.y);  
        switch(mouseState){  
        case 0:  
            if(current==States.MOVE){  
                startX+=(x-tempX);  
                startY+=(y-tempY);  
                endX+=(x-tempX);  
                endY+=(y-tempY);  
                tempX=x;  
                tempY=y;  
            }  
            break;  
        case 1:  
            if(currentX==START_X){  
                startX+=(x-tempX);  
                tempX=x;  
            }else{  
                endX+=(x-tempX);  
                tempX=x;  
            }  
            break;  
        case 2:  
            if(currentX==START_X){  
                startX+=(x-tempX);  
                tempX=x;  
            }else{  
                endX+=(x-tempX);  
                tempX=x;  
            }  
            break;  
        case 3:  
            if(currentY==START_Y){  
                startY+=(y-tempY);  
                tempY=y;  
            }else{  
                endY+=(y-tempY);  
                tempY=y;  
            }  
            break;  
        case 4:  
            System.out.println("currentY="+currentY+" START_Y="+START_Y);  
            if(currentY==START_Y){  
                startY+=(y-tempY);  
                tempY=y;  
            }else{  
                endY+=(y-tempY);  
                tempY=y;  
            }  
            break;  
        case 5:  
            if(currentY==START_Y){  
                startY+=(y-tempY);  
                tempY=y;  
            }else{  
                endY+=(y-tempY);  
                tempY=y;  
            }  
            if(currentX==START_X){  
                startX+=(x-tempX);  
                tempX=x;  
            }else{  
                endX+=(x-tempX);  
                tempX=x;  
            }  
            break;  
        case 6:  
            if(currentY==START_Y){  
                startY+=(y-tempY);  
                tempY=y;  
            }else{  
                endY+=(y-tempY);  
                tempY=y;  
            }  
            if(currentX==START_X){  
                startX+=(x-tempX);  
                tempX=x;  
            }else{  
                endX+=(x-tempX);  
                tempX=x;  
            }  
            break;  
        case 7:  
            if(currentY==START_Y){  
                startY+=(y-tempY);  
                tempY=y;  
            }else{  
                endY+=(y-tempY);  
                tempY=y;  
            }  
            if(currentX==START_X){  
                startX+=(x-tempX);  
                tempX=x;  
            }else{  
                endX+=(x-tempX);  
                tempX=x;  
            }  
            break;  
        case 8:  
            if(currentY==START_Y){  
                startY+=(y-tempY);  
                tempY=y;  
            }else{  
                endY+=(y-tempY);  
                tempY=y;  
            }  
            if(currentX==START_X){  
                startX+=(x-tempX);  
                tempX=x;  
            }else{  
                endX+=(x-tempX);  
                tempX=x;  
            }  
            break;  
        case 9:  
            startX=tempX;  
            startY=tempY;  
            endX=x;  
            endY=y;  
            break;  
        case 10:  
            break;  
        }  
        this.redraw();  
        //if(mouseState==0){  
            //Move(me.x, me.y);  
            //this.setRedraw(true);  
        //}  
        /*if(showTip){ 
            p=new Point(me.x,me.y); 
            this.redraw(); 
        }*/  
    }  
      
//  特意定义一个方法处理鼠标移动,是为了每次都能初始化一下所要选择的地区  
    private void doMouseMoved(MouseEvent me){  
        Point px=new Point(me.x,me.y);  
        if(select.contains(px)){  
            this.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));  
            current=States.MOVE;  
        }else if(saveRect.contains(px)){  
            this.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));  
            saveImage=newSaveImages;  
            saveOk=true;  
            copyOk=false;  
            cleanOk=false;  
            mouseState=10;  
        }else if(cleanRect.contains(px)){  
            this.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));  
            cleanImage=newCleanImages;  
            cleanOk=true;  
            saveOk=false;  
            copyOk=false;  
            mouseState=10;  
        }else if(copyRect.contains(px)){  
            this.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));  
            copyImage=newCopyImages;  
            copyOk=true;  
            cleanOk=false;  
            saveOk=false;  
            mouseState=10;  
        } else{  
            saveOk=false;  
            copyOk=false;  
            cleanOk=false;  
            copyImage=copyImages;  
            cleanImage=cleanImages;  
            saveImage=saveImages;  
            States[] st=States.values();  
            for(int i=0;i<rec.length;i++){  
                if(rec[i].contains(px)){  
                    current=st[i];  
                    this.setCursor(st[i].getCursor());  
                    return;  
                }  
            }  
            this.setCursor(cs);  
            current=States.DEFAULT;  
        }  
        this.redraw();  
    }  
      
    public void getImageScreen(int x,int y,int w,int h) {  
        GC gc = new GC(bi);  
        int actW = Math.min(w, bi.getBounds().width - x);  
        int actH = Math.min(h, bi.getBounds().height - y);  
        get = new Image(getShell().getDisplay(), actW, actH);  
        gc.copyArea(get, x, y);  
        gc.dispose();  
    }  
    public void mouseDoubleClick(MouseEvent me) {  
                //Rectangle rec=new Rectangle(startX,startY,Math.abs(endX-startX),Math.abs(endY-startY));  
                Point p=new Point(me.x,me.y);  
                if(select.contains(p)){  
                    if(select.x+select.width<bi.getBounds().width&&select.y+select.height<bi.getBounds().height){  
                        this.getImageScreen(select.x,select.y,select.width,select.height);  
                        composite.setVisible(false);  
                        //updates();  
                    }else{  
                        int wid=select.width,het=select.height;  
                        if(select.x+select.width>=bi.getBounds().width){  
                            wid=bi.getBounds().width-select.x;  
                        }  
                        if(select.y+select.height>=bi.getBounds().height){  
                            het=bi.getBounds().height-select.y;  
                        }  
                        this.getImageScreen(select.x,select.y,wid,het);  
                        composite.setVisible(false);  
                        //updates();  
                    }  
                }  
                  
                doCopy(get);  
    }  
      
    /** 
     *公共的处理把当前的图片加入剪帖板的方法 
     */  
    public void doCopy(final Image get){  
        try{  
            if(get==null){  
                openMessageBox(this.getShell(),"错误","图片不能为空!!");  
                return;  
            }  
            ImageTransfer imageTransfer = ImageTransfer.getInstance();  
            clipboard.setContents(new Object[] { get.getImageData() }, new Transfer[] { imageTransfer });  
            openMessageBox(this.getShell(),"错误","已复制到系统粘帖板!!");  
        }catch(Exception exe){  
            exe.printStackTrace();  
            openMessageBox(this.getShell(),"错误","复制到系统粘帖板出错!!");  
        }  
        //composite.dispose();  
    }  
      
    /** 
     *公用的处理保存图片的方法 
     *这个方法不再私有了 
     */  
    public  void doSave(Image get){  
        try{  
            if(get==null){  
                openMessageBox(this.getShell(),"错误","图片不能为空!!");  
                return;  
            }  
            FileDialog fd=new FileDialog(this.getShell(),SWT.SAVE);  
            fd.setText("保存图片");  
            String[] filterExt={"*.jpeg"};  
            fd.setFilterExtensions(filterExt);  
            String savePath=fd.open();  
            if(savePath!=null){  
                ImageLoader loader=new ImageLoader();  
                loader.data = new ImageData[] {get.getImageData()};  
                loader.save(savePath, SWT.IMAGE_JPEG);  
            }  
        } catch(Exception exe){  
            exe.printStackTrace();  
        }  
        composite.dispose();  
    }  
    public void mouseDown(MouseEvent me) {  
         //showTip=false;  
         mouseState=9;  
         for(int i=0;i<rec.length;i++){  
                if(rec[i].contains(me.x,me.y)){  
                    System.out.println(i);  
                    switch(i){  
                    case 0:  
                        mouseState=7;  
                        break;  
                    case 1:  
                        mouseState=4;  
                        break;  
                    case 2:  
                        mouseState=5;  
                        break;  
                    case 3:  
                        mouseState=2;  
                        break;  
                    case 4:  
                        mouseState=6;  
                        break;  
                    case 5:  
                        mouseState=3;  
                        break;  
                    case 6:  
                        mouseState=8;  
                        break;  
                    case 7:  
                        mouseState=1;  
                        break;  
                    }  
                }  
          }  
         if(select.contains(me.x,me.y)){  
             mouseState=0;  
         }  
         tempX=me.x;  
         tempY=me.y;  
    }  
    public void mouseUp(MouseEvent me) {  
        mouseState=10;  
        if(me.button==3){  
            if(current==States.MOVE){  
                //showTip=true;  
                p=new Point(me.x,me.y);  
                startX=0;  
                startY=0;  
                endX=0;  
                endY=0;  
                this.redraw();  
            } else{  
                composite.setVisible(false);  
                composite.dispose();  
                //updates();  
            }  
              
        }  
        if (saveOk) {  
            if (select.x + select.width < bi.getBounds().width  
                    && select.y + select.height < bi.getBounds().height) {  
                this.getImageScreen(select.x, select.y, select.width,  
                        select.height);  
                composite.setVisible(false);  
                // updates();  
            } else {  
                int wid = select.width, het = select.height;  
                if (select.x + select.width >= bi.getBounds().width) {  
                    wid = bi.getBounds().width - select.x;  
                }  
                if (select.y + select.height >= bi.getBounds().height) {  
                    het = bi.getBounds().height - select.y;  
                }  
                this.getImageScreen(select.x, select.y, wid, het);  
                composite.setVisible(false);  
                // updates();  
            }  
            doSave(get);  
        }  
        if(copyOk){  
            try {  
                if (select.x + select.width < bi.getBounds().width  
                        && select.y + select.height < bi.getBounds().height) {  
                    this.getImageScreen(select.x, select.y, select.width,  
                            select.height);  
                    composite.setVisible(false);  
                    // updates();  
                } else {  
                    int wid = select.width, het = select.height;  
                    if (select.x + select.width >= bi.getBounds().width) {  
                        wid = bi.getBounds().height - select.x;  
                    }  
                    if (select.y + select.height >= bi.getBounds().height) {  
                        het = bi.getBounds().height - select.y;  
                    }  
                    this.getImageScreen(select.x, select.y, wid, het);  
                    composite.setVisible(false);  
                    // updates();  
                }  
            } catch (Exception e) {  
                // TODO Auto-generated catch block  
                composite.setVisible(false);  
                e.printStackTrace();  
            }  
            doCopy(get);  
        }  
        if(cleanOk){  
            composite.setVisible(false);  
            composite.dispose();  
        }  
    }  
    public void paintControl(PaintEvent e) {  
        GC g=e.gc;  
        g.drawImage(bi,width,height);  
        g.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));  
        g.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));  
        g.drawLine(startX,startY,endX,startY);  
        g.drawLine(startX,endY,endX,endY);  
        g.drawLine(startX,startY,startX,endY);  
        g.drawLine(endX,startY,endX,endY);  
        int x=startX<endX?startX:endX;  
        int y=startY<endY?startY:endY;  
        select=new Rectangle(x,y,Math.abs(endX-startX),Math.abs(endY-startY));  
        int imageY=y+Math.abs(endY-startY)+5;  
        if(imageY>bi.getBounds().height-20){  
            imageY=bi.getBounds().height-Math.abs(endY-startY)-30;  
            if(imageY<20){  
                imageY=5;  
            }  
        }  
        rect=new Rectangle(x+Math.abs(endX-startX)-bagground.getBounds().width, imageY, bagground.getBounds().width, bagground.getBounds().height);  
        g.drawImage(bagground,0,0,bagground.getBounds().width,bagground.getBounds().height,rect.x,rect.y,rect.width,rect.height);  
        saveRect=new Rectangle(rect.x+rect.width-saveImage.getBounds().width-50,rect.y+2,saveImage.getBounds().width,saveImage.getBounds().height);  
        g.drawImage(saveImage,0,0,saveImage.getBounds().width,saveImage.getBounds().height,saveRect.x,saveRect.y,saveRect.width,saveRect.height);  
        cleanRect=new Rectangle(saveRect.x+cleanImage.getBounds().width+2,rect.y+2,cleanImage.getBounds().width,cleanImage.getBounds().height);  
        g.drawImage(cleanImage,0,0,cleanImage.getBounds().width,cleanImage.getBounds().height,cleanRect.x,cleanRect.y,cleanRect.width,cleanRect.height);  
        copyRect=new Rectangle(cleanRect.x+copyImage.getBounds().width+2,rect.y+2,copyImage.getBounds().width,copyImage.getBounds().height);  
        g.drawImage(copyImage,0,0,copyImage.getBounds().width,copyImage.getBounds().height,copyRect.x,copyRect.y,copyRect.width,copyRect.height);  
        int x1=(startX+endX)/2;  
        int y1=(startY+endY)/2;  
        g.fillRectangle(x1-2,startY-2,5,5);  
        g.fillRectangle(x1-2,endY-2,5,5);  
        g.fillRectangle(startX-2,y1-2,5,5);  
        g.fillRectangle(endX-2,y1-2,5,5);  
        g.fillRectangle(startX-2,startY-2,5,5);  
        g.fillRectangle(startX-2,endY-2,5,5);  
        g.fillRectangle(endX-2,startY-2,5,5);  
        g.fillRectangle(endX-2,endY-2,5,5);  
        rec[0]=new Rectangle(x-5,y-5,10,10);  
        rec[1]=new Rectangle(x1-5,y-5,10,10);  
        rec[2]=new Rectangle((startX>endX?startX:endX)-5,y-5,10,10);  
        rec[3]=new Rectangle((startX>endX?startX:endX)-5,y1-5,10,10);  
        rec[4]=new Rectangle((startX>endX?startX:endX)-5,(startY>endY?startY:endY)-5,10,10);  
        rec[5]=new Rectangle(x1-5,(startY>endY?startY:endY)-5,10,10);  
        rec[6]=new Rectangle(x-5,(startY>endY?startY:endY)-5,10,10);  
        rec[7]=new Rectangle(x-5,y1-5,10,10);  
        /*if(showTip){ 
            g.setBackground(getDisplay().getSystemColor(SWT.COLOR_CYAN)); 
            g.fillRectangle(p.x,p.y,170,20); 
            //g.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED)); 
            g.drawRectangle(p.x,p.y,170,20); 
            g.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK)); 
            g.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK)); 
            g.drawString("请按住鼠标左键不放选择截图区",p.x,p.y+2); 
        }*/  
    }  
      
    private void openMessageBox(Shell shell, String title, String text) {  
        MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.NO);  
        mb.setText(title);  
        mb.setMessage(text);  
        mb.open();  
    }  
      
    enum States{  
        NORTH_WEST(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZENW)),//表示西北角  
        NORTH(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEN)),  
        NORTH_EAST(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZENE)),  
        EAST(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEE)),  
        SOUTH_EAST(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZESE)),  
        SOUTH(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZES)),  
        SOUTH_WEST(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZESW)),  
        WEST(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEW)),  
        MOVE(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEALL)),  
        DEFAULT(Display.getCurrent().getSystemCursor(SWT.CURSOR_NO));  
        private Cursor cs;  
        States(Cursor cs){  
            this.cs=cs;  
        }  
        public Cursor getCursor(){  
            return cs;  
        }  
    }  
    public void dragFinished(DragSourceEvent event) {  
        System.out.println("Finished");  
    }  
    public void dragSetData(DragSourceEvent event) {  
        System.out.println("SetData");  
    }  
    public void dragStart(DragSourceEvent event) {  
        System.out.println("Start");  
    }  
    public void dragEnter(DropTargetEvent event) {  
        System.out.println("Enter");  
    }  
    public void dragLeave(DropTargetEvent event) {  
        System.out.println("Leave");  
    }  
    public void dragOperationChanged(DropTargetEvent event) {  
        System.out.println("OperationChanged");  
    }  
    public void dragOver(DropTargetEvent event) {  
        System.out.println("Over");  
    }  
    public void drop(DropTargetEvent event) {  
        System.out.println("drop");  
    }  
    public void dropAccept(DropTargetEvent event) {  
        System.out.println("Accept");  
    }  
      
    public void Move(int x,int y){  
        if(current==States.MOVE){  
            startX+=(x-tempX);  
            startY+=(y-tempY);  
            endX+=(x-tempX);  
            endY+=(y-tempY);  
            tempX=x;  
            tempY=y;  
        }else if(current==States.EAST||current==States.WEST){  
           if(currentX==START_X){  
                startX+=(x-tempX);  
                tempX=x;  
            }else{  
                endX+=(x-tempX);  
                tempX=x;  
            }  
        }else if(current==States.NORTH||current==States.SOUTH){  
            if(currentY==START_Y){  
                startY+=(y-tempY);  
                tempY=y;  
            }else{  
                endY+=(y-tempY);  
                tempY=y;  
            }  
        }else if(current==States.NORTH_EAST||current==States.NORTH_EAST||  
                current==States.SOUTH_EAST||current==States.SOUTH_WEST){  
            if(currentY==START_Y){  
                startY+=(y-tempY);  
                tempY=y;  
            }else{  
                endY+=(y-tempY);  
                tempY=y;  
            }  
            if(currentX==START_X){  
                startX+=(x-tempX);  
                tempX=x;  
            }else{  
                endX+=(x-tempX);  
                tempX=x;  
            }  
              
        }else{  
            startX=tempX;  
            startY=tempY;  
            endX=x;  
            endY=y;  
        }  
        this.redraw();  
    }  
}  