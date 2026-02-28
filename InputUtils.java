import java.util.Scanner;
import java.util.function.*;
import java.lang.Double;
public class InputUtils {

    static <T extends Number> T readNumberAndRetry(Scanner in,String prompt,String errorPrompt,Function<String,T> parser,T minRange,T maxRange){
        System.out.print(prompt);
        while (true){
            try{
                return readNumber(in,prompt,errorPrompt,parser,minRange,maxRange);
            } catch (InvalidInputException e){
                System.out.print(e.getMessage());
            }
        }
    }

    static <T extends Number> T readNumber(Scanner in,String prompt,String errorPrompt,Function<String,T> parser,T minRange,T maxRange) throws InvalidInputException{

        try {
            
            T value = parser.apply(in.nextLine());
            if (value.doubleValue() < minRange.doubleValue() || value.doubleValue()>maxRange.doubleValue()){
                throw new InvalidInputException(errorPrompt);
            }
            return value;
        } catch (NumberFormatException e){
            throw new InvalidInputException(errorPrompt);
        }

    }

    static int readIntAndRetry(Scanner in,String prompt,String errorPrompt,int minRange,int maxRange){
        return readNumberAndRetry(in, prompt, errorPrompt, Integer::parseInt, minRange, maxRange);
    }

    static int readInt(Scanner in,String prompt,String errorPrompt,int minRange,int maxRange) throws InvalidInputException{
        //in the signature of the method, we put throws InvalidInputException. This way, we 
        //force the caller to handle it
        return readNumber(in, prompt, errorPrompt, Integer::parseInt, minRange, maxRange);
    }



    static double readDoubleAndRetry(Scanner in,String prompt,String errorPrompt,double minRange,double maxRange){
        return  readNumberAndRetry(in, prompt, errorPrompt, Double::parseDouble, minRange, maxRange);
    }

    static double readDouble(Scanner in,String prompt,String errorPrompt,double minRange,double maxRange) throws InvalidInputException{
        //in the signature of the method, we put throws InvalidInputException. This way, we 
        //force the caller to handle it
        return readNumber(in, prompt, errorPrompt, Double::parseDouble, minRange, maxRange);
    }

    static char checkChar(String input,char min,char max){
            
        if (input.length()!=1){
            throw new IllegalArgumentException("Enter a single letter");
        }

        char c = input.charAt(0);
        if (c<min||c>max){
            String s = String.format("Enter a character in the range %c-%c",min,max);
            throw new IllegalArgumentException(s);
        }

        return c;     
    }
}

class InvalidInputException extends Exception{ //custom exception we throw when an invalid inpur is introduced
    public InvalidInputException(String message){
        super(message);
    }
}
