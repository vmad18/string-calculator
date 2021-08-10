package mod.quizbot.me.Calculator;

import mod.quizbot.me.QuizBot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
Crude String Evaluator
 */

public class Calculator {

    public List<String> str;

    private final boolean isRad;

    private int steps = 0;

    private String playername = "";

    private QuizBot bot;

    private final DecimalFormat decimalFormat = new DecimalFormat("0.000000000000");

    final double phi = (1+Math.sqrt(5))/2;

    public Calculator(String s, String name, QuizBot b) {
        this.isRad = ((CalculatorSettings) b.calcSets.getO(name)).isRad();
        this.playername = name;
        this.bot = b;
        initialize(s);
    }

    public Calculator(String s, boolean r) {
        this.isRad = r;
        initialize(s);
    }

    private String initialize(String s) {
        StringBuilder new_string = new StringBuilder();

        try {
            if (playername != null) {
                if (bot.calcSets.getM().containsKey(playername)) {
                    s.replaceAll("ans", ((CalculatorSettings) bot.calcSets.getO(playername)).getPrevAns());
                } else {
                    s.replaceAll("ans", "");
                }
            } else {
                s.replaceAll("ans", "");
            }
        } catch (Exception e) {
            s.replaceAll("ans", "");
        }


        s = s.replaceAll("\\s+", "");
        s = s.replaceAll("\n", "");

        //initialize
        s = s.replaceAll("e", String.valueOf(Math.E));
        s = s.replaceAll("pi", String.valueOf(Math.PI));
        s = s.replaceAll("phi", String.valueOf(phi));
        s = s.replaceAll("--", "+");
        s = s.replaceAll("\\+-", "-");
        s = s.replaceAll("\\*\\+", "*");
        s = s.replaceAll("/\\+", "/");

        //parse trig functions (could be done with regex)
        if(s.contains("sin")||s.contains("cos")||s.contains("tan")){
            s = arcParseTrig(s);
            s = parseTrig(s);
        }

        //parse log functions
        if(s.contains("log")||s.contains("ln"))
            s = parseLog(s);

        //implement Spaces between operators
        int strlen = 0;
        for (int i = 0; i < s.length(); i++) {
            String w = String.valueOf(s.charAt(i));
            if (w.equals("+") || w.equals("-") || w.equals("/") || w.equals("*") || w.equals("^") || w.equals("(") || w.equals(")")) {
                String val = s.substring(i - strlen, i);
                if (w.equals(")")) {
                    new_string.append(val).append(" ").append(w);
                } else if (w.equals("(")) {
                    new_string.append(w).append(" ");
                } else if (w.equals("-") && i == 0) {
                    if(i==0){
                        new_string.append(w);
                    }else{
                        new_string.append(" ").append(w+s.charAt(i+1));
                    }
                } else if (!(i == 0)) {
                    if(String.valueOf(s.charAt(i+1)).equals("-")){
                        new_string.append(val).append(" ").append(w);
                    }else{
                        new_string.append(val).append(" ").append(w).append(" ");
                    }
                }
                strlen = 0;
            } else {
                strlen++;
            }
        }
        new_string.append(s.substring(s.length() - strlen));

        //Put equation into array by spaces
        this.str = new ArrayList<>(Arrays.asList(new_string.toString().split(" ")));
        str.remove("");

        //Adjust for negatives
        for (int i = 1; i < str.size(); i++) {
            if (str.get(i).equals("-") && (str.get(i-1).equals("+")||str.get(i-1).equals("-")||str.get(i-1).equals("/")||str.get(i-1).equals("*"))) {
                if(str.get(i+1).equals("(")){
                    List<String> sub = new ArrayList(str).subList(i + 1, str.size()).subList(0,str.indexOf(")")+1);
                    int n = sub.indexOf(")");
                    List<String> sub2 = new ArrayList(sub).subList(0,n+1);
                    sub2.set(0,sub.get(0));
                    sub2.add(0,"-1");
                    sub2.add(1,"*");
                    str.set(i,parseEquation(sub2).get(0));
                    str.subList(i+1, i+n+2).clear();
                }else{
                    str.set(i + 1, "-" + str.get(i + 1));
                    str.set(i, "");
                }
            }else if(str.get(i).equals("-")){
                if(str.get(i+1).equals("(")){
                    List<String> sub = new ArrayList(str).subList(i + 1, str.size()).subList(0,str.indexOf(")")+1);
                    int n = sub.indexOf(")");
                    List<String> sub2 = new ArrayList(sub).subList(0,n+1);
                    sub2.set(0,sub.get(0));
                    sub2.add(0,"-1");
                    sub2.add(1,"*");
                    str.set(i,parseEquation(sub2).get(0));
                    str.subList(i+1, i+n+2).clear();
                    str.add(i,"+");
                }else{
                    str.set(i + 1, "-" + str.get(i + 1));
                    str.set(i, "+");
                }
            }
        }

        ArrayList<String> lis = new ArrayList<>();
        for(String i:str){
            if(!(i.equals(""))&&!(i.equals(""))&&!(i.equals("--"))){
                lis.add(i);
            }
        }
        str = lis;

        //Fix Errors
        for (int i = 2; i < str.size(); i++) {
            if (str.subList(i - 2, i).equals(Arrays.asList("*", "-")) || str.subList(i - 2, i).equals(Arrays.asList("/", "-")) || str.subList(i - 2, i).equals(Arrays.asList("+", "-") ) || str.subList(i - 2, i).equals(Arrays.asList("-", "-"))) {
                str.set(i, str.get(i - 1) + str.get(i));
                str.remove(i - 1);
                i++;
            }
        }
        return parseEquation(str).get(0);
    }

