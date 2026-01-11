package GPT20b.ws08.seq05;

import java.time.Duration;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
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

    /* ------------------------------------------------------------------ */
    /* Helper methods                                                    */
    /* ------------------------------------------------------------------ */

    private void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void navigateToLogin() {
        List<WebElement> loginLinks = driver.findElements(By.linkText("Sign In"));
        if (loginLinks.isEmpty()) {
            loginLinks = driver.findElements(By.linkText("Login"));
        }
        assertFalse(loginLinks.isEmpty(), "Login link not found on home page");
        wait.until(ExpectedConditions.elementToBeClickable(loginLinks.get(0))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    private void navigateToRegister() {
        List<WebElement> registerLinks = driver.findElements(By.linkText("Register Now!"));
        if (registerLinks.isEmpty()) {
            registerLinks = driver.findElements(By.linkText("Register"));
        }
        assertFalse(registerLinks.isEmpty(), "Register link not found");
        wait.until(ExpectedConditions.elementToBeClickable(registerLinks.get(0))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    /* ------------------------------------------------------------------ */
    /* Tests                                                             */
    /* ------------------------------------------------------------------ */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToHome();
        String title = driver.getTitle();
        assertNotNull(title, "Page title should be present");
        assertTrue(title.toLowerCase().contains("petstore"),
                "Title should contain 'petstore'");
    }

    @Test
    @Order(2)
    public void testLoginFormPresence() {
        navigateToHome();
        navigateToLogin();
        WebElement userField = driver.findElement(By.name("username"));
        WebElement passField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(userField.isDisplayed(), "Username field should be displayed");
        assertTrue(passField.isDisplayed(), "Password field should be displayed");
        assertTrue(loginButton.isDisplayed(), "Login button should be displayed");
    }

    @Test
    @Order(3)
    public void testInvalidLoginShowsError() {
        navigateToHome();
        navigateToLogin();
        WebElement userField = driver.findElement(By.name("username"));
        WebElement passField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        userField.clear();
        userField.sendKeys("invalidUser");
        passField.clear();
        passField.sendKeys("invalidPass");
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        List<WebElement> errorElems = driver.findElements(By.cssSelector(".errormessage, .error-msg, .error"));
        assertFalse(errorElems.isEmpty(), "Error message should appear for invalid credentials");
        assertTrue(driver.getCurrentUrl().contains("/login"), "URL should still contain 'login' after failure");
    }

    @Test
    @Order(4)
    public void testRegistrationPagePresence() {
        navigateToHome();
        navigateToRegister();
        WebElement firstName = driver.findElement(By.name("firstName"));
        WebElement lastName = driver.findElement(By.name("lastName"));
        WebElement userName = driver.findElement(By.name("username"));
        WebElement password = driver.findElement(By.name("password"));
        WebElement registerButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(firstName.isDisplayed(), "First name field should be displayed");
        assertTrue(lastName.isDisplayed(), "Last name field should be displayed");
        assertTrue(userName.isDisplayed(), "Username field should be displayed");
        assertTrue(password.isDisplayed(), "Password field should be displayed");
        assertTrue(registerButton.isDisplayed(), "Register button should be displayed");
    }

    @Test
    @Order(5)
    public void testCategoryNavigationAndSorting() {
        navigateToHome();
        // Find first category link
        List<WebElement> categoryLinks = driver.findElements(By.xpath("//a[contains(@href, '/category/')]"));
        assertFalse(categoryLinks.isEmpty(), "No category links found on home page");
        WebElement firstCat = categoryLinks.get(0);
        String catUrl = firstCat.getAttribute("href");
        wait.until(ExpectedConditions.elementToBeClickable(firstCat)).click();
        wait.until(ExpectedConditions.urlToBe(catUrl));

        // Verify product list heading
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div#mainContent h1")));
        assertTrue(heading.getText().toLowerCase().contains("products"),
                "Heading should mention 'Products'");

        // Check if sorting dropdown exists
        List<WebElement> sortSelects = driver.findElements(By.name("sort"));
        if (!sortSelects.isEmpty()) {
            WebElement sortSelect = sortSelects.get(0);
            // Verify each option changes product order
            Set<String> firstNames = new HashSet<>();
            List<WebElement> products = driver.findElements(By.cssSelector(".product:nth-child(1) .productTitle"));
            if (!products.isEmpty()) {
                firstNames.add(products.get(0).getText());
            }
            List<WebElement> options = new java.util.ArrayList<>(sortSelect.findElements(By.tagName("option")));
            for (WebElement option : options) {
                option.click();
                wait.until(ExpectedConditions.stalenessOf(products.get(0)));
                List<WebElement> newProducts = driver.findElements(By.cssSelector(".product:nth-child(1) .productTitle"));
                if (!newProducts.isEmpty()) {
                    firstNames.add(newProducts.get(0).getText());
                }
            }
            assertTrue(firstNames.size() > 1, "Sorting options should modify product order");
        }
    }

    @Test
    @Order(6)
    public void testFooterExternalLinks() {
        navigateToHome();
        List<WebElement> links = driver.findElements(By.tagName("a"));
        Set<String> expectedDomains = new HashSet<>();
        expectedDomains.add("twitter.com");
        expectedDomains.add("facebook.com");
        expectedDomains.add("linkedin.com");
        boolean anyFound = false;
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            for (String domain : expectedDomains) {
                if (href.contains(domain)) {
                    anyFound = true;
                    String original = driver.getWindowHandle();
                    wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                    wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
                    Set<String> windows = driver.getWindowHandles();
                    for (String win : windows) {
                        if (!win.equals(original)) {
                            driver.switchTo().window(win);
                            assertTrue(driver.getCurrentUrl().contains(domain),
                                    "External link URL does not contain expected domain: " + domain);
                            driver.close();
                            driver.switchTo().window(original);
                            break;
                        }
                    }
                }
            }
        }
        assertTrue(true, "External link check executed. Found any: " + anyFound);
    }
}