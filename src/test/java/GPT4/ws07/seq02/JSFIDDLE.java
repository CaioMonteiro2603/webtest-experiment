package GPT4.ws07.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddleTest {

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        Assertions.assertTrue(driver.getTitle().contains("JSFiddle"), "Home page title does not contain JSFiddle");
    }

    @Test
    @Order(2)
    public void testCreateNewFiddleButton() {
        driver.get(BASE_URL);
        WebElement newFiddleButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/']")));
        Assertions.assertTrue(newFiddleButton.isDisplayed(), "New fiddle button is not displayed");
    }

    @Test
    @Order(3)
    public void testDocumentationLink() {
        driver.get(BASE_URL);
        WebElement docLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://docs.jsfiddle.net/']")));
        String originalWindow = driver.getWindowHandle();
        docLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String window : windows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("docs.jsfiddle.net"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("docs.jsfiddle.net"), "Documentation page did not open correctly");
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(4)
    public void testTwitterLinkInFooter() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com/jsfiddle']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String window : windows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("twitter.com"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link did not open correctly");
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testExploreLink() {
        driver.get(BASE_URL);
        WebElement exploreLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/explore']")));
        exploreLink.click();
        wait.until(ExpectedConditions.urlContains("/explore"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/explore"), "Explore page did not load");
    }

    @Test
    @Order(6)
    public void testSignupPageLoads() {
        driver.get(BASE_URL);
        WebElement signupLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/signup']")));
        signupLink.click();
        wait.until(ExpectedConditions.urlContains("/signup"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/signup"), "Signup page did not load");
    }

    @Test
    @Order(7)
    public void testLoginPageLoads() {
        driver.get(BASE_URL);
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/login']")));
        loginLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Login page did not load");
    }

    @Test
    @Order(8)
    public void testForkWithoutLoginShowsPrompt() {
        driver.get(BASE_URL);
        WebElement forkButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("fork")));
        forkButton.click();
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-dialog")));
        Assertions.assertTrue(modal.isDisplayed(), "Login modal not shown when trying to fork without login");
    }

    @Test
    @Order(9)
    public void testEditorPanelsPresent() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("panel_html")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("panel_css")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("panel_js")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("panel_result")));

        Assertions.assertAll("Editor panels should be present",
                () -> Assertions.assertTrue(driver.findElement(By.id("panel_html")).isDisplayed(), "HTML panel not displayed"),
                () -> Assertions.assertTrue(driver.findElement(By.id("panel_css")).isDisplayed(), "CSS panel not displayed"),
                () -> Assertions.assertTrue(driver.findElement(By.id("panel_js")).isDisplayed(), "JS panel not displayed"),
                () -> Assertions.assertTrue(driver.findElement(By.id("panel_result")).isDisplayed(), "Result panel not displayed")
        );
    }

    @Test
    @Order(10)
    public void testChangeFrameworkVersionDropdown() {
        driver.get(BASE_URL);
        WebElement settingsButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".setting-button")));
        settingsButton.click();

        WebElement libraryDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("select#library")));
        List<WebElement> options = libraryDropdown.findElements(By.tagName("option"));
        Assertions.assertTrue(options.size() > 1, "Library dropdown does not have multiple options");
    }
}
