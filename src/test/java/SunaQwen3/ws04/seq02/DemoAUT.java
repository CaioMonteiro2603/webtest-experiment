package SunaQwen3.ws04.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

@TestMethodOrder(OrderAnnotation.class)
public class SiteTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final String USERNAME = "demo";
    private static final String PASSWORD = "demo123";

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
    public void testPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertTrue(title.contains("Form"), "Page title should contain 'Form'");
        Assertions.assertTrue(driver.getCurrentUrl().contains("form.html"), "URL should contain form.html");
    }

    @Test
    @Order(2)
    public void testFormFieldsArePresent() {
        driver.get(BASE_URL);

        By firstNameField = By.name("firstName");
        By lastNameField = By.name("lastName");
        By genderMaleRadio = By.xpath("//input[@value='Male']");
        By genderFemaleRadio = By.xpath("//input[@value='Female']");
        By dobField = By.name("dob");
        By addressField = By.name("address");
        By emailField = By.name("email");
        By passwordField = By.name("password");
        By confirmPasswordField = By.name("confirmPassword");
        By submitButton = By.xpath("//button[@type='submit']");

        wait.until(presenceOfElementLocated(firstNameField));
        wait.until(presenceOfElementLocated(lastNameField));
        wait.until(presenceOfElementLocated(genderMaleRadio));
        wait.until(presenceOfElementLocated(genderFemaleRadio));
        wait.until(presenceOfElementLocated(dobField));
        wait.until(presenceOfElementLocated(addressField));
        wait.until(presenceOfElementLocated(emailField));
        wait.until(presenceOfElementLocated(passwordField));
        wait.until(presenceOfElementLocated(confirmPasswordField));
        wait.until(presenceOfElementLocated(submitButton));

        Assertions.assertTrue(driver.findElement(firstNameField).isDisplayed(), "First Name field should be displayed");
        Assertions.assertTrue(driver.findElement(lastNameField).isDisplayed(), "Last Name field should be displayed");
        Assertions.assertTrue(driver.findElement(genderMaleRadio).isDisplayed(), "Male radio button should be displayed");
        Assertions.assertTrue(driver.findElement(genderFemaleRadio).isDisplayed(), "Female radio button should be displayed");
        Assertions.assertTrue(driver.findElement(dobField).isDisplayed(), "Date of Birth field should be displayed");
        Assertions.assertTrue(driver.findElement(addressField).isDisplayed(), "Address field should be displayed");
        Assertions.assertTrue(driver.findElement(emailField).isDisplayed(), "Email field should be displayed");
        Assertions.assertTrue(driver.findElement(passwordField).isDisplayed(), "Password field should be displayed");
        Assertions.assertTrue(driver.findElement(confirmPasswordField).isDisplayed(), "Confirm Password field should be displayed");
        Assertions.assertTrue(driver.findElement(submitButton).isDisplayed(), "Submit button should be displayed");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);

        By firstNameField = By.name("firstName");
        By lastNameField = By.name("lastName");
        By genderMaleRadio = By.xpath("//input[@value='Male']");
        By dobField = By.name("dob");
        By addressField = By.name("address");
        By emailField = By.name("email");
        By passwordField = By.name("password");
        By confirmPasswordField = By.name("confirmPasswordField");
        By submitButton = By.xpath("//button[@type='submit']");
        By successMessage = By.id("success");

        wait.until(presenceOfElementLocated(firstNameField)).sendKeys("John");
        wait.until(presenceOfElementLocated(lastNameField)).sendKeys("Doe");
        wait.until(elementToBeClickable(genderMaleRadio)).click();
        wait.until(presenceOfElementLocated(dobField)).sendKeys("1990-01-01");
        wait.until(presenceOfElementLocated(addressField)).sendKeys("123 Main St");
        wait.until(presenceOfElementLocated(emailField)).sendKeys("john.doe@example.com");
        wait.until(presenceOfElementLocated(passwordField)).sendKeys("Password123!");
        wait.until(presenceOfElementLocated(confirmPasswordField)).sendKeys("Password123!");
        WebElement submitBtn = wait.until(elementToBeClickable(submitButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitBtn);
        submitBtn.click();

        wait.until(presenceOfElementLocated(successMessage));
        String messageText = driver.findElement(successMessage).getText();
        Assertions.assertTrue(messageText.contains("User registered successfully"), "Success message should be displayed");
    }

    @Test
    @Order(4)
    public void testFormSubmissionWithMismatchedPasswords() {
        driver.get(BASE_URL);

        By firstNameField = By.name("firstName");
        By lastNameField = By.name("lastName");
        By genderMaleRadio = By.xpath("//input[@value='Male']");
        By dobField = By.name("dob");
        By addressField = By.name("address");
        By emailField = By.name("email");
        By passwordField = By.name("password");
        By confirmPasswordField = By.name("confirmPassword");
        By submitButton = By.xpath("//button[@type='submit']");
        By errorMessage = By.id("error");

        wait.until(presenceOfElementLocated(firstNameField)).sendKeys("Jane");
        wait.until(presenceOfElementLocated(lastNameField)).sendKeys("Doe");
        wait.until(elementToBeClickable(genderMaleRadio)).click();
        wait.until(presenceOfElementLocated(dobField)).sendKeys("1990-01-01");
        wait.until(presenceOfElementLocated(addressField)).sendKeys("123 Main St");
        wait.until(presenceOfElementLocated(emailField)).sendKeys("jane.doe@example.com");
        wait.until(presenceOfElementLocated(passwordField)).sendKeys("Password123!");
        wait.until(presenceOfElementLocated(confirmPasswordField)).sendKeys("Password456!");
        WebElement submitBtn = wait.until(elementToBeClickable(submitButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitBtn);
        submitBtn.click();

        wait.until(presenceOfElementLocated(errorMessage));
        String errorText = driver.findElement(errorMessage).getText();
        Assertions.assertTrue(errorText.contains("Passwords do not match"), "Error message should be displayed for mismatched passwords");
    }

    @Test
    @Order(5)
    public void testExternalLinksInFooter() {
        driver.get(BASE_URL);

        By twitterLink = By.cssSelector("footer a[href*='twitter']");
        By facebookLink = By.cssSelector("footer a[href*='facebook']");
        By linkedinLink = By.cssSelector("footer a[href*='linkedin']");

        wait.until(presenceOfElementLocated(twitterLink));
        wait.until(presenceOfElementLocated(facebookLink));
        wait.until(presenceOfElementLocated(linkedinLink));

        String originalWindow = driver.getWindowHandle();
        Set<String> existingWindows = driver.getWindowHandles();

        // Test Twitter link
        driver.findElement(twitterLink).click();
        switchToNewWindow(originalWindow, existingWindows);
        String twitterUrl = driver.getCurrentUrl();
        Assertions.assertTrue(twitterUrl.contains("twitter.com"), "Twitter link should open a URL containing 'twitter.com'");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        driver.findElement(facebookLink).click();
        switchToNewWindow(originalWindow, existingWindows);
        String facebookUrl = driver.getCurrentUrl();
        Assertions.assertTrue(facebookUrl.contains("facebook.com"), "Facebook link should open a URL containing 'facebook.com'");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        driver.findElement(linkedinLink).click();
        switchToNewWindow(originalWindow, existingWindows);
        String linkedinUrl = driver.getCurrentUrl();
        Assertions.assertTrue(linkedinUrl.contains("linkedin.com"), "LinkedIn link should open a URL containing 'linkedin.com'");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void switchToNewWindow(String originalWindow, Set<String> existingWindows) {
        wait.until(webDriver -> {
            Set<String> newWindows = webDriver.getWindowHandles();
            newWindows.removeAll(existingWindows);
            return newWindows.size() > 0;
        });

        Set<String> allWindows = driver.getWindowHandles();
        allWindows.removeAll(existingWindows);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);
    }
}