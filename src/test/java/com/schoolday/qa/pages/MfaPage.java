package com.schoolday.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MfaPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By heading = By.cssSelector("ngc-mfa-login div.title");
    private final By smsInfoText = By.cssSelector("[data-testid='mfa-login-hint-text']");
    private final By codeInput = By.cssSelector("[data-testid='mfa-login-verification-code-input']");
    private final By confirmButton = By.cssSelector("[data-testid='mfa-login-submit-btn']");
    private final By sendNewCodeButton = By.cssSelector("[data-testid='mfa-login-send-new-code-btn']");
    private final By backToSignInLink = By.cssSelector("[data-testid='mfa-login-back-to-credentials-btn']");
    private final By recoveryCodeLink = By.cssSelector("[data-testid='mfa-login-use-recovery-code-link']");
    private final By havingProblemsText = By.xpath("//h4[contains(text(),'Having problems')]");

    public MfaPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public MfaPage waitForPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(heading));
        return this;
    }

    public boolean isDisplayed() {
        try {
            return driver.findElement(heading).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getHeadingText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(heading)).getText();
    }

    public String getSmsInfoText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(smsInfoText)).getText();
    }

    public boolean isCodeInputDisplayed() {
        return driver.findElement(codeInput).isDisplayed();
    }

    public boolean isConfirmButtonDisplayed() {
        return driver.findElement(confirmButton).isDisplayed();
    }

    public boolean isSendNewCodeButtonDisplayed() {
        return driver.findElement(sendNewCodeButton).isDisplayed();
    }

    public String getSendNewCodeButtonText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(sendNewCodeButton)).getText();
    }

    public boolean isHavingProblemsTextDisplayed() {
        return driver.findElement(havingProblemsText).isDisplayed();
    }

    public boolean isRecoveryCodeLinkDisplayed() {
        return driver.findElement(recoveryCodeLink).isDisplayed();
    }

    public void clickRecoveryCodeLink() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(recoveryCodeLink));
        link.click();
    }

    public void clickBackToSignIn() {
        wait.until(ExpectedConditions.elementToBeClickable(backToSignInLink)).click();
    }
}
