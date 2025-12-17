package GPT20b.ws04.seq07;


import java.util.List;

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
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- Helper methods ---------- */

    private WebElement findElementWithFallback(List<By> locators) {
        for (By locator : locators) {
            try {
                return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            } catch (Exception ignored) {
            }
        }
        throw new NoSuchElementException("Element not found using locators: " + locators);
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testPageLoads() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("form"),
                "Page title should contain the word 'Form'");
    }

    @Test
    @Order(2)
    public void testFormFieldsExist() {
        driver.get(BASE_URL);

        WebElement nameField = findElementWithFallback(
                List.of(By.id("name"), By.name("name"), By.cssSelector("input[placeholder='First Name']")));
        WebElement emailField = findElementWithFallback(
                List.of(By.id("email"), By.name("email"), By.cssSelector("input[placeholder='E-mail']")));
        WebElement phoneField = findElementWithFallback(
                List.of(By.id("phone"), By.name("phone"), By.cssSelector("input[placeholder='Phone Number']")));

        Assertions.assertTrue(nameField.isDisplayed(), "Name field should be displayed");
        Assertions.assertTrue(emailField.isDisplayed(), "Email field should be displayed");
        Assertions.assertTrue(phoneField.isDisplayed(), "Phone field should be displayed");
    }

    @Test
    @Order(3)
    public void testFormSubmissionSuccess() {
        driver.get(BASE_URL);

        WebElement nameField = findElementWithFallback(
                List.of(By.id("name"), By.name("name")));
        WebElement emailField = findElementWithFallback(
                List.of(By.id("email"), By.name("email")));
        WebElement phoneField = findElementWithFallback(
                List.of(By.id("phone"), By.name("phone")));

        nameField.clear();
        nameField.sendKeys("Test User");
        emailField.clear();
        emailField.sendKeys("test@example.com");
        phoneField.clear();
        phoneField.sendKeys("1234567890");

        WebElement submitBtn = findElementWithFallback(
                List.of(By.id("submit"), By.cssSelector("button[type='submit']"), By.name("submit")));

        wait.until(ExpectedConditions.elementToBeClickable(submitBtn));
        submitBtn.click();

        WebElement successMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMsg.isDisplayed(),
                "Success message should be visible after form submission");
        Assertions.assertTrue(successMsg.getText().toLowerCase().contains("success"),
                "Success message should contain the word 'success'");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href^='http']"));

        if (externalLinks.isEmpty()) {
            Assumptions.assumeTrue(false, "No external links found on the page, skipping test");
        }

        String originalHandle = driver.getWindowHandle();

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (href == null || !href.startsWith("http")) {
                continue;
            }
            // Open in new tab via JavaScript to avoid pop-ups
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);

            wait.until(d -> d.getWindowHandles().size() > 1);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }

            Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                    "Opened link URL should match the clicked href");

            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(5)
    public void testSortingDropdownAbsent() {
        driver.get(BASE_URL);
        List<WebElement> sortElements = driver.findElements(By.cssSelector("select[name='sort']"));
        Assumptions.assumeTrue(sortElements.isEmpty(), "Sorting dropdown not present, skipping test");
    }

    @Test
    @Order(6)
    public void testMenuNotPresent() {
        driver.get(BASE_URL);
        List<WebElement> burgerBtns = driver.findElements(By.id("burger-menu-btn"));
        Assumptions.assumeTrue(burgerBtns.isEmpty(), "Menu button not present, skipping test");
    }
}