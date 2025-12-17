package GPT4.ws04.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

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

    private void switchToNewTabAndVerify(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        for (String window : allWindows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected domain not found in URL: " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(1)
    public void testFormElementsPresence() {
        driver.get(BASE_URL);

        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("first_name"))).isDisplayed(), "First name field should be present.");
        Assertions.assertTrue(driver.findElement(By.name("last_name")).isDisplayed(), "Last name field should be present.");
        Assertions.assertTrue(driver.findElement(By.name("gender")).isDisplayed(), "Gender radio should be present.");
        Assertions.assertTrue(driver.findElement(By.name("dob")).isDisplayed(), "DOB field should be present.");
        Assertions.assertTrue(driver.findElement(By.name("address")).isDisplayed(), "Address field should be present.");
        Assertions.assertTrue(driver.findElement(By.name("email")).isDisplayed(), "Email field should be present.");
        Assertions.assertTrue(driver.findElement(By.name("password")).isDisplayed(), "Password field should be present.");
    }

    @Test
    @Order(2)
    public void testDropdownSelection() {
        driver.get(BASE_URL);
        WebElement roleDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("role")));
        Select select = new Select(roleDropdown);

        select.selectByVisibleText("Manager");
        Assertions.assertEquals("Manager", select.getFirstSelectedOption().getText(), "Dropdown should have 'Manager' selected.");

        select.selectByVisibleText("Developer");
        Assertions.assertEquals("Developer", select.getFirstSelectedOption().getText(), "Dropdown should have 'Developer' selected.");
    }

    @Test
    @Order(3)
    public void testCheckboxInteraction() {
        driver.get(BASE_URL);

        WebElement interest1 = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Read']")));
        WebElement interest2 = driver.findElement(By.cssSelector("input[value='Travel']"));

        if (!interest1.isSelected()) {
            interest1.click();
        }
        if (!interest2.isSelected()) {
            interest2.click();
        }

        Assertions.assertTrue(interest1.isSelected(), "'Read' checkbox should be selected.");
        Assertions.assertTrue(interest2.isSelected(), "'Travel' checkbox should be selected.");
    }

    @Test
    @Order(4)
    public void testSubmitFormSuccess() {
        driver.get(BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("first_name"))).sendKeys("Caio");
        driver.findElement(By.name("last_name")).sendKeys("Montes");
        driver.findElement(By.cssSelector("input[value='Male']")).click();
        driver.findElement(By.name("dob")).sendKeys("2000-01-01");
        driver.findElement(By.name("address")).sendKeys("123 Main St");
        driver.findElement(By.name("email")).sendKeys("test@example.com");
        driver.findElement(By.name("password")).sendKeys("secure123");

        Select role = new Select(driver.findElement(By.name("role")));
        role.selectByVisibleText("Manager");

        WebElement readCheck = driver.findElement(By.cssSelector("input[value='Read']"));
        if (!readCheck.isSelected()) readCheck.click();

        WebElement submit = driver.findElement(By.cssSelector("button[type='submit']"));
        submit.click();

        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMsg.getText().toLowerCase().contains("success"), "Submission should return success message.");
    }

    @Test
    @Order(5)
    public void testExternalLinkInteraction() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Privacy Policy")));
        footerLink.click();
        switchToNewTabAndVerify("katalon.com");
    }
}
