package jahuwaldt.swing;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: scott.steinhorst
 * Date: 4/12/12
 * Time: 5:13 PM
 */
public class GenericItemListener implements ItemListener {
    
    private final Object target;
    
    private final Method targetMethod;
    
    public GenericItemListener (Object target, Method targetMethod ) {
        super();

        this.target = target;
        this.targetMethod = targetMethod;
    }
    @Override
    public void itemStateChanged(ItemEvent event) {
        try {
            targetMethod.invoke( target, new Object []{ event } );
        } catch( IllegalAccessException e ) {
            e.printStackTrace();
        } catch( InvocationTargetException e ) {
            e.printStackTrace();
        }
    }
}
