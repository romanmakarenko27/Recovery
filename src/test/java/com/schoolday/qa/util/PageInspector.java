package com.schoolday.qa.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PageInspector {

    private static final Path CODES_FILE = Path.of("SchoolDay_reset_codes.txt");

    public static void main(String[] args) throws Exception {
        Properties config = loadProperties();

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            // Step 1: Login
            driver.get(config.getProperty("base.url") + config.getProperty("login.path"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username-input")));

            driver.findElement(By.id("username-input")).sendKeys(config.getProperty("test.email"));
            driver.findElement(By.id("password-input")).sendKeys(config.getProperty("test.password"));
            driver.findElement(By.id("log-in-button")).click();

            // Step 2: Wait for MFA page
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-testid='mfa-login-use-recovery-code-link']")));

            // Step 3: Click recovery code link
            driver.findElement(By.cssSelector("[data-testid='mfa-login-use-recovery-code-link']")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-testid='recovery-code-login-submit-btn']")));

            // Step 4: Enter recovery code and submit
            String code = consumeRecoveryCode();
            System.out.println("Using recovery code: " + code);
            driver.findElement(By.cssSelector("[data-testid='recovery-code-login-recovery-code-input']"))
                    .sendKeys(code);
            driver.findElement(By.cssSelector("[data-testid='recovery-code-login-submit-btn']")).click();

            // Step 5: Wait for /admin/institutions
            wait.until(d -> d.getCurrentUrl().contains("/admin/institutions"));
            System.out.println("Landed on: " + driver.getCurrentUrl());

            // Dump institutions page
            Files.writeString(Path.of("build/institutions-page.html"), driver.getPageSource());
            System.out.println("Institutions page saved to build/institutions-page.html");

            // Step 6: Navigate to user profile
            driver.get(config.getProperty("base.url") + "/user/profile");
            wait.until(ExpectedConditions.urlContains("/user/profile"));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[data-testid='user-profile-tab-group-tabs']")));

            Files.writeString(Path.of("build/profile-page.html"), driver.getPageSource());
            System.out.println("Profile page saved to build/profile-page.html");

            // Step 7: Click Security Settings tab using data-testid
            WebElement securityTab = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='user-profile-tab-security-settings']")));
            securityTab.click();

            // Wait for Security Settings content to render
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
            longWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("ngc-user-security-settings")));

            Files.writeString(Path.of("build/security-settings-page.html"), driver.getPageSource());
            System.out.println("Security settings page saved to build/security-settings-page.html");

            // Step 8: Look for Regenerate recovery codes button and click it
            // Search for buttons/links containing "Regenerate" or "recovery"
            List<WebElement> allButtons = driver.findElements(By.cssSelector("button, a"));
            WebElement regenButton = null;
            for (WebElement btn : allButtons) {
                String text = btn.getText().toLowerCase();
                if (text.contains("regenerate") || text.contains("recovery code")) {
                    regenButton = btn;
                    System.out.println("Found regenerate button: text='" + btn.getText()
                            + "', tag=" + btn.getTagName()
                            + ", class=" + btn.getAttribute("class")
                            + ", data-testid=" + btn.getAttribute("data-testid"));
                    break;
                }
            }

            if (regenButton != null) {
                regenButton.click();
                Thread.sleep(2000);

                Files.writeString(Path.of("build/regenerate-dialog.html"), driver.getPageSource());
                System.out.println("Regenerate dialog saved to build/regenerate-dialog.html");

                // Step 9: Try entering password and submitting
                var passwordInputs = driver.findElements(By.cssSelector(
                        "input[type='password']"));
                if (!passwordInputs.isEmpty()) {
                    passwordInputs.get(0).sendKeys(config.getProperty("test.password"));
                    System.out.println("Entered password in dialog");

                    // Look for confirm button in dialog
                    var dialogButtons = driver.findElements(By.cssSelector(
                            ".cdk-overlay-container button, mat-dialog-container button"));
                    for (WebElement btn : dialogButtons) {
                        String text = btn.getText().toLowerCase();
                        if (text.contains("confirm") || text.contains("generate") || text.contains("submit")) {
                            System.out.println("Found submit button: text='" + btn.getText()
                                    + "', data-testid=" + btn.getAttribute("data-testid"));
                            btn.click();
                            System.out.println("Clicked submit in dialog");
                            break;
                        }
                    }
                }

                Thread.sleep(5000);
                Files.writeString(Path.of("build/new-codes-page.html"), driver.getPageSource());
                System.out.println("New codes page saved to build/new-codes-page.html");
            } else {
                System.out.println("No regenerate button found - check security-settings-page.html");
            }

            System.out.println("Final URL: " + driver.getCurrentUrl());

        } finally {
            driver.quit();
        }
    }

    private static String consumeRecoveryCode() throws Exception {
        List<String> lines = new ArrayList<>(Files.readAllLines(CODES_FILE));

        String code = null;
        int codeIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isBlank() && !line.startsWith("USED:")) {
                code = line;
                codeIndex = i;
                break;
            }
        }

        if (code == null) {
            throw new IllegalStateException("No recovery codes remaining in " + CODES_FILE);
        }

        lines.set(codeIndex, "USED:" + code);
        Files.write(CODES_FILE, lines);

        return code;
    }

    private static Properties loadProperties() throws Exception {
        Properties props = new Properties();
        try (InputStream is = PageInspector.class.getClassLoader().getResourceAsStream("test.properties")) {
            if (is == null) {
                throw new RuntimeException("test.properties not found on classpath");
            }
            props.load(is);
        }
        return props;
    }
}
