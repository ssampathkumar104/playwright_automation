package testBase.listners;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.playwright.Locator;

import testBase.ArtefactBuilder;

public class EventHandler extends ArtefactBuilder{
	
	public void afterNavigate(String url) {
		// Actions after navigation
	}
	
	public void beforeClick(Locator locator) {
		String stmt = "Click highlighted";
		String innerText = "";
		try {
			innerText = locator.innerText().trim();
			if (StringUtils.isBlank(innerText)) {
				innerText = locator.textContent().trim();
			}
		} catch (Exception e) {
			// InnerText is empty
		}
		
		String tagName = "";
		try {
			tagName = locator.evaluate("el => el.tagName").toString().toLowerCase();
		} catch (Exception e) {
			// TagName not available
		}
		
		if (StringUtils.isNoneBlank(innerText)) {
			stmt = "Click '" + innerText + "'";
		}
		
		if ("button".equalsIgnoreCase(tagName)) {
			if (StringUtils.isNoneBlank(innerText)) {
				stmt = "Click '" + innerText + "' button";
			}
			try {
				String title = locator.getAttribute("title");
				if (StringUtils.isNoneBlank(title)) {
					stmt = "Click '" + title + "' button";
				}
			} catch (Exception e) {
				// Title not available
			}
		}
		
		boolean isLink = false;
		try {
			String href = locator.getAttribute("href");
			isLink = StringUtils.isNotBlank(href) && !href.equals("javascript:void(0)");
		} catch (Exception e) {
			// Href not available
		}
		
		if ("a".equalsIgnoreCase(tagName) && isLink) {
			stmt = stmt + " link";
		}
		
		takeArtefact(stmt, locator);
	}

	public void afterFill(Locator locator, String value) {
		String s = null;
		int lenOfPassword = 0;

		try {
			String type = locator.getAttribute("type");
			if (type != null && type.trim().equalsIgnoreCase("password")) {
				s = "password";
				lenOfPassword = value.length();
			}
		} catch (Exception e) {
			// Type not available
		}

		if (Objects.isNull(s)) {
			takeArtefact("Enter '" + value + "' in highlighted field", locator);
		} else if ("password".equalsIgnoreCase(s)) {
			takeArtefact("Enter '" + StringUtils.repeat("*", lenOfPassword) + "' in highlighted field", locator);
		}
	}

	public void afterGetText(Locator locator, String result) {
		// ArtefactBuilder.takeArtefact("Get/Save value from the highlighted field", locator);
	}

	public void afterGetAttribute(Locator locator, String name, String result) {
		// if (name.equalsIgnoreCase("value"))
		//     ArtefactBuilder.takeArtefact("Get/Save " + name + " from the highlighted field", locator);
	}
	
	public void beforeClear(Locator locator) {
		takeArtefact("Clear the text from the highlighted field", locator);
	}
}