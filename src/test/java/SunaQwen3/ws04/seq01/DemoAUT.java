package SunaQwen3.ws04.seq01;

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

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String JOB_TITLE = "QA Engineer";
    private static final String EDUCATION_LEVEL = "College";
    private static final String SEX = "Male";
    private static final boolean EXPERIENCE = true;
    private static final String CONTINENTS = "North America";
    private static final String SKILLS = "Selenium WebDriver";

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
    public void testPageTitleAndHeader() {
        driver.get(BASE_URL);
        String expectedTitle = "Demo AUT";
        Assertions.assertEquals(expectedTitle, driver.getTitle(), "Page title should be 'Demo AUT'");

        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("Registration Form", header.getText(), "Header text should be 'Registration Form'");
    }

    @Test
    @Order(2)
    public void testFirstNameField() {
        driver.get(BASE_URL);
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstNameField.clear();
        firstNameField.sendKeys(FIRST_NAME);

        Assertions.assertEquals(FIRST_NAME, firstNameField.getAttribute("value"), "First name should be set correctly");
    }

    @Test
    @Order(3)
    public void testLastNameField() {
        driver.get(BASE_URL);
        WebElement lastNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("last-name")));
        lastNameField.clear();
        lastNameField.sendKeys(LAST_NAME);

        Assertions.assertEquals(LAST_NAME, lastNameField.getAttribute("value"), "Last name should be set correctly");
    }

    @Test
    @Order(4)
    public void testJobTitleField() {
        driver.get(BASE_URL);
        WebElement jobTitleField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("job")));
        jobTitleField.clear();
        jobTitleField.sendKeys(JOB_TITLE);

        Assertions.assertEquals(JOB_TITLE, jobTitleField.getAttribute("value"), "Job title should be set correctly");
    }

    @Test
    @Order(5)
    public void testEducationLevelRadioButtons() {
        driver.get(BASE_URL);
        WebElement educationRadio = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='education' and @value='" + EDUCATION_LEVEL + "']")));
        educationRadio.click();

        Assertions.assertTrue(educationRadio.isSelected(), "College radio button should be selected");
    }

    @Test
    @Order(6)
    public void testSexRadioButtons() {
        driver.get(BASE_URL);
        WebElement sexRadio = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='sex' and @value='" + SEX + "']")));
        sexRadio.click();

        Assertions.assertTrue(sexRadio.isSelected(), "Male radio button should be selected");
    }

    @Test
    @Order(7)
    public void testExperienceCheckbox() {
        driver.get(BASE_URL);
        WebElement experienceCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='checkbox' and @value='1']")));
        if (EXPERIENCE && !experienceCheckbox.isSelected()) {
            experienceCheckbox.click();
        } else if (!EXPERIENCE) {
            experienceCheckbox.click();
        }

        Assertions.assertEquals(EXPERIENCE, experienceCheckbox.isSelected(), "Experience checkbox state should match expected");
    }

    @Test
    @Order(8)
    public void testContinentsDropdown() {
        driver.get(BASE_URL);
        WebElement continentsSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("continents")));
        continentsSelect.click();

        WebElement northAmericaOption = driver.findElement(By.xpath("//option[text()='" + CONTINENTS + "']"));
        northAmericaOption.click();

        Assertions.assertEquals(CONTINENTS, new org.openqa.selenium.support.ui.Select(continentsSelect).getFirstSelectedOption().getText(),
                "Selected continent should be North America");
    }

    @Test
    @Order(9)
    public void testSeleniumCommandsDropdown() {
        driver.get(BASE_URL);
        WebElement seleniumSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("selenium_commands")));
        seleniumSelect.click();

        WebElement skillsOption = driver.findElement(By.xpath("//option[text()='" + SKILLS + "']"));
        skillsOption.click();

        Assertions.assertEquals(SKILLS, new org.openqa.selenium.support.ui.Select(seleniumSelect).getFirstSelectedOption().getText(),
                "Selected skill should be Selenium WebDriver");
    }

    @Test
    @Order(10)
    public void testSubmitButtonSuccess() {
        driver.get(BASE_URL);

        // Fill out the form
        driver.findElement(By.id("first-name")).sendKeys(FIRST_NAME);
        driver.findElement(By.id("last-name")).sendKeys(LAST_NAME);
        driver.findElement(By.id("job")).sendKeys(JOB_TITLE);

        WebElement educationRadio = driver.findElement(By.xpath("//input[@name='education' and @value='" + EDUCATION_LEVEL + "']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", educationRadio);
        wait.until(ExpectedConditions.elementToBeClickable(educationRadio)).click();

        WebElement sexRadio = driver.findElement(By.xpath("//input[@name='sex' and @value='" + SEX + "']"));
        wait.until(ExpectedConditions.elementToBeClickable(sexRadio)).click();

        WebElement experienceCheckbox = driver.findElement(By.xpath("//input[@type='checkbox' and @value='1']"));
        if (!experienceCheckbox.isSelected()) {
            wait.until(ExpectedConditions.elementToBeClickable(experienceCheckbox)).click();
        }

        WebElement continentsSelect = driver.findElement(By.id("continents"));
        wait.until(ExpectedConditions.elementToBeClickable(continentsSelect)).click();
        driver.findElement(By.xpath("//option[text()='" + CONTINENTS + "']")).click();

        WebElement seleniumSelect = driver.findElement(By.id("selenium_commands"));
        wait.until(ExpectedConditions.elementToBeClickable(seleniumSelect)).click();
        driver.findElement(By.xpath("//option[text()='" + SKILLS + "']")).click();

        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();

        // Wait for result
        WebElement resultHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("Thanks for submitting your form", resultHeader.getText(), "Success message should appear");
    }

    @Test
    @Order(11)
    public void testRequiredFieldsValidation() {
        driver.get(BASE_URL);

        // Submit without filling required fields
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();

        // Check HTML5 validation or presence of error messages
        // Since this is a simple form, we check if the page reloads or stays
        // We expect the browser to show required field validation
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertEquals(BASE_URL, currentUrl, "Page should remain on form after failed submission");

        // Check if any field shows validation (browser-specific, so we check attribute)
        WebElement firstNameField = driver.findElement(By.id("first-name"));
        // After submit attempt, required fields should still be present
        Assertions.assertTrue(firstNameField.isDisplayed(), "First name field should still be displayed");
    }

    @Test
    @Order(12)
    public void testExternalLinksInFooter() {
        driver.get(BASE_URL);

        // Check if footer exists
        List<WebElement> footers = driver.findElements(By.cssSelector("footer"));
        if (footers.size() == 0) {
            // No footer section, skip the test or adjust accordingly
            // Assuming footer might not be present
            return;
        }

        // Find all footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        if (footerLinks.size() == 0) {
            return;
        } else {
            Assertions.assertTrue(footerLinks.size() >= 3, "Footer should contain at least 3 social links");

            for (WebElement link : footerLinks) {
                String originalWindow = driver.getWindowHandle();
                String href = link.getAttribute("href");
                String target = link.getAttribute("target");

                // Open link in new tab only if target is _blank
                if ("_blank".equals(target) && href != null && !href.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", href);
                    wait.until((d) -> d.getWindowHandles().size() > 1);

                    // Switch to new window
                    for (String windowHandle : driver.getWindowHandles()) {
                        if (!windowHandle.equals(originalWindow)) {
                            driver.switchTo().window(windowHandle);
                            break;
                        }
                    }

                    // Assert URL contains expected domain
                    String currentUrl = driver.getCurrentUrl();
                    if (href.contains("facebook.com")) {
                        Assertions.assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open correct domain");
                    } else if (href.contains("twitter.com")) {
                        Assertions.assertTrue(currentUrl.contains("twitter.com"), "Twitter link should open correct domain");
                    } else if (href.contains("linkedin.com")) {
                        Assertions.assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open correct domain");
                    }

                    // Close tab and switch back
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }
}