package com.hp.workpath.pkgmgt.lib;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Utility class to handle HPK hpkFileHandle (creating and reading)
 */
public class HpkFile implements Closeable {
    static final Serializer SERIALIZER = new Persister(new AnnotationStrategy(),
            new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" standalone=\"yes\"?>"));

    public static final String HPK_EXTENSION = ".hpk";

    private final File hpkFileHandle;
    private JarFile hpkFileContent;
    private Connector connector;

    private Certificate[] certificates;

    /**
     * Opens HPK hpkFileHandle for reading using given hpkFileHandle
     * @param file HPK hpkFileHandle
     * @throws IOException if HPK archive is invalid or hpk.xml is missing or invalid
     *     or installFile is missing in the archive
     */
    public HpkFile(final File file) throws IOException {
        this.hpkFileHandle = file;
        open();
    }

    /**
     * Opens HPK hpkFileHandle for reading using given hpkFileHandle path
     * @param filePath hpkFileHandle path to HPK
     * @throws IOException if HPK archive is invalid or hpk.xml is missing or invalid
     *                     or installFile is missing in the archive
     */
    public HpkFile(final String filePath) throws IOException {
        this(new File(filePath));
    }

    /**
     * Extracts hpk.xml from HPK and deserialize as Connector object
     * @return connector
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * Returns opened stream with install file content
     * @return InputStream stream the install file to be read from
     * @throws IOException if install file stream cannot be opened
     */
    public InputStream getInstallFile() throws IOException {
        ZipEntry installedFileEntry = hpkFileContent.getEntry(connector.getInstallFile());
        return hpkFileContent.getInputStream(installedFileEntry);
    }

    /**
     * Returns size of install file
     * @return file size
     * @throws IOException if install file stream cannot be opened
     */
    public long getInstallFileSize() throws IOException {
        ZipEntry installedFileEntry = hpkFileContent.getEntry(connector.getInstallFile());
        return installedFileEntry.getSize();
    }

    /**
     * Returns opened stream with install file content
     * @return InputStream stream the install file to be read from
     * @throws IOException if install file stream cannot be opened
     */
    public InputStream getDefaultConfigFile() throws IOException {
        ZipEntry installedFileEntry = hpkFileContent.getEntry(connector.getDefaultConfig());
        return hpkFileContent.getInputStream(installedFileEntry);
    }

    public InputStream getXmlStream() throws IOException {
        ZipEntry connectorEntry = hpkFileContent.getEntry(Connector.XML_FILENAME);
        return hpkFileContent.getInputStream(connectorEntry);
    }

    public static <T> T parseMetadata(Class<? extends T> type, String metadata) throws Exception{
        return SERIALIZER.read(type, metadata);
    }

    /**
     * Certificates used for signing HPK
     * @return Certificate array
     */
    public Certificate[] getCertificates() {
        return certificates;
    }

    private void open() throws IOException, SecurityException {
        try {
            if (hpkFileHandle.isFile() && hpkFileHandle.exists() && hpkFileHandle.getName().toLowerCase().endsWith(HPK_EXTENSION)) {
                hpkFileContent = new JarFile(hpkFileHandle, true);
                checkValidity();

                ZipEntry connectorEntry = hpkFileContent.getEntry(Connector.XML_FILENAME);
                if (connectorEntry != null) {
                    try {
                        // hpk.xml exists
                        connector = SERIALIZER.read(Connector.class, hpkFileContent.getInputStream(connectorEntry));
                    } catch (Exception e) {
                        throw new IOException("Failed to parse " + Connector.XML_FILENAME, e);
                    }

                    // check is install file presented in HPK file
                    if (hpkFileContent.getEntry(connector.getInstallFile()) == null) {
                        throw new IOException("Install file with name " + connector.getInstallFile() + " is missing in HPK archive");
                    }
                } else {
                    throw new IOException("File " + Connector.XML_FILENAME + " is missing in HPK archive");
                }
            } else {
                throw new IOException("File " + hpkFileHandle.getAbsolutePath() + " is missing or not .hpk file");
            }
        } catch (Exception e) {
            if (hpkFileContent != null) {
                try {
                    hpkFileContent.close();
                } catch (IOException ignored) {
                }
            }

            throw e;
        }
    }

    private void checkValidity() throws IOException {
        // check all entries for correct signature
        Enumeration<JarEntry> entries = hpkFileContent.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            // just read entry
            InputStream is = hpkFileContent.getInputStream(entry);
            byte[] read = new byte[2000];
            while (is.read(read) > 0) {
                // read stream
            }
            if (certificates == null) {
                certificates = entry.getCertificates();
            }
        }
    }

    @Override
    public void close() throws IOException{
        if (hpkFileContent != null) {
            hpkFileContent.close();
        }
    }
}
