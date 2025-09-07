package GPT20b.ws08.seq01;

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

import java.util.List;
import java.util.Set;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JpetStoreTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";

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

    private void waitForVisibility(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void waitForClickability(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /* --------------------------------------------------------------------- */
    /*  Tests                                                              */
    /* --------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        goTo(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Catalog"));
        Assertions.assertTrue(
                driver.getTitle().toLowerCase().contains("catalog"),
                "Home page title should contain 'catalog'.");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        goTo(BASE_URL);
        By signIn = By.linkText("Sign In");
        waitForClickability(signIn);
        driver.findElement(signIn).click();

        waitForVisibility(By.id("username"));
        driver.findElement(By.id("username")).sendKeys("standard_user");
        driver.findElement(By.id("password")).sendKeys("standard_user");
        driver.findElement(By.id("submit")).click();

        wait.until(ExpectedConditions.urlContains("/main"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/main"),
                "After login the URL should contain '/main'.");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        goTo(BASE_URL);
        By signIn = By.linkText("Sign In");
        waitForClickability(signIn);
        driver.findElement(signIn).click();

        waitForVisibility(By.id("username"));
        driver.findElement(By.id("username")).sendKeys("invalid");
        driver.findElement(By.id("password")).sendKeys("invalid");
        driver.findElement(By.id("submit")).click();

        By error = By.xpath("//*[contains(text(),'Login Failed')]");
        waitForVisibility(error);
        Assertions.assertTrue(
                driver.findElement(error).isDisplayed(),
                "An error message should be displayed for invalid credentials.");
    }

    @Test
    @Order(4)
    public void testNavigationToProducts() {
        goTo(BASE_URL);
        By catalog = By.linkText("Catalog");
        waitForClickability(catalog);
        driver.findElement(catalog).click();

        wait.until(ExpectedConditions.urlContains("/main"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/main"),
                "Clicking 'Catalog' should navigate to '/main'.");

        // Verify that product categories display
        By categoryList = By.cssSelector(".mainframe>ul");
        waitForVisibility(categoryList);
        Assertions.assertFalse(
                driver.findElements(categoryList).isEmpty(),
                "Product categories should be displayed.");
    }

    @Test
    @Order(5)
    public void testSortingDropdownOptions() {
        goTo(BASE_URL + "main");
        By sortSelect = By.cssSelector("select#sort");
        List<WebElement> selects = driver.findElements(sortSelect);
        if (!selects.isEmpty()) {
            waitForClickability(sortSelect);
            WebElement selectEl = driver.findElement(sortSelect);
            List<WebElement> options = selectEl.findElements(By.tagName("option"));
            Assertions.assertFalse(options.isEmpty(), "Sort dropdown should have options.");
            String original = selectEl.getAttribute("value");

            for (int i = 0; i < options.size(); i++) {
                if (!options.get(i).getAttribute("value").equals(original)) {
                    options.get(i).click();
                    wait.until(ExpectedConditions.attributeToBe(
                            sortSelect, "value", options.get(i).getAttribute("value")));
                    Assertions.assertEquals(
                            options.get(i).getAttribute("value"),
                            selectEl.getAttribute("value"),
                            "Sorting selection should update the dropdown value.");
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        goTo(BASE_URL + "main");
        // Grab first product link
        By firstProduct = By.cssSelector(".productList>tr:nth-child(2) a");
        waitForClickability(firstProduct);
        driver.findElement(firstProduct).click();

        waitForVisibility(By.id("addProduct"));
        driver.findElement(By.id("addProduct")).click();

        // Go to cart page
        By cartLink = By.linkText("My Cart");
        waitForClickability(cartLink);
        driver.findElement(cartLink).click();

        wait.until(ExpectedConditions.urlContains("/cart"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/cart"),
                "Cart page URL should contain '/cart'.");

        // Proceed to checkout
        By checkoutBtn = By.linkText("Checkout");
        waitForClickability(checkoutBtn);
        driver.findElement(checkoutBtn).click();

        wait.until(ExpectedConditions.urlContains("/checkout"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/checkout"),
                "Checkout page URL should contain '/checkout'.");

        // Complete purchase
        waitForVisibility(By.name("shipFirstName"));
        driver.findElement(By.name("shipFirstName")).sendKeys("John");
        driver.findElement(By.name("shipLastName")).sendKeys("Doe");
        driver.findElement(By.name("shipStreet")).sendKeys("123 Main St");
        driver.findElement(By.name("shipCity")).sendKeys("Anytown");
        driver.findElement(By.name("shipState")).sendKeys("CA");
        driver.findElement(By.name("shipZip")).sendKeys("90210");
        driver.findElement(By.name("shipCountry")).sendKeys("USA");
        driver.findElement(By.name("invoiceFirstName")).sendKeys("John");
        driver.findElement(By.name("invoiceLastName")).sendKeys("Doe");
        driver.findElement(By.name("invoiceStreet")).sendKeys("123 Main St");
        driver.findElement(By.name("invoiceCity")).sendKeys("Anytown");
        driver.findElement(By.name("invoiceState")).sendKeys("CA");
        driver.findElement(By.name("invoiceZip")).sendKeys("90210");
        driver.findElement(By.name("invoiceCountry")).sendKeys("USA");
        driver.findElement(By.name("card")).sendKeys("123456");
        driver.findElement(By.name("name")).sendKeys("John Doe");
        driver.findElement(By.name("exp")).sendKeys("12/25");
        driver.findElement(By.name("cvc")).sendKeys("123");
        driver.findElement(By.name("pay")).click();

        // Verify success message
        By successMsg = By.xpath("//*[contains(text(),'Thank you for your purchase')]");
        waitForVisibility(successMsg);
        Assertions.assertTrue(
                driver.findElement(successMsg).isDisplayed(),
                "Purchase completion message should be displayed.");
    }

    @Test
    @Order(7)
    public void testFooterExternalLinks() {
        goTo(BASE_URL);
        String originalHandle = driver.getWindowHandle();
        List<String> domains = List.of("twitter.com", "facebook.com", "linkedin.com");

        for (String domain : domains) {
            List<WebElement> links = driver.findElements(
                    By.xpath("//a[contains(@href,'" + domain + "')]"));
            if (!links.isEmpty()) {
                links.get(0).click();

                wait.until(driver1 -> {
                    Set<String> handles = driver1.getWindowHandles();
                    return handles.size() > 1 || !driver1.getCurrentUrl().equals(BASE_URL);
                });

                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > 1) {
                    handles.remove(originalHandle);
                    String newHandle = handles.iterator().next();
                    driver.switchTo().window(newHandle);
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "External link should open domain " + domain);
                    driver.close();
                    driver.switchTo().window(originalHandle);
                } else {
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "External link should open domain " + domain);
                    driver.navigate().back();
                    wait.until(ExpectedConditions.urlToBe(BASE_URL));
                }
            }
        }
    }
}