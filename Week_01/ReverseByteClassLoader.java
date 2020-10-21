package luyz.geektime.java.jvm;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author : Luyz
 * @date : 2020/10/21 01:28
 */
public class ReverseByteClassLoader extends ClassLoader{
    public static void main(String[] args) {
        try {
            Class<?> helloClass = new ReverseByteClassLoader().findClass("Hello");
            Object helloObject = helloClass.newInstance();
            Method helloMethod = helloClass.getMethod("hello");
            helloMethod.invoke(helloObject);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = readClassFile(name);
        return defineClass(name,bytes,0,bytes.length);
    }

    private byte[] readClassFile(String name) throws ClassNotFoundException {
        byte[] classByte = new byte[0];
        File file = new File("/Users/Luyz/Documents/learn/geektime/JAVA-000/PlayGround/Hello/Hello.xlass");
        if (!file.exists()) {
            throw new ClassNotFoundException();
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int len = fileInputStream.read(buf);
            classByte = new byte[len];
            for (int i = 0; i < len; i++) {
                classByte[i] = (byte) (255 - buf[i]);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
        return classByte;
    }
}
