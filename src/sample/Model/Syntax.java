package sample.Model;

import java.util.regex.Pattern;

/**
 * Created by HP PC on 31.10.2017.
 */
public class Syntax {

    public static final String[] keyWords = {"ADD", "ADDB", "ADDH","ADDR", "AND", "ALIAS",
            "BYTE",
            "CALL", "CLEAR", "COMP", "COMPB", "COMPH", "COMPR", "CSECT",
            "DIV", "DIVB", "DIVH", "DIVR",
            "EQV", "END", "EQU", "EXPORT",
            "GC",
            "HIO",
            "INCM", "INCMB", "INCMH", "INCR", "INT", "IMPORT", "INCLUDE",
            "JEQ", "JGT", "JLT", "JMP", "LD", "LDB", "LDH",
            "MOV", "MUL", "MULB", "MULH", "MULR", "MACRO", "MEND",
            "NOP", "NOT",
            "OR",
            "POP", "POPB", "POPH", "PUSH", "PUSHB", "PUSHH",
            "RD", "RET", "RSB", "RESB", "RESH", "RESW",
            "SB", "SC", "SHL", "SHR", "SIO", "ST", "STB", "STH", "SUB", "SUBB", "SUBH", "SUBR", "START",
            "TD", "TIO",
            "WD", "WORD"
    };
    public static final String ENCODING = "UTF-8";
    public static final String UTF8_BOM = "\uFEFF";
    public static final String ESCAPE_CODE = "\\s+";

    public static final String INCLUDE = "INCLUDE";
    public static final String BEGIN = "MACRO";
    public static final String END = "MEND";

    public static final char GLOBAL_VARIABLE = '@';

    public static final char COMMENT = ';';
    public static final String COMMENT_STRING = ";";
    public static final char COMMA = ',';

    public static final char COLON = ':';
    public static final String COLON_STRING = ":";

    public static final char NEXT_LINE = '\n';
    public static final char TAB = '\t';

    public static final char SPACE = ' ';
    public static final String SPACE_STRING = " ";

    public static final char QUOTATION_MARK = '"';

    public static final String DOLLAR_STRING = "\\$";
    public static final String DOLLAR_SIGN = "$";
    public static final char DOLLAR = '$';
    public static final char PARAM = '&';
    public static final char PLUS = '+';
    public static final char MINUS = '-';
    public static final char ARR = '[';
    public static final char DOT = '.';
    public static final char SHARP = '#';
    public static final String NOP = "\tNOP\t";
    public static final String EMPTY_LINE = "";
    public static final String SLASH = "\\";

    public static final String MACROS_BEGIN = "=============MACRO=============";
    public static final String MACROS_END = "=============MEND=============";
    public static final String COMMENT_LINE = "=============";

    public static String leftSeparator = "(" + SHARP + "|" + SPACE + "|" + COLON + "|" + DOLLAR + "|" + PARAM + "|" + TAB + "|" + SLASH + PLUS + "|" + COMMA + "|" + MINUS + ")";
    public static String rightSeparator = "(" + SPACE + "|" + TAB + "|" + NEXT_LINE + "|" + SLASH + ARR + "|" + COMMA + ")";

    public static boolean isEmpty(String line){
        line = line.replaceAll(ESCAPE_CODE, EMPTY_LINE); //Clear from enscape code
        line = line.replaceAll(SPACE_STRING, EMPTY_LINE); //Clear from space
        if(line.length() == 0){
            return true;
        }
        return false;
    }

    public static boolean isLabel(String line){
        line = line.replaceAll(ESCAPE_CODE, EMPTY_LINE); //Clear from enscape code
        line = line.replaceAll(SPACE_STRING, EMPTY_LINE); //Clear from space
        if(line.indexOf(COLON) == (line.length() - 1)){
            return true;
        }
        return false;
    }

