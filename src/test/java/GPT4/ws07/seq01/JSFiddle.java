package GPT4.ws07.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

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

    private void switchToNewTabAndVerify(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        for (String window : allWindows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected domain not found in URL: " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.header-bar")));
        Assertions.assertTrue(header.isDisplayed(), "Header bar should be displayed on homepage.");
    }

    @Test
    @Order(2)
    public void testStartFiddleButton() {
        driver.get(BASE_URL);
        WebElement startBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/']")));
        startBtn.click();
        wait.until(ExpectedConditions.urlContains("/"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "URL should start with BASE_URL after starting a fiddle.");
    }

    @Test
    @Order(3)
    public void testExploreLinkNavigation() {
        driver.get(BASE_URL);
        WebElement exploreLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/explore']")));
        exploreLink.click();
        wait.until(ExpectedConditions.urlContains("/explore"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/explore"), "Explore page URL should contain /explore.");
    }

    @Test
    @Order(4)
    public void testLoginLinkNavigation() {
        driver.get(BASE_URL);
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/auth/login/']")));
        loginLink.click();
        wait.until(ExpectedConditions.urlContains("/auth/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/auth/login"), "Login page URL should contain /auth/login.");
    }

    @Test
    @Order(5)
    public void testRegisterLinkNavigation() {
        driver.get(BASE_URL);
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/auth/signup/']")));
        registerLink.click();
        wait.until(ExpectedConditions.urlContains("/auth/signup"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/auth/signup"), "Signup page URL should contain /auth/signup.");
    }

    @Test
    @Order(6)
    public void testGitHubFooterLink() {
        driver.get(BASE_URL);
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github.com']")));
        String originalWindow = driver.getWindowHandle();
        githubLink.click();
        switchToNewTabAndVerify("github.com");
        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Should return to original window.");
    }

    @Test
    @Order(7)
    public void testTwitterFooterLink() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();
        switchToNewTabAndVerify("twitter.com");
        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Should return to original window.");
    }

    @Test
    @Order(8)
    public void testDocumentationExternalLink() {
        driver.get(BASE_URL);
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://docs.jsfiddle.net/']")));
        String originalWindow = driver.getWindowHandle();
        docsLink.click();
        switchToNewTabAndVerify("docs.jsfiddle.net");
        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Should return to original window.");
    }
}
