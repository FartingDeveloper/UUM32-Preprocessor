package sample.Model;

import java.util.ArrayList;

/**
 * Created by HP PC on 31.10.2017.
 */
public class Macros {

    private String macros;
    private ArrayList<String> params;
    private ArrayList<String> labels;

    private static int labelCounter;

    public Macros(String macros) throws Syntax.SyntaxException {
        this.macros = macros.substring(macros.indexOf(Syntax.NEXT_LINE) + 1, macros.length());
        params = collectParams(macros.substring(0, macros.indexOf(Syntax.NEXT_LINE)));
        labels = collectLabels(macros);
        labelCounter = 0;
    }

    public String getMacros(String ... args) throws Syntax.SyntaxException {
        String result = null;

        if((params.size() == 0) && args == null){
            result = macros;
        }
        else{
            result = replaceParams(args);
        }

        for(int i = 0; i < labels.size(); i++){
            String label = "___" + labels.get(i) + "___" + labelCounter;
            result = result.replaceAll(Syntax.DOLLAR_STRING + labels.get(i), label);
        }

        labelCounter++;
        return result;
    }

    private ArrayList<String> collectParams(String macros) throws Syntax.SyntaxException {
        ArrayList<String> result = new ArrayList<>();

        macros = macros.replaceAll(Syntax.ESCAPE_CODE, Syntax.EMPTY_LINE);

        if(!Syntax.isPartOfComment(macros, Syntax.PARAM)) {
            int begin = macros.indexOf(Syntax.PARAM);
            if(begin == -1){
                return result;
            }
            int end = macros.indexOf(Syntax.COMMENT);

            if (end == -1) {
                end = macros.length();
            }

            StringBuilder tmp = new StringBuilder(Syntax.EMPTY_LINE);
            for (int i = begin + 1; i < end; i++) {
                char symb = macros.charAt(i);
                if(symb == Syntax.COMMENT){
                    break;
                }
                if (symb == Syntax.COMMA) {
                    result.add(tmp.toString());
                    tmp.replace(0, tmp.length(), Syntax.EMPTY_LINE);
                    if (macros.charAt(i + 1) != Syntax.PARAM) {
                        throw new Syntax.SyntaxException(Syntax.SyntaxException.WRONG_PARAMS);
                    }
                    i++;
                    continue;
                }

                tmp.append(symb);
            }
            result.add(tmp.toString());
        }
        return result;
    }

    private ArrayList<String> collectLabels(String macros){
        ArrayList<String> result = new ArrayList<>();
        macros = macros.replaceAll(Syntax.SPACE_STRING, Syntax.EMPTY_LINE);

        StringBuilder tmp = new StringBuilder(Syntax.EMPTY_LINE);
        while(macros.length() != 0){
            int nextLinePos = macros.indexOf(Syntax.NEXT_LINE);
            if(nextLinePos == -1){
                nextLinePos = macros.length();
            }
            String line = macros.substring(0, nextLinePos);
            if(line.contains(Syntax.DOLLAR_SIGN) && line.contains(Syntax.COLON_STRING)){

                int startLabelPos = line.indexOf(Syntax.DOLLAR);
                int endLabelPos = line.indexOf(Syntax.COLON);
                int commentPos = line.indexOf(Syntax.COMMENT);
                if(commentPos != -1){
                    if(endLabelPos > commentPos) continue;
                }
                if(startLabelPos < endLabelPos){
                    for(int j = startLabelPos + 1; j < endLabelPos; j++){
                        tmp.append(line.charAt(j));
                    }
                    result.add(tmp.toString());
                    tmp.replace(0, tmp.length(), Syntax.EMPTY_LINE);
                }
            }

            if(nextLinePos == macros.length()){
                break;
            }
            macros = macros.substring(nextLinePos + 1, macros.length());
        }

        return result;
    }

    public void joinMacros(Macros macros){
        labels.addAll(macros.labels);
        labelCounter++;
    }

    private String replaceParams(String... args) throws Syntax.SyntaxException {
        StringBuilder result = new StringBuilder(Syntax.EMPTY_LINE);
        if(args != null){
            if(args.length != this.params.size()){
                throw new Syntax.SyntaxException(Syntax.SyntaxException.WRONG_ARGUMENTS);
            }
            else{
                String tmp = macros;
                while (tmp.length() != 0){
                    int nextLinePos = tmp.indexOf(Syntax.NEXT_LINE);
                    if(nextLinePos == -1){
                        nextLinePos = tmp.length();
                    }
                    String line = tmp.substring(0, nextLinePos);

                    for(int i = 0; i < args.length; i++) {
                        line = replaceParamByLine(line, Syntax.PARAM + params.get(i), args[i]);
                    }
                    result.append(line + Syntax.NEXT_LINE);
                    if(nextLinePos == tmp.length()){
                        break;
                    }
                    tmp = tmp.substring(nextLinePos + 1, tmp.length());
                }
            }
        }
        else{
            return null;
        }
        return result.toString();
    }

    private String replaceParamByLine(String line, String param, String arg){
        if(Syntax.matchWord(line, param)){
            if(arg.indexOf(Syntax.DOLLAR) == 0){
                arg = Syntax.SLASH + arg;
            }
            return line.replaceAll(param, arg);
        }
        return line;
    }
}
