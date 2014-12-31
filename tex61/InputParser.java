package tex61;

import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.MatchResult;
import java.io.Reader;

import static tex61.FormatException.reportError;

/** Reads commands and text from an input source and send the results
 *  to a designated Controller. This essentially breaks the input down
 *  into "tokens"---commands and pieces of text.
 *  @author Jason Qiu
 */
class InputParser {

    /** Matches text between { } in a command, including the last
     *  }, but not the opening {.  When matched, group 1 is the matched
     *  text.  Always matches at least one character against a non-empty
     *  string or input source. If it matches and group 1 is null, the
     *  argument was not well-formed (the final } was missing or the
     *  argument list was nested too deeply). */
    private static final Pattern BALANCED_TEXT =
            Pattern.compile("(?s)((?:\\\\.|[^\\\\{}]"
                    + "|[{](?:\\\\.|[^\\\\{}])*[}])*)"
                    + "\\}"
                    + "|.");

    /** Matches input to the text formatter.  Always matches something
     *  in a non-empty string or input source.  After matching, one or
     *  more of the groups described by *_TOKEN declarations will
     *  be non-null.  See these declarations for descriptions of what
     *  this pattern matches.  To test whether .group(*_TOKEN) is null
     *  quickly, check for .end(*_TOKEN) > -1).  */
    private static final Pattern INPUT_PATTERN =
            Pattern.compile("(?s)(\\p{Blank}+)"
                    + "|(\\r?\\n((?:\\r?\\n)+)?)"
                    + "|\\\\([\\p{Blank}{}\\\\])"
                    + "|\\\\(\\p{Alpha}+)([{]?)"
                    + "|((?:[^\\p{Blank}\\r\\n\\\\{}]+))"
                    + "|(.)");

    /** Symbolic names for the groups in INPUT_PATTERN. */
    private static final int
        /** Blank or tab. */
        BLANK_TOKEN = 1,
        /** End of line or paragraph. */
        EOL_TOKEN = 2,
        /** End of paragraph (>1 newline). EOL_TOKEN group will also
         *  be present. */
        EOP_TOKEN = 3,
        /** \{, \}, \\, or \ .  .group(ESCAPED_CHAR_TOKEN) will be the
         *  character after the backslash. */
        ESCAPED_CHAR_TOKEN = 4,
        /** Command (\<alphabetic characters>).  .group(COMMAND_TOKEN)
         *  will be the characters after the backslash.  */
        COMMAND_TOKEN = 5,
        /** A '{' immediately following a command. When this group is present,
         *  .group(COMMAND_TOKEN) will also be present. */
        COMMAND_ARG_TOKEN = 6,
        /** Segment of other text (none of the above, not including
         *  any of the special characters \, {, or }). */
        TEXT_TOKEN = 7,
        /** A character that should not be here. */
        ERROR_TOKEN = 8;

    /** A new InputParser taking input from READER and sending tokens to
     *  OUT. */
    InputParser(Reader reader, Controller out) {
        _input = new Scanner(reader);
        _out = out;
    }

    /** A new InputParser whose input is TEXT and that sends tokens to
     *  OUT. */
    InputParser(String text, Controller out) {
        _input = new Scanner(text);
        _out = out;
    }

    /** Break all input source text into tokens, and send them to our
     *  output controller.  Finishes by calling .close on the controller.
     */
    void process() {
        while (_input.hasNext()) {
            _input.findWithinHorizon(INPUT_PATTERN, 0);
            MatchResult token = _input.match();
            if (token.end(TEXT_TOKEN) > -1) {
                _out.addText(token.group(TEXT_TOKEN));
            } else if (token.end(BLANK_TOKEN) > -1) {
                _out.endWord();
            } else if (token.end(EOP_TOKEN) > -1) {
                _out.addNewline();
                _out.endWord();
                _out.endParagraph();
            } else if (token.end(EOL_TOKEN) > -1) {
                _out.addNewline();
                _out.endWord();
            } else if (token.end(ESCAPED_CHAR_TOKEN) > -1) {
                _out.addText(token.group(ESCAPED_CHAR_TOKEN));
            } else if (token.end(COMMAND_ARG_TOKEN) > -1) {
                if (token.group(COMMAND_ARG_TOKEN).equals("")) {
                    processCommand(token.group(COMMAND_TOKEN), null);
                } else {
                    if (_input.findWithinHorizon(BALANCED_TEXT, 0) == null) {
                        throw new FormatException("Command not well formed.");
                    }
                    MatchResult arg = _input.match();
                    if (arg.group(1) == null) {
                        throw new FormatException("Command not well formed.");
                    }
                    if (token.group(COMMAND_TOKEN).equals("textheight")
                            && arg.group(1).equals("0")) {
                        throw new FormatException("Invalid argument.");
                    }
                    if (!token.group(COMMAND_TOKEN).equals("endnote")
                            && !arg.group(1).matches("\\d+")) {
                        if (!(arg.group(1).matches("-\\d+") && token.group(
                                COMMAND_TOKEN).equals("parindent"))) {
                            throw new FormatException
                            ("Invalid argument.");
                        }
                    }
                    processCommand(token.group(COMMAND_TOKEN), arg.group(1));
                }
            } else if (token.end(COMMAND_TOKEN) > -1) {
                processCommand(token.group(COMMAND_TOKEN), null);
            } else if (token.end(ERROR_TOKEN) > -1) {
                throw new FormatException("Input contains invalid tokens.");
            }
        }
        _out.endWord();
        _out.endParagraph();
        _out.close();
    }

