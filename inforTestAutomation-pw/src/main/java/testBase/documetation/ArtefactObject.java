package testBase.documetation;

import java.io.File;

import lombok.Data;

/**
*
* ArtefactObject class defining ArtefactBuiler objects.
*/
@Data
public class ArtefactObject {
	private String desc;                // Description of the artefact
    private File elementImg;            // Image file of the web element
    private File screenImg;             // Image file of the screen
    private String screenImgPath;       // File path of the screen image

    /**
     * Constructor for ArtefactObject.
     *
     * @param txt            the description of the artefact
     * @param elementImg     the image file of the web element
     * @param screenImg      the image file of the screen
     * @param screenImgPath  the file path of the screen image
     */
	public ArtefactObject(String txt, File elementImg, File screenImg, String screenImgPath) {
		this.desc = txt;
		this.elementImg = elementImg;
		this.screenImg = screenImg;
		this.screenImgPath = screenImgPath;
	}
}
