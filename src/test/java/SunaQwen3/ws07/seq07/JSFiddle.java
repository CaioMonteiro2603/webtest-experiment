package deepseek.ws07.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsFiddleTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement editorWrapper = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editor-wrapper")));
        Assertions.assertTrue(editorWrapper.isDisplayed(), "Editor wrapper should be visible");
    }

    @Test
    @Order(2)
    public void testBasicFiddleExecution() {
        driver.get(BASE_URL);
        
        WebElement htmlInput = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#editor-html textarea")));
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".button.button-green.run")));
        
        htmlInput.clear();
        htmlInput.sendKeys("<h1>Hello World</h1>");
        runButton.click();
        
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#result")));
        driver.switchTo().frame(resultFrame);
        
        WebElement h1 = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.tagName("h1")));
        Assertions.assertEquals("Hello World", h1.getText(), "Output should match input HTML");
        
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(3)
    public void testDarkThemeSwitch() {
        driver.get(BASE_URL);
        WebElement themeButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".theme-chooser")));
        themeButton.click();
        
        WebElement darkThemeOption = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//li[contains(text(),'Dark')]")));
        darkThemeOption.click();
        
        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.tagName("body")));
        String themeClass = body.getAttribute("class");
        Assertions.assertTrue(themeClass.contains("dark"), "Dark theme should be applied");
    }

    @Test
    @Order(4)
    public void testNewFiddleCreation() {
        driver.get(BASE_URL);
        WebElement newButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".new")));
        newButton.click();
        
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".new-green")));
        confirmButton.click();
        
        WebElement htmlInput = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#editor-html textarea")));
        Assertions.assertEquals("", htmlInput.getText(), "Editor should be cleared for new fiddle");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[title='Twitter']")));
        twitterLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testEditorLayoutSelection() {
        driver.get(BASE_URL);
        WebElement layoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".layout-chooser")));
        layoutButton.click();
        
        WebElement bottomLayoutOption = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//li[contains(text(),'Bottom results')]")));
        bottomLayoutOption.click();
        
        WebElement resultContainer = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".vertical #result-container")));
        Assertions.assertTrue(resultContainer.isDisplayed(), "Bottom layout should be visible");
    }
}