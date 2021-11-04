package seedu.programmer.ui;

import static seedu.programmer.model.Model.PREDICATE_SHOW_ALL_STUDENTS;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import seedu.programmer.commons.core.GuiSettings;
import seedu.programmer.commons.core.LogsCenter;
import seedu.programmer.commons.exceptions.IllegalValueException;
import seedu.programmer.commons.util.FileUtil;
import seedu.programmer.commons.util.JsonUtil;
import seedu.programmer.logic.Logic;
import seedu.programmer.logic.commands.CommandResult;
import seedu.programmer.logic.commands.DashboardCommandResult;
import seedu.programmer.logic.commands.DownloadCommandResult;
import seedu.programmer.logic.commands.ExitCommandResult;
import seedu.programmer.logic.commands.HelpCommandResult;
import seedu.programmer.logic.commands.ShowCommandResult;
import seedu.programmer.logic.commands.UploadCommandResult;
import seedu.programmer.logic.commands.exceptions.CommandException;
import seedu.programmer.logic.parser.exceptions.ParseException;
import seedu.programmer.model.ProgrammerError;
import seedu.programmer.model.student.Student;
import seedu.programmer.model.student.exceptions.DuplicateStudentException;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final Double NINETY_PERCENT = 0.9;


    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private StudentListPanel studentListPanel;
    private LabResultListPanel labResultListPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private DashboardWindow dashboardWindow;
    private Popup popup = new Popup();

    @FXML
    private Scene primaryScene;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private Button helpButton;

    @FXML
    private Button exitButton;

    @FXML
    private Button downloadButton;

    @FXML
    private Button uploadButton;

    @FXML
    private Button dashboardButton;

    @FXML
    private StackPane studentListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane labResultListPanelPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private SplitPane mainPanel;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage} and {@code Logic}.
     */
    public MainWindow(Stage primaryStage, Logic logic) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());
        setAccelerators();

        helpWindow = new HelpWindow();
        dashboardWindow = new DashboardWindow(logic);

    }


    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(KeyCombination.valueOf("F1"), this::handleExit);
        setAccelerator(KeyCombination.valueOf("F2"), this::handleHelp);
        setAccelerator(KeyCombination.valueOf("F3"), this::handleDownload);
        setAccelerator(KeyCombination.valueOf("F4"), this::handleUpload);
        setAccelerator(KeyCombination.valueOf("F5"), this::handleDashboard);
    }

    /**
     * Sets the accelerator of a button.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(KeyCombination keyCombination, Runnable runnable) {
        primaryScene.getAccelerators().put(keyCombination, runnable);
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        studentListPanel = new StudentListPanel(logic.getFilteredStudentList());
        studentListPanelPlaceholder.getChildren().add(studentListPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(logic.getProgrammerErrorFilePath());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() == null) {
            return;
        }

        primaryStage.setX(guiSettings.getWindowCoordinates().getX());
        primaryStage.setY(guiSettings.getWindowCoordinates().getY());
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (helpWindow.isShowing()) {
            helpWindow.focus();
            return;
        }
        logger.fine("Showing help window about the application.");
        helpWindow.show();
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application window.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
        dashboardWindow.hide();
    }

    /**
     * Display the selected student's lab results.
     */
    @FXML
    public void handleShowResult() {
        labResultListPanel = new LabResultListPanel(logic.getSelectedInformation());
        labResultListPanelPlaceholder.getChildren().add(labResultListPanel.getRoot());
        logger.fine("Showing student's lab results.");
    }

    /**
     * Display the dashboard.
     */
    @FXML
    private void handleDashboard() {
        dashboardWindow.update();
        if (dashboardWindow.isShowing()) {
            dashboardWindow.focus();
            return;
        }

        logger.fine("Showing dashboard window...");
        dashboardWindow.show();
    }

    /**
     * Uploads CSV data into ProgrammerError's model storage.
     */
    @FXML
    private void handleUpload() {
        File chosenFile = promptUserForCsvFile();
        if (chosenFile == null) {
            logger.info("User cancelled the file upload.");
            return;
        }

        List<Student> stuList;
        try {
            stuList = FileUtil.getStudentsFromCsv(chosenFile);
        } catch (IllegalArgumentException | IOException e) {
            displayPopup("Upload failed: " + e.getMessage()); // Error with file data
            return;
        } catch (IllegalValueException e) {
            displayPopup(e.getMessage()); // Error with file headers
            return;
        }

        ProgrammerError newPE = new ProgrammerError();
        try {
            newPE.setStudents(stuList);
        } catch (DuplicateStudentException e) {
            displayPopup("Upload failed: " + e.getMessage());
            return;
        }
        saveDataState(newPE);
    }

    private void saveDataState(ProgrammerError newPE) {
        logic.updateProgrammerError(newPE);
        logic.updateFilteredStudents(PREDICATE_SHOW_ALL_STUDENTS);
        logic.saveProgrammerError(newPE);
        logger.info("Uploaded CSV data successfully!");
    }

    /**
     * Downloads the JSON data as a CSV file to the user's chosen directory.
     */
    @FXML
    private void handleDownload() {
        JSONArray jsonData = JsonUtil.getJsonData("data/programmerError.json");
        assert (jsonData != null);

        if (jsonData.length() == 0) {
            displayPopup("No data to download!");
            return;
        }

        File destinationFile = promptUserForDestination();
        if (destinationFile != null) {
            JsonUtil.writeJsonToCsv(jsonData, destinationFile);
            displayPopup("Your data has been downloaded to " + destinationFile + "!");
            logger.info("Data successfully downloaded as CSV.");
        }
    }

    /**
     * Displays a popup message at the top-center with respect to the primaryStage.
     *
     * @param message to be displayed in the popup object on the primaryStage
     */
    private void displayPopup(String message) {
        // We should not need to display an empty popup
        assert (message != null);
        double tenPercent = 1 - NINETY_PERCENT;
        configurePopup(message);

        // Add some left padding according to primaryStage's width
        popup.setX(primaryStage.getX() + primaryStage.getWidth() * tenPercent / 2);

        // Set Y coordinate scaled according to primaryStage's height
        popup.setY(primaryStage.getY() + primaryStage.getHeight() * tenPercent);
        popup.show(primaryStage);
    }

    /**
     * Creates a Popup object with a message.
     *
     * @param message The text to display to the user
     */
    private void configurePopup(String message) {
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);
        Label label = createLabelForPopup(message);
        popup.getContent().clear();
        popup.getContent().add(label);
    }

    private Label createLabelForPopup(String message) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(primaryStage.getWidth() * NINETY_PERCENT);
        label.getStyleClass().add("popup-label");

        // Hide popup when the user clicks on it
        label.setOnMouseReleased(e -> popup.hide());
        return label;
    }

    /**
     * Configures file chooser to accept only CSV files.
     *
     * @param fileChooser FileChooser object
     */
    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("Select CSV file");
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("All CSVs", "*.csv");
        fileChooser.getExtensionFilters().add(csvFilter);
    }

    /**
     * Shows user a dialog to choose a CSV file.
     *
     * @return chosen CSV file
     */
    public File promptUserForCsvFile() {
        FileChooser fileChooser = new FileChooser();
        configureFileChooser(fileChooser);
        return fileChooser.showOpenDialog(primaryStage);
    }

    /**
     * Creates a File object based on user's chosen directory.
     *
     * @return File object with a file name appended to the chosen directory
     */
    public File promptUserForDestination() {
        String destFileName = "programmerError.csv";
        DirectoryChooser dirChooser = new DirectoryChooser();
        File chosenDir = dirChooser.showDialog(primaryStage);
        return chosenDir == null ? null : new File(chosenDir, destFileName);
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.programmer.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());

            if (commandResult instanceof HelpCommandResult) {
                handleHelp();
            } else if (commandResult instanceof ExitCommandResult) {
                handleExit();
            } else if (commandResult instanceof ShowCommandResult) {
                handleShowResult();
            } else if (commandResult instanceof DownloadCommandResult) {
                handleDownload();
            } else if (commandResult instanceof DashboardCommandResult) {
                handleDashboard();
            } else if (commandResult instanceof UploadCommandResult) {
                handleUpload();
            }
            dashboardWindow.update();
            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("Invalid command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }

    @FXML
    private void handleHover() {
        exitButton.setStyle("-fx-background-color: -fx-light-bg-color;");
    }

    @FXML
    private void handleUnhover() {
        exitButton.setStyle("-fx-background-color: -fx-main-bg-color;");
    }
}
