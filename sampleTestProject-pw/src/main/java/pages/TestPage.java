package pages;


import pageFactory.DynamicWebElementLocator;
import pageFactory.FindBy;
import pageFactory.WebElementLocator;
import testBase.BasePageObject;

public class TestPage extends BasePageObject<TestPage> {

	@FindBy(using = "//textarea[@name='q']")
	public WebElementLocator googleInput;
	
	@FindBy(using = "//textarea[@name='%s']")
	public DynamicWebElementLocator googleInputDynamic;

//	@IFrames({
//        @IFrame(xpath="//iframe[contains(@name,'LN_')]" , frameType=FrameType.IFRAME  , attributes={"class=m-app-frame", "name=LN_4bf34403-4d45-419d-868c-988e5cffe2b5", "src=https://eln-lnui-usea1prda.eln.inforcloudsuite.com:443/webui/servlet/fslogin?commonui=true&LogicalId=lid%3A%2F%2Finfor.ln.ln01&HybridCertified=1&OnPremCertified=1&LogicalId=lid%3A%2F%2Finfor.ln.ln01&inforThemeName=Light&inforCurrentLocale=en-US&inforCurrentLanguage=en-US&infor10WorkspaceShell=1&inforWorkspaceVersion=12.0.33&inforStyle=3.0&inforTimeZone=(UTC%2B02%3A00)%20Jerusalem&inforStdTimeZone=Asia%2FJerusalem", "onload=onAppFrameLoad(this)", "onerror=onAppFrameError(e);", "allow=geolocation; microphone; camera", }),
//    })
	@FindBy(using = "//span[text()='Next']/..")
	public WebElementLocator menuIcon2;

}