    private String parseTrig(String s) {
        StringBuilder new_str = new StringBuilder();
        double trigval;

        for (int i = 0; i < s.length(); i++) {
            String s1 = String.valueOf(s.charAt(i));
            switch (s1) {
                case "s": {
                    int c = i;
                    while (!String.valueOf(s.charAt(i)).equals(")")) {
                        i++;
                    }
                    try {
                        trigval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                    } catch (Exception e) {
                        trigval = getTrigVal(s.substring(c + 4, i + 1));

                        i += steps;
                        steps = 0;
                    }
                    new_str.append(decimalFormat.format(isRad ? Math.sin(trigval) : Math.sin(trigval * Math.PI / 180)));
                    break;
                }
                case "c": {
                    int c = i;
                    while (!String.valueOf(s.charAt(i)).equals(")")) {
                        i++;
                    }
                    try {
                        trigval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                    } catch (Exception e) {
                        trigval = getTrigVal(s.substring(c + 4, i + 1));
                        i += steps;
                        steps = 0;
                    }

                    new_str.append(decimalFormat.format((isRad ? Math.cos(trigval) : Math.cos(trigval * Math.PI / 180))));
                    break;
                }
                case "t": {
                    int c = i;
                    while (!String.valueOf(s.charAt(i)).equals(")")) {
                        i++;
                    }
                    try {
                        trigval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                    } catch (Exception e) {
                        trigval = getTrigVal(s.substring(c + 4, i + 1));
                        i += steps;
                        steps = 0;
                    }

                    new_str.append(decimalFormat.format(isRad ? Math.tan(trigval) : Math.tan(trigval * Math.PI / 180)));
                    break;
                }
                default:
                    new_str.append(s1);
                    break;
            }
        }
        return new_str.toString();
    }

    private double getTrigVal(String s) {
        double val = 0;
        double trigval;
        for (int i = 0; i < s.length(); i++) {
            String s1 = String.valueOf(s.charAt(i));
            switch (s1) {
                case "s": {
                    int c = i;
                    while (!String.valueOf(s.charAt(i)).equals(")")) {
                        i++;
                    }
                    try {
                        trigval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                        steps++;
                    } catch (Exception e) {
                        trigval = Double.parseDouble((String.valueOf(getTrigVal(s.substring(c + 4, i + 1)))));
                        steps++;
                    }
                    val = (isRad ? Math.sin(trigval) : Math.sin(trigval * Math.PI / 180));
                    break;
                }
                case "c": {
                    int c = i;
                    while (!String.valueOf(s.charAt(i)).equals(")")) {
                        i++;
                    }
                    try {
                        trigval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                        steps++;
                    } catch (Exception e) {
                        trigval = getTrigVal(s.substring(c + 4, i + 1));
                        steps++;
                    }

                    val = isRad ? Math.cos(trigval) : Math.cos(trigval * Math.PI / 180);
                    break;
                }
                case "t": {
                    int c = i;
                    while (!String.valueOf(s.charAt(i)).equals(")")) {
                        i++;
                    }
                    try {
                        trigval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                        steps++;
                    } catch (Exception e) {
                        trigval = getTrigVal(s.substring(c + 4, i + 1));
                        steps++;
                    }
                    val = (isRad ? Math.tan(trigval) : Math.tan(trigval * Math.PI / 180));
                    break;
                }
            }
        }
        return val;
    }


