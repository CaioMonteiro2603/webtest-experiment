package GPT20b.ws10.seq09;

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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    @BeforeAll
    public static void setUpDriver() {
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

    /* ---------- Helper Methods ---------- */

    private void performLogin(String user, String pass) {
        driver.get(BASE_URL);
        By emailField = By.cssSelector("input[name='email'], input#email");
        By passField = By.cssSelector("input[name='password'], input#password");
        By loginBtn = By.cssSelector("button[type='submit'], button#login");

        try {
            wait.until(ExpectedConditions.elementToBeClickable(emailField)).clear();
            driver.findElement(emailField).sendKeys(user);
            driver.findElement(passField).clear();
            driver.findElement(passField).sendKeys(pass);
            wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
        } catch (Exception e) {
            emailField = By.cssSelector("input[type='email']");
            passField = By.cssSelector("input[type='password']");
            loginBtn = By.xpath("//button[contains(., 'Entrar')] | //button[contains(., 'Login')]");
            
            wait.until(ExpectedConditions.elementToBeClickable(emailField)).clear();
            driver.findElement(emailField).sendKeys(user);
            driver.findElement(passField).clear();
            driver.findElement(passField).sendKeys(pass);
            wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
        }
    }

    private void performLogout() {
        By logoutLink = By.linkText("Logout") ;
        By logoutBtn = By.xpath("//button[contains(., 'Logout')] | //a[contains(@href, 'logout')]");
        
        try {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
        } catch (Exception e) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutBtn)).click();
        }
        wait.until(ExpectedConditions.urlContains("login"));
    }

    private void resetAppState() {
        By resetLink = By.linkText("Reset App State");
        By resetBtn = By.xpath("//button[contains(., 'Reset')] | //a[contains(., 'Reset')]");
        
        if (!driver.findElements(resetLink).isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resetLink)).click();
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory-item")));
        } else if (!driver.findElements(resetBtn).isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resetBtn)).click();
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory-item")));
        }
    }

    private String getCurrentWindowHandle() {
        return driver.getWindowHandle();
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        String title = driver.getTitle().toLowerCase();
        Assertions.assertTrue(title.contains("login") || title.contains("gestão") || title.contains("gestao"),
                "Login page title does not contain expected text");
    }

    @Test
    @Order(2)
    public void testLoginPagePresence() {
        driver.get(BASE_URL);
        Assertions.assertFalse(driver.findElements(By.cssSelector("input[name='email'], input#email")).isEmpty(),
                "Email input field not found");
        Assertions.assertFalse(driver.findElements(By.cssSelector("input[name='password'], input#password")).isEmpty(),
                "Password input field not found");
        Assertions.assertFalse(driver.findElements(By.cssSelector("button[type='submit'], button#login")).isEmpty(),
                "Login button not found");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        performLogin(USERNAME, PASSWORD);
        try {
            wait.until(ExpectedConditions.urlContains("dashboard"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard"),
                    "Valid login did not redirect to dashboard page");
        } catch (Exception e) {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login")));
            Assertions.assertFalse(driver.getCurrentUrl().contains("login"),
                    "Valid login did not redirect away from login page");
        }
        Assertions.assertFalse(driver.findElements(By.cssSelector(".inventory-item")).isEmpty(),
                "No inventory items found after login");
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        By emailField = By.cssSelector("input[name='email'], input#email");
        By passField = By.cssSelector("input[name='password'], input#password");
        By loginBtn = By.cssSelector("button[type='submit'], button#login");

        try {
            wait.until(ExpectedConditions.elementToBeClickable(emailField)).clear();
            driver.findElement(emailField).sendKeys("wrong@example.com");
            driver.findElement(passField).clear();
            driver.findElement(passField).sendKeys("wrongpass");
            wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
        } catch (Exception e) {
            emailField = By.cssSelector("input[type='email']");
            passField = By.cssSelector("input[type='password']");
            loginBtn = By.xpath("//button[contains(., 'Entrar')] | //button[contains(., 'Login')]");
            
            wait.until(ExpectedConditions.elementToBeClickable(emailField)).clear();
            driver.findElement(emailField).sendKeys("wrong@example.com");
            driver.findElement(passField).clear();
            driver.findElement(passField).sendKeys("wrongpass");
            wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
        }

        By errorMsg = By.cssSelector(".alert-danger, .error-message");
        By errorAlert = By.xpath("//div[contains(@class, 'error')] | //div[contains(@class, 'alert')]");
        WebElement error = null;
        try {
            error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
        } catch (Exception e) {
            error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorAlert));
        }
        String errorText = error.getText().toLowerCase();
        Assertions.assertTrue(errorText.contains("invalid") || errorText.contains("erro") || errorText.contains("error"),
                "Error message for invalid credentials not displayed");
    }

    @Test
    @Order(5)
    public void testSortingOptions() {
        performLogin(USERNAME, PASSWORD);
        By sortDropdown = By.cssSelector("select#sort");
        By firstItemName = By.cssSelector(".inventory-item .item-name");

        String[] options = {"name_asc", "name_desc", "price_asc", "price_desc"};
        String previousFirst = "";

        for (String opt : options) {
            try {
                WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));
                dropdown.findElement(By.xpath(String.format(".//option[@value='%s']", opt))).click();
            } catch (Exception e) {
                continue;
            }

            WebElement first = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItemName));
            String currentFirst = first.getText();

            if (!previousFirst.isEmpty()) {
                Assertions.assertNotEquals(previousFirst, currentFirst,
                        "Sorting option " + opt + " did not change first item order");
            }
            previousFirst = currentFirst;
        }
    }

    @Test
    @Order(6)
    public void testBurgerMenuAllItems() {
        performLogin(USERNAME, PASSWORD);
        By menuBtn = By.cssSelector("button[aria-label='Open Menu'], button#menu-toggle");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();
        } catch (Exception e) {
            menuBtn = By.xpath("//button[contains(@class, 'menu')] | //button/*[contains(@class, 'menu')]");
            wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();
        }

        By allItemsLink = By.linkText("All Items");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(allItemsLink)).click();
        } catch (Exception e) {
            allItemsLink = By.xpath("//a[contains(., 'Items')] | //a[contains(., 'Produtos')]");
            wait.until(ExpectedConditions.elementToBeClickable(allItemsLink)).click();
        }

        wait.until(ExpectedConditions.urlContains("items") );
        Assertions.assertTrue(driver.getCurrentUrl().contains("items") ,
                "Burger menu 'All Items' did not navigate to items page");
    }

    @Test
    @Order(7)
    public void testBurgerMenuAboutExternalLink() {
        performLogin(USERNAME, PASSWORD);
        String originalHandle = getCurrentWindowHandle();

        By menuBtn = By.cssSelector("button[aria-label='Open Menu'], button#menu-toggle");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();
        } catch (Exception e) {
            menuBtn = By.xpath("//button[contains(@class, 'menu')] | //button/*[contains(@class, 'menu')]");
            wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();
        }

        By aboutLink = By.linkText("About");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(aboutLink)).click();
        } catch (Exception e) {
            aboutLink = By.xpath("//a[contains(., 'Sobre')]");
            wait.until(ExpectedConditions.elementToBeClickable(aboutLink)).click();
        }

        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(originalHandle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("New window did not open"));

        driver.switchTo().window(newHandle);
        wait.until(ExpectedConditions.urlContains("about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                "About link did not open expected external domain");

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(8)
    public void testLogout() {
        performLogin(USERNAME, PASSWORD);
        performLogout();
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"),
                "Logout did not redirect to login page");
    }

    @Test
    @Order(9)
    public void testResetAppState() {
        performLogin(USERNAME, PASSWORD);
        resetAppState();
        Assertions.assertFalse(driver.findElements(By.cssSelector(".inventory-item")).isEmpty(),
                "Reset App State did not restore inventory list");
    }

    @Test
    @Order(10)
    public void testFooterSocialLinks() {
        performLogin(USERNAME, PASSWORD);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("footer a[target='_blank']"));
        Assertions.assertFalse(externalLinks.isEmpty(),
                "No external social links found in footer");

        String originalHandle = getCurrentWindowHandle();
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            link.click();

            Set<String> handles = driver.getWindowHandles();
            String newHandle = handles.stream()
                    .filter(h -> !h.equals(originalHandle))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("New window did not open"));

            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(href));
            Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                    "External link URL does not contain expected domain: " + href);

            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(11)
    public void testCartAddRemove() {
        performLogin(USERNAME, PASSWORD);

        By addButton = By.cssSelector("button[id^='add-to-cart-']");
        List<WebElement> addButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(addButton));
        Assertions.assertFalse(addButtons.isEmpty(), "No add-to-cart buttons found");
        addButtons.get(0).click();

        By cartBadge = By.id("shopping_cart_badge");
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge));
        Assertions.assertEquals("1", badge.getText(), "Cart badge not updated to 1");

        By cartLink = By.id("shopping_cart");
        wait.until(ExpectedConditions.elementToBeClickable(cartLink)).click();
        wait.until(ExpectedConditions.urlContains("cart"));

        By removeButton = By.cssSelector("button[id^='remove-']");
        WebElement removeBtn = wait.until(ExpectedConditions.elementToBeClickable(removeButton));
        removeBtn.click();

        WebElement badgeAfter = wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge));
        Assertions.assertEquals("0", badgeAfter.getText(), "Cart badge not updated to 0 after removal");
    }

    @Test
    @Order(12)
    public void testCheckoutSuccess() {
        performLogin(USERNAME, PASSWORD);

        By addButton = By.cssSelector("button[id^='add-to-cart-']");
        List<WebElement> addButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(addButton));
        Assertions.assertFalse(addButtons.isEmpty(), "No add-to-cart buttons found");
        addButtons.get(0).click();

        By cartLink = By.id("shopping_cart");
        wait.until(ExpectedConditions.elementToBeClickable(cartLink)).click();
        wait.until(ExpectedConditions.urlContains("checkout"));

        By firstName = By.id("first_name");
        By lastName = By.id("last_name");
        By address = By.id("address");
        By continueBtn = By.id("continue");

        wait.until(ExpectedConditions.visibilityOfElementLocated(firstName)).sendKeys("John");
        wait.until(ExpectedConditions.visibilityOfElementLocated(lastName)).sendKeys("Doe");
        wait.until(ExpectedConditions.visibilityOfElementLocated(address)).sendKeys("123 Test Ave");
        wait.until(ExpectedConditions.elementToBeClickable(continueBtn)).click();

        By finishBtn = By.id("finish");
        wait.until(ExpectedConditions.elementToBeClickable(finishBtn)).click();
        wait.until(ExpectedConditions.urlContains("confirmation"));

        By confirmationMsg = By.cssSelector(".confirmation-message");
        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(confirmationMsg));
        Assertions.assertTrue(msg.getText().toLowerCase().contains("thank you"),
                "Checkout confirmation message not displayed");
    }
}