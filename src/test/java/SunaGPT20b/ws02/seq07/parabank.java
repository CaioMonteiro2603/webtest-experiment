package SunaGPT20b.ws02.seq07;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        createUser(driver);
    }

    private static void createUser(WebDriver driver) {
        driver.get("https://parabank.parasoft.com/parabank/register.htm");
        driver.findElement(By.id("customer.firstName")).click();
        driver.findElement(By.id("customer.firstName")).sendKeys("a");
        driver.findElement(By.id("customer.lastName")).click();
        driver.findElement(By.id("customer.lastName")).sendKeys("a");
        driver.findElement(By.id("customer.address.street")).click();
        driver.findElement(By.id("customer.address.street")).sendKeys("a");
        driver.findElement(By.id("customer.address.city")).click();
        driver.findElement(By.id("customer.address.city")).sendKeys("a");
        driver.findElement(By.id("customer.address.state")).click();
        driver.findElement(By.id("customer.address.state")).sendKeys("a");
        driver.findElement(By.id("customer.address.zipCode")).click();
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("a");
        driver.findElement(By.id("customer.phoneNumber")).click();
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("a");
        driver.findElement(By.id("customer.ssn")).click();
        driver.findElement(By.id("customer.ssn")).sendKeys("a");
        driver.findElement(By.id("customer.username")).click();
        driver.findElement(By.id("customer.username")).sendKeys("caio@gmail.com");
        driver.findElement(By.id("customer.password")).sendKeys("123");
        driver.findElement(By.id("repeatedPassword")).sendKeys("123");
        driver.findElement(By.cssSelector("td > .button")).click();
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("username")));
        userField.clear();
        userField.sendKeys(user);

        WebElement passField = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("password")));
        passField.clear();
        passField.sendKeys(pass);

        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']")));
        loginBtn.click();
    }

    private void logoutIfLoggedIn() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Log Out"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.titleContains("ParaBank"));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        // Verify successful login by checking URL and presence of logout link
        wait.until(ExpectedConditions.urlContains("/parabank/overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/overview.htm"),
                "After login, URL should contain '/parabank/overview.htm'");
        WebElement logoutLink = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.linkText("Log Out")));
        Assertions.assertTrue(logoutLink.isDisplayed(), "Logout link should be displayed after successful login");
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_pass");
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.error")));
        Assertions.assertTrue(errorMsg.getText().contains("The username and password could not be verified"),
                "Error message should indicate invalid credentials");
        logoutIfLoggedIn(); // Ensure clean state
    }

    @Test
    @Order(3)
    public void testNavigateToAccountsOverview() {
        login(USERNAME, PASSWORD);
        WebElement overviewLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/parabank/overview.htm' and contains(text(),'Overview')]")));
        overviewLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/overview.htm"),
                "URL should contain '/parabank/overview.htm' after navigating to Accounts Overview");
        logoutIfLoggedIn();
    }

    @Test
    @Order(4)
    public void testNavigateToTransferFunds() {
        login(USERNAME, PASSWORD);
        WebElement transferLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/parabank/transfer.htm' and contains(text(),'Transfer')]")));
        transferLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/transfer.htm"),
                "URL should contain '/parabank/transfer.htm' after navigating to Transfer Funds");
        logoutIfLoggedIn();
    }

    @Test
    @Order(5)
    public void testNavigateToBillPay() {
        login(USERNAME, PASSWORD);
        WebElement billPayLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/parabank/billpay.htm' and contains(text(),'Bill')]")));
        billPayLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/billpay.htm"),
                "URL should contain '/parabank/billpay.htm' after navigating to Bill Pay");
        logoutIfLoggedIn();
    }

    @Test
    @Order(6)
    public void testExternalFooterLinks() {
        login(USERNAME, PASSWORD);
        // Locate footer links that open in a new tab/window (target="_blank")
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("footer a[target='_blank']"));
        Assertions.assertTrue(!externalLinks.isEmpty(), "There should be external links in the footer");

        String originalWindow = driver.getWindowHandle();
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            Assertions.assertNotNull(href, "External link should have href attribute");
            // Click the link
            link.click();

            // Wait for new window
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            // Verify the URL contains the expected domain (extract domain from href)
            String expectedDomain = href.replaceFirst("https?://([^/]+).*", "$1");
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External page URL should contain expected domain: " + expectedDomain);

            // Close the new window and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        logoutIfLoggedIn();
    }

    @Test
    @Order(7)
    public void testLogoutFunctionality() {
        login(USERNAME, PASSWORD);
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/parabank/logout.htm' and contains(text(),'Log Out')]")));
        logoutLink.click();
        // After logout, the login button should be visible again
        WebElement loginBtn = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[value='Log In']")));
        Assertions.assertTrue(loginBtn.isDisplayed(), "Login button should be displayed after logout");
    }
}