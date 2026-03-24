package com.schoolday.qa.tests;

import com.schoolday.qa.base.BaseTest;
import com.schoolday.qa.pages.LoginPage;
import com.schoolday.qa.pages.MfaPage;
import com.schoolday.qa.pages.RecoveryCodePage;
import com.schoolday.qa.pages.RegenerateCodesDialog;
import com.schoolday.qa.pages.UserProfilePage;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegenerateCodeTest extends BaseTest {

    private LoginPage loginPage;
    private MfaPage mfaPage;
    private RecoveryCodePage recoveryCodePage;
    private UserProfilePage userProfilePage;
    private RegenerateCodesDialog regenerateCodesDialog;

    @Override
    @BeforeEach
    protected void setUp() {
        // no-op — shared browser session
    }

    @Override
    @AfterEach
    protected void tearDown() {
        // no-op — shared browser session
    }

    @BeforeAll
    void initDriver() throws Exception {
        super.setUp();
        loginPage = new LoginPage(driver);
        mfaPage = new MfaPage(driver);
        recoveryCodePage = new RecoveryCodePage(driver);
        userProfilePage = new UserProfilePage(driver);
        regenerateCodesDialog = new RegenerateCodesDialog(driver);
    }

    @AfterAll
    void closeDriver() {
        super.tearDown();
    }

    @Test
    @Order(1)
    @DisplayName("Login with valid credentials and navigate to MFA")
    void testLoginAndNavigateToMfa() {
        loginPage.open(getBaseUrl());
        loginPage.login(getEmail(), getPassword());
        mfaPage.waitForPage();

        assertTrue(mfaPage.isDisplayed(), "MFA page should be displayed after login");
    }

    @Test
    @Order(2)
    @DisplayName("Navigate to recovery code page")
    void testNavigateToRecoveryCodePage() {
        mfaPage.clickRecoveryCodeLink();
        recoveryCodePage.waitForPage();

        assertTrue(recoveryCodePage.isDisplayed(), "Recovery Code page should be displayed");
    }

    @Test
    @Order(3)
    @DisplayName("Submit valid recovery code and login successfully")
    void testSuccessfulRecoveryCodeLogin() throws Exception {
        String recoveryCode = consumeRecoveryCode();

        recoveryCodePage.enterRecoveryCode(recoveryCode);
        recoveryCodePage.clickConfirm();

        wait.until(d -> d.getCurrentUrl().contains("/admin/institutions"));

        assertEquals("https://connect-qa.gg4l.com/admin/institutions", driver.getCurrentUrl(),
                "Should redirect to /admin/institutions after successful recovery code login");
    }

    @Test
    @Order(4)
    @DisplayName("Navigate to My Profile via user menu")
    void testNavigateToMyProfile() {
        userProfilePage.clickUserMenu();
        userProfilePage.clickMyProfile();

        assertTrue(driver.getCurrentUrl().contains("/user/profile"),
                "Should navigate to user profile page");
        assertTrue(userProfilePage.isTabGroupDisplayed(), "Tab group should be visible");
        assertTrue(userProfilePage.isProfileTabDisplayed(), "Profile tab should be visible");
        assertTrue(userProfilePage.isSecuritySettingsTabDisplayed(), "Security Settings tab should be visible");
        assertTrue(userProfilePage.isEmailNotificationsTabDisplayed(), "Email Notifications tab should be visible");
        assertTrue(userProfilePage.isEditOrganizationTabDisplayed(), "Edit Organization tab should be visible");
    }

    @Test
    @Order(5)
    @DisplayName("Navigate to Security Settings and click Regenerate Recovery Codes")
    void testNavigateToSecuritySettingsAndOpenDialog() {
        userProfilePage.clickSecuritySettingsTab();

        assertTrue(userProfilePage.isSecuritySettingsContentLoaded(),
                "Security settings content should be loaded");

        regenerateCodesDialog.clickRegenerateButton();

        assertTrue(regenerateCodesDialog.isDialogDisplayed(),
                "Regenerate Recovery Codes dialog should be displayed");
    }

    @Test
    @Order(6)
    @DisplayName("Verify Regenerate Recovery Codes dialog elements")
    void testRegenerateDialogElements() {
        assertTrue(regenerateCodesDialog.isDialogDisplayed(), "Dialog should be displayed");
        assertTrue(regenerateCodesDialog.isPasswordInputDisplayed(), "Password input should be visible");
        assertTrue(regenerateCodesDialog.isRegenerateButtonDisplayed(), "Regenerate button should be visible");
        assertTrue(regenerateCodesDialog.isCancelButtonDisplayed(), "Cancel button should be visible");

        String dialogText = regenerateCodesDialog.getDialogText();
        assertTrue(dialogText.contains("Regenerate Recovery Codes"),
                "Dialog should contain title 'Regenerate Recovery Codes'");
        assertTrue(dialogText.contains("enter your password"),
                "Dialog should contain password instruction text");
        assertTrue(dialogText.contains("Old codes won't work after generating new ones"),
                "Dialog should contain warning about old codes");
    }

    @Test
    @Order(7)
    @DisplayName("Regenerate button is disabled with empty password")
    void testRegenerateButtonDisabledWithEmptyPassword() {
        assertFalse(regenerateCodesDialog.isRegenerateButtonEnabled(),
                "Regenerate button should be disabled when password is empty");
    }

    @Test
    @Order(8)
    @DisplayName("Password visibility toggle shows and hides password")
    void testPasswordVisibilityToggle() {
        regenerateCodesDialog.enterPassword("TestPassword");

        assertEquals("password", regenerateCodesDialog.getPasswordInputType(),
                "Password input should be masked by default");

        regenerateCodesDialog.clickPasswordToggle();

        assertEquals("text", regenerateCodesDialog.getPasswordInputType(),
                "Password should be visible after clicking toggle");

        regenerateCodesDialog.clickPasswordToggle();

        assertEquals("password", regenerateCodesDialog.getPasswordInputType(),
                "Password should be masked again after clicking toggle");
    }

    @Test
    @Order(9)
    @DisplayName("Invalid password shows error")
    void testInvalidPasswordSubmission() {
        regenerateCodesDialog.enterPassword("WrongPassword123!");
        regenerateCodesDialog.clickRegenerate();

        assertTrue(regenerateCodesDialog.hasSnackbarError(),
                "Error should be shown for invalid password");
        assertTrue(regenerateCodesDialog.isSnackbarAtBottom(),
                "Snackbar should appear at the bottom of the page");

        String errorMessage = regenerateCodesDialog.getSnackbarErrorMessage();
        assertFalse(errorMessage.isBlank(), "Error message should not be blank");

        regenerateCodesDialog.dismissSnackbar();
    }

    @Test
    @Order(10)
    @DisplayName("Cancel button closes dialog")
    void testCancelClosesDialog() {
        regenerateCodesDialog.clickCancel();
        regenerateCodesDialog.waitForDialogClosed();

        assertFalse(regenerateCodesDialog.isDialogDisplayed(),
                "Dialog should be closed after clicking Cancel");
    }
}
