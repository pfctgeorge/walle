package jobs;

import com.google.inject.Inject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import models.R;
import play.Play;
import play.classloading.ApplicationClasses;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Controller;

/**
 * Created by pfctgeorge on 17/1/22.
 */
@OnApplicationStart
public class BootstrapJob extends Job {

    public void doJob() {
        try {
            List<ApplicationClasses.ApplicationClass> applicationClasses = Play.classes.getAssignableClasses(Controller.class);
            for (ApplicationClasses.ApplicationClass applicationClass : applicationClasses) {
                Field[] fields = applicationClass.javaClass.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Inject.class) && Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        Class<?> fieldClazz = field.getType();
                        Object fieldObj = R.inject.getInstance(fieldClazz);
                        field.set(null, fieldObj);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
