

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;



public class VendorImpl extends Vendor {

    @Override
    public void readProductData() {
        String localFilePath = AppConstants.localFilePath;
        File file = new File(localFilePath);

        // Check if the file already exists
        if (!file.exists()) {
            // Only download if the file doesn't exist
            System.out.println("File not found locally, downloading...");
            FTPdownloader.downloadFile(
                AppConstants.FTP_SERVER, AppConstants.FTP_PORT,
                AppConstants.FTP_USER, AppConstants.FTP_PASSWORD,
                AppConstants.REMOTE_FILE_PATH, localFilePath
            );
        } else {
            System.out.println("File already exists, skipping download.");
        }

        try {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS Vendor ("
                + "SKU VARCHAR(50) PRIMARY KEY, "
                + "Barcode VARCHAR(50), "
                + "QuantityInStock INT, "
                + "ETA VARCHAR(50), "
                + "Price DOUBLE, "
                + "SalesPrice DOUBLE, "
                + "SalesStartDate VARCHAR(50), "
                + "SalesEndDate VARCHAR(50), "
                + "Title VARCHAR(255), "
                + "CategoryID INT, "
                + "CategoryName VARCHAR(255), "
                + "MSRP DOUBLE, "
                + "MAP DOUBLE, "
                + "ShippingCost DOUBLE, "
                + "CanadaShippingCost DOUBLE, "
                + "Active BOOLEAN"
                + ")";

            Connection connection = DriverManager.getConnection(AppConstants.DB_URL, AppConstants.DB_USER, AppConstants.DB_PASSWORD);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(localFilePath))));
            Statement statement = connection.createStatement();
            statement.executeUpdate(createTableSQL);
            connection.setAutoCommit(false);
            System.out.println("Table 'services.Vendor' created successfully.");

            String insertSQL = "INSERT INTO Vendor (SKU, Barcode, QuantityInStock, ETA, Price, SalesPrice, "
                + "SalesStartDate, SalesEndDate, Title, CategoryID, CategoryName, MSRP, MAP, ShippingCost, "
                + "CanadaShippingCost, Active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);

            String line;
            int count = 0;
            int batchSize = 10000;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t");
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                // Check if fields array has at least 16 elements
                if (fields.length <= 15) {

                    preparedStatement.setString(1, fields[0]);  // SKU
                    preparedStatement.setString(2, fields[1]);  // Barcode (optional)
                    preparedStatement.setInt(3, !fields[2].isEmpty() ? Integer.parseInt(fields[2]) : 0);  // QuantityInStock
                    preparedStatement.setString(4, fields[3]);  // ETA (optional)

                    // Parse Price (handle empty strings)
                    preparedStatement.setDouble(5, parseDoubleOrDefault(fields[4], 0.0));  // Price
                    preparedStatement.setDouble(6, parseDoubleOrDefault(fields[5], 0.0));  // SalesPrice

                    preparedStatement.setString(7, !fields[6].isEmpty() ? fields[6] : null);  // SalesStartDate (optional)
                    preparedStatement.setString(8, !fields[7].isEmpty() ?  fields[7] : null);  // SalesEndDate (optional)
                    preparedStatement.setString(9, fields[8]);  // Title (optional)
                    preparedStatement.setInt(10, !fields[9].isEmpty() ? Integer.parseInt(fields[9]) : 0);  // CategoryID
                    preparedStatement.setString(11, fields[10]);  // CategoryName (optional)
                    preparedStatement.setDouble(12, parseDoubleOrDefault(fields[11], 0.0));  // MSRP
                    preparedStatement.setDouble(13, parseDoubleOrDefault(fields[12], 0.0));  // MAP
                    preparedStatement.setDouble(14, parseDoubleOrDefault(fields[13], 0.0));  // ShippingCost
                    preparedStatement.setDouble(15, parseDoubleOrDefault(fields[14], 0.0));  // CanadaShippingCost
                    preparedStatement.setBoolean(16, fields.length > 15 && !fields[15].isEmpty() && Boolean.parseBoolean(fields[15]));// Active
                    preparedStatement.addBatch();
                    if(++count % batchSize == 0) {
                        preparedStatement.executeBatch();
                    }
                }
            }
            preparedStatement.executeBatch();
            connection.commit();
            connection.close();
            System.out.println("Product data inserted successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private double parseDoubleOrDefault(String value, double defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;  // Return default value if the string is empty
        }
        try {
            return Double.parseDouble(value);  // Try parsing the value
        } catch (NumberFormatException ex) {
            return defaultValue;  // Return default value in case of invalid number format
        }
    }


    @Override
    public void readInventoryData() {
    }
}
