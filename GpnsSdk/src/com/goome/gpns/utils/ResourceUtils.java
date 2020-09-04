package com.goome.gpns.utils;

import android.content.Context;

public class ResourceUtils {
    
    public static int getIdByName(Context context, String className, String name) {
        String packageName = context.getPackageName();
        int id = -1;
        try {
            Class r = Class.forName(packageName + ".R");
            Class[] e = r.getClasses();
            Class desireClass = null;

            for(int i = 0; i < e.length; ++i) {
                if(e[i].getName().split("\\$")[1].equals(className)) {
                    desireClass = e[i];
                    break;
                }
            }

            if(desireClass != null) {
                id = desireClass.getField(name).getInt(desireClass);
            }
        } catch (ClassNotFoundException var9) {
            var9.printStackTrace();
        } catch (IllegalArgumentException var10) {
            var10.printStackTrace();
        } catch (SecurityException var11) {
            var11.printStackTrace();
        } catch (IllegalAccessException var12) {
            var12.printStackTrace();
        } catch (NoSuchFieldException var13) {
            var13.printStackTrace();
        }

        return id;
    }

}
