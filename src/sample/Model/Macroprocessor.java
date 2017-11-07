package sample.Model;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by HP PC on 30.10.2017.
 */
public class Macroprocessor {

    private HashMap<String, Macros> macroses;

    public void transform(File masm) throws Syntax.SyntaxException {
        macroses = new HashMap<>();

        StringBuffer result = new StringBuffer(Syntax.EMPTY_LINE);
        int lineNumber = 0;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(masm), Syntax.ENCODING))){
            String line = br.readLine();

            if(line == null){
                throw new Syntax.SyntaxException(Syntax.SyntaxException.FILE_EMPTY);
            }
            line = line.replaceAll(Syntax.UTF8_BOM, Syntax.EMPTY_LINE);

            do{
                if(Syntax.isEmpty(line)){
                    lineNumber++;
                    continue;
                }

                if(Syntax.isComment(line)){
                    lineNumber++;
                    continue;
                }

                if(Syntax.isLib(line)){
                    File lib = findLib(line, masm.getAbsolutePath());
                    createMacros(lib);
                    lineNumber++;
                    continue;
                }

                if(!Syntax.checkKeyWord(line)){
                    if(!Syntax.isPartOfComment(line, Syntax.COLON)){
                        result.append(line.trim().substring(0, line.indexOf(Syntax.COLON) + 1));
                    }
                    line = insertMacro(line);
                    if(line == null){
                        throw new Syntax.SyntaxException(lineNumber, Syntax.SyntaxException.MACROS_IS_NOT_FOUND);
                    }
                }

                result.append(line);
                result.append('\n');

                lineNumber++;
            }while ((line = br.readLine()) != null);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        writeResult(masm, result.toString());

        return ;
    }

    private void writeResult(File file, String string){
        String result = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(Syntax.SLASH) + 1) + file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(Syntax.SLASH) + 1, file.getAbsolutePath().indexOf(Syntax.DOT)) + ".uum32asm";
        try(OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(result), Syntax.ENCODING)){
            out.write(string);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createMacros(File lib) throws Syntax.SyntaxException {
        int lineNumber = 0;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(lib), Syntax.ENCODING))){
            ArrayList<Macros> insideMacroses = new ArrayList<>();

            String line = br.readLine();
            if(line == null){
                return;
            }
            line = line.replaceAll(Syntax.UTF8_BOM, Syntax.EMPTY_LINE);

            do{
                if(Syntax.isMacrosBegin(line)){
                    String macroName = line.replaceAll(Syntax.SPACE_STRING, Syntax.EMPTY_LINE).substring(0, line.indexOf(Syntax.COLON)).toUpperCase();

                    StringBuilder tmp = new StringBuilder(line);
                    tmp.append(Syntax.NEXT_LINE);

                    while(!Syntax.isMacrosEnded(line = br.readLine())){

                        if(Syntax.isEmpty(line)){
                            lineNumber++;
                            continue;
                        }

                        if(Syntax.isComment(line)){
                            lineNumber++;
                            continue;
                        }

                        if(!Syntax.checkKeyWord(line)){
                            Macros insideMacros = checkMacros(line);
                            if(insideMacros == null){
                                throw new Syntax.SyntaxException(lineNumber, Syntax.SyntaxException.MACROS_IS_NOT_DECLARED);
                            }else{
                                for(String name : macroses.keySet()){
                                    if(Syntax.matchWord(line, name)){
                                        line = line.substring(line.toUpperCase().indexOf(name) + name.length(), line.length()).replaceAll(Syntax.SPACE_STRING, Syntax.EMPTY_LINE);
                                        break;
                                    }
                                }
                                line = insideMacros.getMacrosWithArgs(prepareArgs(line));
                                insideMacroses.add(insideMacros);
                            }
                        }
                        tmp.append(line);
                        tmp.append(Syntax.NEXT_LINE);

                        lineNumber++;
                    }

                    String end = Syntax.replaceEnd(line);
                    if(end != null){
                        tmp.append(end);
                    }

                    macroses.put(macroName, new Macros(tmp.toString()));

                    if(insideMacroses.size() != 0){
                        for(int i = 0; i < insideMacroses.size(); i++){
                            macroses.get(macroName).joinMacros(insideMacroses.get(i));
                        }
                    }
                }
                lineNumber++;
            }while ((line = br.readLine()) != null);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Macros checkMacros(String line){
        Macros res = null;
        for(String name : macroses.keySet()){
            if(Syntax.matchWord(line, name)){
                res = macroses.get(name);
                break;
            }
        }
        return res;
    }

    private String insertMacro(String line) throws Syntax.SyntaxException {
        String res = null;
        for(String name : macroses.keySet()){
            if(Syntax.matchWord(line, name)){
                if(line.indexOf(Syntax.COMMENT) > -1){
                    line = line.substring(0, line.indexOf(Syntax.COMMENT));
                }
                line = line.substring(line.toUpperCase().lastIndexOf(name) + name.length(), line.length()).replaceAll(Syntax.SPACE_STRING,Syntax.EMPTY_LINE);
                res = macroses.get(name).getMacros(prepareArgs(line));
                break;
            }
        }

        return res;
    }

    private String[] prepareArgs(String args){
        args = args.replaceAll(Syntax.ESCAPE_CODE, Syntax.EMPTY_LINE);
        ArrayList<String> result = new ArrayList<>();
        StringBuffer tmp = new StringBuffer();

        if(Syntax.isEmpty(args)) return null;

        if(Syntax.isComment(args)) return null;

        int end = args.indexOf(Syntax.COMMENT);

        if(end == -1){
            end = args.length();
        }

        for(int i = 0; i < end; i++){
            if(args.charAt(i) == Syntax.COMMENT){
                break;
            }
            if(args.charAt(i) == Syntax.COMMA){
                result.add(tmp.toString());
                tmp.replace(0, tmp.length(), Syntax.EMPTY_LINE);
                continue;
            }
            tmp.append(args.charAt(i));
        }
        result.add(tmp.toString());

        return result.toArray(new String[0]);
    }

    private File findLib(String libName, String libPath) throws Syntax.SyntaxException {
        libName = libName.replaceAll(Syntax.SPACE_STRING, Syntax.EMPTY_LINE); //Delete spaces

        if(Syntax.isPartOfComment(libName, Syntax.QUOTATION_MARK, Syntax.QUOTATION_MARK)){
            throw new Syntax.SyntaxException(Syntax.SyntaxException.INCORRECT_LIB_NAME);
        }
        else{
            int startPos = 0;
            int endPos = 0;
            char arr[] = libName.toCharArray();
            boolean firstFound = false;
            for(int i = 0; i < arr.length; i++){
                if(arr[i] == Syntax.QUOTATION_MARK && !firstFound){
                    startPos = i;
                    firstFound = true;
                    continue;
                }
                if(arr[i] == Syntax.QUOTATION_MARK && firstFound){
                    endPos = i;
                    break;
                }
            }
            libName = libName.substring(startPos + 1, endPos);
        }

        libPath = libPath.substring(0, libPath.lastIndexOf(Syntax.SLASH) + 1);
        libPath += libName;
        File lib = new File(libPath);
        if(lib == null){
            throw new Syntax.SyntaxException(libName + Syntax.SyntaxException.LIB_IS_NOT_FOUND);
        }
        return lib;
    }

}
