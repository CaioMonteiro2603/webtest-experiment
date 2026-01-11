package SunaGPT20b.ws02.seq06;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

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

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']")));

        userField.clear();
        userField.sendKeys(user);
        passField.clear();
        passField.sendKeys(pass);
        loginBtn.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        // After successful login the URL should contain "overview.htm"
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("overview.htm"),
                "Expected URL to contain 'overview.htm' after login, but was: " + currentUrl);
        // Verify that the account overview table is displayed
        WebElement overviewTable = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("accountOverviewTable")));
        Assertions.assertTrue(overviewTable.isDisplayed(),
                "Account overview table should be visible after successful login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login(USERNAME, "wrongPassword");
        // Expect an error message element with class 'error'
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials.");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid login.");
    }

    @Test
    @Order(3)
    public void testNavigateToAboutPage() {
        login(USERNAME, PASSWORD);
        // Find the "About" link in the navigation bar
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();
        wait.until(ExpectedConditions.urlContains("about.htm"));
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("about.htm"),
                "URL should contain 'about.htm' after navigating to About page.");
        // Verify page header
        WebElement header = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("About", header.getText(),
                "About page should have header 'About'.");
    }

    @Test
    @Order(4)
    public void testNavigateToContactPage() {
        login(USERNAME, PASSWORD);
        WebElement contactLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();
        wait.until(ExpectedConditions.urlContains("contact.htm"));
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("contact.htm"),
                "URL should contain 'contact.htm' after navigating to Contact page.");
        WebElement header = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("Contact", header.getText(),
                "Contact page should have header 'Contact'.");
    }

    @Test
    @Order(5)
    public void testExternalSocialLinks() {
        login(USERNAME, PASSWORD);
        // Footer social links (Twitter, Facebook, LinkedIn)
        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : domains) {
            List<WebElement> links = driver.findElements(By.cssSelector("footer a[href*='" + domain + "']"));
            if (links.isEmpty()) {
                continue; // Skip if the link is not present
            }
            WebElement link = links.get(0);
            String originalWindow = driver.getWindowHandle();
            Set<String> existingWindows = driver.getWindowHandles();

            link.click();

            // Wait for new window/tab
            wait.until(driver -> driver.getWindowHandles().size() > existingWindows.size());
            Set<String> newWindows = driver.getWindowHandles();
            newWindows.removeAll(existingWindows);
            Iterator<String> iterator = newWindows.iterator();
            String newWindow = iterator.next();

            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.urlContains(domain));
            String externalUrl = driver.getCurrentUrl();
            Assertions.assertTrue(externalUrl.contains(domain),
                    "External link should navigate to a URL containing '" + domain + "'. Actual: " + externalUrl);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(6)
    public void testLogout() {
        login(USERNAME, PASSWORD);
        // Logout link is typically in the top navigation bar
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.endsWith("index.htm"),
                "After logout, URL should end with 'index.htm'. Actual: " + url);
        // Verify that login form is displayed again
        WebElement loginBtn = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[value='Log In']")));
        Assertions.assertTrue(loginBtn.isDisplayed(),
                "Login button should be visible after logout.");
    }
}