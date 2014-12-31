package tex61;

import java.io.PrintWriter;

/** A PageAssembler that sends lines immediately to a PrintWriter, with
 *  terminating newlines.
 *  @author Jason Qiu
 */
class PagePrinter extends PageAssembler {

    /** A new PagePrinter that sends lines to OUT. */
    PagePrinter(PrintWriter out) {
        super(out);
        _out = out;
    }

    /** Print LINE to my output. */
    @Override
    void write(String line) {
        _out.println(line);
    }

    /** PrintWriter to send output to. */
    private PrintWriter _out;
}
