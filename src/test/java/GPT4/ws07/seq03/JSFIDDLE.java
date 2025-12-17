package GPT4.ws07.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFIDDLE {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    public void goHome() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("jsfiddle"), "Home page title does not contain 'JSFiddle'");
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/']")));
        Assertions.assertTrue(logo.isDisplayed(), "Home logo is not displayed.");
    }

    @Test
    @Order(2)
    public void testExploreLink() {
        WebElement explore = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/explore']")));
        explore.click();
        wait.until(ExpectedConditions.urlContains("/explore"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/explore"), "Explore page URL is incorrect.");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(header.getText().toLowerCase().contains("explore"), "Explore page header is missing.");
    }

    @Test
    @Order(3)
    public void testDocsLink() {
        WebElement docs = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://docs.jsfiddle.net/']")));
        String originalWindow = driver.getWindowHandle();
        docs.sendKeys(Keys.chord(Keys.CONTROL, Keys.RETURN));
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("docs.jsfiddle.net"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("docs.jsfiddle.net"), "Docs external link failed.");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(4)
    public void testGitHubLink() {
        WebElement github = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://github.com/jsfiddle/jsfiddle-code']")));
        String originalWindow = driver.getWindowHandle();
        github.sendKeys(Keys.chord(Keys.CONTROL, Keys.RETURN));
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "GitHub external link failed.");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testTwitterLink() {
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://twitter.com/jsfiddle']")));
        String originalWindow = driver.getWindowHandle();
        twitter.sendKeys(Keys.chord(Keys.CONTROL, Keys.RETURN));
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter external link failed.");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testTryEditorWithoutLogin() {
        WebElement htmlPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".panel-html")));
        Assertions.assertTrue(htmlPanel.isDisplayed(), "HTML panel not displayed in editor.");
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("run")));
        Assertions.assertTrue(runButton.isDisplayed(), "Run button not found.");
    }
}
