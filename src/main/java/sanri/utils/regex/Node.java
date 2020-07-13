package sanri.utils.regex;


import sanri.utils.regex.exception.RegexpIllegalException;
import sanri.utils.regex.exception.TypeNotMatchException;
import sanri.utils.regex.exception.UninitializedException;

public interface Node {

    String getExpression();

    String random() throws UninitializedException, RegexpIllegalException;

    boolean test();

    void init() throws RegexpIllegalException, TypeNotMatchException;

    boolean isInitialized();
}
