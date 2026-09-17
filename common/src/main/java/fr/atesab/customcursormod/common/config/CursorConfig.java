package fr.atesab.customcursormod.common.config;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLPixels;
import org.lwjgl.sdl.SDLSurface;
import org.lwjgl.sdl.SDL_Surface;
import org.lwjgl.system.MemoryUtil;

import fr.atesab.customcursormod.common.handler.CommonResourceLocation;
import fr.atesab.customcursormod.common.utils.MathHelper;

public class CursorConfig {
	public static class CursorConfigStore {
		int xHotSpot;
		int yHotSpot;
		String link;
		int size;
		public CursorConfigStore(CursorConfig current) {
			this();
			xHotSpot = current.xHotSpot;
			yHotSpot = current.yHotSpot;
			link = current.link;
			size = current.size;
		}
		private CursorConfigStore() {
		}
	}
	public static CursorConfig read(CursorConfigStore store, CursorConfig defaultConfig) {
		CursorConfig cursor = new CursorConfig();
		if (store == null) {
			cursor.xHotSpot = defaultConfig.xHotSpot;
			cursor.yHotSpot = defaultConfig.yHotSpot;
			cursor.link = defaultConfig.link;
			cursor.size = defaultConfig.size;
		} else {
			cursor.xHotSpot = store.xHotSpot;
			cursor.yHotSpot = store.yHotSpot;
			cursor.link = store.link == null ? defaultConfig.link : store.link;
			cursor.size = store.size > 0 ? store.size : defaultConfig.size;
		}

		return cursor;
	}

	private int xHotSpot;
	private int yHotSpot;
	private String link;
	private int size = 32; // 默认大小为32像素
	private long cursor = MemoryUtil.NULL;

	private CursorConfig() {
	}

	public CursorConfig(String link) {
		this(link, 0, 0);
	}

	public CursorConfig(String link, int xHotSpot, int yHotSpot) {
		this(link, xHotSpot, yHotSpot, 32);
	}

	public CursorConfig(String link, int xHotSpot, int yHotSpot, int size) {
		this.xHotSpot = xHotSpot;
		this.yHotSpot = yHotSpot;
		this.link = link;
		this.size = size;
	}

	private void allocate() {
		BufferedImage image = readImage();
		if (image == null)
			return;
		if (cursor != MemoryUtil.NULL)
			freeCursor();
		cursor = createCursor(image);
	}

	private long createCursor(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		SDL_Surface surface = SDLSurface.SDL_CreateSurface(width, height, SDLPixels.SDL_PIXELFORMAT_ARGB8888);
		if (surface == null)
			return MemoryUtil.NULL;
		try {
			int[] argb = image.getRGB(0, 0, width, height, null, 0, width);
			IntBuffer pixels = surface.pixels().order(ByteOrder.nativeOrder()).asIntBuffer();
			int rowStride = surface.pitch() / Integer.BYTES;
			for (int y = 0; y < height; y++) {
				pixels.position(y * rowStride);
				pixels.put(argb, y * width, width);
			}
			return SDLMouse.SDL_CreateColorCursor(surface, MathHelper.clamp(xHotSpot, 0, width - 1),
					MathHelper.clamp(yHotSpot, 0, height - 1));
		} finally {
			SDLSurface.SDL_DestroySurface(surface);
		}
	}

	public CursorConfig copy() {
		CursorConfig config = new CursorConfig(link, xHotSpot, yHotSpot, size);
		return config;
	}

	public void freeCursor() {
		if (isAllocate()) {
			SDLMouse.SDL_DestroyCursor(cursor);
			cursor = MemoryUtil.NULL;
		}
	}

	public long getCursor() {
		if (cursor == MemoryUtil.NULL)
			allocate();
		return cursor;
	}

	public String getLink() {
		return link;
	}

	public InputStream getResource() {
		try {
			return getResourceLocation().openStream();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public CommonResourceLocation getResourceLocation() {
		return CommonResourceLocation.create(link);
	}

	public int getxHotSpot() {
		return xHotSpot;
	}

	public int getyHotSpot() {
		return yHotSpot;
	}

	public boolean isAllocate() {
		return cursor != MemoryUtil.NULL;
	}

	private BufferedImage readImage() {
		try (InputStream resource = getResource()) {
			if (resource == null) return null;
			BufferedImage originalImage = ImageIO.read(resource);
			if (originalImage == null) return null;

			int scaledHeight = (int) (originalImage.getHeight() * ((float) size / originalImage.getWidth()));
			BufferedImage scaledImage = new BufferedImage(size, scaledHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = scaledImage.createGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics.drawImage(originalImage, 0, 0, size, scaledHeight, null);
			graphics.dispose();
			return scaledImage;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setLink(String link) {
		if (cursor != MemoryUtil.NULL)
			allocate();
		this.link = link;
	}

	public void setxHotSpot(int xHotSpot) {
		// 根据当前大小调整热点位置
		float scale = (float)size / 32.0f;
		this.xHotSpot = (int)(xHotSpot * scale);
		if (cursor != MemoryUtil.NULL) {
			freeCursor();
			allocate();
		}
	}

	public void setyHotSpot(int yHotSpot) {
		// 根据当前大小调整热点位置
		float scale = (float)size / 32.0f;
		this.yHotSpot = (int)(yHotSpot * scale);
		if (cursor != MemoryUtil.NULL) {
			freeCursor();
			allocate();
		}
	}

	public CursorConfigStore write() {
		CursorConfigStore store = new CursorConfigStore(this);
		// 保存大小设置
		store.size = this.size;
		return store;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		// 确保大小在有效范围内
		int newSize = Math.max(1, Math.min(128, size));
		if (this.size != newSize) {
			this.size = newSize;
			// 重新创建光标
			if (cursor != MemoryUtil.NULL) {
				freeCursor();
				allocate();
			}
		}
	}
}
