package GPT4.ws10.seq02;

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
public class BrasilAgriTestSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String EMAIL = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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

    private void performLogin(String email, String password) {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        loginButton.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        performLogin(EMAIL, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "User should be redirected to dashboard after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        performLogin("invalid@example.com", "wrongpassword");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("invalid-feedback")));
        Assertions.assertTrue(error.isDisplayed(), "Invalid login should show an error message");
    }

    @Test
    @Order(3)
    public void testSidebarNavigationLinks() {
        performLogin(EMAIL, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        List<WebElement> navLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("aside a.nav-link")));
        for (WebElement link : navLinks) {
            String href = link.getAttribute("href");
            if (href != null && href.contains("brasilagritest.com")) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                wait.until(ExpectedConditions.urlContains("/"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("/"), "Navigation link should load page: " + href);
            }
        }
    }

    @Test
    @Order(4)
    public void testLogoutFunctionality() {
        performLogin(EMAIL, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='logout']")));
        logoutBtn.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Logout should redirect to login page");
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> socialLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("footer a[target='_blank']")));
        String originalWindow = driver.getWindowHandle();

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href != null && (href.contains("twitter.com") || href.contains("facebook.com") || href.contains("linkedin.com"))) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
                link.click();
                wait.until(driver -> driver.getWindowHandles().size() > 1);

                Set<String> windows = driver.getWindowHandles();
                for (String win : windows) {
                    if (!win.equals(originalWindow)) {
                        driver.switchTo().window(win);
                        wait.until(ExpectedConditions.urlContains(href.split("//")[1].split("/")[0]));
                        Assertions.assertTrue(driver.getCurrentUrl().contains(href.split("//")[1].split("/")[0]),
                                "Should open external link: " + href);
                        driver.close();
                        break;
                    }
                }
                driver.switchTo().window(originalWindow);
            }
        }
    }
}
