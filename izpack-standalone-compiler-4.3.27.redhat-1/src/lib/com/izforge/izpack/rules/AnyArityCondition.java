package com.izforge.izpack.rules;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.Debug;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the base abstract class for any compound condition that needs to
 * support arbitrary arity: AND, OR, XOR for example.
 *
 * Created by francisco canas on 12/16/13 <fcanas@redhat.com>
 */
public abstract class AnyArityCondition extends Condition {

    protected List<Condition> operands;

    abstract protected String getTag();

    public AnyArityCondition(){
        super();
    }

    public AnyArityCondition(AutomatedInstallData installdata) {
        super();
        this.installdata = installdata;
    }

    /**
     * Constructor used for simple binary conditions. Used during parsing of
     * spec files containing condition strings with +,|, etc.
     */
    public AnyArityCondition(Condition operand1, Condition operand2, AutomatedInstallData installdata)
    {
        this(installdata);
        List<Condition> operands = new ArrayList<Condition>();
        operands.add(operand1);
        operands.add(operand2);
        setConditions(operands);
    }

    /**
     * Method for building an n-arity condition out of a list of operands.
     * @param operands
     */
    protected void setConditions(List<Condition> operands) {
        this.operands = operands;

        for (Condition operand : this.operands) {
            if (operand != null)
                operand.setInstalldata(this.installdata);
        }
    }

    /**
     * Reads n-arity and conditions from the xml spec element.
     * @param xmlcondition
     */
    public void readFromXML(IXMLElement xmlcondition) {
        operands = new ArrayList<Condition>();
        try {
            for (int i = 0; i < xmlcondition.getChildren().size(); i++) {
                operands.add(RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(i)));
            }
        } catch (Exception e) {
            Debug.log("missing element in condition");
        }
    }

    /**
     * Prints the dependencies for this condition.
     * @return
     */
    public String getDependenciesDetails() {
        StringBuffer details = new StringBuffer();

        details.append(this.id);
        details.append(" depends on:<ul>");

        for (Condition condition : operands) {
            details.append("<li>");
            details.append(condition.getDependenciesDetails());
            details.append("</li> ").append(getTag()).append(" ");
        }
        // remove the trailing condition label. thanks java.
        details.substring(0,details.length()-getTag().length() + 1);
        details.append("</ul>");
        return details.toString();

    }

    @Override
    public void makeXMLData(IXMLElement root) {
        for (Condition operand : operands) {
            IXMLElement ele = RulesEngine.createConditionElement(operand, root);
            operand.makeXMLData(ele);
            root.addChild(ele);
        }
    }
}