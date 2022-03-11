package qtds;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class LayoutPane extends JPanel implements ActionListener {
	private static final Color			PASTE_COLOR		= new Color(184, 207, 229, 64);
	private int							tilesX			= 1;
	private int							tilesY			= 1;

	private List<List<BufferedImage>>	cells			= new ArrayList<>();
	private BufferedImage				rendered;
	private RenderPane					pane			= new RenderPane();

	BufferedImage						pasting;
	private int							pastingX;
	private int							pastingY;

	private int							tileWidth;
	private int							tileHeight;

	private JFileChooser				exportChooser	= new JFileChooser();

	private class RenderPane extends JComponent {
		private RenderPane() {
			setTransferHandler(new TransferHandler() {
				@Override
				public boolean canImport(TransferSupport support) {
					boolean supported = support.isDataFlavorSupported(BufferedImageWrapper.FLAVOR);
					if(supported) {
						try {
							Point dropPoint = support.getDropLocation().getDropPoint();
							pastingX = getTileXForPoint(dropPoint.x);
							pastingY = getTileYForPoint(dropPoint.y);
							pasting = (BufferedImage) support.getTransferable().getTransferData(BufferedImageWrapper.FLAVOR);
							pane.repaint();
						} catch(UnsupportedFlavorException | IOException e) {
							e.printStackTrace();
						}
					}
					return supported;
				}
				
				@Override
				public boolean importData(TransferHandler.TransferSupport support) {
					if(!support.isDataFlavorSupported(BufferedImageWrapper.FLAVOR)) return false;
					try {
						Point dropPoint = support.getDropLocation().getDropPoint();
						BufferedImage img = (BufferedImage) support.getTransferable().getTransferData(BufferedImageWrapper.FLAVOR);
						pasting = null;
						if(img != null) setImageAtPoint(img, dropPoint.x, dropPoint.y);
					} catch(UnsupportedFlavorException | IOException e) {
						e.printStackTrace();
					}
					return true;
				}
			});
			//this.
		}
		
		@Override
		public void paint(Graphics g) {
			int width = getWidth();
			int height = getHeight();

			g.setColor(Color.BLACK);
			g.fillRect(0, 0, width, height);
			if(rendered != null) {
				g.drawImage(rendered, 0, 0, width, height, null);
			}
			if(pasting != null) {
				int tw = width / tilesX;
				int th = height / tilesY;
				int x = pastingX * tw;
				int y = pastingY * th;
				g.drawImage(pasting, x, y, tw, th, null);
				g.setColor(PASTE_COLOR);
				g.fillRect(x, y, tw, th);
				pasting = null;
				SwingUtilities.invokeLater(() -> {
					try {
						Thread.sleep(64);
					} catch(Exception e) {}
					repaint();
				});
			}
		}
	}

	LayoutPane() {
		FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("Portable Network Graphics (*.png)", "png");
		exportChooser.addChoosableFileFilter(new FileNameExtensionFilter("All supported image types (*.bmp, *.jpeg, *.jpg, *.png)", "bmp", "jpeg", "jpg", "png"));
		exportChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Image (*.jpg, *.jpeg)", "jpeg", "jpg"));
		exportChooser.addChoosableFileFilter(pngFilter);
		exportChooser.addChoosableFileFilter(new FileNameExtensionFilter("Windows Bitmap (*.bmp)", "bmp"));
		exportChooser.setFileFilter(pngFilter);
		
		setLayout(new BorderLayout());
		String[] cmds = { "ADD_TOP", "ADD_LEFT", "ADD_RIGHT", "ADD_BOTTOM" };
		String[] positions = { BorderLayout.NORTH, BorderLayout.WEST, BorderLayout.EAST, BorderLayout.SOUTH };
		for(int i = 0; i < cmds.length; ++i) {
			JButton btn = new JButton("+");
			btn.setActionCommand(cmds[i]);
			btn.addActionListener(this);
			add(btn, positions[i]);
		}
		add(pane, BorderLayout.CENTER);
		
		cells.add(new ArrayList<>());
		cells.get(0).add(null);
	}
	
	private int getTileXForPoint(int x) {
		return x * tilesX / Math.max(1, pane.getWidth());
	}
	
	private int getTileYForPoint(int y) {
		return y * tilesY / Math.max(1, pane.getHeight());
	}
	
	private void updateTile(int x, int y, BufferedImage img) {
		Graphics g = rendered.createGraphics();
		g.drawImage(img, x * tileWidth, y * tileHeight, null);
		g.dispose();
	}
	
	private void expandPanel(int tx, int ty) {
		if(rendered == null) return;
		BufferedImage newImg = new BufferedImage(tilesX * tileWidth, tilesY * tileHeight, rendered.getType());
		Graphics g = newImg.createGraphics();
		g.drawImage(rendered, tx, ty, null);
		g.dispose();
		rendered = newImg;
		pane.repaint();
	}
	
	public BufferedImage setImageAtPoint(BufferedImage img, int x, int y) {
		if(rendered == null) {
			tileWidth = img.getWidth();
			tileHeight = img.getHeight();
		}
		
		int tileX = getTileXForPoint(x);
		int tileY = getTileYForPoint(y);
		BufferedImage old = cells.get(tileX).set(tileY, img);
		if(rendered == null) {
			rendered = new BufferedImage(tilesX * tileWidth, tilesY * tileHeight, img.getType());
		}
		updateTile(tileX, tileY, img);
		pane.repaint();
		return old;
	}
	
	private void extendTop() {
		++tilesY;
		for(int x = 0; x < tilesX; ++x) {
			cells.get(x).add(0, null);
		}
		expandPanel(0, tileHeight);
	}
	
	private void extendBottom() {
		++tilesY;
		for(int x = 0; x < tilesX; ++x) {
			cells.get(x).add(null);
		}
		expandPanel(0, 0);
	}
	
	private void extendLeft() {
		++tilesX;
		List<BufferedImage> left = new ArrayList<>(Math.max(tilesY, 10));
		for(int y = 0; y < tilesY; ++y) {
			left.add(y, null);
		}
		cells.add(0, left);
		expandPanel(tileWidth, 0);
	}
	
	private void extendRight() {
		++tilesX;
		List<BufferedImage> right = new ArrayList<>(Math.max(tilesY, 10));
		for(int y = 0; y < tilesY; ++y) {
			right.add(y, null);
		}
		cells.add(right);
		expandPanel(0, 0);
	}
	
	private String getFileExtension(File f) {
		String name = f.getName();
		int idx = name.lastIndexOf('.');
		if(idx != -1) return name.substring(idx + 1);
		return null;
	}
	
	public void export() {
		if(rendered == null) {
			JOptionPane.showMessageDialog(this, "There's no map to export!", "No Map Error", JOptionPane.ERROR_MESSAGE);
		}
		int result = exportChooser.showSaveDialog(this);
		if(result == JFileChooser.APPROVE_OPTION) {
			FileFilter filter = exportChooser.getFileFilter();
			File f = exportChooser.getSelectedFile();
			String ext = getFileExtension(f);
			FileNameExtensionFilter fnef = filter instanceof FileNameExtensionFilter ? (FileNameExtensionFilter) filter : null;
			if(ext == null) {
				if(fnef != null) {
					String[] exts = fnef.getExtensions();
					ext = exts[exts.length - 1];
				} else {
					ext = "png";
				}
				f = new File(f.getParentFile(), f.getName() + '.' + ext);
			}
			try {
				ImageIO.write(rendered, ext.toUpperCase(), f);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final String cmd = e.getActionCommand();
		SwingUtilities.invokeLater(() -> {
			switch(cmd) {
				case "ADD_TOP":
					extendTop();
					break;
				case "ADD_BOTTOM":
					extendBottom();
					break;
				case "ADD_LEFT":
					extendLeft();
					break;
				case "ADD_RIGHT":
					extendRight();
					break;
			}
		});
	}
}