package algebradebool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class GenTVerdad {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Expresión: ");
        String expresion = scanner.nextLine();
        generarTVerdad(expresion);

        scanner.close();
    }

    public static void generarTVerdad(String expresion) {
        Set<Character> variables = new HashSet<>();
        for (char c : expresion.toCharArray()) {
            if (Character.isLetter(c)) {
                variables.add(c);
            }
        }

        int numVariables = variables.size();

        if (numVariables <= 0) {
            System.out.println("La expresión no contiene variables.");
            return;
        }

        int numRows = (int) Math.pow(2, numVariables);
        Map<Character, Boolean> variableMap = new HashMap<>();
        String encabezado = genEncabezado(expresion, numVariables);

        System.out.println(encabezado);

        for (int row = 0; row < numRows; row++) {
            String binaryRow = Integer.toBinaryString(row);
            while (binaryRow.length() < numVariables) {
                binaryRow = "0" + binaryRow;
            }

            for (int j = 0; j < numVariables; j++) {
                char variable = (char) ('A' + j);
                variableMap.put(variable, binaryRow.charAt(j) == '1');
                System.out.print(binaryRow.charAt(j) + "\t");
            }

            Stack<Boolean> pasoAPaso = new Stack<>();
            boolean resultado = evalExpresionPasoAPaso(expresion, variableMap, pasoAPaso);

            for (boolean paso : pasoAPaso) {
                System.out.print((paso ? "1" : "0") + "\t");
            }

            System.out.print("| " + (resultado ? "1" : "0") + "\t");
            System.out.println();

        }
        buscarCombEspecifica(expresion, variableMap);
    }

    public static String genEncabezado(String expresion, int numVariables) {
        StringBuilder header = new StringBuilder();

        // Agregar las variables al encabezado
        for (char variable = 'A'; variable < 'A' + numVariables; variable++) {
            header.append(variable).append("\t");
        }
        header.append("\t\t");
        // Agregar las subexpresiones al encabezado
        for (int i = 0; i < expresion.length(); i++) {
            if (expresion.charAt(i) == '(') {
                // Encontrar el índice del paréntesis de cierre correspondiente
                int endIndex = expresion.indexOf(')', i);
                if (endIndex == -1) {
                    throw new RuntimeException("Expresión mal formada: falta un paréntesis de cierre");
                }

                // Agregar la subexpresión al encabezado
                String subexpresion = expresion.substring(i, endIndex + 1);
                header.append(subexpresion).append("\t");

                // Saltar al final de la subexpresión
                i = endIndex;
            }
        }

        // Agregar la expresión completa al final del encabezado
        header.append("| ").append(expresion);

        return header.toString();
    }

    //#=XOR - $=XNOR
    public static boolean evalExpresionPasoAPaso(String expresion, Map<Character, Boolean> variableMap, Stack<Boolean> pasoAPaso) {
        Stack<Character> operadores = new Stack<>();
        Stack<Boolean> operandos = new Stack<>();

        for (char c : expresion.toCharArray()) {
            if (Character.isWhitespace(c)) {
                continue;
            }

            if (c == '(') {
                operadores.push(c);
            } else if (c == ')') {
                while (!operadores.isEmpty() && operadores.peek() != '(') {
                    char operador = operadores.pop();
                    if (operador == '+' || operador == '*' || operador == '#' || operador == '$') {
                        boolean operando2 = operandos.pop();
                        boolean operando1 = operandos.pop();
                        boolean resultado = aplicarOp(operador, operando1, operando2);
                        operandos.push(resultado);
                        pasoAPaso.push(resultado);
                    } else if (operador == '!') {
                        boolean operando = operandos.pop();
                        boolean resultado = !operando;
                        operandos.push(resultado);
                        pasoAPaso.push(resultado);
                    }
                }
                operadores.pop(); // Elimina el paréntesis izquierdo
            } else if (c == '+' || c == '*' || c == '!' || c == '#' || c == '$') {
                while (!operadores.isEmpty() && prece(c) <= prece(operadores.peek())) {
                    char operador = operadores.pop();
                    if (operador == '+' || operador == '*' || operador == '#' || operador == '$') {
                        boolean operando2 = operandos.pop();
                        boolean operando1 = operandos.pop();
                        boolean resultado = aplicarOp(operador, operando1, operando2);
                        operandos.push(resultado);
                        pasoAPaso.push(resultado);
                    } else if (operador == '!') {
                        boolean operando = operandos.pop();
                        boolean resultado = !operando;
                        operandos.push(resultado);
                        pasoAPaso.push(resultado);
                    }
                }
                operadores.push(c);
            } else {
                // Variable si no hay () o +-*
                operandos.push(variableMap.get(c));
                pasoAPaso.push(variableMap.get(c));
            }
        }

        while (!operadores.isEmpty()) {
            char operador = operadores.pop();
            if (operador == '+' || operador == '*' || operador == '#' || operador == '$') {
                boolean operando2 = operandos.pop();
                boolean operando1 = operandos.pop();
                boolean resultado = aplicarOp(operador, operando1, operando2);
                operandos.push(resultado);
                pasoAPaso.push(resultado);
            } else if (operador == '!') {
                boolean operando = operandos.pop();
                boolean resultado = !operando;
                operandos.push(resultado);
                pasoAPaso.push(resultado);
            }
        }

        return operandos.pop();
    }

    public static boolean aplicarOp(char operador, boolean operando1, boolean operando2) {
        if (operador == '+') {
            return operando1 || operando2;
        } else if (operador == '*') {
            return operando1 && operando2;
        } else if (operador == '#') {
            if (operando1 == operando2) {
                return false;
            } else {
                return true;
            }
        } else if (operador == '$') {
            if (operando1 == operando2) {
                return true;
            } else {
                return false;
            }
        } else if (operador == '!') {
            return !operando1;
        }
        return false; // Operador desconocido
    }

    public static int prece(char operador) {
        if (operador == '#' || operador == '$') {
            return 1;
        } else if (operador == '+' || operador == '!') {
            return 2;
        } else if (operador == '*') {
            return 3;
        }
        return 0; // Operador desconocido
    }

    public static void buscarCombEspecifica(String expresion, Map<Character, Boolean> variableMap) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Introduce una combinación de valores (por ejemplo, 101 para A=true, B=false, C=true): ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("salir")) {
                break;
            }

            if (input.length() != variableMap.size()) {
                System.out.println("La longitud de la entrada no coincide con el número de variables.");
                continue;
            }

            boolean validInput = true;

            for (int i = 0; i < input.length(); i++) {
                char variable = (char) ('A' + i);
                boolean valor = input.charAt(i) == '1';
                variableMap.put(variable, valor);

                if (valor) {
                    System.out.println(variable + "=true");
                } else {
                    System.out.println(variable + "=false");
                }
            }

            Stack<Boolean> pasoAPaso = new Stack<>();
            boolean resultado = evalExpresionPasoAPaso(expresion, variableMap, pasoAPaso);

            System.out.println("Resultado de la expresión: " + (resultado ? "1" : "0"));
            System.out.println("Pasos intermedios:");
            for (boolean paso : pasoAPaso) {
                System.out.print((paso ? "1" : "0") + "\t");
            }

            System.out.println();
        }

        scanner.close();
    }

}
