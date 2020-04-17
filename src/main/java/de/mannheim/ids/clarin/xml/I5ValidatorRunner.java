package de.mannheim.ids.clarin.xml;

import picocli.CommandLine;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.concurrent.Callable;

@CommandLine.Command(mixinStandardHelpOptions = true, description = "process and validate XML files", versionProvider = VersionProvider.class)
public class I5ValidatorRunner  implements Callable<Integer> {

    private InputStream inputStream = System.in;

    @CommandLine.Option(names = { "-i",
            "--input" }, description = "input file, by default STDIN")
    private File inputFile;
    @CommandLine.Option(names = { "-d", "--dom" }, description = "use DOM instead of SAX")
    private boolean dom = false;
    @Override
    public Integer call() throws IOException, ParserConfigurationException {
        if (inputFile != null) {
            try {
                inputStream = new FileInputStream(inputFile);
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
    }}