package tex61;

import java.io.PrintWriter;
import java.util.ArrayList;

/** Receives (partial) words and commands, performs commands, and
 *  accumulates and formats words into lines of text, which are sent to a
 *  designated PageAssembler.  At any given time, a Controller has a
 *  current word, which may be added to by addText, a current list of
 *  words that are being accumulated into a line of text, and a list of
 *  lines of endnotes.
 *  @author Jason Qiu
 */
class Controller {

    /** A new Controller that sends formatted output to OUT. */
    Controller(PrintWriter out) {
        _out = out;
    }

    /** Add TEXT to the end of the word of formatted text currently
     *  being accumulated. */
    void addText(String text) {
        lineAssembler.addText(text);
    }

    /** Finish any current word of text and, if present, add to the
     *  list of words for the next line.  Has no effect if no unfinished
     *  word is being accumulated. */
    void endWord() {
        lineAssembler.finishWord();
    }

    /** Finish any current word of formatted text and process an end-of-line
     *  according to the current formatting parameters. */
    void addNewline() {
        lineAssembler.newLine();
    }

    /** Finish any current word of formatted text, format and output any
     *  current line of text, and start a new paragraph. */
    void endParagraph() {
        lineAssembler.endParagraph();
    }

    /** If valid, process TEXT into an endnote, first appending a reference
     *  to it to the line currently being accumuated. */
    void formatEndnote(String text) {
        addText("[" + _refNum + "]");
        text = "[" + _refNum + "]\\ " + text;
        _refNum += 1;
        _endnotes.add(text);
    }

    /** Set the current text height (number of lines per page) to VAL, if
     *  it is a valid setting.  Ignored when accumulating an endnote. */
    void setTextHeight(int val) {
        lineAssembler.setTextHeight(val);
    }

    /** Set the current text width (width of lines including indentation)
     *  to VAL, if it is a valid setting. */
    void setTextWidth(int val) {
        lineAssembler.setTextWidth(val);
    }

    /** Set the current text indentation (number of spaces inserted before
     *  each line of formatted text) to VAL, if it is a valid setting. */
    void setIndentation(int val) {
        lineAssembler.setIndentation(val);
    }

    /** Set the current paragraph indentation (number of spaces inserted before
     *  first line of a paragraph in addition to indentation) to VAL, if it is
     *  a valid setting. */
    void setParIndentation(int val) {
        lineAssembler.setParIndentation(val);
    }

    /** Set the current paragraph skip (number of blank lines inserted before
     *  a new paragraph, if it is not the first on a page) to VAL, if it is
     *  a valid setting. */
    void setParSkip(int val) {
        lineAssembler.setParSkip(val);
    }

    /** Iff ON, begin filling lines of formatted text. */
    void setFill(boolean on) {
        lineAssembler.setFill(on);
    }

    /** Iff ON, begin justifying lines of formatted text whenever filling is
     *  also on. */
    void setJustify(boolean on) {
        lineAssembler.setJustify(on);
    }

    /** Finish the current formatted document or endnote (depending on mode).
     *  Formats and outputs all pending text. */
    void close() {
        writeEndnotes();
        PageAssembler pagePrinter = new PagePrinter(_out);
        for (String line : _lines) {
            pagePrinter.addLine(line);
        }
    }

    /** Write all accumulated endnotes to _mainText. */
    private void writeEndnotes() {
        lineAssembler.setFill(true);
        lineAssembler.setJustify(true);
        lineAssembler.setParSkip(Defaults.ENDNOTE_PARAGRAPH_SKIP);
        lineAssembler.setIndentation(Defaults.ENDNOTE_INDENTATION);
        lineAssembler.setParIndentation(Defaults.ENDNOTE_PARAGRAPH_INDENTATION);
        lineAssembler.setTextWidth(Defaults.ENDNOTE_TEXT_WIDTH);
        for (String s : _endnotes) {
            InputParser endnoteParser = new InputParser(s, this);
            endnoteParser.processEndnote();
        }
    }

    /** Number of next endnote. */
    private int _refNum = 1;
    /** PrinterWriter to send output to. */
    private PrintWriter _out;
    /** List of endnotes. */
    private ArrayList<String> _endnotes = new ArrayList<String>();
    /** List of formatted lines. */
    private ArrayList<String> _lines = new ArrayList<String>();
    /** This controller's PageColletor. */
    private PageAssembler pageCollector = new PageCollector(_lines);
    /** This controller's LineAssembler. */
    private LineAssembler lineAssembler = new LineAssembler(pageCollector);


}

