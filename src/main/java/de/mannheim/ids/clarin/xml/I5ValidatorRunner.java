package de.mannheim.ids.clarin.xml;

import org.apache.commons.compress.archivers.dump.DumpArchiveConstants;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import picocli.CommandLine;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

@CommandLine.Command(mixinStandardHelpOptions = true, description = "process " +
        "and validate XML files", versionProvider = VersionProvider.class)
public class I5ValidatorRunner implements Callable<Integer> {

    private InputStream inputStream = System.in;

    private enum Compression {
        none, bzip2, gzip, xz
    }

    @CommandLine.Option(names = {"-c", "--compression"}, description =
            "compression: ${COMPLETION-CANDIDATES}, default: " +
                    "${DEFAULT-VALUE}", defaultValue = "none")
    Compression compression;
    @CommandLine.Option(names = {"-i",
            "--input"}, description = "input file, by default STDIN")
    private File inputFile;
    @CommandLine.Option(names = {"-d", "--dom"}, description = "use DOM " +
            "instead of SAX")
    private boolean dom = false;

    @Override
    public Integer call() throws IOException, ParserConfigurationException {
        if (inputFile != null) {
            System.err.format("Validating %s\n", inputFile.toString());
            switch (FilenameUtils.getExtension(inputFile.toString())) {
                case "xz":
                    compression = Compression.xz;
                    break;
                case "bz2":
                case "bzip2":
                    compression = Compression.bzip2;
                    break;
                case "gz":
                case "gzip":
                    compression = Compression.gzip;
                    break;
            }
            try {
                inputStream = new FileInputStream(inputFile);
                switch (compression) {
                     case xz:
                        inputStream = new XZCompressorInputStream(inputStream);
                        break;
                    case bzip2:
                        inputStream =
                                new BZip2CompressorInputStream(inputStream);
                        break;
                    case gzip:
                        inputStream = new GZIPInputStream(inputStream);
                        break;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        boolean result;
        if (dom)
            result = I5Validator.validateWithDTDUsingDOM(inputStream);
        else
            result = I5Validator.validateWithDTDUsingSAX(inputStream);
        if (result) {
            System.err.println("Document validated");
            return 0;
        } else {
            System.err.println("Document did not validate");
            return 1;
        }
    }

    /**
     * run CLI
     *
     * @param args
     */
    public static void main(String[] args) {
        System.exit(new CommandLine(new I5ValidatorRunner()).execute(args));
    }
}