package GPT4.ws04.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class DEMOAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testPageTitle() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertEquals("Demo AUT", title, "Page title should be 'Demo AUT'");
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);

        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        WebElement lastName = driver.findElement(By.id("lastName"));
        WebElement genderMale = driver.findElement(By.id("gender-male"));
        WebElement dob = driver.findElement(By.id("dob"));
        WebElement roleSelect = driver.findElement(By.id("role"));
        WebElement jobExpectation = driver.findElement(By.id("expectation-01"));
        WebElement developmentCheckbox = driver.findElement(By.id("development-01"));
        WebElement comment = driver.findElement(By.id("comment"));
        WebElement submit = driver.findElement(By.id("submit"));

        firstName.clear();
        firstName.sendKeys("John");

        lastName.clear();
        lastName.sendKeys("Doe");

        genderMale.click();

        dob.clear();
        dob.sendKeys("2000-01-01");

        Select roleDropdown = new Select(roleSelect);
        roleDropdown.selectByVisibleText("QA");

        if (!jobExpectation.isSelected()) {
            jobExpectation.click();
        }

        if (!developmentCheckbox.isSelected()) {
            developmentCheckbox.click();
        }

        comment.clear();
        comment.sendKeys("This is a test comment.");

        submit.click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String alertText = alert.getText();
        alert.accept();

        Assertions.assertTrue(alertText.contains("successfully submitted"), "Alert should confirm successful submission.");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithMissingFields() {
        driver.get(BASE_URL);
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submit.click();

        wait.until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(submit)));
    }

    @Test
    @Order(4)
    public void testExternalPrivacyLink() {
        driver.get(BASE_URL);
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Privacy")));
        String originalWindow = driver.getWindowHandle();
        privacyLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("katalon.com"));
        String newUrl = driver.getCurrentUrl();
        Assertions.assertTrue(newUrl.contains("katalon.com"), "External link should lead to katalon.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testRoleDropdownOptions() {
        driver.get(BASE_URL);
        WebElement roleSelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("role")));
        Select select = new Select(roleSelect);
        List<WebElement> options = select.getOptions();

        Assertions.assertTrue(options.size() > 1, "Dropdown should have multiple role options.");

        select.selectByIndex(1);
        Assertions.assertEquals(options.get(1).getText(), select.getFirstSelectedOption().getText(), "Selected option should match.");
    }

    @Test
    @Order(6)
    public void testCheckboxesAndRadioButtons() {
        driver.get(BASE_URL);

        WebElement genderFemale = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("gender-female")));
        WebElement expectation2 = driver.findElement(By.id("expectation-02"));
        WebElement development2 = driver.findElement(By.id("development-02"));

        genderFemale.click();
        expectation2.click();
        development2.click();

        Assertions.assertTrue(genderFemale.isSelected(), "Female radio button should be selected.");
        Assertions.assertTrue(expectation2.isSelected(), "Expectation 02 should be selected.");
        Assertions.assertTrue(development2.isSelected(), "Development 02 should be selected.");
    }
}