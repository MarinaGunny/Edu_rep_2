import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

public class InvokeHandler implements InvocationHandler {
    private Object obj;
    private boolean isMutated;      //Значимые поля были изменены
    private Object retCachedObj;         // Возвращаемое кэшированное значение

    InvokeHandler(Object obj) {
        this.obj = obj;
        this.isMutated = true;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //Входящий method не брать! Он от интерфейса и аннотаций там нет.
        Method myMethod = obj.getClass().getMethod(method.getName(), method.getParameterTypes());

        //Проход по аннотациям. Отбираем те что нас интересуют, вдруг их там много разных
        for (Annotation a : Arrays.stream(myMethod.getDeclaredAnnotations()).filter(x->x.annotationType().equals(Mutator.class)||x.annotationType().equals(Cache.class) ).toList()) {
            if (a.annotationType() == Mutator.class) {
                isMutated = true;
            }
            if (a.annotationType() == Cache.class ) {
                if (isMutated)
                    isMutated = false;  //Надо пересчитать ниже и сбросить признак
                else
                    return retCachedObj;  //Надо вернуть старое
            }
        }
        retCachedObj = method.invoke(obj, args);
        return retCachedObj;
    }
}
