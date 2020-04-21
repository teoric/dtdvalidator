package de.mannheim.ids.clarin.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;

@CommandLine.Command(mixinStandardHelpOptions = true, description = "process "
        + "and validate XML files", versionProvider = VersionProvider.class)
public class I5ValidatorRunner implements Callable<Integer> {

    static private final Logger logger = LoggerFactory
            .getLogger(I5Validator.class.getSimpleName());

    private enum Compression {
        none, bzip2, gzip, xz
    }

    @CommandLine.Option(names = { "-L",
            "--log-file" }, defaultValue = "i5validation.json", description = "log file name (default: ${DEFAULT-VALUE})")
    String logFileName;
    @CommandLine.Option(names = { "-p",
            "--parallel" }, description = "use multiple threads")
    private boolean parallel;

    @CommandLine.Option(names = { "-c",
            "--compression" }, description = "compression: ${COMPLETION-CANDIDATES}, default: "
                    + "${DEFAULT-VALUE} (overridden from file name!)", defaultValue = "none")
    Compression compression;
    @CommandLine.Option(names = { "-d", "--dom" }, description = "use DOM "
            + "instead of SAX")
    private boolean dom = false;
    @CommandLine.Option(names = { "-S",
            "--use-schema" }, description = "use XSD with DOM, ignore DTD (implies -d)")
    private boolean useSchema = false;
    @CommandLine.Option(names = { "-l",
            "--log-to-json" }, description = "collect errors "
                    + "and write log file")
    private boolean writeLog = false;
    @CommandLine.Parameters(arity = "1..*", description = "input files")
    private List<File> inputFiles = new ArrayList<>();

    @Override
    public Integer call() {
        I5Validator validator = new I5Validator(writeLog);
        AtomicInteger errorCount = new AtomicInteger();

        Stream<File> inputs = inputFiles.stream();
        if (parallel) {
            inputs = inputs.parallel();
        }
        inputs.forEach(inputFile -> {

            String name = inputFile.toString();
            switch (FilenameUtils.getExtension(name)) {
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

            try (InputStream originalInputStream = new FileInputStream(
                    inputFile)) {
                InputStream inputStream;
                switch (compression) {
                case xz:
                    inputStream = new XZCompressorInputStream(
                            originalInputStream);
                    break;
                case bzip2:
                    inputStream = new BZip2CompressorInputStream(
                            originalInputStream);
                    break;
                case gzip:
                    inputStream = new GZIPInputStream(originalInputStream);
                    break;
                case none:  // i.e.
                default:
                    inputStream = originalInputStream;
                    break;
                }
                boolean result;
                dom = dom || useSchema;
                logger.info("Validating {} using {}{}", name,
                        dom ? "DOM" : "SAX",
                        useSchema ? " and XSD from xsi:schemaLocation" : "");
                if (dom)
                    result = validator.validateWithDTDUsingDOM(inputStream,
                            name, useSchema);
                else
                    result = validator.validateWithDTDUsingSAX(inputStream,
                            name);
                if (result) {
                    logger.info("Document {} validated", name);
                } else {
                    logger.info("Document {} did not validate", name);
                    errorCount.getAndIncrement();
                }
            } catch (IOException | ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        });
        if (writeLog)
            validator.writeErrorMap(new File(logFileName));
        return errorCount.get();
    }

    /**
     * run CLI
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.exit(new CommandLine(new I5ValidatorRunner()).execute(args));
    }
}
