import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;


public class MainCanvas extends Canvas implements Runnable {
	
	private static final int LEFT_SOFT_KEY = 0;
	private static final int DIV = 20;
	private static final int ROWS = 11;
	private static final int PAGES = ROWS*4-1;
	private static final int STYLE_BOLD = 1;
	private static final int STYLE_PLAIN = 0;
	private static final int FACE_SYSTEM = 0;
	private static final int FACE_MONOSPACE = 32;
	private static final int SIZE_LARGE = 16;
	private static final int SIZE_SMALL = 8;
	private static final int SIZE_MINI = 0;
	private Font bFont;
	private Font sFont;
	private Font cFont;
	private Font mFont;
	
	private Image[][] head;
	private Image bg;//背景
	private Image sbg;
	private Image nameBox;//姓名框
	private Image textBox;//对话框
	private Image point;
	private Image buffer;//缓冲画布
	private Image bItem;
	private Image ban;
	private Image ban2;
	
	private Player player = null;
	
	private int scIndex;//场景序号
	private int diaIndex;//对话序号
	private int pIndex;//人物序号
	private int fIndex;//表情序号
	private int aNum;//选项个数
	private byte state;//状态序号 1:普通对话 2:问话 3:选项 4:跳跃 100：开始画面 101：菜单选项
	private byte tempState;
	private int page;	
	private int nextPage;
	private int nowPage;
	private int flag;
	private int banIndex;
	private int tempIndex;
	private int jump;//跳转位数
	private int chosen;//选择项
	private int colorBuffer;//颜色缓存
	private String textName;
	private byte textN;//字游标
	private int ctrlIndex;
	private int index;//选项序号
	private int cgT;
	private int picW,picH;
	private int[] picARGB;
	private boolean cgMOD;
	private boolean[] condition;
	private boolean cont;
	private boolean next;
	private boolean running;//是否继续运行
	private boolean scChange;//是否切换场景
	private boolean sayOver;//是否全部显示
	private boolean saved;
	private boolean choiceShowed;
	private boolean ended;
	