    private String arcParseTrig(String s) {
        StringBuilder new_str = new StringBuilder();
        double trigval;
        for (int i = 0; i < s.length(); i++) {
            String s1 = String.valueOf(s.charAt(i));

            if (s1.equals("a")) {
                String s2 = String.valueOf(s.charAt(i + 1));
                int c;
                switch (s2) {
                    case "s":
                        c = i;
                        while (!String.valueOf(s.charAt(i)).equals(")")) {
                            i++;
                        }
                        try {
                            trigval = Double.parseDouble(initialize(s.substring(c + 5, i)));
                        } catch (Exception e) {
                            trigval = arcGetTrigVal(s.substring(c + 5, i + 1));
                            i += steps;
                            steps = 0;
                        }
                        new_str.append(decimalFormat.format(Math.asin(trigval)));
                        break;
                    case "c":
                        c = i;
                        while (!String.valueOf(s.charAt(i)).equals(")")) {
                            i++;
                        }
                        try {
                            trigval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                        } catch (Exception e) {
                            trigval = arcGetTrigVal(s.substring(c + 5, i + 1));
                            i += steps;
                            steps = 0;
                        }

                        new_str.append(decimalFormat.format(Math.acos(trigval)));
                        break;
                    case "t":
                        c = i;
                        while (!String.valueOf(s.charAt(i)).equals(")")) {
                            i++;
                        }
                        try {
                            trigval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                        } catch (Exception e) {
                            trigval = arcGetTrigVal(s.substring(c + 5, i + 1));
                            i += steps;
                            steps = 0;
                        }
                        new_str.append(decimalFormat.format(Math.atan(trigval)));
                        break;
                    default:
                        new_str.append(s.charAt(i));
                        break;
                }
            } else {
                new_str.append(s.charAt(i));
            }
        }
        return new_str.toString();
    }

    private double arcGetTrigVal(String s) {
        double val = 0;
        double trigval;
        for (int i = 0; i < s.length(); i++) {
            String s1 = String.valueOf(s.charAt(i));
            if (s1.equals("a")) {
                String s2 = String.valueOf(s.charAt(i + 1));
                int c;
                switch (s2) {
                    case "s":
                        c = i;
                        while (!String.valueOf(s.charAt(i)).equals(")")) {
                            i++;
                        }
                        try {
                            trigval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                            steps++;
                        } catch (Exception e) {
                            trigval = Double.parseDouble((String.valueOf(arcGetTrigVal(s.substring(c + 5, i + 1)))));
                            steps++;
                        }
                        val = (Math.asin(trigval));
                        break;
                    case "c":
                        c = i;
                        while (!String.valueOf(s.charAt(i)).equals(")")) {
                            i++;
                        }
                        try {
                            trigval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                            steps++;
                        } catch (Exception e) {
                            trigval = arcGetTrigVal(s.substring(c + 5, i + 1));
                            steps++;
                        }

                        val = isRad ? Math.acos(trigval) : Math.acos(trigval * Math.PI / 180);
                        break;
                    case "t":
                        c = i;
                        while (!String.valueOf(s.charAt(i)).equals(")")) {
                            i++;
                        }
                        try {
                            trigval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                            steps++;
                        } catch (Exception e) {
                            trigval = arcGetTrigVal(s.substring(c + 5, i + 1));
                            steps++;
                        }
                        val = Math.atan(trigval);
                        break;
                }
            }
        }
        return val;
    }

