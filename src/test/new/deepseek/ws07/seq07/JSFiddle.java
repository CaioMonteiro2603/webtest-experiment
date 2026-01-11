package deepseek.ws07.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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
        WebElement mainLayout = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.className("main-layout")));
        WebElement panelsContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".panels")));
        Assertions.assertTrue(panelsContainer.isDisplayed(), "Panels container should be visible");
    }

    @Test
    @Order(2)
    public void testBasicFiddleExecution() {
        driver.get(BASE_URL);
        
        WebElement htmlEditor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".panel-html .CodeMirror")));
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.run")));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].CodeMirror.setValue('<h1>Hello World</h1>');", htmlEditor);
        runButton.click();
        
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#result iframe")));
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
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.js-menu")));
        menuButton.click();
        
        WebElement settingsButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Settings')]")));
        settingsButton.click();
        
        WebElement darkThemeOption = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='dark']")));
        darkThemeOption.click();
        
        WebElement applyButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.apply")));
        applyButton.click();
        
        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.tagName("body")));
        String themeClass = body.getAttribute("class");
        Assertions.assertTrue(themeClass.contains("dark"), "Dark theme should be applied");
    }

    @Test
    @Order(4)
    public void testNewFiddleCreation() {
        driver.get(BASE_URL);
        WebElement createNewButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.js-new")));
        createNewButton.click();
        
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.confirm")));
        confirmButton.click();
        
        WebElement htmlEditor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".panel-html .CodeMirror")));
        String htmlContent = (String)((JavascriptExecutor) driver).executeScript(
            "return arguments[0].CodeMirror.getValue();", htmlEditor);
        Assertions.assertTrue(htmlContent.isEmpty() || htmlContent.trim().isEmpty(), 
            "Editor should be cleared for new fiddle");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement footer = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("footer")));
        WebElement twitterLink = footer.findElement(
            By.xpath(".//a[contains(@href, 'twitter') or contains(text(),'Twitter')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", twitterLink);
        
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
            By.cssSelector("button.js-layout")));
        layoutButton.click();
        
        WebElement bottomLayoutOption = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Bottom')]")));
        bottomLayoutOption.click();
        
        WebElement layoutContainer = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#panel-content-result")));
        Assertions.assertTrue(layoutContainer.isDisplayed(), "Bottom layout should be visible");
    }
}