    /** The process method for endnotes. */
    void processEndnote() {
        while (_input.hasNext()) {
            _input.findWithinHorizon(INPUT_PATTERN, 0);
            MatchResult token = _input.match();
            if (token.end(TEXT_TOKEN) > -1) {
                _out.addText(token.group(TEXT_TOKEN));
            } else if (token.end(BLANK_TOKEN) > -1) {
                _out.endWord();
            } else if (token.end(EOP_TOKEN) > -1) {
                _out.addNewline();
                _out.endWord();
                _out.endParagraph();
            } else if (token.end(EOL_TOKEN) > -1) {
                _out.addNewline();
                _out.endWord();
            } else if (token.end(ESCAPED_CHAR_TOKEN) > -1) {
                _out.addText(token.group(ESCAPED_CHAR_TOKEN));
            } else if (token.end(COMMAND_ARG_TOKEN) > -1) {
                if (token.group(COMMAND_TOKEN).equals("endnote")) {
                    throw new FormatException("Cannot write endnotes in "
                            + "an endnote");
                } else if (token.group(COMMAND_ARG_TOKEN).equals("")) {
                    processCommand(token.group(COMMAND_TOKEN), null);
                } else {
                    if (_input.findWithinHorizon(BALANCED_TEXT, 0) == null) {
                        throw new FormatException("Command not well formed.");
                    }
                    if (!token.group(COMMAND_TOKEN).equals("textheight")) {
                        MatchResult arg = _input.match();
                        if (arg.group(1) == null) {
                            throw new FormatException
                            ("Command not well formed.");
                        }
                        if (!arg.group(1).matches("\\d+")) {
                            if (!(arg.group(1).matches("-\\d+") && token.group(
                                    COMMAND_TOKEN).equals("parindent"))) {
                                throw new FormatException
                                ("Invalid argument.");
                            }
                        }
                        processCommand(token.group(COMMAND_TOKEN)
                                , arg.group(1));
                    }
                }
            } else if (token.end(COMMAND_TOKEN) > -1) {
                if (!token.group(COMMAND_TOKEN).equals("textheight")) {
                    processCommand(token.group(COMMAND_TOKEN), null);
                }
            } else if (token.end(ERROR_TOKEN) > -1) {
                throw new FormatException("Input contains invalid tokens.");
            }
        }
        _out.endWord();
        _out.endParagraph();
    }


    /** Process \COMMAND{ARG} or (if ARG is null) \COMMAND.  Call the
     *  appropriate methods in our Controller (_out). */
    private void processCommand(String command, String arg) {
        try {
            switch (command) {
            case "indent":
                _out.setIndentation(Integer.parseInt(arg));
                break;
            case "parindent":
                _out.setParIndentation(Integer.parseInt(arg));
                break;
            case "textwidth":
                _out.setTextWidth(Integer.parseInt(arg));
                break;
            case "textheight":
                _out.setTextHeight(Integer.parseInt(arg));
                break;
            case "parskip":
                _out.setParSkip(Integer.parseInt(arg));
                break;
            case "nofill":
                _out.setFill(false);
                break;
            case "fill":
                _out.setFill(true);
                break;
            case "justify":
                _out.setJustify(true);
                break;
            case "nojustify":
                _out.setJustify(false);
                break;
            case "endnote":
                _out.formatEndnote(arg);
                break;
            default:
                reportError("unknown command: %s", command);
                break;
            }
        } catch (FormatException e) {
            throw new FormatException("Invalid command");
        }

    }

    /** My input source. */
    private final Scanner _input;
    /** The Controller to which I send input tokens. */
    private Controller _out;

}
