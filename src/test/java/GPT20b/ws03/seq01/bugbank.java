package GPT20b.ws03.seq01;

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

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class BugbankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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

    /*--------------------------- Helper methods ---------------------------*/

    private void goToBase() {
        driver.get(BASE_URL);
    }

    private void login(String user, String pass) {
        goToBase();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(user);
        driver.findElement(By.name("password")).sendKeys(pass);
        By loginBtn = By.xpath("//button[contains(text(),'Log In') or @type='submit' or @name='login']"); 
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    private void waitForVisibility(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void waitForClickability(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private void resetAppState() {
        By burger = By.cssSelector("[data-testid='menu-toggle']");
        waitForClickability(burger);
        driver.findElement(burger).click();

        By resetLink = By.xpath("//a[contains(text(),'Reset App State')]");
        waitForClickability(resetLink);
        driver.findElement(resetLink).click();

        driver.findElement(burger).click(); // close the menu
        waitForVisibility(By.cssSelector(".inventory-item"));
    }

    /*--------------------------- Tests ---------------------------*/

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        By inventoryHeading = By.xpath("//*[contains(text(),'Inventory')]");
        waitForVisibility(inventoryHeading);
        Assertions.assertTrue(
                driver.findElement(inventoryHeading).isDisplayed(),
                "Inventory heading should be visible after successful login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("wrong@example.com", "wrongpass");
        By errorMsg = By.xpath("//*[contains(text(),'Login failed') or contains(text(),'invalid')]"); 
        waitForVisibility(errorMsg);
        Assertions.assertTrue(
                driver.findElement(errorMsg).isDisplayed(),
                "Error message should appear for invalid credentials.");
    }

    @Test
    @Order(3)
    public void testLogout() {
        login(USERNAME, PASSWORD);
        By logoutBtn = By.xpath("//button[contains(text(),'Log Out') or @name='logout']");
        waitForClickability(logoutBtn);
        driver.findElement(logoutBtn).click();
        By loginForm = By.xpath("//form[@id='login-form'] | //form[@class='login']");
        waitForVisibility(loginForm);
        Assertions.assertTrue(
                driver.findElement(loginForm).isDisplayed(),
                "Login form should be visible after logout.");
    }

    @Test
    @Order(4)
    public void testBurgerMenuOptions() {
        login(USERNAME, PASSWORD);
        By burger = By.cssSelector("[data-testid='menu-toggle']");
        waitForClickability(burger);
        driver.findElement(burger).click();

        // All Items
        By allItems = By.xpath("//a[contains(text(),'All Items') or contains(@href,'/inventory')]");
        waitForClickability(allItems);
        driver.findElement(allItems).click();
        By inventoryPage = By.cssSelector(".inventory-item");
        waitForVisibility(inventoryPage);
        Assertions.assertTrue(
                driver.findElements(inventoryPage).size() > 0,
                "Inventory page should display items.");

        // Reopen menu for next actions
        driver.findElement(burger).click();

        // About external link
        By aboutLink = By.xpath("//a[contains(text(),'About')]");
        waitForClickability(aboutLink);
        String originalWindow = driver.getWindowHandle();
        driver.findElement(aboutLink).click();

        wait.until(d -> d.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("about"),
                "About link should navigate to external about page.");
        driver.close();
        driver.switchTo().window(originalWindow);
        driver.findElement(burger).click();

        // Reset App State
        By resetOption = By.xpath("//a[contains(text(),'Reset App State')]");
        waitForClickability(resetOption);
        driver.findElement(resetOption).click();
        driver.findElement(burger).click();
        waitForVisibility(inventoryPage);
        Assertions.assertTrue(
                driver.findElements(inventoryPage).size() > 0,
                "Reset should return to inventory page.");

        // Logout
        By logoutOption = By.xpath("//a[contains(text(),'Log Out')]");
        waitForClickability(logoutOption);
        driver.findElement(logoutOption).click();
        By loginForm = By.xpath("//form[@id='login-form'] | //form[@class='login']");
        waitForVisibility(loginForm);
        Assertions.assertTrue(
                driver.findElement(loginForm).isDisplayed(),
                "Login form visible after logout via menu.");
    }

    @Test
    @Order(5)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        By sortSelect = By.cssSelector("select#sort-menu");
        if (driver.findElements(sortSelect).isEmpty()) {
            sortSelect = By.cssSelector("select.sort-select");
        }
        waitForClickability(sortSelect);
        WebElement select = driver.findElement(sortSelect);

        // Capture original first item
        By firstItem = By.cssSelector(".inventory-item .item-name");
        waitForVisibility(firstItem);
        String originalFirst = driver.findElement(firstItem).getText();

        // Sort by price low to high
        select.findElement(By.xpath("//option[contains(text(),'Price Low to High')]")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(firstItem, originalFirst));
        Assertions.assertTrue(
                !driver.findElement(firstItem).getText().equals(originalFirst),
                "Sorting should change the first item after choosing price low to high.");

        // Sort by price high to low
        select.findElement(By.xpath("//option[contains(text(),'Price High to Low')]")).click();
        Assertions.assertNotEquals(
                driver.findElement(firstItem).getText(),
                originalFirst,
                "Sorting by price high to low should alter order.");

        // Sort by name A to Z
        select.findElement(By.xpath("//option[contains(text(),'Name A to Z')]")).click();
        Assertions.assertNotEquals(
                driver.findElement(firstItem).getText(),
                originalFirst,
                "Sorting by name A to Z should change order.");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        goToBase();
        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : domains) {
            By link = By.xpath(String.format("//a[contains(@href,'%s')]", domain));
            List<WebElement> links = driver.findElements(link);
            Assertions.assertFalse(
                    links.isEmpty(),
                    "Footer should contain link to %s", domain);
            WebElement social = links.get(0);
            String original = driver.getWindowHandle();
            social.click();

            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> win = driver.getWindowHandles();
            win.remove(original);
            String newWin = win.iterator().next();
            driver.switchTo().window(newWin);
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains(domain),
                    "Opened link should contain domain %s", domain);
            driver.close();
            driver.switchTo().window(original);
        }
    }

    @Test
    @Order(7)
    public void testAddRemoveCart() {
        login(USERNAME, PASSWORD);
        By addButtons = By.cssSelector(".add-to-cart");
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(addButtons));
        WebElement firstAdd = driver.findElements(addButtons).get(0);
        firstAdd.click();
        By cartBadge = By.cssSelector(".cart-count");
        waitUntilVisible(cartBadge);
        Assertions.assertEquals(
                "1",
                driver.findElement(cartBadge).getText(),
                "Cart count should be 1 after adding an item.");

        // Remove the item
        By removeBtn = By.xpath("//button[contains(text(),'Remove')]");
        waitForClickability(removeBtn);
        driver.findElement(removeBtn).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(cartBadge));
        Assertions.assertTrue(
                driver.findElements(cartBadge).isEmpty(),
                "Cart badge should disappear after removing item.");
    }

    @Test
    @Order(8)
    public void testCheckoutProcess() {
        login(USERNAME, PASSWORD);
        // Add two items
        By addButtons = By.cssSelector(".add-to-cart");
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(addButtons));
        driver.findElements(addButtons).stream().limit(2).forEach(WebElement::click);

        // Go to cart
        By cartIcon = By.cssSelector("[data-testid='cart-icon']");
        waitForClickability(cartIcon);
        driver.findElement(cartIcon).click();
        By checkoutBtn = By.xpath("//button[contains(text(),'Checkout')]");
        waitForClickability(checkoutBtn);
        driver.findElement(checkoutBtn).click();

        // Fill info
        waitForVisibility(By.name("firstName"));
        driver.findElement(By.name("firstName")).sendKeys("Test");
        driver.findElement(By.name("lastName")).sendKeys("User");
        driver.findElement(By.name("postalCode")).sendKeys("12345");

        By continueBtn = By.xpath("//button[contains(text(),'Continue')]");
        waitForClickability(continueBtn);
        driver.findElement(continueBtn).click();

        // Finish
        By finishBtn = By.xpath("//button[contains(text(),'Finish')]");
        waitForClickability(finishBtn);
        driver.findElement(finishBtn).click();

        // Verify success
        By thanksMsg = By.xpath("//*[contains(text(),'Thank You for Your Order')]");
        waitForVisibility(thanksMsg);
        Assertions.assertTrue(
                driver.findElement(thanksMsg).isDisplayed(),
                "Success message should be displayed after checkout.");
        // Reset state
        resetAppState();
    }

    /*--------------------------- Miscellaneous ---------------------------*/

    private void waitUntilVisible(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}