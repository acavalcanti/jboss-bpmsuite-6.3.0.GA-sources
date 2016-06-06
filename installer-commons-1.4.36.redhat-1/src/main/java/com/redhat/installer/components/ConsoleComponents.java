package com.redhat.installer.components;


import java.util.regex.Pattern;

import com.izforge.izpack.util.Shell;
import com.redhat.installer.asconfiguration.securitydomain.panel.DataHelper;

public class ConsoleComponents {
    
    private static Pattern notEmpty = Pattern.compile(".+");
    /** Combo box represented in the console
     * @param id      Pass in ID that ties a label and variable together
     * @param varName Optionally pass in a variable name where convention is not being adhered to
     * @param answers Pass in list of options
     * @param helper  Pass in the helper that manages your idata information
     */
    public static void showComboBox(String id,  String[] answers, DataHelper helper)
    { showComboBox(id,  id, answers, helper); }
    public static void showComboBox(String id, String varId, String[] answers,  DataHelper helper) {
        Shell console = Shell.getInstance();
        int selected = 0;
        String prevAnswer = helper.getVariable(varId);
        if (prevAnswer == null) prevAnswer = answers[0];
        helper.setVariable(varId, prevAnswer);
        System.out.println(helper.getLabel(id));
        while (true){
            for (int i=0; i<answers.length; i++) 
                System.out.println(i + " [" + (prevAnswer.equals(answers[i]) ? "x" : " ") + "] " + answers[i]);
            String input = console.getInput();
            if(input.isEmpty()) return;
            try   { selected = Integer.parseInt(input); }
            catch ( Exception e ) { continue; }
            
            if ( selected < 0  || selected > answers.length-1) continue;
            break;
        }
        helper.setVariable(varId, answers[selected]);
    }
    
    
    /** JTextField represented in the console
     * 
     * @param id      Pass in ID that ties a label and variable together
     * @param varId   Pass in a variable name where convention is not being adhered to
     * @param errorId Pass in a error ID  where convention is not being adhered to
     * @param regex   Pass in a regex to validate with
     * @param helper  Pass in the helper that manages your idata information
     */
    public static void enterValue(String id, String varId, String errorId, Pattern regex, DataHelper helper) {
        String prevAns = helper.getVariable(varId);
        
        Shell console = Shell.getInstance();
        if (prevAns == null) prevAns = "";
        
        while (true) {
            System.out.println(helper.getLabel(id) + " [" + prevAns + "] ");
            String input = console.getInput();
            if (input.isEmpty()) input = prevAns;
            if(!regex.matcher(input).matches()){
                System.out.println(helper.getErrorMsg(errorId));
                continue;
            } 
            helper.setVariable(varId, input);
            break;
        }
    }
    public static void enterValue(String id, DataHelper helper) { enterValue(id, id, id, notEmpty, helper); }
    public static void enterValue(String id, Pattern regex, DataHelper helper) { enterValue(id, id, id, regex, helper); }
    public static void enterValue(String id, String errorId, Pattern regex,  DataHelper helper) { enterValue(id,id, errorId, regex, helper); }
    public static void enterValue(String id, String varId, DataHelper helper) { enterValue(id, varId, id, notEmpty, helper); }

}
