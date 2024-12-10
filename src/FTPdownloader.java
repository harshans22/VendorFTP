import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileOutputStream;

public class FTPdownloader {
    public static void downloadFile(String ftpServer, int port, String username, String password, String remoteFilePath, String localFilePath) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpServer, port);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            FileOutputStream outputStream = new FileOutputStream(localFilePath);
            boolean success = ftpClient.retrieveFile(remoteFilePath, outputStream);
            outputStream.close();

            if (success) {
                System.out.println("File downloaded successfully.");
            } else {
                System.out.println("Failed to download the file.");
            }

            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
