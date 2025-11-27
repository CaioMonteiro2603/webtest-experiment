package SunaGPT20b.ws02.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class ParabankTestSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Log In']")));

        usernameField.clear();
        usernameField.sendKeys(user);
        passwordField.clear();
        passwordField.sendKeys(pass);
        loginButton.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"),
                "After login the URL should contain 'overview.htm'.");

        // Verify presence of a stable element on the overview page
        WebElement accountsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Accounts Overview')]")));
        Assertions.assertTrue(accountsHeader.isDisplayed(),
                "Accounts Overview header should be displayed after successful login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Log In']")));

        usernameField.clear();
        usernameField.sendKeys("invalid@example.com");
        passwordField.clear();
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'The username and password could not be verified.')]")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials.");
    }

    @Test
    @Order(3)
    public void testLogout() {
        // Ensure we are logged in first
        login(USERNAME, PASSWORD);
        // Click the logout link (it is a link with text 'Log Out')
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "After logout the user should be redirected to the login page.");
    }

    @Test
    @Order(4)
    public void testInternalNavigationLinks() {
        login(USERNAME, PASSWORD);
        // Collect all internal links on the current page (excluding external)
        List<WebElement> linkElements = driver.findElements(By.xpath("//a[@href]"));
        List<String> internalHrefs = linkElements.stream()
                .map(e -> e.getAttribute("href"))
                .filter(href -> href != null && href.startsWith("https://parabank.parasoft.com/parabank/"))
                .distinct()
                .collect(Collectors.toList());

        for (String href : internalHrefs) {
            driver.navigate().to(href);
            // Verify that the URL contains the expected path
            Assertions.assertTrue(driver.getCurrentUrl().contains(href.replace(BASE_URL, "")),
                    "Navigated URL should contain the expected path: " + href);
            // Simple sanity check: page should have a body element
            WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Assertions.assertNotNull(body, "Body element should be present on page: " + href);
        }
    }

    @Test
    @Order(5)
    public void testExternalFooterLinks() {
        login(USERNAME, PASSWORD);
        // Footer social links (Twitter, Facebook, LinkedIn) are identified by their href domains
        List<WebElement> footerLinks = driver.findElements(By.xpath("//footer//a[@href]"));
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href == null) continue;
            // Consider external if domain is not parabank.parasoft.com
            if (!href.contains("parabank.parasoft.com")) {
                String originalWindow = driver.getWindowHandle();
                Set<String> existingWindows = driver.getWindowHandles();

                // Click the link (may open new tab/window)
                link.click();

                // Wait for new window
                wait.until(driver -> driver.getWindowHandles().size() > existingWindows.size());

                Set<String> newWindows = driver.getWindowHandles();
                newWindows.removeAll(existingWindows);
                String newWindowHandle = newWindows.iterator().next();

                driver.switchTo().window(newWindowHandle);
                // Verify the URL contains the expected external domain
                Assertions.assertTrue(driver.getCurrentUrl().contains(new java.net.URL(href).getHost()),
                        "External link should navigate to domain: " + new java.net.URL(href).getHost());

                // Close the external tab and switch back
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }
}