package deepseek.ws05.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class CafeTownsendTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USERNAME = "Luke";
    private static final String PASSWORD = "Skywalker";

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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("employees"));
        WebElement greeting = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greetings")));
        Assertions.assertTrue(greeting.getText().contains("Hello " + USERNAME));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("credentials");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid username or password"));
    }

    @Test
    @Order(3)
    public void testCreateEmployee() {
        login();
        
        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("bAdd")));
        createButton.click();
        
        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[ng-model='selectedEmployee.firstName']")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.cssSelector("input[ng-model='selectedEmployee.lastName']"));
        lastName.sendKeys("Doe");
        
        WebElement startDate = driver.findElement(By.cssSelector("input[ng-model='selectedEmployee.startDate']"));
        startDate.sendKeys("2024-01-01");
        
        WebElement email = driver.findElement(By.cssSelector("input[ng-model='selectedEmployee.email']"));
        email.sendKeys("john.doe@example.com");
        
        WebElement addButton = driver.findElement(By.cssSelector("button[class='formFooter']"));
        addButton.click();
        
        List<WebElement> employees = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("ul#employee-list li"), 1));
        Assertions.assertTrue(employees.stream().anyMatch(e -> e.getText().contains("John Doe")));
    }

    @Test
    @Order(4)
    public void testEditEmployee() {
        login();
        
        WebElement employeeItem = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//ul[@id='employee-list']/li[contains(text(),'Luke Skywalker')]")));
        employeeItem.click();
        
        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("bEdit")));
        editButton.click();
        
        WebElement lastNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[ng-model='selectedEmployee.lastName']")));
        lastNameField.clear();
        lastNameField.sendKeys("Skywalker Edited");
        
        WebElement updateButton = driver.findElement(By.cssSelector("button[class='formFooter']"));
        updateButton.click();
        
        WebElement editedEmployee = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//ul[@id='employee-list']/li[contains(text(),'Luke Skywalker Edited')]")));
        Assertions.assertTrue(editedEmployee.isDisplayed());
    }

    @Test
    @Order(5)
    public void testDeleteEmployee() {
        login();
        
        // First create an employee to delete
        testCreateEmployee();
        
        WebElement employeeToDelete = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//ul[@id='employee-list']/li[contains(text(),'John Doe')]")));
        employeeToDelete.click();
        
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("bDelete")));
        deleteButton.click();
        
        driver.switchTo().alert().accept();
        
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
            By.xpath("//ul[@id='employee-list']/li[contains(text(),'John Doe')]")));
        
        List<WebElement> employees = driver.findElements(By.xpath("//ul[@id='employee-list']/li[contains(text(),'John Doe')]"));
        Assertions.assertEquals(0, employees.size());
    }

    @Test
    @Order(6)
    public void testLogout() {
        login();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("p[class='main-button']")));
        logoutButton.click();
        
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.findElement(By.id("login-button")).isDisplayed());
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("employees"));
    }
}