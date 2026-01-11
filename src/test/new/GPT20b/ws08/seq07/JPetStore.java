package GPT20b.ws08.seq07;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "standard_password";

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- Helper methods -------------------------------- */

    private WebElement findElementWithFallback(List<By> locators) {
        for (By locator : locators) {
            try {
                return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            } catch (Exception ignored) {
            }
        }
        throw new NoSuchElementException("Required element not found using locators: " + locators);
    }

    private void login(String user, String pass) {
        driver.get(BASE_URL + "AccountLogin.do");
        WebElement userField = findElementWithFallback(
                List.of(By.id("username"), By.name("username"), By.cssSelector("input[name='username']")));
        WebElement passField = findElementWithFallback(
                List.of(By.id("password"), By.name("password"), By.cssSelector("input[name='password']")));
        WebElement loginBtn = findElementWithFallback(
                List.of(By.cssSelector("input[type='submit'], input[name='login'], button.loginBtn")));

        userField.clear();
        userField.sendKeys(user);
        passField.clear();
        passField.sendKeys(pass);

        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("Home.do"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("Home.do"),
                "User should land on Home page after login");
    }


    private void resetAppState() {
        // Simple implementation: empty the cart
        driver.get(BASE_URL + "ShoppingCart.do");
        List<WebElement> deleteLinks = driver.findElements(By.cssSelector("input[type='checkbox'].selectAll"));
        if (!deleteLinks.isEmpty()) {
            WebElement deleteBtn = findElementWithFallback(
                    List.of(By.name("validateForm"), By.id("deleteBtn")));
            deleteBtn.click();
        }
    }

    private List<String> getProductNames() {
        List<WebElement> nameEls = driver.findElements(By.cssSelector(".productName, .itemName"));
        return nameEls.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private int getCartCount() {
        List<WebElement> counts = driver.findElements(By.cssSelector(".cart-count, #shoppingCart"));
        return counts.isEmpty() ? 0 : Integer.parseInt(counts.get(0).getText());
    }

    private void scrollToFooter() {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    /* ---------- Tests ------------------------------------------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jpetstore"),
                "Title should contain 'JPetStore'");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        List<WebElement> products = driver.findElements(By.cssSelector(".productName, .itemName"));
        Assertions.assertFalse(products.isEmpty(), "Products should be listed after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "AccountLogin.do");
        WebElement userField = findElementWithFallback(
                List.of(By.id("username"), By.name("username"), By.cssSelector("input[name='username']")));
        WebElement passField = findElementWithFallback(
                List.of(By.id("password"), By.name("password"), By.cssSelector("input[name='password']")));
        WebElement loginBtn = findElementWithFallback(
                List.of(By.cssSelector("input[type='submit'], input[name='login'], button.loginBtn")));

        userField.clear();
        userField.sendKeys("invalid_user");
        passField.clear();
        passField.sendKeys("wrong_pass");

        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        WebElement err = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".errorMessage, .validation-message")));
        Assertions.assertTrue(err.isDisplayed(), "Error message must be displayed for invalid login");
        Assertions.assertTrue(err.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        // If catalog page has a sorting select, test it
        driver.get(BASE_URL + "Showcatalog.do");
        List<WebElement> selects = driver.findElements(By.id("catalogSort"));
        Assumptions.assumeTrue(!selects.isEmpty(), "No sorting dropdown found, skipping test");

        WebElement sortSelect = selects.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(sortSelect));
        sortSelect.click();

        // Grab original order
        List<String> original = getProductNames();
        Assertions.assertFalse(original.isEmpty(), "Product names should be present for sorting test");

        // Select first option that changes order
        WebElement optionA = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//select[@id='catalogSort']/option[2]")));
        optionA.click();

        List<String> afterA = getProductNames();
        Assertions.assertNotEquals(original, afterA, "Order should change after selecting different sort option");

        // Second option
        WebElement optionB = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//select[@id='catalogSort']/option[3]")));
        optionB.click();

        List<String> afterB = getProductNames();
        Assertions.assertNotEquals(afterA, afterB, "Order should change after selecting another sort option");
    }

    @Test
    @Order(5)
    public void testAccountMenuInteraction() {
        login(USERNAME, PASSWORD);

        WebElement accountBtn = findElementWithFallback(
                List.of(By.id("accountMenu"), By.cssSelector(".header .userMenu")));
        wait.until(ExpectedConditions.elementToBeClickable(accountBtn));
        accountBtn.click();

        // Verify "All Items" or "Products" link
        WebElement allItemsLink = findElementWithFallback(
                List.of(By.linkText("Products"), By.xpath("//a[contains(text(),'Products')]")));
        Assertions.assertTrue(allItemsLink.isDisplayed(), "'Products' link should be visible in account menu");

        // About (if external)
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        if (!aboutLinks.isEmpty()) {
            String originalHandle = driver.getWindowHandle();
            aboutLinks.get(0).click();

            wait.until(d -> d.getWindowHandles().size() > 1);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("about"),
                    "About page should point to an external domain");
            driver.close();
            driver.switchTo().window(originalHandle);
        }

        // Logout
        WebElement logoutLink = findElementWithFallback(
                List.of(By.linkText("Logout"), By.xpath("//a[contains(text(),'Logout')]")));
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to base URL");
    }

    @Test
    @Order(6)
    public void testFooterExternalLinks() {
        login(USERNAME, PASSWORD);
        scrollToFooter();
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("footer a[href^='http']"));
        Assumptions.assumeTrue(!externalLinks.isEmpty(), "No external footer links found");

        String originalHandle = driver.getWindowHandle();
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (href == null || !href.startsWith("http")) continue;

            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
            wait.until(d -> d.getWindowHandles().size() > 1);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                    "Opened link URL should contain the href: " + href);
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(7)
    public void testCartAddRemove() {
        login(USERNAME, PASSWORD);

        // Go to catalog and add two items
        driver.get(BASE_URL + "Showcatalog.do");
        List<WebElement> addButtons = driver.findElements(
                By.xpath("//input[@value='Add to Cart' or contains(@value,'Add to Cart')]"));

        Assumptions.assumeTrue(addButtons.size() >= 2, "Less than two add buttons found, skipping");

        addButtons.get(0).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".cart-count, #shoppingCart"), "1"));
        addButtons.get(1).click();

        int count = getCartCount();
        Assertions.assertEquals(2, count, "Cart should contain two items after adding");

        // Remove one item
        driver.get(BASE_URL + "ShoppingCart.do");
        List<WebElement> deleteCheckboxes = driver.findElements(
                By.cssSelector("input[type='checkbox'].product-checkbox"));
        if (!deleteCheckboxes.isEmpty()) {
            deleteCheckboxes.get(0).click();
            WebElement deleteBtn = findElementWithFallback(
                    List.of(By.name("validateForm"), By.xpath("//input[@value='Delete']")));
            deleteBtn.click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.cssSelector(".cart-count, #shoppingCart"), "1"));
            Assertions.assertEquals(1, getCartCount(), "Cart should have one item after deletion");
        } else {
            Assumptions.assumeTrue(false, "No deletable items in cart, skipping further verification");
        }
    }

    @Test
    @Order(8)
    public void testCheckoutFlow() {
        login(USERNAME, PASSWORD);

        // Add one item for checkout
        driver.get(BASE_URL + "Showcatalog.do");
        List<WebElement> addButtons = driver.findElements(
                By.xpath("//input[@value='Add to Cart' or contains(@value,'Add to Cart')]"));
        Assumptions.assumeTrue(!addButtons.isEmpty(), "No add buttons found for checkout test");
        addButtons.get(0).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".cart-count, #shoppingCart"), "1"));

        // Proceed to cart
        driver.get(BASE_URL + "ShoppingCart.do");
        WebElement checkoutBtn = findElementWithFallback(
                List.of(By.cssSelector("input[type='button'][value='Checkout']"), By.xpath("//input[@value='Checkout']")));
        checkoutBtn.click();

        // Fill shipping info
        List<WebElement> fields = driver.findElements(By.cssSelector("input[name='firstName']"));
        Assumptions.assumeTrue(!fields.isEmpty(), "Checkout form not available");

        driver.findElement(By.name("firstName")).sendKeys("John");
        driver.findElement(By.name("lastName")).sendKeys("Doe");
        driver.findElement(By.name("address")).sendKeys("123 Main St");
        driver.findElement(By.name("city")).sendKeys("Anywhere");
        driver.findElement(By.name("state")).sendKeys("NY");
        driver.findElement(By.name("zip")).sendKeys("12345");
        driver.findElement(By.name("country")).sendKeys("United States");
        driver.findElement(By.name("email")).sendKeys("john.doe@example.com");

        WebElement finishBtn = findElementWithFallback(
                List.of(By.cssSelector("input[type='submit'][value='Finish']"), By.xpath("//input[@value='Finish']")));
        finishBtn.click();

        wait.until(ExpectedConditions.urlContains("ConfirmOrder.do"));
        WebElement successMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".title")));
        Assertions.assertTrue(successMsg.getText().toLowerCase().contains("order confirmed"),
                "Order confirmation message should be displayed");
    }

    @Test
    @Order(9)
    public void testResetAppStateIndependence() {
        login(USERNAME, PASSWORD);
        resetAppState();
        int cartCount = getCartCount();
        Assertions.assertEquals(0, cartCount, "Cart should be empty after reset");
    }
}