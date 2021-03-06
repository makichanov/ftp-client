package by.bsuir.ftp;

import by.bsuir.ftp.proto_FtpClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class proto_Control {
    proto_FtpClient ftp;

    public proto_Control(String host,int port, String username, String password) {
        ftp = new proto_FtpClient(host, port, username, password);
    }

    public boolean connect() {
        try {
            ftp.connect();
            return true;
        } catch (IOException e) {
            System.err.println("Cannot connect to server.");
            return false;
        }
    }

    public void disconnect() {
        try {
            ftp.close();
        } catch (IOException e) {
            System.err.println("Cannot close connection with server.");
        }
    }

    public ArrayList<String> listDirectories(String path) {
        ArrayList<String> directories = new ArrayList<>();
        try {
            for (FTPFile file : ftp.listDirectories(path)) {
                directories.add(file.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return directories;
    }

    public ArrayList<FTPFile> listFiles(String path) {
        try {
            return new ArrayList<>(Arrays.asList(ftp.listFiles(path)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean retrieve(String retrievePath, String storePath) {
        try {
            ftp.retrieve(retrievePath, storePath);
            return true;
        } catch (IOException e) {
            System.err.println("Cannot retrieve file " + retrievePath + " (" + e.getLocalizedMessage() + ")");
            return false;
        }
    }

    public boolean retrieveDirectory(String retrievePath, String storePath) {
        try(ZipOutputStream out = new ZipOutputStream(new FileOutputStream(storePath))) {
            retrieveFilesForFolder(retrievePath, out);
            return true;
        } catch (IOException e) {
            System.err.println("Cannot retrieve directory " + retrievePath  + " to " + storePath);
            new File(storePath).delete();
            return false;
        }
    }

    private void retrieveFilesForFolder(String fileName, ZipOutputStream zipOut) throws IOException {
        for (final FTPFile fileEntry : ftp.listFiles(fileName)) {
            if (fileEntry.isDirectory()) {
                ZipEntry zipEntry = new ZipEntry(fileName + "/" + fileEntry.getName() + "/");
                zipOut.putNextEntry(zipEntry);
                zipOut.closeEntry();
                retrieveFilesForFolder(fileName + "/" + fileEntry.getName(), zipOut);
            } else {
                if(!fileEntry.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION)) {
                    throw new AccessDeniedException("Access denied to " + fileEntry.getName());
                }
                ZipEntry zipEntry = new ZipEntry(fileName + "/" + fileEntry.getName());
                zipOut.putNextEntry(zipEntry);
                InputStream dataStream = ftp.retrieve(fileName + "/" + fileEntry.getName());
                zipOut.write(dataStream.readAllBytes());
                zipOut.closeEntry();
            }
        }
    }

    public void store(File toStore, String storePath) {
        try {
            ftp.store(toStore, storePath);
        } catch (IOException e) {
            System.err.println("Cannot upload file to FTP: " + storePath);
        }
    }

    public void storeDirectory(File dir, String storePath) {
        try {
            ftp.makeDirectory(storePath);
            storeFilesForFolder(dir, storePath);
        } catch (IOException e) {
            System.err.println("Cannot make directory " + storePath);
        }
    }

    private void storeFilesForFolder(final File folder, String path) throws IOException {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                ftp.makeDirectory(path + "/" + fileEntry.getName());
                storeFilesForFolder(fileEntry, path + "/" + fileEntry.getName());
            } else {
                ftp.store(fileEntry, path + "/");
            }
        }
    }

    public boolean deleteFile(String path) {
        try {
            return ftp.deleteFile(path);
        } catch (IOException e) {
            System.err.println("Cannot delete file from FTP: " + path);
            return false;
        }
    }

    public boolean deleteDirectory(String path) {
        try {
            return ftp.deleteDirectory(path);
        } catch (IOException e) {
            System.err.println("Cannot delete directory from FTP: " + path);
            return false;
        }
    }

    public void makeDirectory(String path) {
        try {
            ftp.makeDirectory(path);
        } catch (IOException e) {
            System.err.println("Cannot create directory " + path);
        }
    }

    public int lastReply() {
        return ftp.lastReply();
    }

    public void setPassiveMode() {
        try {
            ftp.close();
            ftp.setPassiveWorkMode();
            ftp.connect();
        } catch (IOException e) {
            System.err.println("Error while switching between data modes");
        }
    }

    public void setActiveMode() {
        try {
            ftp.close();
            ftp.setActiveWorkMode();
            ftp.connect();
        } catch (IOException e) {
            System.err.println("Error while switching between data modes");
        }
    }

    public boolean isPassiveWorkMode() {
        return ftp.isPassiveWorkMode();
    }
}
