package qtds;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.*;

public class Window extends JFrame implements ActionListener, KeyListener {
	static final Window						singleton;

	Rectangle								ssArea;
	Dimension								screens				= new Dimension(12, 6);

	Dimension								ssScaled			= new Dimension(320, 220);

	private DefaultListModel<BufferedImage>	unsavedImgsModel	= new DefaultListModel<>();
	private JList<BufferedImage>			unsavedImgsList		= new JList<BufferedImage>(unsavedImgsModel);

	private LayoutPane						layout				= new LayoutPane();

	private Robot							robot;

	private Window() {
		ssArea = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		try {
			robot = new Robot();
		} catch(AWTException e) {
			e.printStackTrace();
			System.exit(1);
		}
		createLayout();

		setTitle("Game mapper");
		setSize(1280, 720);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public void setSSBounds(Rectangle bounds) {
		if(bounds.width < 0) {
			bounds.x += bounds.width;
			bounds.width *= -1;
		}
		if(bounds.height < 0) {
			bounds.y += bounds.height;
			bounds.height *= -1;
		}
		ssArea.setBounds(bounds);
		singleton.saveSettings();
	}

	private void createLayout() {
		JPanel all = new JPanel();
		all.setLayout(new BorderLayout());

		all.add(layout, BorderLayout.CENTER);

		JPanel leftPane = new JPanel();
		leftPane.setLayout(new BorderLayout());
		unsavedImgsList.setCellRenderer(new BICell());
		unsavedImgsList.setDragEnabled(true);
		unsavedImgsList.setTransferHandler(new TransferHandler() {
			@Override
			protected Transferable createTransferable(JComponent c) {
				BufferedImage img = unsavedImgsList.getSelectedValue();
				
				if(img == null) return null;
				return new BufferedImageWrapper(img);
			}
			
			@Override
			public int getSourceActions(JComponent c) {
				return MOVE;
			}

			@Override
		    protected void exportDone(JComponent source, Transferable data, int action) {
				layout.pasting = null;
				layout.repaint();
				if(action == MOVE && source == unsavedImgsList && data instanceof BufferedImageWrapper) {
					unsavedImgsModel.removeElement(((BufferedImageWrapper) data).img);
				}
		    }
		});
		unsavedImgsList.addKeyListener(this);
		leftPane.add(new JScrollPane(unsavedImgsList), BorderLayout.CENTER);
		
		JPanel btnPane = new JPanel();
		String[] btnNames = { "Snap", "Bounds", "Export" };
		String[] btnCmds = { "SCREENSHOT", "BOUNDS", "EXPORT" };
		for(int i = 0; i < btnNames.length; ++i) {
			JButton button = new JButton(btnNames[i]);
			button.setActionCommand(btnCmds[i]);
			button.addActionListener(this);
			btnPane.add(button);
		}
		leftPane.add(btnPane, BorderLayout.SOUTH);
		
		all.add(leftPane, BorderLayout.WEST);

		setContentPane(all);
	}

	private BufferedImage createScreenshot() {
		BufferedImage img = robot.createScreenCapture(ssArea);
		BufferedImage dst = new BufferedImage(ssScaled.width, ssScaled.height, img.getType());
		Graphics2D g = dst.createGraphics();
		g.drawImage(img, 0, 0, ssScaled.width, ssScaled.height, null);
		g.dispose();
		return dst;
	}
	
	static {
		singleton = new Window();
	}

	public static void main(String[] args) {
		singleton.loadSettings();
		singleton.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
			case "SCREENSHOT":
				SwingUtilities.invokeLater(() -> {
					BufferedImage img = createScreenshot();
					unsavedImgsModel.addElement(img);
				});
				break;
			case "BOUNDS":
				BoundsWindow.beginSelect();
				break;
			case "EXPORT":
				SwingUtilities.invokeLater(() -> {
					layout.export();
				});
				break;
		}
	}
	
	private void saveSettings() {
		File f = new File("gm_settings.ini");
		try(PrintWriter out = new PrintWriter(f)) {
			out.println("screenshot-x=" + ssArea.x);
			out.println("screenshot-y=" + ssArea.y);
			out.println("screenshot-width=" + ssArea.width);
			out.println("screenshot-height=" + ssArea.height);
			out.println("downscaled-width=" + ssScaled.width);
			out.println("downscaled-height=" + ssScaled.height);
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private void loadSettings() {
		File f = new File("gm_settings.ini");
		if(!f.exists()) return;
		try(Scanner scan = new Scanner(f)) {
			while(scan.hasNextLine()) {
				String line = scan.nextLine();
				int idx = line.indexOf('#');
				if(idx != -1) line = line.substring(0, idx);
				line = line.trim();
				if(line.isEmpty()) continue;
				String[] parts = line.split("=");
				if(parts.length != 2) continue;
				for(int i = 0; i < 2; ++i) {
					parts[i] = parts[i].trim().toLowerCase();
				}
				int val;
				try {
					val = Integer.parseInt(parts[1]);
				} catch(NumberFormatException nfe) {
					continue;
				}
				switch(parts[0]) {
					case "downscaled-width":
						ssScaled.width = val;
						break;
					case "downscaled-height":
						ssScaled.height = val;
						break;
					case "screenshot-x":
						ssArea.x = val;
						break;
					case "screenshot-y":
						ssArea.y = val;
						break;
					case "screenshot-width":
						ssArea.width = val;
						break;
					case "screenshot-height":
						ssArea.height = val;
						break;
				}
			}

			if(ssArea.width < 0) {
				ssArea.x += ssArea.width;
				ssArea.width *= -1;
			}
			if(ssArea.height < 0) {
				ssArea.y += ssArea.height;
				ssArea.height *= -1;
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void removeActiveImageFromUnsavedList() {
		BufferedImage selected = unsavedImgsList.getSelectedValue();
		if(selected != null) {
			unsavedImgsModel.removeElement(selected);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		switch(e.getKeyChar()) {
			case KeyEvent.VK_DELETE:
				SwingUtilities.invokeLater(() -> {
					removeActiveImageFromUnsavedList();
				});
				break;
			case 's':
				saveSettings();
				break;
			case 'l':
				loadSettings();
				break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}
}