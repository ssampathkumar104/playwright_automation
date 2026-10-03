package testBase;

import java.io.File;

import org.sikuli.script.ImagePath;
import org.sikuli.script.Screen;

import pageFactory.desktop.FindByImageResourceLocation;
import pageFactory.desktop.SikuliFactory;

public class BaseDesktopPage<B extends BaseDesktopPage<B>> {

	private Screen sikuli;
	
	public BaseDesktopPage() {
		this.sikuli = ThreadUtils.getScreenRef();
		String imgPath = this.getClass().getDeclaredAnnotation(FindByImageResourceLocation.class).value();
		String path = (System.getProperty("user.dir") + imgPath).replace("\\", File.separator);
		if (path.endsWith(File.separator)) {
			path = path.substring(0, path.length() - 1);
		}
		ImagePath.add(path.replace("\\", File.separator));
		SikuliFactory.initElements(sikuli, this);
	}
}