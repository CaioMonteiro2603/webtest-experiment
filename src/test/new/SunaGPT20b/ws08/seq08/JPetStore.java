package SunaGPT20b.ws08.seq08;

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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String VALID_USERNAME = "j2ee";
    private static final String VALID_PASSWORD = "j2ee";

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

    /** Helper: navigate to base URL and wait for page load */
    private void goToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    /** Helper: perform login using valid credentials */
    private void login() {
        goToHome();
        // Click Sign In link if present
        List<WebElement> signInLinks = driver.findElements(By.linkText("Sign In"));
        if (!signInLinks.isEmpty()) {
            signInLinks.get(0).click();
        }
        // Wait for login form
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.xpath("//input[@type='submit' and contains(@value,'Login')]"));
        username.clear();
        username.sendKeys(VALID_USERNAME);
        password.clear();
        password.sendKeys(VALID_PASSWORD);
        loginBtn.click();
        // Verify login success by checking presence of logout link
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Sign Out")));
    }

    /** Helper: reset application state via menu */
    private void resetAppState() {
        // Open menu if burger button exists
        List<WebElement> menuButtons = driver.findElements(By.cssSelector("button[id*='menu'], button[class*='menu']"));
        if (!menuButtons.isEmpty()) {
            menuButtons.get(0).click();
        }
        // Click Reset App State link
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            // Wait for page to settle
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        Assertions.assertTrue(driver.findElements(By.linkText("Sign Out")).size() > 0,
                "Sign Out link should be present after successful login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        goToHome();
        // Navigate to login page
        List<WebElement> signInLinks = driver.findElements(By.linkText("Sign In"));
        if (!signInLinks.isEmpty()) {
            signInLinks.get(0).click();
        }
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.xpath("//input[@type='submit' and contains(@value,'Login')]"));
        username.clear();
        username.sendKeys("invalidUser");
        password.clear();
        password.sendKeys("invalidPass");
        loginBtn.click();
        // Expect error message
        WebElement errorMsg = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'Invalid')]")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        // Navigate to a category page (e.g., Fish)
        driver.findElement(By.linkText("Fish")).click();
        wait.until(ExpectedConditions.titleContains("Fish"));
        // Locate sorting dropdown (assume name='sort')
        WebElement sortSelectElem = wait.until(ExpectedConditions.elementToBeClickable(By.name("sort")));
        Select sortSelect = new Select(sortSelectElem);
        List<WebElement> options = sortSelect.getOptions();
        Assertions.assertTrue(options.size() > 1, "Sorting dropdown should have multiple options");
        // Iterate through options and verify that the first item changes
        String previousFirstItem = "";
        for (WebElement option : options) {
            sortSelect.selectByVisibleText(option.getText());
            // Wait for page to refresh after sorting
            wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.cssSelector(".product-list"))));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-list")));
            List<WebElement> items = driver.findElements(By.cssSelector(".product-list .product-name"));
            Assertions.assertFalse(items.isEmpty(), "Product list should not be empty after sorting");
            String currentFirstItem = items.get(0).getText();
            Assertions.assertNotEquals(previousFirstItem, currentFirstItem,
                    "First product should change when sorting option changes");
            previousFirstItem = currentFirstItem;
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login();
        // Open burger/menu button if present
        List<WebElement> menuButtons = driver.findElements(By.cssSelector("button[id*='menu'], button[class*='menu']"));
        if (!menuButtons.isEmpty()) {
            menuButtons.get(0).click();
        }
        // Click All Items (assume link text)
        List<WebElement> allItemsLinks = driver.findElements(By.linkText("All Items"));
        if (!allItemsLinks.isEmpty()) {
            allItemsLinks.get(0).click();
            wait.until(ExpectedConditions.titleContains("All Items"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog"), "URL should contain /catalog after All Items");
        }
        // Click About (external)
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        if (!aboutLinks.isEmpty()) {
            String originalWindow = driver.getWindowHandle();
            aboutLinks.get(0).click();
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.titleIs(driver.getTitle()));
            Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com"),
                    "External About page should contain aspectran.com");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        // Click Logout
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Sign Out"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Sign In")));
            Assertions.assertTrue(driver.findElements(By.linkText("Sign In")).size() > 0,
                    "Sign In link should be visible after logout");
        }
        // Re-login for further tests
        login();
        resetAppState();
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();
        // Scroll to footer
        WebElement footer = driver.findElement(By.tagName("footer"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", footer);
        // Define expected domains
        String[][] socialLinks = {
                {"Twitter", "twitter.com"},
                {"Facebook", "facebook.com"},
                {"LinkedIn", "linkedin.com"}
        };
        for (String[] linkInfo : socialLinks) {
            String linkText = linkInfo[0];
            String expectedDomain = linkInfo[1];
            List<WebElement> links = driver.findElements(By.xpath("//footer//a[contains(@href,'" + expectedDomain + "')]"));
            if (links.isEmpty()) {
                continue; // Skip if not present
            }
            String originalWindow = driver.getWindowHandle();
            links.get(0).click();
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.titleIs(driver.getTitle()));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    linkText + " link should open a page containing " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppState();
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login();
        // Navigate to a product page (Fish -> Angelfish)
        driver.findElement(By.linkText("Fish")).click();
        wait.until(ExpectedConditions.titleContains("Fish"));
        driver.findElement(By.linkText("Angelfish")).click();
        wait.until(ExpectedConditions.titleContains("Angelfish"));
        // Add to cart
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'addItemToCart')]")));
        addToCartBtn.click();
        // Verify cart badge count
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Cart')]")));
        Assertions.assertTrue(cartBadge.getText().contains("1"), "Cart should show 1 item");
        // Go to cart
        driver.findElement(By.linkText("Cart")).click();
        wait.until(ExpectedConditions.titleContains("Cart"));
        // Proceed to checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.titleContains("Checkout"));
        // Fill out checkout form (use dummy data)
        driver.findElement(By.name("firstName")).sendKeys("John");
        driver.findElement(By.name("lastName")).sendKeys("Doe");
        driver.findElement(By.name("address")).sendKeys("123 Main St");
        driver.findElement(By.name("city")).sendKeys("Anytown");
        driver.findElement(By.name("state")).sendKeys("CA");
        driver.findElement(By.name("zip")).sendKeys("12345");
        driver.findElement(By.name("country")).sendKeys("USA");
        driver.findElement(By.name("continue")).click();
        // Confirm order
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(By.name("confirm")));
        confirmBtn.click();
        // Verify success message
        WebElement successMsg = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'successful')]")));
        Assertions.assertTrue(successMsg.isDisplayed(), "Order success message should be displayed");
        resetAppState();
    }
}