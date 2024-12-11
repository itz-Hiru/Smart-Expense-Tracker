package com.hiru.smartexpensetracker;

import com.hiru.smartexpensetracker.controllers.MySQLFinanceRequest;
import com.hiru.smartexpensetracker.models.Transaction;
import com.hiru.smartexpensetracker.views.FinanceTracker;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FinanceTrackerGUI extends Application {
    private final FinanceTracker financeTracker = new FinanceTracker();
    @FXML
    private final TableView<Transaction> transactionTable = new TableView<>();
    @FXML
    private final PieChart pieChart = createPieChart();

    private final File dataFile = new File("src/main/java/com/hiru/smartexpensetracker/utils/data.txt");

    private static final Logger logger = Logger.getLogger("FinanceTrackerLog");

    static {
        try {
            FileHandler fh = new FileHandler("src/main/java/com/hiru/smartexpensetracker/utils/finance_tracker_log.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("AI-Powered Smart Expense Tracker");

        logger.info("FinanceTrackerGUI application started successfully.");

        if (!dataFile.exists() || dataFile.length() == 0) {
            logger.warning("Data file is empty or does not exist. Starting the application without data.");
            showWarningDialog();
        }

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(createMenuBar());

        VBox vBox = new VBox();
        vBox.getChildren().addAll(createTransactionTable(), pieChart);
        borderPane.setCenter(vBox);

        try {
            if (dataFile.exists() && dataFile.length() > 0) {
                loadDataFromFile(dataFile);
            } else {
                showWarningDialog();
            }

            File cssFile = new File("src/main/resources/css/styles.css");
            String cssPath = cssFile.toURI().toURL().toExternalForm();
            borderPane.getStylesheets().add(cssPath);
        } catch (Exception e) {
            showErrorDialog("Error loading files: " + e.getMessage());
        }

        Scene scene = new Scene(borderPane, 800, 600);

        addFadeInAnimation(scene);

        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save data before quitting?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                saveDataToFile(dataFile, financeTracker.getTransactions());
            } else if (alert.getResult() == ButtonType.CANCEL) {
                event.consume();
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addFadeInAnimation(Scene scene) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), scene.getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private MenuBar createMenuBar() {
        logger.info("Creating Menu Bar...");
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem saveMenuItem = new MenuItem("Save");
        saveMenuItem.setOnAction(e -> {
            logger.info("User clicked Save menu item.");
            saveToFile();
        });
        MenuItem loadMenuItem = new MenuItem("Load");
        loadMenuItem.setOnAction(e -> {
            logger.info("User clicked Load menu item.");
            loadFromFile();
        });
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(e -> {
            logger.info("User clicked Exit menu item. Exiting the application...");
            System.exit(0);
        });
        fileMenu.getItems().addAll(saveMenuItem, loadMenuItem, exitMenuItem);

        Menu transactionMenu = new Menu("Transaction");
        MenuItem addTransactionMenuItem = new MenuItem("Add Transaction");
        addTransactionMenuItem.setOnAction(e -> {
            logger.info("User clicked Add Transaction menu item.");
            showAddTransactionDialog();
        });
        MenuItem updatePieChartMenuItem = new MenuItem("Update Pie Chart");
        updatePieChartMenuItem.setOnAction(e -> {
            logger.info("User clicked Update Pie Chart menu item.");
            updatePieChart();
        });
        transactionMenu.getItems().addAll(addTransactionMenuItem, updatePieChartMenuItem);

        Menu viewMenu = new Menu("View Summary");
        MenuItem summaryByDayMenuItem = new MenuItem("View by Day");
        summaryByDayMenuItem.setOnAction(e -> {
            logger.info("User clicked View by Day menu item.");
            showSummary("day");
        });
        MenuItem summaryByMonthMenuItem = new MenuItem("View by Month");
        summaryByMonthMenuItem.setOnAction(e -> {
            logger.info("User clicked View by Month menu item.");
            showSummary("month");
        });
        MenuItem summaryByYearMenuItem = new MenuItem("View by Year");
        summaryByYearMenuItem.setOnAction(e -> {
            logger.info("User clicked View by Year menu item.");
            showSummary("year");
        });
        viewMenu.getItems().addAll(summaryByDayMenuItem, summaryByMonthMenuItem, summaryByYearMenuItem);

        Menu budgetMenu = new Menu("Set Budget");
        MenuItem setMonthlyBudgetMenuItem = new MenuItem("Set Monthly Budget");
        setMonthlyBudgetMenuItem.setOnAction(e -> {
            logger.info("User clicked Set Monthly Budget menu item.");
            showSetBudgetDialog("monthly");
        });
        MenuItem setYearlyBudgetMenuItem = new MenuItem("Set Yearly Budget");
        setYearlyBudgetMenuItem.setOnAction(e -> {
            logger.info("User clicked Set Monthly Budget menu item.");
            showSetBudgetDialog("yearly");
        });
        MenuItem removeMonthlyBudgetMenuItem = new MenuItem("Remove Monthly Budget");
        removeMonthlyBudgetMenuItem.setOnAction(e -> removeMonthlyBudget());
        MenuItem removeYearlyBudgetMenuItem = new MenuItem("Remove Yearly Budget");
        removeYearlyBudgetMenuItem.setOnAction(e -> removeYearlyBudget());
        budgetMenu.getItems().addAll(setMonthlyBudgetMenuItem, setYearlyBudgetMenuItem,
                removeMonthlyBudgetMenuItem, removeYearlyBudgetMenuItem);

        Menu aboutMenu = new Menu("About");
        MenuItem showTipsMenuItem = new MenuItem("Show Financial Tips");
        showTipsMenuItem.setOnAction(e -> {
            logger.info("User clicked Show Financial Tips menu item.");
            showFinanceTipDialog();
        });
        MenuItem aboutAppMenuItem = new MenuItem("About App");
        aboutAppMenuItem.setOnAction(e -> {
            logger.info("User clicked About App menu item.");
            showAboutAppDialog();
        });
        aboutMenu.getItems().addAll(showTipsMenuItem, aboutAppMenuItem);

        menuBar.getMenus().addAll(fileMenu, budgetMenu, transactionMenu, viewMenu, aboutMenu);

        logger.info("Menu Bar created successfully.");

        return menuBar;
    }

    public TableView<Transaction> createTransactionTable() {
        logger.info("Creating Transaction Table...");
        TableColumn<Transaction, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());

        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        TableColumn<Transaction, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());

        TableColumn<Transaction, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());

        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> {
            SimpleObjectProperty<Date> dateProperty = cellData.getValue().dateProperty();
            return new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy").format(dateProperty.get()));
        });

        TableColumn<Transaction, Void> deleteColumn = new TableColumn<>("Delete");
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    deleteTransaction(transaction);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        typeColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.15));
        descriptionColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.2));
        amountColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.15));
        categoryColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.15));
        dateColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.15));
        deleteColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.1));

        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        transactionTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            boolean allColumnsEmpty = typeColumn.getWidth() == 0 &&
                    descriptionColumn.getWidth() == 0 &&
                    amountColumn.getWidth() == 0 &&
                    categoryColumn.getWidth() == 0 &&
                    dateColumn.getWidth() == 0 &&
                    deleteColumn.getWidth() == 0;

            logger.info("Transaction table width changed. Setting column visibility...");
            typeColumn.setVisible(!allColumnsEmpty);
            descriptionColumn.setVisible(!allColumnsEmpty);
            amountColumn.setVisible(!allColumnsEmpty);
            categoryColumn.setVisible(!allColumnsEmpty);
            dateColumn.setVisible(!allColumnsEmpty);
            deleteColumn.setVisible(!allColumnsEmpty);
        });

        logger.info("Transaction Table created successfully.");

        transactionTable.getColumns().addAll(typeColumn, categoryColumn, descriptionColumn, amountColumn, dateColumn, deleteColumn);

        return transactionTable;
    }

    private PieChart createPieChart() {
        logger.info("Creating Pie Chart...");
        PieChart pieChart = new PieChart();
        pieChart.setId("pieChart");
        pieChart.setTitle("Transaction Types");

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        updatePieChartData(pieChartData);
        pieChart.setData(pieChartData);

        financeTracker.transactionsProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("Transactions have changed. Updating Pie Chart data...");
            updatePieChartData(pieChartData);
        });

        logger.info("Pie Chart created successfully.");

        return pieChart;
    }

    private void updatePieChartData(ObservableList<PieChart.Data> pieChartData) {
        logger.info("Updating Pie Chart data...");
        pieChartData.clear();

        double totalIncome = financeTracker.getTransactions().stream()
                .filter(transaction -> "income".equalsIgnoreCase(transaction.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpense = financeTracker.getTransactions().stream()
                .filter(transaction -> "expense".equalsIgnoreCase(transaction.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalAmount = totalIncome + totalExpense;
        double incomePercentage = (totalIncome / totalAmount) * 100;
        double expensePercentage = (totalExpense / totalAmount) * 100;

        logger.info("Total Income: " + totalIncome);
        logger.info("Total Expense: " + totalExpense);
        logger.info("Income Percentage: " + String.format("%.2f%%", incomePercentage));
        logger.info("Expense Percentage: " + String.format("%.2f%%", expensePercentage));

        pieChartData.add(new PieChart.Data("Income (" + String.format("%.2f%%", incomePercentage) + ")", totalIncome));
        pieChartData.add(new PieChart.Data("Expense (" + String.format("%.2f%%", expensePercentage) + ")", totalExpense));
    }

    private void showAddTransactionDialog() {
        logger.info("Opening Add Transaction dialog...");
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Add Transaction");
        dialog.setHeaderText("Enter transaction details");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Income", "Expense");
        typeComboBox.setPromptText("Select Type (Income/Expense)");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        descriptionField.setPromptText("Add description here");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setPromptText("Add amount here");

        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("Salary or Wages", "Bonuses", "Investment Incomes", "Retirement Incomes", "Social Security Benefits", "Housing", "Transportation", "Food", "Clothing and Personal Care", "Education", "Entertainment");
        categoryComboBox.getItems().addAll(financeTracker.getAllCategories());
        categoryComboBox.setEditable(true);
        categoryComboBox.setPromptText("Select or type category");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Set date");

        StringConverter<LocalDate> converter = new StringConverter<>() {
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };

        datePicker.setConverter(converter);
        datePicker.setPromptText("Set date");

        grid.add(new Label("Type : "), 0, 0);
        grid.add(typeComboBox, 1, 0);
        grid.add(new Label("Category : "), 0, 1);
        grid.add(categoryComboBox, 1, 1);
        grid.add(new Label("Amount : "), 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(new Label("Date : "), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Description : "), 0, 4);
        grid.add(descriptionField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                try {
                    String selectedType = typeComboBox.getValue();
                    String description = descriptionField.getText();
                    double amount = Double.parseDouble(amountField.getText());
                    String category = categoryComboBox.getValue();
                    String dateString = datePicker.getEditor().getText();

                    logger.info("Adding transaction - Type: " + selectedType + ", Description: " + description + ", Amount: " + amount + ", Category: " + category + ", Date: " + dateString);

                    financeTracker.addTransaction(selectedType, description, amount, category, dateString);
                    refreshTransactionTable();
                    logger.info("Transaction added successfully.");
                } catch (ParseException e) {
                    logger.warning("Error parsing date: " + e.getMessage());
                    showErrorDialog("Error parsing date. Please enter a valid date in the format dd/MM/yyyy.");
                } catch (NumberFormatException e) {
                    logger.warning("Error parsing amount: " + e.getMessage());
                    showErrorDialog("Please enter a valid amount.");
                }
            }
            return null;
        });

        logger.info("Showing Add Transaction dialog...");
        dialog.showAndWait();
    }

    private void refreshTransactionTable() {
        logger.info("Refreshing Transaction Table...");
        ObservableList<Transaction> transactionData = FXCollections.observableArrayList(financeTracker.getTransactions());
        transactionTable.setItems(transactionData);
        logger.info("Transaction Table refreshed successfully.");
    }

    private void saveToFile() {
        logger.info("User clicked the Save button.");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Financial Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File selectedFile = fileChooser.showSaveDialog(null);

        if (selectedFile != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
                oos.writeObject(financeTracker.getTransactions());
                logger.info("Data saved to file: " + selectedFile.getAbsolutePath());
            } catch (IOException e) {
                logger.severe("Error saving data to file: " + e.getMessage());
                showErrorDialog("Error saving data to file: " + e.getMessage());
            }
        }
    }

    private void loadFromFile() {
        logger.info("User clicked the Load button.");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Financial Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            logger.info("User selected file: " + selectedFile.getAbsolutePath());

            if (showConfirmationDialog()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile))) {
                    financeTracker.setTransactions((ArrayList<Transaction>) ois.readObject());
                    logger.info("Data loaded from file: " + selectedFile.getAbsolutePath());
                    refreshTransactionTable();
                } catch (IOException | ClassNotFoundException e) {
                    logger.severe("Error loading data from file: " + e.getMessage());
                    showErrorDialog("Error loading data from file: " + e.getMessage());
                }
            }
        }
    }

    private void showErrorDialog(String message) {
        logger.warning("An error occurred: " + message);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmationDialog() {
        logger.info("Showing confirmation dialog.");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Loading data from a file will replace the current data. Continue?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == ButtonType.OK) {
                logger.info("User confirmed the action.");
                return true;
            } else {
                logger.info("User canceled the action.");
                return false;
            }
        } else {
            logger.warning("Confirmation dialog closed without response.");
            return false;
        }
    }

    private void updatePieChart() {
        logger.info("Updating Pie Chart...");

        updatePieChartData(pieChart.getData());

        logger.info("Pie Chart updated successfully.");
    }

    public void showSummary(String interval) {
        logger.info("Showing summary for interval: " + interval);

        String title;
        String message = switch (interval) {
            case "day" -> {
                title = "Day Summary";
                yield showSummaryDialog("Enter Date (dd/MM/yyyy):", "Day");
            }
            case "month" -> {
                title = "Month Summary";
                yield showSummaryDialog("Enter Month (MM/yyyy):", "Month");
            }
            case "year" -> {
                title = "Year Summary";
                yield showSummaryDialog("Enter Year (yyyy):", "Year");
            }
            default -> {
                title = "Invalid Interval";
                yield "Invalid interval selected.";
            }
        };

        logger.info("Summary dialog shown: " + title);
        showAlert(title, message);
    }

    private String showSummaryDialog(String prompt, String intervalType) {
        logger.info("Showing summary dialog for " + intervalType);

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter " + intervalType);
        dialog.setHeaderText(prompt);
        dialog.setContentText(intervalType + ":");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String userInput = result.get();
            logger.info("User input received for " + intervalType + ": " + userInput);

            switch (intervalType) {
                case "Day" -> {
                    Date enteredDate = parseDate(userInput, "dd/MM/yyyy");
                    return generateSummaryByDay(enteredDate);
                }
                case "Month" -> {
                    Date startDate = parseDate(userInput + "/01", "MM/yyyy/dd");
                    Calendar calendar = Calendar.getInstance();
                    if (startDate != null) {
                        calendar.setTime(startDate);
                    }
                    calendar.add(Calendar.MONTH, 1);
                    Date endDate = new Date(calendar.getTimeInMillis() - 1);
                    return generateSummaryByMonth(startDate, endDate);
                }
                case "Year" -> {
                    int enteredYear = Integer.parseInt(userInput);
                    return generateSummaryByYear(enteredYear);
                }
                default -> throw new IllegalArgumentException("Invalid interval type");
            }
        } else {
            logger.info(intervalType + " Summary: User canceled the operation.");
            return intervalType + " Summary: User canceled the operation.";
        }
    }

    private Date parseDate(String dateString, String format) {
        logger.info("Parsing date: " + dateString);

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            Date parsedDate = dateFormat.parse(dateString);
            logger.info("Date parsed successfully: " + parsedDate);
            return parsedDate;
        } catch (ParseException e) {
            logger.warning("Error parsing date: " + e.getMessage());
            showErrorDialog("Error parsing date. Please enter a valid date.");
            return null;
        }
    }

    private String generateSummaryByDay(Date enteredDate) {
        logger.info("Generating day summary for date: " + enteredDate);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Double[]> daySummaryMap = new HashMap<>();

        for (Transaction transaction : financeTracker.getTransactions()) {
            Date transactionDate = transaction.getDate();

            if (transactionDate.equals(enteredDate)) {
                String formattedDate = dateFormat.format(transactionDate);

                logger.finest("Processing transaction for date: " + formattedDate);

                if (!daySummaryMap.containsKey(formattedDate)) {
                    daySummaryMap.put(formattedDate, new Double[]{0.0, 0.0});
                }

                if ("income".equalsIgnoreCase(transaction.getType())) {
                    daySummaryMap.get(formattedDate)[0] += transaction.getAmount();
                } else if ("expense".equalsIgnoreCase(transaction.getType())) {
                    daySummaryMap.get(formattedDate)[1] += transaction.getAmount();
                }
            }
        }

        logger.fine("Day summary generated successfully.");

        return formatSummaryMessage("Day Summary", daySummaryMap);
    }

    private String generateSummaryByMonth(Date startDate, Date endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");
        logger.info("Generating month summary for period: " + dateFormat.format(startDate) + " to " + dateFormat.format(endDate));

        Map<String, Double[]> monthSummaryMap = new HashMap<>();

        for (Transaction transaction : financeTracker.getTransactions()) {
            Date transactionDate = transaction.getDate();

            if (transactionDate.after(startDate) && transactionDate.before(endDate)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(transactionDate);
                String formattedDate = dateFormat.format(calendar.getTime());

                logger.finest("Processing transaction for date: " + formattedDate);

                if (!monthSummaryMap.containsKey(formattedDate)) {
                    monthSummaryMap.put(formattedDate, new Double[]{0.0, 0.0});
                }

                if ("income".equalsIgnoreCase(transaction.getType())) {
                    monthSummaryMap.get(formattedDate)[0] += transaction.getAmount();
                } else if ("expense".equalsIgnoreCase(transaction.getType())) {
                    monthSummaryMap.get(formattedDate)[1] += transaction.getAmount();
                }
            }
        }

        logger.fine("Month summary generated successfully.");

        return formatSummaryMessage("Month Summary", monthSummaryMap);
    }

    private String generateSummaryByYear(int enteredYear) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        logger.info("Generating year summary for year: " + enteredYear);

        Map<String, Double[]> yearSummaryMap = new HashMap<>();

        for (Transaction transaction : financeTracker.getTransactions()) {
            Date transactionDate = transaction.getDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(transactionDate);
            int transactionYear = calendar.get(Calendar.YEAR);

            if (transactionYear == enteredYear) {
                String formattedDate = dateFormat.format(transactionDate);

                logger.finest("Processing transaction for date: " + formattedDate);

                if (!yearSummaryMap.containsKey(formattedDate)) {
                    yearSummaryMap.put(formattedDate, new Double[]{0.0, 0.0});
                }

                if ("income".equalsIgnoreCase(transaction.getType())) {
                    yearSummaryMap.get(formattedDate)[0] += transaction.getAmount();
                } else if ("expense".equalsIgnoreCase(transaction.getType())) {
                    yearSummaryMap.get(formattedDate)[1] += transaction.getAmount();
                }
            }
        }

        logger.fine("Year summary generated successfully.");

        return formatSummaryMessage("Year Summary for " + enteredYear, yearSummaryMap);
    }

    private String formatSummaryMessage(String summaryTitle, Map<String, Double[]> summaryMap) {
        logger.info("Formatting summary message: " + summaryTitle);

        StringBuilder summaryMessage = new StringBuilder(summaryTitle + ":\n");
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        for (Map.Entry<String, Double[]> entry : summaryMap.entrySet()) {
            String date = entry.getKey();
            double totalIncome = entry.getValue()[0];
            double totalExpense = entry.getValue()[1];

            logger.finest("Formatting summary for date: " + date);

            summaryMessage.append(date).append(": Income - $").append(decimalFormat.format(totalIncome))
                    .append(", Expense - $").append(decimalFormat.format(totalExpense)).append("\n");
        }

        logger.fine("Summary message formatted successfully.");

        return summaryMessage.toString();
    }

    private void showAlert(String title, String message) {
        logger.info("Showing alert: " + title + " - " + message);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void deleteTransaction(Transaction transaction) {
        logger.info("Deleting transaction: " + transaction.getDescription() + " - Amount: $" + transaction.getAmount());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this transaction?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            financeTracker.removeTransaction(transaction);
            refreshTransactionTable();
            updatePieChart();

            logger.info("Transaction deleted successfully.");
        } else {
            logger.info("Transaction deletion canceled by user.");
        }
    }

    private void showSetBudgetDialog(String interval) {
        logger.info("Opening Set Budget dialog for " + interval + " budget.");

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Set Budget");
        dialog.setHeaderText("Enter the budget amount");

        ButtonType setButton = new ButtonType("Set", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(setButton, ButtonType.CANCEL);

        TextField budgetField = new TextField();
        budgetField.setPromptText("Enter budget amount");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Budget Amount:"), 0, 0);
        grid.add(budgetField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == setButton) {
                try {
                    return Double.parseDouble(budgetField.getText());
                } catch (NumberFormatException e) {
                    showErrorDialog("Please enter a valid budget amount.");
                }
            }
            return null;
        });

        Optional<Double> result = dialog.showAndWait();

        if (result.isPresent()) {
            double budgetAmount = result.get();
            if ("monthly".equals(interval)) {
                financeTracker.setMonthlyBudget(budgetAmount);
                checkMonthlyBudgetWarning();
            } else if ("yearly".equals(interval)) {
                financeTracker.setYearlyBudget(budgetAmount);
                checkYearlyBudgetWarning();
            }

            showAlert("Budget Set Successfully", "The budget has been set successfully.");
            logger.info("Budget set successfully: " + budgetAmount);
        } else {
            showAlert("Budget Not Set", "The operation was canceled. The budget remains unchanged.");
            logger.info("Budget setting canceled by user.");
        }
    }

    private void checkMonthlyBudgetWarning() {
        logger.info("Checking monthly budget warning.");

        double monthlyBudget = financeTracker.getMonthlyBudget();
        double monthlyBalance = calculateMonthlyBalance();

        if (monthlyBalance < monthlyBudget) {
            showAlert("Budget Warning", "Monthly balance is below the set budget amount.");
            logger.warning("Monthly balance is below the set budget amount: Balance = " + monthlyBalance + ", Budget = " + monthlyBudget);
        } else {
            logger.info("Monthly balance is within the set budget amount: Balance = " + monthlyBalance + ", Budget = " + monthlyBudget);
        }
    }

    private void checkYearlyBudgetWarning() {
        logger.info("Checking yearly budget warning.");

        double yearlyBudget = financeTracker.getYearlyBudget();
        double yearlyBalance = calculateYearlyBalance();

        if (yearlyBalance < yearlyBudget) {
            showAlert("Budget Warning", "Yearly balance is below the set budget amount.");
            logger.warning("Yearly balance is below the set budget amount: Balance = " + yearlyBalance + ", Budget = " + yearlyBudget);
        } else {
            logger.info("Yearly balance is within the set budget amount: Balance = " + yearlyBalance + ", Budget = " + yearlyBudget);
        }
    }

    private double calculateMonthlyBalance() {
        logger.info("Calculating monthly balance.");

        LocalDate currentDate = LocalDate.now();
        LocalDate firstDayOfMonth = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);

        logger.info("Date range for calculation: " + firstDayOfMonth + " to " + lastDayOfMonth);

        List<Transaction> monthlyTransactions = financeTracker.getTransactions().stream()
                .filter(transaction -> isWithinDateRange(transaction.getDate(), firstDayOfMonth, lastDayOfMonth))
                .toList();

        double monthlyIncome = monthlyTransactions.stream()
                .filter(transaction -> "income".equalsIgnoreCase(transaction.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double monthlyExpense = monthlyTransactions.stream()
                .filter(transaction -> "expense".equalsIgnoreCase(transaction.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double monthlyBalance = monthlyIncome - monthlyExpense;
        logger.info("Monthly income: $" + monthlyIncome + ", Monthly expense: $" + monthlyExpense + ", Monthly balance: $" + monthlyBalance);

        return monthlyBalance;
    }

    private boolean isWithinDateRange(Date date, LocalDate startDate, LocalDate endDate) {
        logger.info("Checking if date " + date + " is within the range from " + startDate + " to " + endDate);

        LocalDate transactionDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        boolean withinRange = !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);

        if (withinRange) {
            logger.info("Date " + date + " is within the range from " + startDate + " to " + endDate);
        } else {
            logger.info("Date " + date + " is NOT within the range from " + startDate + " to " + endDate);
        }

        return withinRange;
    }

    private double calculateYearlyBalance() {
        logger.info("Calculating yearly balance.");

        LocalDate currentDate = LocalDate.now();
        LocalDate firstDayOfYear = LocalDate.of(currentDate.getYear(), Month.JANUARY, 1);
        LocalDate lastDayOfYear = firstDayOfYear.plusYears(1).minusDays(1);

        logger.info("Date range for calculation: " + firstDayOfYear + " to " + lastDayOfYear);

        List<Transaction> yearlyTransactions = financeTracker.getTransactions().stream()
                .filter(transaction -> isWithinDateRange(transaction.getDate(), firstDayOfYear, lastDayOfYear))
                .toList();

        double yearlyIncome = yearlyTransactions.stream()
                .filter(transaction -> "income".equalsIgnoreCase(transaction.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double yearlyExpense = yearlyTransactions.stream()
                .filter(transaction -> "expense".equalsIgnoreCase(transaction.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double yearlyBalance = yearlyIncome - yearlyExpense;
        logger.info("Yearly income: $" + yearlyIncome + ", Yearly expense: $" + yearlyExpense + ", Yearly balance: $" + yearlyBalance);

        return yearlyBalance;
    }

    private void showBudgetRemovedDialog(String budgetType) {
        logger.info("Showing dialog to inform that " + budgetType + " has been removed successfully.");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Budget Removed");
        alert.setHeaderText(null);
        alert.setContentText(budgetType + " has been removed successfully.");
        alert.showAndWait();
    }

    private void removeMonthlyBudget() {
        logger.info("Attempting to remove monthly budget.");

        if (financeTracker.getMonthlyBudget() > 0) {
            financeTracker.removeMonthlyBudget();
            checkMonthlyBudgetWarning();
            logger.info("Monthly budget successfully removed.");
            showBudgetRemovedDialog("Monthly Budget");
        } else {
            logger.warning("No monthly budget to remove.");
            showErrorDialog("No Monthly Budget to Remove");
        }
    }

    private void removeYearlyBudget() {
        logger.info("Attempting to remove yearly budget.");

        if (financeTracker.getYearlyBudget() > 0) {
            financeTracker.removeYearlyBudget();
            checkYearlyBudgetWarning();
            logger.info("Yearly budget successfully removed.");
            showBudgetRemovedDialog("Yearly Budget");
        } else {
            logger.warning("No yearly budget to remove.");
            showErrorDialog("No Yearly Budget to Remove");
        }
    }

    private void showFinanceTipDialog() {
        logger.info("Attempting to retrieve a finance tip.");

        String financeTip = MySQLFinanceRequest.generateFinanceQuote();
        if (financeTip != null && !financeTip.isEmpty()) {
            logger.info("Finance tip successfully retrieved.");
            showAlert("Financial Tip", financeTip);
        } else {
            logger.warning("Failed to retrieve finance tip.");
            showErrorDialog("Failed to retrieve finance tip. Please try again later.");
        }
    }

    private static void showAboutAppDialog() {
        logger.info("Showing 'About App' dialog.");

        Alert aboutDialog = new Alert(Alert.AlertType.INFORMATION);
        aboutDialog.setTitle("About !");

        Text headerText = new Text("Welcome to Smart Expense Tracker");
        headerText.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 16));
        headerText.setFill(createRainbowGradient());

        aboutDialog.getDialogPane().setHeader(headerText);

        aboutDialog.setContentText("This is a creative and innovative application designed to make financial management enjoyable and efficient. "
                + "With user-friendly features and helpful tips, we strive to empower you in achieving your financial goals. "
                + "Thank you for choosing our App!");

        aboutDialog.showAndWait();
    }

    private static LinearGradient createRainbowGradient() {
        Stop[] stops = new Stop[]{
                new Stop(0, Color.RED),
                new Stop(0.17, Color.ORANGE),
                new Stop(0.33, Color.YELLOW),
                new Stop(0.5, Color.GREEN),
                new Stop(0.67, Color.BLUE),
                new Stop(0.83, Color.INDIGO),
                new Stop(1.0, Color.VIOLET)
        };

        return new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
    }

    private void loadDataFromFile(File dataFile) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            ArrayList<Transaction> loadedTransactions = (ArrayList<Transaction>) ois.readObject();
            financeTracker.setTransactions(loadedTransactions);
            refreshTransactionTable();
            logger.info("Transactions loaded successfully from file: " + dataFile.getAbsolutePath());
        } catch (IOException | ClassNotFoundException e) {
            logger.severe("Error loading transactions from file: " + e.getMessage());
            showErrorDialog("Error loading data from file: " + e.getMessage());
        }
    }

    private void saveDataToFile(File dataFile, List<Transaction> transactions) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(transactions);
            logger.info("Data saved successfully to file: " + dataFile.getAbsolutePath());
            showInfoDialog("Data saved successfully to: " + dataFile.getAbsolutePath());
        } catch (IOException e) {
            logger.severe("Error saving data to file: " + e.getMessage());
            showErrorDialog("Error saving data to file: " + e.getMessage());
        }
    }

    private void showInfoDialog(String message) {
        String defaultMsg = "Operation completed successfully.";
        if (message == null || message.isEmpty()) {
            message = defaultMsg;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        logger.info("Info dialog displayed: " + message);
    }

    private void showWarningDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("Data file is empty or does not exist. Starting the application without data.");
        alert.showAndWait();

        logger.warning("Warning dialog displayed: Data file is empty or does not exist.");
    }
}