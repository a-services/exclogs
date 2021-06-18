package com.exadel.exclogs;

import static java.lang.System.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "exclogs", mixinStandardHelpOptions = true, version = "1.0")

/**
 * Get logs from `exc` utility.
 *
 * @see https://github.com/a-services/exc
 */
public class Main implements Callable<Integer> {

    final int PAGE_SIZE = 300;

    @Parameters(index = "0", description = "Log code.")
    String logCode;

    @Parameters(index = "1", description = "Server URL before /exc.")
    String serverUrl;

    @Option(names = { "-l1", "--start" }, description = "Start line.", defaultValue = "1")
    int startLine;

    @Option(names = { "-l2", "--end" }, description = "End line.", defaultValue = "300")
    int endLine;

    @Option(names = { "-o", "--out" }, description = "Output folder.", defaultValue = "exclogs")
    String outFolder;

    @Option(names = { "-c", "--cache" }, description = "Use cache.")
    boolean useCache;

    @Option(names = { "-p", "--plain" }, description = "Output log file in plain text format.")
    String plainLogFile;

    @Option(names = { "-v", "--verbose" }, description = "Verbose mode.")
    static boolean verbose;

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        out.println("-------------------------------------");

        /* If the folder for logs is not found, create it.
         */
        File outDir = new File(outFolder);
        if (!outDir.exists()) {
            boolean dirCreated = outDir.mkdirs();
            if (dirCreated) {
                out.println("Output folder created: " + outFolder);
            } else {
                out.println("[ERROR] Problem creating output folder: " + outFolder);
                return 1;
            }
        }

        PrintWriter plain = null;
        if (plainLogFile != null) {
            plain = new PrintWriter(new File(plainLogFile));
        }

        /* `exc` prints 300 lines ahead of the requested line
         * plus 20 lines back.
         */
        final int EXTRA = 20;

        int lastLine = -1;
        while (startLine < endLine) {

            /* Output file name and request URL.
             */
            String outFile = outFolder + "/" + logCode + "_" + tstamp(startLine-1) + ".log.html";
            startLine += EXTRA;
            String requestUrl = serverUrl + "/exc/block?name=" + logCode + "&lno=" + startLine;
            out.println("   Request: " + requestUrl);

            String text = null;
            if (useCache) {
                /* Let's try to find the query result in the cache,
                 * if this option is enabled.
                 */
                text = StrUtils.loadStr(outFile);
                if (text == null) {
                    useCache = false;
                } else {

                    /* File from the previous request to this URL was found.
                     */
                    StartEndLine se1 = new StartEndLine(text, logCode);
                    //out.println("se1: " + se1);
                    if (startLine + PAGE_SIZE == se1.end) {
                        out.println("   ...using cache");
                    } else {
                        text = null;
                    }
                }
            }
            if (text == null) {
                text = StrUtils.loadUrl(requestUrl);
            }

            /* If `exc` returned the line window shifted to the beginning of the file,
             * it means that we have reached the end of the file, and there is no need 
             * to continue downloading.
             */
            StrUtils.saveStr(outFile, text);
            StartEndLine se = new StartEndLine(text, logCode);

            if (plain != null) {
                plain.println(se.getPlainLines(lastLine));
                lastLine = se.end;
            }

            out.println("File saved: " + outFile + " " + se);
            if (startLine != se.start + EXTRA) {
                break;
            }
            startLine += PAGE_SIZE;
        }

        if (plain != null) {
            plain.close();
            out.println("File saved: " + plainLogFile);
        }
        out.println("-------------------------------------");
        return 0;
    }

    String tstamp(int lno) {
        return new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + "_" + String.format("%07d", lno);
    }
    
}
