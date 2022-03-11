package qtds;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class BoundsWindow extends JFrame {
	private static BoundsWindow[]	windows;
	private static int				focusCount;
	private static Rectangle		ssBounds			= new Rectangle();
	private static boolean			drawingBounds	= false;

	private static WindowAdapter	hideAll			= new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			dragCancelled();
		}
		
		@Override
		public void windowLostFocus(WindowEvent e) {
			SwingUtilities.invokeLater(() -> {
				try {
					Thread.sleep(80);
					if(--focusCount <= 0) {
						dragCancelled();
					}
				} catch(Exception e1) {
					e1.printStackTrace();
				}
			});
		}
		
		@Override
		public void windowGainedFocus(WindowEvent e) {
			++focusCount;
		}
	};

	private static KeyAdapter		escapeBtn		= new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				dragCancelled();
			}
		}
	};
	
	private static MouseAdapter		dragCheck		= new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			Object source = e.getSource();
			if(!(source instanceof BoundsWindow)) {
				System.err.println("mouse event source isnt what was expected");
				return;
			}
			BoundsWindow window = (BoundsWindow) source;
			ssBounds.x = window.bounds.x + e.getX();
			ssBounds.y = window.bounds.y + e.getY();
			drawingBounds = true;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			Object source = e.getSource();
			if(!(source instanceof BoundsWindow)) {
				System.err.println("mouse event source isnt what was expected");
				return;
			}
			BoundsWindow window = (BoundsWindow) source;
			int dstX = window.bounds.x + e.getX();
			int dstY = window.bounds.y + e.getY();
			ssBounds.width = dstX - ssBounds.x;
			ssBounds.height = dstY - ssBounds.y;
			for(BoundsWindow w : windows) {
				w.repaint();
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			mouseDragged(e);
			drawingBounds = false;
			if(Math.abs(ssBounds.width) < 5 || Math.abs(ssBounds.height) < 5) return;
			showWindows(false);
			Window.singleton.setSSBounds(ssBounds);
		}
	};

	private static void initWindows() {
		GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		windows = new BoundsWindow[screens.length];
		for(int i = 0; i < screens.length; ++i) {
			windows[i] = new BoundsWindow(screens[i], i);
		}
	}
	
	private static void dragCancelled() {
		focusCount = 0;
		showWindows(false);
		drawingBounds = false;
		Window.singleton.requestFocus();
	}
	
	public static void beginSelect() {
		if(windows == null) initWindows();
		showWindows(true);
	}
	
	private static void showWindows(boolean visible) {
		for(BoundsWindow window : windows) {
			window.setVisible(visible);
		}
		if(visible) {
			windows[0].requestFocus();
		}
	}
	
	private Rectangle bounds;
	
	private BoundsWindow(GraphicsDevice device, int index) {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(hideAll);
		addWindowFocusListener(hideAll);
		addKeyListener(escapeBtn);
		addMouseListener(dragCheck);
		addMouseMotionListener(dragCheck);
		setTitle("Bounds Setting for Game Mapper on Screen " + (index + 1));
		setUndecorated(true);
		setAlwaysOnTop(true);
		setOpacity(0.5f);
		
		bounds = device.getDefaultConfiguration().getBounds();
		setBounds(bounds);
		setResizable(false);
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, bounds.width, bounds.height);
		
		if(drawingBounds) {
			g.translate(-bounds.x, -bounds.y);
			g.setColor(Color.RED);
			g.fillRect(ssBounds.x, ssBounds.y, ssBounds.width, ssBounds.height);
			g.translate(bounds.x, bounds.y);
		}
		
		final String msg = "Click and drag to select area"; 
		int x = (bounds.width - g.getFontMetrics().stringWidth(msg)) / 2;
		int y = (bounds.height - 12) / 2;

		g.setColor(Color.BLACK);
		g.drawString(msg, x, y);
	}
}