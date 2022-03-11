package qtds;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BufferedImageWrapper implements Transferable {
	public static final DataFlavor		FLAVOR		= new DataFlavor(BufferedImage.class, "BufferedImage");
	private static final DataFlavor[]	FLAVOR_LIST	= { FLAVOR };

	public final BufferedImage			img;

	public BufferedImageWrapper(BufferedImage img) {
		this.img = img;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return FLAVOR_LIST;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return FLAVOR.equals(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if(flavor == FLAVOR) return img;
		throw new UnsupportedFlavorException(flavor);
	}
}
