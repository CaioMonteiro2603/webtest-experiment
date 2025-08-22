package GPT4.ws09.seq01;

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
public class RealWorldTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

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

    private void switchToNewTabAndVerify(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected external domain not found: " + expectedDomain);
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
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("banner")));
        Assertions.assertTrue(banner.isDisplayed(), "Banner should be visible on homepage.");
        Assertions.assertTrue(driver.getCurrentUrl().contains("realworld.io"), "Homepage URL should be correct.");
    }

    @Test
    @Order(2)
    public void testExternalGitHubLink() {
        driver.get(BASE_URL);
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github.com']")));
        String originalWindow = driver.getWindowHandle();
        githubLink.click();
        switchToNewTabAndVerify("github.com");
        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Should return to original window.");
    }

    @Test
    @Order(3)
    public void testNavigateToLoginPage() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='#login']")));
        signInLink.click();
        WebElement signInHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Sign In']")));
        Assertions.assertTrue(signInHeader.isDisplayed(), "Sign In page should be displayed.");
        Assertions.assertTrue(driver.getCurrentUrl().contains("#login"), "URL should contain #login.");
    }

    @Test
    @Order(4)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='#login']")));
        signInLink.click();

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.sendKeys("invalid@example.com");
        passwordInput.sendKeys("wrongpassword");
        signInButton.click();

        WebElement errorList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages li")));
        Assertions.assertTrue(errorList.getText().toLowerCase().contains("email or password"),
                "Expected invalid credentials error message.");
    }

    @Test
    @Order(5)
    public void testFeedTabVisibility() {
        driver.get(BASE_URL);
        WebElement globalFeedTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Global Feed')]")));
        globalFeedTab.click();
        WebElement articlePreview = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("article-preview")));
        Assertions.assertTrue(articlePreview.isDisplayed(), "Article preview should be visible in global feed.");
    }

    @Test
    @Order(6)
    public void testClickTagFromSidebar() {
        driver.get(BASE_URL);
        WebElement tag = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tag-list a")));
        String tagText = tag.getText();
        tag.click();
        WebElement activeTag = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".feed-toggle .nav-link.active")));
        Assertions.assertEquals(tagText, activeTag.getText(), "Selected tag should be active.");
    }
}
