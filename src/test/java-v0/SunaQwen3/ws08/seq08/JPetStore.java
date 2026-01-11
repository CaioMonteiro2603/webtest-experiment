package SunaQwen3.ws08.seq08;

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
        assertEquals("JPetStore Demo", driver.getTitle());

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        assertEquals("https://jpetstore.aspectran.com/actions/Catalog.action", driver.getCurrentUrl());

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='WelcomeContent']/b")));
        assertTrue(welcomeMessage.getText().contains("j2ee"), "Welcome message should contain username");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get("https://jpetstore.aspectran.com/actions/Account.action?signonForm=");
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("messages")));
        assertTrue(errorMessage.getText().contains("Invalid username or password. Signon failed."));
    }

    @Test
    @Order(3)
    void testBrowseCategoriesAndProducts() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        usernameField.sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.name("signon")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("WelcomeContent")));

        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        fishCategory.click();

        assertTrue(driver.getCurrentUrl().contains("categoryId=FISH"));
        WebElement product = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01")));
        product.click();

        WebElement itemId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='EST-1']")));
        assertEquals("EST-1", itemId.getText());
    }

    @Test
    @Order(4)
    void testAddToCartAndCheckout() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement dogCategory = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Dogs")));
        dogCategory.click();

        WebElement product = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("K9-RT-02")));
        product.click();

        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.name("EST-10")));
        addToCart.click();

        WebElement proceedToCheckout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout")));
        proceedToCheckout.click();

        WebElement billingAddressHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='Billing Address']")));
        assertEquals("Billing Address", billingAddressHeader.getText());

        List<WebElement> continueButtons = driver.findElements(By.name("newOrder"));
        assertFalse(continueButtons.isEmpty(), "Continue button should be present");
        continueButtons.get(0).click();

        WebElement confirmHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='Confirm']")));
        assertEquals("Confirm", confirmHeader.getText());

        List<WebElement> confirmButtons = driver.findElements(By.name("submitOrder"));
        assertFalse(confirmButtons.isEmpty(), "Confirm button should be present");
        confirmButtons.get(0).click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("messages")));
        assertTrue(successMessage.getText().contains("Thank you for shopping at JPetStore!"));
    }

    @Test
    @Order(5)
    void testSortingDropdown() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement catsCategory = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Cats")));
        catsCategory.click();

        WebElement sortSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("sort")));
        
        // Test name ascending
        sortSelect.click();
        WebElement optionNameAsc = driver.findElement(By.cssSelector("select[name='sort'] option[value='name_asc']"));
        optionNameAsc.click();
        wait.until(ExpectedConditions.stalenessOf(sortSelect));
        sortSelect = driver.findElement(By.name("sort"));
        assertEquals("name_asc", sortSelect.getAttribute("value"));

        // Test price descending
        sortSelect.click();
        WebElement optionPriceDesc = driver.findElement(By.cssSelector("select[name='sort'] option[value='price_desc']"));
        optionPriceDesc.click();
        wait.until(ExpectedConditions.stalenessOf(sortSelect));
        sortSelect = driver.findElement(By.name("sort"));
        assertEquals("price_desc", sortSelect.getAttribute("value"));
    }

    @Test
    @Order(6)
    void testMenuNavigation() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("menu-button")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();
        assertTrue(driver.getCurrentUrl().contains("Catalog.action"));

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("menu-button")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        String newWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.isEmpty() ? null : handles.iterator().next();
        });

        assertNotNull(newWindow, "New window should have been opened");
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains("github.com"), "About link should open GitHub page");
        driver.close();
        driver.switchTo().window(originalWindow);

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("menu-button")));
        menuButton.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetLink.click();

        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        alert.accept();

        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("SidebarContent")));
        assertTrue(inventoryList.isDisplayed());
    }

    @Test
    @Order(7)
    void testFooterExternalLinks() {
        driver.get(BASE_URL);
        loginIfNecessary();

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("div.footer a"));
        assertTrue(footerLinks.size() >= 3, "Footer should have at least 3 social links");

        String originalWindow = driver.getWindowHandle();

        // Test Twitter
        WebElement twitterLink = footerLinks.get(0);
        assertEquals("Twitter", twitterLink.getText());
        twitterLink.click();

        String twitterWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.isEmpty() ? null : handles.iterator().next();
        });

        assertNotNull(twitterWindow, "Twitter link should open new tab");
        driver.switchTo().window(twitterWindow);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should contain twitter.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook
        footerLinks = driver.findElements(By.cssSelector("div.footer a"));
        WebElement facebookLink = footerLinks.get(1);
        assertEquals("Facebook", facebookLink.getText());
        facebookLink.click();

        String facebookWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.isEmpty() ? null : handles.iterator().next();
        });

        assertNotNull(facebookWindow, "Facebook link should open new tab");
        driver.switchTo().window(facebookWindow);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should contain facebook.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn
        footerLinks = driver.findElements(By.cssSelector("div.footer a"));
        WebElement linkedinLink = footerLinks.get(2);
        assertEquals("LinkedIn", linkedinLink.getText());
        linkedinLink.click();

        String linkedinWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.isEmpty() ? null : handles.iterator().next();
        });

        assertNotNull(linkedinWindow, "LinkedIn link should open new tab");
        driver.switchTo().window(linkedinWindow);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should contain linkedin.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    void testLogout() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("menu-button")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        assertEquals("Sign In", signInLink.getText());
    }

    private void loginIfNecessary() {
        if (driver.getCurrentUrl().contains("signonForm")) {
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            usernameField.sendKeys(USERNAME);
            driver.findElement(By.name("password")).sendKeys(PASSWORD);
            driver.findElement(By.name("signon")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("WelcomeContent")));
        }
    }
}