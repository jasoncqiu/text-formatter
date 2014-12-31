package tex61;

import java.util.List;

/** A PageAssembler that collects its lines into a designated List.
 *  @author Jason Qiu
 */
class PageCollector extends PageAssembler {

    /** A new PageCollector that stores lines in OUT. */
    PageCollector(List<String> out) {
        super(out);
        _out = out;
    }

    /** Add LINE to my List. */
    @Override
    void write(String line) {
        if (line != null) {
            if (!firstLine && _out.size() % textHeight == 0) {
                line = "\f" + line;
            }
            _out.add(line);
            firstLine = false;
        } else if (_out.size() % textHeight != 0) {
            _out.add("");
        }
    }

    /** Set text height to VAL, where VAL > 0. */
    void setTextHeight(int val) {
        textHeight = val;
    }

    /** List to send output to. */
    private List<String> _out;
    /** Whether the next line is the first of the document. */
    private boolean firstLine = true;
    /** Text height setting. */
    private int textHeight = Defaults.TEXT_HEIGHT;
}
