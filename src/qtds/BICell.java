package qtds;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class BICell extends JComponent implements ListCellRenderer<BufferedImage> {
	public static final Color COLOR_SELECTED = new Color(184, 207, 229);
	public static final Color COLOR_STANDARD = new Color(238, 238, 238);
	private BufferedImage img;
	
	BICell() {
		setPreferredSize(new Dimension(170, 120));
	}
	
	@Override
	public void paint(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		g.setColor(getBackground());
		g.fillRect(0, 0, width, height);
		g.drawImage(img, 5, 5, width - 10, height - 10, null);
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends BufferedImage> list, BufferedImage value, int index, boolean isSelected, boolean cellHasFocus) {
		img = value;
		setBackground(isSelected ? COLOR_SELECTED : COLOR_STANDARD);
		
		return this;
	}
}