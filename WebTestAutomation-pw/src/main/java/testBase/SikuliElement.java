package testBase;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.Arrays;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Key;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

public class SikuliElement {

	private Screen sikuli;
	private String image;
	private String[] images;
	private float similarity0to100;
	private int x;
	private int y;
	private String imagePath;
	
	public SikuliElement(Screen sikuli, String imagePath, String image, String[] images, float similarity0to100, int x, int y) {
		this.sikuli = sikuli;
		this.imagePath = imagePath;
		this.image = image;
		this.images = images;
		this.similarity0to100 = similarity0to100;
		this.x = x;
		this.y = y;
	}

	public String getImage() { return image; }
	public String getImagePath() { return imagePath; }
	public String[] getImages() { return images; }
	public float getSimilarity0to100() { return similarity0to100; }
	public int getX() { return x; }
	public int getY() { return y; }
	public void setImage(String image) { this.image = image; }

	public int click() {
		try {
			return sikuli.click(createPattern(this));
		} catch (FindFailed e) {
			try {
				return sikuli.click(createNewPatternFromPath(this));
			} catch (FindFailed e1) {
				BaseClass.screenshot("Failed finding " + this.image);
				throw new RuntimeException(e1);
			}
		}
	}

	public int click(Integer modifiers) {
		try {
			return sikuli.click(createPattern(this), modifiers);
		} catch (FindFailed e) {
			try {
				return sikuli.click(createNewPatternFromPath(this), modifiers);
			} catch (FindFailed e1) {
				BaseClass.screenshot("Failed finding " + this.image);
				throw new RuntimeException(e1);
			}
		}
	}

	public int doubleClick() {
		try {
			return sikuli.doubleClick(createPattern(this));
		} catch (FindFailed e) {
			try {
				return sikuli.doubleClick(createNewPatternFromPath(this));
			} catch (FindFailed e1) {
				BaseClass.screenshot("Failed finding " + this.image);
				throw new RuntimeException(e1);
			}
		}
	}

	public int rightClick() {
		try {
			return sikuli.rightClick(createPattern(this));
		} catch (FindFailed e) {
			try {
				return sikuli.rightClick(createNewPatternFromPath(this));
			} catch (FindFailed e1) {
				BaseClass.screenshot("Failed finding " + this.image);
				throw new RuntimeException(e1);
			}
		}
	}

	public int dragDrop(SikuliElement sikuliElement) {
		try {
			return sikuli.dragDrop(createPattern(this), createPattern(sikuliElement));
		} catch (FindFailed e) {
			try {
				return sikuli.dragDrop(createNewPatternFromPath(this), createPattern(sikuliElement));
			} catch (FindFailed e1) {
				BaseClass.screenshot("Failed finding " + this.image);
				throw new RuntimeException(e1);
			}
		}
	}

	public int hover() {
		try {
			return sikuli.hover(createPattern(this));
		} catch (FindFailed e) {
			try {
				return sikuli.hover(createNewPatternFromPath(this));
			} catch (FindFailed e1) {
				BaseClass.screenshot("Failed finding " + this.image);
				throw new RuntimeException(e1);
			}
		}
	}

	public boolean exists() {
		return exists((int) sikuli.getAutoWaitTimeout());
	}

	public boolean exists(int timeoutInSeconds) {
		try {
			Pattern pattern = createPattern(this, timeoutInSeconds);
			Match imageMatch = sikuli.exists(pattern, timeoutInSeconds);
			return imageMatch != null;
		} catch (Exception e) {
			return false;
		}
	}

