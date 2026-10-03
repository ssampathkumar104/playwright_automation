package testBase.listners;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ImageLister {

	public static List<String> getImageList(String folderPath) {
		File folder = new File(folderPath);

		// Filter for image files only
		FilenameFilter imageFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				return lowercaseName.endsWith(".jpg") || lowercaseName.endsWith(".jpeg")
						|| lowercaseName.endsWith(".png");
			}
		};

		// Get list of image files
		File[] imageFiles = folder.listFiles(imageFilter);

		// Check for empty folder
		if (imageFiles == null || imageFiles.length == 0) {
			return new ArrayList<>();
		}

		// Sort files by last modified time
		Arrays.sort(imageFiles, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return Long.compare(f1.lastModified(), f2.lastModified());
			}
		});

		// Extract image paths
		List<String> imagePaths = new ArrayList<>();
		for (File file : imageFiles) {
			imagePaths.add(file.getAbsolutePath());
		}

		return imagePaths;
	}

}
