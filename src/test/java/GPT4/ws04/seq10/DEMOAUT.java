package GPT4.ws04.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class FormPageTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    @Test
    @Order(1)
    public void testPageTitleAndHeader() {
        Assertions.assertTrue(driver.getTitle().contains("Sample Form"), "Title should contain 'Sample Form'");
        WebElement heading = driver.findElement(By.tagName("h1"));
        Assertions.assertEquals("AUT Form", heading.getText(), "Header text should match");
    }

    @Test
    @Order(2)
    public void testFormSubmissionValidData() {
        driver.navigate().refresh();
        fillForm("John", "Doe", "john.doe@example.com", "password123", "This is a comment.");
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        Assertions.assertTrue(alert.getText().contains("John"), "Alert should contain first name");
        alert.accept();
    }

    @Test
    @Order(3)
    public void testFormSubmissionMissingRequired() {
        driver.navigate().refresh();
        fillForm("", "Doe", "john.doe@example.com", "password123", "");
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        List<WebElement> alerts = driver.findElements(By.cssSelector(".error"));
        Assertions.assertTrue(alerts.size() > 0 || isAlertPresent(), "Missing required field should trigger alert or error");
        if (isAlertPresent()) driver.switchTo().alert().accept();
    }

    @Test
    @Order(4)
    public void testRadioButtons() {
        driver.navigate().refresh();
        WebElement male = driver.findElement(By.id("gender-male"));
        WebElement female = driver.findElement(By.id("gender-female"));
        male.click();
        Assertions.assertTrue(male.isSelected(), "Male should be selected");
        female.click();
        Assertions.assertTrue(female.isSelected(), "Female should be selected");
        Assertions.assertFalse(male.isSelected(), "Male should be deselected");
    }

    @Test
    @Order(5)
    public void testCheckboxes() {
        driver.navigate().refresh();
        WebElement bike = driver.findElement(By.id("checkbox-bicycle"));
        WebElement car = driver.findElement(By.id("checkbox-car"));
        if (!bike.isSelected()) bike.click();
        if (!car.isSelected()) car.click();
        Assertions.assertTrue(bike.isSelected(), "Bicycle should be checked");
        Assertions.assertTrue(car.isSelected(), "Car should be checked");
        bike.click();
        Assertions.assertFalse(bike.isSelected(), "Bicycle should be unchecked");
    }

    @Test
    @Order(6)
    public void testDropdownSelection() {
        driver.navigate().refresh();
        Select continent = new Select(driver.findElement(By.name("continent")));
        continent.selectByVisibleText("Europe");
        Assertions.assertEquals("Europe", continent.getFirstSelectedOption().getText(), "Selected continent should be Europe");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.navigate().refresh();
        List<WebElement> links = driver.findElements(By.tagName("a"));
        int tested = 0;
        String original = driver.getWindowHandle();
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith("http")) {
                Set<String> before = driver.getWindowHandles();
                link.click();
                wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(BASE_URL));
                if (driver.getWindowHandles().size() > before.size()) {
                    Set<String> after = driver.getWindowHandles();
                    after.removeAll(before);
                    String newTab = after.iterator().next();
                    driver.switchTo().window(newTab);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(getDomain(href)), "Should navigate to correct domain");
                    driver.close();
                    driver.switchTo().window(original);
                } else {
                    Assertions.assertTrue(driver.getCurrentUrl().contains(getDomain(href)), "Should navigate to correct domain");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
                }
                tested++;
                if (tested >= 3) break;
            }
        }
        Assertions.assertTrue(tested > 0, "At least one external link should be tested");
    }

    private void fillForm(String firstName, String lastName, String email, String password, String comments) {
        driver.findElement(By.name("firstname")).clear();
        driver.findElement(By.name("firstname")).sendKeys(firstName);
        driver.findElement(By.name("lastname")).clear();
        driver.findElement(By.name("lastname")).sendKeys(lastName);
        driver.findElement(By.name("email")).clear();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.name("comments")).clear();
        driver.findElement(By.name("comments")).sendKeys(comments);
    }

    private boolean isAlertPresent() {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private String getDomain(String url) {
        try {
            return java.net.URI.create(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }
}