    private String parseLog(String s) {
        StringBuilder new_str = new StringBuilder();
        double logval;
        for (int i = 0; i < s.length(); i++) {
            String s1 = String.valueOf(s.charAt(i));

            if (s1.equals("l")) {
                String s2 = String.valueOf(s.charAt(i + 1));
                int c;
                switch (s2) {
                    case "n":
                        c = i;
                        while (!String.valueOf(s.charAt(i)).equals(")")) {
                            i++;
                        }
                        try {
                            logval = Double.parseDouble(initialize(s.substring(c + 3, i)));
                        } catch (Exception e) {
                            logval = getLog(s.substring(c + 3, i + 1));
                            i += steps;
                            steps = 0;
                        }
                        new_str.append(decimalFormat.format(Math.log(logval)));
                        break;
                    case "o":
                        c = i;
                        while (!String.valueOf(s.charAt(i)).equals(")")) {
                            i++;
                        }
                        try {
                            logval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                        } catch (Exception e) {
                            logval = getLog(s.substring(c + 4, i + 1));
                            i += steps;
                            steps = 0;
                        }

                        new_str.append(decimalFormat.format(Math.log10(logval)));
                        break;
                    default:
                        new_str.append(s.charAt(i));
                        break;
                }
            } else {
                new_str.append(s.charAt(i));
            }
        }
        return new_str.toString();
    }

    private double getLog(String s) {
        double val = 0;
        double logval;
        for (int i = 0; i < s.length(); i++) {
            String s1 = String.valueOf(s.charAt(i));
            if (s1.equals("l")) {
                String s2 = String.valueOf(s.charAt(i + 1));
                int c;
                switch (s2) {
                    case "n":
                        c = i;
                        while (!String.valueOf(s.charAt(i)).equals(")")) {
                            i++;
                        }
                        try {
                            logval = Double.parseDouble(initialize(s.substring(c + 3, i)));
                            steps++;
                        } catch (Exception e) {
                            logval = Double.parseDouble((String.valueOf(getLog(s.substring(c + 3, i + 1)))));
                            steps++;
                        }
                        val = Math.log(logval);
                        break;
                    case "o":
                        c = i;
                        while (!String.valueOf(s.charAt(i)).equals(")")) {
                            i++;
                        }
                        try {
                            logval = Double.parseDouble(initialize(s.substring(c + 4, i)));
                            steps++;
                        } catch (Exception e) {
                            logval = getLog(s.substring(c + 4, i + 1));
                            steps++;
                        }
                        val = Math.log10(logval);
                        break;
                }
            }
        }
        return val;
    }


    public List<String> parseEquation(List<String> str) {
        List<String> ops = Arrays.asList("(", "^", "*", "/", "+", "-");
        for (String o : ops) {
            while (str.contains(o)) {
                for (int i = 0; i < str.size(); i++) {
                    if (str.get(i).equals(o)) {
                        double x;
                        switch (o) {
                            case ("("):
                                int n = str.indexOf(")");
                                str.set(i, parseEquation(str.subList(i + 1, str.indexOf(")"))).get(0));
                                str.subList(i + 1, n).clear();
                                str.remove(")");
                                str.add("x");
                                break;
                            case ("^"):
                                x = Math.pow(Double.parseDouble(str.get(i - 1)), Double.parseDouble(str.get(i + 1)));
                                str.set(i - 1, String.valueOf(x));
                                str.remove(i + 1);
                                str.remove(i);
                                str.add("x");
                                str.add("x");
                                break;
                            case ("*"):
                                x = Double.parseDouble(str.get(i - 1)) * Double.parseDouble(str.get(i + 1));
                                str.set(i - 1, String.valueOf(x));
                                str.remove(i + 1);
                                str.remove(i);
                                str.add("x");
                                str.add("x");
                                break;
                            case ("/"):
                                x = Double.parseDouble(str.get(i - 1)) / Double.parseDouble(str.get(i + 1));
                                str.set(i - 1, String.valueOf(x));
                                str.remove(i + 1);
                                str.remove(i);
                                str.add("x");
                                str.add("x");
                                break;
                            case ("+"):

                                x = Double.parseDouble(str.get(i - 1)) + Double.parseDouble(str.get(i + 1));
                                str.set(i - 1, String.valueOf(x));
                                str.remove(i + 1);
                                str.remove(i);
                                str.add("x");
                                str.add("x");
                                break;
                            case ("-"):
                                x = Double.parseDouble(str.get(i - 1)) - Double.parseDouble(str.get(i + 1));
                                str.set(i - 1, String.valueOf(x));
                                str.remove(i + 1);
                                str.remove(i);
                                str.add("x");
                                str.add("x");
                                break;
                        }
                    }
                }
            }
        }
        return str;
    }
}

