package com.exadel.exclogs;

import static java.lang.System.*;
import static com.exadel.exclogs.Main.verbose;

/**
 * We split the file received from `exc`
 * with signatures for url at the beginning of the line
 * and determine the numbers of the starting and ending lines of the log on the page.
 */
public class StartEndLine {

    // Number of the first line of the log on the page.
    int start = -1;

    // The number of the last line of the log on the page.
    int end   = -1;

    // Page text, split into lines, for later output to `plainLogFile`
    String[] a;

    StartEndLine(String text, String logCode) {
        final String SEP = "<a class=\"num nounder\" href=\"block\\?name=" + logCode + "&lno=";
        a = text.split(SEP);
        //out.println("split length: " + a.length);
        if (a.length > 1) {
            start = extractLineNo(a[1]);
            end = extractLineNo(a[a.length - 1]);
        }
    }

    int extractLineNo(String s) {
        int k = s.indexOf("\"");
        String t = s.substring(0, k);
        return Integer.parseInt(t);
    }

    /**
     * Save the log in plain text format.
     */
    String getPlainLines(int lastLine) {
        final String SEP_1 = "</a>";
        final String SEP_2 = "<br>";
        StringBuilder sb = new StringBuilder();
        int i0 = Math.max(1, lastLine - start + 3);
        if (verbose) {
            out.println("i0: " + i0);
        }
        for (int i = i0; i < a.length; i++) {
            if (verbose) {
                out.println("---\n" + a[i] + "\n===");
            }
            int k1 = a[i].indexOf(SEP_1);
            int k2 = a[i].lastIndexOf(SEP_2);
            assert k1 != -1;
            assert k2 != -1;
            sb.append(a[i].substring(k1 + SEP_1.length(), k2).trim()
                .replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&amp;", "&"))
                .append('\n');
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "[start=" + start + ", end=" + end + "]";
    }
    
}
