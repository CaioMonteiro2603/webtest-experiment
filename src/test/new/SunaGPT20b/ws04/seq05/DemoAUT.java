package SunaGPT20b.ws04.seq05;

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
public class DemoAUT {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
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

    @Test
    @Order(1)
    public void testPageLoadsAndTitle() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertNotNull(title, "Page title should not be null");
        Assertions.assertFalse(title.trim().isEmpty(), "Page title should not be empty");
    }

    @Test
    @Order(2)
    public void testFormSubmission() {
        driver.get(BASE_URL);
        WebElement form = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));

        // Fill out text inputs
        List<WebElement> textInputs = driver.findElements(By.cssSelector("input[type='text'], input[type='email']"));
        for (WebElement input : textInputs) {
            if (input.getAttribute("type").equals("email")) {
                input.sendKeys("test@example.com");
            } else {
                input.sendKeys("TestUser");
            }
        }

        // Select gender radio if present
        List<WebElement> genderRadios = driver.findElements(By.name("gender"));
        if (!genderRadios.isEmpty()) {
            genderRadios.get(0).click();
        }

        // Select a dropdown if present
        List<WebElement> selectElements = driver.findElements(By.tagName("select"));
        for (WebElement select : selectElements) {
            List<WebElement> options = select.findElements(By.tagName("option"));
            if (options.size() > 1) {
                options.get(1).click();
            }
        }

        // Submit form
        WebElement submitBtn = null;
        try {
            submitBtn = form.findElement(By.cssSelector("input[type='submit']"));
        } catch (Exception e) {
            try {
                submitBtn = form.findElement(By.cssSelector("button[type='submit']"));
            } catch (Exception e2) {
                submitBtn = form.findElement(By.tagName("button"));
            }
        }
        submitBtn.click();

        // Verify submission
        try {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("form.html")));
        } catch (Exception e) {
            List<WebElement> successElements = driver.findElements(
                By.xpath("//*[contains(text(),'Thank you') or contains(text(),'Success') or contains(text(),'Submitted')]"));
            Assertions.assertFalse(successElements.isEmpty(), "Expected success message after form submission");
        }
    }

    @Test
    @Order(3)
    public void testExternalLinksOneLevelDeep() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        List<WebElement> links = driver.findElements(By.tagName("a"));
        boolean foundExternal = false;
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith("http") && !href.contains("katalon-test.s3.amazonaws.com")) {
                foundExternal = true;
                link.click();
                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > 1) {
                    for (String h : handles) {
                        if (!h.equals(originalWindow)) {
                           
            at SunaGPT20b5
at java.base/java/util/   3) . . . 4, "true'0x
                | (5
            at Suna4c
...