	public Match wait(int timeoutInSeconds) {
		try {
			return sikuli.wait(createPattern(this, timeoutInSeconds), timeoutInSeconds);
		} catch (FindFailed e) {
			try {
				return sikuli.wait(createNewPatternFromPath(this, timeoutInSeconds), timeoutInSeconds);
			} catch (FindFailed e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	public int type(String text) {
		try {
			clear();
			return sikuli.type(createPattern(this), text);
		} catch (FindFailed e) {
			throw new RuntimeException(e);
		}
	}
	

	public int type(String text, int modifiers) {
		try {
			return sikuli.type(createPattern(this), text, modifiers);
		} catch (FindFailed e) {
			try {
				return sikuli.type(createNewPatternFromPath(this), text, modifiers);
			} catch (FindFailed e1) {
				BaseClass.screenshot("Failed finding " + this.image);
				throw new RuntimeException(e1);
			}
		}
	}
	
	public int type(String text, String modifiers) {
		try {
			return sikuli.type(createPattern(this), text, modifiers);
		} catch (FindFailed e) {
			try {
				return sikuli.type(createNewPatternFromPath(this), text, modifiers);
			} catch (FindFailed e1) {
				BaseClass.screenshot("Failed finding " + this.image);
				throw new RuntimeException(e1);
			}
			
		}
	}


	public int typeWithTab(String text) {
		try {
			clear();
			int result = sikuli.type(createPattern(this), text);
			sikuli.type(Key.TAB);
			return result;
		} catch (FindFailed e) {
			try {
				return sikuli.type(createNewPatternFromPath(this), text);
			} catch (FindFailed e1) {
				BaseClass.screenshot("Failed finding " + this.image);
				throw new RuntimeException(e1);
			}
		}
	}

	public int paste(String text) {
		try {
			return sikuli.paste(createPattern(this), text);
		} catch (FindFailed e) {
			try {
				return sikuli.paste(createNewPatternFromPath(this), text);
			} catch (FindFailed e1) {
				BaseClass.screenshot("Failed finding " + this.image);
				throw new RuntimeException(e1);
			}
		}
	}

	public String getText() {
		clearClipboard();
		sikuli.type("a", Key.CTRL);
		sikuli.type("c", Key.CTRL);
		return getClipboard();
	}

	private int clear() {
		try {
			this.click();
			this.type("a", Key.CTRL);
			return this.type(Key.BACKSPACE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void clearClipboard() {
		StringSelection selection = new StringSelection("");
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}

	private String getClipboard() {
		try {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			return (String) clipboard.getData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void setImageIfUrlsIsSet(SikuliElement sikuliElement, int timeoutInSeconds) {
		if (sikuliElement.getImages().length > 1) {
			boolean imageFound = false;
			long timeoutExpiredMs = System.currentTimeMillis() + timeoutInSeconds * 1000;
			while (!imageFound) {
				for (String img : sikuliElement.getImages()) {
					imageFound = sikuli.exists(new Pattern(img).similar(sikuliElement.getSimilarity0to100() / 100), 0) != null;
					if (imageFound) {
						sikuliElement.setImage(img);
						return;
					}
				}
				if (System.currentTimeMillis() >= timeoutExpiredMs) break;
			}
			if (!imageFound) {
				throw new RuntimeException(new FindFailed("Images not found: " + Arrays.toString(sikuliElement.getImages())));
			}
		}
	}

	private Pattern createPattern(SikuliElement sikuliElement) {
		ThreadUtils.setIsScreen(true);
		setImageIfUrlsIsSet(sikuliElement, (int) sikuli.getAutoWaitTimeout());
		return new Pattern(sikuliElement.getImage())
			.similar(sikuliElement.getSimilarity0to100() / 100)
			.targetOffset(sikuliElement.getX(), sikuliElement.getY());
	}

	private Pattern createPattern(SikuliElement sikuliElement, int timeoutInSeconds) {
		setImageIfUrlsIsSet(sikuliElement, timeoutInSeconds);
		return new Pattern(sikuliElement.getImage())
			.similar(sikuliElement.getSimilarity0to100() / 100)
			.targetOffset(sikuliElement.getX(), sikuliElement.getY());
	}

	private Pattern createNewPatternFromPath(SikuliElement sikuliElement) {
		String path = new File(System.getProperty("user.dir") + sikuliElement.getImagePath() + sikuliElement.getImage()).getPath();
		return new Pattern(path)
			.similar(sikuliElement.getSimilarity0to100() / 100)
			.targetOffset(sikuliElement.getX(), sikuliElement.getY());
	}

	private Pattern createNewPatternFromPath(SikuliElement sikuliElement, int timeoutInSeconds) {
		setImageIfUrlsIsSet(sikuliElement, timeoutInSeconds);
		String path = new File(System.getProperty("user.dir") + sikuliElement.getImagePath() + sikuliElement.getImage()).getPath();
		return new Pattern(path)
			.similar(sikuliElement.getSimilarity0to100() / 100)
			.targetOffset(sikuliElement.getX(), sikuliElement.getY());
	}
}