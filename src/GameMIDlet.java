import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;


public class GameMIDlet extends MIDlet {

	//public static BeginCanvas bc;
	public static GameMIDlet gm;
	public static MainCanvas mc;
	//public static menuCanvas menu;
	public static Display display;
	
	public GameMIDlet(){
		
		gm = this;
		display = Display.getDisplay(this);
		
	}
	
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub
		
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		
		mc = new MainCanvas();
		display.setCurrent(mc);
		
	}
	
	public static void end()
	{
		try {
			gm.destroyApp(true);
			} catch (MIDletStateChangeException e) {
				e.printStackTrace();
			}
        gm.notifyDestroyed();
	}
	
	public static void delay(long time)
	{
		long now,last;
		now = System.currentTimeMillis();
		last = now;
		while((last-now) < time)
		{
			last = System.currentTimeMillis();
		}
	}

}
