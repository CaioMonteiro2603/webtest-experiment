package GPT20b.ws06.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class AutomationTestingOnlineTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";

    /* --------------------------------------------------------------------- */
    /*  Test Setup & Teardown                                               */
    /* --------------------------------------------------------------------- */

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

    /* --------------------------------------------------------------------- */
    /*  Helper Methods                                                      */
    /* --------------------------------------------------------------------- */

    private void goTo(String url) {
        driver.get(url);
    }

    private void login(String user, String pass) {
        goTo(BASE_URL);
        // Locate username and password fields in a flexible manner
        List<WebElement> userInputs = driver.findElements(
                By.cssSelector("input[type='text'], input[name*='user'], input[id*='user']"));
        List<WebElement> passInputs = driver.findElements(
                By.cssSelector("input[type='password'], input[name*='pass'], input[id*='pass']"));
        Assertions.assertFalse(userInputs.isEmpty(), "Username input not found");
        Assertions.assertFalse(passInputs.isEmpty(), "Password input not found");
        userInputs.get(0).clear();
        userInputs.get(0).sendKeys(user);
        passInputs.get(0).clear();
        passInputs.get(0).sendKeys(pass);

        // Find login button
        List<WebElement> loginBtns = driver.findElements(
                By.xpath("//button[normalize-space()='Login' or contains(@type,'submit') or contains(@id,'login')]"));
        Assertions.assertFalse(loginBtns.isEmpty(), "Login button not found");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtns.get(0))).click();
    }

    private void logoutIfLoggedIn() {
        List<WebElement> logoutLink = driver.findElements(
                By.xpath("//a[normalize-space()='Logout' or contains(@href,'logout')]"));
        if (!logoutLink.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLink.get(0))).click();
        }
    }

    private String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    private void resetAppStateIfPossible() {
        List<WebElement> resetLink = driver.findElements(
                By.xpath("//a[normalize-space()='Reset App State' or contains(@href,'reset')]"));
        if (!resetLink.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resetLink.get(0))).click();
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Tests                                                              */
    /* --------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        goTo(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Automation Testing"));
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("automation"),
                "Home page title should contain 'automation'.");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(getCurrentUrl().contains("/dashboard"),
                "After login the URL should contain '/dashboard'.");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        login("wronguser", "wrongpass");
        List<WebElement> error = driver.findElements(
                By.xpath("//*[contains(text(),'Login failed') or contains(@class,'error')]"));
        Assertions.assertFalse(error.isEmpty(), "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(4)
    public void testSortingDropdownOptions() {
        login(USERNAME, PASSWORD);

        By sortLocator = By.cssSelector("select.sort-options, select[name='sort']");

        wait.until(ExpectedConditions.visibilityOfElementLocated(sortLocator));
        WebElement sortElement = driver.findElement(sortLocator);

        List<WebElement> options = sortElement.findElements(By.tagName("option"));
        Assertions.assertTrue(options.size() > 0, "Sorting dropdown should have at least one option");

        String firstOption = options.get(0).getText();
        List<String> optionTexts = new ArrayList<>();
        for (WebElement opt : options) {
            optionTexts.add(opt.getText());
        }

        for (String optText : optionTexts) {
            if (!optText.equals(firstOption)) {
                sortElement.findElement(By.xpath(String.format("./option[normalize-space()='%s']",
                        optText))).click();
                // Basic validation: the first visible item name should change
                By firstItem = By.cssSelector(".product-title, .item-title, .name");
                wait.until(ExpectedConditions.visibilityOfElementLocated(firstItem));
                String currentFirst = driver.findElement(firstItem).getText();
                Assertions.assertNotEquals(firstOption, currentFirst,
                        "After selecting '" + optText + "' the first item should change.");
            }
        }
    }

    @Test
    @Order(5)
    public void testBurgerMenuActions() {
        login(USERNAME, PASSWORD);

        By burgerBtn = By.cssSelector("[data-testid='menu-toggle'], .burger-menu, .menuBtn");
        wait.until(ExpectedConditions.elementToBeClickable(burgerBtn));
        driver.findElement(burgerBtn).click();

        // All Items / Shop
        List<WebElement> allItemsLinks = driver.findElements(
                By.xpath("//a[normalize-space()='All Items' or contains(@href,'inventory') or contains(@href,'shop')]"));
        if (!allItemsLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(allItemsLinks.get(0))).click();
            wait.until(ExpectedConditions.urlContains("/inventory") || ExpectedConditions.urlContains("/shop"));
            Assertions.assertTrue(getCurrentUrl().contains("/inventory") || getCurrentUrl().contains("/shop"),
                    "Clicking All Items should navigate to inventory/shop page.");
        }

        // Reopen menu for next actions
        driver.findElement(burgerBtn).click();

        // About (external)
        List<WebElement> aboutLinks = driver.findElements(
                By.xpath("//a[normalize-space()='About' or contains(@href,'about')]"));
        if (!aboutLinks.isEmpty()) {
            String original = getCurrentUrl();
            aboutLinks.get(0).click();
            wait.until(driver1 -> {
                String url = driver1.getCurrentUrl();
                return !url.equals(original);
            });
            Assertions.assertTrue(getCurrentUrl().contains("about") || getCurrentUrl().contains("katalon") || getCurrentUrl().contains("github"),
                    "About link should navigate to an external page.");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(original));
        }

        // Reopen menu for logout
        driver.findElement(burgerBtn).click();

        // Logout
        List<WebElement> logoutLinks = driver.findElements(
                By.xpath("//a[normalize-space()='Logout' or contains(@href,'logout')]"));
        if (!logoutLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLinks.get(0))).click();
            wait.until(ExpectedConditions.urlContains("/login") || ExpectedConditions.urlContains("/sign-in"));
            Assertions.assertTrue(getCurrentUrl().contains("/login") || getCurrentUrl().contains("/sign-in"),
                    "After logout, URL should point to login page.");
        }

        // Re-login for reset test
        login(USERNAME, PASSWORD);

        // Reopen menu for reset
        driver.findElement(burgerBtn).click();

        // Reset App State
        List<WebElement> resetLinks = driver.findElements(
                By.xpath("//a[normalize-space()='Reset App State' or contains(@href,'reset')]"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            // Expectation: stays on inventory page
            wait.until(ExpectedConditions.urlContains("/inventory") || ExpectedConditions.urlContains("/shop"));
            Assertions.assertTrue(getCurrentUrl().contains("/inventory") || getCurrentUrl().contains("/shop"),
                    "After reset, user should be on inventory page.");
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        goTo(BASE_URL);
        List<String> domains = List.of("twitter.com", "facebook.com", "linkedin.com");
        String originalHandle = driver.getWindowHandle();

        for (String domain : domains) {
            List<WebElement> links = driver.findElements(
                    By.xpath("//a[contains(@href,'" + domain + "')]"));
            if (!links.isEmpty()) {
                links.get(0).click();

                // Wait for either a new tab or URL change
                wait.until(driver1 -> {
                    Set<String> handles = driver1.getWindowHandles();
                    return handles.size() > 1 || !driver1.getCurrentUrl().equals(BASE_URL);
                });

                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > 1) {
                    handles.remove(originalHandle);
                    String newHandle = handles.iterator().next();
                    driver.switchTo().window(newHandle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                            "External link should load domain " + domain);
                    driver.close();
                    driver.switchTo().window(originalHandle);
                } else {
                    Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                            "External link should load domain " + domain);
                    driver.navigate().back();
                    wait.until(ExpectedConditions.urlToBe(BASE_URL));
                }
            }
        }
    }

    @Test
    @Order(7)
    public void testAddRemoveCartBadge() {
        login(USERNAME, PASSWORD);

        By addToCartBtns = By.cssSelector(".add-to-cart, button:contains('Add to Cart')");

        List<WebElement> addBtns = driver.findElements(addToCartBtns);
        Assertions.assertFalse(addBtns.isEmpty(), "No add-to-cart buttons found");

        addBtns.get(0).click();

        By cartBadge = By.cssSelector(".cart-count, .cart-badge");
        wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge));
        Assertions.assertEquals("1", driver.findElement(cartBadge).getText(),
                "Cart badge should show 1 after adding an item.");

        // Remove item
        By removeBtn = By.xpath("//button[normalize-space()='Remove' or contains(@class,'remove')]");
        wait.until(ExpectedConditions.elementToBeClickable(removeBtn));
        driver.findElement(removeBtn).click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(cartBadge));
        Assertions.assertTrue(driver.findElements(cartBadge).isEmpty(),
                "Cart badge should disappear after removing item.");
    }

    @Test
    @Order(8)
    public void testCheckoutProcess() {
        login(USERNAME, PASSWORD);

        // Add two items
        List<WebElement> addBtns = driver.findElements(
                By.cssSelector(".add-to-cart, button:contains('Add to Cart')"));
        Assertions.assertTrue(addBtns.size() >= 2, "Not enough items to add to cart");
        addBtns.get(0).click();
        addBtns.get(1).click();

        // Go to cart
        By cartIcon = By.cssSelector(".cart-icon, .cart-link");
        wait.until(ExpectedConditions.elementToBeClickable(cartIcon));
        driver.findElement(cartIcon).click();

        // Proceed to checkout
        By checkoutBtn = By.xpath("//button[normalize-space()='Checkout' or contains(@class,'checkout')]");
        wait.until(ExpectedConditions.elementToBeClickable(checkoutBtn));
        driver.findElement(checkoutBtn).click();

        // Fill checkout info
        By firstName = By.cssSelector("input[name='firstName'], input#first-name");
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstName));
        driver.findElement(firstName).sendKeys("Test");
        driver.findElement(By.cssSelector("input[name='lastName'], input#last-name")).sendKeys("User");
        driver.findElement(By.cssSelector("input[name='postalCode'], input#postal-code")).sendKeys("12345");

        // Continue
        By continueBtn = By.xpath("//button[normalize-space()='Continue' or contains(@class,'continue')]");
        wait.until(ExpectedConditions.elementToBeClickable(continueBtn));
        driver.findElement(continueBtn).click();

        // Finish
        By finishBtn = By.xpath("//button[normalize-space()='Finish' or contains(@class,'finish')]");
        wait.until(ExpectedConditions.elementToBeClickable(finishBtn));
        driver.findElement(finishBtn).click();

        // Verify success
        By thanksMsg = By.xpath("//*[contains(text(),'Thank You') or contains(text(),'Order Complete')]"); 
        wait.until(ExpectedConditions.visibilityOfElementLocated(thanksMsg));
        Assertions.assertTrue(driver.findElement(thanksMsg).isDisplayed(),
                "Checkout completion message should be visible.");
    }
}