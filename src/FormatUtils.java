import java.util.Set;

public final class FormatUtils {

    private FormatUtils(){};
    
    public static String musclesToSaveFormat(Set<Muscles> m){
        //stream means go through each of the elements of the set
        //map just returns a function value for each element
        //reduce "reduces" it all to a string
        return m.stream().map(Enum::name).reduce((a,b)->a+";"+b).orElse("");
    }

    public static String valueAfterColon(String line){
        int idx = line.indexOf(":");
        return (idx>=0) ? line.substring(idx+1) : "";
    }

    



}
