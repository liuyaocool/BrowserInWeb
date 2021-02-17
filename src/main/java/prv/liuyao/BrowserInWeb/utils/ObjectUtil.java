package prv.liuyao.BrowserInWeb.utils;

import java.util.Collection;
import java.util.Map;

public abstract class ObjectUtil {

    private static final String[] RESULT = {"null","int", "double"};

    //判断是否是数字
    private static String isNumber(Object obj){
        if (isEmpty(obj)){ return RESULT[0]; }
        char[] aa = (String.valueOf(obj)).toCharArray();
        if (aa.length == 0) { return RESULT[0]; }
        int point = 0;
        String num = "0123456789";
        for (int i = 0; i < aa.length; i++) {
            if ('.' == aa[i]){
                point++;
                continue;
            }
            if (num.indexOf(aa[i]) < 0){
                return RESULT[0];
            }
        }
        switch (point){
            case 0: return RESULT[1];
            case 1: return RESULT[2];
            default: return RESULT[0];
        }
    }

    /**
     *
     * @param obj
     * @param fix 0：舍小数点 1：小数点进1
     * @return
     */
    public static Integer parseInt(String obj, int fix){
        switch (isNumber(obj)){
            case "null": return null;
            case "int": return Integer.valueOf(String.valueOf(obj));
            case "double":
                return Double.parseDouble(String.valueOf(obj)) % 1 > 0
                        ? removePoint(obj) + fix : removePoint(obj);
            default: return null;
        }
    }

    private static int removePoint(String obj){
        return Integer.parseInt(obj.substring(0, obj.indexOf(".")));
    }

    public static double doubleFormat(String number, int points){
        return Double.valueOf(String.format("%."+points+"f", Double.parseDouble(number)));
    }

    public static boolean equalsIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            String[] var2 = strs;
            for(int i = 0; i < var2.length; ++i) {
                String s = var2[i];
                if (str.equalsIgnoreCase(trim(s))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    public static boolean isEmpty(Object obj){
        if (null == obj){
            return true;
        }else if (obj instanceof Collection){
            return ((Collection) obj).size() == 0;
        } else if (obj instanceof String || obj instanceof Number || obj instanceof Character){
            return String.valueOf(obj).isEmpty();
        }else if (obj.getClass().isArray()){
            return ((Object[]) obj).length == 0;
        }else if (obj instanceof Map) {
            return ((Map) obj).size() == 0;
        }else if (obj instanceof Boolean){
            return !(Boolean) obj;
        }else{
//            Class clazz = obj.getClass();
//            Field[] fields = clazz.getDeclaredFields();
//            boolean ifEmpty = true;
//            Object bean = null;
//            for (int i = 0; i < fields.length; i++) {
//                try {
//                    bean = bean == null ? clazz.getConstructor().newInstance() : bean;
//                    System.out.println(fields[i].getName());
//                    PropertyDescriptor pd = new PropertyDescriptor(fields[i].getName(), clazz);
//                    Method method = pd.getReadMethod();
//                    method.setAccessible(true);
//                    Object attrVal = method.invoke(bean);
//                    if (!isEmpty(attrVal)){
//                        ifEmpty = false;
//                    }
//                } catch (IntrospectionException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                } catch (InstantiationException e) {
//                    e.printStackTrace();
//                } catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
            return false;
        }
    }
}
