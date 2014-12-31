package tex61;

import java.util.ArrayList;

/** An object that receives a sequence of words of text and formats
 *  the words into filled and justified text lines that are sent to a receiver.
 *  @author Jason Qiu
 */
class LineAssembler {

    /** A new, empty line assembler with default settings of all
     *  parameters, sending finished lines to PAGES. */
    LineAssembler(PageAssembler pages) {
        _pages = pages;
    }

    /** Add TEXT to the word currently being built. */
    void addText(String text) {
        _word += text;
    }

    /** Finish the current word, if any, and add to words being accumulated. */
    void finishWord() {
        if (!_word.equals("")) {
            int lineSize = 0;
            for (String s : _words) {
                lineSize += s.length();
            }
            int numBlanks =
                    textWidth - (indentation + lineSize + _word.length());
            if (newParagraph || firstLine) {
                numBlanks = textWidth - (indentation + paragraphIndentation
                        + lineSize + _word.length());
            }
            if (numBlanks < _words.size() && fillMode) {
                outputLine(false);
            }
            addWord(_word);
            _word = "";
        }
    }

    /** Add WORD to the formatted text. */
    void addWord(String word) {
        _words.add(word);
    }

    /** Add LINE to our output, with no preceding paragraph skip.  There must
     *  not be an unfinished line pending. */
    void addLine(String line) {
        if (!_words.isEmpty()) {
            _pages.addLine(line);
        }
    }

    /** Set the current indentation to VAL. VAL >= 0. */
    void setIndentation(int val) {
        indentation = val;
    }

    /** Set the current paragraph indentation to VAL. VAL >= 0. */
    void setParIndentation(int val) {
        paragraphIndentation = val;
    }

    /** Set the text width to VAL, where VAL >= 0. */
    void setTextWidth(int val) {
        textWidth = val;
    }

    /** Iff ON, set fill mode. */
    void setFill(boolean on) {
        fillMode = on;
    }

    /** Iff ON, set justify mode (which is active only when filling is
     *  also on). */
    void setJustify(boolean on) {
        justifyMode = on;
    }

    /** Set paragraph skip to VAL.  VAL >= 0. */
    void setParSkip(int val) {
        paragraphSkip = val;
    }

    /** Set page height to VAL > 0. */
    void setTextHeight(int val) {
        _pages.setTextHeight(val);
    }

    /** Process the end of the current input line.  No effect if
     *  current line accumulator is empty or in fill mode.  Otherwise,
     *  adds a new complete line to the finished line queue and clears
     *  the line accumulator. */
    void newLine() {
        if (!fillMode && !_words.isEmpty()) {
            addWord(_word);
            _word = "";
            outputLine(true);
        }
    }

    /** If there is a current unfinished paragraph pending, close it
     *  out and start a new one. */
    void endParagraph() {
        outputLine(true);
        newParagraph = true;
    }

    /** Transfer contents of _words to _pages, adding INDENT characters of
     *  indentation, and a total of SPACES spaces between words, evenly
     *  distributed.  Assumes _words is not empty.  Clears _words and _chars. */
    private void emitLine(int indent) {
        String output = "";
        for (int i = 0; i < indent; i += 1) {
            output += " ";
        }
        int lineSize = 0;
        for (String s : _words) {
            lineSize += s.length();
        }
        int B = textWidth - (indent + lineSize);
        if  (B >= 3 * (_words.size() - 1)) {
            for (int i = 0; i < _words.size() - 1; i += 1) {
                _words.set(i, _words.get(i) + "   ");
            }
        } else {
            int totalSpaces = 0;
            for (int k = 1; k < _words.size(); k += 1) {
                int condition =
                        (int) (0.5 + (double) (B * k) / (_words.size() - 1));
                for (; totalSpaces < condition; totalSpaces += 1) {
                    _words.set(k - 1, _words.get(k - 1) + " ");
                }
            }
        }
        for (String s : _words) {
            output += s;
        }
        _pages.addLine(output);
        _words.clear();
    }

    /** If the line accumulator is non-empty, justify its current
     *  contents, if needed, add a new complete line to _pages,
     *  and clear the line accumulator. LASTLINE indicates the last line
     *  of a paragraph. */
    private void outputLine(boolean lastLine) {
        if (!_words.isEmpty()) {
            if (!fillMode || !justifyMode || lastLine) {
                String output = "";
                if (firstLine) {
                    for (int i = 0; i < indentation + paragraphIndentation;
                            i += 1) {
                        output += " ";
                    }
                } else if (newParagraph) {
                    for (int i = 0; i < indentation + paragraphIndentation;
                            i += 1) {
                        output += " ";
                    }
                    for (int i = 0; i < paragraphSkip; i += 1) {
                        _pages.addLine(null);
                    }
                    newParagraph = false;
                } else {
                    for (int i = 0; i < indentation; i += 1) {
                        output += " ";
                    }
                }
                for (int i = 0; i < _words.size() - 1; i += 1) {
                    _words.set(i, _words.get(i) + " ");
                }
                for (String s : _words) {
                    output += s;
                }
                _pages.addLine(output);
                _words.clear();
            } else {
                if (firstLine) {
                    emitLine(indentation + paragraphIndentation);
                } else if (newParagraph) {
                    for (int i = 0; i < paragraphSkip; i += 1) {
                        _pages.addLine(null);
                    }
                    emitLine(indentation + paragraphIndentation);
                    newParagraph = false;
                } else {
                    emitLine(indentation);
                }
            }
        }
        firstLine = false;
    }

    /** Destination given in constructor for formatted lines. */
    private PageAssembler _pages;
    /** Whether the next line is the first line. */
    private boolean firstLine = true;
    /** Whether the next line is a new paragraph. */
    private boolean newParagraph = false;
    /** To fill or not to fill. */
    private boolean fillMode = true;
    /** To justify or not to justify. */
    private boolean justifyMode = true;
    /** indentation setting. */
    private int indentation = Defaults.INDENTATION;
    /** paragraph indentation setting. */
    private int paragraphIndentation = Defaults.PARAGRAPH_INDENTATION;
    /** Text width setting. */
    private int textWidth = Defaults.TEXT_WIDTH;
    /** paragraph skip setting. */
    private int paragraphSkip = Defaults.PARAGRAPH_SKIP;
    /** Current word. */
    private String _word = "";
    /** List of words being accumulated. */
    private ArrayList<String> _words = new ArrayList<String>();
}