	private String[] dia;//={"2","|d012你好，今天看起来挺高兴的你","|d019你昨天和春原说什么失礼的话了?","|a0122你说什么","|t02没什么","|t04确实没什么","|d012你选的是没什么","02","|d012你选的是确实没什么","|d012我说啊","|d012明天陪我和椋去买东西吧","end"};
	private String[] item = {"返回游戏","返回菜单","存储游戏","退出游戏"};
	private String stringTemp;
	private RecordStore rs;
	protected Graphics bufGraph;//缓冲画布画笔
	protected Thread instance;
	
	
	public MainCanvas() {
		super();
		setFullScreenMode(true);
		initialize();//初始化
		
		instance = new Thread(this);
		instance.start();
		
	}
	
	
	
/*************************
 * 初始化区域
 */
	
	
	
	
	private void initialize() {
		
		head = new Image[10][10];
		try {
			textBox = Image.createImage("/text_m.png");
			nameBox = Image.createImage("/name.png");
			point = Image.createImage("/point.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		diaIndex = 1;
		state = 102;
		textName = "0";
		//readDataOver();
		running = true;
		scChange = true;
		cont = false;
		condition = new boolean[20];
		buffer = Image.createImage( 240, 320);
		bufGraph = buffer.getGraphics();
		
		bufGraph.setColor(0xffffff);
		bFont = Font.getFont(FACE_SYSTEM, STYLE_BOLD, SIZE_LARGE);
		sFont = Font.getFont(FACE_SYSTEM,STYLE_BOLD,SIZE_SMALL);
		cFont = Font.getFont(FACE_SYSTEM, STYLE_PLAIN, SIZE_SMALL);
		mFont = Font.getFont(FACE_SYSTEM, STYLE_PLAIN, SIZE_MINI);
		
	}

	
	/*************************************
	 * 绘图区域
	 */
	
	//绘制普通对话
	private void paintBuffer1()
	{
		
		int temp;
		temp = stringTemp.length();
		textN = 0;
		bufGraph.setFont(cFont);

		while(!sayOver)
			{
				
				bufGraph.setColor(0xffffff);
				bufGraph.drawImage(bg, 0, 0, 0);
				if ((pIndex > 0 && pIndex < 6) || pIndex>22)
				{
					bufGraph.drawImage(head[pIndex % 22][fIndex], Data.pFix[pIndex % 22][0], Data.pFix[pIndex % 22][1], 0);
					
				}
				bufGraph.drawImage(textBox, 0, 219, 0);
				if(pIndex != 0)
				{
					bufGraph.drawImage(nameBox, 5, 174, 0);
					bufGraph.drawString(Data.name[pIndex], 33 - cFont.substringWidth(Data.name[pIndex], 0, Data.name[pIndex].length())/2, 179, 0);
				}	
				drawStringByRow(stringTemp.substring(0,textN), 10, 229);
				textN++;
				if (textN == temp)
					sayOver = true;
				
				repaint();
				serviceRepaints();
				
				GameMIDlet.delay(50);
			}
		bufGraph.setColor(0xffffff);
		bufGraph.drawImage(bg, 0, 0, 0);
		if ((pIndex > 0 && pIndex < 6) || pIndex>22)
			bufGraph.drawImage(head[pIndex % 22][fIndex], Data.pFix[pIndex % 22][0], Data.pFix[pIndex % 22][1], 0);
		bufGraph.drawImage(textBox, 0, 219, 0);
		if(pIndex != 0)
		{
			bufGraph.drawImage(nameBox, 5, 174, 0);
			bufGraph.drawString(Data.name[pIndex], 33 - cFont.substringWidth(Data.name[pIndex], 0, Data.name[pIndex].length())/2, 179, 0);
		}	
		drawStringByRow(stringTemp, 10, 229);
		
		bufGraph.setClip(202, 295, 20, 20);
		bufGraph.drawImage(point, 202-flag*20, 295, 0);
		bufGraph.setClip(0, 0, 240, 320);
		flag = (flag+1) % 7;
		
		GameMIDlet.delay(150);
	}
	
	
	//绘制问题
	private void paintBuffer2()
	{	
		int temp;
		temp = stringTemp.length();
		textN = 0;
		bufGraph.setFont(cFont);
		while(!sayOver)
		{
			bufGraph.setColor(0xffffff);
			bufGraph.drawImage(bg, 0, 0, 0);
			if (pIndex > 0 && pIndex < 6)
			{
				bufGraph.drawImage(head[pIndex][fIndex], Data.pFix[pIndex][0], Data.pFix[pIndex][1], 0);
			}
			bufGraph.drawImage(textBox, 0, 219, 0);
			if(pIndex != 0)	
			{
				bufGraph.drawImage(nameBox, 5, 174, 0);
				bufGraph.drawString(Data.name[pIndex], 33 - cFont.substringWidth(Data.name[pIndex], 0, Data.name[pIndex].length())/2, 179, 0);
			}
			drawStringByRow(stringTemp.substring(0,textN), 10, 229);
			textN++;
			if (textN == temp)
				sayOver = true;
			repaint();
			serviceRepaints();
			GameMIDlet.delay(50);
		}
		bufGraph.setColor(0xffffff);
		bufGraph.drawImage(bg, 0, 0, 0);
		if (pIndex > 0 && pIndex < 6)
		{
			bufGraph.drawImage(head[pIndex][fIndex], Data.pFix[pIndex][0], Data.pFix[pIndex][1], 0);
		}
		bufGraph.drawImage(textBox, 0, 219, 0);
		if(pIndex != 0)
		{
			bufGraph.drawImage(nameBox, 5, 174, 0);
			bufGraph.drawString(Data.name[pIndex], 33 - cFont.substringWidth(Data.name[pIndex], 0, Data.name[pIndex].length())/2, 179, 0);
		}
		drawStringByRow(stringTemp, 10, 229);
		
		bufGraph.setClip(202, 295, 20, 20);
		bufGraph.drawImage(point, 202-flag*20, 295, 0);
		bufGraph.setClip(0, 0, 240, 320);
		flag = (flag+1) % 7;
		
		GameMIDlet.delay(200);
	}
	
	//绘制选项
	private void paintBuffer3()
	{
		bufGraph.setFont(cFont);
		bufGraph.drawImage(bg, 0, 0, 0);
		bufGraph.drawImage(textBox, 0, 219, 0);
		
		for (int i=0;i<aNum;i++)
		{
			if (i == chosen)
				{
					colorBuffer = bufGraph.getColor();
					bufGraph.setColor(0xffffff);
					bufGraph.fillRect(10, 229+i*DIV, 200, 20);	
					bufGraph.setColor(0x000000);
					bufGraph.drawString(dia[diaIndex+i].substring(4), 10, 229+i*DIV, 0);
					bufGraph.setColor(colorBuffer);
				}
			else
				{	
					colorBuffer = bufGraph.getColor();
					bufGraph.setColor(0xffffff);
					bufGraph.drawString(dia[diaIndex+i].substring(4), 10, 229+i*DIV, 0);
					bufGraph.setColor(colorBuffer);
				}
		}	
	}
	
	
	//绘制菜单
	private void paintBuffer4()
	{
		if (choiceShowed)
		{
			bufGraph.drawImage(bg, 0, 0 ,0);
			//外边框
			bufGraph.setColor(0xffffff);
			bufGraph.fillRoundRect(40, 50, 160, 170, 30, 30);
			
			//内部
			bufGraph.setColor(0x00bfff);
			bufGraph.fillRoundRect(45, 55, 150, 160, 20, 20);
		
			//标题文字
			bufGraph.setColor(0xf8f8ff);
			bufGraph.setFont(bFont);
			bufGraph.drawString("游戏选项", 70, 60, 0);
			
			//选项文字
			bufGraph.setFont(sFont);
			bufGraph.setColor(0xf8f8ff);
			for (int i=0;i < 4; i++)
			{
				
				if (i == index)
				{
					bufGraph.setColor(0xf0ffff);
					bufGraph.fillRect(75, 100 + i*20 , 90, 20);
					
					bufGraph.setColor(0x000000);
					bufGraph.drawString(item[i], 85, 100 + i*20, 0);
					bufGraph.setColor(0xf8f8ff);
				}else
				{
					bufGraph.drawString(item[i], 85, 100 + i*20, 0);
				}
			}
			if (saved)
			{
				int c = 0;
				bufGraph.setColor(0x000000);
				while (c < 10)
				{
					bufGraph.drawRoundRect(120-c*6, 140-c*3, c*12, c*6, 20, 20);
					bufGraph.drawRoundRect(125-c*6, 145-c*3, c*12-10, c*6-10, 10, 10);
					c++;
					repaint(120-c*6, 140-c*3, c*12, c*6);
					serviceRepaints();
					GameMIDlet.delay(50);
				}
				
				bufGraph.setColor(0xf8f8ff);
				bufGraph.fillRoundRect(60, 110, 120, 60, 20, 20);
				bufGraph.setColor(0x00bfff);
				bufGraph.fillRoundRect(65, 115, 110, 50, 10, 10);
				bufGraph.setColor(0xf8f8ff);
				bufGraph.setFont(bFont);
				bufGraph.drawString("存储成功", 70, 125, 0);
				
				repaint(60,110,120,60);
				serviceRepaints();
				GameMIDlet.delay(2000);
				
				saved = false;
			}
			
		}else {
			
			int c = 0;
			bufGraph.drawImage(bg, 0, 0 ,0);
			
			while(c<=140)
			{
				//外边框
				bufGraph.setColor(0xf8f8ff);
				bufGraph.fillRoundRect(40, 50, 160, 30+c, 30, 30);
				
				//内部
				bufGraph.setColor(0x00bfff);
				bufGraph.fillRoundRect(45, 55, 150, 20+c, 20, 20);	
								
				repaint(40,50,160,30+c);
				
				c = c+(160-c)/20;
				GameMIDlet.delay((c/100)*2);

			}
			choiceShowed = true;
			
		}
	}
	
	
	//绘制开始画面
	private void paintBuffer5()
	{
		bufGraph.setClip(0, 0, 240, 320);
		bufGraph.drawImage(bg, 0, 0, 0);
		bufGraph.setClip(40, 165, 15, 15);
		bufGraph.drawImage(ban, 40-banIndex*15, 165, 0);
		bufGraph.setClip(175, 165, 15, 15);
		bufGraph.drawImage(ban2, 100+banIndex*15, 165, 0);
		bufGraph.setClip( 0, 160, 240, 25);
		bufGraph.drawImage(bItem, 65, 160-index*25, 0);
		bufGraph.setClip(0, 0, 240, 320);
	}
	
	
	//绘制加载界面
	private void paintBuffer6()
	{
		bufGraph.setColor(0x000000);
		bufGraph.fillRect(0, 0, 240, 320);
		bufGraph.setColor(0xffffff);
		bufGraph.drawString("加载中", 100, 150, 0);
	}
	
	//渐入方法
	private void in(int m)	
	{
		int n = 0;
		bufGraph.setColor(0x000000);
		switch(m)
		{
		case 0:	while (n<41)
				{
					bufGraph.drawImage(bg, 0, 0, 0);
					for (int i=0; i<8; i++)
					{
						bufGraph.fillRect(0, i*40, 240, 40-n);
					}
					n++;
					repaint();
					serviceRepaints();
					
					GameMIDlet.delay(10);
				}
				bufGraph.setColor(0xffffff);break;
		case 1:	while (n<41)
				{
					bufGraph.drawImage(bg, 0, 0, 0);
					for (int i=0; i<8; i++)
					{
						bufGraph.fillRect(0, i*40+n, 240, 40-n);
					}
					n++;
					repaint();
					serviceRepaints();
					
					GameMIDlet.delay(10);
				}
				bufGraph.setColor(0xffffff);break;
		case 2:	while (n<25)
				{
					bufGraph.drawImage(bg, 0, 0, 0);
					for (int i=0; i<10; i++)
					{
						bufGraph.fillRect(i*24, 0, 24-n, 320);
					}
					n++;
					repaint();
					serviceRepaints();
					
					GameMIDlet.delay(10);
				}
				bufGraph.setColor(0xffffff);break;
		case 3:while (n<25)
				{
					bufGraph.drawImage(bg, 0, 0, 0);
					for (int i=0; i<10; i++)
					{
						bufGraph.fillRect(i*24 + n, 0, 24-n, 320);
					}
					n++;
					repaint();
					serviceRepaints();
					
					GameMIDlet.delay(10);
				
				}
				bufGraph.setColor(0xffffff);break;
		}
		
	}
	
	//渐出方法
	private void out(int m)
	{
		int n = 0;
		bufGraph.setColor(0x000000);
		switch(m)
		{
		case 0:	while (n<41)
				{
					bufGraph.drawImage(bg, 0, 0, 0);
					for (int i=0; i<8; i++)
					{
						bufGraph.fillRect(0, i * 40, 240, n);
					}
					n++;
					repaint();
					serviceRepaints();
					
					GameMIDlet.delay(10);
				}
				bufGraph.setColor(0xffffff);break;
		case 1:	while (n<41)
				{
					bufGraph.drawImage(bg, 0, 0, 0);
					for (int i=0; i<8; i++)
					{
						bufGraph.fillRect(0, (i+1) * 40 - n, 240, n);
					}
					n++;
					repaint();
					serviceRepaints();

					GameMIDlet.delay(10);
				}
				bufGraph.setColor(0xffffff);break;
		case 2:	while (n<25)
				{
					bufGraph.drawImage(bg, 0, 0, 0);
					for (int i=0; i<10; i++)
					{
						bufGraph.fillRect((i+1)*24-n, 0, n, 320);
					}
					n++;
					repaint();
					serviceRepaints();

					GameMIDlet.delay(10);
				}
				bufGraph.setColor(0xffffff);break;
		case 3:	while (n<25)
				{
					bufGraph.drawImage(bg, 0, 0, 0);
					for (int i=0; i<10; i++)
					{
						bufGraph.fillRect((i+1)*24-n, 0, n, 320);
					}
					n++;
					repaint();
					serviceRepaints();

					GameMIDlet.delay(10);
				}
				bufGraph.setColor(0xffffff);break;
		}
	}
	
	
	//绘制屏幕
	protected void paint(Graphics g) {
		g.drawImage(buffer, 0, 0, 0);
	}
	
	/*****************************
	 * 绘制主方法
	 */
	
	private void drawLogo()
	{
		Image logo1 = readImage("1.bin",0);
		Image logo2 = readImage("1.bin",1282);
		int alpha = 0x00000000;
		bufGraph.setColor(0x000000);
		GameMIDlet.delay(1000);
		
		getARGB(logo1);
		do
		{	
			
			bufGraph.fillRect(0, 0, 240, 320);
			alpha += 0x0f000000;
			drawAlpha(picARGB, picW, picH, bufGraph,75, 100, alpha);
			repaint();
			serviceRepaints();
			
		}while (alpha != 0xff000000);
		GameMIDlet.delay(2000);
		do
		{	
			
			bufGraph.fillRect(0, 0, 240, 320);
			alpha -= 0x0f000000;
			drawAlpha(picARGB, picW, picH, bufGraph,75, 100, alpha);
			repaint();
			serviceRepaints();
			
		}while (alpha != 0x00000000);
		GameMIDlet.delay(200);
		picARGB = null;
		
		getARGB(logo2);
		do
		{	
			
			bufGraph.fillRect(0, 0, 240, 320);
			alpha += 0x0f000000;
			drawAlpha(picARGB, picW, picH, bufGraph, 70, 40, alpha);
			repaint();
			serviceRepaints();
			
		}while (alpha != 0xff000000);
		GameMIDlet.delay(2000);
		do
		{	
			
			bufGraph.fillRect(0, 0, 240, 320);
			alpha -= 0x0f000000;
			drawAlpha(picARGB, picW, picH, bufGraph, 70, 40, alpha);
			repaint();
			serviceRepaints();
			
		}while (alpha != 0x00000000);
		GameMIDlet.delay(200);
		picARGB = null;
		
		logo1 = null;
		logo2 = null;
	}
	
	private void drawBegin()
	{
		paintBuffer5();
		repaint();
		serviceRepaints();
		tempIndex = (tempIndex + 1) % 11;
		if (tempIndex < 6)
			banIndex = tempIndex;
		else banIndex = 11 - tempIndex;
		
		GameMIDlet.delay(200);
	}
	
	private void drawDia(){
		pIndex = Integer.parseInt(dia[diaIndex].substring(2,4));
		fIndex = Integer.parseInt(dia[diaIndex].substring(4,5));
		paintBuffer1();
		repaint();
		serviceRepaints();
	}
	
	private void drawAsk(){
		pIndex = Integer.parseInt(dia[diaIndex].substring(2,4));
		fIndex = Integer.parseInt(dia[diaIndex].substring(4,5));
		aNum = Integer.parseInt(dia[diaIndex].substring(5,6));
		paintBuffer2();
		repaint();	
		serviceRepaints();
	}
	
	private void drawItem(){
		paintBuffer3();
		repaint();
		serviceRepaints();
	}
	
	private void drawChoice()
	{
		paintBuffer4();
		repaint();
		serviceRepaints();

	}
	
	private void drawShake()
	{
		Image tempimage = Image.createImage(240,320);
		tempimage.getGraphics().drawImage(buffer, 0, 0, 0);
		int i=11;
		while(i>0)
		{
			if (i%2 == 1)
			{
				bufGraph.setColor(0x000000);
				bufGraph.fillRect(0, 0, 240, 320);
				bufGraph.drawImage(tempimage, 0, -i, 0);
				repaint();
			}else{
				bufGraph.setColor(0x000000);
				bufGraph.fillRect(0, 0, 240, 320);
				bufGraph.drawImage(tempimage, 0, i, 0);
				repaint();
			}
			i--;
			GameMIDlet.delay(100);
		}
		tempimage = null;
		System.gc();
	}
	
	//绘制幻想世界
	private void drawFant()
	{

		try {
			sbg = Image.createImage("/beg/sbg.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		int tempAlpha = 0x00000000;
		int cont = 0;
		bufGraph.setColor(0x000000);
		GameMIDlet.delay(1000);

		getARGB(sbg);
		do
		{	
			
			bufGraph.fillRect(0, 0, 240, 320);
			tempAlpha += 0x0f000000;
			cont += 10;	
			drawAlpha(picARGB, picW, picH, bufGraph, 0, 0, tempAlpha);
			bufGraph.drawLine(0, 90, cont, 90);
			bufGraph.drawLine(0, 91, cont, 91);
			repaint();
			serviceRepaints();
			
		}while (tempAlpha != 0xff000000);
		GameMIDlet.delay(1000);
		do
		{	
			
			bufGraph.fillRect(0, 0, 240, 320);
			tempAlpha -= 0x0f000000;
			drawAlpha(picARGB, picW, picH, bufGraph, 0, 0, tempAlpha);
			bufGraph.drawLine(0, 90, cont, 90);
			bufGraph.drawLine(0, 91, cont, 91);
			repaint();
			serviceRepaints();
			
		}while (tempAlpha != 0x00000000);
		picARGB = null;
		sbg = null;
		
	}
	
	void drawCP(String name)
	{
		Image temp = Image.createImage(240,320);
		temp.getGraphics().drawImage(bg, 0, 0, 0);
		bufGraph.drawImage(bg, 0, 0, 0);
		repaint();
		int alpha = 0x00000000;
		try {
			bg = Image.createImage("/bg/"+name+".png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		GameMIDlet.delay(100);
		
		getARGB(bg);
		do
		{
			bufGraph.drawImage(temp, 0, 0, 0);
			drawAlpha(picARGB, picW, picH, bufGraph, 0, 0, alpha);			
			repaint();
			serviceRepaints();
			alpha += 0x0f000000;		
		}while(alpha != 0xff000000);
		picARGB = null;
		temp = null;
		
		System.gc();
		GameMIDlet.delay(1000);
	}
	
	void drawEnd()
	{
		bufGraph.setColor(0x000000);
		int alpha = 0xff000000;
		
		getARGB(bg);
		do
		{
			bufGraph.fillRect(0, 0, 240, 320);
			drawAlpha(picARGB, picW, picH, bufGraph, 0, 0, alpha);
			repaint();
			serviceRepaints();
			alpha -= 0x0f000000;
			
		}while(alpha != 0x00000000);
		picARGB = null;
		GameMIDlet.delay(1000);
	}
	
	private void drawStringByRow(String str, int x, int y)
	{
		switch((str.length()-1)/ROWS)
		{
			case 0: bufGraph.drawString(str, x, y, 0);break;
			case 1: bufGraph.drawString(str.substring(0, ROWS), x, y, 0);
					bufGraph.drawString(str.substring(ROWS), x, y + DIV, 0);
					break;
			case 2:	 bufGraph.drawString(str.substring(0, ROWS), x, y, 0);
					 bufGraph.drawString(str.substring(ROWS, ROWS*2), x, y + DIV, 0);
					 bufGraph.drawString(str.substring(ROWS*2), x, y + DIV*2, 0);
					 break;
			case 3:	 bufGraph.drawString(str.substring(0, ROWS), x, y, 0);
			 		 bufGraph.drawString(str.substring(ROWS, ROWS*2), x, y + DIV, 0);
			 		 bufGraph.drawString(str.substring(ROWS*2, ROWS*3), x, y + DIV*2, 0);
			 		 bufGraph.drawString(str.substring(ROWS*3), x, y + DIV*3, 0);
			 break;
		}
		
	}
	
	private void drawAlpha(int[] picARGB,int width,int height,Graphics target,int x,int	y, int alpha)
	{

		for (int i =0; i < width * height;i++)
		{
			if ((picARGB[i] & 0xff000000)!= 0)
			{
				picARGB[i] = (picARGB[i] & 0x00ffffff) + alpha;
			}
		}
		target.drawRGB(picARGB, 0, width, x, y, width, height, true);	
		//System.gc();

	}
	
	private void getARGB(Image pic)
	{
		picW = pic.getWidth();
		picH = pic.getHeight();
		picARGB = new int[picW*picH];
		pic.getRGB(picARGB, 0, picW, 0, 0, picW, picH);
	}
	
	
	/*********************************
	 * 按键控制
	 */
	protected void keyPressed(int keyCode)
	{
		switch (state)
		{
		case 1:
		case 2:	switch(getGameAction(keyCode))
				{
				case FIRE:	{
								if (sayOver)
								{
									if (nowPage == page)
										cont = true;
									else nextPage ++;
								}
								else sayOver = true;
								break;
							}	
				case LEFT_SOFT_KEY: 
							{
								tempState = state;
								index = 0;
								choiceShowed = false;
								state = 101;
								break;
							}
				default:break;
				}
				break;
		case 3:switch(getGameAction(keyCode))
				{
				case UP: 	chosen = (chosen + aNum -1) % aNum; break;
				case DOWN:	chosen = (chosen+1) % aNum ;break;
				case FIRE: 	cont = true; 
							jump = Integer.parseInt(dia[diaIndex+chosen].substring(2,4));
							break;
				default:break;
				}
				break;
		case 100:switch(getGameAction(keyCode))
				{
					case UP		: index--;
								  if (index < 0)
									index = 4;
								  if ((index ==3 && !cgMOD) || index ==2)
									index = 1;
								  break;
					case DOWN	: index++;
								  if (index == 5)
									index = 0;
								  if ((index ==3 && !cgMOD) || index ==2)
									index = 4;
							  	  break;
					case FIRE	: switch(index)
								  {
									case 0:{ 	
												try {
													player.close();
													player = null;
												} catch (Exception e) {	
													e.printStackTrace();
												}
												paintBuffer6();
												repaint();
												unloadPic();
												state = 1; 
												scChange = true;
												
												break;
											}			
									case 1:	{ 	
												try {
													player.close();
													player = null;
												} catch (Exception e) {	
													e.printStackTrace();
												}
												
												paintBuffer6();
												repaint();
												readData();
												unloadPic();
												state = 1;
												scChange = true;
												GameMIDlet.delay(100);												
												break;
											}
									case 3:{
												//TODO 这里
												if (cgMOD)
												{
													
												}
											}
									case 4:	{ 	
												GameMIDlet.end(); 
												instance = null; 
												break;
											}
									
									}
								  break;
					default: break;
				}
				break;
		case 101:if(choiceShowed)
					{
						switch(getGameAction(keyCode))
						{
							case UP: index = (index + 3) % 4;break;
							case DOWN: index = (index +1) % 4;break;
							case FIRE: switch(index)
										{
										case 0: {
													state = tempState;
													return;	
												}
										case 1: {
													paintBuffer6();
													repaint();
													unloadPic();
													state = 100;
													scChange = true;
													
													return;
												}
										case 2: {	
													saveData();
													repaint();
									
													break;
												}   	
										case 3: GameMIDlet.end(); break;
										}
							default :break;
						}
					}
		}
	}
	
	
	/******************************
	 * 图片预加载和卸载
	 */
	private void loadPic()
	{
		if (state <100)
		{
			int n = 0;
			int pi,fi;
			dia = readText("/text/"+textName+".txt");
			scIndex = Integer.parseInt(dia[0].substring(0,2));
			
			try {
				bg = Image.createImage("/bg/"+scIndex+".png");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			do
			{
				pi = Integer.parseInt(dia[0].substring(2+n*3,4+n*3)) % 22;
				fi = Integer.parseInt(dia[0].substring(4+n*3,5+n*3));
				if (pi > 0 && pi <6)
					try {
						
						head [pi][fi] = Image.createImage("/p"+pi+"/"+fi+".png");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				n++;
			}while (5 + n * 3 <= dia[0].length());		
		}else if(state == 100)
		{
			try {
				bg = Image.createImage("/beg/bbg_m_2.png");
				bItem = Image.createImage("/beg/item.png");
				ban = Image.createImage("/beg/ban_m.png");
				ban2 = Image.createImage("/beg/ban_m_2.png");
				InputStream is = getClass().getResourceAsStream("/music/1.mid");
				
				player = Manager.createPlayer(is, "audio/midi");
				
				player.prefetch();
				player.realize();
				player.setLoopCount(-1);
				player.start();
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
	}
	
	private void unloadPic()
	{
		if (state <100)	
		{
			int n = 0;
			int pi,fi;
			do
			{
				pi = Integer.parseInt(dia[0].substring(2+n*3,4+n*3)) % 22;
				fi = Integer.parseInt(dia[0].substring(4+n*3,5+n*3));
	
				head [pi][fi] = null;
				n++;
			}while (5 + n * 3 <= dia[0].length());	
			System.gc();
		}else if (state == 100)
		{
			bg = null;
			bItem = null;
			ban = null;
			ban2 = null;
			System.gc();
		}
			
	}
		
	
	/**************************************
	 * 存储、读取存档
	 */
	public void saveData()
	{
		saved = false;
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try {
			dos.writeUTF(textName);
			dos.writeInt(diaIndex);
			for (int i=0;i<20;i++)
			{
				dos.writeBoolean(condition[i]);
			}
			dos.close();
		    
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		byte[] temp = bos.toByteArray();
		
		try {
			bos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		rs = null;
		try {
			rs = RecordStore.openRecordStore("ct", false);
		} catch (Exception e1) {}
		
		if (rs == null)
		{
			try{
				rs = RecordStore.openRecordStore("ct",true);
				rs.addRecord(temp, 0, temp.length);
				rs.closeRecordStore();
				rs = null;
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}else {
			try {
				rs.setRecord(1, temp, 0, temp.length);
				rs.closeRecordStore();
			} catch (Exception e) {}
		}
		saved = true;
		return;
	}
	
	public void saveDataOver()
	{
		byte[] temp;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		rs = null;
		try {
			rs = RecordStore.openRecordStore("ct",true);
			temp = rs.getRecord(1);	
			
			rs.closeRecordStore();
			dos.write(temp, 0, temp.length-1);
			dos.writeBoolean(condition[19]);
			temp = bos.toByteArray();
			
			RecordStore.deleteRecordStore("ct");
			rs = RecordStore.openRecordStore("ct",true);
			rs.addRecord(temp, 0, temp.length);		
			rs.closeRecordStore();
			
			bos.close();
			dos.close();
		} catch (Exception e) {
			return;
		}
		
	
	}	
	
	public void readData()
	{
		byte[] temp;
		ByteArrayInputStream bis ;
		DataInputStream dis;
		
		rs = null;
		
		try {
			rs = RecordStore.openRecordStore("ct", false);
		} catch (Exception e1) { 
			return;
		}
		
		if (rs != null)
		{
			try {
				temp = rs.getRecord(1);	
				rs.closeRecordStore();
				rs = null;
			} catch (Exception e) {
				return;
			}
			
			bis = new ByteArrayInputStream(temp);
			dis = new DataInputStream(bis);
			
			try {
				textName = dis.readUTF();
				diaIndex = dis.readInt();
				for (int i=0;i<20;i++)
					condition[i] = dis.readBoolean();
				dis.close();
				bis.close();
			} catch (IOException e) {
				return;
			}
		}
	
	}
	
	public void readDataOver()
	{
		byte[] temp;
		ByteArrayInputStream bis ;
		DataInputStream dis;
		
		rs = null;
		try {
			rs = RecordStore.openRecordStore("ct",true);
			temp = rs.getRecord(1);	
			rs.closeRecordStore();
		} catch (Exception e) {
			return;
		}
		bis = new ByteArrayInputStream(temp);
		dis = new DataInputStream(bis);
		
		try {
			dis.skip(27);
			cgMOD = dis.readBoolean();
			dis.close();
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
	}
	
	
	/******************************
	 * 读取文本
	 */
	
	//从txt文件中读取文本
	private String[] readText(String s1) 
    {
        StringBuffer stringbuffer = new StringBuffer();
        char ac1[] = new char[10000];
        Vector vector = new Vector();
        int i1 = 0;
        try
        {
            InputStreamReader inputstreamreader = new InputStreamReader(getClass().getResourceAsStream(s1), "UTF-8");
            inputstreamreader.read(ac1, 0, 1);
            
            if (ac1[0] != 0xfeff )
            	stringbuffer.append(ac1[0]);
            while((i1 = inputstreamreader.read(ac1, 0, ac1.length)) != -1) 
                stringbuffer.append(ac1, 0, i1);
            inputstreamreader.close();
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
        String s2 = stringbuffer.toString();
        stringbuffer = null;
        i1 = 0;
        for(int j1 = 0; j1 < s2.length(); j1++)
        { 
        	if(s2.charAt(j1) == '\n')
            {
                vector.addElement(s2.substring(i1, j1 - 1));
                i1 = j1 + 1;
            }
        }
        vector.addElement(s2.substring(i1));
        s2 = null;
        
        String as1[] = new String[vector.size()];
        vector.copyInto(as1);
        vector = null;
        return as1;
   
    }
	
	//从bin文件中读取字节流
	public byte[] readFile(String binfile, int pos)
	{
		byte buffer[];
		int len;

		try {

			InputStream is = getClass().getResourceAsStream("/" + binfile);

			is.skip(pos);

			len  = (is.read() & 0xFF) << 24;
			len  |= (is.read() & 0xFF) << 16;
			len  |= (is.read() & 0xFF) << 8;
			len  |= (is.read() & 0xFF);

			buffer = new byte[len];
		
			is.read(buffer, 0, buffer.length);

			is.close();
			is = null;
		
			System.gc();
		} catch (Exception e) {
			buffer = null;
			e.printStackTrace();
			System.gc();
			return null;
		}

		return buffer;
	}
	
	//从bin文件中读取图片
	public Image readImage(String binfile, long pos)
	{
		byte buffer[];
		int len;

		try {
			InputStream is = getClass().getResourceAsStream("/" + binfile);
			
			is.skip(pos);
			
			len  = (is.read() & 0xFF) << 24;
			len  |= (is.read() & 0xFF) << 16;
			len  |= (is.read() & 0xFF) << 8;
			len  |= (is.read() & 0xFF);

			buffer = new byte[len];
		
			is.read(buffer, 0, buffer.length);

			is.close();
			is = null;
		
			System.gc();
		} catch (Exception e) {
			buffer = null;
			e.printStackTrace();
			System.gc();
			return null;
		}

		return Image.createImage(buffer, 0, buffer.length);
	}
	
	
	/**************************************************
	 * 主线程
	 */
	public void run() 
	{
		while(running)
		{
			GameMIDlet.delay(50);
			if(state <100)
			{
				if (scChange)
				{
						
						loadPic();
						if (dia[diaIndex].charAt(1) == 'd')
						{
							page = (dia[diaIndex].length()-5)/PAGES;
							nowPage = -1;
							nextPage = 0;
							if (0 < page)
							{
								stringTemp = dia[diaIndex].substring(5, PAGES+5);
							}else{
								stringTemp = dia[diaIndex].substring(5);
							}
							in(Math.abs(new Random().nextInt(4)));
						}
									
					scChange = false;
					next = true;
					ended = false;
					
				}
				
				if (cont)
				{
					switch (state)
					{
						case 1:{
									diaIndex++;
									next = true; 
									chosen = 0;
									
									sayOver = false;
									cont = false;
									break;
								}
						case 2:	{
									diaIndex++;
									next = true;
									chosen = 0;
									
									sayOver = false;
									cont = false;
									break;
								}
						case 3:diaIndex += jump;
								cont = false;
								break;
					}
				}
				
				if(dia[diaIndex].length()<3 || !dia[diaIndex].substring(0, 3).equals("end"))
				{
					switch (dia[diaIndex].charAt(1))
					{
						case 'd':	state = 1;break;
						case 'a':	state = 2;break;
						case 't':	state = 3;break;
						case 'c':	state = 4;break;
						case 'f':	state = 5;break;
						case 'g':	state = 7;break;
						case 's':	state = 8;break;
						case 'h':	state = 9;break;
						case 'p':	state = 10;break;
						case 'e':	state = 11;break;
						default:	state = 6;
					}				
					switch (state)
					{
					
						case 1:{
								
									if (next)
									{
										page = (dia[diaIndex].length()-6)/PAGES;
										nowPage = -1;
										nextPage = 0;
										next = false;
									}
									if (nextPage != nowPage)
									{
										nowPage = nextPage;
										if (page > nowPage)
										{	
											stringTemp = dia[diaIndex].substring(nowPage*PAGES+5, (nowPage+1)*PAGES+5);
										
										}
										else 
										{
											stringTemp = dia[diaIndex].substring(nowPage*PAGES+5);
											
										}
										sayOver = false;
										
									}
									
									drawDia();
								}break;
								
						case 2:	{
									if (next)
									{
										page = (dia[diaIndex].length()-3)/PAGES;
										nowPage = -1;
										nextPage = 0;
										next = false;
									}
									
									if(nextPage != nowPage)
									{
										nowPage = nextPage;
										if (page > nowPage)
											stringTemp = dia[diaIndex].substring(nowPage*PAGES+6, (nowPage+1)*PAGES+6);
										else stringTemp = dia[diaIndex].substring(nowPage*PAGES+6);
										sayOver = false;
								
									}
									
									drawAsk();
								}break;
						
						case 3:drawItem();break;
						
						case 4:{
									ctrlIndex = Integer.parseInt(dia[diaIndex].substring(2,4));
									
									if (dia[diaIndex].charAt(4) == '0')
										condition[ctrlIndex] = false;
									else condition[ctrlIndex] = true;
									diaIndex++;
									
									break;
								}
						
						case 5:{
									ctrlIndex = Integer.parseInt(dia[diaIndex].substring(2,4));
									if (condition[ctrlIndex] == true)
										diaIndex++;
									else diaIndex += 2;
									break;
								}
						case 6:{
									diaIndex = Integer.parseInt(dia[diaIndex]);
									
									break;
								}
						case 7:{
									cgT = Integer.parseInt(dia[diaIndex].substring(2));
									bufGraph.drawImage(bg, 0, 0, 0);
									repaint();
									serviceRepaints();
									GameMIDlet.delay(cgT);
									break;
								}	
						case 8:{
									drawShake();
									diaIndex++;
									break;
								}
						case 9:{
									drawFant();
									diaIndex++;
									break;
								}
						case 10:{
									String name = dia[diaIndex].substring(2);
									drawCP(name);
									diaIndex++;
									break;
								}
						case 11:{
									drawEnd();
									ended = true;
									diaIndex++;
								}
							
					}
					
				}else
				{
					
					
					
					if (dia[diaIndex].indexOf("endall") == -1)
					{
						if (!ended)
							out(Math.abs(new Random().nextInt(4)));
						textName = dia[diaIndex].substring(3);
						scChange = true;
						unloadPic();
						state = 1;
						diaIndex = 1;
						dia = null;
						System.gc();					
					} else 
					{
						paintBuffer6();
						repaint();
						unloadPic();
						if (condition[19])
							saveDataOver();
						state = 100;
						scChange = true;
						GameMIDlet.delay(1000);
					
					}
				
					GameMIDlet.delay(100);
				}
			}else
			{
				switch (state)
				{
					case 100:{
								if (scChange)
								{
									loadPic();
									index = 0;
									scChange = false;
								}
								drawBegin();
								break;
							  }
					case 101: 	drawChoice();
							   	break;
					case 102:{
								drawLogo();
								state = 100;
								break;
							  }
				}
			}
		}
		
	
	}


}