    public static boolean checkKeyWord(String line) {
        for (int i = 0; i < keyWords.length; i++) {
            if (matchWord(line, keyWords[i])) {
                return true;
            }
            else{
                line = line.toUpperCase();

                Pattern pattern = Pattern.compile(leftSeparator + "*" + keyWords[i]);
                if(pattern.matcher(line).find()){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean matchWord(String line, String word){
        int com = line.indexOf(COMMENT);

        if(com != -1){
            line = line.substring(0, com);
        }

        line = line.toUpperCase();
        word = word.toUpperCase();

        Pattern pattern = Pattern.compile(leftSeparator + "+" + word + rightSeparator + "+");
        return pattern.matcher(line).find();
    }

    public static boolean isComment(String line){
        if(line.startsWith(UTF8_BOM)){
            line = line.substring(1);
        }
        line = line.replaceAll(ESCAPE_CODE, EMPTY_LINE); //Clear from enscape code
        line = line.replaceAll(SPACE_STRING, EMPTY_LINE); //Clear from space
        if(line.charAt(0) == COMMENT){
            return true;
        }
        return false;
    }

    public static boolean isLib(String line){
        line = line.toUpperCase();

        Pattern pattern = Pattern.compile(leftSeparator + "*" + INCLUDE + rightSeparator + "+");
        return pattern.matcher(line).find();
    }

    public static boolean isMacrosBegin(String line){
        line = line.toUpperCase();

        Pattern pattern = Pattern.compile(leftSeparator + "+" + BEGIN + rightSeparator + "*");
        return pattern.matcher(line).find();
    }

    public static boolean isMacrosEnded(String line) throws SyntaxException {
        line = line.toUpperCase();

        Pattern pattern = Pattern.compile(leftSeparator + "*" + END + rightSeparator + "*");
        return pattern.matcher(line).find();
    }

    public static String replaceEnd(String line){
        if(isPartOfComment(line, COLON)){
            return null;
        }
        else {
            int pos = line.indexOf(COLON) + 1;
            line = line.substring(0, pos + 1) + NOP + Syntax.NEXT_LINE;
            return line;
        }
    }

    public static boolean isPartOfComment(String line, char symb){
        int symbPos = line.indexOf(symb);
        if(symbPos != -1){
            int commaPos = line.indexOf(COMMENT);
            if(commaPos != -1){
                if(symbPos < commaPos){
                    return false;
                }
            }else{
                return false;
            }
        }
        return true;
    }

    public static boolean isPartOfComment(String line, char firstSymb, char secondSymb){
        int firstSymbPos = 0;
        int secondSymbPos = 0;
        if(firstSymb == secondSymb){
            char arr[] = line.toCharArray();
            boolean firstFound = false;
            for(int i = 0; i < arr.length; i++){
                if(arr[i] == firstSymb && !firstFound){
                    firstSymbPos = i;
                    firstFound = true;
                    continue;
                }
                if(arr[i] == firstSymb && firstFound){
                    secondSymbPos = i;
                    break;
                }
            }
        }
        else{
            firstSymbPos = line.indexOf(firstSymb);
            secondSymbPos = line.indexOf(secondSymb);
        }

        if(firstSymbPos != -1 && secondSymbPos != -1){
            int commaPos = line.indexOf(COMMENT);
            if(commaPos != -1){
                if(secondSymbPos < commaPos){
                    return false;
                }
            }else{
                return false;
            }
        }
        return true;
    }

    public static class SyntaxException extends Exception{
        public static final String LIB_IS_NOT_FOUND = " библиотека не найдена. Файл библиотеки должен находиться в одной директории с файлом *.UUM32MASM";
        public static final String INCORRECT_LIB_NAME = "Некорректное имя библиотеки";
        public static final String MACROS_IS_NOT_DECLARED = "Макрос не объявлен ранее.";
        public static final String MACROS_IS_NOT_FOUND = "Макрос не найден.";
        public static final String WRONG_ARGUMENTS = "Не совпадение параметров макроса и передаваемых аргументов.";
        public static final String WRONG_PARAMS = "Ошибка в записи параметра.";
        public static final String FILE_EMPTY = "Файл пуст.";

        private int lineNumber;

        public SyntaxException(String message) {
            super(message);
        }

        public SyntaxException(int lineNumber, String exc){
            super(exc);
            this.lineNumber = lineNumber;
        }

        @Override
        public String getMessage() {
            String message = "Ошибка в строке " + lineNumber + ": " + super.getMessage();
            return message;
        }
    }

    public static void main(String[] args) {
        String rightSeparator = "(" + SLASH + PLUS + ")";
        System.out.println(INCLUDE  + rightSeparator);
        Pattern pattern = Pattern.compile(INCLUDE + rightSeparator);
        System.out.println(pattern.matcher("    INCLUDE").find());
    }

}
