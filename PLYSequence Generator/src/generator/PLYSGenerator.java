package generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

/**
 * Represents a PLYS file.
 *
 * @author Patrick Rafferty
 */
public class PLYSGenerator {
    /**
     * Prefix added to temp files.
     */
    static final String TEMP_PREFIX = "PLY_Gen";
    /**
     * plys suffix.
     */
    static final String PLYS_SUFFIX = ".plys";
    /**
     * A CRLF byte array.
     */
    private static final byte[] CRLF = {0x0D, 0x0A};
    /**
     * The temporary file used for storing the PLYS.
     */
    private final Path tempFile;
    /**
     * The total number of PLY files.
     */
    private final int fileCount;
    /**
     * The number of files processed.
     */
    private int numProcessed;
    /**
     * If the file has already been saved.
     */
    private boolean saved;
    
    /**
     * Creates a new PLYS.
     *
     * @param childFiles the PLY files to use to generate.
     * @param gzip       If GZIP compression should be used.
     * @param framerate  the framerate of the PLYS file.
     * @throws IOException if there was a problem reading or writing.
     */
    public PLYSGenerator(final File[] childFiles, final boolean gzip, final float framerate)
            throws IOException {
        fileCount = childFiles.length;
        tempFile = Files.createTempFile(TEMP_PREFIX, PLYS_SUFFIX + (gzip ? ".gz" : ""));
        Arrays.sort(childFiles);
        try (final OutputStream outputStream = makeOutputStream(tempFile, gzip)) {
            final PrintWriter printWriter =
                    new PrintWriter(outputStream, true, StandardCharsets.UTF_8);
            printWriter.println("plys");
            printWriter.println("framerate " + framerate);
            printWriter.println("end_sequence_header");
            for (final File file : childFiles) {
                try (final InputStream in = new FileInputStream(file)) {
                    outputStream.write(in.readAllBytes());
                    outputStream.write(CRLF);
                    numProcessed++;
                }
            }
            printWriter.close();
        }
    }
    
    /**
     * Creates an OutputStream that either goes straight to a file or is GZIP compressed first.
     *
     * @param tempFile The file to use for the stream.
     * @param gzip     if a GZIP stream should be used.
     * @return the OutputStream.
     * @throws IOException if there was an IO problem.
     */
    private static OutputStream makeOutputStream(final Path tempFile, final boolean gzip)
            throws IOException {
        OutputStream outputStream = new FileOutputStream(tempFile.toFile(), true);
        if (gzip) {
            // Note: FilterOutputStream closes its filtered stream with itself.
            outputStream = new GZIPOutputStream(outputStream);
        }
        return outputStream;
    }
    
    /**
     * Saves the PLYS to a new location.
     *
     * @param newFile the file to save to.
     * @throws IOException if there was a problem saving the file.
     */
    public void save(final File newFile) throws IOException {
        if (saved) {
            throw new IllegalStateException("File was already saved.");
        }
        Files.move(tempFile, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        saved = true;
    }
    
    public int getNumProcessed() {
        return numProcessed;
    }
    
    public int getFileCount() {
        return fileCount;
    }
}
