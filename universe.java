import java.net.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.imageio.*;

class Star{
  double x,x1,x2,x3,x4;
  double y,y1,y2,y3,y4;
  double vx;
  double vy;
  double ax;
  double ay;

  Star(double x,double y,double vx,double vy){
    this.x = x1 = x2 = x3 = x4 = x;
    this.y = y1 = y2 = y3 = y4 =y;
    this.vx = vx;
    this.vy = vy;
    ax = ay = 0;
  }

  public void aClear(){
    ax = ay = 0;
  }

  public void aAdd(double ax,double ay){
    this.ax += ax;
    this.ay += ay;
  }

  public void next(){
    x4 = x3; y4 = y3;
    x3 = x2; y3 = y2;
    x2 = x1; y2 = y1;
    x1 = x;  y1 = y;

    vx += ax;
    vy += ay;
    x += vx;
    y += vy;
  }
}

class Gravity{
  int x;
  int y;
  double g;
  Gravity(int x,int y){
    this.x = x;
    this.y = y;
    g = 0.00980665;
  }
}

//フレームレートとかのやつ
class FrameRate {
  private long basetime;   //測定基準時間
  private int count;      //フレーム数
  private float framerate;  //フレームレート
    
  //コンストラクタ
  public FrameRate() {
    basetime = System.currentTimeMillis();  //基準時間をセット
  }

  //フレームレートを取得
  public float getFrameRate() {
    return framerate;
  }

  //描画時に呼ぶ
  public void count() {
    ++count;        //フレーム数をインクリメント
    long now = System.currentTimeMillis();      //現在時刻を取得
    if (now - basetime >= 1000)
      {       //１秒以上経過していれば
	framerate = (float)(count * 1000) / (float)(now - basetime);        //フレームレートを計算
	basetime = now;     //現在時刻を基準時間に
	count = 0;          //フレーム数をリセット
      }
  }
}



public class universe extends JFrame implements MouseListener,MouseMotionListener,KeyListener,Runnable{
  private int screenWidth = 1024;
  private int screenHeight = 768;

  private FrameRate framerate;

  private int mouseX;
  private int mouseY;
  private boolean mousePressFlag;
  private int mousePressX;
  private int mousePressY;

  private Image offImage;

