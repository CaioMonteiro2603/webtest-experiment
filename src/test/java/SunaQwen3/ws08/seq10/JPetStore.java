package SunaQwen3.ws08.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL);
        assertEquals("JPetStore Demo", driver.getTitle(), "Page title should be 'JPetStore Demo'");

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Enter the Store")));
        signInLink.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));

        WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInButton.click();

        wait.until(ExpectedConditions.urlContains("/actions/Account.action?viewSignonForm="));

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should be redirected to catalog after login");
        assertTrue(driver.getPageSource().contains("Fish"), "Catalog should contain 'Fish' category");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?viewSignonForm=");
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("messages")));
        String errorMessage = driver.findElement(By.className("messages")).getText();
        assertTrue(errorMessage.contains("Invalid username or password"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testBrowseCategoriesAndProducts() {
        driver.get(BASE_URL + "actions/Catalog.action");

        List<WebElement> categoryLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.category a")));
        assertFalse(categoryLinks.isEmpty(), "At least one category should be present");

        for (WebElement link : categoryLinks) {
            String categoryName = link.getText();
            link.click();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h2")));
            assertTrue(driver.getPageSource().contains(categoryName), "Page should contain category name: " + categoryName);

            List<WebElement> itemLinks = driver.findElements(By.cssSelector("a[href*='viewProduct']"));
            if (!itemLinks.isEmpty()) {
                itemLinks.get(0).click();

                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("th")));
                assertTrue(driver.getPageSource().contains("Add to Cart"), "Product page should have 'Add to Cart' button");

                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.category a")));
            }

            driver.navigate().to(BASE_URL + "actions/Catalog.action");
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.category a")));
        }
    }

    @Test
    @Order(4)
    void testAddToCartAndCheckout() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        fishLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("FI-SW-01")));
        WebElement productLink = driver.findElement(By.linkText("FI-SW-01"));
        productLink.click();

        wait.until(ExpectedConditions.elementToBeClickable(By.name("EST-6")));
        WebElement addToCart = driver.findElement(By.name("EST-6"));
        addToCart.click();

        wait.until(ExpectedConditions.elementToBeClickable(By.name("updateCartQuantities")));
        WebElement quantityInput = driver.findElement(By.name("EST-6"));
        quantityInput.clear();
        quantityInput.sendKeys("2");

        WebElement updateButton = driver.findElement(By.name("updateCartQuantities"));
        updateButton.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("tr.cart"), "2"));

        WebElement proceedToCheckout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout")));
        proceedToCheckout.click();

        wait.until(ExpectedConditions.urlContains("viewOrder"));
        assertTrue(driver.getPageSource().contains("Confirm"), "Should be on confirmation page");
        assertTrue(driver.getPageSource().contains("Total: $"), "Total amount should be displayed");
    }

    @Test
    @Order(5)
    void testMenuNavigation() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[src='/images/menu.gif']")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getPageSource().contains("Fish"), "All Items should show catalog");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[src='/images/menu.gif']")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("aspectran"), "About link should open aspectran domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[src='/images/menu.gif']")));
        menuButton.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetLink.click();

        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h2")));
        assertTrue(driver.getPageSource().contains("Fish"), "App state should be reset and catalog visible");
    }

    @Test
    @Order(6)
    void testFooterSocialLinks() {
        driver.get(BASE_URL + "actions/Catalog.action");

        List<WebElement> socialLinks = driver.findElements(By.cssSelector("div.footer a"));
        assertFalse(socialLinks.isEmpty(), "Footer should contain social links");

        String originalWindow = driver.getWindowHandle();

        for (WebElement link : socialLinks) {
            String linkText = link.getText().toLowerCase();

            link.click();

            wait.until(d -> d.getWindowHandles().size() > 1);
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            String expectedDomain;
            if (linkText.contains("twitter") || linkText.contains("x")) {
                expectedDomain = "twitter.com";
            } else if (linkText.contains("facebook")) {
                expectedDomain = "facebook.com";
            } else if (linkText.contains("linkedin")) {
                expectedDomain = "linkedin.com";
            } else {
                expectedDomain = "aspectran.com";
            }

            assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Social link should navigate to expected domain: " + expectedDomain);

            driver.close();
            driver.switchTo().window(originalWindow);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.footer a")));
        }
    }

    @Test
    @Order(7)
    void testLogout() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[src='/images/menu.gif']")));
        menuButton.click();

        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOutLink.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getPageSource().contains("You have logged out."), "Logout message should be displayed");
        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should return to catalog after logout");
    }
}