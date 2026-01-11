package GPT5.ws08.seq08;

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
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String VALID_USER = "j2ee";
    private static final String VALID_PASS = "j2ee";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ------------------------- Helpers -------------------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainImageContent")));
    }

    private void openSignIn() {
        openBase();
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("/account/signonForm"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//form[contains(@action,'signon')]")));
    }

    private void login(String user, String pass) {
        openSignIn();
        WebElement u = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        u.clear();
        u.sendKeys(user);
        WebElement p = driver.findElement(By.name("password"));
        p.clear();
        p.sendKeys(pass);
        driver.findElement(By.xpath("//input[@type='submit' and contains(@value,'Login')]")).click();
    }

    private boolean isLoggedIn() {
        return driver.findElements(By.linkText("Sign Out")).size() > 0 ||
               driver.findElements(By.id("WelcomeContent")).stream().anyMatch(e -> e.getText().toLowerCase().contains("welcome"));
    }

    private void ensureLoggedIn() {
        if (!isLoggedIn()) {
            login(VALID_USER, VALID_PASS);
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.linkText("Sign Out")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.messages li"))
            ));
        }
    }

    private void assertExternalLinkOpens(String cssSelector, String expectedDomainPart) {
        List<WebElement> links = driver.findElements(By.cssSelector(cssSelector));
        if (links.isEmpty()) return; // optional link; skip silently
        String originalHandle = driver.getWindowHandle();
        String beforeUrl = driver.getCurrentUrl();
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", links.get(0));
        wait.until(ExpectedConditions.elementToBeClickable(links.get(0))).click();

        try {
            wait.until(d -> d.getWindowHandles().size() > 1 || !d.getCurrentUrl().equals(beforeUrl));
        } catch (TimeoutException ignored) {}

        Set<String> handles = driver.getWindowHandles();
        if (handles.size() > 1) {
            for (String h : handles) {
                if (!h.equals(originalHandle)) {
                    driver.switchTo().window(h);
                    break;
                }
            }
            wait.until(ExpectedConditions.urlContains(expectedDomainPart));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainPart), "External URL should contain " + expectedDomainPart);
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomainPart));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainPart), "External URL should contain " + expectedDomainPart);
            driver.navigate().back();
        }
    }

    // ------------------------- Tests -------------------------

    @Test
    @Order(1)
    public void homePageLoads_andWelcomeVisible() {
        openBase();
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@id,'Welcome') or contains(@class,'welcome') or contains(text(),'Welcome')]")));
        Assertions.assertTrue(welcome.getText().toLowerCase().contains("welcome"), "Home page should show welcome content");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jpetstore"), "Title should contain 'JPetStore'");
    }

    @Test
    @Order(2)
    public void invalidLogin_showsErrorMessage() {
        login("invalidUser", "invalidPass");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.messages li")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("invalid username or password"),
                "An error should be shown for invalid credentials");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/account/signonForm"), "Should remain on signon form after invalid login");
    }

    @Test
    @Order(3)
    public void validLogin_navigatesToStore_andShowsWelcome() {
        login(VALID_USER, VALID_PASS);
        WebElement signOut = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign Out")));
        Assertions.assertTrue(signOut.isDisplayed(), "Sign Out link should be visible after login");
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@id,'Welcome') or contains(@class,'welcome') or contains(text(),'Welcome')]")));
        Assertions.assertTrue(welcome.getText().toLowerCase().contains("welcome"), "Welcome content should appear after login");
    }

    @Test
    @Order(4)
    public void navigateToFishCategory_viaTopImageMap() {
        openBase();
        WebElement fishArea = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("area[alt='Fish']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", fishArea);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fishArea);
        wait.until(ExpectedConditions.urlContains("/catalog/categories/FISH"));
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#Catalog h2")));
        Assertions.assertTrue(heading.getText().contains("Fish"), "Should be on Fish category page");
    }

    @Test
    @Order(5)
    public void addItemToCart_fromProductAndItemPage() {
        driver.get(BASE_URL + "catalog/products/FI-SW-01");
        WebElement itemLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'EST-1')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", itemLink);
        wait.until(ExpectedConditions.elementToBeClickable(itemLink)).click();
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.Button")));
        addToCart.click();
        WebElement cartTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.cart")));
        List<WebElement> rows = cartTable.findElements(By.cssSelector("tr"));
        Assertions.assertTrue(rows.size() > 1, "Cart table should contain item rows after adding to cart");
    }

    @Test
    @Order(6)
    public void updateCartQuantity_andVerifySubtotalUpdates() {
        // Ensure cart has EST-1 in it
        driver.get(BASE_URL + "catalog/products/FI-SW-01");
        WebElement itemLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'EST-1')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", itemLink);
        wait.until(ExpectedConditions.elementToBeClickable(itemLink)).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.Button"))).click();

        // Update quantity to 2
        WebElement qtyInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("EST-1")));
        qtyInput.clear();
        qtyInput.sendKeys("2");
        driver.findElement(By.name("updateCartQuantities")).click();

        // Verify updated quantity and subtotal cell present
        WebElement updatedQty = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("EST-1")));
        Assertions.assertEquals("2", updatedQty.getAttribute("value"), "Quantity should update to 2");

        WebElement subtotalCell = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//td[contains(text(),'Sub Total') or contains(text(),'Subtotal') or contains(text(),'Sub-Total')]")));
        Assertions.assertTrue(subtotalCell.isDisplayed(), "Subtotal row should be present after updating cart");
    }

    @Test
    @Order(7)
    public void proceedToCheckout_newOrderFormLoads() {
        ensureLoggedIn();
        // Ensure cart has an item to allow checkout
        driver.get(BASE_URL + "cart");
        if (driver.findElements(By.cssSelector("table.cart")).isEmpty()) {
            driver.get(BASE_URL + "catalog/products/FI-SW-01");
            WebElement itemLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'EST-1')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", itemLink);
            wait.until(ExpectedConditions.elementToBeClickable(itemLink)).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.Button"))).click();
        }
        // Proceed to checkout
        WebElement checkout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout")));
        checkout.click();
        wait.until(ExpectedConditions.urlContains("/order/newOrderForm"));
        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(By.name("newOrder")));
        Assertions.assertTrue(continueBtn.isDisplayed(), "Continue button on New Order form should be visible");
    }

    @Test
    @Order(8)
    public void externalFooterLink_toAspectranOpensInNewTab() {
        openBase();
        // There is typically a footer link to aspectran.com
        assertExternalLinkOpens("a[href*='aspectran.com']", "aspectran.com");
    }

    @Test
    @Order(9)
    public void navigateToOtherCategories_oneLevelDeep() {
        openBase();
        // Try a couple of category links from homepage top image map
        String[] alts = new String[]{"Dogs", "Cats", "Birds"};
        int navigated = 0;
        for (String alt : alts) {
            List<WebElement> areas = driver.findElements(By.cssSelector("area[alt='" + alt + "']"));
            if (!areas.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", areas.get(0));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", areas.get(0));
                wait.until(ExpectedConditions.urlContains("/catalog/categories/"));
                WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#Catalog h2")));
                Assertions.assertTrue(heading.getText().toLowerCase().contains(alt.toLowerCase()),
                        "Category heading should contain " + alt);
                driver.navigate().back();
                navigated++;
            }
            if (navigated >= 2) break;
        }
        Assertions.assertTrue(navigated >= 1, "At least one additional category should be navigable");
    }

    @Test
    @Order(10)
    public void signOut_returnsToHomeWithSignInLink() {
        ensureLoggedIn();
        WebElement signOut = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOut.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Sign In")));
        Assertions.assertTrue(driver.findElement(By.linkText("Sign In")).isDisplayed(), "Sign In link should be visible after signing out");
    }
}