  private ArrayList<Star> star;
  private ArrayList<Gravity> gravity;

  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}

  public void mousePressed(MouseEvent e){
    if(e.getButton() == MouseEvent.BUTTON1){
      mousePressFlag = true;
      mouseX = e.getPoint().x;
      mouseY = e.getPoint().y;
      mousePressX = e.getPoint().x;
      mousePressY = e.getPoint().y;
    }else if(e.getButton() == MouseEvent.BUTTON3){
      gravity.add(new Gravity(e.getPoint().x,e.getPoint().y));
    }
  }

  public void mouseReleased(MouseEvent e){
    if(e.getButton() == MouseEvent.BUTTON1){
      mousePressFlag = false;
      star.add(new Star(mousePressX,mousePressY,(double)(e.getPoint().x - mousePressX) / 30,(double)(e.getPoint().y - mousePressY) / 10));
    }
  }

  public void mouseClicked(MouseEvent e){
  }

  public void mouseDragged(MouseEvent e){
    mouseX = e.getPoint().x;
    mouseY = e.getPoint().y;
  }
  public void mouseMoved(MouseEvent e){
    mouseX = e.getPoint().x;
    mouseY = e.getPoint().y;
  }

  public void keyPressed(KeyEvent e){}
  public void keyReleased(KeyEvent e){}
  public void keyTyped(KeyEvent e){}


  public static void main(String[] args){
    universe u = new universe();
    
    //はっじまーるよー
    u.setVisible(true);
  }
  
  public void addNotify() {
    super.addNotify();
    offImage = createImage(screenWidth, screenHeight);  //イメージバッファ生成
  }

  public universe(){
    addNotify();
    addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);

    framerate = new FrameRate();
    star = new ArrayList<Star>();
    gravity = new ArrayList<Gravity>();
    mousePressFlag = false;
	
    //サイズ指定するよ
    setSize(screenWidth,screenHeight);

    //サイズ変更しちゃダメ
    setResizable(false);

    //画面真ん中に出したいな
    setLocationRelativeTo(null);

    //バツ押された時どうすんの
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    new Thread(this).start();
  }

  public void run(){
    long error = 0;  
    int fps = 60;  
    long idealSleep = (1000 << 16) / fps;  
    long oldTime;  
    long newTime = System.currentTimeMillis() << 16; 
    long sleepTime;
    while(true){
      oldTime = newTime;
      
      for(int i = 0; i < star.size(); i++){
	star.get(i).aClear();
	for(int j = 0; j < gravity.size(); j++){
	  double r = 6356.766;
	  double mx = (gravity.get(j).x - star.get(i).x) * 10;
	  double my = (gravity.get(j).y - star.get(i).y) * 10;
	  double z = Math.sqrt(mx*mx + my*my);
	  double k = (r*r) / ((r+z)*(r+z));
	  double g = gravity.get(j).g * k * fps;
	  star.get(i).aAdd(g * mx/z,g * my/z);
	}
	star.get(i).next();
	//System.out.println(i + ": (" + star.get(i).vx + "," + star.get(i).vy + ")");
      }
      
      repaint();
      
      try{		    
	newTime = System.currentTimeMillis() << 16;  
	sleepTime = idealSleep - (newTime - oldTime) - error; // 休止できる時間  
	if (sleepTime < 0x20000) sleepTime = 0x20000; // 最低でも2msは休止  
	oldTime = newTime;  
	Thread.sleep(sleepTime >> 16); // 休止  
	newTime = System.currentTimeMillis() << 16;  
	error = newTime - oldTime - sleepTime; // 休止時間の誤差  
      }catch(Exception e){System.out.println("loopng");}
    }
  }
  
  public void updata(Graphics g){
    paint(g);
  }

  public void paint(Graphics g){
    framerate.count();

    Graphics gv = offImage.getGraphics();

    gv.setColor(Color.black);
    gv.fillRect(0, 0, screenWidth, screenHeight);

    if(mousePressFlag){
      gv.setColor(Color.yellow);
      gv.fillOval(mousePressX-5,mousePressY-5,10,10);
      gv.setColor(Color.red);
      gv.drawLine(mousePressX,mousePressY,mouseX,mouseY);
    }

    gv.setColor(Color.yellow);
    for(int i = 0; i < star.size(); i++){
      gv.fillOval((int)star.get(i).x4-1,(int)star.get(i).y4-1, 2, 2);
      gv.drawLine((int)star.get(i).x4,(int)star.get(i).y4,(int)star.get(i).x3,(int)star.get(i).y3);
      gv.fillOval((int)star.get(i).x3-2,(int)star.get(i).y3-2, 4, 4);
      gv.drawLine((int)star.get(i).x3,(int)star.get(i).y3,(int)star.get(i).x2,(int)star.get(i).y2);
      gv.fillOval((int)star.get(i).x2-3,(int)star.get(i).y2-3, 6, 6);
      gv.drawLine((int)star.get(i).x2,(int)star.get(i).y2,(int)star.get(i).x1,(int)star.get(i).y1);
      gv.fillOval((int)star.get(i).x1-4,(int)star.get(i).y1-4, 8, 8);
      gv.drawLine((int)star.get(i).x1,(int)star.get(i).y1,(int)star.get(i).x ,(int)star.get(i).y );
      gv.fillOval((int)star.get(i).x -5,(int)star.get(i).y -5,10,10);
    }

    gv.setColor(Color.magenta);
    for(int i = 0; i < gravity.size(); i++){
      gv.fillOval(gravity.get(i).x-5,gravity.get(i).y-5,10,10);
    }

    g.drawImage(offImage,0,0,screenWidth,screenHeight,this);
  }
}

