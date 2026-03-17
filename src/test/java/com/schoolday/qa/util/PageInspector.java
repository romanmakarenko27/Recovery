package com.schoolday.qa.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class PageInspector {
    public static void main(String[] args) throws Exception {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            // Step 1: Login
            driver.get("https://connect-qa.gg4l.com/login/vendor");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username-input")));

            driver.findElement(By.id("username-input")).sendKeys("claude_rm@test.test");
            driver.findElement(By.id("password-input")).sendKeys("1mM24%488PfY(*");
            driver.findElement(By.id("log-in-button")).click();

            // Step 2: Wait for MFA page to load
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-testid='mfa-login-use-recovery-code-link']")));

            // Step 3: Click recovery code link
            driver.findElement(By.cssSelector("[data-testid='mfa-login-use-recovery-code-link']")).click();
            Thread.sleep(3000);

            // Step 4: Click Confirm with empty field
            driver.findElement(By.cssSelector("[data-testid='recovery-code-login-submit-btn']")).click();
            Thread.sleep(2000);

            // Step 5: Dump page after empty submit
            String emptySubmitSource = driver.getPageSource();
            Files.writeString(Path.of("build/recovery-empty-submit.html"), emptySubmitSource);
            System.out.println("Empty submit page saved to build/recovery-empty-submit.html");

            // Step 6: Enter invalid code and submit
            driver.findElement(By.cssSelector("[data-testid='recovery-code-login-recovery-code-input']"))
                    .sendKeys("INVALIDCODE123");
            driver.findElement(By.cssSelector("[data-testid='recovery-code-login-submit-btn']")).click();
            Thread.sleep(3000);

            // Step 7: Dump page after invalid submit
            String invalidSubmitSource = driver.getPageSource();
            Files.writeString(Path.of("build/recovery-invalid-submit.html"), invalidSubmitSource);
            System.out.println("Invalid submit page saved to build/recovery-invalid-submit.html");
            System.out.println("Current URL: " + driver.getCurrentUrl());

        } finally {
            driver.quit();
        }
    }
}
